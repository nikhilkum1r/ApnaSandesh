import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { SITE_CONFIG } from '@/lib/site-config';
import { ArrowLeft, BookOpen, FileSpreadsheet, Smartphone, Clock, ShieldAlert } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Documentation & Merchant User Manual',
  description:
    'Complete setup guide for ApnaSandesh: CSV spreadsheet formatting, dual-SIM slot selection, campaign scheduling, and opt-out suppression rules.',
};

export default function DocsPage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
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
        <div className="max-w-3xl space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-accent" />
            <span>Developer & Merchant Manual // Version {SITE_CONFIG.version}</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            ApnaSandesh Operational Documentation
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Everything you need to configure offline campaigns, format customer contact spreadsheets, and manage carrier SIM quotas.
          </p>
        </div>

        {/* Content Modules */}
        <div className="space-y-10">
          {/* Module 1: CSV Formatting */}
          <div className="p-8 bg-white border border-[#E6E4DD] rounded-[4px] space-y-4 shadow-xs">
            <div className="flex items-center gap-3 font-mono text-xs text-accent font-semibold">
              <FileSpreadsheet className="w-5 h-5" />
              <span>01 / CSV SPREADSHEET INGESTION SPECIFICATION</span>
            </div>
            <h2 className="font-display font-bold text-xl text-ink">
              Formatting Your Contact Lists
            </h2>
            <p className="text-sm text-ink-secondary font-sans leading-relaxed">
              ApnaSandesh supports standard comma-delimited (.csv) files exported from Microsoft Excel, Google Sheets, LibreOffice, or your billing software (Tally, Vyapar, Marg, Khatabook).
            </p>

            <div className="p-4 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] font-mono text-xs text-ink space-y-2">
              <div className="text-[10px] text-[#8C8B85] uppercase">Recommended Header Row:</div>
              <pre className="text-xs overflow-x-auto text-[#141413]">
{`name,phone,amount,order_id,date
Rajesh Sharma,9820144521,1450,810,Oct 28
Priya Verma,9711088234,890,4892,Oct 29
Sunil Kumar,9443210982,2200,9021,Nov 01`}
              </pre>
            </div>

            <ul className="text-xs font-mono text-ink-secondary space-y-1.5 list-disc list-inside">
              <li>Phone numbers can be 10 digits (`9820144521`), with 0 prefix (`09820144521`), or with country code (`+919820144521`).</li>
              <li>Columns other than phone are automatically converted into dynamic template tokens like <code className="text-accent">{`{name}`}</code>, <code className="text-accent">{`{amount}`}</code>, and <code className="text-accent">{`{order_id}`}</code>.</li>
            </ul>
          </div>

          {/* Module 2: Dual SIM & Carrier Settings */}
          <div className="p-8 bg-white border border-[#E6E4DD] rounded-[4px] space-y-4 shadow-xs">
            <div className="flex items-center gap-3 font-mono text-xs text-accent font-semibold">
              <Smartphone className="w-5 h-5" />
              <span>02 / DUAL-SIM CARRIER HARDWARE SELECTION</span>
            </div>
            <h2 className="font-display font-bold text-xl text-ink">
              Selecting SIM 1 or SIM 2 for Dispatch
            </h2>
            <p className="text-sm text-ink-secondary font-sans leading-relaxed">
              If your Android phone has dual physical SIMs or a SIM + eSIM setup (e.g. SIM 1: Jio 5G, SIM 2: Airtel 5G), ApnaSandesh lets you bind campaigns to a specific slot.
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 font-mono text-xs">
              <div className="p-4 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] space-y-1">
                <div className="font-bold text-ink">Slot 0 (SIM 1)</div>
                <p className="text-ink-secondary text-[11px] font-sans">
                  Directs dispatches via your primary SIM card. Best for daily operations where you have an active unlimited national calling and 100 SMS/day allowance.
                </p>
              </div>
              <div className="p-4 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] space-y-1">
                <div className="font-bold text-ink">Slot 1 (SIM 2)</div>
                <p className="text-ink-secondary text-[11px] font-sans">
                  Useful if you maintain a dedicated business SIM number registered under your shop or clinic brand name.
                </p>
              </div>
            </div>
          </div>

          {/* Module 3: Scheduling */}
          <div className="p-8 bg-white border border-[#E6E4DD] rounded-[4px] space-y-4 shadow-xs">
            <div className="flex items-center gap-3 font-mono text-xs text-accent font-semibold">
              <Clock className="w-5 h-5" />
              <span>03 / DEFERRED CAMPAIGN SCHEDULING</span>
            </div>
            <h2 className="font-display font-bold text-xl text-ink">
              Scheduling Campaigns via Android WorkManager
            </h2>
            <p className="text-sm text-ink-secondary font-sans leading-relaxed">
              Compose campaigns ahead of time and schedule them for optimal customer attention windows (such as tomorrow at 10:30 AM). ApnaSandesh delegates execution to Android’s OS-level alarm and work scheduler, waking up only when the timer expires.
            </p>
          </div>

          {/* Module 4: Opt-Out Firewall */}
          <div className="p-8 bg-white border border-[#E6E4DD] rounded-[4px] space-y-4 shadow-xs">
            <div className="flex items-center gap-3 font-mono text-xs text-forest font-semibold">
              <ShieldAlert className="w-5 h-5" />
              <span>04 / AUTOMATIC UNSUBSCRIBE & SUPPRESSION</span>
            </div>
            <h2 className="font-display font-bold text-xl text-ink">
              Managing Customer Opt-Outs
            </h2>
            <p className="text-sm text-ink-secondary font-sans leading-relaxed">
              When a recipient replies with standard opt-out keywords (&ldquo;STOP&rdquo;, &ldquo;CANCEL&rdquo;, &ldquo;UNSUBSCRIBE&rdquo;), you can add them to your suppression list with 1 tap. ApnaSandesh permanently skips suppressed contacts in all future campaigns, ensuring full consumer preference compliance.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
