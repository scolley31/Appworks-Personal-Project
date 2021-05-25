package com.example.betterlife.timer.item

import android.util.Log
import android.widget.Switch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.betterlife.PlanApplication
import com.example.betterlife.R
import com.example.betterlife.data.Completed
import com.example.betterlife.data.Plan
import com.example.betterlife.data.Rank
import com.example.betterlife.data.Result
import com.example.betterlife.data.source.PlanRepository
import com.example.betterlife.newwork.LoadApiStatus
import com.example.betterlife.util.TimeConverters
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TimerInfoViewModel(private val repository: PlanRepository): ViewModel() {

    val _info = MutableLiveData<Plan>()

    val info: LiveData<Plan>
        get() = _info

    private val _completedTest = MutableLiveData<List<Completed>>()

    val completedTest: LiveData<List<Completed>>
        get() = _completedTest

    private val _rank = MutableLiveData<List<Rank>>()

    val rank: LiveData<List<Rank>>
        get() = _rank

    private val _leaveTimer = MutableLiveData<Boolean>()

    val leaveTimer: LiveData<Boolean>
        get() = _leaveTimer

    private val _status = MutableLiveData<LoadApiStatus>()

    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _refreshStatus = MutableLiveData<Boolean>()

    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    private val _error = MutableLiveData<String>()

    val error: LiveData<String>
        get() = _error

    val averageDailyTime = MutableLiveData<Int>()

    val xTitle = mutableListOf<String>()

    val entries : MutableList<BarEntry> = arrayListOf()

    val label: MutableList<String> = mutableListOf()

    val forPrintChat = MutableLiveData<Boolean>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun leaveTimer() {
        _leaveTimer.value = true
    }

    init {
        getCompletedForChart()
    }

    fun getRank() {

        var rankTmp = mutableListOf<Rank>()

        coroutineScope.launch {

            for (i in info.value!!.members.indices) {

                var oneRank = repository.getCompleted(info.value!!.id, info.value!!.members[i])
                var oneRankToCount : List<Completed>? = when(oneRank) {
                    is Result.Success -> {
                        _error.value = null
                        _status.value = LoadApiStatus.DONE
                        oneRank.data
                    }
                    is Result.Fail -> {
                        _error.value = oneRank.error
                        _status.value = LoadApiStatus.ERROR
                        null
                    }
                    is Result.Error -> {
                        _error.value = oneRank.exception.toString()
                        _status.value = LoadApiStatus.ERROR
                        null
                    }
                    else -> {
                        _error.value = PlanApplication.instance.getString(R.string.you_know_nothing)
                        _status.value = LoadApiStatus.ERROR
                        null
                    }
                }

                var sum = 0

                if (oneRankToCount != null) {
                    for ( j in oneRankToCount.indices) {
                        sum += oneRankToCount[j].daily
                    }
                }

                rankTmp.run {
                    add(Rank(info.value!!.members[i],sum/60))
                }
                Log.d("test","rankTmp = $rankTmp")
            }

            var rankTmpForSort = mutableListOf<Rank>()
            rankTmpForSort = sortToRank(rankTmp)
            Log.d("test","rankTmpForSort = $rankTmpForSort")
            _rank.value = rankTmpForSort

        }
    }

    fun sortToRank (rankList : MutableList<Rank>): MutableList<Rank> {
        for (i in 0 until rankList.size-1) {
            Log.d("test","rankList.size = ${rankList.size}")
            Log.d("test","i = $i")
            var num = i

                for (j in (num+1) .. rankList.size-1) {
                    Log.d("test","j = $j")
                    Log.d("test","i = $i")
                    if (rankList[j].totalTime > rankList[i].totalTime) {
                        var tmpTotalTime = rankList[i].totalTime
                        var tmpName = rankList[i].user_id
                        rankList[i].totalTime = rankList[j].totalTime
                        rankList[i].user_id = rankList[j].user_id
                        rankList[j].totalTime = tmpTotalTime
                        rankList[j].user_id = tmpName
                    }
                }

        }
        return rankList
    }


    fun getCompletedForChart() {

        coroutineScope.launch {

            var completed = repository.getCompleted(_info.value!!.id, "Scolley")
            _completedTest.value = when(completed) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    completed.data
                }
                is Result.Fail -> {
                    _error.value = completed.error
                    _status.value = LoadApiStatus.ERROR
                    null
                }
                is Result.Error -> {
                    _error.value = completed.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                    null
                }
                else -> {
                    _error.value = PlanApplication.instance.getString(R.string.you_know_nothing)
                    _status.value = LoadApiStatus.ERROR
                    null
                }
            }


            var sum = 0
            var number = 0
            for ( i in completedTest.value!!.indices) {

                xTitle.add(completedTest.value!![i].date.let {
                    TimeConverters.timestampToDate(it, Locale.TAIWAN)
                })

                label.add(completedTest.value!![i].date.let {
                    TimeConverters.timestampToDate(it, Locale.TAIWAN)
                })


                 entries.add(BarEntry(i.toFloat(), completedTest.value!![i].daily/60.toFloat()))

                sum += completedTest.value!![i].daily/60
                number += 1
            }
            if(number != 0) {
                averageDailyTime.value = sum / number
            }
            forPrintChat.value = true


            Log.d("test","xTitle = $xTitle")
            Log.d("test","label = $label")
            Log.d("test","entries = $entries")
        }
    }

}