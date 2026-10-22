# AUTONOMOUS ENGINEERING MISSION: PRODUCTION-READY ANDROID BUSINESS SMS APP

You are a Principal Android Software Engineer operating in full autonomous execution mode. Your mission is to inspect the current workspace, architect, implement, compile, test, debug, verify, and package a complete, production-ready native Android Business SMS application from start to finish in a single continuous engineering workflow.

---

## 0. OPERATING RULES & HUMAN-ENGINEER WORKFLOW CONTRACT

1. **No Stops, No Prototypes, No Stubs**:
   - Do NOT stop after generating a plan or ask for permission to continue. Execute Phases 1 through 8 continuously until every deliverable is complete and verified.
   - Do NOT leave `TODO()`, placeholder composables, fake in-memory repositories, hardcoded dummy lists, or unimplemented click handlers anywhere in production source sets.
   - Every screen must be wired to real Room database flows, DataStore preferences, and domain use cases.

2. **Work Like a Senior Human Engineer (Incremental Build-Verify Loops)**:
   - Inspect the filesystem and environment before writing a single line of code.
   - Build the project in strict dependency order: **Gradle/Toolchain Setup -> Core Models & Database -> Domain Logic & Unit Tests -> Data Repositories -> UI Features & Navigation -> Integration & QA -> Release Packaging**.
   - Run `./gradlew compileDebugKotlin` after completing each architectural layer to catch and fix compilation, KSP, Hilt, or Room schema errors immediately rather than accumulating hundreds of errors at the end.

3. **Strict Verification Honesty**:
   - Never claim a build succeeded, a test passed, or a device check worked unless you actually executed the command in the terminal and inspected the output.
   - If no physical device or emulator is attached via `adb devices`, explicitly record that instrumentation/device tests were blocked by hardware unavailability while ensuring 100% of JVM unit tests and APK compilation checks execute and pass.

---

## 1. PRODUCT VISION & CORE VALUE PROPOSITION

Build a production-quality native Android application that empowers small business owners, shopkeepers, independent service providers, local retailers, and freelancers to communicate with their existing customers through their own Android phone number and SIM card.

- **Core Value Proposition**: Use your own phone. Use your own SIM. Manage your customers locally. Send business SMS affordably. No mandatory cloud account or SMS gateway subscription.
- **Target Audience**: Non-technical small business owners operating on tight budgets who need a trustworthy, transparent, offline-first tool.
- **Compliance First**: The priority is a useful, trustworthy, low-cost product—never maximizing message volume by bypassing Android permissions, Google Play policies, carrier quotas, or telecom regulations (such as TRAI DLT rules in India).

---

## 2. TARGET PLATFORM, TECH STACK & ARCHITECTURE

