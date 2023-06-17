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
import java.util.zip.CRC32;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Main {
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
		final String SignData = "### PASTE SIGN DATA HERE ###";
		
		try {
			final File apkCopy = context.getFileStreamPath(ApkPath);
			boolean copy = false;
			if (apkCopy.exists()) {
				InputStream apkOrigInput = context.getAssets().open(ApkPath);
				InputStream apkCopyInput = new FileInputStream(apkCopy);
				
				int apkOrigSize = apkOrigInput.available();
				int apkCopySize = apkCopyInput.available();
				
				String apkOrigHash = CalcSHA256(apkOrigInput);
				String apkCopyHash = CalcSHA256(apkCopyInput);
				
				apkOrigInput.close();
				apkCopyInput.close();
				
				if ((apkOrigSize != apkCopySize) || !(apkOrigHash.equals(apkCopyHash))) {
					apkCopy.delete();
					copy = true;
				}
			} else {
				copy = true;
			}
			if (copy) {
				InputStream apkOrigInput = context.getAssets().open(ApkPath);
				OutputStream apkCopyOutput = new FileOutputStream(apkCopy);
				byte[] buf = new byte[4096];
				int bytesRead = 0;
				while ((bytesRead = apkOrigInput.read(buf, 0, buf.length)) != -1) {
					apkCopyOutput.write(buf, 0, bytesRead);
					apkCopyOutput.flush();
				}
				apkOrigInput.close();
				apkCopyOutput.close();
			}
			
			Field sCurrentActivityThreadF = ClassLoader.getSystemClassLoader().loadClass("android.app.ActivityThread").getDeclaredField("sCurrentActivityThread");
			sCurrentActivityThreadF.setAccessible(true);
			Object sCurrentActivityThread = sCurrentActivityThreadF.get(null);
			Field mPackagesF = sCurrentActivityThread.getClass().getDeclaredField("mPackages");
			mPackagesF.setAccessible(true);
			Object mPackages = ((WeakReference) ((Map) mPackagesF.get(sCurrentActivityThread)).get(context.getPackageName())).get();
			Field mAppDirF = mPackages.getClass().getDeclaredField("mAppDir");
			mAppDirF.setAccessible(true);
			mAppDirF.set(mPackages, apkCopy.getAbsolutePath());
			Field mApplicationInfoF = mPackages.getClass().getDeclaredField("mApplicationInfo");
			mApplicationInfoF.setAccessible(true);
			Object mApplicationInfo = mApplicationInfoF.get(mPackages);
			ApplicationInfo applicationInfo = (ApplicationInfo) mApplicationInfo;
			applicationInfo.publicSourceDir = apkCopy.getAbsolutePath();
			applicationInfo.sourceDir = apkCopy.getAbsolutePath();
			
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(Base64.decode(SignData, 0)));
			final byte[][] originalSigns = new byte[(dis.read() & 0xFF)][];
			for (int i = 0; i < originalSigns.length; i++) {
				originalSigns[i] = new byte[dis.readInt()];
				dis.readFully(originalSigns[i]);
			}
			Class<?> ActivityThreadC = Class.forName("android.app.ActivityThread");
			Object currentActivityThread = ActivityThreadC.getDeclaredMethod("currentActivityThread").invoke(null);
			Field sPackageManagerF = ActivityThreadC.getDeclaredField("sPackageManager");
			sPackageManagerF.setAccessible(true);
			final Object sPackageManager = sPackageManagerF.get(currentActivityThread);
			Class<?> IPackageManagerC = Class.forName("android.content.pm.IPackageManager");
			final String packageName = context.getPackageName();
			Object mPMProxy = Proxy.newProxyInstance(IPackageManagerC.getClassLoader(), new Class[]{IPackageManagerC}, new InvocationHandler() {
				@Override 
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					PackageInfo info;
					String methodName = method.getName();
					if (methodName.equals("getPackageInfo") && (args[0] instanceof String) && (args[0].toString() == packageName) && ((info = (PackageInfo) method.invoke(sPackageManager, args)) != null)) {
						if (info.signatures != null) {
							info.signatures = new Signature[originalSigns.length];
							for (int i = 0; i < info.signatures.length; i++) {
								info.signatures[i] = new Signature(originalSigns[i]);
							}
						}
						if (info.applicationInfo != null) {
							info.applicationInfo.sourceDir = apkCopy.getAbsolutePath();
							info.applicationInfo.publicSourceDir = apkCopy.getAbsolutePath();
						}
						return info;
					} else if (!methodName.equals("getApplicationInfo") || !(args[0] instanceof String) || args[0].toString() != packageName) {
						if (methodName.equals("getPackageArchiveInfo") && (args[0] instanceof String) && args[0].toString().endsWith(".apk") && (args[0].toString() == packageName)) {
							args[0] = apkCopy.getAbsolutePath();
						}
						return method.invoke(sPackageManager, args);
					} else {
						ApplicationInfo appInfo = (ApplicationInfo) method.invoke(sPackageManager, args);
						if (appInfo != null) {
							appInfo.sourceDir = apkCopy.getAbsolutePath();
							appInfo.publicSourceDir = apkCopy.getAbsolutePath();
						}
						return appInfo;
					}
				}
			});
			sPackageManagerF.set(currentActivityThread, mPMProxy);
			PackageManager pm = context.getPackageManager();
			Field mPMF = pm.getClass().getDeclaredField("mPM");
			mPMF.setAccessible(true);
			mPMF.set(pm, mPMProxy);
			
			Log.i("SignatureKiller", "Hook Success (" + context.getPackageName() + ")");
		} catch (IOException | NoSuchAlgorithmException | ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
			Log.e("SignatureKiller", "Hook Failed (" + context.getPackageName() + ")");
			ex.printStackTrace();
		}
	}
}
