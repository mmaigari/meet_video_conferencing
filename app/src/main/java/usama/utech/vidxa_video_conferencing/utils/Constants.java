package usama.utech.vidxa_video_conferencing.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.activities.MainActivity;
import usama.utech.vidxa_video_conferencing.activities.RateUsAndPrivacyWebView;
import usama.utech.vidxa_video_conferencing.receiver.AlarmReceiver;

public class Constants {


// TODO in vidxa
//1. Startapp, fb and admob ads working together
//2. ads on video screen (huge modification)
//3. fixed tons of bugs which makes vidxa fit for any large enterprise implementation (not codecanyon quality code, but enterprise quality code )
//4. subscriptions to remove ads, and enable Picture in Picture (we had picture in picture disabled by default)
//5. Very easy Meeting Code share function
//6. Larger meeting code
//7. Huge modification in web version with option to use Razorpay for subscriptions to remove ads, adsense elegant ads in screen etc.
//8. Apple ID implemented in iOS


    public static final String JITSI_SERVER_URL = "https://meet.jit.si";
    public static final String JITSI_SERVER_URL_WITH_SLASH = "https://vidxa.infusiblecoder.com/";
    public static final String MEETING_CODE_STRING = "vidxa";
    public static final String IMAGE_DIRECTORY_NAME = "Vidxa";
    public static final String ALLOWED_CHARACTERS = "qwertyuasdxcvfghjklzbiopnm";
    public static final int LENGTH_OF_MEETING_CODE = 10;


    public static final String USER_INFO = "user_info";
    private static final String REMINDER_TIME = "reminder_pref";
    private static final String REMINDER_REQUEST_CODE = "REMINDER_REQUEST_CODE";
    private static final String DARKMODE_STATUS = "darkmode";
    public static int DAILY_REMINDER_REQUEST_CODE = 111;
    public static final String INTENT_BEAN = "IntentBeanData";
    public static final String INTENT_ID = "ID";
    public static final String Storage_Path = "Images/";
    public static String NAME = "Unknown";
    public static String MEETING_ID = "";
    private static final String MyPref = "MyPreflanguage";
    private static final String LANGUAGE_CODE = "LANGUAGE_CODE";


    public static boolean checkDateisFuture(String selectedDate) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat myFormat = new SimpleDateFormat(DateFormats.DATE_FORMAT_DASH);

        try {
            Date date1 = myFormat.parse(selectedDate);
            Date date2 = myFormat.parse(SharedObjectsAndAppController.getTodaysDate(Constants.DateFormats.DATE_FORMAT_DASH));

            if (Objects.requireNonNull(date2).before(date1)) {
                return true;
            } else if (date2.equals(date1)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void showSnackBar(String message, View view) {
        final Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackBar.show();
    }

    public static void showAlertDialog(String Msg, Context context) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setMessage(Msg);
        materialAlertDialogBuilder.setCancelable(false).setPositiveButton(context.getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        materialAlertDialogBuilder.show();
    }

    public static boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String getMeetingCode(Context context) {

        StringBuilder meetingCode = new StringBuilder(Constants.MEETING_CODE_STRING );

        String randomString = getRandomString();
        int size = 3;
        for (int start = 0; start < randomString.length(); start += size) {
            meetingCode.append(randomString.substring(start, Math.min(randomString.length(), start + size)));
        }
        return meetingCode.substring(0, meetingCode.length() - 1);
    }

    private static String getRandomString() {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(Constants.LENGTH_OF_MEETING_CODE);
        for (int i = 0; i < Constants.LENGTH_OF_MEETING_CODE; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public static class Table {
        public static String USERS = "Users";
        public static String MEETING_HISTORY = "MeetingHistory";
        public static String SCHEDULE = "Schedule";
    }




    public static void cancelReminderAlarm(Context context,int reminderIdandrequestcode) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, reminderIdandrequestcode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(sender);
    }



    public static int generateRandomInt(int upperRange){

        Random random = new Random();

        return random.nextInt(upperRange);

    }

    public static void sendNotification(Context context, String notificationTitle, String notificationBody) {

        Random random1 = new Random();
        int j = random1.nextInt(5);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, j, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSound = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.notification);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_baseline_videocam_24)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Random random = new Random();
        int num = random.nextInt(5);

        noti.notify(num, builder.build());
    }


    public static void sendOreoNotification(Context context, String notificationTitle, String notificationBody) {

        Random random1 = new Random();
        int j = random1.nextInt(5);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, j, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Uri defaultSound = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.notification);

        OreoNotification oreoNotification = new OreoNotification(context);
        Notification.Builder builder = oreoNotification.getOreoNotification(notificationTitle, notificationBody, pendingIntent,
                defaultSound, R.drawable.ic_baseline_videocam_24);

        Random random = new Random();
        int num = random.nextInt(5);

        oreoNotification.getManager().notify(num, builder.build());

    }




