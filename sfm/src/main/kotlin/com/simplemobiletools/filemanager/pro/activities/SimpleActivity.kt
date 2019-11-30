package com.simplemobiletools.filemanager.pro.activities

import com.secure.filemanager.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity

open class SimpleActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = arrayListOf(
            R.drawable.ic_sheild_folder
    )

    override fun getAppLauncherName() = getString(R.string.app_launcher_name)
}
