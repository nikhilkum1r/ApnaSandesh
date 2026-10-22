import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { UseCasesShowcase } from '@/components/UseCasesShowcase';
import { ArrowLeft } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Industry Blueprints & Field Use Cases',
  description:
    'Discover how Indian and global merchants use ApnaSandesh: Kirana stores, doctors clinics, coaching institutes, mobile repair shops, and boutique salons.',
};

export default function UseCasesPage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div>
          <Link
            href="/"
            className="inline-flex items-center gap-1.5 font-mono text-xs text-ink-secondary hover:text-ink transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to overview</span>
          </Link>
        </div>

        <div className="max-w-3xl space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>Field Applications // Practical Economics</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Built for local merchants and independent practitioners.
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            See how business owners bypass enterprise SaaS bills and reach their regular customers reliably through standard cellular SMS.
          </p>
        </div>

        <UseCasesShowcase />
      </div>
    </div>
  );
}
