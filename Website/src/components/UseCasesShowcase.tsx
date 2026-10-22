'use client';

import React, { useState } from 'react';

interface UseCase {
  id: string;
  category: string;
  title: string;
  narrative: string;
  roi: string;
  image: string;
  sampleSms: string;
  stats: { label: string; value: string }[];
}

const USE_CASES: UseCase[] = [
  {
    id: 'kirana',
    category: 'GROCERY & RETAIL',
    title: 'Kirana & General Stores: Monthly Grocery Billing & Restock Alerts',
    narrative:
      'Instead of calling 60 customers individually when fresh oil, grains, or festival hampers arrive, store owners dispatch personalized SMS in 3 minutes directly over their store smartphone.',
    roi: 'Saves ~₹18,000/yr vs cloud SMS portals while recovering credit khata dues 4x faster.',
    image: '/images/use-cases/kirana-retail-store.jpg',
    sampleSms:
      'Namaste {name} ji, your monthly grocery order #{order_id} at Gupta Provisions is packed and ready. Total: Rs {amount}. Home delivery available today.',
    stats: [
      { label: 'Daily Volume', value: '45-80 SMS' },
      { label: 'Cloud Cost', value: '₹0 (Jio/Airtel)' },
      { label: 'Khata Recovery', value: '+35% speed' },
    ],
  },
  {
    id: 'clinic',
    category: 'HEALTHCARE & DIAGNOSTICS',
    title: 'Clinics & Doctors: Patient Follow-Ups & Diagnostic Lab Reports',
    narrative:
      'Small neighborhood medical practices and pathology labs send automated appointment time reminders and test readiness alerts without sharing sensitive patient health lists with online SaaS platforms.',
    roi: 'Eliminates 70% of patient no-shows with absolute patient record confidentiality.',
    image: '/images/use-cases/clinic-doctor-visit.jpg',
    sampleSms:
      'Dear {name}, your lab investigation report for Dr. Mehta Consultation is ready. You may collect it between 4 PM to 8 PM. - Apex Diagnostic',
    stats: [
      { label: 'No-Show Drop', value: '-68%' },
      { label: 'Patient Privacy', value: '100% On-Device' },
      { label: 'Staff Time Saved', value: '2.5 hrs/day' },
    ],
  },
  {
    id: 'repair',
    category: 'ELECTRONICS & WORKSHOPS',
    title: 'Mobile Repair & Service Centers: Device Readiness Notifications',
    narrative:
      'Service shops notify customers instantly when their smartphone screen, laptop motherboard, or appliance has been diagnosed or repaired with exact estimate approvals.',
    roi: 'Zero uncollected customer phones sitting in stock; instantaneous pickup turnarounds.',
    image: '/images/use-cases/mobile-repair-shop.jpg',
    sampleSms:
      'Hello {name}, your phone repair (Job #{job_no}) is complete and thoroughly tested. Estimated pickup balance: Rs {balance}. FastFix Sector 18.',
    stats: [
      { label: 'Pickup Turnaround', value: '< 24 Hours' },
      { label: 'Daily Volume', value: '25-50 SMS' },
      { label: 'Annual Savings', value: '₹14,400' },
    ],
  },
  {
    id: 'coaching',
    category: 'EDUCATION & TUITION',
    title: 'Coaching Academies: Student Attendance & Parent Test Alerts',
    narrative:
      'Teachers and institute directors send daily absence notices and weekly test score summaries to parents directly after class without paying per-student subscription fees.',
    roi: 'Replaces costly custom school management apps with 100% reliable SMS that parents actually read.',
    image: '/images/use-cases/coaching-classroom.jpg',
    sampleSms:
      'Respected Parent, {student_name} was present for Physics batch today and scored {score}/50 in the thermodynamics weekly test. - BrightFuture Academy',
    stats: [
      { label: 'Open Rate', value: '98% on SMS' },
      { label: 'Parent Engagement', value: '+85%' },
      { label: 'Per-Student Fee', value: '₹0 / Month' },
    ],
  },
  {
    id: 'salon',
    category: 'BEAUTY & WELLNESS',
    title: 'Salons & Spas: Service Confirmations & Birthday Greetings',
    narrative:
      'Boutique unisex salons and aesthetic studios send appointment slots and personalized celebratory discount vouchers to loyal clients on their special occasions.',
    roi: 'Boosts re-booking rates by 42% on quiet weekday slots.',
    image: '/images/use-cases/salon-spa.jpg',
    sampleSms:
      'Dear {name}, Happy Birthday from Glow Unisex Studio! Enjoy an exclusive 25% privilege voucher valid till {expiry}. Call 98112-XXXX to book.',
    stats: [
      { label: 'Repeat Visits', value: '+42%' },
      { label: 'Weekday Fills', value: '+30%' },
      { label: 'Software Cost', value: '₹0' },
    ],
  },
];

