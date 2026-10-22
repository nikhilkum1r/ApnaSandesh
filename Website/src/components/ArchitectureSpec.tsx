import React from 'react';

export function ArchitectureSpec() {
  return (
    <section id="architecture" className="py-24 bg-[#111110] text-[#FAF9F6] border-b border-[#2C2B28]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="max-w-3xl mb-12 space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-[#8C8B85] flex items-center gap-2">
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>05 / Engineering Blueprint & Security</span>
          </div>
          <h2 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-white font-normal tracking-tight leading-[1.1]">
            Native Android architecture. Zero network sockets.
          </h2>
          <p className="text-base sm:text-lg text-[#8C8B85] font-sans leading-relaxed">
            Written in Kotlin with Jetpack Compose, Dagger Hilt, and Room SQLite. ApnaSandesh operates inside Android’s isolated security sandbox with complete offline autonomy.
          </p>
        </div>

        {/* Architecture Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* ASCII Architecture Flowchart (7 cols) */}
          <div className="lg:col-span-7 bg-[#181816] border border-[#2C2B28] rounded-[4px] p-5 font-mono text-xs text-[#FAF9F6] space-y-4 shadow-xs">
            <div className="flex items-center justify-between pb-3 border-b border-[#2C2B28] text-[#8C8B85]">
              <span>TELEPHONY PIPELINE & PRIVACY SANDBOX</span>
              <span className="text-accent">100% ON-DEVICE</span>
            </div>

            <pre className="overflow-x-auto text-[11px] leading-relaxed text-[#B8B7B0]">
{`┌─────────────────────────────────────────────────────────────────┐
│               ANDROID STORAGE ACCESS FRAMEWORK                  │
│       Contacts Address Book    │     CSV Spreadsheet Import     │
└───────────────────────────────┬─────────────────────────────────┘
                                │ SAF Intent (Sandboxed read)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   APNASANDESH DOMAIN ENGINE                     │
│   • E.164 Normalizer (+91)    │ • Dynamic Template Renderer     │
│   • Duplicate Resolver        │ • GSM-7 / UCS-2 Segment Guard   │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│               LOCAL ROOM DATABASE (SQLITE SANDBOX)              │
│   • customers table           │ • message_templates             │
│   • campaigns table           │ • opt_out_suppression_firewall  │
└───────────────────────────────┬─────────────────────────────────┘
                                │
          ┌─────────────────────┴─────────────────────┐
          │                                           │
          ▼ Mode 1: COMPOSER                          ▼ Mode 2: DIRECT SIM
┌───────────────────────────────┐           ┌───────────────────────────────┐
│   COMPOSER SMS INTENT         │           │   FOREGROUND SERVICE WAKELOCK │
│   Handoff to Google Messages  │           │   Sequential throttle (3s-6s) │
│   Zero Android permissions    │           │   Dual-SIM Slot Selection     │
└───────────────────────────────┘           └───────────────┬───────────────┘
                                                            │
                                                            ▼
                                            ┌───────────────────────────────┐
                                            │   ANDROID TELEPHONY MANAGER   │
                                            │   SmsManager.sendTextMessage  │
                                            └───────────────┬───────────────┘
                                                            │ Cellular Radio
                                                            ▼
                                            ┌───────────────────────────────┐
                                            │   PHYSICAL SIM CARD (JIO/AIR) │
                                            └───────────────────────────────┘`}
            </pre>
          </div>

          {/* Module Specs (5 cols) */}
          <div className="lg:col-span-5 space-y-3 font-mono text-xs">
            <div className="p-4 bg-[#181816] border border-[#2C2B28] rounded-[3px] space-y-1">
              <div className="text-accent text-[11px] font-semibold uppercase">01 / TelephonyManager & SmsManager</div>
              <div className="text-white font-medium">Direct Carrier Radio Access</div>
              <p className="text-[#8C8B85] text-[11px] font-sans leading-normal">
                Dispatches messages through native Android RIL radio interface. Honors carrier limits with configurable 3.5s to 6.0s spacing intervals.
              </p>
            </div>

            <div className="p-4 bg-[#181816] border border-[#2C2B28] rounded-[3px] space-y-1">
              <div className="text-accent text-[11px] font-semibold uppercase">02 / Android Foreground Service</div>
              <div className="text-white font-medium">Background Process Stability</div>
              <p className="text-[#8C8B85] text-[11px] font-sans leading-normal">
                Uses an explicit Android Foreground Service with a sticky status notification so OEM battery killers (MIUI, ColorOS, OneUI) do not terminate dispatches mid-campaign.
              </p>
            </div>

            <div className="p-4 bg-[#181816] border border-[#2C2B28] rounded-[3px] space-y-1">
              <div className="text-accent text-[11px] font-semibold uppercase">03 / Room 2.6 Persistence</div>
              <div className="text-white font-medium">Isolated SQLite Storage</div>
              <p className="text-[#8C8B85] text-[11px] font-sans leading-normal">
                Every contact, campaign, and template is saved locally in private app storage. Zero cloud synchronizers. Complete data custody remains in your pocket.
              </p>
            </div>

            <div className="p-4 bg-[#181816] border border-[#2C2B28] rounded-[3px] space-y-1">
              <div className="text-accent text-[11px] font-semibold uppercase">04 / Opt-Out Firewall</div>
              <div className="text-white font-medium">Autonomous Unsubscribe Guard</div>
              <p className="text-[#8C8B85] text-[11px] font-sans leading-normal">
                Recipients who reply with STOP, UNSUBSCRIBE, or CANCEL are automatically added to the local suppression table and excluded from future dispatches.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
