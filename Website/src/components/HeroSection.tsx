'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import HeroSpecimen from '@/components/HeroSpecimen';
import { SITE_CONFIG } from '@/lib/site-config';
import { Smartphone, Sliders, CheckCircle2 } from 'lucide-react';

export function HeroSection() {
  const [viewMode, setViewMode] = useState<'interactive' | 'visual'>('visual');

  return (
    <section className="py-16 sm:py-24 border-b border-[#E6E4DD] bg-canvas">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left Column: Editorial Headline & Value (7 cols) */}
          <div className="lg:col-span-7 space-y-6">
            {/* Monospace Eyebrow */}
            <div className="flex items-center gap-2">
              <span className="inline-block w-2 h-2 rounded-[1px] bg-accent" />
              <span className="font-mono text-xs uppercase tracking-wider text-ink-secondary font-semibold">
                ApnaSandesh / Autonomous Android Utility
              </span>
              <span className="font-mono text-xs text-[#8C8B85]">/ Google Play v{SITE_CONFIG.version}</span>
            </div>

            {/* Editorial Headline in Sentence Case */}
            <h1 className="font-serif text-4xl sm:text-5xl lg:text-6xl text-ink font-normal tracking-tight leading-[1.08]">
              Send business SMS directly from your SIM card.
            </h1>

            {/* Concrete & Direct Body (Max 60ch) */}
            <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed max-w-[62ch]">
              ApnaSandesh turns your Android phone into an autonomous messaging workstation. Import contacts from CSV or your address book, preview personalized templates, and send reminders or alerts over your cellular plan without paying monthly SaaS subscriptions or gateway markups.
            </p>

            {/* Concrete Metric Specimen Strip */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-2 border-t border-b border-[#E6E4DD] py-4 font-mono text-xs">
              <div>
                <div className="text-[10px] text-[#8C8B85] uppercase">Gateway markup</div>
                <div className="text-lg font-bold text-ink mt-0.5">₹0 / month</div>
              </div>
              <div>
                <div className="text-[10px] text-[#8C8B85] uppercase">Cloud leakage</div>
                <div className="text-lg font-bold text-forest mt-0.5">0 KB sent</div>
              </div>
              <div>
                <div className="text-[10px] text-[#8C8B85] uppercase">Database engine</div>
                <div className="text-lg font-bold text-ink mt-0.5">Room SQLite</div>
              </div>
              <div>
                <div className="text-[10px] text-[#8C8B85] uppercase">Google Play</div>
                <div className="text-lg font-bold text-forest mt-0.5">Verified Safe</div>
              </div>
            </div>

            {/* Actions */}
            <div className="pt-2 flex flex-wrap items-center gap-4">
              <a
                href={SITE_CONFIG.playStoreUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center justify-center gap-2 px-6 py-3.5 text-xs font-mono font-medium tracking-wide text-white bg-accent hover:bg-accent-hover rounded-[3px] border border-accent shadow-xs transition-colors"
              >
                <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
                  <path d="M3.609 1.814L13.793 12 3.61 22.186a2.38 2.38 0 0 1-.22-.99V2.805c0-.36.08-.7.22-.991zm11.242 11.243l2.25 2.25-11.83 6.702 9.58-8.952zm0-2.114L5.27 1.99l11.83 6.703-2.25 2.25zm1.414 1.414l3.125-1.77c.883-.5 1.418-1.42 1.418-2.437s-.535-1.937-1.418-2.437l-3.125-1.77-2.353 2.354 2.353 2.354z" />
                </svg>
                <span>Get on Google Play</span>
              </a>
              <Link
                href="/calculator"
                className="inline-flex items-center justify-center px-5 py-3.5 text-xs font-mono font-medium tracking-wide text-ink bg-canvas-muted hover:bg-[#EFEFED] rounded-[3px] border border-[#E6E4DD] transition-colors"
              >
                Open GSM-7 calculator →
              </Link>
              <a
                href="#architecture"
                className="text-xs font-mono text-ink-secondary hover:text-ink underline underline-offset-4 ml-1"
              >
                Read technical specs ↓
              </a>
            </div>

            {/* Live Trust Badges */}
            <div className="pt-3 flex flex-wrap items-center gap-4 text-xs font-mono text-ink-secondary">
              <span className="flex items-center gap-1.5 text-forest">
                <CheckCircle2 className="w-4 h-4" />
                No SIM ban risk (throttled queue)
              </span>
              <span className="flex items-center gap-1.5 text-ink-secondary">
                <CheckCircle2 className="w-4 h-4 text-accent" />
                Works with Jio, Airtel, Vi, BSNL
              </span>
            </div>
          </div>

          {/* Right Column: Hero Visual & Interactive Specimen (5 cols) */}
          <div className="lg:col-span-5 space-y-3">
            {/* Mode Switcher */}
            <div className="flex items-center justify-between bg-white border border-[#E6E4DD] rounded-[3px] p-1 font-mono text-xs">
              <span className="text-[11px] text-[#8C8B85] px-2 uppercase tracking-wider">
                Hero Display
              </span>
              <div className="flex items-center gap-1">
                <button
                  type="button"
                  onClick={() => setViewMode('visual')}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-[2px] transition-colors ${
                    viewMode === 'visual'
                      ? 'bg-ink text-canvas font-semibold'
                      : 'text-ink-secondary hover:text-ink hover:bg-[#F3F2ED]'
                  }`}
                >
                  <Smartphone className="w-3.5 h-3.5" />
                  <span>App Mockup</span>
                </button>
                <button
                  type="button"
                  onClick={() => setViewMode('interactive')}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-[2px] transition-colors ${
                    viewMode === 'interactive'
                      ? 'bg-ink text-canvas font-semibold'
                      : 'text-ink-secondary hover:text-ink hover:bg-[#F3F2ED]'
                  }`}
                >
                  <Sliders className="w-3.5 h-3.5" />
                  <span>Live Simulator</span>
                </button>
              </div>
            </div>

            {/* Switchable View Container */}
            {viewMode === 'visual' ? (
              <div className="relative rounded-[4px] border border-[#E6E4DD] bg-white overflow-hidden shadow-xs group">
                <div className="aspect-[16/10] overflow-hidden bg-[#FAF9F6] relative">
                  <img
                    src="/apnasandesh_hero_banner.png"
                    alt="ApnaSandesh Android Application Hero Banner"
                    className="w-full h-full object-cover group-hover:scale-102 transition-transform duration-500"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-studio-black/80 via-transparent to-transparent flex items-end p-4">
                    <div className="text-white space-y-1">
                      <div className="font-mono text-[10px] text-accent font-semibold uppercase tracking-wider">
                        Native Android Interface
                      </div>
                      <div className="font-serif text-lg text-white font-normal">
                        Material 3 Dark Theme on Android 15
                      </div>
                    </div>
                  </div>
                </div>

                <div className="p-4 bg-white border-t border-[#E6E4DD] flex items-center justify-between text-xs font-mono text-ink-secondary">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-forest" />
                    <span>Jio 5G / Airtel 5G Dual SIM Ready</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setViewMode('interactive')}
                    className="text-accent hover:underline flex items-center gap-1"
                  >
                    Test simulator →
                  </button>
                </div>
              </div>
            ) : (
              <HeroSpecimen />
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
