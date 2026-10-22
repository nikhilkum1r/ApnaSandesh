import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { SITE_CONFIG } from '@/lib/site-config';
import { ArrowLeft, CheckCircle2, Shield } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Get ApnaSandesh on Google Play — Official Android Release',
  description:
    'Install ApnaSandesh Android application securely from Google Play Store. 100% offline-first Room SQLite business SMS workstation.',
};

export default function DownloadPage() {
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
            <span>Official Distribution // Google Play Store</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Get ApnaSandesh on Google Play
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Install the verified production release directly on any smartphone running Android 8.0 or higher.
          </p>
        </div>

        {/* Action Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Primary: Google Play Store Card */}
          <div className="p-8 bg-white border border-[#E6E4DD] rounded-[4px] space-y-6 shadow-xs flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="px-2.5 py-1 rounded-[2px] font-mono text-[11px] bg-[#EDF5F1] text-forest border border-[#D5E2DA] font-semibold">
                  OFFICIAL RELEASE
                </span>
                <span className="font-mono text-xs text-ink-secondary">Google Play Protect</span>
              </div>

              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-[4px] bg-white p-2 border border-[#E6E4DD] flex items-center justify-center">
                  <img src="/logo.svg" alt="ApnaSandesh logo" className="w-full h-full object-contain" />
                </div>
                <div>
                  <h2 className="font-display font-bold text-xl text-ink">Google Play Store</h2>
                  <p className="text-xs text-ink-secondary font-mono mt-0.5">Instant install & automatic updates</p>
                </div>
              </div>

              <p className="text-sm text-ink-secondary font-sans leading-relaxed">
                Install via Google Play with automated updates, verified app integrity, and Google Play Protect security screening. Zero cloud account or registration needed.
              </p>

              <div className="space-y-2 text-xs font-mono text-ink-secondary border-t border-[#E6E4DD] pt-4">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-forest" />
                  <span>Google Play verified safe developer</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-forest" />
                  <span>Zero user trackers or third-party analytics</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-forest" />
                  <span>Signed with TruStoryX production keystore</span>
                </div>
              </div>
            </div>

            <div className="pt-6">
              <a
                href={SITE_CONFIG.playStoreUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full inline-flex items-center justify-center gap-2.5 py-4 px-6 text-xs font-mono font-medium tracking-wide text-white bg-accent hover:bg-accent-hover rounded-[3px] border border-accent uppercase shadow-xs transition-colors"
              >
                <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
                  <path d="M3.609 1.814L13.793 12 3.61 22.186a2.38 2.38 0 0 1-.22-.99V2.805c0-.36.08-.7.22-.991zm11.242 11.243l2.25 2.25-11.83 6.702 9.58-8.952zm0-2.114L5.27 1.99l11.83 6.703-2.25 2.25zm1.414 1.414l3.125-1.77c.883-.5 1.418-1.42 1.418-2.437s-.535-1.937-1.418-2.437l-3.125-1.77-2.353 2.354 2.353 2.354z" />
                </svg>
                <span>View on Google Play ↗</span>
              </a>
            </div>
          </div>

          {/* Secondary: Security & Data Safety Contract */}
          <div className="p-8 bg-[#181816] text-[#FAF9F6] border border-[#2C2B28] rounded-[4px] space-y-6 shadow-xs flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="px-2.5 py-1 rounded-[2px] font-mono text-[11px] bg-[#2C2B28] text-accent border border-[#3E3D38] font-semibold">
                  PRIVACY ARCHITECTURE
                </span>
                <span className="font-mono text-xs text-[#8C8B85]">100% On-Device</span>
              </div>

              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-[4px] bg-[#111110] p-2 border border-[#2C2B28] flex items-center justify-center">
                  <Shield className="w-6 h-6 text-accent" />
                </div>
                <div>
                  <h2 className="font-display font-bold text-xl text-white">Google Play Data Safety</h2>
                  <p className="text-xs text-[#8C8B85] font-mono mt-0.5">Zero Data Collected Declaration</p>
                </div>
              </div>

              <p className="text-sm text-[#8C8B85] font-sans leading-relaxed">
                ApnaSandesh operates without remote servers or cloud accounts. All contact databases, message templates, and campaign records remain strictly in your device sandbox.
              </p>

              <div className="p-3 bg-[#111110] border border-[#2C2B28] rounded-[3px] font-mono text-[11px] text-[#B8B7B0] space-y-1">
                <div className="text-[10px] text-[#8C8B85] uppercase">Package Identifier:</div>
                <div className="text-accent">{SITE_CONFIG.packageId}</div>
              </div>

              <div className="space-y-2 text-xs font-mono text-[#8C8B85] border-t border-[#262624] pt-4">
                <div>Minimum OS: <span className="text-[#B8B7B0]">{SITE_CONFIG.minSdk}</span></div>
                <div>Target OS: <span className="text-[#B8B7B0]">{SITE_CONFIG.targetSdk}</span></div>
                <div>Network Requests: <span className="text-forest font-bold">0 KB (Zero Sockets)</span></div>
              </div>
            </div>

            <div className="pt-6">
              <a
                href={SITE_CONFIG.playStoreUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full inline-flex items-center justify-center gap-2 py-4 px-6 text-xs font-mono font-medium tracking-wide text-white bg-[#2C2B28] hover:bg-[#3E3D38] rounded-[3px] border border-[#3E3D38] uppercase shadow-xs transition-colors"
              >
                <span>Open Google Play Store</span>
              </a>
            </div>
          </div>
        </div>

        {/* Transparent Permissions Breakdown */}
        <div className="bg-white p-8 sm:p-10 border border-[#E6E4DD] rounded-[4px] space-y-6 shadow-xs">
          <div className="space-y-2">
            <div className="font-mono text-xs uppercase tracking-wider text-accent font-semibold">
              Zero Surprise Permissions
            </div>
            <h2 className="font-serif text-2xl sm:text-3xl text-ink font-normal tracking-tight">
              Android permission manifest & strict audit
            </h2>
            <p className="text-sm text-ink-secondary font-sans leading-relaxed max-w-3xl">
              We believe in total transparency. ApnaSandesh requests only the bare minimum operating system capabilities required to perform cellular SMS tasks.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 font-mono text-xs">
            <div className="p-4 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] space-y-1">
              <div className="text-ink font-bold">android.permission.SEND_SMS</div>
              <p className="text-ink-secondary text-[11px] font-sans leading-normal">
                Required only if you enable Direct SIM mode to allow the app to dispatch queued messages sequentially in the background. In Composer Mode, this permission is not requested at all.
              </p>
            </div>

            <div className="p-4 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] space-y-1">
              <div className="text-ink font-bold">android.permission.READ_PHONE_STATE</div>
              <p className="text-ink-secondary text-[11px] font-sans leading-normal">
                Used strictly to detect dual-SIM slot availability (SIM 1 vs SIM 2) and carrier network operator names (Jio, Airtel, Vi) so you can choose which SIM to use.
              </p>
            </div>

            <div className="p-4 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] space-y-1">
              <div className="text-ink font-bold">android.permission.FOREGROUND_SERVICE</div>
              <p className="text-ink-secondary text-[11px] font-sans leading-normal">
                Displays a persistent notification during active campaign execution so Android’s battery manager does not terminate your queue while your screen is off.
              </p>
            </div>

            <div className="p-4 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] space-y-1">
              <div className="text-forest font-bold">INTERNET PERMISSION: NONE</div>
              <p className="text-ink-secondary text-[11px] font-sans leading-normal">
                ApnaSandesh does not require or declare internet network access for core operations. Your contact phone numbers and message logs cannot physically leave the device.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
