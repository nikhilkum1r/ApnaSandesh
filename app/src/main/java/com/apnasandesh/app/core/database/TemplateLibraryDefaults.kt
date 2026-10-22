package com.apnasandesh.app.core.database

import com.apnasandesh.app.core.model.TemplateCategory
import com.apnasandesh.app.core.model.TemplateLanguage
import com.apnasandesh.app.core.model.TemplateTone

object TemplateLibraryDefaults {

    fun getDefaultTemplates(): List<MessageTemplateEntity> = listOf(
        // ==========================================
        // CATEGORY A: Greetings & Customer Relationships
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_greet_welcome",
            title = "Welcome New Customer",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "Welcome {{first_name}} to {{business_name}}! We are thrilled to have you with us. If you ever need anything, call us at {{business_phone}}.",
            description = "Warm welcome for newly registered customers",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = false,
            isSystemDefault = true,
            isFavorite = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_greet_thankyou",
            title = "Customer Thank You",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "Thank you {{first_name}} for shopping at {{business_name}}! We appreciate your trust in us and look forward to serving you again soon.",
            description = "Appreciation message after in-store or online visit",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Retail Shops"
        ),
        MessageTemplateEntity(
            id = "tpl_greet_birthday",
            title = "Birthday Wishes & Gift",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "Wishing you a very Happy Birthday {{first_name}}! May your year ahead be full of happiness. Visit {{business_name}} this week to claim your birthday surprise gift!",
            description = "Personalized birthday greeting with special gift",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_greet_anniversary",
            title = "Anniversary Greeting",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "Happy Anniversary {{first_name}}! Wishing you joy on your special day. From all of us at {{business_name}}.",
            description = "Warm anniversary wishes to loyal clients",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_greet_diwali_hinglish",
            title = "Diwali Greetings (Hinglish)",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "Aapko aur aapke poore parivar ko {{business_name}} ki taraf se Happy Diwali! Wishing you health, wealth aur prosperity.",
            description = "Festive Diwali message in friendly Hinglish",
            language = TemplateLanguage.HINGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = true,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Retail Shops"
        ),
        MessageTemplateEntity(
            id = "tpl_greet_festival_hindi",
            title = "Festival Greetings (Hindi)",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "नमस्ते {{first_name}} जी, {{business_name}} की तरफ से आपको और आपके परिवार को त्योहार की हार्दिक शुभकामनाएं! आपका जीवन सुख-समृद्धि से भरा रहे।",
            description = "Devanagari Hindi festive greetings",
            language = TemplateLanguage.HINDI.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_greet_customer_appreciation",
            title = "Customer Appreciation",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "Dear {{first_name}}, as a valued patron of {{business_name}}, we wanted to thank you for your continued loyalty. You make our journey rewarding!",
            description = "Appreciation note for long-time customers",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PREMIUM.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_greet_reengagement",
            title = "Customer Re-Engagement",
            category = TemplateCategory.GREETINGS_RELATIONSHIPS.name,
            bodyTemplate = "Hi {{first_name}}, we haven't seen you at {{business_name}} in a while! Drop by this week for new arrivals and a special welcome back discount.",
            description = "Reach out to customers who haven't visited lately",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Retail Shops"
        ),

