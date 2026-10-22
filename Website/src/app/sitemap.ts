import { MetadataRoute } from 'next';
import { SITE_CONFIG } from '@/lib/site-config';
import { BLOG_POSTS } from '@/data/blog';

export default function sitemap(): MetadataRoute.Sitemap {
  const staticRoutes = [
    '',
    '/calculator',
    '/features',
    '/use-cases',
    '/compare',
    '/download',
    '/docs',
    '/templates',
    '/compliance',
    '/changelog',
    '/terms',
    '/privacy',
    '/blog',
  ];

  const now = new Date();

  const staticEntries: MetadataRoute.Sitemap = staticRoutes.map((route) => ({
    url: `${SITE_CONFIG.url}${route}`,
    lastModified: now,
    changeFrequency: route === '' ? 'daily' : 'weekly',
    priority: route === '' ? 1.0 : route === '/download' || route === '/calculator' ? 0.9 : 0.8,
  }));

  const blogEntries: MetadataRoute.Sitemap = BLOG_POSTS.map((post) => ({
    url: `${SITE_CONFIG.url}/blog/${post.slug}`,
    lastModified: new Date(post.publishedAt),
    changeFrequency: 'monthly',
    priority: 0.7,
  }));

  return [...staticEntries, ...blogEntries];
}
