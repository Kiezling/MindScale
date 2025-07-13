package com.example.mindscale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mindscale.data.WellnessDao
import com.example.mindscale.data.WellnessEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val dao: WellnessDao) : ViewModel() {

    val recentEntries = dao.getRecentEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // Note: The function signature is now different
    fun addEntry(intensity: Int, type: String, timestamp: Long, note: String? = null) {
        viewModelScope.launch {
            val entry = WellnessEntry(
                intensity = intensity,
                timestamp = timestamp,
                type = type,
                note = note
            )
            dao.insert(entry)
        }
    }

    fun deleteEntry(entry: WellnessEntry) {
        viewModelScope.launch {
            dao.delete(entry)
        }
    }
}

class MainViewModelFactory(private val dao: WellnessDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}