### 2.1 Technology Stack
- **Platform**: Native Android (`minSdk = 26`, `targetSdk = 35`, `compileSdk = 35` — verify against installed Android SDK platforms and adjust to the highest installed stable API >= 34 if 35 is not installed locally, documenting the exact version).
- **Language**: Kotlin (2.0+ or 1.9.24+ compatible with installed AGP) with Coroutines and Flow.
- **UI Framework**: Jetpack Compose with Material 3 (`androidx.compose.material3`), dynamic/accessible typography, and full Light/Dark theme support.
- **Architecture**: Clean modular package structure using MVVM, Repository Pattern, Domain Use Cases, and strict Unidirectional Data Flow (`UiState`, `UiEvent`, `UiEffect`).
- **Dependency Injection**: Dagger Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`) via KSP.
- **Local Persistence**: Room / SQLite with transactional DAOs, explicit indices, foreign keys, UUID strings, schema export enabled, and deterministic migrations.
- **Preferences**: Jetpack DataStore (Preferences) for lightweight onboarding/runtime flags, combined with Room `AppSettings` for structured configuration.
- **Background Work**: `WorkManager` strictly for eligible deferrable local maintenance, orphan state reconciliation, and cleanup tasks.
- **File I/O**: Android Storage Access Framework (SAF) (`ActivityResultContracts.OpenDocument`, `CreateDocument`) and `ActivityResultContracts.PickContact`.
- **Testing**: JUnit 4/5, Kotlin Coroutines Test, Turbine (or flow collection helpers), Room in-memory testing, and Compose UI / AndroidX Instrumentation tests.
- **Offline-First & Zero Backend**: No cloud backend, no mandatory login, no third-party analytics/ad SDKs, no network permissions (`INTERNET` permission must NOT be declared in `AndroidManifest.xml`).

### 2.2 Required Package / Module Structure
Organize all source code under clean, decoupled packages (or Gradle modules) matching this exact structure:
- `core/model` — Pure Kotlin domain models, enums, value objects, and state definitions.
- `core/database` — Room database, entities, DAOs, type converters, migrations, and seeding callbacks.
- `core/common` — Dispatchers, Result wrappers, Clock/UUID providers, safe logger (redacting PII), and formatting utilities.
- `data/customer` — Customer, Group, Consent, and Suppression repositories + SAF CSV parser/exporter + Contact picker mapper.
- `data/campaign` — Campaign, Template, Recipient, and SendAttempt repositories + report exporter.
- `domain/validation` — E.164 phone number normalizer/validator, duplicate detector, CSV row validator, and SMS 7-bit/UCS-2 character & segment calculator.
- `domain/consent` — Consent tracking rules, opt-out processor, and mandatory suppression firewall.
- `domain/campaign` — Campaign creation, recipient resolution, template personalization engine, state machine transition validator, and reconciliation worker logic.
- `domain/transport` — `SmsTransport` abstraction, `ComposerSmsTransport` (Transport 1), `DirectSimSmsTransport` (Transport 2), SIM subscription discovery, and policy eligibility gate.
- `feature/dashboard` — Onboarding, Business Profile setup/edit, and Dashboard screen + ViewModels.
- `feature/customers` — Customer Directory, Add/Edit Customer, Customer Import (Contacts + CSV), and Customer Groups & Tags screens + ViewModels.
- `feature/templates` — Message Template Library and Template Editor/Preview screens + ViewModels.
- `feature/campaigns` — Campaign Editor, Recipient Selection, Campaign Review & Confirmation, and Sending / Handoff Status coordinator screens + ViewModels.
- `feature/history` — Campaign History list, Campaign Detail & Recipient Outcomes, Recovery actions, and Report export screens + ViewModels.
- `feature/settings` — Settings, Transport Policy Gate, Privacy Controls, Data Wipe/Export, and India/TRAI Compliance Guide screens + ViewModels.

Keep all Android framework dependencies (`Context`, `Intent`, `SmsManager`, `SubscriptionManager`, `ContentResolver`) behind clean interfaces injected via Hilt so domain and viewmodel logic can be 100% unit-tested on the JVM.

---

## 3. LOCAL DATA MODEL (ROOM DATABASE SPECIFICATION)

Implement all 11 required Room entities with `String` UUID primary keys, explicit foreign keys, indices, and `@Transaction` integrity:

1. **`BusinessProfile` (`business_profiles`)**
   - `id: String` (PK, default singleton ID `"default_profile"`)
   - `businessName: String`
   - `ownerDisplayName: String`
   - `businessCategory: String`
   - `communicationPhone: String` (user-entered & verified, never assumed readable from SIM)
   - `businessAddress: String?`
   - `defaultCountryCallingCode: String` (e.g., `"+91"`, `"+1"`)
   - `defaultMessageSignature: String`
   - `updatedAtEpochMillis: Long`

2. **`Customer` (`customers`)**
   - `id: String` (PK, UUID)
   - `fullName: String`
   - `rawPhoneNumber: String`
   - `normalizedPhoneE164: String` (Indexed, Unique constraint to prevent duplicates)
   - `isPhoneValid: Boolean`
   - `tagsCsv: String` (or normalized tag storage with clean query filtering)
   - `consentStatus: String` (`GRANTED`, `UNKNOWN`, `OPTED_OUT`)
   - `consentSource: String` (e.g., `"MANUAL_ENTRY"`, `"CSV_IMPORT"`, `"CONTACT_PICKER"`, `"CUSTOMER_REQUEST"`)
   - `consentTimestampMillis: Long`
   - `isOptedOut: Boolean` (Indexed)
   - `optedOutAtMillis: Long?`
   - `notes: String` (with UI guidance against storing sensitive financial/health PII)
   - `createdAtMillis: Long`
   - `updatedAtMillis: Long`

3. **`CustomerGroup` (`customer_groups`)**
   - `id: String` (PK, UUID)
   - `name: String` (Unique index)
   - `description: String`
   - `createdAtMillis: Long`

4. **`GroupMembership` (`group_memberships`)**
   - `groupId: String` (FK -> `CustomerGroup.id` ON DELETE CASCADE, Indexed)
   - `customerId: String` (FK -> `Customer.id` ON DELETE CASCADE, Indexed)
   - `addedAtMillis: Long`
   - Primary Key: `primaryKeys = ["groupId", "customerId"]`

5. **`MessageTemplate` (`message_templates`)**
   - `id: String` (PK, UUID)
   - `title: String`
   - `category: String` (`ORDER_CONFIRMATION`, `APPOINTMENT_REMINDER`, `PAYMENT_REMINDER`, `SERVICE_COMPLETION`, `DELIVERY_UPDATE`, `THANK_YOU`, `ANNOUNCEMENT`, `OPT_IN_PROMOTION`, `FESTIVAL_GREETING`, `CUSTOM`)
   - `bodyTemplate: String` (supports safe placeholders: `{{customer_name}}`, `{{business_name}}`, `{{owner_name}}`, `{{business_phone}}`, `{{signature}}`)
   - `isPromotional: Boolean`
   - `isSystemDefault: Boolean`
   - `updatedAtMillis: Long`

6. **`Campaign` (`campaigns`)**
   - `id: String` (PK, UUID)
   - `name: String`
   - `templateId: String?` (FK -> `MessageTemplate.id` ON DELETE SET NULL, Indexed)
   - `rawMessageTemplate: String`
   - `transportType: String` (`COMPOSER_HANDOFF`, `DIRECT_SIM`)
   - `selectedSubscriptionId: Int?`
   - `status: String` (`DRAFT`, `VALIDATED`, `AWAITING_CONFIRMATION`, `IN_PROGRESS`, `COMPLETED`, `PARTIAL`, `CANCELLED`, `FAILED`)
   - `totalRecipients: Int`
   - `createdAtMillis: Long`
   - `updatedAtMillis: Long`
   - `confirmedAtMillis: Long?`
   - `completedAtMillis: Long?`

7. **`CampaignRecipient` (`campaign_recipients`)**
   - `id: String` (PK, UUID)
   - `campaignId: String` (FK -> `Campaign.id` ON DELETE CASCADE, Indexed)
   - `customerId: String` (FK -> `Customer.id` ON DELETE RESTRICT, Indexed)
   - `recipientNameSnapshot: String`
   - `normalizedPhoneSnapshot: String`
   - `resolvedMessageBody: String`
   - `characterCount: Int`
   - `estimatedSegments: Int`
   - `encodingType: String` (`GSM_7BIT`, `UCS_2`)
   - `state: String` (`DRAFT`, `VALIDATED`, `AWAITING_CONFIRMATION`, `HANDED_OFF_TO_MESSAGING_APP`, `QUEUED`, `SUBMITTED`, `SENT`, `DELIVERED`, `FAILED`, `UNKNOWN`, `CANCELLED`)
   - `lastErrorCode: String?`
   - `lastErrorMessage: String?`
   - `updatedAtMillis: Long`
   - **Mandatory Unique Index**: `Index(value = ["campaignId", "customerId"], unique = true)` to strictly prevent duplicate recipient records within the same campaign.

8. **`SendAttempt` (`send_attempts`)**
   - `id: String` (PK, UUID)
   - `campaignRecipientId: String` (FK -> `CampaignRecipient.id` ON DELETE CASCADE, Indexed)
   - `campaignId: String` (Indexed)
   - `transportUsed: String`
   - `subscriptionId: Int?`
   - `outcomeState: String`
   - `errorCode: String?`
   - `errorDetail: String?`
   - `startedAtMillis: Long`
   - `finishedAtMillis: Long?`

9. **`ConsentRecord` (`consent_records`)**
   - `id: String` (PK, UUID)
   - `customerId: String` (FK -> `Customer.id` ON DELETE CASCADE, Indexed)
   - `normalizedPhoneE164: String` (Indexed)
   - `previousStatus: String?`
   - `newStatus: String` (`GRANTED`, `UNKNOWN`, `OPTED_OUT`)
   - `source: String`
   - `note: String`
   - `recordedAtMillis: Long`

10. **`SuppressionEntry` (`suppression_entries`)**
    - `id: String` (PK, UUID)
    - `normalizedPhoneE164: String` (Unique Index — survives even if customer record is edited)
    - `customerId: String?` (Indexed)
    - `reason: String` (e.g., `"USER_OPT_OUT"`, `"STOP_RECEIVED"`, `"MANUAL_SUPPRESSION"`)
    - `suppressedAtMillis: Long`
    - `isActive: Boolean`

11. **`AppSettings` (`app_settings`)**
    - `id: String` (PK, singleton `"app_settings"`)
    - `onboardingCompleted: Boolean`
    - `preferredTransport: String` (`COMPOSER_HANDOFF`, `DIRECT_SIM`)
    - `directSimPolicyAcknowledged: Boolean`
    - `directSimFeatureEnabled: Boolean`
    - `selectedSimSubscriptionId: Int?`
    - `indiaTraiComplianceMode: Boolean`
    - `appendSignatureByDefault: Boolean`
    - `themeMode: String` (`SYSTEM`, `LIGHT`, `DARK`)
    - `updatedAtMillis: Long`

### Database Migration & Seeding Strategy
- Export Room schemas to `$projectDir/schemas`.
- Provide an explicit `Migration(1, 2)` path alongside a database callback or initial initializer that pre-populates the 10 default business `MessageTemplate` items on first launch.

---

## 4. CORE FEATURE & DOMAIN SPECIFICATIONS

### 4.1 Phone Number Normalization & Validation (`domain/validation`)
- Normalize numbers into strict E.164 format (`+[country_code][subscriber_number]`).
- Strip spaces, dashes, parentheses, and dots. Convert leading `00` to `+`.
- If a number starts with a leading `0` (local trunk prefix, e.g., `09876543210` in India) or is a standard 10-digit national number, prepend the configured `defaultCountryCallingCode` from `BusinessProfile` (defaulting to `+91` if configured for India or user-selected country code).
- Validate length (ITU-T E.164 rules: 8 to 15 digits after `+`, non-zero country code).
- Flag invalid numbers (`isPhoneValid = false`) and block them from entering `VALIDATED` campaign recipient lists.
- Detect duplicates by `normalizedPhoneE164` both during single customer creation and batch CSV/Contact imports.

### 4.2 Opt-Out & Suppression Firewall (`domain/consent`)
- Recording an opt-out on a customer MUST execute in a single Room `@Transaction`:
  1. Set `Customer.isOptedOut = true`, `Customer.consentStatus = "OPTED_OUT"`, `Customer.optedOutAtMillis = now`.
  2. Insert an immutable `ConsentRecord` audit entry.
  3. Upsert an active `SuppressionEntry` keyed by `normalizedPhoneE164`.
  4. Automatically cancel any pending (`DRAFT`, `VALIDATED`, `AWAITING_CONFIRMATION`, `QUEUED`) `CampaignRecipient` rows for that customer across active draft campaigns.
- **Double Suppression Gate**: Check both `Customer.isOptedOut == false` AND `SuppressionEntryDao.isPhoneSuppressed(normalizedPhoneE164) == false` during:
  1. Recipient selection in the Campaign Editor.
  2. Pre-send campaign validation (`ValidateCampaignUseCase`).
  3. Immediately prior to dispatching each individual message in the transport coordinator.

### 4.3 Message Templates & Safe Personalization (`domain/campaign`)
- Seed 10 editable templates covering all required business categories:
  1. Order confirmation
  2. Appointment reminder
  3. Payment reminder
  4. Service completion
  5. Delivery update
  6. Customer thank-you message
  7. Business announcement
  8. Opt-in customer promotion
  9. Festival greeting
  10. Custom message
- Support safe placeholders: `{{customer_name}}`, `{{business_name}}`, `{{owner_name}}`, `{{business_phone}}`, `{{signature}}`.
- Validate resolved messages before sending: if a template uses `{{customer_name}}` and a customer's name is blank, substitute a safe fallback (e.g., `"Valued Customer"`) or warn the user during Campaign Review. Never send raw unresolved `{{...}}` tokens.
- Implement an exact **SMS Segment & Encoding Calculator**:
  - Detect whether the resolved text fits within the GSM 7-bit default alphabet (160 chars for 1 segment; 153 chars per segment for multi-part concatenated SMS; count GSM-7 extension characters `^ { } \ [ ~ ] | €` as 2 septets).
  - If any non-GSM character (e.g., emoji, Devanagari/regional scripts, curly quotes) is present, switch calculation to `UCS_2` (70 chars for 1 segment; 67 chars per segment for multi-part SMS).

### 4.4 Dual-Transport Architecture (`domain/transport`)
Design a clean `SmsTransport` interface so UI and Campaign orchestration are decoupled from the underlying mechanism:

1. **Transport 1: Android SMS Composer (`ComposerSmsTransport` — Default & Google Play Baseline)**
   - Uses `Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:${Uri.encode(phone)}"); putExtra("sms_body", body) }`.
   - Never requires `SEND_SMS` permission.
   - Provides an interactive **Sequential Handoff Coordinator Screen**:
     - Launches the system SMS composer for the current recipient.
     - Transitions the recipient state to `HANDED_OFF_TO_MESSAGING_APP`.
     - Clearly explains to the user that because Android isolates third-party messaging apps, the user must confirm in the messaging app and then mark the step as "Completed in Messaging App", "Skipped", or "Failed" upon returning, or advance to the next recipient.
     - Supports pausing, resuming across app restarts, and cancelling the remaining queue.

