package com.example.websiteblocker.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPrefHandler private constructor() {
    fun getValue(key: String?): String? {
        val prefs: SharedPreferences = context!!.getSharedPreferences(sharedPrefName,
            Context.MODE_PRIVATE
        )
        return prefs.getString(key, null)
    }

    fun getBooleanValue(key: String?): Boolean {
        val prefs: SharedPreferences = context!!.getSharedPreferences(sharedPrefName,
            Context.MODE_PRIVATE
        )
        return prefs.getBoolean(key, false)
    }

    fun add(key: String?, value: String?): SharedPrefHandler? {
        return try {
            context?.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)?.edit()?.putString(key, value)?.apply()
            me
        } catch (ex: Exception) {
            Log.e(">>> ", "SharedPrefHandler: AddSharedPref: Exception: " + ex.message, ex)
            throw ex
        }
    }

    fun add(key: String?, value: Boolean): SharedPrefHandler? {
        return try {
            context?.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)?.edit()?.putBoolean(key, value)?.apply()
            me
        } catch (e: Exception) {
            Log.e(">>> ", "SharedPrefHandler: AddSharedPref: Exception: " + e.message, e)
            throw e
        }
    }

    fun remove(key: String?): SharedPrefHandler? {
        return try {
            context?.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)?.edit()?.remove(key)?.apply()
            me
        } catch (ex: Exception) {
            Log.e(">>> ", "SharedPrefHandler: AddSharedPref: Exception: " + ex.message, ex)
            throw ex
        }
    }

    enum class PrefFiles {
        USER_DETAILS_PREF
    }


    companion object {
        private var me: SharedPrefHandler? = null
        private var sharedPrefName: String? = null

        private var context: Context? = null
        fun getInstance(cntx: Context?, sharedPrefFileName: PrefFiles): SharedPrefHandler? {
            if (me == null) {
                me = SharedPrefHandler()
            }
            context = cntx
            sharedPrefName = sharedPrefFileName.name
            return me
        }
    }
}