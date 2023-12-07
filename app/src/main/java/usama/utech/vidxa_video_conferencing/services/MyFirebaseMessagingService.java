package usama.utech.vidxa_video_conferencing.services;


import android.os.Build;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import usama.utech.vidxa_video_conferencing.utils.Constants;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String MyTag = "MessagingService";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        sendRegistrationToServer(s);

    }

    private void sendRegistrationToServer(String token) {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {



        if (remoteMessage.getData().size() > 0) {

        }

        if (remoteMessage.getNotification() != null) {

           if(Objects.requireNonNull(remoteMessage.getNotification().getTitle()).trim().toLowerCase().equals("rate")){
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   Constants.sendOreoNotificationWithURL(this, remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getBody());
               } else {
                   Constants.sendNotificationWithURL(this, remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getBody());
               }

           }else {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   Constants.sendOreoNotification(this, remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
               } else {
                   Constants.sendNotification(this, remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
               }
           }



        }


    }



}
