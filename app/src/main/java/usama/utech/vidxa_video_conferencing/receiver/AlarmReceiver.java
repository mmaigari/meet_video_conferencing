package usama.utech.vidxa_video_conferencing.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import usama.utech.vidxa_video_conferencing.utils.Constants;

public class AlarmReceiver extends BroadcastReceiver {


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        DateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormats.DATE_FORMAT_DD_MMM_YYYY, Locale.ENGLISH);


        Calendar calendar = Calendar.getInstance();
        String time = simpleDateFormat.format(calendar.getTime());
        if (Constants.getReminderTime(context).equals(time)) {
            Constants.showReminderNotification(context);

        }


    }
}