2. **Transport 2: Direct SIM Transport (`DirectSimSmsTransport` — Policy & Permission Gated)**
   - **Strict Eligibility Gate**: Disabled by default. Accessible only when the user enables the Direct SIM Policy Gate in Settings after reading an explicit disclosure that Google Play restricts `SEND_SMS` to default SMS handlers or qualifying enterprise/sideloaded distributions, and that personal SIM plans have carrier/TRAI quotas.
   - Checks runtime permission (`Manifest.permission.SEND_SMS` and `Manifest.permission.READ_PHONE_STATE` for dual-SIM discovery) before every action.
   - **Dual-SIM Subscription Support**: Queries `SubscriptionManager.activeSubscriptionInfoList` when permitted, displaying carrier name, slot index, and display number so the user can explicitly pin a SIM subscription ID. Never silently switches SIMs if the chosen subscription ID becomes unavailable—fail safely with a clear error code (`INVALID_SUBSCRIPTION_ID`).
   - Uses `SmsManager` (`createForSubscriptionId` on supported APIs) and `divideMessage` / `sendMultipartTextMessage` (or `sendTextMessage` for single segment) with unique `PendingIntent` broadcast receivers for `SMS_SENT` and `SMS_DELIVERED`.
   - **State Integrity**:
     - Calling `SmsManager.send*` transitions the recipient to `SUBMITTED`.
     - Only the `SMS_SENT` broadcast with `Activity.RESULT_OK` transitions the recipient to `SENT`.
     - Only the `SMS_DELIVERED` broadcast confirms `DELIVERED`. Never mark a message `DELIVERED` merely because `sendTextMessage` returned or `SMS_SENT` fired.
     - Handle `RESULT_ERROR_GENERIC_FAILURE`, `RESULT_ERROR_NO_SERVICE`, `RESULT_ERROR_NULL_PDU`, `RESULT_ERROR_RADIO_OFF`, and missing/unready SIMs explicitly.
     - **No Blind Retries**: If a submission outcome is `UNKNOWN` (e.g., process death before `SMS_SENT` callback), mark it `UNKNOWN` via the reconciliation logic and require deliberate user decision rather than auto-retrying and risking duplicate messages.

