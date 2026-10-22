import { BLOG_POSTS } from '@/data/blog';
import { SITE_CONFIG } from '@/lib/site-config';

export const dynamic = 'force-static';

export async function GET() {
  const baseUrl = SITE_CONFIG.url;

  const feedObj = {
    version: 'https://jsonfeed.org/version/1.1',
    title: `${SITE_CONFIG.name} by ${SITE_CONFIG.parentBrand.name} — Updates & Guides`,
    home_page_url: `${baseUrl}/`,
    feed_url: `${baseUrl}/feed.json`,
    description: SITE_CONFIG.description,
    icon: `${baseUrl}/logo.svg`,
    favicon: `${baseUrl}/favicon.svg`,
    authors: [
      {
        name: SITE_CONFIG.parentBrand.name,
        url: SITE_CONFIG.parentBrand.url,
      },
    ],
    items: BLOG_POSTS.map((post) => ({
      id: `${baseUrl}/blog/${post.slug}`,
      url: `${baseUrl}/blog/${post.slug}`,
      title: post.title,
      summary: post.summary,
      date_published: new Date(post.publishedAt).toISOString(),
      tags: post.tags,
      authors: [
        {
          name: post.author.name,
        },
      ],
    })),
  };

  return new Response(JSON.stringify(feedObj, null, 2), {
    headers: {
      'Content-Type': 'application/feed+json; charset=utf-8',
      'Cache-Control': 'public, max-age=86400',
    },
  });
}
