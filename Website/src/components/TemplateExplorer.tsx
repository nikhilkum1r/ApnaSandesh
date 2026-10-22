'use client';

import React, { useState, useMemo } from 'react';
import { BUSINESS_TEMPLATES, calculateSmsMetrics, type SmsTemplate } from '../data/templates';

const PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.apnasandesh.app";

export default function TemplateExplorer() {
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const categories = [
    { id: 'all', label: 'All templates' },
    { id: 'order', label: 'Orders' },
    { id: 'appointment', label: 'Appointments' },
    { id: 'payment', label: 'Billing' },
    { id: 'service', label: 'Repairs & service' },
    { id: 'delivery', label: 'Deliveries' },
    { id: 'gratitude', label: 'Thank-you' },
    { id: 'announcement', label: 'Notices' },
    { id: 'promotion', label: 'Promotions' },
    { id: 'festival', label: 'Festivals' },
  ];

  const filteredTemplates = useMemo(() => {
    return BUSINESS_TEMPLATES.filter((t) => {
      const matchesCategory = selectedCategory === 'all' || t.category === selectedCategory;
      const matchesSearch =
        t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        t.text.toLowerCase().includes(searchQuery.toLowerCase()) ||
        t.description.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }, [selectedCategory, searchQuery]);

  const copyTemplate = (template: SmsTemplate) => {
    navigator.clipboard.writeText(template.text);
    setCopiedId(template.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="w-full space-y-6">
      {/* Search & Category Filter Controls */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 p-4 bg-[#FFFFFF] border border-[#E6E4DD] rounded-[4px]">
        <div className="relative flex-1">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search templates (e.g. appointment, invoice, diwali, pickup)..."
            className="w-full px-3 py-2 text-xs font-mono bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px] text-ink placeholder:text-[#8C8B85] focus:outline-none focus:border-[#141413]"
          />
        </div>

        <div className="flex items-center gap-2 font-mono text-xs text-[#6B6A65]">
          <span>Count:</span>
          <span className="font-bold text-[#141413]">{filteredTemplates.length} templates</span>
        </div>
      </div>

      {/* Category Pills */}
      <div className="flex flex-wrap items-center gap-1.5">
        {categories.map((c) => (
          <button
            key={c.id}
            type="button"
            onClick={() => setSelectedCategory(c.id)}
            className={`px-3 py-1.5 text-xs font-mono transition-colors rounded-[2px] border ${
              selectedCategory === c.id
                ? 'bg-[#141413] text-[#FAF9F6] border-[#141413]'
                : 'bg-[#FFFFFF] text-[#6B6A65] border-[#E6E4DD] hover:text-[#141413] hover:bg-[#FAF9F6]'
            }`}
          >
            {c.label}
          </button>
        ))}
      </div>

      {/* Template Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredTemplates.map((template) => {
          const metrics = calculateSmsMetrics(template.text);
          const isCopied = copiedId === template.id;

          return (
            <div
              key={template.id}
              className="p-5 bg-[#FFFFFF] border border-[#E6E4DD] rounded-[4px] flex flex-col justify-between space-y-4 hover:border-[#141413] transition-colors"
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-mono text-[10px] uppercase tracking-wider text-[#8C8B85] bg-[#FAF9F6] px-2 py-0.5 border border-[#E6E4DD] rounded-[2px]">
                    {template.category}
                  </span>
                  <span className="font-mono text-[11px] text-[#6B6A65]">
                    {metrics.charCount} chars • {metrics.segments} SMS {metrics.segments === 1 ? 'segment' : 'segments'}
                  </span>
                </div>

                <h3 className="font-sans font-semibold text-base text-[#141413]">
                  {template.title}
                </h3>
                <p className="text-xs text-[#6B6A65] font-sans leading-normal">
                  {template.description}
                </p>

                {/* Preformatted Template Box */}
                <div className="p-3 bg-[#FAF9F6] border border-[#E6E4DD] rounded-[2px] font-mono text-xs text-[#141413] whitespace-pre-line leading-relaxed">
                  {template.text}
                </div>
              </div>

              {/* Bottom bar with action */}
              <div className="pt-2 border-t border-[#E6E4DD] flex items-center justify-between font-mono text-xs">
                <span className={`text-[11px] ${metrics.encoding === 'GSM-7' ? 'text-[#1B4332]' : 'text-[#FF4F00]'}`}>
                  {metrics.encoding === 'GSM-7' ? 'GSM 7-bit' : 'UCS-2 Unicode'}
                </span>

                <button
                  type="button"
                  onClick={() => copyTemplate(template)}
                  className={`px-3 py-1.5 rounded-[2px] border text-xs font-mono uppercase tracking-wider transition-colors ${
                    isCopied
                      ? 'bg-[#1B4332] text-white border-[#1B4332]'
                      : 'bg-[#FAF9F6] text-[#141413] border-[#E6E4DD] hover:bg-[#141413] hover:text-white'
                  }`}
                >
                  {isCopied ? 'Copied' : 'Copy template'}
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Bottom CTA to Google Play */}
      <div className="p-6 bg-[#111110] text-[#FAF9F6] rounded-[4px] border border-[#2C2B28] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 font-mono text-xs">
        <div className="space-y-1">
          <div className="font-bold text-white text-sm">All 30+ templates are pre-installed in ApnaSandesh</div>
          <p className="text-[#8C8B85] font-sans text-xs">
            Personalize variables with one tap directly inside the Android app.
          </p>
        </div>
        <a
          href={PLAY_STORE_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="px-4 py-2 bg-[#FF4F00] hover:bg-[#E04500] text-white rounded-[2px] uppercase tracking-wider font-semibold whitespace-nowrap"
        >
          Get on Google Play ↗
        </a>
      </div>
    </div>
  );
}