### 4.5 India-Specific & TRAI TCCCPR Compliance (`feature/settings` & `feature/campaigns`)
- Include dedicated validation warnings and an educational compliance guide covering India's TRAI Telecom Commercial Communications Customer Preference Regulations (TCCCPR):
  - Explain that personal P2P SIM plans in India are typically capped by telecom operators at **100 SMS per SIM per day** and are intended for personal or low-volume one-to-one customer interaction, not unlicensed commercial bulk broadcasting.
  - Warn users when a campaign uses a promotional template (`isPromotional == true`) outside TRAI's permitted promotional window (**10:00 AM to 9:00 PM IST**) or includes customers without explicit `GRANTED` opt-in consent.
  - Explicitly state that this app does not bypass DLT (Distributed Ledger Technology) header/template registration, scrubbing, or carrier anti-spam filters, and advise businesses requiring high-volume automated commercial blasts to use a TRAI-registered DLT Principal Entity & Telemarketer gateway.

### 4.6 Privacy & Security Hardening
- **No Network Access**: Do not request `android.permission.INTERNET` in `AndroidManifest.xml`.
- **Backup Exclusion**: Configure `android:allowBackup="false"` or strict `backup_rules.xml` and `data_extraction_rules.xml` that exclude the Room SQLite database and DataStore files from cloud backups and unencrypted device transfers.
- **PII-Safe Logging**: Implement a `SafeLogger` utility that strips digits and never logs phone numbers, customer names, or message bodies to `Logcat`.
- **CSV Export Warning**: Show an explicit confirmation dialog before exporting customer lists or campaign reports, warning the owner that exported CSV files are unencrypted and contain personal customer information.
- **Full Data Deletion**: Provide a "Delete All Local Business Data" control in Settings protected by a destructive-action confirmation dialog that clears all Room tables and DataStore preferences.

