import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import { BLOG_POSTS, type BlogPost } from '@/data/blog';
import { SITE_CONFIG } from '@/lib/site-config';
import { ArrowLeft, Clock, Calendar, Tag, ExternalLink } from 'lucide-react';

interface Props {
  params: {
    slug: string;
  };
}

export async function generateStaticParams() {
  return BLOG_POSTS.map((post) => ({
    slug: post.slug,
  }));
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const post = BLOG_POSTS.find((p) => p.slug === params.slug);
  if (!post) {
    return {
      title: 'Article Not Found',
    };
  }

  return {
    title: `${post.title} — ApnaSandesh Notes`,
    description: post.summary,
    openGraph: {
      title: post.title,
      description: post.summary,
      type: 'article',
      publishedTime: post.publishedAt,
      authors: [post.author.name],
    },
  };
}

// Simple markdown formatter to render blog post contents cleanly
function renderMarkdown(content: string) {
  const lines = content.trim().split('\n');
  const elements: React.ReactNode[] = [];
  let currentList: string[] = [];
  let listType: 'ul' | 'ol' | null = null;
  let key = 0;

  const flushList = () => {
    if (currentList.length > 0 && listType) {
      if (listType === 'ul') {
        elements.push(
          <ul key={`list-${key++}`} className="space-y-2 my-4 pl-5 list-disc text-ink-secondary">
            {currentList.map((item, idx) => (
              <li key={idx} dangerouslySetInnerHTML={{ __html: formatInline(item) }} />
            ))}
          </ul>
        );
      } else {
        elements.push(
          <ol key={`list-${key++}`} className="space-y-2 my-4 pl-5 list-decimal text-ink-secondary">
            {currentList.map((item, idx) => (
              <li key={idx} dangerouslySetInnerHTML={{ __html: formatInline(item) }} />
            ))}
          </ol>
        );
      }
      currentList = [];
      listType = null;
    }
  };

  const formatInline = (text: string) => {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong class="font-semibold text-ink">$1</strong>')
      .replace(/\*(.*?)\*/g, '<em class="italic">$1</em>')
      .replace(/`([^`]+)`/g, '<code class="font-mono text-xs text-ink bg-[#FAF9F6] px-1.5 py-0.5 border border-[#E6E4DD] rounded-[2px]">$1</code>')
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" class="text-accent underline hover:text-accent-hover underline-offset-4">$1</a>');
  };

  for (let i = 0; i < lines.length; i++) {
    const rawLine = lines[i];
    const trimmed = rawLine.trim();

    if (!trimmed) {
      flushList();
      continue;
    }

    if (trimmed.startsWith('## ')) {
      flushList();
      elements.push(
        <h2 key={`h2-${key++}`} className="font-display font-bold text-2xl text-ink mt-8 mb-4">
          {trimmed.replace('## ', '')}
        </h2>
      );
    } else if (trimmed.startsWith('### ')) {
      flushList();
      elements.push(
        <h3 key={`h3-${key++}`} className="font-display font-bold text-lg text-ink mt-6 mb-3">
          {trimmed.replace('### ', '')}
        </h3>
      );
    } else if (trimmed.startsWith('#### ')) {
      flushList();
      elements.push(
        <h4 key={`h4-${key++}`} className="font-display font-semibold text-base text-ink mt-4 mb-2">
          {trimmed.replace('#### ', '')}
        </h4>
      );
    } else if (trimmed === '---') {
      flushList();
      elements.push(
        <hr key={`hr-${key++}`} className="my-8 border-t border-[#E6E4DD]" />
      );
    } else if (trimmed.startsWith('- ')) {
      if (listType !== 'ul') {
        flushList();
        listType = 'ul';
      }
      currentList.push(trimmed.replace('- ', ''));
    } else if (/^\d+\.\s/.test(trimmed)) {
      if (listType !== 'ol') {
        flushList();
        listType = 'ol';
      }
      currentList.push(trimmed.replace(/^\d+\.\s/, ''));
    } else {
      flushList();
      elements.push(
        <p
          key={`p-${key++}`}
          className="text-base text-ink-secondary font-sans leading-relaxed my-3"
          dangerouslySetInnerHTML={{ __html: formatInline(trimmed) }}
        />
      );
    }
  }

  flushList();
  return elements;
}

export default function BlogPostPage({ params }: Props) {
  const post = BLOG_POSTS.find((p) => p.slug === params.slug);

  if (!post) {
    notFound();
  }

  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        {/* Back Link */}
        <div>
          <Link
            href="/blog"
            className="font-mono text-xs text-ink-secondary hover:text-ink inline-flex items-center gap-1.5 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to all guides & notes</span>
          </Link>
        </div>

        {/* Header */}
        <header className="space-y-4 pb-8 border-b border-[#E6E4DD]">
          <div className="flex flex-wrap items-center gap-3 font-mono text-xs text-[#8C8B85]">
            <span className="inline-flex items-center gap-1">
              <Calendar className="w-3 h-3" />
              {post.publishedAt}
            </span>
            <span>•</span>
            <span className="inline-flex items-center gap-1">
              <Clock className="w-3 h-3" />
              {post.readTime}
            </span>
            <span>•</span>
            <span className="text-ink font-semibold">{post.author.name}</span>
          </div>

          <h1 className="font-serif text-3xl sm:text-4xl lg:text-5xl text-ink font-normal tracking-tight leading-[1.12]">
            {post.title}
          </h1>

          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            {post.summary}
          </p>

          <div className="flex flex-wrap gap-1.5 pt-2 font-mono text-xs">
            {post.tags.map((tag) => (
              <span
                key={tag}
                className="px-2 py-0.5 bg-white border border-[#E6E4DD] rounded-[2px] text-ink-secondary inline-flex items-center gap-1"
              >
                <Tag className="w-2.5 h-2.5 text-accent" />
                #{tag}
              </span>
            ))}
          </div>
        </header>

        {/* Article Body */}
        <article className="p-6 sm:p-10 bg-white border border-[#E6E4DD] rounded-[4px] space-y-4 shadow-xs text-sm sm:text-base leading-relaxed text-ink font-sans">
          {renderMarkdown(post.content)}
        </article>

        {/* Footer CTA to Google Play */}
        <div className="p-6 bg-studio-black text-[#FAF9F6] rounded-[4px] border border-[#2C2B28] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 font-mono text-xs">
          <div className="space-y-1">
            <div className="font-bold text-white text-sm">Download ApnaSandesh on Google Play</div>
            <p className="text-[#8C8B85] font-sans text-xs">
              Start communicating directly from your Android phone without cloud servers.
            </p>
          </div>
          <a
            href={SITE_CONFIG.playStoreUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 bg-accent hover:bg-accent-hover text-white rounded-[2px] uppercase tracking-wider font-semibold whitespace-nowrap inline-flex items-center gap-1.5 transition-colors"
          >
            <span>View on Google Play</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>
    </div>
  );
}
