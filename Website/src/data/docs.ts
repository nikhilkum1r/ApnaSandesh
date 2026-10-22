export interface DocSection {
  id: string;
  title: string;
  description: string;
  subsections: {
    id: string;
    title: string;
    content: string;
  }[];
}

export const DOCS_DATA: DocSection[] = [
  {
    id: 'getting-started',
    title: '1. Getting Started & Onboarding',
    description: 'First-time setup, onboarding wizard, and initial business profile configuration.',
    subsections: [
      {
        id: 'installation',
        title: 'Installation & Editions',
        content: `
ApnaSandesh is available in two distinct editions tailored to your operating model:
- **Standard Edition (Google Play)**: Utilizes the Android system SMS composer handoff. Requires zero special permissions. Best for small-volume senders (5–25 reminders/day).
- **Direct Enterprise Edition (Direct APK)**: Utilizes Android SmsManager with dual-SIM selection for hands-free sequential dispatch and carrier delivery confirmation receipts.

Download the latest verified APK directly from our [Download Hub](/download).
        `
      },
      {
        id: 'business-profile',
        title: 'Configuring Your Business Profile',
        content: `
On initial launch, complete your Business Profile in Settings:
- **Business Name**: Used in template tokens (\`{business_name}\`) to identify your establishment to customers.
- **Owner Display Name**: Used for personal sign-offs (\`{owner_name}\`).
- **Business Category**: Tailors default template suggestions (e.g., Retail, Healthcare Clinic, Salon, Auto Workshop, Professional Services).
- **Primary Contact Phone**: Included in templates for customer callbacks and rescheduling inquiries.
- **Default Country Calling Code**: Sets the fallback for phone number normalization (defaults to \`+91\` for India).
        `
      }
    ]
  },
  {
    id: 'customer-management',
    title: '2. Customer Management & CSV Import',
    description: 'Managing local customer directories, contact tags, and importing spreadsheets.',
    subsections: [
      {
        id: 'contact-picker',
        title: 'System Contact Picker (Zero Permissions)',
        content: `
To add customers without granting global contacts access:
1. Navigate to **Customers** and tap **+ Add Customer**.
2. Tap **Pick from Address Book**.
3. Select an existing contact using Android's native system picker (\`ActivityResultContracts.PickContact()\`).
4. The system securely returns only the selected contact's name and number to ApnaSandesh without reading your broader address book.
        `
      },
      {
        id: 'csv-schema',
        title: 'CSV Spreadsheet Import Specification',
        content: `
ApnaSandesh supports bulk customer onboarding via standard CSV files. The importer automatically detects headers and trims unnecessary whitespace.

**Standard CSV Column Headers:**
\`\`\`csv
Name,Phone,Tags,Consent,Notes
Rajesh Kumar,+919820012345,VIP;Retail,OPTED_IN,Purchased gold necklace
Dr. Priya Sharma,9845077123,Clinic,OPTED_IN,Next root canal due
Vikas Patel,09811054321,Auto,OPTED_IN,Honda City KA05-1234
Sunita Verma,+919712044889,Grocery,OPTED_IN,Prefers evening deliveries
\`\`\`

- **Name** (Required): Customer or contact name.
- **Phone** (Required): 10-digit mobile number, with or without leading zero, or full E.164 format. ApnaSandesh automatically normalizes to \`+91\`.
- **Tags** (Optional): Semicolon-separated labels for grouping (e.g., \`VIP;Wholesale;Zone1\`).
- **Consent** (Required for promotional templates): Must be \`OPTED_IN\`.
- **Notes** (Optional): Operational context (e.g. vehicle model or order reference).

You can [download our pre-formatted sample CSV file](/sample-customers.csv) to test immediately.
        `
      },
      {
        id: 'phone-normalizer',
        title: 'E.164 Phone Normalization & Duplicate Detection',
        content: `
During import, ApnaSandesh's telephony validator runs every entry through a multi-pass normalizer:
- Strips spaces, hyphens, and parenthesis (\`98200-12345\` → \`9820012345\`).
- Handles Indian trunk prefixes (\`09820012345\` → \`+919820012345\`).
- Rejects numbers with invalid digit counts (less than 10 or exceeding 15 digits).
- Flags duplicate phone numbers and presents a merge/skip dialog so you never pay to message the same number twice.
        `
      }
    ]
  },
  {
    id: 'suppression-firewall',
    title: '3. Double Opt-Out Suppression Firewall',
    description: 'Permanent on-device suppression list protecting consumer privacy and regulatory compliance.',
    subsections: [
      {
        id: 'how-opt-out-works',
        title: 'How Suppression Works',
        content: `
When a client requests not to receive future SMS updates:
1. Open the customer's profile and toggle **Opted Out (DND)**.
2. ApnaSandesh instantly creates an immutable record in the \`SuppressionEntry\` SQLite table.
3. The customer is immediately greyed out and excluded from all subsequent recipient pickers.
4. If a future CSV file contains that same phone number, the importer automatically detects the suppression lock and strips it from the import batch.
        `
      }
    ]
  },
  {
    id: 'campaign-state-machine',
    title: '4. Campaign Workflow & State Machine',
    description: 'Transaction-safe campaign lifecycle preventing accidental re-sends or phantom states.',
    subsections: [
      {
        id: 'state-lifecycle',
        title: 'Campaign State Transitions',
        content: `
ApnaSandesh processes every campaign through a deterministic Room SQLite transaction lifecycle:

1. **DRAFT**: Message template selected, variables bound, initial recipient filters applied.
2. **VALIDATED**: Phone numbers verified, suppressed contacts stripped, segment count estimated.
3. **AWAITING_CONFIRMATION**: Review screen displaying exact recipient count, total estimated SMS segments, and SIM quota warning.
4. **SUBMITTED**: User confirms dispatch; background execution begins via Android Foreground Service.
5. **SENT**: Telephony layer confirms carrier acceptance via \`SmsManager.STATUS_ON_ICC_SENT\`.
6. **DELIVERED**: Carrier network confirms delivery receipt via BroadcastReceiver (\`SMS_DELIVERED\`).
7. **FAILED / UNKNOWN**: Flagged with detailed carrier diagnostic code (e.g. \`RESULT_ERROR_NO_SERVICE\`, \`RESULT_ERROR_LIMIT_EXCEEDED\`).
        `
      }
    ]
  },
  {
    id: 'database-architecture',
    title: '5. Room SQLite Database Schema',
    description: 'Complete breakdown of the 11 on-device SQLite tables powering ApnaSandesh.',
    subsections: [
      {
        id: 'tables-overview',
        title: '11-Table Normalized SQLite Architecture',
        content: `
ApnaSandesh stores all records in an on-device SQLite database managed by Android Room (\`bizsms_secure.db\`):

1. **BusinessProfile**: Single-row entity storing business name, category, phone, and signature.
2. **Customer**: UUID primary key, normalized E.164 phone, full name, notes, creation timestamp.
3. **CustomerGroup**: Named cohorts (e.g., "Saturday Salon Clients", "Wholesale Buyers").
4. **GroupMembership**: Many-to-many junction table linking Customers to Groups.
5. **MessageTemplate**: Built-in and user-customized templates with category and variable slots.
6. **Campaign**: Campaign master record (title, template snapshot, status, scheduled time, dispatched count).
7. **CampaignRecipient**: Individual customer assignment per campaign with unique composite index \`(campaign_id, customer_id)\` to prevent duplicate sends.
8. **SendAttempt**: Telephony audit record storing attempt timestamp, SIM slot used, carrier result code, and status.
9. **ConsentRecord**: Audit log of consent acquisition (timestamp, source e.g. "IN_STORE_INVOICE", proof).
10. **SuppressionEntry**: Phone number blacklist hash for permanently opted-out recipients.
11. **AppSettings**: Local user preferences (pacing interval, SIM preference, TRAI compliance window).
        `
      }
    ]
  }
];
