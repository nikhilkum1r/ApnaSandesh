'use client';

import React, { useState } from 'react';

interface FaqItem {
  question: string;
  answer: string;
}

const FAQ_ITEMS: FaqItem[] = [
  {
    question: 'Will telecom carriers block or flag my SIM card for sending SMS?',
    answer:
      'Carriers flag SIM cards when they detect high-velocity burst traffic (e.g. 50 messages per second) or receive repeated user spam complaints. ApnaSandesh prevents this through two mechanisms: first, an enforced 3.5s to 6s safety throttle between sequential dispatches; second, strict opt-out suppression so unconsenting recipients are never contacted again. As long as you stay within your operator’s fair use policy (such as the standard 100 SMS/day guideline in India) and message consented customers, your SIM operates normally.',
  },
  {
    question: 'Why is there no cloud dashboard or web login?',
    answer:
      'Cloud dashboards require external servers, web infrastructure, and continuous user databases. That model forces companies to charge monthly subscription fees and increases privacy liability. By keeping ApnaSandesh 100% on your Android device in a local Room SQLite database, we eliminate server operating costs, avoid user data leaks, and ensure the app works even with mobile data turned off.',
  },
  {
    question: 'What is the difference between composer mode and direct SIM mode?',
    answer:
      'In composer mode, ApnaSandesh generates the personalized message and opens your phone’s default SMS app (Google Messages or Samsung Messages) with the recipient and text already loaded. You tap send. This requires zero Android permissions. In direct SIM mode, the app uses Android’s TelephonyManager and a Foreground Service to dispatch queued messages sequentially in the background while you do other work.',
  },
  {
    question: 'What happens when a customer replies to an SMS?',
    answer:
      'Because messages are sent from your real cellular SIM card, replies arrive directly in your normal Android messaging inbox (Google Messages, Samsung Messages, or Truecaller). You can reply personally just like any regular text message.',
  },
  {
    question: 'How do I import customer lists from spreadsheets or point-of-sale systems?',
    answer:
      'Export your client list as a standard .csv file from Excel, Google Sheets, or your POS. In ApnaSandesh, tap "Import CSV". The parser reads the columns (Name, Phone, Tags, Notes, Custom Fields), normalizes numbers into international E.164 format (e.g. +91), filters out duplicates, and saves them directly to your local database.',
  },
  {
    question: 'Can I schedule campaigns to send at a specific future time?',
    answer:
      'Yes. ApnaSandesh integrates with Android’s native WorkManager to queue campaigns for deferred execution (such as "+1 Hour", "Tomorrow 10:00 AM", or custom date/time). Android wakes up the execution service at the scheduled time to run the dispatch.',
  },
];

export default function EditorialFaq() {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  const toggle = (index: number) => {
    setOpenIndex(openIndex === index ? null : index);
  };

  return (
    <section id="faq" className="py-20 hairline-b bg-canvas">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="max-w-3xl mb-12 space-y-3">
          <div className="font-mono text-xs uppercase tracking-wider text-ink-secondary">
            Questions & practical answers
          </div>
          <h2 className="font-serif text-3xl sm:text-4xl text-ink tracking-tight font-normal">
            Frequently asked questions about on-device messaging
          </h2>
          <p className="text-base text-ink-secondary font-sans leading-relaxed">
            Direct answers about hardware limits, carrier regulations, and data safety.
          </p>
        </div>

        <div className="max-w-4xl divide-y divide-[#E6E4DD] border-t border-b border-[#E6E4DD]">
          {FAQ_ITEMS.map((item, idx) => {
            const isOpen = openIndex === idx;
            return (
              <div key={idx} className="py-5">
                <button
                  type="button"
                  onClick={() => toggle(idx)}
                  className="w-full flex items-center justify-between text-left group focus:outline-none"
                >
                  <span className="font-sans font-semibold text-base text-ink group-hover:text-accent transition-colors pr-6">
                    {item.question}
                  </span>
                  <span className="font-mono text-xs text-ink-secondary flex-shrink-0">
                    {isOpen ? '[ – ]' : '[ + ]'}
                  </span>
                </button>
                {isOpen && (
                  <div className="pt-3 pr-8 text-sm text-ink-secondary font-sans leading-relaxed">
                    <p>{item.answer}</p>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
