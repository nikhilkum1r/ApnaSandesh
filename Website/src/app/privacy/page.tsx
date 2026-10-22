import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { SITE_CONFIG } from '@/lib/site-config';
import { ArrowLeft, ShieldCheck, Lock, Database, WifiOff } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Privacy Policy & Zero Cloud Custody Guarantee',
  description:
    'ApnaSandesh Privacy Policy: 100% on-device Room SQLite database. Zero external servers, zero telemetry, zero analytics tracking.',
};

export default function PrivacyPage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        {/* Back Link */}
        <div>
          <Link
            href="/"
            className="inline-flex items-center gap-1.5 font-mono text-xs text-ink-secondary hover:text-ink transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to overview</span>
          </Link>
        </div>

        {/* Page Header */}
        <div className="space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-accent font-semibold flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-forest" />
            <span>Privacy Policy // Effective October 2026</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Zero-Cloud Data Custody Guarantee
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            ApnaSandesh is an offline-first Android application designed and distributed by {SITE_CONFIG.parentBrand.name}. We built this utility on a radical principle: your customer data belongs exclusively on your physical phone, never on our servers.
          </p>
        </div>

        {/* 4 Pillars of Privacy */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 font-mono text-xs">
          <div className="p-5 bg-white border border-[#E6E4DD] rounded-[4px] space-y-2 shadow-xs">
            <div className="flex items-center gap-2 text-forest font-semibold">
              <Database className="w-4 h-4" />
              <span>100% LOCAL ROOM SQLITE</span>
            </div>
            <p className="text-xs text-ink-secondary font-sans leading-relaxed">
              Contacts, campaign logs, dynamic templates, and suppression lists reside entirely in local private application storage (`/data/data/{SITE_CONFIG.packageId}/databases`).
            </p>
          </div>

          <div className="p-5 bg-white border border-[#E6E4DD] rounded-[4px] space-y-2 shadow-xs">
            <div className="flex items-center gap-2 text-forest font-semibold">
              <WifiOff className="w-4 h-4" />
              <span>ZERO NETWORK TRANSMISSION</span>
            </div>
            <p className="text-xs text-ink-secondary font-sans leading-relaxed">
              ApnaSandesh does not declare or use remote network communication sockets to transfer contact lists, message text, or customer data to external cloud servers.
            </p>
          </div>

          <div className="p-5 bg-white border border-[#E6E4DD] rounded-[4px] space-y-2 shadow-xs">
            <div className="flex items-center gap-2 text-forest font-semibold">
              <Lock className="w-4 h-4" />
              <span>NO ANALYTICS OR TRACKERS</span>
            </div>
            <p className="text-xs text-ink-secondary font-sans leading-relaxed">
              We do not embed Google Firebase Analytics, Facebook SDK, Mixpanel, AppsFlyer, or advertising identifiers. Zero user behavioral profiling.
            </p>
          </div>

          <div className="p-5 bg-white border border-[#E6E4DD] rounded-[4px] space-y-2 shadow-xs">
            <div className="flex items-center gap-2 text-forest font-semibold">
              <ShieldCheck className="w-4 h-4" />
              <span>GOOGLE PLAY VERIFIED</span>
            </div>
            <p className="text-xs text-ink-secondary font-sans leading-relaxed">
              Fully compliant with Google Play Data Safety policies declaring &ldquo;No data collected&rdquo; and &ldquo;No data shared with third parties&rdquo;.
            </p>
          </div>
        </div>

        {/* Full Legal Text */}
        <div className="p-8 sm:p-10 bg-white border border-[#E6E4DD] rounded-[4px] space-y-8 text-sm text-ink-secondary font-sans leading-relaxed shadow-xs">
          <section className="space-y-3">
            <h2 className="font-display font-bold text-lg text-ink">1. Information We Collect</h2>
            <p>
              We do not collect, harvest, store, or transmit any personal data, contact information, customer lists, message templates, or dispatch histories to external servers. All operations happen within the memory and local storage of your Android hardware device.
            </p>
          </section>

          <section className="space-y-3">
            <h2 className="font-display font-bold text-lg text-ink">2. Use of Android Telephony Capabilities</h2>
            <p>
              ApnaSandesh requests access to Android telephony services solely to transmit messages you explicitly create and trigger. Messages are dispatched via standard cellular SIM radio channels governed by your contract with your cellular provider (Jio, Airtel, Vi, BSNL, etc.).
            </p>
          </section>

          <section className="space-y-3">
            <h2 className="font-display font-bold text-lg text-ink">3. Data Retention and Deletion</h2>
            <p>
              Because your data is saved solely in the application&apos;s local Room SQLite database, you retain full ownership. If you uninstall ApnaSandesh or clear application storage via Android Settings, all contacts, templates, and history logs are permanently and immediately erased from your device.
            </p>
          </section>

          <section className="space-y-3">
            <h2 className="font-display font-bold text-lg text-ink">4. Telecom Compliance and TRAI Regulations</h2>
            <p>
              Users are responsible for ensuring that messages sent through ApnaSandesh comply with all applicable local, state, and national telecommunications regulations, including the Telecom Commercial Communications Customer Preference Regulations (TCCCPR) in India and carrier fair use policies.
            </p>
          </section>

          <section className="space-y-3 pt-4 border-t border-[#E6E4DD] font-mono text-xs">
            <div>Developer: {SITE_CONFIG.parentBrand.name}</div>
            <div>Contact: <a href={`mailto:${SITE_CONFIG.parentBrand.supportEmail}`} className="text-accent hover:underline">{SITE_CONFIG.parentBrand.supportEmail}</a></div>
            <div>Registered URL: <a href={SITE_CONFIG.url} className="text-ink hover:underline">{SITE_CONFIG.url}</a></div>
          </section>
        </div>
      </div>
    </div>
  );
}
