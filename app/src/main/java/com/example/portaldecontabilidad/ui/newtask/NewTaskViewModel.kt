package com.example.portaldecontabilidad.ui.newtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portaldecontabilidad.data.Priority
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskRepository
import com.example.portaldecontabilidad.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

class NewTaskViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NewTaskUiState())
    val uiState: StateFlow<NewTaskUiState> = _uiState.asStateFlow()

    private val repository = TaskRepository()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onDateChangeString(dateString: String) {
        _uiState.update { it.copy(dueDateString = dateString) }
    }

    fun onDepartmentChange(department: String) {
        _uiState.update { it.copy(department = department) }
    }

    fun onPriorityChange(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun createTask() {
        viewModelScope.launch {
            val state = uiState.value
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dueDate: LocalDate?
            try {
                dueDate = LocalDate.parse(state.dueDateString, dateFormatter)
            } catch (e: DateTimeParseException) {
                _uiState.update { it.copy(userMessage = "Formato de fecha inválido. Usa dd/MM/yyyy.") }
                return@launch
            }

            if (state.title.isNotBlank() && state.department.isNotBlank()) {
                val dueDateShortFormatter = DateTimeFormatter.ofPattern("d MMM")
                val dueDateFullFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = state.title,
                    description = state.description,
                    department = state.department,
                    priority = state.priority,
                    status = TaskStatus.PENDING,
                    dueDate = dueDate.format(dueDateShortFormatter),
                    fullDueDate = dueDate.format(dueDateFullFormatter),
                    taskNumber = "#${state.department.substring(0, 3).uppercase()}-${UUID.randomUUID().toString().substring(0, 4).uppercase()}"
                )
                repository.addTask(newTask)
                _uiState.update { it.copy(taskCreated = true, userMessage = "Tarea creada con éxito") }
            } else {
                _uiState.update { it.copy(userMessage = "Por favor, completa todos los campos obligatorios.") }
            }
        }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun onNavigated() {
        _uiState.update { it.copy(taskCreated = false) }
    }
}

data class NewTaskUiState(
    val title: String = "",
    val description: String = "",
    val dueDateString: String = "",
    val department: String = "",
    val priority: Priority = Priority.LOW,
    val taskCreated: Boolean = false,
    val userMessage: String? = null
)
