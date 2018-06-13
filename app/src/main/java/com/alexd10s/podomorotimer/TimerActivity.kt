package com.alexd10s.podomorotimer


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.widget.Toast


import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.content_timer.*
import java.util.*

class TimerActivity : AppCompatActivity() {

    companion object {
        val STOPPED = 0
        val PAUSED = 1
        val RUNNING = 2
        val MINUTES = 25

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000

//        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long{
//            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
//            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            val intent = Intent(context, TimerExpiredReceiver::class.java)
//            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
//            PrefUtil.setAlarmSetTime(nowSeconds, context)
//            return wakeUpTime
//        }
//
//        fun removeAlarm(context: Context){
//            val intent = Intent(context, TimerExpiredReceiver::class.java)
//            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
//            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            alarmManager.cancel(pendingIntent)
//            PrefUtil.setAlarmSetTime(0, context)
//        }

    }
    private var timerState = STOPPED
    private lateinit var timer: CountDownTimer

    private var timerLengthSeconds: Long = 0
    private var secondsRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        setSupportActionBar(toolbar)

        setupActionBar()
        setupButtons()

    }

    override fun onResume() {
        super.onResume()

        initTimer()
        //removeAlarm(this)
    }

    override fun onPause() {
        super.onPause()

        if (timerState == RUNNING){
            timer.cancel()
            //val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            //NotificationUtil.showTimerRunning(this, wakeUpTime)
        }
        else if (timerState == PAUSED){
            //NotificationUtil.showTimerPaused(this)
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(Integer(timerState), this)
    }

    private fun setupActionBar() {
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = "           Podomoro Timer"
    }
    private fun setupButtons() {
        fab_play.setOnClickListener{v ->
            Toast.makeText(this,"clicked",Toast.LENGTH_LONG)
            startTimer()
            timerState =  RUNNING
            updateButtons()
        }

        fab_pause.setOnClickListener { v ->
            timer.cancel()
            timerState = PAUSED
            updateButtons()
        }

        fab_stop.setOnClickListener { v ->
            timer.cancel()
            onTimerFinished()

        }
    }

    private fun initTimer(){
        timerState = PrefUtil.getTimerState(this).toInt()


        if (timerState == STOPPED) {
            val lengthInMinutes = MINUTES
            timerLengthSeconds = (lengthInMinutes * 60L)
            countdown_progress.max = timerLengthSeconds.toInt()
        }
        else {
            timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
            countdown_progress.max = timerLengthSeconds.toInt()
        }

        secondsRemaining = if (timerState == RUNNING || timerState == PAUSED)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

//        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
//        if (alarmSetTime > 0)
//            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == RUNNING)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer(){
        timerState = RUNNING

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }
    private fun onTimerFinished(){
        timerState = STOPPED

        val lengthInMinutes = MINUTES
        timerLengthSeconds = (lengthInMinutes * 60L)

        countdown_progress.max = timerLengthSeconds.toInt()
        countdown_progress.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun updateButtons(){
        when (timerState) {
            RUNNING ->{
                fab_play.isEnabled = false
                fab_pause.isEnabled = true
                fab_stop.isEnabled = true
            }
            STOPPED -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = false
            }
            PAUSED -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = true
            }
        }
    }

    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        countdown_text.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        countdown_progress.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }
}

