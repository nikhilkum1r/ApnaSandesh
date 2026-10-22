import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { FeatureEditorialList } from '@/components/FeatureEditorialList';
import { ArchitectureSpec } from '@/components/ArchitectureSpec';
import { ArrowLeft } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Technical Features & Android Architecture',
  description:
    'Deep-dive into ApnaSandesh on-device architecture: Room SQLite sandbox, dual-SIM dispatch engine, dynamic template interpolation, and automated opt-out suppression.',
};

export default function FeaturesPage() {
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
            <span>Deep Dive // System Architecture</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Built for physical hardware sovereignty.
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Examine how every component inside ApnaSandesh functions offline, from the TelephonyManager RIL radio hooks to the encrypted Room SQLite local storage.
          </p>
        </div>

        <FeatureEditorialList />
        <ArchitectureSpec />
      </div>
    </div>
  );
}
