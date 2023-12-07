package usama.utech.vidxa_video_conferencing.firebase_database_manager;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.models.MeetingHistory;
import usama.utech.vidxa_video_conferencing.models.Schedule;
import usama.utech.vidxa_video_conferencing.models.UserProfile;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;


public class DatabaseManager {

    private static final String TAG = DatabaseManager.class.getSimpleName();
    DatabaseReference databaseUsers;
    DatabaseReference databaseMeetingHistory;
    DatabaseReference databaseSchedule;
    Context context;
    ArrayList<MeetingHistory> arrMeetingHistory = new ArrayList<>();
    ArrayList<Schedule> arrSchedule = new ArrayList<>();
    ArrayList<UserProfile> arrUsers = new ArrayList<>();
    SharedObjectsAndAppController sharedObjectsAndAppController;
    UserProfile userProfile = null;
    private FirebaseDatabase mDatabase;
    private OnDatabaseDataChanged mDatabaseListener;
    private OnUserAddedListener onUserAddedListener;
    private OnUserListener onUserListener;
    private OnUserPasswordListener onUserPasswordListener;
    private OnScheduleListener onScheduleListener;
    private OnMeetingHistoryListener onMeetingHistoryListener;
    private OnUserDeleteListener onUserDeleteListener;

    public DatabaseManager(Context context) {
        this.context = context;

        sharedObjectsAndAppController = new SharedObjectsAndAppController(context);

        mDatabase = FirebaseDatabase.getInstance();

        databaseUsers = mDatabase.getReference(Constants.Table.USERS);
        databaseUsers.keepSynced(true);

        databaseMeetingHistory = mDatabase.getReference(Constants.Table.MEETING_HISTORY);
        databaseMeetingHistory.keepSynced(true);

        databaseSchedule = mDatabase.getReference(Constants.Table.SCHEDULE);
        databaseSchedule.keepSynced(true);
    }

