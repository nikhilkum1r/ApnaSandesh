# ApnaSandesh (अपना संदेश) — Local-First Business SMS & CRM

<div align="center">

[![Android Version](https://img.shields.io/badge/Android-8.0%2B%20(API%2026--36)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Room-SQLite%20(Offline%20First)-003B57?logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device%20(Zero%20Internet%20Permission)-green)](https://apnasandesh.trustoryx.digital/privacy)
[![License](https://img.shields.io/badge/License-Proprietary-blue)](https://trustoryx.digital)
[![Latest Release](https://img.shields.io/badge/Release-v1.0.3-blueviolet)](https://github.com/nikhilkum1r/ApnaSandesh/releases/tag/v1.0.3)

**Aapka Number, Aapke Customers.**  
*Autonomous, 100% Private Business SMS & Customer Management Workstation for Indian Small Businesses, Shopkeepers, Clinics, Salons, Coaching Centres, and Retailers.*

[Website](https://apnasandesh.trustoryx.digital) • [Download APK](https://github.com/nikhilkum1r/ApnaSandesh/releases/download/v1.0.3/ApnaSandesh-v1.0.3-release.apk) • [Google Play Bundle](https://github.com/nikhilkum1r/ApnaSandesh/releases/download/v1.0.3/ApnaSandesh-v1.0.3-release.aab) • [Privacy Policy](https://apnasandesh.trustoryx.digital/privacy)

</div>

---

## 📖 Executive Overview

**ApnaSandesh** turns any Android smartphone into an autonomous, subscription-free business SMS workstation. Small business owners communicate directly with their clients using their existing mobile cellular plan and SIM card.

### Why ApnaSandesh?
* **Zero Recurring Gateways**: No expensive cloud SMS APIs (Twilio, Gupshup, Exotel) or per-message markups.
* **100% Data Sovereignty**: Operates without the `android.permission.INTERNET` permission. Contacts, customer histories, and drafts never leave the physical device.
* **Carrier Safety & Anti-Spam Control**: Built-in pacing delay rings (1–10s intervals), batch caps, and automatic TRAI commercial timing safeguards protect your personal SIM from telecommunication blocks.
* **Integrated SMS Cost Optimizer**: Real-time GSM-7 vs. UCS-2 Unicode character analysis prevents accidental multipart SMS billing spikes.

---

## 🚀 Key Features

### 1. Dual Dispatch Modes
* **Direct SIM Sequential Dispatch**:
  - Automatically dispatches queued personalized SMS messages through the device's physical cellular SIM card.
  - Powered by an Android Foreground Service with live notification progress, pause, resume, and cancellation controls.
  - Tracks carrier delivery callbacks (`SMS_SENT` and `SMS_DELIVERED`) with status receipts written to the local database.
* **Default SMS Composer Mode (Zero Sensitive Permissions)**:
  - Uses native Android `Intent.ACTION_SENDTO` (`smsto:`) to pre-fill the phone's default messaging app (Google Messages, Samsung Messages).
  - Designed for high-trust manual verification before hitting Send.

### 2. Smart Customer Directory & Bulk Import
* **CSV Import with Formula Injection Defense**:
  - Automatically detects column headers (`Name`, `Phone`, `Tags`, `Notes`, `Company`, `Order ID`, etc.).
  - Built-in sanitization against CSV/DDE formula injection (`=`, `+`, `-`, `@`).
* **Address Book Integration**: Pick and multi-select contacts directly from the device's native address book.
* **E.164 Phone Normalization**: Auto-formats numbers with country code prefixes (`+91` default). Detects invalid, duplicate, and short codes before launching campaigns.
* **Tagging & Group Segmentation**: Tag customers as VIP, Regular, Batch-A, Walk-in, Due Payment, etc.

### 3. Rich Hindi, English & Hinglish Template Library
* **30+ Pre-built Industry Templates**:
  - Appointments, payment reminders, order-ready alerts, service pickup notices, festive greetings (Diwali, Eid, Holi, New Year), and feedback requests.
* **Dynamic Tag Personalization**: Auto-replaces `{{name}}`, `{{business_name}}`, `{{order_id}}`, `{{amount}}`, `{{date}}`, and `{{signature}}`.
* **Interactive Chat Bubble Preview**: Live WYSIWYG bubble showing actual rendering before sending.

### 4. SMS Cost Optimizer & GSM-7 / UCS-2 Encoding Engine
* Detects standard GSM 7-bit characters (160 chars/segment, 153 chars for multipart).
* Detects Unicode / Devanagari / Emoji characters triggering UCS-2 encoding (70 chars/segment, 67 chars for multipart).
* Provides real-time suggestions to replace accidental Unicode punctuation (smart quotes `“”`, em-dashes `—`) with GSM-7 equivalents to save SMS costs.

### 5. Automated Scheduling & Background Reliability
* Schedule campaigns for `+1 Hour`, `Tomorrow 10:00 AM`, `Tomorrow 06:00 PM`, or custom date/time.
* Scheduled execution handled by Android `WorkManager` with battery-optimized background constraints.

### 6. Regulatory Telecom & Opt-Out (DNC) Compliance
* **Persistent Do-Not-Contact (DNC) Registry**: Contacts who reply `STOP` or opt out are permanently excluded from future campaigns across CSV re-imports.
* **TRAI Timing Guidance**: Alerts users if campaigns are scheduled outside legitimate commercial communication hours (9:00 AM – 9:00 PM).

---

## 📱 User Guide: Setup & First Campaign

### Step 1: Install the APK
1. Download **`ApnaSandesh-v1.0.3-release.apk`** from the [GitHub Releases](https://github.com/nikhilkum1r/ApnaSandesh/releases/tag/v1.0.3).
2. Tap the downloaded file in your Android notification bar or Files app.
3. If prompted, enable **Install unknown apps** for your browser or file manager.
4. Tap **Install** and open **ApnaSandesh**.

### Step 2: Configure Business Profile
1. Navigate to **Settings** (`⚙️`).
2. Enter your **Business Name**, **Owner Name**, **Category**, and **Sender Phone Number**.
3. Set your default message **Signature** (e.g., `- Sharma Electronics, Karol Bagh`).
4. Choose your preferred **Pacing Interval** (default: 4 seconds between sequential messages).

### Step 3: Add Customers
* **Option A (CSV Import)**:
  - Tap **Customers** -> **Import CSV**.
  - Choose any CSV file from your phone or Google Drive.
  - Review detected columns and tap **Confirm Import**.
* **Option B (Phone Contacts)**:
  - Tap **Browse Device Contacts** and select your existing clients.
* **Option C (Manual Entry)**:
  - Tap **Add Customer** (`+`) to add individual names, numbers, and tags.

### Step 4: Create & Send a Campaign
1. Go to **Campaigns** -> **New Campaign**.
2. Select target recipients by Group or Tag (or select all).
3. Pick a template from the library or draft a custom message using variables like `{{name}}`.
4. Review the **SMS Cost Optimizer** box (verify character count and segment estimate).
5. Choose **Send Now** (Sequential Direct SIM) or **Schedule for Later**.
6. Monitor live dispatch progress in the foreground notification or on the Campaign details screen.

---

## 🏗️ Architecture & Technical Stack

```text
ApnaSandesh/
├── app/
│   ├── src/main/java/com/apnasandesh/app/
│   │   ├── core/database/           # Room Database, Entities, TypeConverters & Migrations (v1->v5)
│   │   ├── data/                    # Repositories (CustomerRepository, CampaignRepository) & Workers
│   │   ├── di/                      # Hilt Dependency Injection Modules
│   │   ├── domain/
│   │   │   ├── campaign/            # CampaignStateMachine, AutomaticCampaignSendingEngine, ForegroundService
│   │   │   ├── transport/           # DirectSimSmsTransport, SmsStatusBroadcastReceiver, TransportPolicyGate
│   │   │   └── validation/          # TemplateRenderer, SmsCostOptimizer (GSM-7/UCS-2)
│   │   ├── feature/
│   │   │   ├── dashboard/           # Analytics, quick actions, metric cards
│   │   │   ├── customers/           # Directory, search, filter, CSV import dialog, customer details
│   │   │   ├── templates/           # Category browser, live editor, preview bubble
│   │   │   ├── campaigns/           # Wizard, recipient selection, execution observer, composer handoff
│   │   │   ├── history/             # Past campaign logs, delivery stats, CSV export
│   │   │   └── settings/            # Profile, pacing configuration, backup/restore
│   │   └── theme/                   # Material 3 Dynamic Color, Typography, Shapes
│   ├── src/test/                    # JUnit 4 & Kotlin Coroutines Unit Tests
│   └── build.gradle.kts             # CompileSdk 36, TargetSdk 36, MinSdk 26, Proguard/R8
└── Website/                         # Next.js 14 companion marketing and documentation portal
```

### Core Technologies
* **Language**: Kotlin 2.0.21
* **UI**: Jetpack Compose with Material Design 3
* **Dependency Injection**: Hilt 2.51.1
* **Local Persistence**: Room 2.6.1 (SQLite) with auto-migrations
* **Preferences**: AndroidX Jetpack DataStore Preferences
* **Background Processing**: AndroidX WorkManager & Native Foreground Service
* **Build System**: Gradle 8.9 with Android Gradle Plugin 8.7.2
* **Compilation**: Java 17, Target SDK 36 (Android 16), Min SDK 26 (Android 8.0 Oreo)

---

## 🛠️ Developer Setup & Build Instructions

### Prerequisites
* JDK 17 or JDK 21 installed (`java -version`)
* Android SDK with Build-Tools 35.0.0 or 36.0.0 (`$ANDROID_HOME` configured)
* Git

### Clone & Test
```bash
git clone https://github.com/nikhilkum1r/ApnaSandesh.git
cd ApnaSandesh

# Run domain logic and unit tests
./gradlew test

# Linting and verification
./gradlew lint
```

### Build Production Release APK & AAB
```bash
# Build signed production release APK (outputs to app/build/outputs/apk/release/app-release.apk)
./gradlew assembleRelease

# Build Google Play App Bundle (outputs to app/build/outputs/bundle/release/app-release.aab)
./gradlew bundleRelease
```

---

## 🔐 Data Safety & Privacy Guarantee

ApnaSandesh strictly adheres to zero-cloud architecture:
* **No `INTERNET` Permission**: The application cannot communicate with external servers, tracking services, or cloud analytics.
* **No External Analytics**: Zero Google Analytics, Firebase, or third-party SDKs.
* **Local Storage Only**: All data is stored in on-device SQLite databases inside Android's sandboxed app storage.
* **Total User Control**: Clearing app data or uninstalling removes 100% of contact and campaign records permanently.

---

## 📦 Release Artifacts (v1.0.3)

| File | Type | Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **`ApnaSandesh-v1.0.3-release.apk`** | Production APK | 4.2 MB | `74a5e76688f9453bed0fb2ffe445dbf88d5a7f7cba3b56b7962957fc10a46689` |
| **`ApnaSandesh-v1.0.3-release.aab`** | Google Play Bundle | 7.0 MB | `99f8dfc99294636d619fb5885e50b92a22f177a62eb7320c1909aa89fc2cb411` |

---

## 📄 License & Attribution

Designed and engineered by **Trustoryx Digital** (`support@trustoryx.digital`).  
All rights reserved. Dedicated to empowering Indian local commerce and independent enterprises.
