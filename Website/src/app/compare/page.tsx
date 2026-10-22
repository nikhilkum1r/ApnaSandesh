import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { CostComparisonTable } from '@/components/CostComparisonTable';
import { ArrowLeft } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Cost & Architecture Comparison: ApnaSandesh vs Cloud SMS Gateways',
  description:
    'Compare ApnaSandesh against Twilio, Fast2SMS, Exotel, and MSG91. Understand the real financial and privacy costs of cloud vs local SIM dispatch.',
};

export default function ComparePage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
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
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>Economics & Trade-Offs // Direct Comparison</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Why pay ₹30,000/year for messages your SIM already includes?
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Standard prepaid plans from Jio, Airtel, and Vodafone Idea already include 100 free SMS daily. Cloud gateways charge high markups and store your customer records remotely.
          </p>
        </div>

        <CostComparisonTable />
      </div>
    </div>
  );
}