---

## 5. USER EXPERIENCE & 13 REQUIRED SCREENS

Implement a cohesive Jetpack Compose navigation graph (`NavHost`) with Material 3 components, clear loading/empty/error states, accessible touch targets, and confirmation dialogs for all destructive or sending actions across all 13 screens:

1. **Onboarding & Permissions Explanation (`OnboardingScreen`)**: Explains local-only storage, Transport 1 (Composer) vs. Transport 2 (Direct SIM policy gate), minimal permissions, and TRAI/carrier responsibility.
2. **Business Profile (`BusinessProfileScreen`)**: Configure business name, owner display name, category, communication phone number, optional address, default country calling code (e.g., `+91`), and default signature.
3. **Dashboard (`DashboardScreen`)**: Displays business summary, total eligible customers, opted-out suppression count, draft campaigns, recent campaign status breakdown (without fake delivery claims), and quick actions.
4. **Customer Directory (`CustomerDirectoryScreen`)**: Search by name/phone/notes, filter by group, tag, or consent status (`GRANTED`, `UNKNOWN`, `OPTED_OUT`), sort, add/edit/delete customer, and one-tap Opt-Out toggle with confirmation.
5. **Customer Import (`CustomerImportScreen`)**:
   - System Contact Picker (`ActivityResultContracts.PickContact` / `ContactsContract.CommonDataKinds.Phone`) requiring zero broad `READ_CONTACTS` permission when using the system picker intent, or minimal scoped permission if browsing.
   - SAF CSV Import (`OpenDocument`) with column mapping/parsing (`name,phone,tags,consent_status,consent_source,notes`), live validation preview showing valid, duplicate, and invalid rows before committing, plus SAF CSV Export (`CreateDocument`).
