import { MetadataRoute } from 'next';
import { SITE_CONFIG } from '@/lib/site-config';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: [
      {
        userAgent: '*',
        allow: '/',
        disallow: ['/api/', '/_next/'],
      },
      {
        userAgent: [
          'GPTBot',
          'ChatGPT-User',
          'Google-Extended',
          'Anthropic-ai',
          'Claude-Web',
          'PerplexityBot',
          'Bytespider',
          'CCBot',
        ],
        allow: [
          '/',
          '/llms.txt',
          '/llms-full.txt',
          '/ai.txt',
          '/calculator',
          '/features',
          '/use-cases',
          '/compare',
          '/docs',
          '/download',
          '/templates',
          '/compliance',
          '/changelog',
          '/terms',
          '/privacy',
          '/blog',
        ],
      },
    ],
    sitemap: `${SITE_CONFIG.url}/sitemap.xml`,
  };
}
