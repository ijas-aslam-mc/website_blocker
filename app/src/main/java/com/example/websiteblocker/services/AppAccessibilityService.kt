package com.example.websiteblocker.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.websiteblocker.activity.AccessDenied
import com.example.websiteblocker.model.SupportedBrowserModel
import com.example.websiteblocker.utils.Constants
import com.example.websiteblocker.utils.Constants.Companion.BROWSER_PACKAGE
import com.example.websiteblocker.utils.Constants.Companion.blackList
import java.text.SimpleDateFormat
import java.util.*

class AppAccessibilityService : AccessibilityService() {
    private val previousUrlDetections = HashMap<String, Long>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.packageNames = packageNames()
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.notificationTimeout = 300
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        var parentNodeInfo: AccessibilityNodeInfo? = p0?.source ?: return

        var packageName : String = p0.packageName.toString()
        var browserConfig : SupportedBrowserModel? = null

        for (supportedConfig in getSupportedBrowsers()){
            if (supportedConfig.packageName == packageName){
                browserConfig = supportedConfig
            }
        }

        var detectedURL : String? = getUrl(parentNodeInfo!!, browserConfig!!)
        parentNodeInfo.recycle();

        if(detectedURL == null){
            return
        }
        //Log.d(">>>>>>>>> Url", detectedURL)

        val eventTime: Long = p0.eventTime
        val detectionId = "$packageName, and url $detectedURL"
        //noinspection ConstantConditions
        val lastRecordedTime: Long =
            if (previousUrlDetections.containsKey(detectionId)) previousUrlDetections[detectionId]!! else 0
        //some kind of redirect throttling
        if (eventTime - lastRecordedTime > 2000) {
            previousUrlDetections[detectionId] = eventTime
            //check any schedule here
            var enabledSchedule  : Boolean = SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.getBooleanValue(Constants.ENB_SCHEDULE)
            if(enabledSchedule){
                //yes enabled
                //check schedule time
                var strTime = SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.getValue(Constants.START_TIME)
                var edTime = SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.getValue(Constants.END_TIME)

                //format date format
                val dateFormat = SimpleDateFormat("h:mm a")
                var startTime = dateFormat.parse(strTime)
                var endTime = dateFormat.parse(edTime)

                //current time
                val currentTime = dateFormat.parse(getCurrentDate())

                if(currentTime.after(startTime) && currentTime.before(endTime)){
                    //schedule available this time
                    //check which mode selected
                    var mode : Boolean = SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.getBooleanValue(Constants.WHITE_MODE)

                    //analyze detected urls
                    if(detectedURL.startsWith("http")){
                        analyzeDetectedUrl(detectedURL, browserConfig.packageName, mode)
                    }
                }else{
                    //schedule not available this time
                }

            }else{
                Log.d(">>>>>>>>> ", "No schedules");
            }
        }


    }

    override fun onInterrupt() {
    }


    private fun analyzeDetectedUrl(detectedURL : String, browserPackage : String, mode : Boolean) {
        if(mode){
            //blacklist mode
            if(blackList.contains(detectedURL)) {
                performRedirect(browserPackage)
            }
        }else{
            //whitelist mode
            if(!blackList.contains(detectedURL) && !detectedURL.startsWith("https://parentry.app") && !detectedURL.startsWith("https://www.google.com/search?q")) {
                //redirect website
                performRedirect(browserPackage)
            }
        }
    }

    private fun performRedirect(browserPackage: String) {
        var intent  = Intent(this, AccessDenied::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK;
        intent.putExtra(BROWSER_PACKAGE, browserPackage)
        startActivity(intent);
    }

    private fun getCurrentDate():String{
        val sdf = SimpleDateFormat("h:mm a")
        return sdf.format(Date())
    }

    private fun getUrl(info : AccessibilityNodeInfo, config : SupportedBrowserModel) : String? {
        var nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId)
        if(nodes == null || nodes.size <= 0){
            return null
        }

        var addressBarNodeInfo : AccessibilityNodeInfo? = nodes[0]
        var url : String? = null
        if(addressBarNodeInfo?.text != null){
            url = addressBarNodeInfo.text.toString()
        }
        addressBarNodeInfo?.recycle()
        return url
    }


    private fun packageNames(): Array<String>? {
        val packageNames: MutableList<String> = ArrayList()
        for (config in getSupportedBrowsers()) {
            packageNames.add(config.packageName)
        }
        return packageNames.toTypedArray()
    }

    private fun getSupportedBrowsers(): List<SupportedBrowserModel> {
        val browsers: MutableList<SupportedBrowserModel> = ArrayList()
        browsers.add(
            SupportedBrowserModel(
                "com.android.chrome",
                "com.android.chrome:id/url_bar"
            )
        )
        browsers.add(
            SupportedBrowserModel(
                "org.mozilla.firefox",
                "org.mozilla.firefox:id/url_bar_title"
            )
        )
        return browsers
    }


}