        // ==========================================
        // CATEGORY B: Marketing & Promotions
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_promo_new_product",
            title = "New Product Announcement",
            category = TemplateCategory.MARKETING_PROMOTIONS.name,
            bodyTemplate = "Exciting news {{first_name}}! We just launched our brand new {{product_name}} at {{business_name}}. Visit us today or call {{business_phone}} to reserve yours.",
            description = "Announce fresh stock and product releases",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROMOTIONAL.displayName,
            isPromotional = true,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Electronics Shops"
        ),
        MessageTemplateEntity(
            id = "tpl_promo_discount_offer",
            title = "Special Discount Offer",
            category = TemplateCategory.MARKETING_PROMOTIONS.name,
            bodyTemplate = "Namaste {{first_name}}! Enjoy {{discount_percentage}}% off on your next visit to {{business_name}}. Use code {{coupon_code}} before {{offer_expiry_date}}.",
            description = "Promotional percentage discount with promo code",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROMOTIONAL.displayName,
            isPromotional = true,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Clothing Stores"
        ),
        MessageTemplateEntity(
            id = "tpl_promo_seasonal_sale",
            title = "Seasonal Sale Alert",
            category = TemplateCategory.MARKETING_PROMOTIONS.name,
            bodyTemplate = "Mega Seasonal Sale is LIVE at {{business_name}}! Up to {{discount_percentage}}% off storewide for {{first_name}}. Hurry, stock is limited!",
            description = "Seasonal and festival clearance announcements",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROMOTIONAL.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Retail Shops"
        ),
        MessageTemplateEntity(
            id = "tpl_promo_loyalty_rewards",
            title = "Loyalty Points Reward",
            category = TemplateCategory.MARKETING_PROMOTIONS.name,
            bodyTemplate = "Hello {{first_name}}, you have accumulated loyalty reward points at {{business_name}}! Redeem them on your next bill for instant savings.",
            description = "Remind members to redeem loyalty points",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Grocery Stores"
        ),
        MessageTemplateEntity(
            id = "tpl_promo_referral",
            title = "Refer a Friend Offer",
            category = TemplateCategory.MARKETING_PROMOTIONS.name,
            bodyTemplate = "Hi {{first_name}}, love our services at {{business_name}}? Refer a friend and both of you get {{discount_percentage}}% off your next invoice!",
            description = "Customer referral incentive campaign",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Salons & Beauty"
        ),
        MessageTemplateEntity(
            id = "tpl_promo_store_opening",
            title = "Store Opening Announcement",
            category = TemplateCategory.MARKETING_PROMOTIONS.name,
            bodyTemplate = "We are expanding! {{business_name}} is pleased to invite {{first_name}} to our new branch opening at {{business_address}}. Exclusive treats await you!",
            description = "Grand opening of new shop or relocated branch",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Restaurants & Cafés"
        ),

        // ==========================================
        // CATEGORY C: Sales & Lead Follow-ups
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_sales_inquiry_followup",
            title = "New Inquiry Follow-up",
            category = TemplateCategory.SALES_LEADS.name,
            bodyTemplate = "Hello {{first_name}}, thank you for inquiring about {{service_name}} with {{business_name}}. I am available to answer any questions or share a quote. Best, {{owner_name}}.",
            description = "Prompt response to product or service inquiry",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            isFavorite = true,
            industry = "B2B Companies"
        ),
        MessageTemplateEntity(
            id = "tpl_sales_quote_followup",
            title = "Quotation Follow-up",
            category = TemplateCategory.SALES_LEADS.name,
            bodyTemplate = "Hi {{first_name}}, following up on the quotation sent for {{product_name}} from {{business_name}}. Please let us know if you would like any modifications. Contact {{business_phone}}.",
            description = "Check status of previously shared estimate",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Freelancers & Consultants"
        ),
        MessageTemplateEntity(
            id = "tpl_sales_demo_invite",
            title = "Demo / Consultation Invitation",
            category = TemplateCategory.SALES_LEADS.name,
            bodyTemplate = "Dear {{first_name}}, would you like a quick walkthrough of {{service_name}}? Book a free consultation slot with {{business_name}} at {{business_phone}}.",
            description = "Invite potential leads to a live demo or meeting",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Coaching & Education"
        ),
        MessageTemplateEntity(
            id = "tpl_sales_post_visit",
            title = "Post-Visit Follow-up",
            category = TemplateCategory.SALES_LEADS.name,
            bodyTemplate = "Hi {{first_name}}, thank you for visiting {{business_name}} today! It was a pleasure discussing your requirements. Feel free to call {{business_phone}} if you need further details.",
            description = "Follow-up after in-person visit or site consultation",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Real Estate Agents"
        ),

        // ==========================================
        // CATEGORY D: Orders, Invoices & Payments
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_order_confirm",
            title = "Order Confirmation",
            category = TemplateCategory.ORDERS_PAYMENTS.name,
            bodyTemplate = "Hello {{first_name}}, order #{{order_id}} has been confirmed by {{business_name}} on {{order_date}}. We are preparing it now and will notify you when ready.",
            description = "Instant confirmation after customer places order",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Retail Shops"
        ),
        MessageTemplateEntity(
            id = "tpl_order_delivery_out",
            title = "Out for Delivery Notice",
            category = TemplateCategory.ORDERS_PAYMENTS.name,
            bodyTemplate = "Hi {{first_name}}, your order #{{order_id}} from {{business_name}} is out for delivery today. Please keep your phone reachable at {{customer_phone}}.",
            description = "Alert customer that package/order is on the way",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.CONCISE.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Grocery Stores"
        ),
        MessageTemplateEntity(
            id = "tpl_order_invoice_notice",
            title = "Invoice Notification",
            category = TemplateCategory.ORDERS_PAYMENTS.name,
            bodyTemplate = "Dear {{first_name}}, invoice #{{invoice_number}} for {{invoice_amount}} from {{business_name}} has been generated. Due date is {{payment_due_date}}.",
            description = "Formal invoice bill notification",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "B2B Companies"
        ),
        MessageTemplateEntity(
            id = "tpl_payment_reminder",
            title = "Payment Gentle Reminder",
            category = TemplateCategory.ORDERS_PAYMENTS.name,
            bodyTemplate = "Dear {{first_name}}, gentle reminder from {{business_name}} regarding pending balance of {{invoice_amount}} for invoice #{{invoice_number}}. Please clear by {{payment_due_date}}. Ignore if paid.",
            description = "Polite reminder for pending customer balance",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            isFavorite = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_payment_confirmation",
            title = "Payment Received Confirmation",
            category = TemplateCategory.ORDERS_PAYMENTS.name,
            bodyTemplate = "Thank you {{first_name}}! We have successfully received payment of {{invoice_amount}} for order #{{order_id}}. {{business_name}} thanks you for your business.",
            description = "Acknowledge received UPI or cash settlement",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_payment_refund",
            title = "Refund Processed Update",
            category = TemplateCategory.ORDERS_PAYMENTS.name,
            bodyTemplate = "Hello {{first_name}}, a refund of {{invoice_amount}} for order #{{order_id}} has been initiated by {{business_name}}. It should reflect in your account within 2-3 business days.",
            description = "Inform customer regarding approved refund",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "E-commerce"
        ),

        // ==========================================
        // CATEGORY E: Appointments & Services
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_appointment_booked",
            title = "Booking Confirmation",
            category = TemplateCategory.APPOINTMENTS_SERVICES.name,
            bodyTemplate = "Hello {{first_name}}, your booking #{{booking_id}} for {{service_name}} with {{service_provider}} at {{business_name}} is confirmed for {{appointment_date}} at {{appointment_time}}.",
            description = "Confirmed booking details and provider info",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Salons & Beauty"
        ),
        MessageTemplateEntity(
            id = "tpl_appointment_reminder",
            title = "Appointment Reminder (24h)",
            category = TemplateCategory.APPOINTMENTS_SERVICES.name,
            bodyTemplate = "Hi {{first_name}}, friendly reminder of your appointment at {{business_name}} tomorrow, {{appointment_date}} at {{appointment_time}}. To reschedule, call {{business_phone}}.",
            description = "Prevent no-shows with advance appointment alert",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = false,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Clinics & Wellness"
        ),
        MessageTemplateEntity(
            id = "tpl_appointment_rescheduled",
            title = "Reschedule Notification",
            category = TemplateCategory.APPOINTMENTS_SERVICES.name,
            bodyTemplate = "Dear {{first_name}}, your appointment at {{business_name}} has been rescheduled to {{appointment_date}} at {{appointment_time}}. Reach us at {{business_phone}} for assistance.",
            description = "Update customer on changed appointment timing",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Clinics & Wellness"
        ),
        MessageTemplateEntity(
            id = "tpl_service_completion",
            title = "Service / Repair Ready for Pickup",
            category = TemplateCategory.APPOINTMENTS_SERVICES.name,
            bodyTemplate = "Hello {{first_name}}, your {{product_name}} is ready for pickup at {{business_name}}! All requested service has been completed. Store address: {{business_address}}.",
            description = "Notify customer that device or garment is ready",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = false,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Repair & Maintenance"
        ),
        MessageTemplateEntity(
            id = "tpl_service_feedback",
            title = "Customer Feedback Request",
            category = TemplateCategory.APPOINTMENTS_SERVICES.name,
            bodyTemplate = "Hi {{first_name}}, how was your recent experience with {{service_name}} at {{business_name}}? Your feedback helps us improve. Reply directly or call {{business_phone}}.",
            description = "Solicit honest customer feedback and reviews",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Salons & Beauty"
        ),

        // ==========================================
        // CATEGORY F: Customer Retention
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_retention_review",
            title = "Google / Map Review Request",
            category = TemplateCategory.CUSTOMER_RETENTION.name,
            bodyTemplate = "Hi {{first_name}}, thank you for choosing {{business_name}}! If you loved our service, would you take 30 seconds to leave us a 5-star review? We appreciate your support!",
            description = "Drive authentic Google Maps and online ratings",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = true,
            isSystemDefault = true,
            isFavorite = true,
            industry = "Local Services"
        ),
        MessageTemplateEntity(
            id = "tpl_retention_winback",
            title = "Win-Back Special Offer",
            category = TemplateCategory.CUSTOMER_RETENTION.name,
            bodyTemplate = "We miss you {{first_name}}! Come back to {{business_name}} this month and enjoy a flat {{discount_percentage}}% discount using code {{coupon_code}}.",
            description = "Re-activate dormant customers with incentive",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Retail Shops"
        ),
        MessageTemplateEntity(
            id = "tpl_retention_renewal",
            title = "Service Renewal Reminder",
            category = TemplateCategory.CUSTOMER_RETENTION.name,
            bodyTemplate = "Dear {{first_name}}, your maintenance contract with {{business_name}} expires soon. Renew before {{offer_expiry_date}} to keep uninterrupted service.",
            description = "Annual maintenance or subscription renewal notice",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Repair & Maintenance"
        ),

        // ==========================================
        // CATEGORY G: Industry-Specific Templates
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_ind_grocery_stock",
            title = "Grocery Fresh Stock Alert",
            category = TemplateCategory.INDUSTRY_SPECIFIC.name,
            bodyTemplate = "Fresh seasonal fruits, vegetables, and pantry essentials have arrived at {{business_name}}, {{first_name}}! Home delivery available: {{business_phone}}.",
            description = "Weekly or daily fresh arrivals for grocery stores",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.FRIENDLY.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Grocery Stores"
        ),
        MessageTemplateEntity(
            id = "tpl_ind_restaurant_table",
            title = "Restaurant Table Booking",
            category = TemplateCategory.INDUSTRY_SPECIFIC.name,
            bodyTemplate = "Namaste {{first_name}}! Your table reservation at {{business_name}} is confirmed for {{appointment_date}} at {{appointment_time}}. We look forward to hosting you!",
            description = "Dining reservation confirmation for restaurants & cafés",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Restaurants & Cafés"
        ),
        MessageTemplateEntity(
            id = "tpl_ind_coaching_class",
            title = "Coaching Institute Batch Notice",
            category = TemplateCategory.INDUSTRY_SPECIFIC.name,
            bodyTemplate = "Dear {{first_name}}, new batch for {{service_name}} at {{business_name}} commences on {{appointment_date}}. Limited seats. Contact {{business_phone}} to enroll.",
            description = "Class schedules and new batch alerts for institutes",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Coaching & Education"
        ),
        MessageTemplateEntity(
            id = "tpl_ind_auto_repair",
            title = "Vehicle Service Due Alert",
            category = TemplateCategory.INDUSTRY_SPECIFIC.name,
            bodyTemplate = "Hello {{first_name}}, regular vehicle service is due for your vehicle at {{business_name}}. Book a slot today at {{business_phone}} for free pick-up and drop.",
            description = "Periodic car/bike maintenance notification",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = true,
            isSystemDefault = true,
            industry = "Repair & Maintenance"
        ),
        MessageTemplateEntity(
            id = "tpl_ind_clinic_prescription",
            title = "Clinic Follow-up & Checkup",
            category = TemplateCategory.INDUSTRY_SPECIFIC.name,
            bodyTemplate = "Dear {{first_name}}, this is a follow-up check from {{business_name}}. Please remember your routine check-up with {{service_provider}} on {{appointment_date}}.",
            description = "Patient health follow-up and reminder",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.WARM.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Clinics & Wellness"
        ),

        // ==========================================
        // CATEGORY H: Operational & Announcements
        // ==========================================
        MessageTemplateEntity(
            id = "tpl_ops_holiday",
            title = "Store Holiday Notice",
            category = TemplateCategory.OPERATIONAL.name,
            bodyTemplate = "Notice for {{first_name}}: {{business_name}} will remain closed on {{current_date}} on account of festival holiday. Normal store hours resume tomorrow.",
            description = "Holiday schedule or unexpected day-off update",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.CONCISE.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "General Business"
        ),
        MessageTemplateEntity(
            id = "tpl_ops_hours_change",
            title = "Updated Store Hours",
            category = TemplateCategory.OPERATIONAL.name,
            bodyTemplate = "Dear {{first_name}}, please note our updated store hours at {{business_name}}: open daily 9:00 AM to 9:30 PM. For queries, contact {{business_phone}}.",
            description = "Announcement regarding changed business timings",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.PROFESSIONAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "Retail Shops"
        ),
        MessageTemplateEntity(
            id = "tpl_ops_custom_blank",
            title = "Blank Custom Template",
            category = TemplateCategory.OPERATIONAL.name,
            bodyTemplate = "Hello {{first_name}}, update from {{business_name}}: ",
            description = "Flexible starter template for custom business notes",
            language = TemplateLanguage.ENGLISH.displayName,
            tone = TemplateTone.CASUAL.displayName,
            isPromotional = false,
            isSystemDefault = true,
            industry = "General Business"
        )
    )
}
