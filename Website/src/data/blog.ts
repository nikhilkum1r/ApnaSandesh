export interface BlogPost {
  slug: string;
  title: string;
  summary: string;
  publishedAt: string;
  readTime: string;
  author: {
    name: string;
    role: string;
  };
  tags: string[];
  content: string; // Markdown / HTML formatted text
}

export const BLOG_POSTS: BlogPost[] = [
  {
    slug: 'apnasandesh-v1-release-offline-first-business-sms',
    title: 'ApnaSandesh v1.0 Release: Offline-First Business SMS for Android',
    summary: 'Announcing the production launch of ApnaSandesh v1.0. Discover how small businesses can manage customers locally and send automated SIM SMS without cloud fees or data leaks.',
    publishedAt: '2026-10-22',
    readTime: '4 min read',
    author: {
      name: 'TruStoryX Digital Team',
      role: 'Core Systems Engineering'
    },
    tags: ['Product Release', 'Android', 'Offline-First', 'Privacy'],
    content: `
## A New Baseline for Local Business Communication

Today, TruStoryX Digital is proud to release **ApnaSandesh v1.0**, a 100% offline-first native Android business SMS application designed specifically for independent retailers, clinics, salons, freelancers, and service contractors.

For over a decade, small business owners have faced a frustrating dilemma: either manually type identical reminders to 50 clients every day in the stock Android messaging app, or pay ₹12,000 to ₹35,000 annually to a cloud CRM aggregator while uploading confidential customer contact lists to remote servers.

ApnaSandesh eliminates this compromise entirely:
- **Zero Cloud Subscriptions**: Operates on your physical Android smartphone and leverages your existing cellular mobile plan.
- **100% On-Device Privacy**: Does not request or declare \`android.permission.INTERNET\`. Your customer records, phone numbers, and notes never leave your handset.
- **Dual-Transport Architecture**: Choose between the zero-permission Google Play system composer handoff or the high-throughput Direct Enterprise SIM transport with delivery confirmation receipts.

---

### What's Inside ApnaSandesh v1.0

1. **Local Customer Directory & E.164 Normalizer**: Add, tag, search, and group your local clients. Import spreadsheets in standard CSV format or pick directly from your device address book.
2. **Double Opt-Out Suppression Firewall**: Never message an unhappy or opted-out customer twice. Any recipient who replies STOP is permanently suppressed in an immutable SQLite table.
3. **10 Built-In Business Templates with Live Variable Substitution**: Covers order confirmations, dental & medical appointment reminders, overdue payment notices, service completion alerts, and festive greetings.
4. **Interactive GSM-7 vs. UCS-2 Segment & Quota Calculator**: Know your exact character count, encoding mode, and telecom SIM quota impact before sending a single message.
5. **Dual-SIM Slot Selection**: Explicitly pin campaigns to SIM 1 or SIM 2 on dual-SIM Android devices, complete with carrier network name and subscription ID validation.

ApnaSandesh v1.0 is available immediately on [our Download Hub](/download) with SHA-256 cryptographic verification checksums.
`
  },
  {
    slug: 'india-small-shops-trai-sms-compliance-guide',
    title: 'How Small Shops in India Can Send Order & Appointment SMS Without Violating TRAI Rules',
    summary: 'A plain-English operational guide to India’s TRAI TCCCPR regulations, personal SIM 100 SMS/day limits, and how to maintain legal compliance without costly DLT portals.',
    publishedAt: '2026-10-18',
    readTime: '6 min read',
    author: {
      name: 'Legal & Telecom Practice',
      role: 'TruStoryX Digital Advisory'
    },
    tags: ['Compliance', 'TRAI', 'India Business', 'DLT'],
    content: `
## Understanding Telecom Regulations for Indian Retailers

If you run a retail shop, medical practice, coaching institute, or service repair workshop in India, keeping your existing customers informed via SMS is essential. However, the Telecom Regulatory Authority of India (TRAI) enforces strict consumer safeguards under the **Telecom Commercial Communications Customer Preference Regulations (TCCCPR, 2018)**.

Many business owners are confused about what is permitted on their personal SIM cards (Jio, Airtel, Vodafone Idea, BSNL) versus what requires a commercial enterprise Distributed Ledger Technology (DLT) account.

Here is the operational truth every Indian business owner must know:

---

### 1. The 100 SMS Per Day Personal SIM Threshold

All personal prepaid and postpaid SIM cards in India operate under a TRAI-mandated fair-use ceiling of **100 outgoing SMS per day per SIM**.

- Within the first 100 SMS: Dispatched at normal bundled plan rates (typically free under unlimited recharges).
- Beyond 100 SMS: Carriers either disable outgoing SMS for the remainder of the calendar day, or apply a non-commercial surcharge of ₹0.50 to ₹1.00 per SMS.
- High-velocity spikes (e.g. attempting to blast 500 SMS in 60 seconds): Trigger automated carrier anti-spam algorithms that can permanently suspend outgoing SMS capability on your SIM card.

**How ApnaSandesh Protects You:**
ApnaSandesh incorporates an active SIM Quota Monitor calibrated for the 100 SMS/day threshold. When building a campaign, ApnaSandesh warns you if your recipient list exceeds safe daily limits, allowing you to split the campaign across two days or utilize Dual-SIM routing.

---

### 2. Transactional vs. Unsolicited Promotional Communication

TRAI draws a firm boundary between two types of messages:

#### A. Relationship-Based Transactional Messages (Permitted)
Messages sent to existing clients who have had a direct commercial transaction with you (e.g., appointment confirmations, repair completion status, invoice receipts, pickup alerts). These are legitimate customer service communications.

#### B. Cold Promotional Blasts (Strictly Prohibited on Personal SIMs)
Sending mass marketing pitches, sales discounts, or cold offers to numbers scraped from directories or purchased lead databases is illegal. Recipients on the National Do Not Disturb (NDND) registry can report your number via the 1909 grievance portal, resulting in immediate telecom blacklisting.

---

### 3. Golden Rules for Compliant Business Messaging

1. **Only Message Opted-In Customers**: Collect consent when clients visit your shop, book an appointment, or receive an invoice.
2. **Observe the 10:00 AM to 9:00 PM IST Window**: Never dispatch business reminders outside daylight hours. ApnaSandesh enforces this window in app settings.
3. **Include Sender Identity**: Always identify your shop or firm clearly in the message body (e.g., *"From Sharma Dental Clinic"*).
4. **Honor Opt-Outs Immediately**: If a customer replies STOP or indicates they no longer want updates, use ApnaSandesh’s 1-tap suppression to permanently block their number.
`
  },
  {
    slug: 'gsm7-vs-ucs2-unicode-sms-cost-calculator',
    title: 'Understanding GSM-7 vs. UCS-2 Unicode: Why One Emoji Doubles Your SMS Cost',
    summary: 'The technical mechanics of SMS character encoding explained. Why 160 characters becomes 70 characters the instant you type an emoji or Hindi Devanagari character.',
    publishedAt: '2026-10-14',
    readTime: '5 min read',
    author: {
      name: 'Telephony Architecture',
      role: 'TruStoryX Digital Engineering'
    },
    tags: ['Technical Deep Dive', 'GSM-7', 'Unicode', 'SMS Segmentation'],
    content: `
## Why Did My 80-Character Message Split into Multiple Segments?

Every month, thousands of business owners are surprised to find their SMS allowances exhausted twice as fast as expected. The culprit is almost always character encoding.

Understanding how cellular telecommunication networks process text messages is critical for keeping your dispatches efficient and within your carrier quotas.

---

### The GSM-7 Standard: 160 Characters per Segment

In standard cellular specifications (3GPP TS 23.038), Short Message Service (SMS) payloads are limited to **140 octets (1,120 bits)** of data.

Standard Latin letters (A–Z, a–z), numbers (0–9), and a specific set of basic punctuation marks are encoded using the **7-bit GSM Default Alphabet**:
- 1,120 bits ÷ 7 bits per character = **160 characters** in a single standalone SMS.

If your message exceeds 160 characters, cellular networks split it into multiple parts. Because each multipart segment requires a 6-octet **User Data Header (UDH)** for reassembly instructions on the recipient's phone:
- Each concatenated segment holds only **153 characters**.
- A 165-character GSM-7 message consumes **2 SMS segments** (153 + 12).

---

### Enter UCS-2 Unicode: 70 Characters per Segment

The moment your message contains even a single character outside the 7-bit GSM alphabet:
- Non-Latin scripts (Hindi/Devanagari, Marathi, Tamil, Bengali, Arabic, Cyrillic)
- Modern graphical emojis (😊, 🚗, 🎉, 🙏)
- Special punctuation marks like curly quotes or smart apostrophes

The cellular carrier's SMSC (Short Message Service Center) must immediately switch the entire message encoding to **UCS-2 (16-bit Unicode)**.

At 16 bits per character:
- 1,120 bits ÷ 16 bits = **70 characters** for a single standalone SMS.
- For multipart concatenated Unicode messages (with UDH headers): **67 characters per segment**.

---

### The Real-World Impact

Consider this common business reminder:
\`\`\`text
Hello Rajesh, your appointment at Dr. Mehta Clinic is confirmed for tomorrow at 4 PM. Please arrive 10 min early.
\`\`\`
- Length: 114 characters.
- Encoding: GSM-7.
- **Segments: 1 SMS segment.**

Now add a single friendly smiley emoji at the end:
\`\`\`text
Hello Rajesh, your appointment at Dr. Mehta Clinic is confirmed for tomorrow at 4 PM. Please arrive 10 min early. 😊
\`\`\`
- Length: 116 characters.
- Encoding: **UCS-2 Unicode**.
- **Segments: 2 SMS segments** (exceeds the 70-character limit, split into 67 + 49 chars).

By adding one emoji, you consumed **200% of your SIM quota** for the exact same message.

ApnaSandesh features an integrated [Live SMS Segment Calculator](/templates) that inspects character encoding in real time and highlights non-GSM characters before you hit send.
`
  },
  {
    slug: 'why-we-built-apnasandesh-without-internet-permission',
    title: 'Why We Built ApnaSandesh Without android.permission.INTERNET',
    summary: 'An engineering manifesto on radical privacy by design. Why omitting network permissions is the only verifiable guarantee against small business customer data leakage.',
    publishedAt: '2026-10-10',
    readTime: '5 min read',
    author: {
      name: 'Security & Systems Team',
      role: 'TruStoryX Digital Engineering'
    },
    tags: ['Architecture', 'Security', 'Privacy', 'Android'],
    content: `
## Privacy Promises Are Cheap. Architecture Is Absolute.

Almost every SaaS marketing platform advertises that they *"value your privacy"* and *"encrypt your data in transit"*. Yet every week, another cloud database misconfiguration exposes millions of phone numbers, addresses, and purchase records to the public internet.

When we designed ApnaSandesh for local retailers, doctors, tailors, and auto mechanics, we decided that legal promises in a 40-page terms of service were insufficient. We wanted a product where privacy could be mathematically and cryptographically verified by anyone with basic Android developer tools.

That is why ApnaSandesh contains **zero lines of networking code** and does not even request \`android.permission.INTERNET\`.

---

### What Android's INTERNET Permission Really Means

On the Android operating system, an app cannot open a TCP/UDP socket, resolve a DNS query, or connect to any web address unless it explicitly declares:
\`\`\`xml
<uses-permission android:name="android.permission.INTERNET" />
\`\`\`
in its compiled \`AndroidManifest.xml\` file.

Because ApnaSandesh completely omits this declaration:
1. **The Operating System Enforces the Block**: Even if a rogue library or dependency attempted to transmit data, the Linux kernel and Android runtime would immediately reject the socket creation with a \`SecurityException\`.
2. **Zero Analytics or Tracking SDKs**: We cannot include Google Analytics, Facebook Pixel, Mixpanel, or advertising trackers, because none of them can phone home.
3. **No Account Servers**: There are no passwords to hack, no remote user accounts to breach, and no centralized databases to leak.

---

### What About Customer Backups?

Because ApnaSandesh stores customer records in a local SQLite database using Android's Room persistence library (\`/data/data/com.apnasandesh.app/\`), we also set:
\`\`\`xml
android:allowBackup="false"
\`\`\`
This prevents Google Cloud's automated device backup from quietly copying your customer address book to unencrypted remote cloud drives without your knowledge.

Instead, ApnaSandesh puts data sovereignty entirely in your hands: you can export your verified customer directory and campaign logs to a clean CSV file anytime using the native Android Storage Access Framework (SAF).

You own your phone. You own your SIM. You own your customer relationships.
`
  }
];
