import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { SITE_CONFIG } from '@/lib/site-config';
import { ArrowLeft, ShieldAlert } from 'lucide-react';

export const metadata: Metadata = {
  title: 'India TRAI TCCCPR & SIM Compliance Guide',
  description:
    'Understand TRAI TCCCPR regulations, personal SIM 100 SMS/day limits, DLT requirements, and safe customer communication guidelines with ApnaSandesh.',
};

export default function CompliancePage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        {/* Breadcrumb & Header */}
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
            <span>Regulatory Clarity // India Telecom Policy</span>
          </div>

          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            India TRAI regulations & SIM fair-use handbook
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            The practical guide to sending lawful business messages from your cellular SIM without incurring carrier blacklists or telecom penalties.
          </p>
        </div>

        {/* Legal Notice Callout */}
        <div className="p-5 bg-white border border-[#E6E4DD] rounded-[4px] space-y-2 font-mono text-xs shadow-xs">
          <div className="text-ink font-semibold uppercase tracking-wider flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-accent" />
            <span>Regulatory Clarification: Relationship Messages vs Commercial Telemarketing</span>
          </div>
          <p className="font-sans text-xs text-ink-secondary leading-relaxed">
            ApnaSandesh is designed for relationship communication with your existing, opted-in customers (patients, registered repair clients, retail invoice recipients). It is strictly not intended for unsolicited cold marketing or mass promotional blasting.
          </p>
        </div>

        {/* Main Guidance Sections */}
        <div className="space-y-10 text-ink-secondary font-sans text-sm leading-relaxed">
          {/* 01 TRAI 100 SMS/Day Cap */}
          <section className="space-y-3">
            <div className="font-mono text-xs text-accent uppercase tracking-wider font-semibold">
              01 / Daily Volume Quotas
            </div>
            <h2 className="font-display font-bold text-xl text-ink">
              Understanding the 100 SMS/day per SIM threshold
            </h2>
            <p>
              Under TRAI's Telecom Commercial Communications Customer Preference Regulations (TCCCPR), Indian cellular operators (Jio, Airtel, Vodafone Idea, BSNL) enforce a standard cap of 100 SMS per day per SIM card for individual prepaid and postpaid subscribers.
            </p>
            <div className="p-4 bg-white border border-[#E6E4DD] rounded-[4px] space-y-2 text-xs shadow-xs">
              <div className="font-mono text-ink font-semibold">What this means for your daily operations:</div>
              <p>
                If your clinic or shop sends 40 to 80 appointment reminders, readiness alerts, or payment receipts each day, you remain entirely within your carrier’s standard bundled SMS quota. ApnaSandesh includes a live counter to alert you before you reach daily carrier thresholds.
              </p>
            </div>
          </section>

          {/* 02 Daylight Sending Windows */}
          <section className="space-y-3">
            <div className="font-mono text-xs text-accent uppercase tracking-wider font-semibold">
              02 / Time Windows
            </div>
            <h2 className="font-display font-bold text-xl text-ink">
              Enforced daylight sending hours (10:00 AM to 9:00 PM IST)
            </h2>
            <p>
              Indian telecom regulations strictly prohibit promotional commercial communications outside the 10:00 AM to 9:00 PM IST window.
            </p>
            <p>
              ApnaSandesh includes built-in scheduling via Android WorkManager. If you compose campaign drafts late in the evening, the app lets you schedule them to dispatch automatically the following morning at 10:00 AM.
            </p>
          </section>

          {/* 03 Mandatory Opt-Out & Suppression */}
          <section className="space-y-3">
            <div className="font-mono text-xs text-accent uppercase tracking-wider font-semibold">
              03 / Customer Rights
            </div>
            <h2 className="font-display font-bold text-xl text-ink">
              Mandatory opt-out instructions and on-device suppression
            </h2>
            <p>
              Always provide a clear opt-out path in your message templates (e.g., &ldquo;Reply STOP to cancel&rdquo;). When a customer replies STOP, mark them as opted-out in ApnaSandesh.
            </p>
            <p>
              ApnaSandesh&apos;s SQLite suppression firewall permanently blacklists opted-out numbers. Even if you subsequently re-import a customer CSV list that includes that number, ApnaSandesh automatically silences and suppresses the dispatch.
            </p>
          </section>

          {/* 04 Telecom Checklist Table */}
          <section className="space-y-4 pt-4 border-t border-[#E6E4DD]">
            <h2 className="font-display font-bold text-xl text-ink">
              Compliance verification checklist
            </h2>
            <div className="w-full overflow-x-auto border border-[#E6E4DD] rounded-[4px] bg-white shadow-xs">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="bg-[#FAF9F6] border-b border-[#E6E4DD] font-mono text-ink">
                    <th className="p-3.5 font-semibold">Requirement</th>
                    <th className="p-3.5 font-semibold">TRAI / Carrier Rule</th>
                    <th className="p-3.5 font-semibold">ApnaSandesh Implementation</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#E6E4DD] text-ink-secondary">
                  <tr>
                    <td className="p-3.5 font-mono text-ink font-medium">Daily Quota</td>
                    <td className="p-3.5">100 SMS / day / SIM</td>
                    <td className="p-3.5">Real-time daily segment tracker & limit alert</td>
                  </tr>
                  <tr>
                    <td className="p-3.5 font-mono text-ink font-medium">Sending Hours</td>
                    <td className="p-3.5">10 AM – 9 PM IST</td>
                    <td className="p-3.5">Campaign scheduler powered by Android WorkManager</td>
                  </tr>
                  <tr>
                    <td className="p-3.5 font-mono text-ink font-medium">Opt-Out Handling</td>
                    <td className="p-3.5">Mandatory cessation upon request</td>
                    <td className="p-3.5">Permanent on-device SQLite suppression firewall</td>
                  </tr>
                  <tr>
                    <td className="p-3.5 font-mono text-ink font-medium">Burst Throttling</td>
                    <td className="p-3.5">Prevent automated burst spam</td>
                    <td className="p-3.5">Configurable 3.5s to 6.0s sequential delay per SMS</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>

        {/* Play Store Link Banner */}
        <div className="p-6 bg-studio-black text-[#FAF9F6] rounded-[4px] border border-[#2C2B28] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 font-mono text-xs">
          <div className="space-y-1">
            <div className="font-bold text-white text-sm">Download ApnaSandesh on Google Play</div>
            <p className="text-[#8C8B85] font-sans text-xs">
              Start communicating responsibly with your client list today.
            </p>
          </div>
          <a
            href={SITE_CONFIG.playStoreUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 bg-accent hover:bg-accent-hover text-white rounded-[2px] uppercase tracking-wider font-semibold whitespace-nowrap transition-colors"
          >
            View on Google Play ↗
          </a>
        </div>
      </div>
    </div>
  );
}
