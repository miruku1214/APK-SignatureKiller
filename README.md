# Required tools
- Java (For checking original signatures)
- Android Studio or AIDE (For building core APK)

# Warning
- This utility doubles the APK size.
- After opening the modded APK, your storage decreases the size of original APK.

# How to Inject
- Follow the how-to-use.txt in the SignDataReader folder, read the original APK using SignDataReader.jar, and copy and paste it into the SignData variable in `com.SignatureKiller.Hook(android.content.Context)`.
- Build with Android Studio or AIDE.
- Copy the DEX file of the completed APK to the APK to be modified.
- Rename the original APK to orig.apk and copy it into the assets folder of the APK to be modified. (If the folder does not exist, create it.)
- Paste the below smali code at the top of any entry point method (onCreate, attachBaseContext, etc.) that takes Android.content.Context or a class object that extends it as its first argument.
```smali
invoke-static {p0}, Lcom/SignatureKiller/Main;->Hook(Landroid/content/Context;)V
````
- Signed and install the modified APK, check it passes the signature check.
