package com.example.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MainViewModel:ViewModel() {


    private val _getLocation:MutableSharedFlow<Boolean> = MutableSharedFlow()
    val getLocation:SharedFlow<Boolean> = _getLocation


    fun runLocation(start: Boolean) {
       viewModelScope.launch{
           _getLocation.emit(start)
       }
    }

}