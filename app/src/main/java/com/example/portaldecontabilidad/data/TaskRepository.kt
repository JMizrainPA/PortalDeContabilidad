package com.example.portaldecontabilidad.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val tasksCollection = db.collection("tasks")

    fun getTasks(): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val tasks = snapshot?.documents?.mapNotNull {
                it.toObject(Task::class.java)?.copy(id = it.id)
            } ?: emptyList()
            trySend(tasks)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addTask(task: Task) {
        tasksCollection.add(task).await()
    }

    fun getTaskById(taskId: String): Flow<Task?> = callbackFlow {
        val listener = tasksCollection.document(taskId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(Task::class.java)?.copy(id = snapshot.id))
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateTask(task: Task) {
        if (task.id.isNotBlank()) {
            tasksCollection.document(task.id).set(task).await()
        }
    }

    suspend fun deleteTask(taskId: String) {
        if (taskId.isNotBlank()) {
            tasksCollection.document(taskId).delete().await()
        }
    }
}