export interface SmsTemplate {
  id: string;
  title: string;
  category: 'order' | 'appointment' | 'payment' | 'service' | 'delivery' | 'gratitude' | 'announcement' | 'promotion' | 'festival' | 'custom';
  categoryLabel: string;
  type: 'Transactional' | 'Opt-In Promotional';
  text: string;
  description: string;
  sampleVariables: Record<string, string>;
  isUnicode?: boolean;
}

export const BUSINESS_TEMPLATES: SmsTemplate[] = [
  {
    id: 'order-confirm',
    title: 'Order Confirmation',
    category: 'order',
    categoryLabel: 'Orders & Bookings',
    type: 'Transactional',
    description: 'Instant notification sent immediately upon receiving or preparing a customer order.',
    sampleVariables: {
      customer_name: 'Rajesh',
      business_name: 'Sharma Electronics',
      order_id: '#ORD-8821',
      business_phone: '+91 98200 12345'
    },
    text: 'Hi {customer_name}, your order {order_id} has been confirmed at {business_name}. We are preparing it now. Call {business_phone} for queries.'
  },
  {
    id: 'appt-reminder',
    title: 'Appointment Reminder',
    category: 'appointment',
    categoryLabel: 'Appointments & Consultations',
    type: 'Transactional',
    description: 'Reduces no-shows for dental clinics, salons, repair shops, and professional consultants.',
    sampleVariables: {
      customer_name: 'Priya',
      business_name: 'Dr. Mehta Dental Clinic',
      time: 'Tomorrow at 4:30 PM',
      business_phone: '+91 98450 77123'
    },
    text: 'Hello {customer_name}, reminder for your appointment at {business_name} on {time}. Reply YES to confirm or call {business_phone} to reschedule.'
  },
  {
    id: 'payment-reminder',
    title: 'Payment Reminder',
    category: 'payment',
    categoryLabel: 'Billing & Invoicing',
    type: 'Transactional',
    description: 'Polite, firm payment balance follow-up for suppliers, wholesalers, and service contractors.',
    sampleVariables: {
      customer_name: 'Amit',
      business_name: 'Apex Electricals',
      amount: '₹4,500',
      due_date: '25th Oct',
      upi_id: 'apex@okaxis'
    },
    text: 'Dear {customer_name}, invoice balance of {amount} for {business_name} is due on {due_date}. Kindly pay via UPI {upi_id}. Thank you!'
  },
  {
    id: 'service-ready',
    title: 'Service Completion & Ready for Pickup',
    category: 'service',
    categoryLabel: 'Service & Repairs',
    type: 'Transactional',
    description: 'Alerts vehicle owners, laptop/phone repair clients, or dry-cleaners that their item is ready.',
    sampleVariables: {
      customer_name: 'Vikas',
      business_name: 'QuickFix Auto Care',
      item: 'Honda City (KA05-1234)',
      business_phone: '+91 98110 54321'
    },
    text: 'Hi {customer_name}, your vehicle {item} is fully serviced and ready for pickup at {business_name}. Workshop closes at 8 PM. Ph: {business_phone}.'
  },
  {
    id: 'delivery-dispatch',
    title: 'Delivery Out for Dispatch',
    category: 'delivery',
    categoryLabel: 'Delivery & Logistics',
    type: 'Transactional',
    description: 'Informs local customers when their package or grocery basket is out with the delivery executive.',
    sampleVariables: {
      customer_name: 'Sunita',
      business_name: 'FreshBazaar',
      order_id: '#5512',
      rider_phone: '+91 97120 44889'
    },
    text: 'Hi {customer_name}, your grocery basket {order_id} from {business_name} is out for delivery. Rider contact: {rider_phone}. Thank you!'
  },
  {
    id: 'customer-gratitude',
    title: 'Customer Thank-You & Receipt',
    category: 'gratitude',
    categoryLabel: 'Customer Retention',
    type: 'Transactional',
    description: 'Builds long-term loyalty immediately after an in-store purchase or completed job.',
    sampleVariables: {
      customer_name: 'Kavita',
      business_name: 'Silk Haven Boutique',
      owner_name: 'Meenakshi',
      business_phone: '+91 98900 66778'
    },
    text: 'Thank you for visiting {business_name}, {customer_name}! It was a pleasure serving you. Keep this message as proof of your 1-year stitch warranty.'
  },
  {
    id: 'biz-announcement',
    title: 'Business Hours or Location Announcement',
    category: 'announcement',
    categoryLabel: 'Operational Updates',
    type: 'Transactional',
    description: 'Notifies regular clients of holiday timings, shop relocation, or new contact numbers.',
    sampleVariables: {
      customer_name: 'Valued Client',
      business_name: 'City Pathology Lab',
      announcement: 'shifted 50m ahead to 2nd Floor, Sai Complex',
      business_phone: '+91 98220 33112'
    },
    text: 'Notice: {business_name} has {announcement}. Timings: 7 AM to 9 PM daily. Call {business_phone} for home sample collection.'
  },
  {
    id: 'optin-promo',
    title: 'Opt-In Customer Exclusive Promotion',
    category: 'promotion',
    categoryLabel: 'Promotions & Discounts',
    type: 'Opt-In Promotional',
    description: 'For verified opted-in repeat customers. Includes mandatory TRAI 10am-9pm & STOP opt-out footer.',
    sampleVariables: {
      customer_name: 'Rohan',
      business_name: 'StyleStudio Salon',
      discount: '25% OFF',
      valid_till: 'Sunday',
      business_phone: '+91 98765 43210'
    },
    text: '{business_name}: Hi {customer_name}! Enjoy {discount} on hair spa & grooming this {valid_till}. Book slot at {business_phone}. Reply STOP to opt out.'
  },
  {
    id: 'festival-greeting',
    title: 'Festive Greeting (English)',
    category: 'festival',
    categoryLabel: 'Festivals & Occasions',
    type: 'Opt-In Promotional',
    description: 'Warm seasonal greeting for Diwali, Eid, New Year, or Christmas with business signature.',
    sampleVariables: {
      customer_name: 'Suresh & Family',
      business_name: 'Kothari Jewelers',
      festival: 'Diwali',
      owner_name: 'Ramesh Kothari'
    },
    text: 'Wishing {customer_name} a very Happy and Prosperous {festival}! May this season bring joy and abundance. Warm regards, {owner_name} & Team {business_name}.'
  },
  {
    id: 'festival-hindi-unicode',
    title: 'Festive Greeting (Hindi Unicode / Devanagari)',
    category: 'festival',
    categoryLabel: 'Regional & Bilingual',
    type: 'Opt-In Promotional',
    description: 'Demonstrates UCS-2 Unicode encoding (70 chars per segment) for Devanagari Hindi text.',
    sampleVariables: {
      customer_name: 'रमेश जी',
      business_name: 'अग्रवाल स्वीट्स',
      owner_name: 'संजय अग्रवाल'
    },
    isUnicode: true,
    text: 'नमस्ते {customer_name}, {business_name} की ओर से आपको एवं आपके परिवार को दीपावली की हार्दिक शुभकामनाएं। - {owner_name}'
  },
  {
    id: 'custom-template',
    title: 'Custom Follow-Up & Feedback Request',
    category: 'custom',
    categoryLabel: 'Custom & Ad-Hoc',
    type: 'Transactional',
    description: 'Custom inquiry asking for honest Google review or feedback on a recent visit.',
    sampleVariables: {
      customer_name: 'Ananya',
      business_name: 'GreenWood Dental',
      review_shortlink: 'g.page/gw-dental/review'
    },
    text: 'Hi {customer_name}, how was your experience today at {business_name}? If satisfied, please leave us a 5-star review at {review_shortlink}. Thank you!'
  }
];

