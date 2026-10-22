export interface FaqItem {
  id: string;
  question: string;
  directAnswer: string;
  details: string[];
  category: 'product' | 'compliance' | 'technical' | 'privacy';
}

export const FAQS_DATA: FaqItem[] = [
  {
    id: 'how-it-works',
    question: 'How does ApnaSandesh send business SMS without a cloud gateway?',
    directAnswer: 'ApnaSandesh sends SMS directly through your Android phone’s built-in cellular SIM card using either the system SMS composer or native Android telephony APIs. It requires zero cloud servers, zero API tokens, and zero per-message aggregator fees.',
    details: [
      'In Default Composer Mode (Play Store Edition), ApnaSandesh populates your native messaging app with personalized text and recipient numbers so you verify each dispatch with zero restricted permissions.',
      'In Direct Enterprise Mode (APK build), ApnaSandesh uses Android’s SmsManager with explicit Dual-SIM slot pinning to sequentially dispatch campaigns in the background with real-time carrier delivery confirmation receipts.'
    ],
    category: 'product'
  },
  {
    id: 'trai-limit-india',
    question: 'How does ApnaSandesh handle TRAI’s 100 SMS per day limit in India?',
    directAnswer: 'ApnaSandesh includes an intelligent SIM Quota Monitor calibrated for India’s TRAI TCCCPR limit of 100 SMS/day per personal SIM. It alerts you when a campaign approaches or exceeds your carrier’s threshold, helping you divide dispatches across multiple days or dual SIM cards.',
    details: [
      'Personal prepaid and postpaid SIM cards in India (Jio, Airtel, Vodafone Idea, BSNL) are subject to a TRAI-mandated fair-use cap of 100 outgoing SMS per day (after which carriers either block SMS or charge ₹0.50–₹1.00 per SMS).',
      'ApnaSandesh never promises to magically bypass carrier limits. Instead, it respects telecom regulations and provides transparent pacing controls (1–10 second delays between sends) to safeguard your SIM from anti-spam throttling.'
    ],
    category: 'compliance'
  },
  {
    id: 'privacy-internet-permission',
    question: 'Why doesn’t ApnaSandesh request the INTERNET permission on Android?',
    directAnswer: 'ApnaSandesh completely omits android.permission.INTERNET from its AndroidManifest.xml. This guarantees at the operating system level that customer phone numbers, names, purchase notes, and message drafts can never leave your physical device.',
    details: [
      'Without the INTERNET permission, it is technically impossible for the application to upload your address book, sell your data, or connect to tracking/advertising networks.',
      'All campaign databases reside in an encrypted Room SQLite database sandbox (/data/data/com.apnasandesh.app/) accessible only on your device, with automatic exclusion from unencrypted cloud backups.'
    ],
    category: 'privacy'
  },
  {
    id: 'dual-sim-support',
    question: 'Can I choose which SIM card to send messages from on Dual-SIM phones?',
    directAnswer: 'Yes. In ApnaSandesh Direct Enterprise Edition, you can explicitly pin campaigns to SIM 1 (e.g. Jio Business) or SIM 2 (e.g. Airtel Personal). The app detects active subscription IDs and never silently switches SIMs during a campaign.',
    details: [
      'ApnaSandesh queries SubscriptionManager to display the carrier network name, slot index, and subscription ID for each active SIM.',
      'If your chosen SIM runs out of balance or is removed, the campaign automatically pauses with an actionable alert rather than silently billing your other SIM.'
    ],
    category: 'technical'
  },
  {
    id: 'opt-out-suppression',
    question: 'What is the Double Opt-Out Suppression Firewall and how does it protect me?',
    directAnswer: 'The Double Opt-Out Suppression Firewall is an immutable local database table that permanently records any customer who replies STOP or asks not to be contacted. Once marked, ApnaSandesh blocks their number from being selected or messaged in any future campaign.',
    details: [
      'Even if you accidentally re-import that customer in a new CSV spreadsheet next month, ApnaSandesh cross-references the suppression table and strips their number prior to campaign validation.',
      'This guarantees strict compliance with consumer protection laws and prevents annoying customers who have revoked consent.'
    ],
    category: 'compliance'
  },
  {
    id: 'csv-import-schema',
    question: 'What CSV file format does ApnaSandesh support for importing customer lists?',
    directAnswer: 'ApnaSandesh supports standard UTF-8 CSV files with headers: Name, Phone, Tags, Consent, and Notes. The importer automatically detects column headers, trims whitespace, and normalizes phone numbers into E.164 international format (+91).',
    details: [
      'You can download a ready-to-use sample CSV template directly from our documentation page (/sample-customers.csv).',
      'The importer flags duplicate numbers, malformed digits, and missing names before insertion, giving you a clean pre-import summary report.'
    ],
    category: 'technical'
  },
  {
    id: 'gsm7-vs-unicode',
    question: 'What is the difference between GSM-7 and UCS-2 Unicode messages?',
    directAnswer: 'Standard English messages use GSM-7 encoding (160 characters per SMS segment). Adding regional languages (like Hindi/Devanagari) or a single emoji forces the entire message into UCS-2 Unicode (70 characters per segment), which more than doubles your carrier segment usage.',
    details: [
      'For example, a 140-character English text is 1 SMS segment. But adding a single 😊 emoji drops the per-segment limit to 70 characters, turning that same text into 3 separate concatenated SMS segments (67 chars each).',
      'ApnaSandesh features a live encoding inspector that flags non-GSM characters in real time so you avoid unexpected SIM quota consumption.'
    ],
    category: 'technical'
  },
  {
    id: 'play-store-vs-direct-apk',
    question: 'What is the difference between the Google Play Edition and Direct Enterprise APK?',
    directAnswer: 'The Google Play Edition complies with Google’s SMS restrictions by delegating dispatches to your device’s default SMS app with zero restricted permissions. The Direct Enterprise APK uses native SmsManager for fully autonomous background dispatch with delivery reports.',
    details: [
      'Google Play strictly restricts the SEND_SMS permission for store-listed apps. Therefore, the Play Store edition uses Android Intent.ACTION_SENDTO (smsto:) where you tap Send in your SMS app for each customer.',
      'The Direct Enterprise APK is distributed directly via apnasandesh.trustoryx.digital/download for registered business owners who deliberately grant SEND_SMS to automate 30–80 sequential sends hands-free.'
    ],
    category: 'product'
  },
  {
    id: 'commercial-vs-transactional',
    question: 'Can I send promotional sales blasts to strangers with ApnaSandesh?',
    directAnswer: 'No. ApnaSandesh is designed exclusively for legitimate relationship-based business communication with existing customers who have provided affirmative opt-in consent (such as appointments, order updates, invoices, and service reminders).',
    details: [
      'Sending unsolicited marketing blasts to purchased lead lists violates telecom regulations (such as India’s TRAI TCCCPR) and carrier Acceptable Use Policies. Carriers actively flag and suspend personal SIM cards engaged in high-velocity spamming.',
      'All promotional templates in ApnaSandesh enforce the 10:00 AM – 9:00 PM IST sending window and include mandatory "Reply STOP to opt out" instructions.'
    ],
    category: 'compliance'
  },
  {
    id: 'backup-and-export',
    question: 'How do I backup or transfer my customer database to a new phone?',
    directAnswer: 'You can export your complete customer directory and campaign history to a standard CSV file at any time via Android’s Storage Access Framework (SAF). Simply save the file to your SD card, Google Drive, or transfer it to your new phone.',
    details: [
      'Because ApnaSandesh disables unencrypted system cloud backups (android:allowBackup="false") to protect sensitive customer data, manual CSV export is the recommended secure migration path.',
      'Your exported CSV can be imported into ApnaSandesh on your new Android device in under 30 seconds.'
    ],
    category: 'privacy'
  }
];
