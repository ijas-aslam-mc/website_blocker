package com.example.websiteblocker.utils

class Constants {

    companion object {
        // accessibility permission result code
        val blackList : List<String> = listOf("https://m.facebook.com", "https://mobile.twitter.com/?lang=en", "https://www.instagram.com", "https://www.reddit.com", "https://9gag.com")
        const val BROWSER_PACKAGE : String =  "PACKAGE_FOR_BROWSER"
        const val REDIRECT_URL : String = "https://parentry.app"
        const val START_TIME = "start_time"
        const val END_TIME = "end_time"
        const val ENB_SCHEDULE = "enable_shedule"
        const val WHITE_MODE = "white_mode"

        const val BLACK_MODE_DESC = "In blacklist mode, the <b>websites in the list are blocked</b> but all other websites are allowed."
        const val WHITE_MODE_DESC = "In whitelist mode, the <b>websites in the list are allowed</b> but all other websites are blocked."
    }

}