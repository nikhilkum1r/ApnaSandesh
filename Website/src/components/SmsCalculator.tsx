'use client';

import React, { useState, useMemo } from 'react';

// Basic GSM 7-bit character set check
const GSM_7_REGEX = /^[@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ\x1bÆæßÉ !"#¤%&'()*+,\-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~\]|€]*$/;

const PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.apnasandesh.app";

export default function SmsCalculator() {
  const [text, setText] = useState<string>(
    'Namaste Priya, your dental cleaning session is scheduled for Friday at 4 PM with Dr. Kulkarni. Please reply 1 to confirm or STOP to opt out.'
  );
  const [recipientCount, setRecipientCount] = useState<number>(1000);
  const [carrierRate, setCarrierRate] = useState<number>(0.28); // INR per SMS gateway segment

  // Analysis logic
  const analysis = useMemo(() => {
    const isGsm = GSM_7_REGEX.test(text);
    const length = text.length;
    
    // Find non-gsm characters if any
    const nonGsmChars: string[] = [];
    if (!isGsm) {
      for (const char of text) {
        if (!GSM_7_REGEX.test(char) && !nonGsmChars.includes(char)) {
          nonGsmChars.push(char);
        }
      }
    }

    let segmentLimit = isGsm ? 160 : 70;
    let multiLimit = isGsm ? 153 : 67;
    let segments = 1;

    if (length === 0) {
      segments = 0;
    } else if (length <= segmentLimit) {
      segments = 1;
    } else {
      segments = Math.ceil(length / multiLimit);
    }

    const totalGatewaySegments = segments * recipientCount;
    const monthlyGatewayCost = (totalGatewaySegments * carrierRate) + 1200; // 1200 INR base platform charge
    const annualGatewayCost = monthlyGatewayCost * 12;

    const monthlyApnaSandeshCost = 0; // Marginal cost on standard SIM
    const annualSavings = annualGatewayCost;

    return {
      isGsm,
      length,
      segments,
      nonGsmChars,
      segmentLimit,
      multiLimit,
      totalGatewaySegments,
      monthlyGatewayCost,
      annualGatewayCost,
      annualSavings,
    };
  }, [text, recipientCount, carrierRate]);

  return (
    <div className="w-full bg-[#FFFFFF] border border-[#E6E4DD] rounded-[4px] overflow-hidden">
      {/* Header bar */}
      <div className="bg-[#FAF9F6] px-5 py-4 border-b border-[#E6E4DD] flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="font-mono text-xs uppercase tracking-wider text-[#6B6A65] font-semibold">
            Telemetry & Telephony Engine
          </div>
          <h3 className="font-display font-bold text-lg text-[#141413] tracking-tight mt-0.5">
            Live GSM-7 and UCS-2 segment calculator
          </h3>
        </div>
        <div className="flex items-center gap-2 font-mono text-xs">
          <span className={`px-2.5 py-1 rounded-[2px] border ${
            analysis.isGsm
              ? 'bg-[#EDF5F1] text-[#1B4332] border-[#C2DDD0]'
              : 'bg-[#FFF1EB] text-[#FF4F00] border-[#FFD5C2]'
          }`}>
            Encoding: {analysis.isGsm ? 'GSM 7-bit (Standard)' : 'UCS-2 (Unicode / 70 chars)'}
          </span>
        </div>
      </div>

      <div className="p-5 sm:p-6 space-y-6">
        {/* Editor input */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs font-mono text-[#6B6A65]">
            <label htmlFor="sms-text-input" className="uppercase tracking-wider">
              Message text composition
            </label>
            <span>
              {analysis.length} characters ({analysis.segments} SMS {analysis.segments === 1 ? 'segment' : 'segments'})
            </span>
          </div>
          <textarea
            id="sms-text-input"
            rows={4}
            value={text}
            onChange={(e) => setText(e.target.value)}
            className="w-full p-3 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[3px] font-sans text-sm text-[#141413] focus:outline-none focus:border-[#141413] resize-y"
            placeholder="Type your message template here..."
          />
        </div>

        {/* Warning if UCS-2 non-gsm characters detected */}
        {!analysis.isGsm && (
          <div className="p-3.5 bg-[#FFF1EB] border border-[#FFD5C2] rounded-[3px] text-xs font-sans text-[#141413] space-y-1">
            <div className="font-semibold text-[#FF4F00] font-mono uppercase tracking-wider text-[11px]">
              Unicode character detected: 70-character limit applied
            </div>
            <p className="text-[#6B6A65]">
              Your text contains characters outside the GSM-7 alphabet. Carriers will encode this as UCS-2, shrinking each SMS segment from 160 characters to 70 characters.
            </p>
            <div className="pt-1 flex items-center gap-1.5 font-mono text-xs">
              <span className="text-[#8C8B85]">Offending characters:</span>
              {analysis.nonGsmChars.map((ch, idx) => (
                <span key={idx} className="px-1.5 py-0.5 bg-white border border-[#FFD5C2] rounded-[2px] font-bold text-[#FF4F00]">
                  '{ch}' (U+{ch.charCodeAt(0).toString(16).toUpperCase().padStart(4, '0')})
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Telephony metrics grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 font-mono text-xs">
          <div className="p-3 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">Total characters</div>
            <div className="text-xl font-bold text-[#141413] mt-1">{analysis.length}</div>
            <div className="text-[10px] text-[#6B6A65] mt-0.5">
              {analysis.isGsm ? '160 chars/single' : '70 chars/single'}
            </div>
          </div>

          <div className="p-3 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">Segments / contact</div>
            <div className="text-xl font-bold text-[#141413] mt-1">{analysis.segments}</div>
            <div className="text-[10px] text-[#6B6A65] mt-0.5">
              {analysis.segments > 1 ? `Multi-part: ${analysis.multiLimit} chars/seg` : 'Single part'}
            </div>
          </div>

          <div className="p-3 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">Cloud gateway cost</div>
            <div className="text-xl font-bold text-[#FF4F00] mt-1">
              ₹{(analysis.segments * carrierRate).toFixed(2)}
            </div>
            <div className="text-[10px] text-[#6B6A65] mt-0.5">Per single recipient</div>
          </div>

          <div className="p-3 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">ApnaSandesh SIM cost</div>
            <div className="text-xl font-bold text-[#1B4332] mt-1">₹0.00</div>
            <div className="text-[10px] text-[#6B6A65] mt-0.5">Uses phone cellular plan</div>
          </div>
        </div>

        {/* Campaign Cost Forecaster */}
        <div className="pt-4 border-t border-[#E6E4DD] space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <div className="font-mono text-xs uppercase tracking-wider text-[#6B6A65]">
                Annual financial difference
              </div>
              <h4 className="font-display font-semibold text-base text-[#141413]">
                Estimated savings across your customer list
              </h4>
            </div>
            {/* Quick volume presets */}
            <div className="flex items-center gap-1.5 font-mono text-xs">
              <span className="text-[#8C8B85] text-[11px] mr-1">Recipients:</span>
              {[250, 500, 1000, 2500].map((count) => (
                <button
                  key={count}
                  type="button"
                  onClick={() => setRecipientCount(count)}
                  className={`px-2 py-1 rounded-[2px] border transition-colors ${
                    recipientCount === count
                      ? 'bg-[#141413] text-white border-[#141413]'
                      : 'bg-[#FAF9F6] text-[#141413] border-[#E6E4DD] hover:bg-[#F0EFEA]'
                  }`}
                >
                  {count}
                </button>
              ))}
            </div>
          </div>

          {/* Savings banner */}
          <div className="p-4 bg-[#111110] text-[#FAF9F6] rounded-[3px] border border-[#2C2B28] flex flex-col md:flex-row md:items-center justify-between gap-4 font-mono">
            <div className="space-y-1">
              <div className="text-xs text-[#8C8B85] uppercase tracking-wider">
                Annual budget saved with on-device ApnaSandesh
              </div>
              <div className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                ₹{Math.round(analysis.annualSavings).toLocaleString('en-IN')} <span className="text-xs text-[#8C8B85] font-normal font-sans">/ year retained</span>
              </div>
              <p className="text-xs text-[#8C8B85] font-sans">
                Based on {recipientCount.toLocaleString()} monthly recipients ({analysis.totalGatewaySegments.toLocaleString()} billable segments) vs Twilio/Exotel gateway markups + platform subscriptions.
              </p>
            </div>

            <div className="text-right flex md:flex-col items-center md:items-end justify-between gap-2 border-t md:border-t-0 pt-2 md:pt-0 border-[#262624]">
              <div className="text-xs text-[#8C8B85]">Zero cloud lock-in</div>
              <a
                href={PLAY_STORE_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 px-4 py-2 bg-[#FF4F00] hover:bg-[#E04500] text-white text-xs font-semibold rounded-[2px] tracking-wider uppercase"
              >
                <span>Get on Google Play ↗</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
