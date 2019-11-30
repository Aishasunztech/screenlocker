package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.helpers.APP_FAQ
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.commons.models.FAQItem
import java.util.*

class FAQActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        val titleColor = getAdjustedPrimaryColor()
        val textColor = baseConfig.textColor

        val inflater = LayoutInflater.from(this)
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        faqItems.forEach {
            val faqItem = it
            inflater.inflate(R.layout.license_faq_item, null).apply {

            }
        }
    }
}
