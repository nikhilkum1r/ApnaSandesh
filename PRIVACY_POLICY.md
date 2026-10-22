# Privacy Policy for ApnaSandesh

**Last Updated:** October 22, 2026  
**Effective Date:** October 22, 2026  
**Application Name:** ApnaSandesh  
**Package Identifier:** `com.apnasandesh.app`  
**Developer Entity:** TruStoryX Digital  
**Official Website:** [https://apnasandesh.trustoryx.digital](https://apnasandesh.trustoryx.digital)  
**Dedicated Privacy URL:** [https://apnasandesh.trustoryx.digital/privacy](https://apnasandesh.trustoryx.digital/privacy)  
**Data Protection & Privacy Contact:** [privacy@trustoryx.digital](mailto:privacy@trustoryx.digital)  
**Support Contact:** [support@trustoryx.digital](mailto:support@trustoryx.digital)  

---

## Executive Summary & Core Commitment

**ApnaSandesh** is an autonomous, on-device business SMS messaging, customer directory, and client notification utility built for local merchants, retail stores, clinics, service contractors, and small businesses.

We believe that your customer lists, phone numbers, transaction histories, and business communications are strictly your intellectual property and private confidential data. 

* **100% Offline & On-Device:** ApnaSandesh operates completely on your physical Android hardware. We do not operate remote databases, cloud servers, or data collection endpoints.
* **Zero Internet Access:** The application does **NOT** declare or request the Android `android.permission.INTERNET` permission in its manifest. The Android operating system physically prevents the application from making any network calls, opening sockets, or uploading your data.
* **Zero Third-Party Trackers or Advertising:** There are zero tracking SDKs, zero third-party analytics libraries (no Firebase, no Google Analytics, no Adjust), and zero advertising networks.
* **100% User Sovereignty & Self-Service Deletion:** All data stays in your local app sandbox. You can erase all data instantly at any time with one click.

---

## 1. Zero-Data-Collection Architecture

Unlike conventional SaaS CRM platforms that force you to upload sensitive client contact records to third-party cloud infrastructure:
1. **No External Servers:** Trustoryx Digital does not maintain, rent, or access remote cloud servers that receive or store your contacts, message contents, delivery receipts, or business profiles.
2. **OS-Enforced Network Isolation:** Because the application does not hold `android.permission.INTERNET`, the Android kernel enforces a strict network barrier around the application sandbox. The application cannot transmit data off your device.
3. **Local Encrypted SQLite Sandbox:** All customer profiles, message templates, contact groupings, opt-out suppression entries, and dispatch audit logs are stored exclusively in the private, sandboxed SQLite database on your device (`/data/data/com.apnasandesh.app/databases/biz_sms_database`).
4. **Cloud Backup Exclusions:** Both `dataExtractionRules` and `backupRules` in the application manifest explicitly exclude the SQLite database from automated Google Drive cloud backups, ensuring your database is never silently synced to third-party cloud drives.

---

## 2. Operating System Permissions & Sensitive Data Handling

ApnaSandesh requests only strictly necessary Android runtime permissions to perform user-initiated business messaging tasks. In compliance with the **Google Play Developer Policy** and **Google Play Sensitive Permissions Policy**, here is the transparent breakdown of each permission:

### A. `SEND_SMS` (SMS Permissions Policy)
* **Status:** Optional (Runtime Permission).
* **Core Functionality Purpose:** Enables "Direct SIM Sending" mode. When explicitly activated by the user in settings, the application utilizes the device's cellular hardware (`SmsManager`) to dispatch personalized business messages sequentially through the user's mobile SIM card.
* **Strict Limitations:**
  * Messages are dispatched **only** when explicitly commanded by the user for a designated campaign or client alert.
  * No hidden, background, or unsolicited SMS messages are ever initiated.
  * The app provides an interactive real-time preview showing character count, GSM-7 / Unicode encoding, and exact segment calculation before sending.
* **Zero-Permission Alternative:** Users may keep Direct SIM mode disabled. In this case, ApnaSandesh uses the Android system SMS composer (`COMPOSER_HANDOFF`), which hands off drafts to your default messaging app with **zero** SMS permissions required.
* **No Inbox Access:** The app does **not** declare `READ_SMS` or `RECEIVE_SMS`. The application has zero ability to view, monitor, intercept, or read incoming private text messages or two-factor authentication codes.

### B. `READ_PHONE_STATE` (Telephony & Multi-SIM Slot Selection)
* **Status:** Optional (Runtime Permission).
* **Purpose:** On Multi-SIM (Dual-SIM) devices, this permission is used solely when Direct SIM mode is active to inspect available subscription IDs via Android `SubscriptionManager`. This enables the business owner to select which SIM card (e.g., SIM 1 vs SIM 2) should route the business dispatch.
* **Scope & Non-Collection:**
  * We **do not** collect or read IMEI, IMSI, hardware serial numbers, device phone numbers, or cellular data connection traffic.
  * We **do not** access call logs (`READ_CALL_LOG` is not requested) or monitor phone calls.

### C. `READ_CONTACTS` (Personal Contact Data Access)
* **Status:** Optional (Runtime Permission).
* **Purpose:** Requested only when the user chooses "Browse All Device Contacts" in the customer directory to select existing client records from their phone's address book for import into the local customer database.
* **Privacy & Isolation:** Selected contact names and phone numbers are loaded into memory and saved directly to the on-device SQLite database. They are **never** transmitted over any network or shared with external parties.
* **Zero-Permission Alternatives:** Users can import customer contacts one-by-one using the Android system contact picker (`ActivityResultContracts.PickContact()`) or via local CSV spreadsheet files, requiring **zero** contact permissions.

### D. `POST_NOTIFICATIONS` (Android 13+)
* **Status:** Optional (Runtime Permission).
* **Purpose:** Displays an ongoing notification in the Android system drawer during an active multi-message campaign (e.g., "Sending message 14 of 50..."), providing real-time progress indicators and interactive **Pause** and **Stop** controls.
* **Non-Marketing:** Used exclusively for local execution state feedback. The app does not integrate remote push notifications (e.g., Firebase Cloud Messaging) and sends zero promotional or unsolicited push messages.

### E. `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE` (Android 14+)
* **Declared Subtype:** `Autonomous campaign SMS message dispatching and progress tracking`
* **Purpose:** In accordance with Android 14 foreground service policies, this service ensures sequential message dispatch is not abruptly terminated by Android battery management or Doze mode when the user navigates away or locks their screen.
* **Lifecycle:** Starts only when the user explicitly triggers a campaign dispatch, displays an active persistent notification with controls, and terminates immediately once the dispatch finishes or is cancelled.

---

## 3. Third-Party Services, SDKs & Trackers

* **No Analytics SDKs:** We do not embed Google Analytics, Firebase Analytics, Flurry, Mixpanel, Segment, or Adjust.
* **No Advertising SDKs:** We do not embed AdMob, Meta Audience Network, Unity Ads, AppLovin, or any commercial monetization SDKs.
* **No Social Authentication:** We do not integrate Facebook Login, Google Sign-In, or third-party OAuth SDKs.
* **No Telemetry / Crash Reporting Over Network:** Because there is no internet permission, crash logs cannot be transmitted externally. Error diagnostics are retained only in local ephemeral memory via privacy-safe redacted logs (`PrivacySafeLogger`) that redact phone numbers and message content.

---

## 4. Data Retention, Security & User Deletion Rights

Google Play requires developers to provide comprehensive data retention and deletion policies:

### A. Data Retention
All customer contact information, templates, tags, delivery receipts, and suppression records remain stored exclusively on your device for as long as you keep the application installed, or until you choose to modify or delete them.

### B. User-Initiated Data Deletion (Mandatory Google Play Disclosure)
Because ApnaSandesh stores data exclusively on your device and operates without user accounts or cloud servers, you have full, immediate, self-service data deletion capabilities:
1. **In-App Complete Wipe:** Open the app and navigate to **Settings → Danger Zone → Permanently Delete All Data**. Confirming this action instantly drops and recreates all database tables, permanently erasing all customer profiles, suppression lists, templates, and campaign records.
2. **Granular In-App Deletion:** You can delete individual customer profiles, contact groups, message templates, or dispatch histories directly from their respective screens at any time.
3. **Android System Storage Clear:** Navigate to **Android Settings → Apps → ApnaSandesh (or ApnaSandesh) → Storage & Cache → Clear Storage (or Clear Data)**. This immediately purges all application sandbox data.
4. **Application Uninstallation:** Uninstalling the application from your Android device causes the operating system to immediately and permanently destroy all private app sandbox files and databases.

*Because no data is ever transmitted to or stored on remote servers, no server-side deletion ticket or waiting period is required. Deletion is instantaneous and irrevocable.*

### C. Data Security & Storage Architecture
* All data is stored in the Android internal application sandbox directory (`/data/data/com.apnasandesh.app/`), which is inaccessible to other third-party apps on standard Android security models.
* The data is protected by Android device hardware encryption (File-Based Encryption).
* Exported CSV files are generated exclusively via Android's secure Storage Access Framework (SAF) into directories explicitly selected by the user.

---

## 5. Customer Privacy, Opt-Outs & Telecom Compliance

To help small businesses respect their customers' privacy rights and comply with telecommunications regulations:
* **Built-in On-Device Suppression List:** If a client replies with "STOP" or requests not to receive further business alerts, the user can mark them opted-out. The application automatically suppresses the recipient from all future campaign dispatches, even if their number is subsequently included in a newly imported CSV spreadsheet.
* **Commercial Timing Assistance:** Built-in timing indicators advise users against dispatching non-urgent promotional messages outside authorized commercial business hours (09:00 to 21:00 local time) in compliance with regulations like TRAI (India Telecom Regulatory Authority) and international TCPA rules.
* **On-Device Audit Logs:** Delivery status receipts (`SENT`, `DELIVERED`, `FAILED`) are maintained strictly on-device for the user's compliance record-keeping.

---

## 6. Children's Privacy (COPPA & Google Play Families Policy)

ApnaSandesh is a business communication and utility application designed exclusively for commercial operations, merchants, and independent professionals aged 18 and older. The application is not directed to children under the age of 13 (or under 16 in the European Economic Area). We do not knowingly collect, request, or solicit personal data from children.

---

## 7. Global & Regional Legal Compliance

* **GDPR (European Union & UK):** Complies with the principles of Data Minimization, Purpose Limitation, Storage Limitation, and Privacy by Design. Users retain complete sovereignty (Right of Access, Rectification, and Erasure).
* **CCPA / CPRA (California, United States):** We do not sell, rent, disclose, or share personal information with third parties for commercial or cross-context behavioral advertising.
* **Digital Personal Data Protection Act (DPDP Act, India):** All customer records and processing remain localized within the user's personal device, under the direct control and responsibility of the business owner.

---

## 8. Google Play Data Safety Section Reference Table

For developers, regulators, and users reviewing the Google Play Console **Data Safety Section**, here is our official declaration:

| Google Play Console Question | Declaration | Technical Justification |
|---|---|---|
| **Does your app collect or share user data?** | **NO** | No data is collected or transmitted off the physical device. |
| **Is all user data encrypted in transit?** | **Not Applicable** | No data is transmitted over the internet or network. |
| **Do you provide a way for users to request data deletion?** | **YES** | Users can wipe all data in-app (*Settings → Wipe All Data*) or uninstall the app. |
| **Personal Information (Name, Phone, Email)** | **Not Collected** | Processed solely locally on device in app private sandbox. |
| **Messages (SMS/MMS text)** | **Not Collected** | Dispatched via cellular carrier; zero cloud transmission. |
| **Contacts** | **Not Collected** | Read locally into on-device SQLite database; zero cloud transmission. |
| **Financial / Payment Information** | **Not Collected** | App contains no payments or financial data handling. |
| **Location Information** | **Not Collected** | App requests zero location permissions. |
| **Device or other IDs** | **Not Collected** | Zero hardware IDs, IMEI, or advertising IDs collected. |

---

## 9. Changes to This Privacy Policy

Trustoryx Digital may update this Privacy Policy from time to time to reflect operational or regulatory changes. Any revisions will be published with an updated "Last Updated" timestamp on our official website at [https://trustoryx.digital/privacy](https://trustoryx.digital/privacy). Material modifications will also be noted in application release notes in the Google Play Store.

---

## 10. Developer Identification & Privacy Inquiries

If you have questions, regulatory compliance inquiries, or technical privacy questions regarding ApnaSandesh, please reach out to our privacy officer:

**Trustoryx Digital**  
*Attention: Data Protection & Privacy Compliance*  
* Email: [privacy@trustoryx.digital](mailto:privacy@trustoryx.digital)  
* Support Desk: [support@trustoryx.digital](mailto:support@trustoryx.digital)  
* Corporate Website: [https://trustoryx.digital](https://trustoryx.digital)  
* Product Portal: [https://apnasandesh.trustoryx.digital](https://apnasandesh.trustoryx.digital)  
