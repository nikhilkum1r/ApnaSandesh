# Build a Production-Ready Android Business SMS App — Complete One-Shot Engineering Mission

## 1. Product Vision

Build a production-quality native Android application that empowers small business owners, shopkeepers, independent service providers, local retailers, and freelancers to communicate with their existing customers through their own Android phone number and SIM card.

The core value proposition is:

**Use your own phone. Use your own SIM. Manage your customers locally. Send business SMS affordably. No mandatory cloud account or SMS gateway subscription.**

The application must be practical for nontechnical users, including small businesses with limited budgets.

Do not stop at a prototype, dashboard mockup, or partially working application. Inspect the existing repository, implement the complete feasible product, test it, resolve defects, and provide evidence of the final result.

## 2. Target Platform and Technology

- Platform: Native Android
- Language: Kotlin
- UI: Jetpack Compose and Material 3
- Architecture: MVVM, repository pattern, unidirectional data flow
- Dependency injection: Hilt
- Local database: Room / SQLite
- Preferences: DataStore
- Asynchronous work: Kotlin Coroutines and Flow
- File import/export: Android Storage Access Framework
- Testing: JUnit, Compose UI tests, instrumentation tests
- Distribution: Google Play
- Backend: None required for the initial release
- Cloud synchronization: Not required
- Primary language: English, with localization-ready resources
- Offline-first operation: Required for locally available features

Use stable, mutually compatible dependencies. Verify the current Android SDK and Google Play target API requirements before release.

## 3. Core Product Features

### A. Business Profile

Allow users to configure:
- Business name
- Owner's display name
- Business category
- Phone number used for communication
- Business address, if desired
- Default country calling code
- Default message signature

Do not assume the SIM's phone number can always be read automatically. Let the owner enter and verify the preferred contact number where necessary.

### B. Customer Management

Implement a complete local customer directory:
- Add, edit, and delete customers
- Import contacts using Android's system contact picker
- Import CSV files
- Export customer lists where appropriate
- Normalize phone numbers into international format
- Detect duplicates and invalid numbers
- Add customer tags and groups
- Search, filter, and sort customers
- Record consent status and consent source
- Record opt-outs and permanently suppress opted-out recipients from future campaigns
- Support customer notes without unnecessary sensitive information

Request only the Android permissions necessary for selected features.

### C. Business Message Templates

Provide editable templates for legitimate business communication:
- Order confirmation
- Appointment reminder
- Payment reminder
- Service completion
- Delivery update
- Customer thank-you message
- Business announcement
- Opt-in customer promotion
- Festival greeting
- Custom message

Allow optional personalization using safe, validated fields such as customer name and business name.

Preview every personalized message before sending. Never silently send a template to an unintended recipient.

### D. Manual Campaign Manager

Implement:
- Create, edit, duplicate, and delete campaign drafts
- Select individual customers, groups, or eligible customers
- Show the exact recipient count
- Preview the message for representative recipients
- Display SMS character count and estimated SMS segments
- Display a final review screen
- Require deliberate confirmation before each sending session
- Show campaign history and recipient-level outcomes
- Support cancellation and recovery where technically possible
- Export campaign reports

The user must be able to review the campaign before initiating message composition or sending.

### E. Personal SIM-Based Sending

The preferred product experience is sending through the owner's own SIM, using their existing mobile plan where the carrier permits it.

Implement a transport abstraction rather than coupling the UI to a single sending mechanism.

**Transport 1 — Android SMS composer**

- Provide a Google Play-compatible baseline.
- Open the system messaging interface with recipient and message content where supported.
- Require the user to review and confirm the actual send.
- Explain that large campaigns may require multiple manual actions and that the application cannot reliably observe every action performed in another messaging app.

**Transport 2 — Direct SIM transport**

- Implement only behind a policy and permission eligibility gate.
- Use Android's supported SMS APIs and the appropriate subscription configuration.
- Request `SEND_SMS` only when the exact application use and distribution route qualify under applicable policy.
- Support explicit subscription selection for dual-SIM devices where supported.
- Handle permission denial, missing SIMs, invalid subscription IDs, carrier failures, insufficient balance, and unsupported devices.
- Do not silently switch SIMs or automatically retry an ambiguous submission.
- Do not bypass carrier quotas, Android restrictions, anti-spam controls, or Google Play requirements.

A user-initiated campaign does not automatically qualify for restricted SMS permissions. Do not claim that publishing the application on Google Play guarantees direct SIM sending.

### F. Reliable Sending and Campaign History

Track separate states:
- Draft
- Validated
- Awaiting confirmation
- Handed off to messaging app
- Queued, when applicable
- Submitted
- Sent
- Delivered, when confirmed by an available delivery report
- Failed
- Unknown
- Cancelled

Use Room transactions and explicit state transitions to preserve campaign integrity.

Never mark a message as delivered solely because the SMS API accepted the request.

Do not blindly retry messages with uncertain submission outcomes. Prevent duplicate campaign-recipient records and provide clear recovery options.

Use WorkManager only for eligible deferrable tasks, such as local maintenance and reconciliation. Do not assume it guarantees precise execution or overrides background restrictions.

### G. User Experience

