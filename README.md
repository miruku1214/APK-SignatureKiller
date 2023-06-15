# Disclaimer
- This content is intended to increase your knowledge of Java, security, and APK structure. If you use this in someone else's app or game and run into trouble with the law or face data loss, the developer cannot be held responsible, so please use at your own risk.

# Warning.
- The size of the APK will be doubled after application.
- After opening the modded APK, free storage of the original APK size will be consumed.

# Required environment
- Java (for checking the signature data of the original APK)
- Android Studio or AIDE (for building the APK)

# Injection method
- Follow the how-to-use_en.txt in the APKSignReader folder, use APKSignReader.jar to read the signature data of the original APK, and then add it to the `Hook` method of the `com.SignatureKiller.Main` class in the Java source code. Main` method of the Java source code.
- Build with Android Studio or AIDE.
- Add the DEX file in the built APK to the modded APK.
- Rename the original APK to orig.apk and copy it to the assets folder of the modded APK. (If the folder does not exist, create it.)
- Paste the following smali code at the top of the entry point method where this is a class object that extends Context. (onCreate, attachBaseContext, etc.)
```smali
invoke-static {p0}, Lcom/SignatureKiller/Main;->Hook(Landroid/content/Context;)V
````
- After signing and installing the modded APK, make sure it passes signature authentication.