6. **Customer Groups & Tags (`CustomerGroupsScreen`)**: Create, rename, and delete customer groups; assign/remove customers; inspect group member counts.
7. **Message Template Library (`TemplateLibraryScreen`)**: Browse, filter by category, create, edit, duplicate, and delete templates; live token preview with sample customer data.
8. **Campaign Editor (`CampaignEditorScreen`)**: Set campaign title, pick a template or write custom body, insert placeholder chips (`{{customer_name}}`, etc.), view live character count, encoding (`GSM_7BIT` vs `UCS_2`), and segment count.
9. **Recipient Selection (`RecipientSelectionScreen`)**: Select by group, tag, individual customers, or "All Eligible Customers". Automatically disables/excludes `isOptedOut` / suppressed and invalid-phone customers with a clear explanation badge. Shows exact selected recipient count.
10. **Campaign Review & Confirmation (`CampaignReviewScreen`)**: Shows exact recipient count, suppressed/excluded count, representative personalized previews for 3+ actual recipients, character/segment estimates, chosen transport mode (`Composer Handoff` vs `Direct SIM`), TRAI/carrier advisory, and a mandatory deliberate confirmation checkbox + "Start Sending Session" button.
11. **Sending or Messaging-App Handoff Status (`CampaignSendingScreen`)**:
    - In Composer Mode: Interactive step-by-step card showing current recipient (`X of N`), personalized message preview, "Open in Messaging App" button, and "Mark Sent / Skip / Cancel Campaign" controls.
    - In Direct SIM Mode: Real-time progress tracker showing `QUEUED`, `SUBMITTED`, `SENT`, `DELIVERED`, `FAILED`, and `UNKNOWN` counts, active SIM badge, pause/cancel controls, and safe recovery options for interrupted sessions.