    public void initUsers() {
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                try {
                    arrUsers = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        UserProfile customer = postSnapshot.getValue(UserProfile.class);
                        if (customer != null) {
                            arrUsers.add(customer);
                        }
                    }
                    if (mDatabaseListener != null) {
                        mDatabaseListener.onDataChanged(Constants.Table.USERS, dataSnapshot);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Timber.tag(TAG).d(databaseError.getMessage());
                if (mDatabaseListener != null) {
                    mDatabaseListener.onCancelled(databaseError);
                }
            }
        });
    }

    public ArrayList<UserProfile> getUsers() {
        return arrUsers;
    }

    public UserProfile getCurrentUser() {
        return userProfile;
    }

    public void addUser(UserProfile bean) {


        databaseUsers.child(bean.getId()).setValue(bean).addOnSuccessListener(aVoid -> {
            if (onUserAddedListener != null) {
                onUserAddedListener.onSuccess();
            }
        }).addOnFailureListener(e -> {
            if (onUserAddedListener != null) {
                onUserAddedListener.onFail();
            }
        });
    }

    public void updateUser(UserProfile bean) {
        DatabaseReference db = databaseUsers.child(bean.getId());
        db.setValue(bean).addOnSuccessListener(aVoid -> {
            if (onUserAddedListener != null) {
                onUserAddedListener.onSuccess();
            }
        }).addOnFailureListener(e -> {
            if (onUserAddedListener != null) {
                onUserAddedListener.onFail();
            }
        });
    }

    public void getUser(final String id) {
        Query query = databaseUsers.orderByChild("id").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(postSnapshot.getValue(UserProfile.class)).getId().equals(id)) {
                            userProfile = postSnapshot.getValue(UserProfile.class);
                        }

                    }
                }
                if (onUserListener != null) {
                    onUserListener.onUserFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (onUserListener != null) {
                    onUserListener.onUserNotFound();
                }
            }
        });
    }

    public void updateUserPassword(UserProfile bean) {
        DatabaseReference db = databaseUsers.child(bean.getId());
        db.setValue(bean).addOnSuccessListener(aVoid -> {
            if (onUserPasswordListener != null) {
                onUserPasswordListener.onPasswordUpdateSuccess();
            }
        }).addOnFailureListener(e -> {
            if (onUserPasswordListener != null) {
                onUserPasswordListener.onPasswordUpdateFail();
            }
        });
    }

    public void deleteUser(UserProfile bean) {
        databaseUsers.child(bean.getId()).removeValue((databaseError, databaseReference) -> {

            if (databaseError != null) {
                if (onUserDeleteListener != null) {
                    onUserDeleteListener.onUserDeleteFail();
                }
            } else {
                if (onUserDeleteListener != null) {
                    onUserDeleteListener.onUserDeleteSuccess();
                }
            }
        });
    }

    public void getScheduleByUser(final String id) {
        Query query = databaseSchedule.orderByChild("userId").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrSchedule = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(postSnapshot.getValue(Schedule.class)).getUserId().equals(id)) {
                            Schedule products = postSnapshot.getValue(Schedule.class);
                            if (products != null) {
                                arrSchedule.add(products);
                            }
                        }

                    }
                }
                if (mDatabaseListener != null) {
                    mDatabaseListener.onDataChanged(Constants.Table.SCHEDULE, dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (mDatabaseListener != null) {
                    mDatabaseListener.onCancelled(databaseError);
                }
            }
        });
    }

    public ArrayList<Schedule> getUserSchedule() {
        return arrSchedule;
    }

    public void addSchedule(Schedule bean) {
        String id = databaseSchedule.push().getKey();
        bean.setId(id);
        databaseSchedule.child(bean.getId()).setValue(bean).addOnSuccessListener(aVoid -> {
            if (onScheduleListener != null) {
                onScheduleListener.onAddSuccess();
            }
        }).addOnFailureListener(e -> {
            if (onScheduleListener != null) {
                onScheduleListener.onAddFail();
            }
        });
    }

    public void updateSchedule(Schedule bean) {
        DatabaseReference db = databaseSchedule.child(bean.getId());
        db.setValue(bean).addOnSuccessListener(aVoid -> {
            if (onScheduleListener != null) {
                onScheduleListener.onUpdateSuccess();
            }
        }).addOnFailureListener(e -> {
            if (onScheduleListener != null) {
                onScheduleListener.onUpdateFail();
            }
        });
    }

    public void deleteSchedule(Schedule bean) {
        databaseSchedule.child(bean.getId()).removeValue((databaseError, databaseReference) -> {

            if (databaseError != null) {
                if (onScheduleListener != null) {
                    onScheduleListener.onDeleteFail();
                }
            } else {
                if (onScheduleListener != null) {
                    onScheduleListener.onDeleteSuccess();
                    if (!TextUtils.isEmpty(bean.getReminderIdandrequestcode())) {
                        Constants.cancelReminderAlarm(context, Integer.parseInt(bean.getReminderIdandrequestcode()));
                    }
                }
            }
        });
    }

    public void addMeetingHistory(MeetingHistory bean) {
        databaseMeetingHistory.child(bean.getId()).setValue(bean).addOnSuccessListener(aVoid -> {
            if (onMeetingHistoryListener != null) {
                onMeetingHistoryListener.onAddSuccess();
            }
        }).addOnFailureListener(e -> {
            if (onMeetingHistoryListener != null) {
                onMeetingHistoryListener.onAddFail();
            }
        });
    }

    public String getKeyForMeetingHistory() {
        return databaseMeetingHistory.push().getKey();
    }

    public void updateMeetingHistory(MeetingHistory bean) {
        DatabaseReference db = databaseMeetingHistory.child(bean.getId());
        db.setValue(bean).addOnSuccessListener(
                aVoid -> {
                    if (onMeetingHistoryListener != null) {
                        onMeetingHistoryListener.onUpdateSuccess();
                    }
                }).addOnFailureListener(e -> {
            if (onMeetingHistoryListener != null) {
                onMeetingHistoryListener.onUpdateFail();
            }
        });
    }

    public void getMeetingHistoryByUser(final String userId) {
        Query query = databaseMeetingHistory.orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                arrMeetingHistory = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(postSnapshot.getValue(MeetingHistory.class)).getUserId().equals(userId)) {
                            MeetingHistory meetingHistory = postSnapshot.getValue(MeetingHistory.class);
                            if (meetingHistory != null) {
                                arrMeetingHistory.add(meetingHistory);
                            }
                        }

                    }
                }
                if (mDatabaseListener != null) {
                    mDatabaseListener.onDataChanged(Constants.Table.MEETING_HISTORY, dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                if (mDatabaseListener != null) {
                    mDatabaseListener.onCancelled(databaseError);
                }
            }
        });
    }

    public ArrayList<MeetingHistory> getUserMeetingHistory() {
        return arrMeetingHistory;
    }

    public void deleteMeetingHistory(MeetingHistory bean) {
        databaseMeetingHistory.child(bean.getId()).removeValue((databaseError, databaseReference) -> {

            if (databaseError != null) {
                if (onMeetingHistoryListener != null) {
                    onMeetingHistoryListener.onDeleteFail();
                }
            } else {
                if (onMeetingHistoryListener != null) {
                    onMeetingHistoryListener.onDeleteSuccess();
                }
            }
        });
    }

    public void setOnUserAddedListener(OnUserAddedListener listener) {
        onUserAddedListener = listener;
    }

    public void setDatabaseManagerListener(OnDatabaseDataChanged listener) {
        mDatabaseListener = listener;
    }

    public void setOnUserPasswordListener(OnUserPasswordListener onUserPasswordListener) {
        this.onUserPasswordListener = onUserPasswordListener;
    }

    public OnUserListener getOnUserListener() {
        return onUserListener;
    }

    public void setOnUserListener(OnUserListener onUserListener) {
        this.onUserListener = onUserListener;
    }

    public OnScheduleListener getOnScheduleListener() {
        return onScheduleListener;
    }

    public void setOnScheduleListener(OnScheduleListener onScheduleListener) {
        this.onScheduleListener = onScheduleListener;
    }

    public interface OnUserListener {
        void onUserFound();

        void onUserNotFound();
    }

    public interface OnDatabaseDataChanged {
        void onDataChanged(String url, DataSnapshot dataSnapshot);

        void onCancelled(DatabaseError error);
    }

    public interface OnUserAddedListener {
        void onSuccess();

        void onFail();
    }

    public interface OnUserDeleteListener {
        void onUserDeleteSuccess();

        void onUserDeleteFail();
    }

    public interface OnUserPasswordListener {
        void onPasswordUpdateSuccess();

        void onPasswordUpdateFail();
    }

    public interface OnScheduleListener {
        void onAddSuccess();

        void onUpdateSuccess();

        void onDeleteSuccess();

        void onAddFail();

        void onUpdateFail();

        void onDeleteFail();
    }

    public interface OnMeetingHistoryListener {
        void onAddSuccess();

        void onUpdateSuccess();

        void onDeleteSuccess();

        void onAddFail();

        void onUpdateFail();

        void onDeleteFail();
    }
}
