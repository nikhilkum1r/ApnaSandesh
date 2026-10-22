import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { BLOG_POSTS } from '@/data/blog';
import { ArrowLeft, BookOpen, Clock, Tag } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Small Business SMS Guides & Engineering Notes',
  description:
    'Practical engineering articles, TRAI regulatory guides, and SMS optimization strategies for small business owners and Android operators.',
};

export default function BlogIndexPage() {
  return (
    <div className="py-16 sm:py-24 bg-canvas min-h-screen">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        {/* Header */}
        <div className="space-y-4">
          <div>
            <Link
              href="/"
              className="inline-flex items-center gap-1.5 font-mono text-xs text-ink-secondary hover:text-ink transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Back to overview</span>
            </Link>
          </div>

          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-accent" />
            <span>Field Guides & Engineering Notes</span>
          </div>

          <h1 className="font-serif text-4xl sm:text-5xl text-ink font-normal tracking-tight leading-[1.08]">
            Guides, Engineering Notes & Carrier Rules
          </h1>
          <p className="text-base sm:text-lg text-ink-secondary font-sans leading-relaxed">
            Plain-language articles explaining telecom regulation, character optimization, and offline architecture for local businesses.
          </p>
        </div>

        {/* Articles List */}
        <div className="divide-y divide-[#E6E4DD] border-t border-b border-[#E6E4DD]">
          {BLOG_POSTS.map((post) => (
            <article key={post.slug} className="py-8 space-y-3 group">
              <div className="flex flex-wrap items-center gap-3 font-mono text-xs text-[#8C8B85]">
                <span>{post.publishedAt}</span>
                <span>•</span>
                <span className="inline-flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  {post.readTime}
                </span>
                <span>•</span>
                <span className="text-ink font-medium">{post.author.name}</span>
              </div>

              <h2 className="font-display font-bold text-xl sm:text-2xl text-ink group-hover:text-accent transition-colors">
                <Link href={`/blog/${post.slug}`}>
                  {post.title}
                </Link>
              </h2>

              <p className="text-xs sm:text-sm text-ink-secondary font-sans leading-relaxed max-w-3xl">
                {post.summary}
              </p>

              <div className="flex flex-wrap items-center justify-between gap-4 pt-2">
                <div className="flex flex-wrap gap-1.5 font-mono text-[11px]">
                  {post.tags.map((tag) => (
                    <span
                      key={tag}
                      className="px-2 py-0.5 bg-white border border-[#E6E4DD] rounded-[2px] text-ink-secondary inline-flex items-center gap-1"
                    >
                      <Tag className="w-2.5 h-2.5 text-accent" />
                      {tag}
                    </span>
                  ))}
                </div>

                <Link
                  href={`/blog/${post.slug}`}
                  className="font-mono text-xs text-accent hover:underline inline-flex items-center gap-1 font-semibold"
                >
                  Read full note →
                </Link>
              </div>
            </article>
          ))}
        </div>
      </div>
    </div>
  );
}
