package com.ekpss.quizapp.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.ekpss.quizapp.model.Note
import com.ekpss.quizapp.model.Question
import com.ekpss.quizapp.model.Test
import com.ekpss.quizapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import android.util.Log

class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    fun getNotes(
        subject: String,
        onSuccess: (List<Note>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val slug = Constants.SUBJECT_SLUGS[subject] ?: subject.lowercase()

        db.collection("subjects")
            .document(slug)
            .collection("notes")
            .get()
            .addOnSuccessListener { result ->
                val notes = result.map { it.toObject(Note::class.java) }
                onSuccess(notes)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getQuestions(
        subject: String,
        testId: String,
        onSuccess: (List<Question>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val slug = Constants.SUBJECT_SLUGS[subject] ?: subject.lowercase()

        db.collection("subjects")
            .document(slug)
            .collection("tests")
            .document(testId)
            .collection("questions")
            .get()
            .addOnSuccessListener { result ->
                val questions = result.map { it.toObject(Question::class.java) }
                onSuccess(questions)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getTests(
        subject: String,
        onSuccess: (List<Test>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val slug = Constants.SUBJECT_SLUGS[subject] ?: subject.lowercase()

        db.collection("subjects")
            .document(slug)
            .collection("tests")
            .get()
            .addOnSuccessListener { result ->
                val tests = result.map {
                    val id = it.id
                    val title = it.getString("title") ?: id
                    Test(id, title)
                }
                onSuccess(tests)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getBookmarkedQuestions(
        onSuccess: (List<Question>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        db.collection("users")
            .document(userId)
            .collection("bookmarks")
            .get()
            .addOnSuccessListener { result ->
                val questions = result.mapNotNull { doc ->
                    try {
                        doc.toObject(Question::class.java)
                    } catch (e: Exception) {
                        Log.e("FirestoreHelper", "Soru dönüştürme hatası: ${e.message}")
                        null
                    }
                }
                onSuccess(questions)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun saveBookmarkedQuestion(
        subject: String,
        question: Question,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        db.collection("users")
            .document(userId)
            .collection("bookmarks")
            .whereEqualTo("question", question.question)
            .whereEqualTo("testId", question.testId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    val data = hashMapOf(
                        "question" to question.question,
                        "options" to question.options,
                        "answer" to question.answer,
                        "testId" to question.testId,
                        "subject" to subject
                    )
                    db.collection("users")
                        .document(userId)
                        .collection("bookmarks")
                        .add(data)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteBookmarkedQuestion(
        subject: String,
        question: Question,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        db.collection("users")
            .document(userId)
            .collection("bookmarks")
            .whereEqualTo("question", question.question)
            .whereEqualTo("testId", question.testId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (doc in querySnapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getBookmarkedQuestionsBySubject(
        subject: String,
        onSuccess: (List<Question>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        db.collection("users")
            .document(userId)
            .collection("bookmarks")
            .whereEqualTo("subject", subject)
            .get()
            .addOnSuccessListener { result ->
                val questions = result.mapNotNull { it.toObject(Question::class.java) }
                onSuccess(questions)
            }
            .addOnFailureListener { onFailure(it) }
    }

}
