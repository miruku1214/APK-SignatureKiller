package com.SignatureKiller;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Map;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Main {
	private static final int GET_SIGNATURES = 0x40;
	
	
	public static String CalcSHA256(InputStream is) throws IOException, NoSuchAlgorithmException {
		String output;
		int read;
		byte[] buffer = new byte[8192];
		
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		while ((read = is.read(buffer)) > 0) {
			digest.update(buffer, 0, read);
		}
		byte[] hash = digest.digest();
		BigInteger bigInt = new BigInteger(1, hash);
		output = bigInt.toString(16);
		while (output.length() < 64) {
			output = "0" + output;
		}

		return output;
	}
	
	public static void Hook(final Context context) {
		final String ApkPath = "orig.apk";
		final String SignData = "#### PASTE SIGN DATA HERE ####";
		
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(Base64.decode(SignData, Base64.DEFAULT)));
			final byte[][] sign = new byte[(dis.read() & 0xFF)][];
			for (int i = 0; i < sign.length; i++) {
				sign[i] = new byte[dis.readInt()];
				dis.readFully(sign[i]);
			}
			
			File filesDir = context.getFilesDir();
			filesDir.mkdirs();
			
			final File apkCopy = new File(filesDir, ApkPath);
			
			boolean copyOrReplace = false;
			if (!apkCopy.exists()) {
				copyOrReplace = true;
			} else {
				InputStream apkOrigInput = context.getAssets().open(ApkPath);
				InputStream apkCopyInput = new FileInputStream(apkCopy);
				
				int apkOrigSize = apkOrigInput.available();
				int apkCopySize = apkCopyInput.available();
				
				if (apkOrigSize != apkCopySize) {
					copyOrReplace = true;
				} else {
					String apkOrigHash = CalcSHA256(apkOrigInput);
					String apkCopyHash = CalcSHA256(apkCopyInput);
					
					if (!apkOrigHash.equals(apkCopyHash)) {
						copyOrReplace = true;
					}
				}
				
				apkOrigInput.close();
				apkCopyInput.close();
			}
			if (copyOrReplace) {
				if (apkCopy.exists()) {
					apkCopy.delete();
				}
				InputStream apkOrigInput = context.getAssets().open(ApkPath);
				OutputStream apkCopyOutput = new FileOutputStream(apkCopy);
				byte[] buf = new byte[8192];
				int read = 0;
				while ((read = apkOrigInput.read(buf, 0, buf.length)) != -1) {
					apkCopyOutput.write(buf, 0, read);
					apkCopyOutput.flush();
				}
				apkOrigInput.close();
				apkCopyOutput.close();
			}
			
			Class<?> ActivityThreadC = Class.forName("android.app.ActivityThread");
			Method currentActivityThreadM = ActivityThreadC.getDeclaredMethod("currentActivityThread");
			Object currentActivityThread = currentActivityThreadM.invoke(null);
			
			Field mPackagesF = currentActivityThread.getClass().getDeclaredField("mPackages");
			mPackagesF.setAccessible(true);
			Object mPackages = mPackagesF.get(currentActivityThread);
			
			Object loadedApk = ((Map<String, WeakReference<?>>) mPackages).get(context.getPackageName()).get();
			
			Field mAppDirF = loadedApk.getClass().getDeclaredField("mAppDir");
			mAppDirF.setAccessible(true);
			mAppDirF.set(loadedApk, apkCopy.getAbsolutePath());
			
			Field mApplicationInfoF = loadedApk.getClass().getDeclaredField("mApplicationInfo");
			mApplicationInfoF.setAccessible(true);
			Object mApplicationInfo = mApplicationInfoF.get(loadedApk);
			
			ApplicationInfo applicationInfo = (ApplicationInfo) mApplicationInfo;
			applicationInfo.sourceDir = apkCopy.getAbsolutePath();
			applicationInfo.publicSourceDir = apkCopy.getAbsolutePath();
			
			Field sPackageManagerF = ActivityThreadC.getDeclaredField("sPackageManager");
			sPackageManagerF.setAccessible(true);
			final Object sPackageManager = sPackageManagerF.get(currentActivityThread);
			
			Class<?> IPackageManagerC = Class.forName("android.content.pm.IPackageManager");
			Object mPMProxy = Proxy.newProxyInstance(IPackageManagerC.getClassLoader(), new Class[] {IPackageManagerC}, new InvocationHandler() {
				@Override 
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					switch (method.getName()) {
						case "getPackageInfo": {
							String packageName = (String) args[0];
							Integer flag = (Integer) args[1];
							
							if (packageName.equals(context.getPackageName())) {
								PackageInfo info = (PackageInfo) method.invoke(sPackageManager, args);
								if ((flag & GET_SIGNATURES) != 0) {
									info.signatures = new Signature[sign.length];
									for (int i = 0; i < info.signatures.length; i++) {
										info.signatures[i] = new Signature(sign[i]);
									}
								}
								info.applicationInfo.sourceDir = apkCopy.getAbsolutePath();
								info.applicationInfo.publicSourceDir = apkCopy.getAbsolutePath();
								return info;
							}
						} break;
						case "getPackageArchiveInfo": {
							String archivePath = (String) args[0];
							Integer flag = (Integer) args[1];
							
							if (archivePath.equals(context.getApplicationInfo().sourceDir)) {
								PackageInfo info = (PackageInfo) method.invoke(sPackageManager, args);
								if ((flag & GET_SIGNATURES) != 0) {
									info.signatures = new Signature[sign.length];
									for (int i = 0; i < info.signatures.length; i++) {
										info.signatures[i] = new Signature(sign[i]);
									}
								}
								info.applicationInfo.sourceDir = apkCopy.getAbsolutePath();
								info.applicationInfo.publicSourceDir = apkCopy.getAbsolutePath();
								return info;
							}
						} break;
					}
					return method.invoke(sPackageManager, args);
				}
			});
			sPackageManagerF.set(currentActivityThread, mPMProxy);
			
			PackageManager pm = context.getPackageManager();
			
			Field mPMF = pm.getClass().getDeclaredField("mPM");
			mPMF.setAccessible(true);
			mPMF.set(pm, mPMProxy);
			
			Log.i("APK-SignatureKiller Core (" + context.getPackageName() + ")", "Hook Success");
		} catch (
			IOException
			| NoSuchAlgorithmException
			| ClassNotFoundException
			| NoSuchFieldException
			| NoSuchMethodException
			| InvocationTargetException
			| IllegalAccessException ex
		) {
			Log.e("APK-SignatureKiller Core (" + context.getPackageName() + ")", "Hook Failed (" + ex.getMessage() + ")");
			ex.printStackTrace();
		}
	}
}
