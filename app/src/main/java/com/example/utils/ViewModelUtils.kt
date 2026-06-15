package com.example.utils

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.PixelVerseApplication

fun CreationExtras.pixelVerseApplication(): PixelVerseApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PixelVerseApplication)
