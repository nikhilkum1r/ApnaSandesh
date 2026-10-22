import React from 'react';

const COMPARISON_ROWS = [
  {
    metric: 'Monthly platform fee',
    gateways: '$20 – $100 / month base',
    portals: '₹1,000 – ₹3,500 / month',
    apnasandesh: '₹0 / month (Zero subscription)',
    highlight: true,
  },
  {
    metric: 'Per-message outbound cost',
    gateways: '₹0.25 – ₹0.45 per SMS + GST',
    portals: '₹0.20 – ₹0.35 per SMS credits',
    apnasandesh: '₹0.00 (Included in cellular SIM recharge)',
    highlight: true,
  },
  {
    metric: 'Customer data privacy',
    gateways: 'Stored on US/EU multi-tenant servers',
    portals: 'Stored on central database servers',
    apnasandesh: '100% on-device Room SQLite (0 KB sent to cloud)',
    highlight: true,
  },
  {
    metric: 'Internet connection dependency',
    gateways: 'Mandatory high-uptime cloud access',
    portals: 'Mandatory web browser access',
    apnasandesh: 'Offline-first: Works with Wi-Fi & data turned off',
    highlight: false,
  },
  {
    metric: 'Mandatory DLT registration',
    gateways: 'Required (₹5,900 DLT registration fee)',
    portals: 'Required or shared generic header',
    apnasandesh: 'Direct SIM dispatch via subscriber allowance',
    highlight: false,
  },
  {
    metric: 'Customer reply routing',
    gateways: 'Lost or requires 2-way webhook setup ($$$)',
    portals: 'Often lost or dumped to email report',
    apnasandesh: 'Direct to your native Android SMS inbox',
    highlight: false,
  },
  {
    metric: 'Average annual cost (50 SMS/day)',
    gateways: '₹22,000 – ₹34,000 / year',
    portals: '₹14,000 – ₹20,000 / year',
    apnasandesh: '₹0 extra (Uses your existing unlimited SIM plan)',
    highlight: true,
  },
];

export function CostComparisonTable() {
  return (
    <section id="comparison" className="py-24 bg-white border-b border-[#E6E4DD]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="max-w-3xl mb-12 space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>04 / Economics & Infrastructure</span>
          </div>
          <h2 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-ink font-normal tracking-tight leading-[1.1]">
            Compare economics, data custody, and trade-offs
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Cloud SMS platforms charge per-message tolls, setup fees, and recurring monthly subscriptions. Here is how ApnaSandesh compares against industry alternatives.
          </p>
        </div>

        {/* Table Container */}
        <div className="w-full overflow-x-auto border border-[#E6E4DD] rounded-[4px] bg-white shadow-xs">
          <table className="w-full text-left border-collapse text-xs sm:text-sm">
            <thead>
              <tr className="bg-[#FAF9F6] border-b border-[#E6E4DD] font-mono text-xs text-ink">
                <th className="p-4 sm:p-5 font-semibold uppercase tracking-wider text-ink-secondary w-1/4">
                  Capability & Metric
                </th>
                <th className="p-4 sm:p-5 font-semibold text-ink-secondary w-1/4 border-l border-[#E6E4DD]">
                  Cloud Gateways <br />
                  <span className="text-[11px] font-normal text-[#8C8B85]">(Twilio, Exotel, Gupshup)</span>
                </th>
                <th className="p-4 sm:p-5 font-semibold text-ink-secondary w-1/4 border-l border-[#E6E4DD]">
                  Web SaaS Portals <br />
                  <span className="text-[11px] font-normal text-[#8C8B85]">(Fast2SMS, Textlocal)</span>
                </th>
                <th className="p-4 sm:p-5 font-bold text-ink w-1/4 border-l border-[#E6E4DD] bg-[#F4F3EF]">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-[1px] bg-accent" />
                    <span>ApnaSandesh Android</span>
                  </div>
                  <span className="text-[11px] font-normal text-accent font-mono">v1.0.4 Native</span>
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E6E4DD] font-sans">
              {COMPARISON_ROWS.map((row, i) => (
                <tr key={i} className="hover:bg-[#FAF9F6] transition-colors">
                  <td className="p-4 sm:p-5 font-mono text-xs font-semibold text-ink">
                    {row.metric}
                  </td>
                  <td className="p-4 sm:p-5 text-ink-secondary border-l border-[#E6E4DD]">
                    {row.gateways}
                  </td>
                  <td className="p-4 sm:p-5 text-ink-secondary border-l border-[#E6E4DD]">
                    {row.portals}
                  </td>
                  <td
                    className={`p-4 sm:p-5 font-mono text-xs border-l border-[#E6E4DD] bg-[#F4F3EF] ${
                      row.highlight ? 'font-bold text-accent' : 'text-ink font-semibold'
                    }`}
                  >
                    {row.apnasandesh}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
