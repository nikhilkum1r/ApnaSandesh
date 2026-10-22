import React from 'react';
import { SITE_CONFIG } from '@/lib/site-config';

export function TruStoryXBar() {
  return (
    <aside
      aria-label="Parent network banner"
      className="hairline-b bg-studio-black text-[#FAF9F6] text-xs font-mono py-2 px-4 border-b border-[#2C2B28]"
    >
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse" />
          <span className="text-studio-muted">FLAGSHIP PRODUCT OF</span>
          <a
            href={SITE_CONFIG.parentBrand.url}
            target="_blank"
            rel="noopener noreferrer"
            className="text-[#FAF9F6] font-semibold hover:text-accent transition-colors underline-offset-4 hover:underline"
          >
            {SITE_CONFIG.parentBrand.name}
          </a>
        </div>
        <div className="hidden sm:flex items-center gap-4 text-studio-muted text-[11px]">
          <span>Android v{SITE_CONFIG.version} Release</span>
          <span className="text-[#3E3D38]">|</span>
          <span className="text-[#10B981]">0 KB Cloud Leakage Guaranteed</span>
        </div>
      </div>
    </aside>
  );
}
