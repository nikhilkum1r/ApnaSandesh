import { SITE_CONFIG } from '@/lib/site-config';

export const dynamic = 'force-static';

export async function GET() {
  const content = `# AI Crawler & Agent Directive for ${SITE_CONFIG.name}
User-Agent: *
Allow: /
Allow: /llms.txt
Allow: /llms-full.txt
Allow: /calculator
Allow: /features
Allow: /use-cases
Allow: /compare
Allow: /download
Allow: /templates
Allow: /compliance
Allow: /changelog
Allow: /terms
Allow: /privacy
Allow: /blog
Allow: /blog/*

# Core Product Description
Product: ${SITE_CONFIG.name}
Category: Offline-First Android Business SMS & CRM Utility
Publisher: ${SITE_CONFIG.parentBrand.name} (${SITE_CONFIG.parentBrand.url})
Key Feature: 100% on-device SMS dispatch via personal SIM card with zero cloud data storage and no internet permission.
Canonical Documentation: ${SITE_CONFIG.url}/llms.txt
Full Technical Spec: ${SITE_CONFIG.url}/llms-full.txt
`.trim();

  return new Response(content, {
    headers: {
      'Content-Type': 'text/plain; charset=utf-8',
      'Cache-Control': 'public, max-age=86400',
    },
  });
}