export function calculateSmsMetrics(content: string) {
  // Check if string contains any non-GSM-7 characters
  // Basic GSM 7-bit default alphabet characters
  const gsm7Regex = /^[@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ\x1bÆæßÉ !\"#¤%&'()*+,\-.\/0-9:;<=>?¡A-ZÄÖÑÜ§¿a-zäöñüà\^{}\\[~\]|\\]*$/;
  
  const isUnicode = !gsm7Regex.test(content);
  const charLength = Array.from(content).length; // Proper Unicode code point count
  
  let segments = 0;
  let maxPerSingle = isUnicode ? 70 : 160;
  let maxPerConcat = isUnicode ? 67 : 153;

  if (charLength === 0) {
    segments = 0;
  } else if (charLength <= maxPerSingle) {
    segments = 1;
  } else {
    segments = Math.ceil(charLength / maxPerConcat);
  }

  const remainingInSegment = segments === 0 
    ? maxPerSingle 
    : segments === 1 
      ? maxPerSingle - charLength 
      : (segments * maxPerConcat) - charLength;

  return {
    isUnicode,
    charLength,
    charCount: charLength,
    segments,
    remainingInSegment,
    maxPerSingle,
    maxPerConcat,
    encoding: isUnicode ? 'UCS-2 (Unicode)' : 'GSM-7'
  };
}
