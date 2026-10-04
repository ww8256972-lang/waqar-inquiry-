package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiHelpService
import com.example.data.pdf.PdfGenerator
import com.example.data.security.SecurityUtils
import com.example.data.whatsapp.WhatsAppService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WAQAR WEBSITE INQUIRY", appName)
    }

    @Test
    fun testPasswordHashingAndVerification() {
        val rawPassword = "waqar"
        val hash = SecurityUtils.hashPassword(rawPassword)
        assertNotNull(hash)
        assertTrue(SecurityUtils.verifyPassword(rawPassword, hash))
        assertTrue(SecurityUtils.verifyPassword("Waqar", SecurityUtils.hashPassword("Waqar")))
        assertTrue(!SecurityUtils.verifyPassword("WrongPassword", hash))
    }

    @Test
    fun testRupeeMonetaryCalculations() {
        val subtotal = 10000.0
        val taxRate = 18.0
        val discount = 500.0
        val taxAmount = subtotal * (taxRate / 100.0)
        val grandTotal = subtotal + taxAmount - discount
        assertEquals(11300.0, grandTotal, 0.001)

        val amountPaid = 5000.0
        val remaining = (grandTotal - amountPaid).coerceAtLeast(0.0)
        assertEquals(6300.0, remaining, 0.001)

        val formatted = PdfGenerator.formatRupee(grandTotal)
        assertTrue(formatted.contains("₹") && formatted.contains("11,300"))
    }

    @Test
    fun testWhatsAppTemplateSubstitution() {
        val service = WhatsAppService(ApplicationProvider.getApplicationContext(), com.example.data.local.WaqarDatabase.getInstance(ApplicationProvider.getApplicationContext()).whatsAppDao())
        val rendered = service.renderMessage(
            template = "Hello {customer_name}, amount due: {remaining_amount}",
            customerName = "Rahul",
            remainingAmount = "₹2500"
        )
        assertEquals("Hello Rahul, amount due: ₹2500", rendered)
    }

    @Test
    fun testAiHelpOfflineService() {
        val inquiryHelp = AiHelpService.getScreenContextHelp("inquiry")
        assertNotNull(inquiryHelp)
        assertTrue(inquiryHelp.title.contains("Inquiry", ignoreCase = true))

        val searchResults = AiHelpService.searchHelp("record")
        assertTrue(searchResults.isNotEmpty())
    }
}
