'use client';

import React, { useState } from 'react';

interface TemplateOption {
  id: string;
  label: string;
  recipient: string;
  phone: string;
  message: string;
  variables: Record<string, string>;
}

const TEMPLATES: TemplateOption[] = [
  {
    id: 'appointment',
    label: 'Appointment reminder',
    recipient: 'Dr. Mehta Clinic',
    phone: '+91 98201 44521',
    message: 'Hello {name}, your follow-up appointment with Dr. Mehta is confirmed for tomorrow at {time}. Reply STOP to cancel.',
    variables: { name: 'Sunil Rao', time: '11:30 AM' },
  },
  {
    id: 'order',
    label: 'Order ready for pickup',
    recipient: 'Sharma Optics',
    phone: '+91 97110 88234',
    message: 'Dear {name}, your spectacle lenses for order #{order_id} are ready for pickup at our Sector 14 branch.',
    variables: { name: 'Priya Verma', order_id: '4892' },
  },
  {
    id: 'payment',
    label: 'Payment due notice',
    recipient: 'Apex Diagnostics',
    phone: '+91 94432 10982',
    message: 'Invoice #{invoice_id} for INR {amount} is pending. Kindly clear the balance by {due_date}. Thank you, {company}.',
    variables: { invoice_id: '810', amount: '1,450', due_date: 'Oct 28', company: 'Apex Lab' },
  },
];