    public static void sendNotificationWithURL(Context context, String notificationTitle, String notificationBody,String link) {

        Random random1 = new Random();
        int j = random1.nextInt(5);

        Intent intent = new Intent(context, RateUsAndPrivacyWebView.class);
        intent.putExtra("link",link);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, j, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSound = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.notification);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_baseline_videocam_24)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Random random = new Random();
        int num = random.nextInt(5);

        noti.notify(num, builder.build());
    }


    public static void sendOreoNotificationWithURL(Context context, String notificationTitle, String notificationBody,String link) {

        Random random1 = new Random();
        int j = random1.nextInt(5);

        Intent intent = new Intent(context, RateUsAndPrivacyWebView.class);
        intent.putExtra("link",link);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, j, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Uri defaultSound = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.notification);

        OreoNotification oreoNotification = new OreoNotification(context);
        Notification.Builder builder = oreoNotification.getOreoNotification(notificationTitle, notificationBody, pendingIntent,
                defaultSound, R.drawable.ic_baseline_videocam_24);

        Random random = new Random();
        int num = random.nextInt(5);

        oreoNotification.getManager().notify(num, builder.build());

    }




    public static void setLanguageCode(Context context, String s) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LANGUAGE_CODE, s);
        editor.apply();
    }
    public static void setDarkmodeStatus(Context context, String s) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DARKMODE_STATUS, s);
        editor.apply();
    }

    public static void setReminderTime(Context context, String s, int randonID) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(REMINDER_TIME, s);
        editor.putInt(REMINDER_REQUEST_CODE,randonID);
        editor.apply();
    }

    public static String getReminderTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        return sharedPreferences.getString(REMINDER_TIME, context.getString(R.string.default_reminder_text));
    }

    public static int getREMINDER_REQUEST_CODE(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(REMINDER_REQUEST_CODE, 0);
    }

    public static void showReminderNotification(Context context) {
        Toast.makeText(context, "Alaram done", Toast.LENGTH_SHORT).show();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context, MainActivity.class);

        String channelId = "usama.utech.vidxa_video_conferencing";
        String channelName = "video_conferencing";
        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent1);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(DAILY_REMINDER_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_videocam_24).setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(context.getString(R.string.a_video_conferencing_app))
                .setAutoCancel(true).setContentIntent(resultPendingIntent);
        assert notificationManager != null;
        notificationManager.notify(DAILY_REMINDER_REQUEST_CODE, mBuilder.build());
    }


    public static class DateFormats {

        public static String DATE_FORMAT_DD_MMM_YYYY = "dd MMM yyyy";
        public static String DATE_FORMAT_DASH = "dd-MM-yyyy";

        public static String TIME_FORMAT_12 = "hh:mm a";
        public static String TIME_FORMAT_24 = "HH:mm";

        public static String DATETIME_FORMAT_24 = "dd-MM-yyyy HH:mm:ss";
        public static String DATETIME_FORMAT_12 = "dd-MM-yyyy hh:mm a";
    }




    public static String getDarkmodeStatus(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DARKMODE_STATUS, "false");
    }


    public static String getLanguageCode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LANGUAGE_CODE, context.getString(R.string.en_code));
    }

}
