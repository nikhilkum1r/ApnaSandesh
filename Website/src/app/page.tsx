import React from 'react';
import Link from 'next/link';
import { HeroSection } from '@/components/HeroSection';
import { FeatureEditorialList } from '@/components/FeatureEditorialList';
import { UseCasesShowcase } from '@/components/UseCasesShowcase';
import SmsCalculator from '@/components/SmsCalculator';
import { CostComparisonTable } from '@/components/CostComparisonTable';
import { ArchitectureSpec } from '@/components/ArchitectureSpec';
import { TelecomComplianceSection } from '@/components/TelecomComplianceSection';
import EditorialFaq from '@/components/EditorialFaq';
import { SITE_CONFIG } from '@/lib/site-config';

export default function HomePage() {
  return (
    <>
      {/* 01. Hero Section with Photographic Showcase & Interactive Simulator */}
      <HeroSection />

      {/* 02. Numbered Editorial Feature Breakdown with High-Res Architecture Visuals */}
      <FeatureEditorialList />

      {/* 03. Field Applications & Industry Use Cases with Real Photography */}
      <UseCasesShowcase />

      {/* 04. Interactive GSM-7 / UCS-2 Segment & Savings Calculator */}
      <section className="py-24 bg-[#FAF9F6] border-b border-[#E6E4DD]" id="calculator">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="max-w-3xl mb-12 space-y-4">
            <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
              <span className="w-2 h-2 rounded-[1px] bg-accent" />
              <span>Interactive Telephony Forecaster</span>
            </div>
            <h2 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-ink font-normal tracking-tight leading-[1.1]">
              Test any message template against real carrier encoding limits
            </h2>
            <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
              Type or paste your message to inspect character counts, GSM-7 vs. Unicode encoding switches, multipart concatenation overhead, and daily SIM limit impact.
            </p>
          </div>

          <div className="bg-white p-6 sm:p-10 border border-[#E6E4DD] rounded-[4px] shadow-xs">
            <SmsCalculator />
          </div>
        </div>
      </section>

      {/* 05. Direct Cost Comparison: SIM Quotas vs. Cloud SMS Aggregators */}
      <section id="comparison">
        <CostComparisonTable />
      </section>

      {/* 06. Technical Architecture: SQLite Schemas, Telephony API & Security Model */}
      <section id="architecture">
        <ArchitectureSpec />
      </section>

      {/* 07. Regulatory Compliance: India TRAI Guidelines, 100 SMS Cap & DLT Rules */}
      <section id="telecom-rules">
        <TelecomComplianceSection />
      </section>

      {/* 08. In-Depth Operational FAQ */}
      <section id="faq">
        <EditorialFaq />
      </section>

      {/* 09. Production CTA Strip */}
      <section className="py-20 bg-studio-black text-[#FAF9F6] border-t border-[#2C2B28]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="max-w-4xl mx-auto text-center space-y-8">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-[2px] bg-[#1F1F1D] border border-[#2C2B28] font-mono text-xs text-accent">
              <span>ZERO INTERNET PERMISSION IN MANIFEST</span>
            </div>

            <h2 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-white font-normal tracking-tight leading-[1.1]">
              Start running customer communications from your own SIM card today.
            </h2>

            <p className="text-base sm:text-lg text-studio-muted font-sans max-w-2xl mx-auto leading-relaxed">
              Download the official Google Play release of ApnaSandesh v{SITE_CONFIG.version}. 100% on-device SQLite database, 0 cloud subscription fees, zero customer data tracking.
            </p>

            <div className="flex flex-wrap items-center justify-center gap-4 pt-4">
              <a
                href={SITE_CONFIG.playStoreUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-6 py-4 bg-accent hover:bg-accent-hover text-white text-xs font-mono font-medium rounded-[3px] border border-accent uppercase tracking-wider shadow-xs transition-colors"
              >
                <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
                  <path d="M3.609 1.814L13.793 12 3.61 22.186a2.38 2.38 0 0 1-.22-.99V2.805c0-.36.08-.7.22-.991zm11.242 11.243l2.25 2.25-11.83 6.702 9.58-8.952zm0-2.114L5.27 1.99l11.83 6.703-2.25 2.25zm1.414 1.414l3.125-1.77c.883-.5 1.418-1.42 1.418-2.437s-.535-1.937-1.418-2.437l-3.125-1.77-2.353 2.354 2.353 2.354z" />
                </svg>
                <span>Get on Google Play</span>
              </a>
              <Link
                href="/download"
                className="px-5 py-4 bg-[#1F1F1D] hover:bg-[#2C2B28] text-[#FAF9F6] text-xs font-mono rounded-[3px] border border-[#2C2B28] transition-colors"
              >
                Review APK Checksum & Direct Download
              </Link>
              <a
                href={`mailto:${SITE_CONFIG.parentBrand.engineeringEmail}`}
                className="text-xs font-mono text-studio-muted hover:text-white underline underline-offset-4"
              >
                Contact TruStoryX engineering →
              </a>
            </div>

            <div className="pt-8 border-t border-[#262624] grid grid-cols-1 sm:grid-cols-4 gap-4 font-mono text-xs text-studio-muted">
              <div>Package: <span className="text-[#B8B7B0]">{SITE_CONFIG.packageId}</span></div>
              <div>Data Safety: <span className="text-[#10B981]">0 Data Collected</span></div>
              <div>Min SDK: <span className="text-[#B8B7B0]">{SITE_CONFIG.minSdk}</span></div>
              <div>Target: <span className="text-[#B8B7B0]">{SITE_CONFIG.targetSdk}</span></div>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}