export default function HeroSpecimen() {
  const [selectedTemplate, setSelectedTemplate] = useState<TemplateOption>(TEMPLATES[0]);
  const [dispatchMode, setDispatchMode] = useState<'composer' | 'direct'>('composer');
  const [isDispatched, setIsDispatched] = useState<boolean>(false);
  const [auditLog, setAuditLog] = useState<string[]>([]);

  // Calculate personalized text
  const renderedMessage = Object.entries(selectedTemplate.variables).reduce(
    (acc, [key, val]) => acc.replace(new RegExp(`{${key}}`, 'g'), val),
    selectedTemplate.message
  );

  const charLength = renderedMessage.length;
  const segments = Math.ceil(charLength / 160) || 1;

  const handleSimulateDispatch = () => {
    setIsDispatched(true);
    const timestamp = new Date().toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const logEntry = dispatchMode === 'composer'
      ? `[${timestamp}] COMPOSER_HANDOFF -> Pre-filled system SMS intent for ${selectedTemplate.phone}`
      : `[${timestamp}] DIRECT_SIM_DISPATCH -> Sent via SIM 1 (Jio 5G), 4.5s throttle applied, logged to Room DB`;
    
    setAuditLog(prev => [logEntry, ...prev.slice(0, 3)]);
    setTimeout(() => setIsDispatched(false), 2400);
  };

  return (
    <div className="w-full bg-[#FFFFFF] border border-[#E6E4DD] rounded-[4px] shadow-none overflow-hidden">
      {/* Specimen Masthead */}
      <div className="bg-[#FAF9F6] px-4 py-3 border-b border-[#E6E4DD] flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-[1px] bg-[#FF4F00]"></span>
          <span className="font-mono text-xs uppercase tracking-wider text-[#141413] font-semibold">
            Interactive specimen
          </span>
          <span className="font-mono text-xs text-[#6B6A65] hidden sm:inline">/ on-device preview</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="inline-flex items-center px-2 py-0.5 rounded-[2px] text-[11px] font-mono bg-[#EFEFED] text-[#6B6A65] border border-[#E6E4DD]">
            SIM 1: Jio 5G
          </span>
          <span className="inline-flex items-center px-2 py-0.5 rounded-[2px] text-[11px] font-mono bg-[#EFEFED] text-[#1B4332] border border-[#D5E2DA]">
            SQLite active
          </span>
        </div>
      </div>

      <div className="p-4 sm:p-6 space-y-5">
        {/* Step 1: Select business template */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <label className="font-mono text-xs text-[#6B6A65] uppercase tracking-wider">
              01. Select business template
            </label>
            <span className="font-mono text-xs text-[#8C8B85]">30+ pre-installed in v1.0.4</span>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
            {TEMPLATES.map((tmpl) => (
              <button
                key={tmpl.id}
                type="button"
                onClick={() => setSelectedTemplate(tmpl)}
                className={`text-left p-2.5 rounded-[3px] border text-xs font-mono transition-colors ${
                  selectedTemplate.id === tmpl.id
                    ? 'border-[#141413] bg-[#141413] text-[#FAF9F6]'
                    : 'border-[#E6E4DD] bg-[#FAF9F6] text-[#141413] hover:bg-[#F3F2ED]'
                }`}
              >
                <div className="font-sans font-medium text-xs truncate">{tmpl.label}</div>
                <div className="text-[10px] opacity-70 truncate mt-0.5">{tmpl.recipient}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Step 2: Message preview canvas */}
        <div className="space-y-2">
          <div className="flex items-center justify-between font-mono text-xs text-[#6B6A65]">
            <span className="uppercase tracking-wider">02. Live personalized SMS composition</span>
            <span>Recipient: <strong className="text-[#141413]">{selectedTemplate.phone}</strong></span>
          </div>
          
          <div className="p-4 bg-[#F4F3EF] border border-[#E6E4DD] rounded-[3px] font-sans text-sm text-[#141413] leading-relaxed relative">
            <p className="whitespace-pre-line">{renderedMessage}</p>
            
            {/* Live Token Pills */}
            <div className="mt-3 pt-3 border-t border-[#E6E4DD] flex flex-wrap items-center gap-1.5 font-mono text-[11px] text-[#6B6A65]">
              <span className="text-[#8C8B85]">Substituted tokens:</span>
              {Object.entries(selectedTemplate.variables).map(([k, v]) => (
                <span key={k} className="px-1.5 py-0.5 bg-[#FFFFFF] border border-[#E6E4DD] rounded-[2px] text-[#141413]">
                  {`{${k}}`}: <span className="font-medium text-[#FF4F00]">{v}</span>
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* Step 3: Telephony inspection strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 font-mono text-xs">
          <div className="p-2.5 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">Length</div>
            <div className="font-bold text-[#141413] text-sm mt-0.5">{charLength} chars</div>
          </div>
          <div className="p-2.5 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">SMS segment</div>
            <div className="font-bold text-[#141413] text-sm mt-0.5">{segments} of 160</div>
          </div>
          <div className="p-2.5 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">Encoding</div>
            <div className="font-bold text-[#1B4332] text-sm mt-0.5">GSM 7-bit</div>
          </div>
          <div className="p-2.5 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px]">
            <div className="text-[10px] text-[#8C8B85] uppercase">Gateway fee</div>
            <div className="font-bold text-[#FF4F00] text-sm mt-0.5">INR 0.00</div>
          </div>
        </div>

        {/* Step 4: Dispatch mode selection & execution */}
        <div className="space-y-2">
          <div className="font-mono text-xs text-[#6B6A65] uppercase tracking-wider">
            03. Select Android dispatch engine
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => setDispatchMode('composer')}
              className={`p-3 text-left border rounded-[3px] transition-colors ${
                dispatchMode === 'composer'
                  ? 'border-[#141413] bg-[#FAF9F6]'
                  : 'border-[#E6E4DD] bg-[#FFFFFF] hover:bg-[#FAF9F6]'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="font-sans font-semibold text-xs text-[#141413]">Default SMS app</span>
                <span className="font-mono text-[10px] text-[#1B4332] bg-[#EDF5F1] px-1.5 py-0.5 rounded-[2px]">
                  0 permissions
                </span>
              </div>
              <p className="font-sans text-[11px] text-[#6B6A65] mt-1 leading-normal">
                Pre-fills native Google/Samsung Messages app. User reviews and sends in 1 tap.
              </p>
            </button>

            <button
              type="button"
              onClick={() => setDispatchMode('direct')}
              className={`p-3 text-left border rounded-[3px] transition-colors ${
                dispatchMode === 'direct'
                  ? 'border-[#141413] bg-[#FAF9F6]'
                  : 'border-[#E6E4DD] bg-[#FFFFFF] hover:bg-[#FAF9F6]'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="font-sans font-semibold text-xs text-[#141413]">Direct SIM queue</span>
                <span className="font-mono text-[10px] text-[#8C8B85] bg-[#EFEFED] px-1.5 py-0.5 rounded-[2px]">
                  Foreground service
                </span>
              </div>
              <p className="font-sans text-[11px] text-[#6B6A65] mt-1 leading-normal">
                Automated sequential background queue with 3s-8s safety interval to protect SIM quotas.
              </p>
            </button>
          </div>
        </div>

        {/* Trigger Button */}
        <div>
          <button
            type="button"
            onClick={handleSimulateDispatch}
            disabled={isDispatched}
            className={`w-full py-3 px-4 rounded-[3px] font-mono text-xs font-semibold tracking-wider uppercase transition-colors ${
              isDispatched
                ? 'bg-[#1B4332] text-white cursor-default'
                : 'bg-[#FF4F00] hover:bg-[#E04500] text-white'
            }`}
          >
            {isDispatched
              ? 'Dispatch confirmed on local device'
              : `Simulate ${dispatchMode === 'composer' ? 'composer handoff' : 'direct SIM dispatch'}`}
          </button>
        </div>

        {/* Local Room DB Audit Log output */}
        {auditLog.length > 0 && (
          <div className="p-3 bg-[#111110] text-[#FAF9F6] rounded-[2px] font-mono text-[11px] space-y-1">
            <div className="text-[#8C8B85] text-[10px] uppercase tracking-wider pb-1 border-b border-[#2C2B28]">
              Local SQLite audit log (device sandbox)
            </div>
            {auditLog.map((log, i) => (
              <div key={i} className="text-[#B8B7B0] break-all leading-tight">
                {log}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
