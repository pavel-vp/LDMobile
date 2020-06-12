package com.elewise.ldmobile.service

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.elewise.ldmobile.R

object Prefs {
    private val STORAGE_NAME = "settings"
    private val PREFS_CONNECT_ADDRESS = "connect_address"
    private val PREFS_LAST_LOGIN = "last_login"
    private val PREFS_TOKEN = "token"
    private val PREFS_CONTAINER_ALIAS = "container_alias"
    private var storage: SharedPreferences? = null

    private fun getStorage(context: Context): SharedPreferences {
        if (storage == null) {
            storage = context.getSharedPreferences(STORAGE_NAME, Activity.MODE_PRIVATE)
        }
        return storage!!
    }

    fun getConnectAddress(context: Context): String {
        return getStorage(context).getString(PREFS_CONNECT_ADDRESS, context.getString(R.string.rest_server_base_url))
    }

    fun saveConnectAddress(context: Context, address: String) {
        val edit = getStorage(context).edit()
        edit.putString(PREFS_CONNECT_ADDRESS, address)
        edit.commit()

        // пересоздадим рест хелпер
        Session.getInstance().createRestHelper()
    }

    fun getLastLogin(context: Context): String {
        return getStorage(context).getString(PREFS_LAST_LOGIN, "")
    }

    fun saveLastLogin(context: Context, address: String) {
        val edit = getStorage(context).edit()
        edit.putString(PREFS_LAST_LOGIN, address)
        edit.apply()
    }

    fun getToken(context: Context): String {
        return getStorage(context).getString(PREFS_TOKEN, "")
    }

    fun saveToken(context: Context, token: String) {
        val edit = getStorage(context).edit()
        edit.putString(PREFS_TOKEN, token)
        edit.commit()
    }

    fun getContainerAlias(context: Context): String {
        return getStorage(context).getString(PREFS_CONTAINER_ALIAS, "")
    }

    fun saveContainerAlias(context: Context, alias: String) {
        val edit = getStorage(context).edit()
        edit.putString(PREFS_CONTAINER_ALIAS, alias)
        edit.commit()
    }
}