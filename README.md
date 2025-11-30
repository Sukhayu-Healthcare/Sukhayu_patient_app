RoleBasedTelemedicinePatient - Patient-side frontend (Kotlin)

How to run:
1. Download and extract this project.
2. Open Android Studio -> Open an existing project -> select the extracted folder.
3. Wait for Gradle sync. If Android Studio prompts to install SDKs or Gradle, follow the prompts.
4. Run the app on an emulator or physical device.

Login credentials (dummy):
Username: Dummy Patient
OTP: 123456


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

Compile code:
gradlew compileDebugKotlin

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
adb install -r C:<Add Path>RoleBasedTelemedicinePatient\app\build\outputs\apk\debug\app-debug.apk

-r = reinstall if the app already exists.

6️⃣ Launch the App
adb shell am start -n com.sukhayu.patient/.LoginActivity


Replace LoginActivity if your launcher activity is different.

7️⃣ Uninstall / Stop App

To uninstall the app from your device:

adb uninstall com.sukhayu.patient

This removes the app completely.

Required structure for app

RoleBasedTelemedicinePatient/                 ← Root Android project folder
│   .gitignore                       # Git ignore rules
│   build.gradle                     # Root Gradle build script
│   gradle.properties                # Global Gradle configuration
│   gradlew                          # Unix Gradle wrapper
│   gradlew.bat                      # Windows Gradle wrapper
│   local.properties                 # Local SDK path settings
│   README.md                        # Project documentation
│   settings.gradle                  # Includes app modules
│
├───.gradle
│   │   config.properties            # Gradle cache config
│   │   file-system.probe            # Gradle FS probe
│   │
│   ├───8.7                          # Gradle version-specific cache
│   │   │   gc.properties            # Garbage collection config
│   │   │
│   │   ├───checksums                # Dependency checksum cache
│   │   ├───dependencies-accessors   # Accessor generation cache
│   │   ├───executionHistory         # Build execution history
│   │   ├───expanded                 # Expanded dependencies
│   │   ├───fileChanges              # Build input file change logs
│   │   ├───fileHashes               # File hashing cache
│   │   └───vcsMetadata              # Version control metadata cache
│   │
│   ├───9.0-milestone-1              # Another Gradle version cache
│   ├───buildOutputCleanup           # Cleanup tracking for builds
│   ├───kotlin                       # Kotlin incremental build cache
│   └───vcs-1                        # VCS metadata cache
│
├───.idea                             # Android Studio project config
│   ├───caches                        # Editor cache
│   └───… (other project XML configs) # IDE-specific settings
│
├───.vscode                           # VS Code workspace settings
│       settings.json                 # VS Code project config
│
├───app                               # Main Android application module
│   │   build.gradle                  # Module build script
│   │   proguard-rules.pro            # Obfuscation rules
│   │
│   └───src
│       └───main
│           │   AndroidManifest.xml   # App component declarations
│           │
│           ├───java
│           │   └───com
│           │       └───sukhayu
│           │           └───patient
│           │               │   DummyData.kt                # Hardcoded sample data
│           │               │   MyApp.kt                    # Application class
│           │               │
│           │               ├───ai                           # AI utilities (empty)
│           │               ├───data
│           │               │   ├───local                   # Room database classes
│           │               │   ├───remote                  # Retrofit/network layer
│           │               │   └───repository              # Repository abstractions
│           │               │
│           │               ├───model                       # Data model classes
│           │               ├───notification                # Call/notification helper
│           │               ├───sync                        # Sync-related code (empty)
│           │               ├───ui                          # All UI screens
│           │               │   ├───adapters                # RecyclerView adapters
│           │               │   ├───ai_symptom              # AI symptom checker screens
│           │               │   ├───asha                    # ASHA worker module UI
│           │               │   ├───awareness               # Health awareness screens
│           │               │   ├───common                  # Shared UI components
│           │               │   ├───consultation            # Doctor consultation features
│           │               │   ├───dashboard               # User dashboard screens
│           │               │   ├───emergency               # Emergency calling module
│           │               │   ├───login                   # Login/Authentication UI
│           │               │   ├───profile                 # User profile screens
│           │               │   ├───supervisor              # Supervisor workflow screens
│           │               │   └───teleconsult             # Video/audio call module
│           │               │
│           │               ├───utils                       # Utility/helper functions
│           │               └───viewmodel                   # MVVM ViewModels
│           │
│           └───res
│               ├───drawable                                 # App images & vector drawables
│               ├───layout                                   # XML UI layouts
│               ├───mipmap-anydpi-v26                        # Adaptive launcher icons
│               ├───raw                                      # Raw resource files
│               ├───values                                   # Strings, colors, themes
│               └───xml                                      # App configuration XMLs
│
├───build                                # Root build output
│   └───reports
│       └───problems                     # Build issue reports
│
└───gradle
    └───wrapper
            gradle-wrapper.jar           # Wrapper bootstrap
            gradle-wrapper.properties    # Wrapper configuration
            gradle.properties            # Gradle environment config
                                               ← Root-level Gradle config


i use github copilot , give me a complete prompt to use voice input in each normal input(no password/ choice based input) in english and marathi. 