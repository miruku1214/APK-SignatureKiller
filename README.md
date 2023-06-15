# Needed tools
- Java (For checking original signatures)
- Android Studio or AIDE (For building core APK)
# How to Inject
- Follow the how-to-use.txt in the SignDataReader folder, read the original APK using SignDataReader.jar, and copy and paste it into the SignData variable in `com.SignatureKiller.Hook(android.content.Context)`.
- Build with Android Studio or AIDE.
- Copy the DEX file of the completed APK to the APK to be modified.
- Rename the original APK to orig.apk and copy it into the assets folder of the APK to be modified. (If the folder does not exist, create it.)
- Copy the following smali code into the Activity entry point method (like onCreate, attachBaseContext, etc.) in the DEX.
```smali
invoke-static {p0}, Lcom/SignatureKiller/Main;->Hook(Landroid/content/Context;)V
````
- Signed and install the modified APK, check it passes the signature check.