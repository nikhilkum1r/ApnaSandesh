import React from 'react';

const RULES = [
  {
    tag: '01 / CARRIER QUOTAS',
    sub: 'TRAI INDIA',
    title: 'The 100 SMS/day per SIM guideline',
    body1:
      'In India, the Telecom Regulatory Authority of India (TRAI) guidelines stipulate a limit of 100 SMS per day per SIM card for individual subscriber plans to curb unsolicited commercial calling.',
    body2:
      'For small clinics, repair shops, and boutique retailers sending 30 to 80 targeted reminders daily, ApnaSandesh operates comfortably within standard SIM quotas.',
  },
  {
    tag: '02 / TIME WINDOWS',
    sub: '9 AM – 9 PM',
    title: 'Strict daylight sending hours',
    body1:
      'Never dispatch business notifications or reminders outside of standard business hours (9:00 AM to 9:00 PM local time). ApnaSandesh includes built-in campaign scheduling to defer campaigns until appropriate daylight hours.',
    body2:
      'Sending messages late in the evening or early morning increases customer complaints and risks carrier spam flagging.',
  },
  {
    tag: '03 / CONSENT & SUPPRESSION',
    sub: 'MANDATORY',
    title: 'Explicit consent & opt-out compliance',
    body1:
      'Only message customers who have an established business relationship with you (in-store purchases, clinic visits, service bookings). Always append opt-out instructions like "Reply STOP to cancel" in promotional messages.',
    body2:
      'ApnaSandesh provides an automated opt-out blacklist: if a customer replies STOP, they are permanently suppressed from future dispatches.',
  },
];

export function TelecomComplianceSection() {
  return (
    <section id="telecom-rules" className="py-24 bg-[#FAF9F6] border-b border-[#E6E4DD]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="max-w-3xl mb-12 space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>06 / Regulatory Handbook & TRAI Rules</span>
          </div>
          <h2 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-ink font-normal tracking-tight leading-[1.1]">
            Practical rules for sending business SMS responsibly
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Sending messages through your cellular SIM requires compliance with national telecom regulations and carrier terms. Here is what every business owner needs to know.
          </p>
        </div>

        {/* Guidelines Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {RULES.map((r, i) => (
            <div
              key={i}
              className="p-6 bg-white border border-[#E6E4DD] rounded-[4px] space-y-3 shadow-xs"
            >
              <div className="font-mono text-xs text-accent font-semibold flex items-center justify-between">
                <span>{r.tag}</span>
                <span className="text-ink-secondary text-[11px]">{r.sub}</span>
              </div>
              <h3 className="font-display font-bold text-base text-ink">
                {r.title}
              </h3>
              <p className="text-xs text-ink-secondary font-sans leading-relaxed">
                {r.body1}
              </p>
              <p className="text-xs text-ink-secondary font-sans leading-relaxed">
                {r.body2}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