Design a clean, professional interface suitable for nontechnical business owners.

Screens:
1. Onboarding and permissions explanation
2. Business profile
3. Dashboard
4. Customer directory
5. Customer import
6. Customer groups and tags
7. Message template library
8. Campaign editor
9. Recipient selection
10. Campaign review and confirmation
11. Sending or messaging-app handoff status
12. Campaign history
13. Settings and privacy controls

Use accessible typography, consistent spacing, loading and empty states, useful validation messages, and light/dark themes.

Avoid cluttered dashboards, misleading delivery statistics, unnecessary animations, and destructive actions without confirmation.

### H. Privacy and Security

The initial version must operate locally without a mandatory account.

Requirements:
- Store data in app-private storage.
- Do not upload contacts or messages to a server.
- Do not add advertising SDKs or analytics that transmit customer information without a justified and disclosed need.
- Exclude sensitive customer and campaign data from inappropriate cloud backups.
- Provide deletion and export controls.
- Validate all imported data.
- Avoid logging phone numbers, message bodies, or other personal data.
- Explain that exported CSV files may contain personal information.
- Request minimum permissions and disclose data practices accurately.
- Provide a privacy policy consistent with the actual implementation.

### I. India-Specific Considerations

The application may be used by businesses in India, but do not assume personal SIM plans authorize every business messaging use.

Investigate applicable TRAI requirements, telecom provider terms, commercial messaging classifications, consent requirements, and promotional-message restrictions before enabling commercial bulk messaging.

Distinguish personal communication from commercial messaging. Do not advertise the application as a way to bypass DLT registration, sender registration, template rules, carrier restrictions, or anti-spam controls.

If a business use case requires an approved commercial messaging channel, explain the limitation and offer an appropriate compliant alternative rather than implementing a bypass.

## 4. Local Data Model

Use Room entities for:

- BusinessProfile
- Customer
- CustomerGroup
- GroupMembership
- MessageTemplate
- Campaign
- CampaignRecipient
- SendAttempt
- ConsentRecord
- SuppressionEntry
- AppSettings

Use UUID primary keys, foreign keys, suitable indexes, database migrations, and transactional updates.

Enforce a unique campaign/customer relationship to prevent duplicate recipient records within the same campaign.

Preserve consent history appropriately, but ensure that opted-out customers cannot be selected for future campaigns accidentally.

## 5. Architecture and Maintainability

Organize code into clear modules or packages:

- core/model
- core/database
- core/common
- data/customer
- data/campaign
- domain/validation
- domain/consent
- domain/campaign
- domain/transport
- feature/dashboard
- feature/customers
- feature/templates
- feature/campaigns
- feature/history
- feature/settings

Keep Android platform APIs behind interfaces. Use dependency injection and testable domain logic.

Do not introduce a backend, distributed services, or unnecessary architectural complexity for the initial offline-first release.

## 6. Engineering Execution

Work autonomously within the available project and device environment.

Phase 1: Inspect the repository, Gradle configuration, Android SDK, available tools, connected devices, and existing implementation.

Phase 2: Document the existing architecture and identify broken, incomplete, duplicated, or unnecessary code.

Phase 3: Produce an implementation plan, then implement the complete feature set in dependency order.

Phase 4: Build the app, fix compilation errors, run unit tests, run available instrumentation tests, and inspect runtime logs.

Phase 5: Test CSV import, duplicate detection, campaign creation, opt-out suppression, app restarts, permission denial, and dual-SIM behavior where hardware is available.

Phase 6: Test the messaging-composer integration on a real device. Test direct SIM behavior only if the permission model permits it.

Phase 7: Review privacy, Android permissions, accessibility, error handling, performance, and Google Play policy eligibility.

Phase 8: Produce a release build only after the required checks pass.

Use available local tools and connected devices where appropriate. Never claim a test passed unless it was actually executed and its result was inspected. Do not claim carrier delivery or real-device compatibility without evidence.

## 7. Definition of Done

The application is complete when:
- The project builds successfully.
- All implemented features are connected to real data.
- Customer data persists across application restarts.
- CSV import and export work correctly.
- Campaign previews match the intended recipient and message.
- Opted-out recipients are suppressed.
- Sending actions are explicit and transparent.
- Direct SMS permissions are not requested without verified eligibility.
- Errors and uncertain outcomes are handled safely.
- Core business logic has automated test coverage.
- The available real-device tests have been completed and documented.
- Privacy disclosures match actual data handling.
- Google Play release blockers are documented rather than concealed.

## 8. Final Deliverables

Provide:
1. Complete Android Studio project source
2. Reproducible build instructions
3. Debug APK and release artifact, if successfully built
4. Database schema and migration strategy
5. Implemented feature inventory
6. Test results and device details
7. Permission and Google Play compliance assessment
8. Known limitations and unresolved blockers
9. Step-by-step installation and usage guide
10. Final engineering report distinguishing verified functionality from untested functionality

Do not merely generate code snippets or a plan. Execute the work in the existing repository, validate the implementation, and report honestly on what is finished.

The priority is a useful, trustworthy, low-cost product for small business owners—not maximizing message volume by bypassing Android or telecom restrictions.