package com.example.portaldecontabilidad.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskRepository
import com.example.portaldecontabilidad.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TaskDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val taskId: String = checkNotNull(savedStateHandle["taskId"])
    private val repository = TaskRepository()

    private val _uiState = MutableStateFlow<Task?>(null)
    val uiState: StateFlow<Task?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTaskById(taskId).collect {
                _uiState.value = it
            }
        }
    }

    fun updateStatus(status: TaskStatus) {
        _uiState.value?.let { task ->
            val updatedTask = task.copy(status = status)
            viewModelScope.launch {
                repository.updateTask(updatedTask)
            }
        }
    }
}
