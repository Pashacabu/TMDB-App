package com.pashcabu.hw2.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData


class ConnectionChecker(context: Context) : LiveData<Boolean>() {

    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val builderCellular = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    private val builderWiFi = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    private var cellularIsOn = false
    private var wiFiIsOn = false


    override fun onActive() {
        Log.d("CCH", "im being observed")
        manager.registerNetworkCallback(
                builderCellular.build(),
                MyCellularCallback())
        manager.registerNetworkCallback(
                builderWiFi.build(),
                MyWiFiCallback()
        )
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
            Log.d("CCH", "cellular available")
            cellularIsOn = true
            postActualConnectivityStatus()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("CCH", "cellular lost")
            cellularIsOn = false
            postActualConnectivityStatus()
        }
    }

    inner class MyWiFiCallback() : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("CCH", "wifi available")
            wiFiIsOn = true
            postActualConnectivityStatus()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("CCH", "wifi lost")
            wiFiIsOn = false
            postActualConnectivityStatus()
        }
    }


}