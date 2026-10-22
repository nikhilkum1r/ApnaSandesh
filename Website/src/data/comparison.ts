export interface ComparisonRow {
  dimension: string;
  category: 'Economics' | 'Data Privacy' | 'Customer Experience' | 'Operations & Legal';
  apnasandesh: {
    title: string;
    description: string;
    badge: 'Superior' | 'Compliant' | 'Zero Cost' | 'Instant';
  };
  cloudGateways: {
    title: string;
    description: string;
    badge: 'Expensive' | 'Complex' | 'Cloud Hosted';
  };
  manualSms: {
    title: string;
    description: string;
    badge: 'Tedious' | 'High Friction' | 'Error Prone';
  };
}

export const COMPARISON_DATA: ComparisonRow[] = [
  {
    dimension: 'Ongoing Cost & Subscriptions',
    category: 'Economics',
    apnasandesh: {
      title: '₹0 / month. Uses your existing SIM balance',
      description: 'Zero platform fee. Leverages your existing Jio, Airtel, or Vi 100 SMS/day bundled quota.',
      badge: 'Zero Cost'
    },
    cloudGateways: {
      title: '₹12,000–₹35,000 / year + per-SMS surcharge',
      description: 'Requires monthly SaaS subscription, per-message fees (₹0.15–₹0.40/SMS), and DLT portal annual maintenance.',
      badge: 'Expensive'
    },
    manualSms: {
      title: '₹0 (Free SIM SMS)',
      description: 'Included in plan, but costs hours of manual typing and copy-pasting.',
      badge: 'Tedious'
    }
  },
  {
    dimension: 'Customer Contact Privacy & Data Sovereignty',
    category: 'Data Privacy',
    apnasandesh: {
      title: '100% On-Device Sandbox. Zero Network Sockets',
      description: 'ApnaSandesh does not declare the INTERNET permission. Contacts never leave your physical phone.',
      badge: 'Superior'
    },
    cloudGateways: {
      title: 'Customer address book uploaded to remote servers',
      description: 'Client numbers, names, and purchase histories reside in third-party US/EU/India cloud databases subject to data leaks.',
      badge: 'Cloud Hosted'
    },
    manualSms: {
      title: 'Local to phone, but unorganized',
      description: 'Stored in system contacts without business tags, audit trail, or suppression tracking.',
      badge: 'Error Prone'
    }
  },
  {
    dimension: 'Sender Identity & Customer Trust',
    category: 'Customer Experience',
    apnasandesh: {
      title: 'Sent from your known, recognizable mobile number',
      description: 'Customers immediately recognize their local shopkeeper, dentist, or mechanic. High open & reply rates.',
      badge: 'Superior'
    },
    cloudGateways: {
      title: 'Cryptic 6-character header (e.g. AX-SHRPAY)',
      description: 'Customers frequently mistake headers for phishing spam or automated banking alerts and ignore them.',
      badge: 'Complex'
    },
    manualSms: {
      title: 'Sent from your personal number',
      description: 'Recognizable, but difficult to maintain professional signatures and clean formatting across 40 clients.',
      badge: 'High Friction'
    }
  },
  {
    dimension: 'Setup Time & Onboarding Complexity',
    category: 'Operations & Legal',
    apnasandesh: {
      title: 'Under 3 minutes. Zero KYC or portal approvals',
      description: 'Install APK, set your business name, import contacts via CSV or address book, and start dispatching.',
      badge: 'Instant'
    },
    cloudGateways: {
      title: '14 to 30 days of bureaucratic DLT registration',
      description: 'Requires telemarketer registration, entity verification, KYC pan/gstin documents, template scrubbing approvals, and credit card setup.',
      badge: 'Complex'
    },
    manualSms: {
      title: 'Immediate, but completely unscalable',
      description: 'No setup, but typing 30 individual messages takes 45–60 minutes every single day.',
      badge: 'Tedious'
    }
  },
  {
    dimension: 'Opt-Out & Suppression Compliance',
    category: 'Operations & Legal',
    apnasandesh: {
      title: 'Permanent SQLite Suppression Firewall',
      description: '1-tap opt-out suppression prevents accidental re-messaging. Automatic TRAI 10am–9pm IST safety check.',
      badge: 'Compliant'
    },
    cloudGateways: {
      title: 'Requires complex webhook integration',
      description: 'Must configure incoming webhooks and cloud database synchronizers to handle STOP replies.',
      badge: 'Complex'
    },
    manualSms: {
      title: 'Zero suppression protection',
      description: 'Relying on mental memory inevitably results in messaging angry opted-out customers.',
      badge: 'Error Prone'
    }
  },
  {
    dimension: 'Offline Reliability',
    category: 'Operations & Legal',
    apnasandesh: {
      title: 'Operates in basements & remote areas with cellular signal',
      description: 'Does not require Wi-Fi, broadband, or active internet connection. Dispatches whenever cellular tower is in range.',
      badge: 'Superior'
    },
    cloudGateways: {
      title: 'Fails immediately when internet is down',
      description: 'If your shop Wi-Fi or mobile data fails, you cannot access the cloud dashboard or send urgent updates.',
      badge: 'Cloud Hosted'
    },
    manualSms: {
      title: 'Works offline via cellular',
      description: 'Works offline, but lacks batch management, personalized variables, or delivery confirmation receipts.',
      badge: 'High Friction'
    }
  }
];