export function UseCasesShowcase() {
  const [activeTab, setActiveTab] = useState<string>(USE_CASES[0].id);
  const current = USE_CASES.find((u) => u.id === activeTab) || USE_CASES[0];

  return (
    <section id="use-cases" className="py-24 bg-white border-b border-[#E6E4DD]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="max-w-3xl mb-12 space-y-4">
          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <span className="w-2 h-2 rounded-[1px] bg-accent" />
            <span>03 / Field Applications & Proof</span>
          </div>
          <h2 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-ink font-normal tracking-tight leading-[1.1]">
            Real businesses. Real SIM cards. Tangible savings.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            See how neighborhood retailers, healthcare practitioners, and independent service providers deploy ApnaSandesh to automate daily operations.
          </p>
        </div>

        {/* Tab Selection Navigation */}
        <div className="flex flex-wrap gap-2 border-b border-[#E6E4DD] pb-4 mb-8 font-mono text-xs">
          {USE_CASES.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => setActiveTab(item.id)}
              className={`px-4 py-2.5 rounded-[3px] border transition-colors ${
                activeTab === item.id
                  ? 'border-ink bg-ink text-canvas font-semibold'
                  : 'border-[#E6E4DD] bg-[#FAF9F6] text-ink hover:bg-[#F3F2ED]'
              }`}
            >
              {item.category}
            </button>
          ))}
        </div>

        {/* Active Tab Card */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center p-6 sm:p-10 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[4px]">
          {/* Visual Showcase (6 cols) */}
          <div className="lg:col-span-6 relative rounded-[3px] overflow-hidden border border-[#E6E4DD] aspect-[16/10] bg-white group shadow-xs">
            <img
              src={current.image}
              alt={current.title}
              className="w-full h-full object-cover group-hover:scale-102 transition-transform duration-500"
            />
            <div className="absolute top-3 left-3 bg-studio-black/85 backdrop-blur-xs text-[#FAF9F6] px-2.5 py-1 rounded-[2px] font-mono text-[11px] border border-white/10">
              {current.category}
            </div>
          </div>

          {/* Details (6 cols) */}
          <div className="lg:col-span-6 space-y-5">
            <div className="font-mono text-xs text-accent uppercase tracking-wider font-semibold">
              FIELD BLUEPRINT
            </div>

            <h3 className="font-serif text-2xl sm:text-3xl text-ink font-normal tracking-tight">
              {current.title}
            </h3>

            <p className="text-sm text-ink-secondary leading-relaxed font-sans">
              {current.narrative}
            </p>

            <div className="p-3 bg-white border border-[#E6E4DD] rounded-[3px] text-xs font-mono text-ink-secondary space-y-1">
              <div className="text-[10px] text-[#8C8B85] uppercase tracking-wider">
                Production SMS Template:
              </div>
              <div className="text-ink text-xs font-sans italic bg-[#F8F7F3] p-2 rounded-[2px] border border-[#EFEFED]">
                "{current.sampleSms}"
              </div>
            </div>

            <div className="p-3 bg-[#EDF5F1] border border-[#D5E2DA] rounded-[3px] font-mono text-xs text-forest">
              <strong>Business ROI:</strong> {current.roi}
            </div>

            {/* Metrics */}
            <div className="grid grid-cols-3 gap-2 pt-2 border-t border-[#E6E4DD] font-mono text-xs">
              {current.stats.map((s) => (
                <div key={s.label} className="p-2 bg-white border border-[#E6E4DD] rounded-[2px]">
                  <div className="text-[10px] text-[#8C8B85] uppercase truncate">{s.label}</div>
                  <div className="text-xs font-bold text-ink mt-0.5">{s.value}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
