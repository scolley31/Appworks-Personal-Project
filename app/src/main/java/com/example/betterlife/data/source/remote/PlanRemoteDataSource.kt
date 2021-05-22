package com.example.betterlife.data.source.remote

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import com.example.betterlife.PlanApplication
import com.example.betterlife.PlanApplication.Companion.instance
import com.example.betterlife.R
import com.example.betterlife.data.Completed
import com.example.betterlife.data.Plan
import com.example.betterlife.data.source.PlanDataSource
import com.example.betterlife.util.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.example.betterlife.data.Result
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

object PlanRemoteDataSource : PlanDataSource {

    private const val PATH_ARTICLES = "plan"
    private const val KEY_CREATED_TIME = "createdTime"
    private const val KEY_COMPLETED_TIME = "date"
    private const val KEY_PLAN_MEMBER = "members"


    override suspend fun deleteTask(taskId: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_ARTICLES).document(taskId)
                .delete()
                .addOnCompleteListener { addId ->
                    if (addId.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    } else {
                        addId.exception?.let {

                            Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                            continuation.resume(Result.Error(it))
                        }
                        continuation.resume(Result.Fail(PlanApplication.instance.getString(R.string.you_know_nothing)))
                    }
                }
        }

    override suspend fun deleteUserOngoingTask(userId: String, taskId: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_ARTICLES).document(taskId)
                .update(KEY_PLAN_MEMBER, FieldValue.arrayRemove(userId))
                .addOnCompleteListener { addId ->
                    if (addId.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    } else {
                        addId.exception?.let {

                            Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                            continuation.resume(Result.Error(it))
                        }
                        continuation.resume(Result.Fail(PlanApplication.instance.getString(R.string.you_know_nothing)))
                    }
                }
        }


    override suspend fun getCompleted(taskID: String, userID:String): Result<List<Completed>> =
        suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
                .collection(PATH_ARTICLES)
                .document(taskID)
                .collection("completedList")
                .whereEqualTo("user_id",userID)
                .orderBy(KEY_COMPLETED_TIME, Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<Completed>()
                        for (document in task.result!!) {
                            Logger.d(document.id + " => " + document.data)

                            val completed = document.toObject(Completed::class.java)
                            list.add(completed)
                        }
                        continuation.resume(Result.Success(list))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(PlanApplication.instance.getString(R.string.you_know_nothing)))
                    }
                }
    }



    override suspend fun getPlanResult(): Result<List<Plan>> = suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
            .collection(PATH_ARTICLES)
            .whereArrayContains("members","Scolley")
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<Plan>()
                    for (document in task.result!!) {
                        Logger.d(document.id + " => " + document.data)

                        val article = document.toObject(Plan::class.java)
                        list.add(article)
                    }
                    continuation.resume(Result.Success(list))
                } else {
                    task.exception?.let {

                        Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(PlanApplication.instance.getString(R.string.you_know_nothing)))
                }
            }
    }

    override suspend fun getOtherPlanResult(): Result<List<Plan>> =
        suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
            .collection(PATH_ARTICLES)
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val list = mutableListOf<Plan>()
                    for (document in task.result!!) {
                        Logger.d(document.id + " => " + document.data)

                        val article = document.toObject(Plan::class.java)
                        list.add(article)
                    }
                    continuation.resume(Result.Success(list))
                } else {
                    task.exception?.let {

                        Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(PlanApplication.instance.getString(R.string.you_know_nothing)))
                }
            }
    }

    override fun getLivePlanResult(): MutableLiveData<List<Plan>> {

        val liveData = MutableLiveData<List<Plan>>()

        FirebaseFirestore.getInstance()
            .collection(PATH_ARTICLES)
            .whereArrayContains("members","Scolley")
            .orderBy(KEY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                Logger.i("addSnapshotListener detect")

                exception?.let {
                    Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                val list = mutableListOf<Plan>()
                for (document in snapshot!!) {
                    Logger.d(document.id + " => " + document.data)

                    val article = document.toObject(Plan::class.java)
                    list.add(article)
                }

                liveData.value = list
            }
        return liveData
    }

    override suspend fun addTask(plan: Plan): Result<Boolean> =
        suspendCoroutine { continuation ->
        val plans = FirebaseFirestore.getInstance().collection(PATH_ARTICLES)
        val document = plans.document()

        plan.id = document.id
        plan.createdTime = Calendar.getInstance().timeInMillis

        document
            .set(plan)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Logger.i("Publish: $plan")

                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {

                        Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(PlanApplication.instance.getString(R.string.you_know_nothing)))
                }
            }
    }

    override suspend fun sendCompleted(completed: Completed, taskID: String): Result<Boolean> =
        suspendCoroutine { continuation ->
        val plans = FirebaseFirestore.getInstance().collection(PATH_ARTICLES).document(taskID)
        val subCollection = plans.collection("completedList")
        val document = subCollection.document()

        completed.id = document.id

        document.set(completed)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.i("Publish: $completed")

                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {

                            Logger.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(PlanApplication.instance.getString(R.string.you_know_nothing)))
                    }
                }
    }

}