'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { SITE_CONFIG } from '@/lib/site-config';
import { Menu, X, ArrowUpRight } from 'lucide-react';

export function Navbar() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const pathname = usePathname();

  return (
    <header className="sticky top-0 z-40 bg-canvas/95 backdrop-blur-md hairline-b border-b border-[#E6E4DD]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo Brand Link */}
          <Link href="/" className="flex items-center gap-3 group">
            <div className="relative w-9 h-9 rounded-[4px] overflow-hidden border border-[#E6E4DD] bg-white p-1 flex items-center justify-center shadow-xs group-hover:border-accent transition-colors">
              <img
                src="/logo.svg"
                alt="ApnaSandesh Logo"
                className="w-full h-full object-contain"
                width={36}
                height={36}
              />
            </div>
            <div className="flex flex-col">
              <span className="font-display font-bold text-lg text-ink tracking-tight leading-none group-hover:text-accent transition-colors">
                ApnaSandesh
              </span>
              <span className="font-mono text-[10px] text-ink-muted uppercase tracking-wider mt-0.5">
                SIM SMS Workstation
              </span>
            </div>
          </Link>

          {/* Desktop Navigation Links */}
          <nav className="hidden md:flex items-center gap-1 font-mono text-xs">
            {SITE_CONFIG.navLinks.map((link) => {
              const isActive = pathname === link.href;
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`px-3 py-1.5 rounded-[2px] transition-colors ${
                    isActive
                      ? 'text-ink font-semibold bg-[#EFEFED]'
                      : 'text-ink-secondary hover:text-ink hover:bg-[#F3F2ED]'
                  }`}
                >
                  {link.label}
                </Link>
              );
            })}
          </nav>

          {/* Action CTAs */}
          <div className="hidden sm:flex items-center gap-3">
            <Link
              href="/calculator"
              className="text-xs font-mono text-ink-secondary hover:text-ink px-3 py-1.5 rounded-[2px] border border-[#E6E4DD] hover:bg-[#F3F2ED] transition-colors"
            >
              GSM-7 Tool
            </Link>
            <a
              href={SITE_CONFIG.playStoreUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 px-4 py-2 text-xs font-mono font-medium tracking-wide text-white bg-accent hover:bg-accent-hover rounded-[3px] border border-accent shadow-xs transition-colors"
            >
              <svg className="w-3.5 h-3.5 fill-current" viewBox="0 0 24 24">
                <path d="M3.609 1.814L13.793 12 3.61 22.186a2.38 2.38 0 0 1-.22-.99V2.805c0-.36.08-.7.22-.991zm11.242 11.243l2.25 2.25-11.83 6.702 9.58-8.952zm0-2.114L5.27 1.99l11.83 6.703-2.25 2.25zm1.414 1.414l3.125-1.77c.883-.5 1.418-1.42 1.418-2.437s-.535-1.937-1.418-2.437l-3.125-1.77-2.353 2.354 2.353 2.354z" />
              </svg>
              <span>Get App</span>
            </a>
          </div>

          {/* Mobile Menu Toggle Button */}
          <div className="md:hidden flex items-center gap-2">
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-[2px] border border-[#E6E4DD] text-ink hover:bg-[#F3F2ED]"
              aria-label="Toggle navigation menu"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-canvas border-b border-[#E6E4DD] px-4 pt-3 pb-5 space-y-2 font-mono text-sm">
          {SITE_CONFIG.navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              onClick={() => setMobileMenuOpen(false)}
              className="block px-3 py-2 rounded-[2px] text-ink hover:bg-[#F3F2ED] transition-colors"
            >
              {link.label}
            </Link>
          ))}
          <div className="pt-3 border-t border-[#E6E4DD] flex flex-col gap-2">
            <a
              href={SITE_CONFIG.playStoreUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="w-full inline-flex items-center justify-center gap-2 py-3 text-xs font-mono font-medium text-white bg-accent rounded-[3px]"
            >
              <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
                <path d="M3.609 1.814L13.793 12 3.61 22.186a2.38 2.38 0 0 1-.22-.99V2.805c0-.36.08-.7.22-.991zm11.242 11.243l2.25 2.25-11.83 6.702 9.58-8.952zm0-2.114L5.27 1.99l11.83 6.703-2.25 2.25zm1.414 1.414l3.125-1.77c.883-.5 1.418-1.42 1.418-2.437s-.535-1.937-1.418-2.437l-3.125-1.77-2.353 2.354 2.353 2.354z" />
              </svg>
              <span>Download on Google Play</span>
            </a>
          </div>
        </div>
      )}
    </header>
  );
}
