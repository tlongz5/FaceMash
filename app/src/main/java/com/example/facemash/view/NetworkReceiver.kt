package com.example.facemash.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkReceiver(
    private val activity: Activity,
    private val callback: () -> Unit
): BroadcastReceiver() {
    private val dialog by lazy { DialogNetworkError(activity) }
    override fun onReceive(context: Context, intent: Intent) {
        if(!isNetworkAvailable(context)) {
            dialog.show()
        }else {
            dialog.dismiss()
            callback()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}