# ApnaSandesh — Google Play Store Listing & Publishing Kit

This guide contains the exact copy, declarations, and Data Safety answers required to publish **ApnaSandesh** on the Google Play Console.

---

## 1. Store Listing Metadata

| Field | Content |
|---|---|
| **App Name** | `ApnaSandesh — Business SMS & CRM` |
| **Short Description** (Max 80 chars) | `Aapka Number, Aapke Customers. 100% private business SMS from your own SIM.` |
| **Category** | Business / Communication |
| **Tags** | Business SMS, SMS Marketing, Client Messaging, Local Business CRM, Bulk SMS |
| **Contact Email** | `support@trustoryx.digital` |
| **Privacy Policy URL** | `https://apnasandesh.trustoryx.digital/privacy` (or link to your hosted `privacy_policy.html`) |

---

## 2. Full Description (Max 4,000 characters)

```text
ApnaSandesh — Aapka Number, Aapke Customers.

ApnaSandesh empowers Indian small businesses, shopkeepers, independent service providers, and local retailers to communicate directly with their customers through fast, personalized SMS messages—using your own phone and cellular SIM card.

Zero subscriptions. Zero third-party cloud servers. 100% private, on-device, and local.

WHY CHOOSE APNASANDESH?
Unlike expensive cloud marketing tools that upload your customer contacts to external servers and charge recurring per-message fees, ApnaSandesh works directly on your Android phone using your existing mobile cellular plan. All customer databases, drafts, and delivery records remain strictly on your physical device.

KEY FEATURES:

📱 1. TWO FLEXIBLE DISPATCH MODES
• Default SMS Composer Mode (Zero Permissions): Pre-fills your default messaging app with personalized text and client numbers for transparent, one-tap sending.
• Direct SIM Mode: Sends sequential messages automatically using your phone's cellular SIM card with custom safety delay intervals (1-10s) to protect personal SIM quotas.

👥 2. SMART CUSTOMER DIRECTORY & BULK IMPORT
• Browse and select multiple contacts directly from your device's address book with search and select-all.
• Flexible CSV Spreadsheet Import/Export with automatic header detection (Name, Phone, Tags, Notes, Company, Order ID, and more).
• Automatic phone normalization to Indian and international E.164 standards (+91, etc.).

📝 3. RICH TEMPLATE LIBRARY WITH LIVE PERSONALIZATION
• 30+ pre-built, industry-tailored message templates for retail, services, appointments, order confirmations, festive greetings (Diwali, Eid, Holi), and promotions.
• Dynamic personalization tags: {name}, {business_name}, {order_id}, {amount}, {date}, and {signature}.
• Live interactive SMS chat bubble preview showing exact characters, GSM-7 / Unicode detection, and estimated SMS segment count before sending.

⏱️ 4. CAMPAIGN SCHEDULING & FOREGROUND RELIABILITY
• Schedule campaigns for later: "+1 Hour", "Tomorrow 10 AM", "Tomorrow 6 PM", or custom timing powered by Android WorkManager.
• Background dispatching supported by an active foreground service with real-time notification progress, Pause, and Stop controls.

🛡️ 5. OPT-OUT & TELECOM COMPLIANCE
• On-device suppression list: Customers who reply STOP or request opt-out are permanently excluded from future dispatches.
• Built-in TRAI / commercial timing guidance to keep your promotional messages compliant with telecommunication regulations.
• Audit logs and delivery confirmation receipts (Sent, Delivered, Failed) with one-click CSV export.

🔒 PRIVACY-FIRST GUARANTEE:
• ApnaSandesh does NOT declare the INTERNET permission.
• Your client contact information and business communications NEVER leave your phone.
• No account creation, no trackers, and no advertisements.

Developed by Trustoryx Digital.
Support & Feedback: support@trustoryx.digital
Privacy Policy: https://apnasandesh.trustoryx.digital/privacy
```

---

## 3. Google Play Data Safety Questionnaire Answers

When completing the **Data Safety form** in the Google Play Console:

1. **Does your app collect or share any user data?**
   - Select **NO**. (Because the app has no `INTERNET` permission and does not transmit data off the device).
2. **Data Encrypted in Transit?**
   - Answer: **Not applicable** (The app does not transfer data over any network).
3. **Account Deletion / Data Deletion:**
   - Answer: Users can delete all records directly inside the app or by clearing app storage / uninstalling.

---

## 4. Sensitive Permissions Declaration Form

Google Play requires declaration for apps declaring `SEND_SMS`:

### Permission: `SEND_SMS`
- **Core Functionality:** Business SMS message dispatching.
- **Why is it needed:** ApnaSandesh provides an automated Direct SIM dispatch mode for small business owners sending individual personalized SMS messages to their existing clients.
- **Is there an alternative?** Yes, ApnaSandesh also provides a zero-permission SMS Composer mode (`COMPOSER_HANDOFF`). Direct SIM is explicitly gated behind user opt-in in Settings.

### Permission: `FOREGROUND_SERVICE_SPECIAL_USE` (Android 14+)
- **Subtype Value:** `Autonomous campaign SMS message dispatching and progress tracking`
- **Explanation:** Ensures sequential message transmission is not killed by the OS when the screen locks or when the user temporarily switches apps during a multi-client dispatch.

---

## 5. Artifact Build Locations

- **Android App Bundle (.aab)** for Google Play Console:
  `app/build/outputs/bundle/release/app-release.aab`
- **Release APK (.apk)** for direct testing / sideloading:
  `app/build/outputs/apk/release/app-release.apk`
- **Keystore:**
  `app/apnasandesh-release.jks`
  - Alias: `textflow`
  - Store Password: `textflow2026`
  - Key Password: `textflow2026`
