package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppSettings {
    var showHome by mutableStateOf(true)
    var showSearch by mutableStateOf(true)
    var showAdd by mutableStateOf(true)
    var showSnaply by mutableStateOf(true)
    var showAccount by mutableStateOf(true)
}
