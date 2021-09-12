package com.pashacabu.tmdb_app.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashacabu.tmdb_app.model.NetworkModule
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.PersonResponse
import com.pashacabu.tmdb_app.model.SingleNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class PersonViewModel @Inject constructor() : ViewModel() {

    private val mutablePersonData: MutableLiveData<PersonResponse> = MutableLiveData()
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    private var connectionState: Boolean = false
    private var personID: Int = 0
    val personData: LiveData<PersonResponse> get() = mutablePersonData
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    private val network = SingleNetwork.service

    fun loadData(_personID: Int?) {
        personID = _personID ?: 0
//        try {
            if (connectionState) {
                viewModelScope.launch {
                    if (mutablePersonData.value == null) {
                        mutableLoadingState.value = true
                        try {
                            mutablePersonData.value =
                                network.getPerson(_personID ?: 0, NetworkModule.api_key)
                        } catch (e : Exception){
                            e.printStackTrace()
                            mutableLoadingState.value = false
                        }

                        mutableLoadingState.value = false
                    }

                }
            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    fun setConnectionState(connected: Boolean) {
        if (!connectionState && connected) {
            connectionState = connected
            refreshData(personID)
        } else {
            connectionState = connected
        }

    }

    fun refreshData(personID: Int?) {
        var refreshedPerson: PersonResponse?
//        try {
            viewModelScope.launch {
                mutableLoadingState.value = true
                try {
                    refreshedPerson = network.getPerson(personID ?: 0, NetworkModule.api_key)
                    if (refreshedPerson != null) {
                        mutablePersonData.postValue(refreshedPerson)
                        mutableLoadingState.value = false
                    }
                } catch (e : Exception){
                    e.printStackTrace()
                    mutableLoadingState.value = false
                }

            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

}