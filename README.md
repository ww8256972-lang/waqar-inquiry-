# WAQAR WEBSITE INQUIRY

A production-grade, genuine Native Android application built with **Kotlin** and **Jetpack Compose** for inquiry management, admission form processing, payment receipts, order workflows, and automatic WhatsApp messaging.

---

## 🌟 Key Features

1. **Inquiry & Record Management**
   - Full lifecycle tracking: *New, Pending, Under Review, Verification Pending, Verified, Admission Pending, Order Processing, Completed*.
   - Rupee (₹) cost, paid amount, and real-time remaining balance calculations.
   - Internal notes, tags, and follow-up scheduling.

2. **Admission Form & Document Verification**
   - Candidate applicant profiles, registration numbers, and enrolled courses.
   - Document checklist (*ID proofs, certificates, photographs*).
   - Instant verification workflow with automatic candidate notifications.

3. **Orders Workflow**
   - Client service orders with milestone tracking (*Pending, Processing, Completed*).
   - Financial ledger tracking with partial and full settlement status.

4. **Payment Ledger & Native PDF Receipt Generator**
   - Offline-capable PDF document rendering via `android.graphics.pdf.PdfDocument`.
   - Generates official tax invoices and payment receipts with Rupee (₹) symbols, itemized breakdowns, and authorized signature blocks.
   - Instant 1-tap sharing via WhatsApp or system print.

5. **WhatsApp Integration & Automatic Engine**
   - Uses official Android WhatsApp deep-link intents (`https://api.whatsapp.com/send?phone=...`).
   - Dynamic variable placeholders: `{customer_name}`, `{phone}`, `{amount}`, `{remaining_amount}`, `{record_id}`, `{order_id}`, `{admission_id}`, `{status}`.
   - Configurable daily dispatch rate limit to prevent spam flagging.
   - Default inquiry notification:
     > *"Congratulations! Thank you for contacting us. On behalf of Waqar, your inquiry and admission form will be sent automatically; we won't need to send it manually."*

6. **Customer 360° Profile**
   - Complete unified timeline of linked inquiries, admissions, orders, and payment history per client.

7. **Ask AI Assistant**
   - Context-aware operational guidance for every screen.
   - Complete offline FAQ knowledge base with step-by-step procedures.

8. **Recycle Bin & Zero Data Loss Policy**
   - Safe soft-deletion with operator tagging and timestamping.
   - 1-tap restore and authorized permanent purging.

9. **Immutable Security Audit Log**
   - Tamper-resistant log of logins, record modifications, payments, backups, and dispatches.

10. **Offline-First & Cloud Sync**
    - Seamless local SQLite Room persistence.
    - Sync queue with automatic conflict handling when network is restored.

11. **Scalable Large Database Support (15,000+ Records)**
    - Room indexed queries and Paging 3 architecture.
    - Built-in synthetic 15,000 record benchmark generator in Settings.

---

## 🔐 Administrative Access

- **Initial Admin Username:** `Waqar`
- **Initial Password:** `Waqar`
- **First Login Security:** Forced password rotation is required on first login. Passwords are encrypted with SHA-256 and salted cryptographic hashing. Plaintext passwords are never stored.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.2.10
- **UI Framework:** Jetpack Compose + Material Design 3
- **Local Database:** Room 2.7.0 (with KSP)
- **Architecture:** Clean Architecture / MVVM with Kotlin Coroutines & Flow
- **PDF Engine:** Native Android `PdfDocument` & `FileProvider`
- **Build System:** Gradle 9.3.1 with Android Gradle Plugin (AGP) 9.1.1
- **Target SDK:** 36 (Android 16), Min SDK: 24 (Android 7.0)

---

## 🚀 Building & Packaging

### Run Unit & Robolectric Tests:
```bash
./gradlew testDebugUnitTest
```

### Build Debug APK:
```bash
./gradlew assembleDebug
```
*Output:* `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK & AAB:
```bash
./gradlew assembleRelease
./gradlew bundleRelease
```
*Outputs:*
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

---

## 🤖 GitHub Actions CI/CD Workflows

- `.github/workflows/android-build.yml`: Verifies wrapper, runs unit tests, executes Android lint, and builds debug APK artifact on push/pull-request.
- `.github/workflows/android-release.yml`: Automates release signing, assembling release APK, and packaging Google Play App Bundle (AAB).

### Required GitHub Secrets for Release:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The password and alias secrets may also use the legacy names `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`; the workflow falls back to those names when the corresponding `ANDROID_*` secret is unset.
