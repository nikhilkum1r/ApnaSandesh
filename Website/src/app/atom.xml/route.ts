import { BLOG_POSTS } from '@/data/blog';
import { SITE_CONFIG } from '@/lib/site-config';

export const dynamic = 'force-static';

export async function GET() {
  const baseUrl = SITE_CONFIG.url;
  const updatedDate = new Date().toISOString();

  const entriesXml = BLOG_POSTS.map((post) => {
    const publishedIso = new Date(post.publishedAt).toISOString();
    return `
  <entry>
    <title>${post.title}</title>
    <link rel="alternate" href="${baseUrl}/blog/${post.slug}" />
    <id>${baseUrl}/blog/${post.slug}</id>
    <updated>${publishedIso}</updated>
    <published>${publishedIso}</published>
    <summary>${post.summary}</summary>
    <author>
      <name>${post.author.name}</name>
      <email>${SITE_CONFIG.parentBrand.supportEmail}</email>
    </author>
  </entry>`;
  }).join('');

  const atomXml = `<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <title>${SITE_CONFIG.name} by ${SITE_CONFIG.parentBrand.name}</title>
  <subtitle>${SITE_CONFIG.tagline}</subtitle>
  <link href="${baseUrl}/atom.xml" rel="self" />
  <link href="${baseUrl}/" />
  <id>${baseUrl}/</id>
  <updated>${updatedDate}</updated>
  <author>
    <name>${SITE_CONFIG.parentBrand.name}</name>
    <uri>${SITE_CONFIG.parentBrand.url}</uri>
  </author>
  ${entriesXml}
</feed>`.trim();

  return new Response(atomXml, {
    headers: {
      'Content-Type': 'application/atom+xml; charset=utf-8',
      'Cache-Control': 'public, max-age=86400',
    },
  });
}
