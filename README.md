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
│
├── app/                                      ← Main Android application module
│   ├── build.gradle                           ← App-level Gradle config (dependencies, plugins)
│   ├── proguard-rules.pro                     ← ProGuard/R8 rules for code shrinking/obfuscation
│   │
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml            ← Declares activities, permissions, app metadata
│   │       │
│   │       ├── java/
│   │       │   └── com/sukhayu/patient/       ← Main application package
│   │       │       │
│   │       │       ├── ui/                    ← All UI screens & flows
│   │       │       │   ├── login/
│   │       │       │   │   └── LoginActivity.kt             ← Handles user login UI & logic
│   │       │       │   │
│   │       │       │   ├── dashboard/
│   │       │       │   │   ├── DashboardActivity.kt         ← Main home dashboard for patient
│   │       │       │   │   ├── DashboardAdapter.kt          ← Adapter for dashboard cards list
│   │       │       │   │
│   │       │       │   ├── profile/
│   │       │       │   │   └── ProfileActivity.kt           ← Displays & edits patient profile
│   │       │       │   │
│   │       │       │   ├── ai_symptom/
│   │       │       │   │   ├── CheckSymptomsActivity.kt      ← UI for starting symptom checker
│   │       │       │   │   ├── SymptomChatActivity.kt        ← Chat-style symptom input screen
│   │       │       │   │   ├── SymptomChecker.kt             ← Logic for analyzing symptoms
│   │       │       │   │   ├── SymptomRules.kt               ← Symptom decision-tree rules
│   │       │       │   │   └── SymptomAdapter.kt             ← Adapter for displaying chat messages
│   │       │       │   │
│   │       │       │   ├── consultation/
│   │       │       │   │   ├── ConsultDoctorActivity.kt      ← Shows available doctors for consultation
│   │       │       │   │   ├── DoctorDetailActivity.kt       ← Displays doctor profile & info
│   │       │       │   │   ├── PastConsultationsActivity.kt  ← History of past consultations
│   │       │       │   │   ├── PrescriptionActivity.kt       ← Displays prescriptions
│   │       │       │   │   ├── MedicinesActivity.kt          ← Shows prescribed medicines list
│   │       │       │   │   └── adapters/
│   │       │       │   │       └── DoctorAdapter.kt          ← Adapter for doctor list UI
│   │       │       │   │
│   │       │       │   ├── teleconsult/
│   │       │       │   │   ├── VideoCallActivity.kt          ← Handles video teleconsultation
│   │       │       │   │   ├── ConsentActivity.kt            ← Consent screen before teleconsultation
│   │       │       │   │   ├── VoiceCallActivity.kt          ← Handles audio-only consultation
│   │       │       │   │   └── ChatFallbackActivity.kt       ← Chat fallback if voice/video fails
│   │       │       │   │
│   │       │       │   ├── emergency/
│   │       │       │   │   ├── EmergencyActivity.kt          ← Emergency request screen
│   │       │       │   │   ├── EmergencyInitActivity.kt      ← Initial emergency instructions
│   │       │       │   │   └── EmergencyVCActivity.kt        ← Emergency video consultation
│   │       │       │   │
│   │       │       │   ├── awareness/
│   │       │       │   │   └── DiseaseOutbreakActivity.kt    ← Awareness info about disease outbreaks
│   │       │       │   │
│   │       │       │   ├── common/
│   │       │       │   │   ├── SplashActivity.kt             ← App splash screen
│   │       │       │   │   ├── LanguageSelectionDialog.kt    ← Popup for language change
│   │       │       │   │   └── include_header.xml            ← Reusable app header layout
│   │       │       │   │
│   │       │       │   └── adapters/
│   │       │       │       └── SymptomMessageAdapter.kt      ← Adapter for symptom chat messages
│   │       │       │
│   │       │       ├── viewmodel/                             ← ViewModels for MVVM architecture
│   │       │       │   ├── PatientViewModel.kt               ← Manages patient-related UI data
│   │       │       │   ├── SymptomViewModel.kt               ← Handles symptom checker data flow
│   │       │       │   ├── ConsultationViewModel.kt          ← Manages consultation data
│   │       │       │   ├── EmergencyViewModel.kt             ← Manages emergency service interactions
│   │       │       │   ├── TeleconsultViewModel.kt           ← Handles teleconsultation logic
│   │       │       │   └── AwarenessViewModel.kt             ← Supplies disease awareness data
│   │       │       │
│   │       │       ├── data/                                  ← Data layer (local + remote + repo)
│   │       │       │   ├── local/
│   │       │       │   │   ├── AppDatabase.kt                ← Room database configuration
│   │       │       │   │   ├── dao/
│   │       │       │   │   │   ├── PatientDao.kt             ← Patient table database operations
│   │       │       │   │   │   ├── ConsultationDao.kt        ← Consultation DB operations
│   │       │       │   │   │   ├── MedicineDao.kt            ← Medicine DB operations
│   │       │       │   │   │   └── EmergencyDao.kt           ← Emergency DB operations
│   │       │       │   │   └── entities/
│   │       │       │   │       ├── PatientEntity.kt          ← Patient table schema
│   │       │       │   │       ├── ConsultationEntity.kt     ← Consultation table schema
│   │       │       │   │       ├── MedicineEntity.kt         ← Medicine table schema
│   │       │       │   │       └── EmergencyEntity.kt        ← Emergency table schema
│   │       │       │   │
│   │       │       │   ├── remote/
│   │       │       │   │   ├── ApiClient.kt                  ← Retrofit client setup
│   │       │       │   │   ├── ApiService.kt                 ← Retrofit API endpoints
│   │       │       │   │   ├── SyncService.kt                ← Background sync API logic
│   │       │       │   │   └── SocketManager.kt              ← WebSocket/real-time communication manager
│   │       │       │   │
│   │       │       │   └── repository/
│   │       │       │       ├── PatientRepository.kt          ← Repository for patient data
│   │       │       │       ├── ConsultationRepository.kt     ← Repository for consultations
│   │       │       │       ├── SymptomRepository.kt          ← Repository for symptom checker
│   │       │       │       ├── EmergencyRepository.kt        ← Repository for emergency services
│   │       │       │       └── AwarenessRepository.kt        ← Repository for disease awareness
│   │       │       │
│   │       │       ├── notification/
│   │       │       │   ├── NotificationHelper.kt             ← Notification creation helper
│   │       │       │   ├── ReminderScheduler.kt              ← Schedules reminders/alarms
│   │       │       │   └── CallNotificationManager.kt        ← Handles call-style notifications
│   │       │       │
│   │       │       ├── sync/
│   │       │       │   ├── SyncManager.kt                    ← Controls app-wide background sync logic
│   │       │       │   └── SyncWorker.kt                     ← WorkManager worker for syncing data
│   │       │       │
│   │       │       ├── utils/
│   │       │       │   ├── Constants.kt                      ← Global constants
│   │       │       │   ├── NetworkUtils.kt                   ← Network checks & utilities
│   │       │       │   ├── PermissionUtils.kt                ← Helper for runtime permissions
│   │       │       │   ├── PdfGenerator.kt                   ← Generates PDFs for prescriptions
│   │       │       │   ├── LanguageHelper.kt                 ← Multilingual support utilities
│   │       │       │   └── Extensions.kt                     ← Kotlin extension functions
│   │       │       │
│   │       │       └── model/
│   │       │           ├── Patient.kt                        ← Patient data model
│   │       │           ├── Consultation.kt                   ← Consultation data model
│   │       │           ├── Medicine.kt                       ← Medicine data model
│   │       │           ├── Emergency.kt                      ← Emergency data model
│   │       │           └── Disease.kt                        ← Disease awareness model
│   │       │
│   │       └── res/                                           ← XML UI resources
│   │           ├── drawable/                                  ← App icons, shapes, images
│   │           ├── layout/                                    ← XML layouts for all screens
│   │           ├── values/                                    ← Colors, strings, themes, styles
│   │           ├── xml/                                       ← Network configs, provider paths
│   │           ├── mipmap/                                    ← Launcher icons
│   │           └── raw/                                       ← Raw assets (symptom rules JSON)
│   │
│   └── test/                                                  ← Unit & instrumentation tests
│
├── gradle/
│   └── wrapper/                                               ← Gradle wrapper files
│       ├── gradle-wrapper.jar                                ← Wrapper executable
│       └── gradle-wrapper.properties                          ← Wrapper config
│
├── gradle.properties                                          ← Global Gradle build configuration
├── settings.gradle                                            ← Includes modules in project
└── build.gradle                                               ← Root-level Gradle config


