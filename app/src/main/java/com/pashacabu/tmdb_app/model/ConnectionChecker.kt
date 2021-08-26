package com.pashacabu.tmdb_app.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData


class ConnectionChecker(context: Context) : LiveData<Boolean>() {

    private val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val builderCellular = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    private val builderWiFi = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    private var cellularIsOn = false
    private var wiFiIsOn = false
    private var isRegistered = false


    override fun onActive() {
        if (!isRegistered) {
            manager.registerNetworkCallback(
                builderCellular.build(),
                MyCellularCallback()
            )
            manager.registerNetworkCallback(
                builderWiFi.build(),
                MyWiFiCallback()
            )
            isRegistered = true
        }
    }

    fun postActualConnectivityStatus() {
        if (cellularIsOn || wiFiIsOn) {
            postValue(true)
        }
        if (!cellularIsOn && !wiFiIsOn) {
            postValue(false)
        }
    }

    inner class MyCellularCallback() : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            cellularIsOn = true
            postActualConnectivityStatus()
        }

        override fun onLost(network: Network) {
            cellularIsOn = false
            postActualConnectivityStatus()
        }
    }

    inner class MyWiFiCallback() : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            wiFiIsOn = true
            postActualConnectivityStatus()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            wiFiIsOn = false
            postActualConnectivityStatus()
        }
    }

    companion object : SingletonHolder<ConnectionChecker, Context>(::ConnectionChecker)
}
