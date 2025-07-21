package com.example.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show();


        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {

            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }


        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }
    }
}
