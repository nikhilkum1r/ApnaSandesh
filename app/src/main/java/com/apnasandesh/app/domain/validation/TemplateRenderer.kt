package com.apnasandesh.app.domain.validation

import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.model.TemplateVariable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRenderer @Inject constructor() {

    companion object {
        val ALL_VARIABLES: List<TemplateVariable> = listOf(
            // Customer Information
            TemplateVariable(
                token = "{{customer_name}}",
                displayName = "Full Name",
                description = "Customer's full name (e.g. Aarav Sharma)",
                sampleValue = "Aarav Sharma",
                group = "Customer"
            ),
            TemplateVariable(
                token = "{{first_name}}",
                displayName = "First Name",
                description = "Customer's first name (e.g. Aarav)",
                sampleValue = "Aarav",
                group = "Customer"
            ),
            TemplateVariable(
                token = "{{last_name}}",
                displayName = "Last Name",
                description = "Customer's last name (e.g. Sharma)",
                sampleValue = "Sharma",
                group = "Customer"
            ),
            TemplateVariable(
                token = "{{customer_phone}}",
                displayName = "Customer Phone",
                description = "Customer's phone number",
                sampleValue = "+91 98765 43210",
                group = "Customer"
            ),
            TemplateVariable(
                token = "{{customer_email}}",
                displayName = "Customer Email",
                description = "Customer's email address",
                sampleValue = "aarav@example.com",
                group = "Customer"
            ),
            TemplateVariable(
                token = "{{customer_company}}",
                displayName = "Customer Company",
                description = "Customer's company or workplace",
                sampleValue = "Sharma Traders",
                group = "Customer"
            ),

            // Business Information
            TemplateVariable(
                token = "{{business_name}}",
                displayName = "Business Name",
                description = "Registered name of your business",
                sampleValue = "Apna General Store",
                group = "Business"
            ),
            TemplateVariable(
                token = "{{owner_name}}",
                displayName = "Owner Name",
                description = "Owner or contact person display name",
                sampleValue = "Shop Manager",
                group = "Business"
            ),
            TemplateVariable(
                token = "{{business_phone}}",
                displayName = "Official Phone",
                description = "Callback phone number for queries",
                sampleValue = "+91 98765 00000",
                group = "Business"
            ),
            TemplateVariable(
                token = "{{business_address}}",
                displayName = "Business Address",
                description = "Store location or pickup address",
                sampleValue = "Shop #4, Market Complex",
                group = "Business"
            ),
            TemplateVariable(
                token = "{{business_website}}",
                displayName = "Website",
                description = "Store or business website URL",
                sampleValue = "www.mybiz.com",
                group = "Business"
            ),
            TemplateVariable(
                token = "{{business_signature}}",
                displayName = "Business Signature",
                description = "Default automated closing signature",
                sampleValue = "- Apna General Store",
                group = "Business"
            ),
            TemplateVariable(
                token = "{{signature}}",
                displayName = "Signature",
                description = "Closing signature",
                sampleValue = "- Apna General Store",
                group = "Business"
            ),

            // Sales & Products
            TemplateVariable(
                token = "{{product_name}}",
                displayName = "Product Name",
                description = "Product or item name",
                sampleValue = "Smart Watch Pro",
                group = "Sales"
            ),
            TemplateVariable(
                token = "{{service_name}}",
                displayName = "Service Name",
                description = "Service or package name",
                sampleValue = "Full AC Servicing",
                group = "Sales"
            ),
            TemplateVariable(
                token = "{{discount_percentage}}",
                displayName = "Discount %",
                description = "Discount percentage number (e.g. 15)",
                sampleValue = "15",
                group = "Sales"
            ),
            TemplateVariable(
                token = "{{coupon_code}}",
                displayName = "Coupon Code",
                description = "Promo or discount voucher code",
                sampleValue = "SAVE15",
                group = "Sales"
            ),
            TemplateVariable(
                token = "{{offer_expiry_date}}",
                displayName = "Offer Expiry",
                description = "Expiry date of promotion",
                sampleValue = "Sunday",
                group = "Sales"
            ),

            // Orders & Payments
            TemplateVariable(
                token = "{{order_id}}",
                displayName = "Order ID",
                description = "Order reference or bill number",
                sampleValue = "ORD-4821",
                group = "Orders"
            ),
            TemplateVariable(
                token = "{{order_date}}",
                displayName = "Order Date",
                description = "Date order was placed",
                sampleValue = "22 Oct 2026",
                group = "Orders"
            ),
            TemplateVariable(
                token = "{{invoice_number}}",
                displayName = "Invoice #",
                description = "Tax or bill invoice number",
                sampleValue = "INV-2026-99",
                group = "Orders"
            ),
            TemplateVariable(
                token = "{{invoice_amount}}",
                displayName = "Invoice Amount",
                description = "Total bill amount with currency",
                sampleValue = "₹1,250",
                group = "Orders"
            ),
            TemplateVariable(
                token = "{{payment_due_date}}",
                displayName = "Payment Due Date",
                description = "Last date to settle invoice",
                sampleValue = "28 Oct 2026",
                group = "Orders"
            ),
            TemplateVariable(
                token = "{{payment_status}}",
                displayName = "Payment Status",
                description = "Current payment state (e.g. Received / Pending)",
                sampleValue = "Pending",
                group = "Orders"
            ),
            TemplateVariable(
                token = "{{payment_link}}",
                displayName = "Payment Link",
                description = "Online UPI or gateway payment URL",
                sampleValue = "https://pay.biz/pay",
                group = "Orders"
            ),

            // Appointments & Services
            TemplateVariable(
                token = "{{appointment_date}}",
                displayName = "Appointment Date",
                description = "Date of scheduled appointment or service",
                sampleValue = "23 Oct 2026",
                group = "Appointments"
            ),
            TemplateVariable(
                token = "{{appointment_time}}",
                displayName = "Appointment Time",
                description = "Scheduled appointment time",
                sampleValue = "11:30 AM",
                group = "Appointments"
            ),
            TemplateVariable(
                token = "{{booking_id}}",
                displayName = "Booking ID",
                description = "Booking or token reservation number",
                sampleValue = "BKG-771",
                group = "Appointments"
            ),
            TemplateVariable(
                token = "{{service_provider}}",
                displayName = "Service Provider",
                description = "Doctor, stylist, technician, or staff member",
                sampleValue = "Dr. Sharma",
                group = "Appointments"
            ),
            TemplateVariable(
                token = "{{service_status}}",
                displayName = "Service Status",
                description = "Status of job (e.g. Ready for Pickup)",
                sampleValue = "Ready for Pickup",
                group = "Appointments"
            ),

            // General & Custom Fields
            TemplateVariable(
                token = "{{current_date}}",
                displayName = "Current Date",
                description = "Today's date",
                sampleValue = "22 Oct 2026",
                group = "General"
            ),
            TemplateVariable(
                token = "{{current_time}}",
                displayName = "Current Time",
                description = "Current time",
                sampleValue = "10:00 AM",
                group = "General"
            ),
            TemplateVariable(
                token = "{{custom_field_1}}",
                displayName = "Custom Field 1",
                description = "User-defined custom customer field 1",
                sampleValue = "Silver Member",
                group = "General"
            ),
            TemplateVariable(
                token = "{{custom_field_2}}",
                displayName = "Custom Field 2",
                description = "User-defined custom customer field 2",
                sampleValue = "Zone B",
                group = "General"
            )
        )
    }

    private val extraSupportedAliases = setOf(
        "name",
        "full_name",
        "phone",
        "phone_number",
        "address",
        "category",
        "business_category",
        "notes",
        "tags"
    )

    private val supportedTokens = (ALL_VARIABLES.map {
        it.token.removePrefix("{{").removeSuffix("}}")
    }.toSet() + extraSupportedAliases)

    private val doubleBraceRegex = Regex("\\{\\{([a-zA-Z0-9_]+)\\}\\}")
    private val singleBraceRegex = Regex("\\{([a-zA-Z0-9_]+)\\}")

    /**
     * Finds any unresolved tokens in the template that are not in the supported tokens list.
     */
    fun validateTemplateTokens(rawTemplate: String): List<String> {
        val foundDouble = doubleBraceRegex.findAll(rawTemplate).map { it.groupValues[1] }
        val foundSingle = singleBraceRegex.findAll(rawTemplate).map { it.groupValues[1] }
        val allFound = (foundDouble + foundSingle).toSet()
        return allFound.filter { it !in supportedTokens }.map { "{{$it}}" }
    }

    /**
     * Finds placeholders present in the template that lack values for a specific recipient.
     */
    fun findMissingVariables(
        rawTemplate: String,
        customer: CustomerEntity,
        profile: BusinessProfileEntity,
        contextOverrides: Map<String, String> = emptyMap()
    ): List<String> {
        val foundDouble = doubleBraceRegex.findAll(rawTemplate).map { it.groupValues[1] }
        val foundSingle = singleBraceRegex.findAll(rawTemplate).map { it.groupValues[1] }
        val allFound = (foundDouble + foundSingle).toSet()

        val missing = mutableListOf<String>()
        for (token in allFound) {
            val value = resolveToken(token, customer, profile, contextOverrides)
            if (value.isBlank()) {
                missing.add("{{$token}}")
            }
        }
        return missing
    }

    /**
     * Resolves all placeholders for a specific recipient, applying sensible defaults.
     */
    fun render(
        rawTemplate: String,
        customer: CustomerEntity,
        profile: BusinessProfileEntity,
        appendSignature: Boolean = true,
        appendOptOutFooter: Boolean = false,
        optOutFooterText: String = "Reply STOP to opt out.",
        contextOverrides: Map<String, String> = emptyMap()
    ): String {
        var rendered = rawTemplate

        // First resolve double braces {{...}}
        rendered = doubleBraceRegex.replace(rendered) { matchResult ->
            val tokenName = matchResult.groupValues[1]
            resolveToken(tokenName, customer, profile, contextOverrides).ifEmpty {
                getDefaultFallback(tokenName, profile)
            }
        }

        // Then resolve single braces {...} for backwards compatibility
        rendered = singleBraceRegex.replace(rendered) { matchResult ->
            val tokenName = matchResult.groupValues[1]
            if (tokenName in supportedTokens) {
                resolveToken(tokenName, customer, profile, contextOverrides).ifEmpty {
                    getDefaultFallback(tokenName, profile)
                }
            } else {
                matchResult.value
            }
        }.trim()

        val safeSignature = profile.defaultMessageSignature.trim()
        if (appendSignature && safeSignature.isNotEmpty() && !rendered.contains(safeSignature)) {
            rendered = "$rendered\n$safeSignature"
        }

        if (appendOptOutFooter && optOutFooterText.isNotBlank()) {
            rendered = "$rendered\n${optOutFooterText.trim()}"
        }

        return rendered
    }

    private fun resolveToken(
        token: String,
        customer: CustomerEntity,
        profile: BusinessProfileEntity,
        overrides: Map<String, String>
    ): String {
        // Priority to explicit context overrides if supplied (e.g. campaign-wide discount code)
        overrides[token]?.let { return it }

        val nameParts = customer.fullName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val firstName = nameParts.firstOrNull() ?: ""
        val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

        val currency = profile.currencySymbol.ifBlank { "₹" }

        return when (token) {
            // Customer
            "customer_name", "name", "full_name" -> customer.fullName.trim().ifEmpty { "Valued Customer" }
            "first_name" -> firstName.ifEmpty { "Valued Customer" }
            "last_name" -> lastName
            "customer_phone", "phone", "phone_number" -> customer.rawPhoneNumber.ifBlank { customer.normalizedPhoneE164 }
            "customer_email" -> customer.email
            "customer_company" -> customer.company
            "notes" -> customer.notes.trim()
            "tags" -> customer.tagsCsv.trim()

            // Business
            "business_name" -> profile.businessName.trim().ifEmpty { "Our Store" }
            "owner_name" -> profile.ownerDisplayName.trim().ifEmpty { profile.businessName.trim().ifEmpty { "Store Manager" } }
            "business_phone" -> profile.communicationPhone.trim()
            "business_address", "address" -> profile.businessAddress?.trim().orEmpty()
            "business_category", "category" -> profile.businessCategory.trim()
            "business_website" -> profile.website.trim()
            "business_signature", "signature" -> profile.defaultMessageSignature.trim()

            // Sales & Products
            "product_name" -> customer.lastServiceName.ifBlank { "our product" }
            "service_name" -> customer.lastServiceName.ifBlank { "our service" }
            "discount_percentage" -> "15"
            "coupon_code" -> "SAVE15"
            "offer_expiry_date" -> getFormattedDateOffset(daysOffset = 3)

            // Orders & Payments
            "order_id" -> customer.lastOrderId.ifBlank { "ORD-101" }
            "order_date" -> customer.lastOrderDate.ifBlank { getFormattedTodayDate() }
            "invoice_number" -> customer.lastOrderId.ifBlank { "INV-101" }
            "invoice_amount" -> {
                val amt = customer.lastOrderAmount.trim()
                when {
                    amt.isEmpty() -> "${currency}500"
                    amt.startsWith(currency) || amt.startsWith("₹") || amt.startsWith("$") -> amt
                    else -> "$currency$amt"
                }
            }
            "payment_due_date" -> getFormattedDateOffset(daysOffset = 5)
            "payment_status" -> "Pending"
            "payment_link" -> if (profile.website.isNotBlank()) "${profile.website.trim()}/pay" else "https://pay.biz/order"

            // Appointments & Services
            "appointment_date" -> customer.lastAppointmentDate.ifBlank { getFormattedDateOffset(daysOffset = 1) }
            "appointment_time" -> customer.lastAppointmentTime.ifBlank { "11:00 AM" }
            "booking_id" -> customer.lastOrderId.ifBlank { "BKG-202" }
            "service_provider" -> profile.ownerDisplayName.ifBlank { profile.businessName }
            "service_status" -> "Confirmed"

            // General & Custom
            "current_date" -> getFormattedTodayDate()
            "current_time" -> getFormattedCurrentTime()
            "custom_field_1" -> customer.customField1
            "custom_field_2" -> customer.customField2

            else -> ""
        }
    }

    private fun getDefaultFallback(token: String, profile: BusinessProfileEntity): String {
        return when (token) {
            "customer_name", "first_name" -> "Valued Customer"
            "business_name" -> profile.businessName.ifEmpty { "Our Business" }
            "owner_name" -> profile.ownerDisplayName.ifEmpty { "Store Manager" }
            "business_phone" -> profile.communicationPhone
            "discount_percentage" -> "10"
            "coupon_code" -> "SPECIAL"
            "current_date" -> getFormattedTodayDate()
            else -> ""
        }
    }

    private fun getFormattedTodayDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getFormattedCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getFormattedDateOffset(daysOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(cal.time)
    }
}
