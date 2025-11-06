RoleBasedTelemedicinePatient - Patient-side frontend (Kotlin)

How to run:
1. Download and extract this project.
2. Open Android Studio -> Open an existing project -> select the extracted folder.
3. Wait for Gradle sync. If Android Studio prompts to install SDKs or Gradle, follow the prompts.
4. Run the app on an emulator or physical device.

Login credentials (dummy):
Email: patient@example.com
Password: password


First, navigate to your Android SDK tools directory:
cd C:\Users\<YourUser>\AppData\Local\Android\Sdk\emulator

Ensure platform-tools is in your PATH:
cd C:\Users\<YourUser>\AppData\Local\Android\Sdk\platform-tools
adb devices

Output example:
List of devices attached
emulator-5554   device

Start Emulator
emulator -avd <emulator name>


New terminal - 
Navigate to your project root (where gradlew is located):

cd C:\Users\....
gradlew assembleDebug
adb shell am start -n com.sukhayu.patient/.LoginActivity


After stopping
gradlew clean


2. USING USB DEBUGGING
1️⃣ Enable USB Debugging on Your Device

On your Android device:

Go to Settings → About phone → Build number → tap 7 times to enable Developer options.

Go to Settings → System → Developer options → USB debugging → enable it.

2️⃣ Connect Your Device via USB

Use a USB cable to connect your phone.

Allow USB debugging when the prompt appears on your device.

3️⃣ Verify Device Connection

Make sure adb can see your device:

cd C:\Users\<YourUser>\AppData\Local\Android\Sdk\platform-tools
adb devices


Example output:

List of devices attached
1234567890abcdef    device


If you see unauthorized, check your device screen and accept the debug authorization.

4️⃣ Build the Project

Navigate to your project folder:

cd C:\Users\anike\Downloads\RoleBasedTelemedicinePatient
gradlew assembleDebug


APK path:

app\build\outputs\apk\debug\app-debug.apk

5️⃣ Install APK on Device

Replace the path with your actual APK location:

AppData\Local\Android\Sdk\platform-tools


-r = reinstall if the app already exists.

6️⃣ Launch the App
adb shell am start -n com.sukhayu.patient/.LoginActivity


Replace LoginActivity if your launcher activity is different.

7️⃣ Uninstall / Stop App

To uninstall the app from your device:

adb uninstall com.sukhayu.patient


This removes the app completely.