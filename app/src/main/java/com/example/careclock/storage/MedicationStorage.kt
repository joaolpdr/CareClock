package com.example.careclock.storage

import android.util.Log
import com.example.careclock.models.Medication
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

object MedicationStorage {

    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    // Esta função agora decide ONDE buscar os medicamentos
    private fun getMedicationsCollection(profileId: String): CollectionReference {
        val currentUser = auth.currentUser!!
        // Se o profileId for o do utilizador atual, busca na coleção principal dele.
        // Se for diferente, busca na sub-coleção do dependente.
        return if (profileId == currentUser.uid) {
            db.collection("users").document(currentUser.uid).collection("medications")
        } else {
            db.collection("users").document(currentUser.uid)
                .collection("dependents").document(profileId).collection("medications")
        }
    }

    fun save(medication: Medication, profileId: String, onComplete: (Boolean) -> Unit) {
        val collection = getMedicationsCollection(profileId)

        val document = if (medication.id.isNotBlank() && !medication.id.startsWith("temp_")) {
            collection.document(medication.id)
        } else {
            collection.document()
        }
        medication.id = document.id

        document.set(medication)
            .addOnSuccessListener {
                Log.d("Firestore", "Medicamento salvo com sucesso para o perfil $profileId")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao salvar medicamento", e)
                onComplete(false)
            }
    }

    fun load(profileId: String, onComplete: (List<Medication>) -> Unit) {
        getMedicationsCollection(profileId).get()
            .addOnSuccessListener { result ->
                try {
                    val medications = result.toObjects(Medication::class.java)
                    onComplete(medications)
                } catch (e: Exception) {
                    Log.e("Firestore", "Erro ao converter objetos.", e)
                    onComplete(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Erro ao carregar medicamentos.", exception)
                onComplete(emptyList())
            }
    }

    fun delete(medicationId: String, profileId: String, onComplete: (Boolean) -> Unit) {
        getMedicationsCollection(profileId).document(medicationId).delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Medicamento apagado com sucesso!")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao apagar medicamento", e)
                onComplete(false)
            }
    }
}