12. **Campaign History & Detail (`CampaignHistoryScreen` & `CampaignDetailScreen`)**: Lists all past and active campaigns; drill down into recipient-level outcomes and `SendAttempt` logs; duplicate campaign; reconcile interrupted sessions; export recipient-level CSV campaign report via SAF.
13. **Settings & Privacy Controls (`SettingsScreen`)**: Transport selection & Direct SIM eligibility gate, dual-SIM subscription selector, India TRAI compliance mode toggle, theme selector (System/Light/Dark), In-App Privacy Policy viewer, CSV export, and full local data wipe.

---

## 6. AUTONOMOUS 8-PHASE EXECUTION PLAN

Execute these phases sequentially in the workspace without pausing:

### Phase 1: Environment & Repository Discovery
- Run shell commands to inspect the current directory structure, Gradle wrapper (`./gradlew --version`), installed JDK (`java -version`), Android SDK path (`ANDROID_HOME` / `local.properties`), installed build-tools/platforms, and connected devices (`adb devices -l`).
- Inspect any existing source files, manifests, or Gradle scripts.

### Phase 2: Architecture Audit & Blueprint
- Document existing files (if any) and identify missing, broken, or incompatible configurations.
- Configure `settings.gradle.kts`, root `build.gradle.kts`, `gradle/libs.versions.toml` (or `app/build.gradle.kts`), and `gradle.properties` with mutually compatible, stable versions of AGP, Kotlin, KSP, Hilt, Room, Compose BOM, Navigation Compose, DataStore, WorkManager, Coroutines, and JUnit/AndroidX Test.

### Phase 3: Full Dependency-Ordered Implementation
1. **Core Layer**: Implement `core/common`, `core/model`, and `core/database` (all 11 entities, DAOs with `@Transaction`, Room database, migrations, and template seeding). Compile to verify KSP/Room code generation.
2. **Domain Layer**: Implement `domain/validation` (E.164 normalizer, duplicate checker, CSV parser, SMS segment calculator), `domain/consent` (opt-out firewall), `domain/campaign` (template engine, state machine), and `domain/transport` (`ComposerSmsTransport` and `DirectSimSmsTransport`).
3. **Data Layer**: Implement `data/customer` and `data/campaign` repositories, SAF CSV import/export handlers, and `WorkManager` reconciliation worker.
4. **Feature & UI Layer**: Implement all 13 screens, ViewModels, Hilt modules, `MainActivity`, navigation graph, Material 3 theme, string resources, `backup_rules.xml`, `data_extraction_rules.xml`, and `AndroidManifest.xml`.

