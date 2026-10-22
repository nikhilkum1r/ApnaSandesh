import React from 'react';
import Link from 'next/link';

interface FeatureItem {
  number: string;
  tag: string;
  title: string;
  description: string;
  subtext: string;
  image: string;
  badgeText: string;
  metrics: { label: string; value: string }[];
}

const FEATURES: FeatureItem[] = [
  {
    number: '01',
    tag: 'HARDWARE TELEPHONY',
    title: 'Autonomous SIM dispatch with adaptive safety intervals',
    description:
      'Rather than routing your business messages through expensive third-party cloud aggregators with 300% markup, ApnaSandesh leverages Android’s native TelephonyManager and SmsManager directly. Sequential dispatches are spaced by an enforced 3.5s to 6.0s jittered throttle to honor carrier fair-use policies and protect your SIM reputation.',
    subtext:
      'Operates seamlessly with single or dual SIM configurations across Jio, Airtel, Vodafone Idea, and BSNL without requiring DLT portal registration for individual subscriber quotas.',
    image: '/images/features/state-machine-flow.webp',
    badgeText: 'Carrier Compliant',
    metrics: [
      { label: 'Carrier Markup', value: '₹0.00 / SMS' },
      { label: 'Dispatch Throttle', value: '3.5s – 6.0s' },
      { label: 'Dual-SIM Mode', value: 'Auto-Select' },
    ],
  },
  {
    number: '02',
    tag: 'PRIVACY & DATA CUSTODY',
    title: '100% on-device Room SQLite database. Zero cloud leakage.',
    description:
      'Cloud SMS portals store your customer names, mobile numbers, purchase history, and message content on remote multi-tenant databases prone to scraping, breaches, and competitive surveillance. ApnaSandesh writes every contact, campaign log, and opt-out suppression entry to an encrypted Room SQLite database inside your phone’s isolated application sandbox.',
    subtext:
      'Even if you completely disable Wi-Fi and Cellular Mobile Data, ApnaSandesh executes your queued campaigns flawlessly using only cellular baseband radio.',
    image: '/images/features/offline-privacy-architecture.webp',
    badgeText: 'Zero Cloud Storage',
    metrics: [
      { label: 'Remote Server Calls', value: '0 per day' },
      { label: 'Database Engine', value: 'Room 2.6 SQLite' },
      { label: 'Data Encryption', value: 'Device Sandbox' },
    ],
  },
  {
    number: '03',
    tag: 'TEMPLATING & ENCODING',
    title: 'Real-time dynamic template tokens with live GSM-7 safety',
    description:
      'Personalize bulk messages with custom placeholder tags ({name}, {date}, {balance}, {order_id}) imported directly from your contact spreadsheets. ApnaSandesh’s live tokenizer recalculates character counts on keystroke, warning you before unicode characters convert a standard 160-char SMS into a 70-char UCS-2 multi-part segment.',
    subtext:
      'Comes pre-loaded with 30+ production-tested Hindi, Hinglish, and English templates designed for billing reminders, festive offers, and service updates.',
    image: '/images/features/template-manager.webp',
    badgeText: 'Unicode Protected',
    metrics: [
      { label: 'Pre-loaded Templates', value: '30+ Industry Sets' },
      { label: 'Token Interpolation', value: 'Sub-millisecond' },
      { label: 'Encoding Guard', value: 'GSM-7 Auto-lock' },
    ],
  },
  {
    number: '04',
    tag: 'DATA INGESTION',
    title: 'Sanitized CSV contact importer with automated deduplication',
    description:
      'Bring your existing customer databases without tedious reformatting. ApnaSandesh accepts standard comma-delimited or semicolon-delimited CSV spreadsheets exported from Excel, Google Sheets, Tally, Vyapar, or Khatabook. The ingestion pipeline strips malicious formula injections, validates 10-digit Indian mobile formats, and suppresses unsubscribed numbers.',
    subtext:
      'Import 500 contacts in under 1.2 seconds directly from your phone’s storage or SD card.',
    image: '/images/features/csv-import-security.webp',
    badgeText: 'Injection Safe',
    metrics: [
      { label: 'Import Speed', value: '500 rows / 1.2s' },
      { label: 'Format Validation', value: 'E.164 & 10-Digit' },
      { label: 'Opt-Out Filter', value: 'Auto-suppressed' },
    ],
  },
];

