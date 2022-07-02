package com.example.websiteblocker.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Browser
import android.view.View
import android.widget.Toast
import com.example.websiteblocker.R
import com.example.websiteblocker.utils.Constants

class AccessDenied : AppCompatActivity() {
    private var browserPackage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_denied)
        getExtraDataFromIntent();
    }

    fun onBack(view: View) {
        redirectPage();
        finish();
    }

    override fun onDestroy() {
        super.onDestroy()
        redirectPage();
    }


    private fun getExtraDataFromIntent(){
        val bundle: Bundle? = intent.extras
        if(bundle != null){
            browserPackage = intent.getStringExtra(Constants.BROWSER_PACKAGE)
        }
    }

    private fun redirectPage(){
        if(browserPackage != null){
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.REDIRECT_URL))
                intent.setPackage(browserPackage)
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, browserPackage)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }catch (e: ActivityNotFoundException){
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.REDIRECT_URL))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            }
        }else{
            Toast.makeText(this, "Browser Package not yet.", Toast.LENGTH_LONG).show()
        }
    }
}