### Phase 4: Build, Unit Testing & Static Verification
- Run `./gradlew assembleDebug` and resolve all compilation, resource, manifest, or Hilt DI errors.
- Write comprehensive JUnit tests covering:
  - Phone number normalization (Indian `+91` numbers, international formats, trunk prefixes, invalid lengths, non-numeric noise).
  - Duplicate customer detection and CSV import row validation.
  - SMS segment & encoding calculation (GSM-7 single, GSM-7 multipart, GSM-7 extended characters, UCS-2 Unicode/emoji single & multipart).
  - Template personalization and fallback safety.
  - Opt-out suppression firewall (verifying opted-out numbers are blocked from campaign validation and recipient lists).
  - Campaign & recipient state machine transitions (verifying `SUBMITTED` is never auto-marked `DELIVERED` and `UNKNOWN` states are not blindly retried).
- Execute `./gradlew testDebugUnitTest` and fix any failing tests until 100% pass.

### Phase 5 & Phase 6: Functional, Persistence & Device/Instrumentation Testing
- Write instrumentation / Compose UI tests under `app/src/androidTest` testing Room database persistence, DAO unique constraints (`(campaignId, customerId)`), migration integrity, and UI screen flows.
- Check `adb devices`. If an emulator or physical device is connected:
  - Run `./gradlew connectedDebugAndroidTest`.
  - Install and launch the debug APK (`adb install -r app/build/outputs/apk/debug/app-debug.apk`), test CSV import, campaign creation, opt-out suppression, process restart persistence, permission denial handling, and Composer intent handoff, and inspect `adb logcat` for crashes or PII leaks.
- If no device/emulator is connected in the execution environment, compile the androidTest APK via `./gradlew assembleDebugAndroidTest` to verify zero test compilation errors, and explicitly document the hardware unavailability in the final report.

### Phase 7: Privacy, Accessibility, Policy & Lint Audit
- Verify `AndroidManifest.xml` has no `INTERNET` permission, properly declares `<queries>` for `smsto:` intents, scopes any optional permissions transparently, and disables unencrypted cloud backup of customer data.
- Run `./gradlew lintDebug` and resolve any critical security, privacy, or accessibility issues.

### Phase 8: Release Packaging & Final Verification
- Configure release build settings (R8/ProGuard rules for Room, Hilt, and Serialization if needed, plus signing config fallback or unsigned release artifact generation) and run `./gradlew assembleDebug assembleRelease`.
- Verify the generated APK artifacts on disk (`ls -lh app/build/outputs/apk/**`).

---

## 7. REQUIRED FINAL DELIVERABLES REPORT

Upon completing Phases 1–8, output a comprehensive **Final Engineering Report** containing these 10 sections:

1. **Complete Project Source Inventory**: Full directory tree and package mapping of all implemented files.
2. **Reproducible Build Instructions**: Exact JDK, Android SDK, and Gradle commands to build, test, and package the app from a clean checkout.
3. **Built Artifacts Summary**: File paths, sizes, and SHA-256 checksums of the generated Debug APK, AndroidTest APK, and Release APK/AAB artifacts.
4. **Database Schema & Migration Strategy**: Summary of all 11 Room entities, indices, foreign keys, unique constraints, and migration paths.
5. **Implemented Feature Inventory**: Verification matrix mapping every requirement (Sections 3A–3I) to its concrete classes and composables.
6. **Test Results & Device Details**: Exact output summaries from `./gradlew testDebugUnitTest`, `./gradlew assembleDebugAndroidTest`, `./gradlew lintDebug`, and any executed `adb` device tests.
7. **Permission & Google Play Compliance Assessment**: Detailed audit of declared permissions, why Transport 1 (`ACTION_SENDTO`) is Google Play compliant by default, and the policy restrictions governing Transport 2 (`SEND_SMS`).
8. **India TRAI / DLT & Telecom Compliance Assessment**: How the app enforces opt-out suppression, personal SIM rate warnings, promotional window advisories, and non-bypass transparency.
9. **Step-by-Step Installation & Usage Guide**: Clear guide for a non-technical business owner to install the APK, configure their profile, import customers via CSV/Contacts, run a campaign, and manage opt-outs.
10. **Honest Verification Matrix (Verified vs. Untested Functionality)**: Transparent separation of what was verified via automated JVM tests, Gradle builds, and connected hardware vs. what requires physical dual-SIM carrier hardware verification.

Begin Phase 1 immediately by inspecting the workspace and toolchain.