package usama.utech.vidxa_video_conferencing.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.modules.core.PermissionListener;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.jitsi.meet.sdk.JitsiMeetView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.MeetingHistory;
import usama.utech.vidxa_video_conferencing.models.UserProfile;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class MeetingActivity extends FragmentActivity implements JitsiMeetActivityInterface {

    SharedObjectsAndAppController sharedObjectsAndAppController;
    DatabaseManager mDatabaseManager;
    MeetingHistory meetingHistory = null;
    private JitsiMeetView view;


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JitsiMeetActivityDelegate.onActivityResult(
                this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new JitsiMeetView(this);

        sharedObjectsAndAppController = new SharedObjectsAndAppController(MeetingActivity.this);
        mDatabaseManager = new DatabaseManager(MeetingActivity.this);

        UserProfile userProfile = null;
        if (sharedObjectsAndAppController.getUserInfo() != null) {
            userProfile = sharedObjectsAndAppController.getUserInfo();
            meetingHistory = new MeetingHistory();
            meetingHistory.setId(mDatabaseManager.getKeyForMeetingHistory());
            meetingHistory.setUserId(sharedObjectsAndAppController.getUserInfo().getId());
            meetingHistory.setMeeting_id(Constants.MEETING_ID);
        }

        JitsiMeetUserInfo jitsiMeetUserInfo = new JitsiMeetUserInfo();

        if (userProfile != null) {
            jitsiMeetUserInfo.setDisplayName(userProfile.getName());
            try {
                if (!TextUtils.isEmpty(userProfile.getProfile_pic())) {
                    jitsiMeetUserInfo.setAvatar(new URL(userProfile.getProfile_pic()));
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            jitsiMeetUserInfo.setDisplayName(Constants.NAME);
        }

        JitsiMeetConferenceOptions options = null;
        try {
            options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL(Constants.JITSI_SERVER_URL))
                    .setRoom(Constants.MEETING_ID)
                    .setUserInfo(jitsiMeetUserInfo)
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("pip.enabled", true)
                    .setFeatureFlag("call-integration.enabled", false)
                    .setFeatureFlag("live-streaming.enabled", true)
                    .setFeatureFlag("recording.enabled", true)
                    .build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (meetingHistory != null) {
            meetingHistory.setSubject(Objects.requireNonNull(options).getRoom());
        }
        view.join(options);
        //JitsiMeetActivity.launch(this, options);

        setContentView(view);


        registerForBroadcastMessages();
    }


    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.tag("Jitsi_12345 Conference ").d("");

                    if (meetingHistory != null) {
                        meetingHistory.setStartTime(SharedObjectsAndAppController.getTodaysDate(Constants.DateFormats.DATETIME_FORMAT_24));
                        meetingHistory.setEndTime("");

                        saveMeetingDetails();
                    }

                    break;
                case PARTICIPANT_JOINED:
                    Timber.tag("Jitsi_12345 Participant").d("");
                    break;

                case CONFERENCE_TERMINATED:
                    Timber.tag("Jitsi_12345 Terminated ").d("");
                    if (meetingHistory != null) {
                        if (TextUtils.isEmpty(meetingHistory.getStartTime())) {
                            meetingHistory.setStartTime(SharedObjectsAndAppController.getTodaysDate(Constants.DateFormats.DATETIME_FORMAT_24));
                        }
                        meetingHistory.setEndTime(SharedObjectsAndAppController.getTodaysDate(Constants.DateFormats.DATETIME_FORMAT_24));
                        updateMeetingDetails();
                    }
                    hangUp();
                    onBackPressed();
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);

    }


    private void saveMeetingDetails() {
        mDatabaseManager.addMeetingHistory(meetingHistory);
    }

    private void updateMeetingDetails() {
        mDatabaseManager.updateMeetingHistory(meetingHistory);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        view.dispose();
        view = null;

        JitsiMeetActivityDelegate.onHostDestroy(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final String @NotNull [] permissions,
            final int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JitsiMeetActivityDelegate.onHostResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        JitsiMeetActivityDelegate.onHostPause(this);
    }

    @Override
    public void requestPermissions(String[] strings, int i, PermissionListener permissionListener) {
    }
}