import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { SITE_CONFIG } from '@/lib/site-config';
import { ArrowLeft, FileText } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Terms of Service & Fair-Use Agreement',
  description:
    'Terms of service, acceptable use policies, and carrier fair-use agreements for ApnaSandesh native Android Business SMS utility.',
};

export default function TermsPage() {
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
            <FileText className="w-4 h-4 text-accent" />
            <span>Legal Agreement // Commercial Policies</span>
          </div>

          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Terms of Service & Fair Use Agreement
          </h1>
          <div className="font-mono text-xs text-ink-secondary flex items-center gap-3">
            <span>Effective Date: October 22, 2026</span>
            <span>•</span>
            <span>{SITE_CONFIG.parentBrand.name}</span>
          </div>
        </div>

        {/* Agreement Body */}
        <div className="p-8 sm:p-10 bg-white border border-[#E6E4DD] rounded-[4px] space-y-8 text-ink-secondary font-sans text-xs sm:text-sm leading-relaxed shadow-xs">
          <section className="space-y-3">
            <h2 className="font-display font-bold text-xl text-ink">
              1. Acceptance of Terms & Product Purpose
            </h2>
            <p>
              By downloading, installing, or operating <strong>ApnaSandesh</strong> (the &ldquo;Application&rdquo;), you agree to be bound by these Terms of Service. ApnaSandesh is provided by <strong>{SITE_CONFIG.parentBrand.name}</strong> as an on-device business messaging and customer management utility.
            </p>
            <p>
              The Application is intended exclusively for relationship-based commercial communications between legitimate business owners and their existing, opted-in customers.
            </p>
          </section>

          <section className="space-y-3">
            <h2 className="font-display font-bold text-xl text-ink">
              2. Prohibition of Unsolicited Commercial Spam
            </h2>
            <p>
              You agree not to use ApnaSandesh to transmit:
            </p>
            <ul className="space-y-2 pl-4 list-disc text-xs">
              <li>Unsolicited commercial advertisements to purchased, rented, or scraped phone lists.</li>
              <li>Fraudulent phishing messages or deceptive solicitations.</li>
              <li>Harassing, defamatory, or abusive communications.</li>
              <li>Messages violating national telecom regulations (such as India&apos;s TRAI TCCCPR or equivalent global rules).</li>
            </ul>
          </section>

          <section className="space-y-3">
            <h2 className="font-display font-bold text-xl text-ink">
              3. Cellular Carrier Tariffs & Quotas
            </h2>
            <p>
              ApnaSandesh sends messages directly through your device&apos;s cellular SIM card. All SMS charges, segment tariffs, and bundled plan allowances are governed strictly by your contract with your cellular provider (e.g., Jio, Airtel, Vodafone Idea).
            </p>
            <p>
              {SITE_CONFIG.parentBrand.name} does not charge per-message fees and is not liable for carrier overage fees incurred from sending messages outside your operator plan.
            </p>
          </section>

          <section className="space-y-3">
            <h2 className="font-display font-bold text-xl text-ink">
              4. Local Data Sandbox Ownership
            </h2>
            <p>
              Because ApnaSandesh has no cloud servers or network synchronization, you maintain total custody of your contact records and message logs. You are solely responsible for exporting and backing up your database before clearing device storage or replacing hardware.
            </p>
          </section>

          <section className="space-y-3">
            <h2 className="font-display font-bold text-xl text-ink">
              5. Inquiries & Legal Contact
            </h2>
            <p>
              For legal inquiries or regulatory questions regarding ApnaSandesh:
            </p>
            <div className="font-mono text-xs text-ink bg-[#FAF9F6] p-4 border border-[#E6E4DD] rounded-[2px] space-y-1">
              <div>Engineering Studio: {SITE_CONFIG.parentBrand.name}</div>
              <div>Email: <a href={`mailto:${SITE_CONFIG.parentBrand.supportEmail}`} className="text-accent hover:underline">{SITE_CONFIG.parentBrand.supportEmail}</a></div>
              <div>Website: <a href={SITE_CONFIG.parentBrand.url} className="text-accent hover:underline">{SITE_CONFIG.parentBrand.url}</a></div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
