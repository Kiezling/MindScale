package com.example.mindscale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mindscale.data.EntryType
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

    fun addEntry(intensity: Int, entryType: EntryType, note: String? = null) {
        viewModelScope.launch {
            val entry = WellnessEntry(
                intensity = intensity,
                timestamp = System.currentTimeMillis(),
                entryType = entryType,
                note = note // Pass the note to the entry
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