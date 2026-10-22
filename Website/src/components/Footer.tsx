'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { SITE_CONFIG } from '@/lib/site-config';
import {
  ArrowUp,
  Check,
  Copy,
  ExternalLink,
  ShieldCheck,
  Smartphone,
  ChevronDown,
  Sparkles,
  Terminal,
  Calculator,
  Layers,
  FileCode2,
  Lock,
  Download,
} from 'lucide-react';

interface LinkItem {
  label: string;
  href: string;
  badge?: string;
  isExternal?: boolean;
}

interface FooterSection {
  title: string;
  id: string;
  links: LinkItem[];
}

export function Footer() {
  const [copied, setCopied] = useState(false);
  // Default all sections to open on larger screens; on mobile, allow toggling
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({
    product: true,
    solutions: true,
    docs: true,
    machine: false,
  });

  const toggleSection = (id: string) => {
    setOpenSections((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  const copyEmail = () => {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      navigator.clipboard.writeText(SITE_CONFIG.parentBrand.supportEmail);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  const scrollToTop = () => {
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const sections: FooterSection[] = [
    {
      title: 'Product & Engine',
      id: 'product',
      links: [
        { label: 'System Overview', href: '/' },
        { label: 'Technical Architecture', href: '/features', badge: 'On-Device' },
        { label: 'GSM-7 Segment Calculator', href: '/calculator', badge: 'Tool' },
        { label: 'SMS Template Library', href: '/templates', badge: '100+ Free' },
        { label: 'Compare vs Cloud Gateways', href: '/compare' },
        { label: 'Download Android APK', href: '/download', badge: 'v1.0.0' },
        { label: 'Changelog & Releases', href: '/changelog' },
      ],
    },
    {
      title: 'Industry Blueprints',
      id: 'solutions',
      links: [
        { label: 'All Use Cases Hub', href: '/use-cases' },
        { label: 'Kirana & Local Retail', href: '/use-cases#retail' },
        { label: 'Clinics & Independent Doctors', href: '/use-cases#healthcare' },
        { label: 'Salons, Spas & Beauty Parlours', href: '/use-cases#beauty' },
        { label: 'Gyms & Personal Fitness', href: '/use-cases#fitness' },
        { label: 'Coaching Classes & Tutors', href: '/use-cases#education' },
        { label: 'Automobile Repair & Garages', href: '/use-cases#automotive' },
      ],
    },
    {
      title: 'Docs & Compliance',
      id: 'docs',
      links: [
        { label: 'Documentation Hub', href: '/docs' },
        { label: 'TRAI Compliance Handbook', href: '/compliance', badge: 'DLT Guide' },
        { label: '100 SMS/Day SIM Limits', href: '/compliance#limits' },
        { label: 'Privacy Policy', href: '/privacy' },
        { label: 'Terms of Service', href: '/terms' },
        { label: 'Engineering Blog & Guides', href: '/blog' },
        { label: 'Offline Room SQLite Engine', href: '/features#architecture' },
      ],
    },
    {
      title: 'Machine Endpoints (AEO)',
      id: 'machine',
      links: [
        { label: '/llms.txt (AI Model Summary)', href: '/llms.txt', badge: 'LLM' },
        { label: '/llms-full.txt (Full Model Specs)', href: '/llms-full.txt' },
        { label: '/ai.txt (Agent Directive)', href: '/ai.txt' },
        { label: '/rss.xml (Standard Feed)', href: '/rss.xml' },
        { label: '/atom.xml (Syndication Feed)', href: '/atom.xml' },
        { label: '/feed.json (JSON Feed)', href: '/feed.json' },
        { label: '/sitemap.xml (Site Index)', href: '/sitemap.xml' },
      ],
    },
  ];

  return (
    <footer className="relative bg-studio-black text-[#FAF9F6] border-t border-[#2C2B28] pt-14 pb-12 font-sans overflow-hidden">
      {/* Background Ambient Glow */}
      <div
        className="pointer-events-none absolute inset-x-0 -top-24 h-64 bg-[radial-gradient(ellipse_60%_50%_at_50%_0%,rgba(255,79,0,0.1),transparent_70%)]"
        aria-hidden="true"
      />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Top Pre-Footer Action Banner */}
        <div className="mb-14 rounded-md border border-[#2C2B28] bg-studio-panel/80 backdrop-blur-md p-6 sm:p-8 lg:p-10 shadow-2xl relative overflow-hidden group">
          <div
            className="pointer-events-none absolute -right-20 -bottom-20 w-80 h-80 bg-accent/5 rounded-full blur-3xl group-hover:bg-accent/10 transition-colors duration-500"
            aria-hidden="true"
          />

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
            {/* Left text */}
            <div className="lg:col-span-7 space-y-3">
              <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-[3px] bg-studio-card border border-[#3E3D38] text-[11px] font-mono text-studio-muted">
                <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse" />
                <span className="text-white font-medium">Direct Carrier SIM Dispatch</span>
                <span className="text-[#3E3D38]">/</span>
                <span className="text-[#10B981]">100% On-Device</span>
              </div>
              <h2 className="font-display font-bold text-2xl sm:text-3xl text-white tracking-tight leading-tight">
                Empower your business with autonomous SMS.
              </h2>
              <p className="text-sm text-studio-muted leading-relaxed max-w-xl">
                Replace expensive monthly SMS gateway bills. Run personalized transactional and reminder broadcasts straight from your Jio, Airtel, or Vi SIM card without third-party cloud leakage.
              </p>
            </div>

            {/* Right CTAs */}
            <div className="lg:col-span-5 flex flex-col sm:flex-row lg:flex-col xl:flex-row items-stretch sm:items-center lg:items-stretch xl:items-center justify-end gap-3 pt-2 lg:pt-0">
              <a
                href={SITE_CONFIG.playStoreUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center justify-center gap-2.5 px-5 py-3 text-xs font-mono font-medium tracking-wide text-white bg-accent hover:bg-accent-hover rounded-[3px] border border-accent shadow-lg transition-all duration-200 active:scale-[0.98]"
              >
                <Download className="w-4 h-4" />
                <span>Get on Google Play</span>
              </a>

              <Link
                href="/calculator"
                className="inline-flex items-center justify-center gap-2 px-4 py-3 text-xs font-mono font-medium text-[#FAF9F6] bg-studio-card hover:bg-[#252523] rounded-[3px] border border-[#3E3D38] hover:border-[#525048] transition-all duration-200"
              >
                <Calculator className="w-4 h-4 text-accent" />
                <span>GSM-7 Calculator</span>
              </Link>
            </div>
          </div>
        </div>

        {/* Main Footer Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-12 gap-10 lg:gap-8 pb-12 border-b border-[#262624]">
          {/* Brand Info Column */}
          <div className="md:col-span-2 lg:col-span-4 space-y-5">
            {/* Logo & Name */}
            <Link href="/" className="inline-flex items-center gap-3 group">
              <div className="w-9 h-9 rounded-[4px] bg-white p-1 border border-[#3E3D38] flex items-center justify-center shadow-sm group-hover:border-accent transition-colors">
                <img
                  src="/logo.svg"
                  alt="ApnaSandesh logo"
                  className="w-full h-full object-contain"
                  width={36}
                  height={36}
                />
              </div>
              <div className="flex flex-col">
                <span className="font-display font-bold text-xl text-white tracking-tight group-hover:text-accent transition-colors">
                  ApnaSandesh
                </span>
                <span className="font-mono text-[10px] text-studio-muted uppercase tracking-wider">
                  SIM SMS Workstation
                </span>
              </div>
            </Link>

            {/* Description */}
            <p className="text-xs text-studio-muted leading-relaxed font-sans max-w-sm">
              Autonomous on-device Android business SMS engine for independent shopkeepers, clinics, tutors, and merchants. Carrier SIM dispatch, Room SQLite persistence, and zero monthly subscriptions.
            </p>

            {/* Live Operational Beacon Pill */}
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-[4px] bg-studio-card border border-[#2E2E2B] text-[11px] font-mono text-studio-muted">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#10B981] opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-[#10B981]" />
              </span>
              <span className="text-white font-medium">Engine Status:</span>
              <span className="text-[#10B981]">Autonomous & Operational</span>
            </div>

            {/* Studio Meta & Direct Copy Support */}
            <div className="space-y-2 pt-1 font-mono text-xs text-studio-muted">
              <div className="flex items-center gap-1.5">
                <span className="text-studio-faint">Studio:</span>
                <a
                  href={SITE_CONFIG.parentBrand.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-white hover:text-accent transition-colors inline-flex items-center gap-1 group"
                >
                  <span>{SITE_CONFIG.parentBrand.name}</span>
                  <ExternalLink className="w-3 h-3 text-studio-muted group-hover:text-accent transition-colors" />
                </a>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-studio-faint">Support:</span>
                <a
                  href={`mailto:${SITE_CONFIG.parentBrand.supportEmail}`}
                  className="text-white hover:text-accent transition-colors"
                >
                  {SITE_CONFIG.parentBrand.supportEmail}
                </a>
                <button
                  type="button"
                  onClick={copyEmail}
                  className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-[2px] bg-studio-card hover:bg-[#2C2B28] text-[10px] text-studio-muted hover:text-white border border-[#3E3D38] transition-colors"
                  title="Copy support email address"
                  aria-label="Copy support email"
                >
                  {copied ? (
                    <>
                      <Check className="w-2.5 h-2.5 text-[#10B981]" />
                      <span className="text-[#10B981]">Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-2.5 h-2.5" />
                      <span>Copy</span>
                    </>
                  )}
                </button>
              </div>

              <div className="flex items-center gap-2 text-[11px] text-studio-muted">
                <span className="text-studio-faint">Specs:</span>
                <span className="text-white">{SITE_CONFIG.minSdk}</span>
                <span>•</span>
                <span className="text-white">Room SQLite</span>
              </div>
            </div>
          </div>

          {/* Navigation Columns (Responsive Accordion on mobile, grid on md/lg) */}
          {sections.map((section) => (
            <div
              key={section.id}
              className="lg:col-span-2 border-t md:border-t-0 border-[#262624] pt-4 md:pt-0"
            >
              {/* Column Header (Clickable toggle on mobile, static on md+) */}
              <button
                type="button"
                onClick={() => toggleSection(section.id)}
                className="w-full flex items-center justify-between text-left md:pointer-events-none pb-2 group"
                aria-expanded={openSections[section.id]}
              >
                <div className="text-accent uppercase tracking-wider font-mono font-semibold text-[11px] flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-[1px] bg-accent" />
                  <span>{section.title}</span>
                </div>
                {/* Mobile Chevron */}
                <div className="md:hidden text-studio-muted group-hover:text-white transition-colors">
                  <ChevronDown
                    className={`w-4 h-4 transition-transform duration-200 ${
                      openSections[section.id] ? 'rotate-180 text-white' : ''
                    }`}
                  />
                </div>
              </button>

              {/* Links List */}
              <div
                className={`transition-all duration-200 overflow-hidden ${
                  openSections[section.id]
                    ? 'max-h-96 opacity-100 mt-2.5'
                    : 'max-h-0 opacity-0 md:max-h-96 md:opacity-100 md:mt-2.5'
                }`}
              >
                <ul className="space-y-2 font-mono text-xs text-studio-muted">
                  {section.links.map((link) => (
                    <li key={link.href}>
                      <Link
                        href={link.href}
                        className="group flex items-center justify-between py-0.5 hover:text-white transition-colors"
                      >
                        <span className="group-hover:translate-x-0.5 transition-transform duration-150">
                          {link.label}
                        </span>
                        {link.badge && (
                          <span className="text-[9px] px-1.5 py-0.5 rounded-[2px] bg-studio-card border border-[#3E3D38] text-studio-muted group-hover:text-white group-hover:border-accent transition-colors">
                            {link.badge}
                          </span>
                        )}
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>

        {/* Technical Guarantee & Security Row */}
        <div className="py-6 border-b border-[#262624] grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs font-mono">
          <div className="flex items-center gap-2.5 text-studio-muted">
            <Lock className="w-4 h-4 text-accent shrink-0" />
            <div>
              <span className="text-white block font-medium">100% On-Device DB</span>
              <span className="text-[11px] text-studio-faint">Zero external server relay</span>
            </div>
          </div>

          <div className="flex items-center gap-2.5 text-studio-muted">
            <Smartphone className="w-4 h-4 text-[#10B981] shrink-0" />
            <div>
              <span className="text-white block font-medium">Direct Carrier SIM</span>
              <span className="text-[11px] text-studio-faint">Jio, Airtel, Vi & BSNL</span>
            </div>
          </div>

          <div className="flex items-center gap-2.5 text-studio-muted">
            <ShieldCheck className="w-4 h-4 text-accent shrink-0" />
            <div>
              <span className="text-white block font-medium">TRAI Rule Compliant</span>
              <span className="text-[11px] text-studio-faint">100 SMS/Day limit enforcement</span>
            </div>
          </div>

          <div className="flex items-center gap-2.5 text-studio-muted">
            <Sparkles className="w-4 h-4 text-[#38BDF8] shrink-0" />
            <div>
              <span className="text-white block font-medium">Zero Monthly Bills</span>
              <span className="text-[11px] text-studio-faint">No per-message platform tax</span>
            </div>
          </div>
        </div>

        {/* Bottom Bar (Legal, Copyright & Back to Top) */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between gap-6 font-mono text-xs text-studio-muted">
          {/* Copyright & Parent Attribution */}
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-2 sm:gap-4 text-center sm:text-left">
            <div>
              © {new Date().getFullYear()} ApnaSandesh by{' '}
              <a
                href={SITE_CONFIG.parentBrand.url}
                target="_blank"
                rel="noopener noreferrer"
                className="text-white hover:text-accent underline underline-offset-4"
              >
                {SITE_CONFIG.parentBrand.name}
              </a>
              . All rights reserved.
            </div>
            <div className="hidden sm:block text-[#3E3D38]">•</div>
            <div className="flex items-center gap-3 text-[11px]">
              <Link href="/privacy" className="hover:text-white transition-colors">
                Privacy
              </Link>
              <span>/</span>
              <Link href="/terms" className="hover:text-white transition-colors">
                Terms
              </Link>
              <span>/</span>
              <Link href="/compliance" className="hover:text-white transition-colors">
                TRAI DLT
              </Link>
              <span>/</span>
              <Link href="/sitemap.xml" className="hover:text-white transition-colors">
                Sitemap
              </Link>
            </div>
          </div>

          {/* Package details & Back to Top button */}
          <div className="flex items-center gap-4">
            <div className="hidden lg:flex items-center gap-2 text-[11px] text-studio-faint">
              <span>Pkg:</span>
              <span className="text-studio-muted">{SITE_CONFIG.packageId}</span>
            </div>

            {/* Back to top button */}
            <button
              type="button"
              onClick={scrollToTop}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-[3px] bg-studio-card hover:bg-[#252523] border border-[#3E3D38] hover:border-accent text-white text-[11px] transition-all duration-200 group"
              aria-label="Scroll back to top of page"
            >
              <span>Back to top</span>
              <ArrowUp className="w-3.5 h-3.5 text-accent group-hover:-translate-y-0.5 transition-transform" />
            </button>
          </div>
        </div>
      </div>
    </footer>
  );
}
