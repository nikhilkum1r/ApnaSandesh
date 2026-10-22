import React from 'react';
import type { Metadata } from 'next';
import SmsCalculator from '@/components/SmsCalculator';
import Link from 'next/link';
import { ArrowLeft, Cpu, ShieldCheck } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Live GSM-7 / UCS-2 Segment Calculator & Carrier Cost Forecaster',
  description:
    'Inspect character counts, detect unicode penalties, calculate SMS segment splits, and forecast annual savings with ApnaSandesh offline SIM messaging.',
};

export default function CalculatorPage() {
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
            <span>Interactive Telephony Utility</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            GSM-7 vs UCS-2 Encoding & Segment Forecaster
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Test any message template against real cellular carrier encoding limits. Understand how single emojis or Hindi characters cut your message capacity from 160 to 70 characters.
          </p>
        </div>

        {/* Interactive Tool */}
        <div className="bg-white p-6 sm:p-10 border border-[#E6E4DD] rounded-[4px] shadow-xs">
          <SmsCalculator />
        </div>

        {/* Educational Reference Strip */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 pt-8 border-t border-[#E6E4DD]">
          <div className="p-6 bg-white border border-[#E6E4DD] rounded-[4px] space-y-3 shadow-xs">
            <div className="font-mono text-xs text-forest font-semibold flex items-center gap-2">
              <Cpu className="w-4 h-4" />
              <span>GSM 7-BIT ENCODING STANDARD</span>
            </div>
            <h3 className="font-display font-bold text-base text-ink">
              160 Characters per Segment (153 if Multi-part)
            </h3>
            <p className="text-xs text-ink-secondary font-sans leading-relaxed">
              Standard Latin characters, numbers, and basic punctuation are packed into 7 bits per character. A single message payload holds 140 octets (1,120 bits), allowing exactly 160 characters. When concatenated into multiple segments, 6 bytes are reserved for the User Data Header (UDH), reducing per-segment capacity to 153.
            </p>
          </div>

          <div className="p-6 bg-white border border-[#E6E4DD] rounded-[4px] space-y-3 shadow-xs">
            <div className="font-mono text-xs text-accent font-semibold flex items-center gap-2">
              <ShieldCheck className="w-4 h-4" />
              <span>UCS-2 16-BIT UNICODE PENALTY</span>
            </div>
            <h3 className="font-display font-bold text-base text-ink">
              70 Characters per Segment (67 if Multi-part)
            </h3>
            <p className="text-xs text-ink-secondary font-sans leading-relaxed">
              Inserting a single non-GSM character (such as Devanagari script, accented letters, or emojis) forces the mobile handset and SMSC switch into UCS-2 encoding. Each character now consumes 16 bits, instantly slashing the per-message limit from 160 to 70 characters.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
