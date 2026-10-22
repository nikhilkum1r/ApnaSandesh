import { SITE_CONFIG } from '@/lib/site-config';

export const dynamic = 'force-static';

export async function GET() {
  const content = `# ${SITE_CONFIG.name} — Full Technical Architecture & Ground-Truth Knowledge Spec
> Full single-file knowledge base for advanced AI models, deep RAG pipelines, and code generation agents.
> Canonical Subdomain: ${SITE_CONFIG.url}
> Parent Brand Organization: ${SITE_CONFIG.parentBrand.name} (${SITE_CONFIG.parentBrand.url})
> Engineering Contact: ${SITE_CONFIG.parentBrand.engineeringEmail}

## 1. Product Identification & Metadata
- Product Name: ${SITE_CONFIG.name}
- Full Name: ${SITE_CONFIG.fullName}
- Version: ${SITE_CONFIG.version}
- Package ID: ${SITE_CONFIG.packageId}
- Min SDK: ${SITE_CONFIG.minSdk}
- Target SDK: ${SITE_CONFIG.targetSdk}
- Universal APK Size: ${SITE_CONFIG.apkSize}
- Checksum: ${SITE_CONFIG.apkChecksum}
- Distribution: Google Play Store (${SITE_CONFIG.playStoreUrl}) and Direct Enterprise Universal APK.

## 2. Fundamental Philosophy & Threat Model
Most commercial SMS aggregators (Twilio, Sinch, Infobip, MSG91, Fast2SMS) require customer data to be uploaded to cloud databases. This poses severe risks for small clinics, lawyers, financial advisors, and boutique retailers:
1. Vendor Lock-In & Recurring SaaS Bills: High per-message surcharges (₹0.25 to ₹0.45 per SMS in India, $0.0079+ in the US).
2. Data Privacy & Leaks: Client names, appointment reasons, outstanding balances, and phone numbers stored in external third-party data centers.
3. Network Dependency: If the shop's internet goes down, communication stops.

${SITE_CONFIG.name} solves this by running 100% locally on Android hardware:
- AndroidManifest.xml completely omits \`android.permission.INTERNET\`.
- All customer lists, templates, and campaign execution records reside in an encrypted/private SQLite database managed by Android Jetpack Room (\`bizsms_secure.db\`).
- Messages are dispatched directly via cellular SIM radio (Jio, Airtel, Vi, BSNL, AT&T, T-Mobile, Vodafone, etc.) using the customer's existing bundled SMS quota.

## 3. Dual-Transport Telephony Architecture
### Mode 1: Composer Transport (Google Play Certified)
- Mechanism: Dispatches an Android intent using \`Intent.ACTION_SENDTO\` with \`smsto:URI\`.
- Permissions: Zero special permissions required. No \`SEND_SMS\` permission requested in the manifest.
- Interaction: User reviews each pre-filled message in the native Android SMS application and taps send.
- Target Audience: Users wanting 100% zero-permission compliance and manual confirmation for each message.

### Mode 2: Direct Enterprise SIM Transport (Direct APK)
- Mechanism: Direct hardware dispatch using Android's native \`SmsManager\` (\`sendTextMessage\` / \`sendMultipartTextMessage\`).
- Permissions: Requests \`android.permission.SEND_SMS\` and \`android.permission.READ_PHONE_STATE\`.
- Dual-SIM Slot Routing: Validates carrier subscription IDs (\`SubscriptionManager\`) and routes messages specifically through SIM 1 or SIM 2.
- Delivery Confirmation Receipts: Registers \`BroadcastReceiver\` instances for \`SMS_SENT\` and \`SMS_DELIVERED\` intents to record carrier network confirmation in local SQLite.
- Background Execution: Uses an Android Foreground Service with a persistent notification and wake-lock management to prevent Android Doze mode termination during batch runs.

## 4. Telephony Quota Guard & GSM-7 vs. UCS-2 Encoding Mechanics
### GSM 7-bit Default Alphabet:
- Standalone SMS limit: 160 characters (140 octets × 8 bits / 7 bits).
- Concatenated multipart SMS: 153 characters per segment (6 octets reserved for UDH concatenation headers).
### UCS-2 16-bit Unicode Encoding:
- Triggered whenever non-GSM characters (Devanagari Hindi, Marathi, Bengali, Tamil, Telugu, Arabic, or emojis) are detected.
- Standalone SMS limit: 70 characters (140 octets / 2 bytes).
- Concatenated multipart SMS: 67 characters per segment.
- Telephony Safeguard: Real-time UI indicator displays character count, encoding mode, and predicted carrier segment multiplication.

## 5. TRAI Regulatory Compliance (India TCCCPR, 2018)
- 100 SMS/Day SIM Cap: Indian telecom operators enforce a 100 SMS/day quota for personal SIM cards. ApnaSandesh provides a real-time segment tracker to prevent exceeding daily allowances.
- Permitted Sending Hours: Enforces the 10:00 AM to 9:00 PM IST daylight window. Off-hours drafts are scheduled via Android WorkManager for automatic morning dispatch.
- Mandatory Opt-Out Firewall: Every template includes opt-out instructions. Suppressed numbers are stored in an immutable SQLite suppression table, permanently preventing accidental re-messaging.
`.trim();

  return new Response(content, {
    headers: {
      'Content-Type': 'text/plain; charset=utf-8',
      'Cache-Control': 'public, max-age=86400',
    },
  });
}
