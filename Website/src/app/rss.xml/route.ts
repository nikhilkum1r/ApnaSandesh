import { BLOG_POSTS } from '@/data/blog';
import { SITE_CONFIG } from '@/lib/site-config';

export const dynamic = 'force-static';

export async function GET() {
  const baseUrl = SITE_CONFIG.url;

  const itemsXml = BLOG_POSTS.map((post) => {
    const pubDate = new Date(post.publishedAt).toUTCString();
    return `
    <item>
      <title><![CDATA[${post.title}]]></title>
      <link>${baseUrl}/blog/${post.slug}</link>
      <guid isPermaLink="true">${baseUrl}/blog/${post.slug}</guid>
      <description><![CDATA[${post.summary}]]></description>
      <pubDate>${pubDate}</pubDate>
      <author>${SITE_CONFIG.parentBrand.supportEmail} (${post.author.name})</author>
      ${post.tags.map((t) => `<category>${t}</category>`).join('')}
    </item>`;
  }).join('');

  const rssXml = `<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
    <title>${SITE_CONFIG.name} by ${SITE_CONFIG.parentBrand.name} — Updates &amp; Release Notes</title>
    <link>${baseUrl}/</link>
    <description>${SITE_CONFIG.description}</description>
    <language>en-us</language>
    <lastBuildDate>${new Date().toUTCString()}</lastBuildDate>
    <atom:link href="${baseUrl}/rss.xml" rel="self" type="application/rss+xml" />
    ${itemsXml}
  </channel>
</rss>`.trim();

  return new Response(rssXml, {
    headers: {
      'Content-Type': 'application/rss+xml; charset=utf-8',
      'Cache-Control': 'public, max-age=86400',
    },
  });
}
