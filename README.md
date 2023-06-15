# Disclaimer
- This content was created for the purpose of deepening knowledge about Java, security, and APK structure. If you use this on someone else's app or game and encounter legal issues or data loss, the developer cannot take any responsibility, so please use it at your own risk.

# Warning
- After kill signature, doubles size of the APK.
- After opening the modded APK, the free storage space of the original APK's size will be consumed.

# Required Environment
- Java (for reading signature data of the original APK)
- Android Studio or AIDE (for building the core APK)

# Injection Method
- Follow the instructions in how-to-use.txt in the APKSignReader folder to use APKSignReader.jar to read the signature data of the original APK, and copy and paste it into the `SignData` variable in the `Hook` method of the `com.SignatureKiller.Main` class in the source code.
- Build with Android Studio or AIDE.
- Add the DEX file from the built APK to the modded APK.
- Rename the original APK to orig.apk and copy it to the assets folder of the modded APK (create the folder if it does not exist).
- Paste the below smali code at the top of the entry point method where `this(p0)` is a class object that inherits from Context (onCreate, attachBaseContext, etc.):
```smali
invoke-static {p0}, Lcom/SignatureKiller/Main;->Hook(Landroid/content/Context;)V
```
- After signing and installing the modded APK, check if it passes the signature check.
