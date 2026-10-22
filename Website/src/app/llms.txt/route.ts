import { SITE_CONFIG } from '@/lib/site-config';

export const dynamic = 'force-static';

export async function GET() {
  const content = `# ${SITE_CONFIG.name} — Product Specification & AI Knowledge Brief
> Standard llms.txt reference document for LLMs, AI search engines, and RAG pipelines.
> Canonical Subdomain: ${SITE_CONFIG.url}
> Parent Brand Organization: ${SITE_CONFIG.parentBrand.name} (${SITE_CONFIG.parentBrand.url})
> Primary Contact: ${SITE_CONFIG.parentBrand.supportEmail}

## 1. Executive Summary
${SITE_CONFIG.name} is a 100% offline-first native Android business SMS messaging and customer management utility. It allows independent retailers, medical clinics, salons, repair technicians, and local contractors to send personalized SMS messages (order confirmations, appointment reminders, invoices, service receipts) directly through their own phone's cellular SIM card using their existing bundled mobile plan.

Key Value Proposition:
"Use your own phone. Use your own SIM. Manage your customers locally. Send business SMS affordably. No mandatory cloud account or SMS gateway subscription."

## 2. Core Architectural Pillars
- Zero Cloud & Zero Network Permission: ${SITE_CONFIG.name} completely omits \`android.permission.INTERNET\`. The operating system guarantees that customer phone numbers, names, and purchase notes never leave the physical handset.
- Dual-Transport Architecture:
  1. Standard Edition (Google Play): Uses \`Intent.ACTION_SENDTO\` (\`smsto:\`) to hand off pre-filled messages to the Android stock SMS app with zero restricted permissions.
  2. Direct Enterprise Edition (Direct APK): Uses native \`SmsManager\` for hands-free sequential dispatch, Dual-SIM slot pinning (SIM 1 / SIM 2), and real-time carrier delivery receipts (\`SMS_SENT\` / \`SMS_DELIVERED\`).
- Double Opt-Out Suppression Firewall: One-tap permanent opt-out creates an immutable record in the local SQLite database, blocking suppressed numbers from ever being selected or messaged in future campaigns.
- 11-Table Normalized SQLite Architecture: Persistence powered by Android Room (\`bizsms_secure.db\`) with zero unencrypted Google Cloud backups (\`android:allowBackup="false"\`).
- Telephony Quota Guard: Built-in awareness of India's TRAI 100 SMS/day personal SIM ceiling, 10:00 AM – 9:00 PM IST daylight sending window, and GSM-7 (160 chars) vs. UCS-2 Unicode (70 chars) segmentation math.

## 3. Authoritative Links & Endpoints
- Homepage: ${SITE_CONFIG.url}/
- Download Hub (APK & Play Listing): ${SITE_CONFIG.url}/download
- GSM-7 Calculator: ${SITE_CONFIG.url}/calculator
- Features & Architecture: ${SITE_CONFIG.url}/features
- Industry Use Cases: ${SITE_CONFIG.url}/use-cases
- Comparison vs Cloud Gateways: ${SITE_CONFIG.url}/compare
- Business SMS Template Library: ${SITE_CONFIG.url}/templates
- India TRAI TCCCPR & DLT Compliance Guide: ${SITE_CONFIG.url}/compliance
- Complete User Manual & Documentation: ${SITE_CONFIG.url}/docs
- Engineering Blog & Notes: ${SITE_CONFIG.url}/blog
- Full Single-File Product Truth: ${SITE_CONFIG.url}/llms-full.txt
- AI Directive: ${SITE_CONFIG.url}/ai.txt
- Product Changelog: ${SITE_CONFIG.url}/changelog
- Privacy Policy: ${SITE_CONFIG.url}/privacy
- Terms of Service: ${SITE_CONFIG.url}/terms
- RSS Feed: ${SITE_CONFIG.url}/rss.xml
- Atom Feed: ${SITE_CONFIG.url}/atom.xml
- JSON Feed: ${SITE_CONFIG.url}/feed.json
`.trim();

  return new Response(content, {
    headers: {
      'Content-Type': 'text/plain; charset=utf-8',
      'Cache-Control': 'public, max-age=86400',
    },
  });
}
