package com.simplemobiletools.commons.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import kotlinx.android.synthetic.main.activity_about.*
import java.util.*

class AboutActivity : BaseSimpleActivity() {
    private var appName = ""
    private var linkColor = 0

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        linkColor = getAdjustedPrimaryColor()
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_holder)

        setupWebsite()
        setupEmail()
        setupFAQ()
        setupUpgradeToPro()
        setupMoreApps()
        setupRateUs()
        setupInvite()
        setupLicense()
        setupFacebook()
        setupReddit()
        setupCopyright()
    }

    private fun setupWebsite() {
        val websiteText = String.format(getString(R.string.two_string_placeholder), getString(R.string.website_label), getString(R.string.my_website))
    }

    private fun setupEmail() {
        val label = getString(R.string.email_label)
        val email = getString(R.string.my_email)

        val appVersion = String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
        val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
        val newline = "%0D%0A"
        val separator = "------------------------------"
        val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline$newline"
        val href = "$label<br><a href=\"mailto:$email?subject=$appName&body=$body\">$email</a>"


    }

    private fun setupFAQ() {


    }

    private fun setupUpgradeToPro() {

    }

    private fun openFAQ(faqItems: ArrayList<FAQItem>) {
        Intent(applicationContext, FAQActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            putExtra(APP_FAQ, faqItems)
            startActivity(this)
        }
    }

    private fun setupMoreApps() {

    }

    private fun setupInvite() {

    }

    private fun setupRateUs() {

    }

    private fun setupLicense() {

    }

    private fun setupFacebook() {
        about_facebook.setOnClickListener {
            var link = "https://www.facebook.com/simplemobiletools"
            try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                link = "fb://page/150270895341774"
            } catch (ignored: Exception) {
            }

            launchViewIntent(link)
        }
    }

    private fun setupReddit() {
        about_reddit.setOnClickListener {
            launchViewIntent("https://www.reddit.com/r/SimpleMobileTools")
        }
    }

    private fun setupCopyright() {

    }
}
