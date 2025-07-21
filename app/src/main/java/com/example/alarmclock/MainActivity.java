package com.example.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmManager;
    private PendingIntent alarmPendingIntent;
    private Handler timeUpdateHandler;
    private Runnable timeUpdateRunnable;

    private TextView textViewCurrentTime, textViewAlarmStatus;
    private TimePicker timePickerAlarm;
    private Button buttonSetAlarm, buttonCancelAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        textViewCurrentTime = findViewById(R.id.textViewCurrentTime);
        textViewAlarmStatus = findViewById(R.id.textViewAlarmStatus);
        timePickerAlarm = findViewById(R.id.timePickerAlarm);
        buttonSetAlarm = findViewById(R.id.buttonSetAlarm);
        buttonCancelAlarm = findViewById(R.id.buttonCancelAlarm);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        timeUpdateHandler = new Handler(Looper.getMainLooper());
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                textViewCurrentTime.setText(currentTime);
                timeUpdateHandler.postDelayed(this, 1000);
            }
        };


        buttonSetAlarm.setOnClickListener(v -> setAlarm());


        buttonCancelAlarm.setOnClickListener(v -> cancelAlarm());


        updateUIForNoAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeUpdateHandler.post(timeUpdateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeUpdateHandler.removeCallbacks(timeUpdateRunnable);
    }

    private void setAlarm() {
        Calendar calendar = Calendar.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendar.set(Calendar.HOUR_OF_DAY, timePickerAlarm.getHour());
            calendar.set(Calendar.MINUTE, timePickerAlarm.getMinute());
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, timePickerAlarm.getCurrentHour());
            calendar.set(Calendar.MINUTE, timePickerAlarm.getCurrentMinute());
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);


        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        alarmPendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
            } else {

                Intent intentn = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intentn);

                Toast.makeText(this, "Please enable 'Schedule exact alarms' permission in settings.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
        }

        String timeSet = String.format(Locale.getDefault(), "%02d:%02d %s",
                (calendar.get(Calendar.HOUR) == 0) ? 12 : calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM");

        textViewAlarmStatus.setText("Alarm set for " + timeSet);
        Toast.makeText(this, "Alarm set for " + timeSet, Toast.LENGTH_SHORT).show();
        updateUIForAlarmSet();
    }

    private void cancelAlarm() {
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
        }
        textViewAlarmStatus.setText("No Alarm Set");
        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show();
        updateUIForNoAlarm();
    }

    private void updateUIForAlarmSet() {
        buttonSetAlarm.setVisibility(View.GONE);
        buttonCancelAlarm.setVisibility(View.VISIBLE);
        timePickerAlarm.setEnabled(false);
    }

    private void updateUIForNoAlarm() {
        buttonSetAlarm.setVisibility(View.VISIBLE);
        buttonCancelAlarm.setVisibility(View.GONE);
        timePickerAlarm.setEnabled(true);
    }
}
