package com.SignatureKiller;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
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


public class Main {
	private static long GetCRC32(final InputStream is) throws IOException {
		CRC32 crc32 = new CRC32();
		byte[] buf = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = is.read(buf)) != -1) {
			crc32.update(buf, 0, bytesRead);
		}
		return crc32.getValue();
	}
	
	public static void Hook(final Context context) {
		final String ApkPath = "orig.apk";
		final String SignData = "AQAABYswggWHMIIDb6ADAgECAhUAtX6C6eRGo8pmgh0BaDFOsSH9MdIwDQYJKoZIhvcNAQELBQAwdDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDU1vdW50YWluIFZpZXcxFDASBgNVBAoTC0dvb2dsZSBJbmMuMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMB4XDTE5MDcwNTE4Mjg0OVoXDTQ5MDcwNTE4Mjg0OVowdDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDU1vdW50YWluIFZpZXcxFDASBgNVBAoTC0dvb2dsZSBJbmMuMRAwDgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAzdoVNB4NKNodQ/cbfTXbJg+4bomxXvU8AxW8uNNgYaNkk/YVOzUBeEqyY774A+ul0VrCrz0jt+mZ9KvgkOflJGDxvMkMUZQ5WW3ZNviOaNtZf3bxJtxP4mN8fSz/uLgMXOXaUD97nxaVYzaAp6ovhqZ6PRMQ+EdBEGT1xW7NR7YcRkPjCT6bcK4pd1UoDzwGmkYcvHQ3RgdWGafF/T1wvNTO05GZKMycMyV/I+c0NYcAKaDh8jzbQTzsBusnbCl7sPv9YwyYOzsfGN5PuucgRuP+ctErON53t0sJHDMcHulmHCIefAQaoiN4JLRnwBL/u0bMgRrT4CO0lW39jsE3/RFM4Zm1pP6VYAPr28Bd844+p8X5W8Hxd5p/xhsBTXU6Xdn3R8BI8MJ0+iTa+AKMYj1izuvJC6arocw0caEkSQLrExHps1bl76EWWEsIDnJxG+Qv4cSLX7G3bC/ae2ZZWY8/72uQgr49GM1gDFe4Nok04BLfHBqnvbJQVvX1HzbgPkYu6QC733EH6LiyqYo+WZDOI/CqAHe7mABabK/DqaYEJu1qYaaJsvtOu5bOyt4YDLdWnPCbClEsGrTiKpNtWwg3RjeK1bUhcsbjJYDLh2txsUF4fm/7c7vPnet8LWL3cJXXYgoGo1FGOEe1nF/0cTH2uiCoUJyu2+B7rRiZqJ8CAwEAAaMQMA4wDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEArqD/GdtzGUvNUPIFz5P6miDadg6V1Ro7VCUjrN4fY9LM2S/2dAtjBImNhyNToXOhflbN5ozC9AyKkk4Vrlz12d5qdbk7ImcX04lY37BNXX9kS5yUcG/1gE2M++F24byWhlh1oQQczmrpfB9NtnrILnlYq6DfjyNGZ6gfnih1ZLp0+QTIL8eCjK6dpMg8Woph2KZutw1s/yVG57lVlkn1Ri2+vPWNE+G1QPq5bUVk72OPXiEj7Akrv++yH3guoRU+M1uGY5Jkz/ecpz7a0oSk/2Mj40t3lXi4ojETzd2jphoh/IB9ZQHtL/09hDesqRWBFGiuon7xmLTPM4xn0/S8L8tRFaL0vBjcsZSAs01vkSj0k7QLrNyCe+6SJydUNRCDXxStGEAKfQcaHxbULcf0IZcF4/c9wIy62PQunm00I6k6aqznjm0H4n+fgL6S81u6seLH5+OhKgZ0PhRVT++mCyh9S3WWpNRWH98De+PheVmNRtJGHziGyjoSmZajwCA4CtGdYdtUZ6faIs1V8W8WMOB2YSOFbsBGApOHmepLISLyjwZ6I3gw+ycB5UtUwvWwAx9F+KuNXX3pNzmoHp+Fel3naqO3rfjEht6aya3X79HBTbJFkRUBfuBW8wfKmwExpUtLMtBH6/50cbHYQecL8leGqyxMYTdY0GcV6tY3lBA=";
		
		try {
			final File apkCopy = context.getFileStreamPath(ApkPath);
			boolean copy = false;
			if (apkCopy.exists()) {
				InputStream apkOrigInput = context.getAssets().open(ApkPath);
				InputStream apkCopyInput = new FileInputStream(apkCopy);
				
				final int apkOrigSize = apkOrigInput.available();
				final int apkCopySize = apkCopyInput.available();
				
				final long apkOrigHash = GetCRC32(apkOrigInput);
				final long apkCopyHash = GetCRC32(apkCopyInput);
				
				if ((apkOrigSize != apkCopySize) || (apkOrigHash != apkCopyHash)) {
					copy = true;
				}
				
				apkOrigInput.close();
				apkCopyInput.close();
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
			
			Field sCurrentActivityThreadField = ClassLoader.getSystemClassLoader().loadClass("android.app.ActivityThread").getDeclaredField("sCurrentActivityThread");
			sCurrentActivityThreadField.setAccessible(true);
			Object sCurrentActivityThread = sCurrentActivityThreadField.get(null);
			Field mPackagesField = sCurrentActivityThread.getClass().getDeclaredField("mPackages");
			mPackagesField.setAccessible(true);
			Object mPackages = ((WeakReference) ((Map) mPackagesField.get(sCurrentActivityThread)).get(context.getPackageName())).get();
			Field mAppDirField = mPackages.getClass().getDeclaredField("mAppDir");
			mAppDirField.setAccessible(true);
			mAppDirField.set(mPackages, apkCopy.getAbsolutePath());
			Field mApplicationInfoField = mPackages.getClass().getDeclaredField("mApplicationInfo");
			mApplicationInfoField.setAccessible(true);
			ApplicationInfo applicationInfo = (ApplicationInfo) mApplicationInfoField.get(mPackages);
			applicationInfo.publicSourceDir = apkCopy.getAbsolutePath();
			applicationInfo.sourceDir = apkCopy.getAbsolutePath();
			
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(Base64.decode(SignData, 0)));
			final byte[][] originalSigns = new byte[(dis.read() & 255)][];
			for (int i = 0; i < originalSigns.length; i++) {
				originalSigns[i] = new byte[dis.readInt()];
				dis.readFully(originalSigns[i]);
			}
			Class<?> ActivityThreadClass = Class.forName("android.app.ActivityThread");
			Object currentActivityThread = ActivityThreadClass.getDeclaredMethod("currentActivityThread").invoke(null);
			Field sPackageManagerField = ActivityThreadClass.getDeclaredField("sPackageManager");
			sPackageManagerField.setAccessible(true);
			final Object sPackageManager = sPackageManagerField.get(currentActivityThread);
			Class<?> IPackageManagerClass = Class.forName("android.content.pm.IPackageManager");
			final String packageName = context.getPackageName();
			Object mPMProxy = Proxy.newProxyInstance(IPackageManagerClass.getClassLoader(), new Class[]{IPackageManagerClass}, new InvocationHandler() {
				@Override 
				public Object invoke(Object mPMProxy, Method method, Object[] args) throws Throwable {
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
			sPackageManagerField.set(currentActivityThread, mPMProxy);
			PackageManager pm = context.getPackageManager();
			Field mPMField = pm.getClass().getDeclaredField("mPM");
			mPMField.setAccessible(true);
			mPMField.set(pm, mPMProxy);
			
			Log.i("SignatureKiller", "Hook Success (" + context.getPackageName() + ")");
		} catch (Exception e) {
			Log.e("SignatureKiller", "Hook Failed (" + context.getPackageName() + ")");
			e.printStackTrace();
		}
	}
}
