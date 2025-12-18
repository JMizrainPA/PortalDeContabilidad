package com.example.portaldecontabilidad.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portaldecontabilidad.data.Priority
import com.example.portaldecontabilidad.data.Task
import com.example.portaldecontabilidad.data.TaskStatus

/**
 * Esta es la "Vista" que muestra el detalle de una tarea específica.
 * La dividimos en dos partes para seguir las buenas prácticas de Compose:
 * 1.  `TaskDetailScreen`: El composable "inteligente" (smart) que se conecta al ViewModel.
 * 2.  `TaskDetailContent`: El composable "tonto" (dumb) que solo se encarga de pintar la UI.
 */
@Composable
fun TaskDetailScreen(
    // Inyectamos el ViewModel que manejará la lógica de esta pantalla.
    taskDetailViewModel: TaskDetailViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // Nos suscribimos al estado del ViewModel. Cuando la tarea cargue o cambie, `uiState` se actualizará.
    val uiState by taskDetailViewModel.uiState.collectAsState()

    // Le pasamos los datos del estado al composable "tonto" para que los muestre.
    TaskDetailContent(
        task = uiState,
        onNavigateBack = onNavigateBack,
        // Cuando el usuario cambia el estado en la UI, le avisamos al ViewModel.
        onStatusChange = { newStatus -> taskDetailViewModel.updateStatus(newStatus) }
    )
}

/**
 * Este es el composable "tonto". No sabe nada sobre el ViewModel.
 * Solo recibe datos y funciones, y se dedica a pintar la interfaz. Esto lo hace muy reutilizable y fácil de probar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailContent(
    task: Task?, // La tarea a mostrar. Puede ser nula mientras carga.
    onNavigateBack: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit
) {

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = { DetailTopBar(onNavigateBack) },
    ) { paddingValues ->
        // Usamos `task?.let` para mostrar el contenido solo cuando la tarea ya ha cargado.
        task?.let {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // --- Componentes que muestran los detalles de la tarea ---
                InfoChip(it.department, it.taskNumber)
                Spacer(modifier = Modifier.height(8.dp))
                Text(it.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(24.dp))
                StatusSelector(it.status, onStatusChange = onStatusChange) // El selector de estado.
                Spacer(modifier = Modifier.height(16.dp))
                InfoCard(icon = Icons.Filled.CalendarToday, title = "Fecha de vencimiento", value = it.fullDueDate, tag = it.dueDate, tagColor = Color(0xFFF09A34))
                Spacer(modifier = Modifier.height(16.dp))
                InfoCard(icon = Icons.Filled.Warning, title = "Prioridad", value = it.priority.displayName, iconColor = it.priority.color)
                Spacer(modifier = Modifier.height(24.dp))
                DescriptionCard(it.description)
            }
        } ?: run {
            // Si la tarea es nula (todavía está cargando), mostramos un indicador de progreso.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

// --- COMPONENTES PEQUEÑOS Y REUTILIZABLES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Detalle de Tarea", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun InfoChip(department: String, taskNumber: String) {
    Row {
        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color.LightGray.copy(alpha = 0.5f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(department.uppercase(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(taskNumber, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
fun StatusSelector(currentStatus: TaskStatus, onStatusChange: (TaskStatus) -> Unit) {
    val statuses = listOf(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
    val statusNames = listOf("Pendiente", "En Progreso", "Hecho")
    // `remember` con `key` se usa para que el estado se reinicie si la `currentStatus` que viene de fuera cambia.
    var selectedIndex by remember(currentStatus) { mutableStateOf(statuses.indexOf(currentStatus).coerceAtLeast(0)) }

    Column {
        Text("ESTADO", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        SegmentedButton(statuses, statusNames, selectedIndex) { index ->
            selectedIndex = index
            onStatusChange(statuses[index]) // Notificamos hacia fuera sobre el cambio.
        }
    }
}

@Composable
fun <T> SegmentedButton(options: List<T>, optionNames: List<String>, selectedIndex: Int, onOptionSelected: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
            options.forEachIndexed { index, _ ->
                Card(
                    modifier = Modifier.weight(1f).clickable { onOptionSelected(index) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedIndex == index) Color.White else Color.Transparent),
                    elevation = CardDefaults.cardElevation(if (selectedIndex == index) 2.dp else 0.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)){
                        Text(optionNames[index], color = if (selectedIndex == index) Color.Black else Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, title: String, value: String, tag: String? = null, tagColor: Color? = null, iconColor: Color = Color(0xFFF09A34)) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.1f))) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.align(Alignment.Center))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.Gray, fontSize = 12.sp)
                    Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            tag?.let {
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(tagColor?.copy(alpha = 0.2f) ?: Color.Transparent).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(it, color = tagColor ?: Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DescriptionCard(description: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("DESCRIPCIÓN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { /* TODO */ }) {
                Text("Editar")
            }
        }
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
            Text(description, modifier = Modifier.padding(16.dp), color = Color.Gray, lineHeight = 22.sp)
        }
    }
}

/**
 * Una vista previa para ver cómo se ve la pantalla de detalle en Android Studio.
 */
@Preview(showBackground = true, backgroundColor = 0xFFF7F8FA)
@Composable
fun TaskDetailScreenPreview() {
    val task = Task(
        id = "1",
        title = "Conciliación Bancaria - Julio",
        department = "Contabilidad",
        status = TaskStatus.PENDING,
        dueDate = "Próximo",
        taskNumber = "#CNT-2023-84",
        description = "Revisar todas las facturas del 100 al 150 y cruzarlas con el extracto bancario de la cuenta principal. Asegurarse de adjuntar los comprobantes faltantes en el sistema.",
        priority = Priority.HIGH,
        fullDueDate = "25 Oct 2023"
    )

    TaskDetailContent(task = task, onNavigateBack = {}, onStatusChange = {})
}