export function FeatureEditorialList() {
  return (
    <section className="py-24 bg-[#FAF9F6] border-b border-[#E6E4DD]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="max-w-3xl mb-16 space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>02 / System Architecture & Features</span>
          </div>
          <h2 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-ink font-normal tracking-tight leading-[1.1]">
            Engineered for physical reliability, not cloud lock-in.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Every layer of ApnaSandesh is designed for local business owners who want complete ownership over their outreach without server bills, API downtime, or privacy compromises.
          </p>
        </div>

        {/* Feature Cards Grid */}
        <div className="space-y-16">
          {FEATURES.map((feat, idx) => (
            <div
              key={feat.number}
              className={`grid grid-cols-1 lg:grid-cols-12 gap-8 items-center p-6 sm:p-10 bg-white border border-[#E6E4DD] rounded-[4px] shadow-xs ${
                idx % 2 === 1 ? 'lg:grid-flow-dense' : ''
              }`}
            >
              {/* Text Specs (7 cols) */}
              <div
                className={`space-y-5 lg:col-span-7 ${
                  idx % 2 === 1 ? 'lg:col-start-6' : ''
                }`}
              >
                <div className="flex items-center gap-3 font-mono text-xs">
                  <span className="text-accent font-bold text-sm">{feat.number}</span>
                  <span className="text-[#8C8B85]">/</span>
                  <span className="text-ink-secondary uppercase tracking-wider font-semibold">
                    {feat.tag}
                  </span>
                  <span className="ml-auto px-2 py-0.5 rounded-[2px] text-[10px] bg-[#EDF5F1] text-forest border border-[#D5E2DA]">
                    {feat.badgeText}
                  </span>
                </div>

                <h3 className="font-serif text-2xl sm:text-3xl text-ink font-normal tracking-tight leading-snug">
                  {feat.title}
                </h3>

                <p className="text-sm sm:text-base text-ink-secondary leading-relaxed font-sans">
                  {feat.description}
                </p>

                <p className="text-xs text-ink-muted leading-relaxed font-sans border-l-2 border-[#E6E4DD] pl-3 italic">
                  {feat.subtext}
                </p>

                {/* Micro Metric Tiles */}
                <div className="grid grid-cols-3 gap-2 pt-2 border-t border-[#E6E4DD] font-mono text-xs">
                  {feat.metrics.map((m) => (
                    <div key={m.label} className="p-2 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
                      <div className="text-[10px] text-[#8C8B85] uppercase truncate">{m.label}</div>
                      <div className="text-xs font-bold text-ink mt-0.5">{m.value}</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Photographic Showcase Visual (5 cols) */}
              <div
                className={`lg:col-span-5 relative overflow-hidden rounded-[3px] border border-[#E6E4DD] bg-[#FAF9F6] aspect-[4/3] group ${
                  idx % 2 === 1 ? 'lg:col-start-1' : ''
                }`}
              >
                <img
                  src={feat.image}
                  alt={feat.title}
                  className="w-full h-full object-cover group-hover:scale-102 transition-transform duration-500"
                  loading="lazy"
                />
                <div className="absolute bottom-2 left-2 right-2 bg-studio-black/85 backdrop-blur-xs text-[#FAF9F6] p-2 rounded-[2px] font-mono text-[10px] flex items-center justify-between border border-white/10">
                  <span>SPECIMEN {feat.number} // HIGH RES</span>
                  <span className="text-accent">100% OPERATIONAL</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
