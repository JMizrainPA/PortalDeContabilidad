package com.example.portaldecontabilidad.data

import androidx.compose.ui.graphics.Color

enum class TaskStatus(val displayName: String, val color: Color) {
    ALL("Todas", Color.Transparent),
    PENDING("Pendiente", Color(0xFFE6B800)),
    IN_PROGRESS("En Progreso", Color(0xFF1b396a)),
    COMPLETED("Completada", Color(0xFF4CAF50)),
    HIGH_PRIORITY("Alta Prioridad", Color.Red)
}

enum class Priority(val displayName: String, val color: Color) {
    HIGH("Alta", Color.Red),
    MEDIUM("Media", Color.Yellow),
    LOW("Baja", Color.Green)
}

data class Task(
    val id: String = "",
    val title: String = "",
    val department: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: String = "",
    val taskNumber: String = "",
    val description: String = "",
    val priority: Priority = Priority.LOW,
    val fullDueDate: String = ""
) {}