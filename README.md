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

RoleBasedTelemedicinePatient/
│
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   │
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml
│   │       │
│   │       ├── java/
│   │       │   └── com/sukhayu/patient/
│   │       │       │
│   │       │       ├── ui/
│   │       │       │   ├── login/
│   │       │       │   │   └── LoginActivity.kt
│   │       │       │   │
│   │       │       │   ├── dashboard/
│   │       │       │   │   ├── DashboardActivity.kt
│   │       │       │   │   ├── DashboardAdapter.kt
│   │       │       │   │   
│   │       │       │   │
│   │       │       │   ├── profile/
│   │       │       │   │   └── ProfileActivity.kt
│   │       │       │   │
│   │       │       │   ├── ai_symptom/
│   │       │       │   │   ├── SymptomChatActivity.kt
│   │       │       │   │   ├── SymptomChecker.kt
│   │       │       │   │   ├── SymptomRules.kt
│   │       │       │   │   └── SymptomAdapter.kt
│   │       │       │   │
│   │       │       │   ├── consultation/
│   │       │       │   │   ├── ConsultDoctorActivity.kt
│   │       │       │   │   ├── DoctorDetailActivity.kt
│   │       │       │   │   ├── PastConsultationsActivity.kt
│   │       │       │   │   ├── PrescriptionActivity.kt
│   │       │       │   │   ├── MedicinesActivity.kt
│   │       │       │   │   └── adapters/
│   │       │       │   │       └── DoctorAdapter.kt
│   │       │       │   │
│   │       │       │   ├── teleconsult/
│   │       │       │   │   ├── VideoCallActivity.kt
│   │       │       │   │   ├── ConsentActivity.kt
│   │       │       │   │   ├── VoiceCallActivity.kt
│   │       │       │   │   └── ChatFallbackActivity.kt
│   │       │       │   │
│   │       │       │   ├── emergency/
│   │       │       │   │   ├── EmergencyActivity.kt
│   │       │       │   │   ├── EmergencyInitActivity.kt
│   │       │       │   │   └── EmergencyVCActivity.kt
│   │       │       │   │
│   │       │       │   ├── awareness/
│   │       │       │   │   └── DiseaseOutbreakActivity.kt
│   │       │       │   │
│   │       │       │   ├── common/
│   │       │       │   │   ├── SplashActivity.kt
│   │       │       │   │   ├── LanguageSelectionDialog.kt
│   │       │       │   │   └── include_header.xml
│   │       │       │   │
│   │       │       │   └── adapters/
│   │       │       │       └── SymptomMessageAdapter.kt
│   │       │       │
│   │       │       ├── viewmodel/
│   │       │       │   ├── PatientViewModel.kt
│   │       │       │   ├── SymptomViewModel.kt
│   │       │       │   ├── ConsultationViewModel.kt
│   │       │       │   ├── EmergencyViewModel.kt
│   │       │       │   ├── TeleconsultViewModel.kt
│   │       │       │   └── AwarenessViewModel.kt
│   │       │       │
│   │       │       ├── data/
│   │       │       │   ├── local/
│   │       │       │   │   ├── AppDatabase.kt
│   │       │       │   │   ├── dao/
│   │       │       │   │   │   ├── PatientDao.kt
│   │       │       │   │   │   ├── ConsultationDao.kt
│   │       │       │   │   │   ├── MedicineDao.kt
│   │       │       │   │   │   └── EmergencyDao.kt
│   │       │       │   │   └── entities/
│   │       │       │   │       ├── PatientEntity.kt
│   │       │       │   │       ├── ConsultationEntity.kt
│   │       │       │   │       ├── MedicineEntity.kt
│   │       │       │   │       └── EmergencyEntity.kt
│   │       │       │   │
│   │       │       │   ├── remote/
│   │       │       │   │   ├── ApiClient.kt
│   │       │       │   │   ├── ApiService.kt
│   │       │       │   │   ├── SyncService.kt
│   │       │       │   │   └── SocketManager.kt
│   │       │       │   │
│   │       │       │   └── repository/
│   │       │       │       ├── PatientRepository.kt
│   │       │       │       ├── ConsultationRepository.kt
│   │       │       │       ├── SymptomRepository.kt
│   │       │       │       ├── EmergencyRepository.kt
│   │       │       │       └── AwarenessRepository.kt
│   │       │       │
│   │       │       ├── notification/
│   │       │       │   ├── NotificationHelper.kt
│   │       │       │   ├── ReminderScheduler.kt
│   │       │       │   └── CallNotificationManager.kt
│   │       │       │
│   │       │       ├── sync/
│   │       │       │   ├── SyncManager.kt
│   │       │       │   └── SyncWorker.kt
│   │       │       │
│   │       │       ├── utils/
│   │       │       │   ├── Constants.kt
│   │       │       │   ├── NetworkUtils.kt
│   │       │       │   ├── PermissionUtils.kt
│   │       │       │   ├── PdfGenerator.kt
│   │       │       │   ├── LanguageHelper.kt
│   │       │       │   └── Extensions.kt
│   │       │       │
│   │       │       └── model/
│   │       │           ├── Patient.kt
│   │       │           ├── Consultation.kt
│   │       │           ├── Medicine.kt
│   │       │           ├── Emergency.kt
│   │       │           └── Disease.kt
│   │       │
│   │       └── res/
│   │           ├── drawable/
│   │           ├── layout/
│   │           │   ├── activity_login.xml
│   │           │   ├── activity_dashboard.xml
│   │           │   ├── activity_profile.xml
│   │           │   ├── activity_symptom_chat.xml
│   │           │   ├── activity_consult_doctor.xml
│   │           │   ├── activity_doctor_detail.xml
│   │           │   ├── activity_video_call.xml
│   │           │   ├── activity_voice_call.xml
│   │           │   ├── activity_chat_fallback.xml
│   │           │   ├── activity_medicines.xml
│   │           │   ├── activity_prescription.xml
│   │           │   ├── activity_past_consultations.xml
│   │           │   ├── activity_disease_outbreak.xml
│   │           │   ├── activity_emergency.xml
│   │           │   ├── activity_emergency_init.xml
│   │           │   ├── activity_emergency_vc.xml
│   │           │   ├── dialog_language_selector.xml
│   │           │   ├── include_header.xml
│   │           │   ├── item_doctor.xml
│   │           │   ├── item_symptom_message.xml
│   │           │   └── item_dashboard_card.xml
│   │           │
│   │           ├── values/
│   │           │   ├── colors.xml
│   │           │   ├── strings.xml
│   │           │   ├── themes.xml
│   │           │   └── styles.xml
│   │           │
│   │           ├── xml/
│   │           │   ├── network_security_config.xml
│   │           │   └── provider_paths.xml
│   │           │
│   │           ├── mipmap/
│   │           └── raw/
│   │               └── symptom_rules.json
│   │
│   └── test/
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── gradle.properties
├── settings.gradle
└── build.gradle


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
