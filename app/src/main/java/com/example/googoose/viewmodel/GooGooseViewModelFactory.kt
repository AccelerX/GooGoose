package com.example.googoose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.googoose.data.GooGooseRepository

class GooGooseViewModelFactory(private val repository: GooGooseRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass.isAssignableFrom(GooGooseViewModel::class.java))
        return GooGooseViewModel(repository) as T
    }
}
