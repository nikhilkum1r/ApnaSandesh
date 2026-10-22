import type { Metadata } from 'next';
import '@/styles/globals.css';
import { SITE_CONFIG } from '@/lib/site-config';
import { TruStoryXBar } from '@/components/TruStoryXBar';
import { Navbar } from '@/components/Navbar';
import { Footer } from '@/components/Footer';

export const metadata: Metadata = {
  metadataBase: new URL(SITE_CONFIG.url),
  title: {
    default: 'ApnaSandesh — Aapka Number, Aapke Customers.',
    template: '%s | ApnaSandesh',
  },
  description: SITE_CONFIG.description,
  keywords: [
    'ApnaSandesh',
    'business SMS Android',
    'SIM card SMS marketing',
    'send SMS from phone SIM',
    'bulk SMS without gateway',
    'Jio Airtel business SMS',
    'offline SMS campaign manager',
    'TRAI 100 SMS limit compliant',
    'local Room SQLite SMS app',
    'free business SMS tool India',
    'TruStoryX Digital',
  ],
  authors: [{ name: SITE_CONFIG.parentBrand.name, url: SITE_CONFIG.parentBrand.url }],
  creator: SITE_CONFIG.parentBrand.name,
  publisher: SITE_CONFIG.parentBrand.name,
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large',
      'max-snippet': -1,
    },
  },
  openGraph: {
    type: 'website',
    locale: 'en_IN',
    url: SITE_CONFIG.url,
    title: 'ApnaSandesh — Aapka Number, Aapke Customers.',
    description: SITE_CONFIG.description,
    siteName: 'ApnaSandesh by TruStoryX Digital',
    images: [
      {
        url: '/og-image.jpg',
        width: 1200,
        height: 630,
        alt: 'ApnaSandesh Android Business SMS Workstation',
      },
      {
        url: '/apnasandesh_hero_banner.png',
        width: 1024,
        height: 576,
        alt: 'ApnaSandesh Android UI Showcase',
      },
    ],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'ApnaSandesh — Aapka Number, Aapke Customers.',
    description: SITE_CONFIG.description,
    images: ['/og-image.jpg'],
    creator: '@trustoryx',
  },
  icons: {
    icon: [
      { url: '/favicon.svg', type: 'image/svg+xml' },
      { url: '/favicon.png', type: 'image/png' },
      { url: '/ic_apnasandesh_mark.png', type: 'image/png' },
    ],
    apple: [{ url: '/ic_apnasandesh_mark.png', sizes: '96x96' }],
  },
  alternates: {
    canonical: SITE_CONFIG.url,
    types: {
      'application/rss+xml': `${SITE_CONFIG.url}/rss.xml`,
      'application/atom+xml': `${SITE_CONFIG.url}/atom.xml`,
      'application/json': `${SITE_CONFIG.url}/feed.json`,
    },
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const jsonLd = {
    '@context': 'https://schema.org',
    '@graph': [
      {
        '@type': 'Organization',
        '@id': `${SITE_CONFIG.parentBrand.url}/#organization`,
        name: SITE_CONFIG.parentBrand.name,
        url: SITE_CONFIG.parentBrand.url,
        logo: `${SITE_CONFIG.url}/logo.png`,
        contactPoint: {
          '@type': 'ContactPoint',
          email: SITE_CONFIG.parentBrand.supportEmail,
          contactType: 'customer support',
          areaServed: 'IN',
        },
      },
      {
        '@type': 'SoftwareApplication',
        '@id': `${SITE_CONFIG.url}/#software`,
        name: SITE_CONFIG.name,
        operatingSystem: 'Android 8.0 or higher',
        applicationCategory: 'BusinessApplication',
        offers: {
          '@type': 'Offer',
          price: '0.00',
          priceCurrency: 'INR',
        },
        description: SITE_CONFIG.description,
        aggregateRating: {
          '@type': 'AggregateRating',
          ratingValue: '4.9',
          ratingCount: '128',
        },
        softwareVersion: SITE_CONFIG.version,
        downloadUrl: SITE_CONFIG.playStoreUrl,
        publisher: {
          '@id': `${SITE_CONFIG.parentBrand.url}/#organization`,
        },
      },
      {
        '@type': 'FAQPage',
        '@id': `${SITE_CONFIG.url}/#faq`,
        mainEntity: [
          {
            '@type': 'Question',
            name: 'Will telecom carriers block or flag my SIM card for sending SMS?',
            acceptedAnswer: {
              '@type': 'Answer',
              text: 'Carriers flag SIM cards when they detect burst traffic or receive user spam complaints. ApnaSandesh enforces a 3.5s to 6s safety throttle between sequential dispatches and automatic opt-out suppression to keep your SIM in complete compliance.',
            },
          },
          {
            '@type': 'Question',
            name: 'Why is there no cloud dashboard or web login?',
            acceptedAnswer: {
              '@type': 'Answer',
              text: 'By keeping ApnaSandesh 100% on your Android device in a local Room SQLite database, we eliminate server operating costs, avoid user data leaks, and ensure the app works even with mobile data turned off.',
            },
          },
        ],
      },
    ],
  };

  return (
    <html lang="en" className="scroll-smooth">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=Plus+Jakarta+Sans:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap"
          rel="stylesheet"
        />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      </head>
      <body className="bg-canvas text-ink antialiased min-h-screen flex flex-col selection:bg-accent selection:text-white font-sans">
        <TruStoryXBar />
        <Navbar />
        <main className="flex-1">{children}</main>
        <Footer />
      </body>
    </html>
  );
}
