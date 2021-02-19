package com.pashcabu.hw2.view_model


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConnectionViewModel : ViewModel() {

    private val mutableConnectionState = MutableLiveData<Boolean>()
    val connectionState : LiveData<Boolean> get() = mutableConnectionState

    fun setConnectionState(connected: Boolean) {
        mutableConnectionState.value = connected
    }
}