New files to be added list



Done
ui/ai_symptom/SymptomChatActivity.kt	Chat-style AI symptom checker UI
ui/ai_symptom/SymptomAdapter.kt	RecyclerView adapter for chat messages
ui/ai_symptom/SymptomChecker.kt	Core symptom analysis logic
ui/ai_symptom/SymptomRules.kt	Rule-based inference for disease prediction
ui/consultation/PrescriptionActivity.kt	Displays doctor’s prescription (text + PDF view)
ui/teleconsult/VideoCallActivity.kt	Video consultation screen using WebRTC or Jitsi
ui/teleconsult/ConsentActivity.kt	Consent popup before video call
ui/teleconsult/VoiceCallActivity.kt	Voice-only call fallback
ui/teleconsult/ChatFallbackActivity.kt	Chat fallback screen for weak network
ui/emergency/EmergencyInitActivity.kt	Starts SOS request and shows countdown
ui/emergency/EmergencyVCActivity.kt	Video call with emergency doctor
ui/awareness/DiseaseOutbreakActivity.kt	Displays disease outbreak/news updates
viewmodel/TeleconsultViewModel.kt	Handles logic for voice/video call states
layout/activity_symptom_chat.xml	UI layout for chat-based symptom checker
layout/activity_prescription.xml	Layout for prescription screen
layout/activity_video_call.xml	Layout for video call interface
layout/activity_voice_call.xml	Layout for voice call
layout/activity_chat_fallback.xml	Layout for text-based fallback chat
layout/activity_disease_outbreak.xml	Layout for awareness/outbreak info
layout/activity_emergency_init.xml	Layout for SOS countdown/initiation
layout/activity_emergency_vc.xml	Layout for emergency VC
layout/item_symptom_message.xml	Layout for chat message bubble
viewmodel/EmergencyViewModel.kt	Manages SOS requests and alerts
viewmodel/AwarenessViewModel.kt	Fetches and caches awareness/outbreak data
data/local/dao/EmergencyDao.kt	DAO for emergency call/session storage
data/local/entities/EmergencyEntity.kt	Entity for emergency events
data/remote/SocketManager.kt	Handles real-time socket/video events
data/repository/EmergencyRepository.kt	Business logic for emergency actions
data/repository/AwarenessRepository.kt	Fetches awareness content from API
notification/CallNotificationManager.kt	Manages incoming/outgoing call notifications
raw/symptom_rules.json	JSON file containing AI symptom–disease mappings
