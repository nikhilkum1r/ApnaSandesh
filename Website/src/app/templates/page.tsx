import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import TemplateExplorer from '@/components/TemplateExplorer';
import { ArrowLeft } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Business SMS Template Library — Character-Optimized Copy',
  description:
    'Explore ready-to-use business SMS templates for order confirmations, appointment reminders, payment collections, and service receipts. GSM-7 & Unicode optimized.',
};

export default function TemplatesPage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
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
            <span>Character-Optimized Drafts // Free Library</span>
          </div>

          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Business SMS Template Library
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed max-w-2xl">
            Plain, character-optimized message drafts for service professionals, clinics, auto shops, and retailers. Tested against standard GSM-7 limits to prevent unexpected segment charges.
          </p>
        </div>

        {/* Template Explorer Component */}
        <TemplateExplorer />

        {/* Technical Guidelines Strip */}
        <div className="p-6 bg-[#FFFFFF] border border-[#E6E4DD] rounded-[4px] space-y-4 shadow-xs">
          <h2 className="font-display font-semibold text-lg text-ink">
            Telecom guidelines for message templates
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs text-ink-secondary font-sans leading-relaxed">
            <div className="space-y-2">
              <h3 className="font-mono font-bold text-xs text-ink uppercase tracking-wider">
                1. Dynamic Token Safety
              </h3>
              <p>
                When substituting names (<code className="font-mono text-ink bg-[#FAF9F6] px-1 py-0.5 border border-[#E6E4DD] rounded-[2px]">{`{name}`}</code>) or order identifiers, leave 20 to 25 characters of safety margin to ensure long customer names do not spill into a second SMS segment.
              </p>
            </div>
            <div className="space-y-2">
              <h3 className="font-mono font-bold text-xs text-ink uppercase tracking-wider">
                2. Transactional vs Promotional Wording
              </h3>
              <p>
                Transactional notices (appointment confirmations, repair pickups, invoice balance notices) enjoy significantly higher open rates and near-zero carrier dispute rates when kept strictly factual and polite.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
