import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { SITE_CONFIG } from '@/lib/site-config';
import { ArrowLeft, ExternalLink, Sparkles } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Release Notes & Version History',
  description:
    'Explore the complete version history, release notes, and technical updates for ApnaSandesh native Android Business SMS app.',
};

const releases = [
  {
    version: 'v1.0.4',
    name: 'Production Launch & Dual-Transport Architecture',
    date: 'October 22, 2026',
    isLatest: true,
    highlights: [
      'Dual-transport engine: Choose between zero-permission Android SMS composer (Intent.ACTION_SENDTO) or automated Direct SIM dispatch.',
      'Dual-SIM slot selection: Pin campaigns to SIM 1 (e.g. Jio) or SIM 2 (e.g. Airtel) with automatic carrier subscription ID validation.',
      'Opt-out suppression firewall: Permanent on-device blacklist blocks opted-out recipients from all current and future campaign batches.',
      'Interactive GSM-7 & UCS-2 engine: Real-time encoding detection and character counter preventing unexpected segment multiplication.',
      'Zero cloud & zero network permission: Completely omits android.permission.INTERNET from AndroidManifest.xml. Customer data never leaves the device.',
      '11-table SQLite persistence: Room-managed normalized database with transactional integrity and CSV import/export via Storage Access Framework.'
    ]
  }
];

export default function ChangelogPage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        {/* Header */}
        <div className="space-y-4">
          <div>
            <Link
              href="/"
              className="inline-flex items-center gap-1.5 font-mono text-xs text-ink-secondary hover:text-ink transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Back to overview</span>
            </Link>
          </div>

          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>Changelog & Technical Audit</span>
          </div>

          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Version History & Release Notes
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Every release of ApnaSandesh is documented with technical transparency and verified Google Play builds.
          </p>
        </div>

        {/* Release List */}
        <div className="space-y-8">
          {releases.map((rel) => (
            <article key={rel.version} className="p-6 sm:p-8 bg-white border border-[#E6E4DD] rounded-[4px] space-y-6 shadow-xs">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-[#E6E4DD]">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-base font-bold text-accent">
                      {rel.version}
                    </span>
                    <span className="font-display font-semibold text-base text-ink">
                      — {rel.name}
                    </span>
                    {rel.isLatest && (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-[2px] font-mono text-[10px] bg-[#EDF5F1] text-forest border border-[#D5E2DA] font-semibold">
                        <Sparkles className="w-3 h-3" />
                        LATEST RELEASE
                      </span>
                    )}
                  </div>
                  <div className="font-mono text-xs text-[#8C8B85]">
                    Released: {rel.date} • Google Play Certified
                  </div>
                </div>

                <a
                  href={SITE_CONFIG.playStoreUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-[#FAF9F6] hover:bg-studio-black hover:text-white text-ink font-mono text-xs border border-[#E6E4DD] rounded-[2px] transition-colors"
                >
                  <span>View on Google Play</span>
                  <ExternalLink className="w-3 h-3" />
                </a>
              </div>

              <div className="space-y-3 font-sans text-xs sm:text-sm text-ink-secondary">
                <div className="font-mono text-xs uppercase tracking-wider text-ink font-semibold">
                  Key updates in this build:
                </div>
                <ul className="space-y-2">
                  {rel.highlights.map((highlight, idx) => (
                    <li key={idx} className="flex items-start gap-2">
                      <span className="font-mono text-accent font-bold">—</span>
                      <span>{highlight}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </article>
          ))}
        </div>
      </div>
    </div>
  );
}
