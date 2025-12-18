package com.example.portaldecontabilidad.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskRepository
import com.example.portaldecontabilidad.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val taskRepository = TaskRepository()
    private var allTasks: List<Task> = emptyList()

    init {
        viewModelScope.launch {
            taskRepository.getTasks().collect { tasks ->
                allTasks = tasks
                filterTasks(uiState.value.selectedStatus)
            }
        }
    }

    fun filterTasks(status: TaskStatus) {
        val filteredTasks = if (status == TaskStatus.ALL) {
            allTasks
        } else {
            allTasks.filter { it.status == status }
        }
        _uiState.update { it.copy(tasks = filteredTasks, selectedStatus = status) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
        }
    }
}

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val selectedStatus: TaskStatus = TaskStatus.ALL
)
