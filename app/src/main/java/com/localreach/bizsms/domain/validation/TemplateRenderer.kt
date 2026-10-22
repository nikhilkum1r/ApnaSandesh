package com.localreach.bizsms.domain.validation

import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CustomerEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRenderer @Inject constructor() {

    private val supportedTokens = setOf(
        "customer_name",
        "business_name",
        "owner_name",
        "business_phone",
        "signature"
    )

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
     * Resolves the template placeholders with safe fallbacks.
     */
    fun render(
        rawTemplate: String,
        customer: CustomerEntity,
        profile: BusinessProfileEntity,
        appendSignature: Boolean,
        appendOptOutFooter: Boolean = false,
        optOutFooterText: String = "Reply STOP to opt out."
    ): String {
        val safeCustomerName = customer.fullName.trim().ifEmpty { "Valued Customer" }
        val safeBusinessName = profile.businessName.trim().ifEmpty { "Our Store" }
        val safeOwnerName = profile.ownerDisplayName.trim().ifEmpty { safeBusinessName }
        val safeBusinessPhone = profile.communicationPhone.trim()
        val safeSignature = profile.defaultMessageSignature.trim()

        var rendered = rawTemplate
            // Double braces {{...}}
            .replace("{{customer_name}}", safeCustomerName)
            .replace("{{business_name}}", safeBusinessName)
            .replace("{{owner_name}}", safeOwnerName)
            .replace("{{business_phone}}", safeBusinessPhone)
            .replace("{{signature}}", safeSignature)
            // Single braces {...} compatibility
            .replace("{customer_name}", safeCustomerName)
            .replace("{business_name}", safeBusinessName)
            .replace("{owner_name}", safeOwnerName)
            .replace("{business_phone}", safeBusinessPhone)
            .replace("{signature}", safeSignature)
            .trim()

        if (appendSignature && safeSignature.isNotEmpty() && !rendered.contains(safeSignature)) {
            rendered = "$rendered\n$safeSignature"
        }

        if (appendOptOutFooter && optOutFooterText.isNotBlank()) {
            rendered = "$rendered\n${optOutFooterText.trim()}"
        }

        return rendered
    }
}
