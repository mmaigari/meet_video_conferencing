package usama.utech.vidxa_video_conferencing.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import me.ibrahimsn.lib.OnItemSelectedListener;
import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.databinding.ActivityMainBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.fragments.HomeFragment;
import usama.utech.vidxa_video_conferencing.fragments.MeetingHistoryFragment;
import usama.utech.vidxa_video_conferencing.fragments.ProfileFragment;
import usama.utech.vidxa_video_conferencing.fragments.ScheduleFragment;
import usama.utech.vidxa_video_conferencing.receiver.AlarmReceiver;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.OnSwipeTouchListener;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

@Keep
public class MainActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    DatabaseReference databaseReferenceUser;
    DatabaseManager databaseManager;
    SharedObjectsAndAppController sharedObjectsAndAppController;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;

    static int tabCounter = 0;

    public ActivityMainBinding binding;

    private static MainActivity sInstance = null;
    public OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener(MainActivity.getInstance()) {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return super.onTouch(v, event);
        }

        @Override
        public void onSwipeRight() {
            if (tabCounter > 0) {
                tabCounter--;

                changeFragmentOnSwipe(tabCounter);
            }
            super.onSwipeRight();
        }

        @Override
        public void onSwipeLeft() {
            if (tabCounter < 3) {
                tabCounter++;
                changeFragmentOnSwipe(tabCounter);
            }
            super.onSwipeLeft();
        }

        @Override
        public void onSwipeTop() {
            super.onSwipeTop();
        }

        @Override
        public void onSwipeBottom() {
            super.onSwipeBottom();
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleHelper.onAttach(newBase);

        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    // Getter to access Singleton instance
    public static MainActivity getInstance() {
        return sInstance;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        sInstance = this;
        sharedObjectsAndAppController = new SharedObjectsAndAppController(MainActivity.this);

        //get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();
        databaseManager = new DatabaseManager(MainActivity.this);


        databaseManager.setOnUserListener(new DatabaseManager.OnUserListener() {
            @Override
            public void onUserFound() {
                sharedObjectsAndAppController.setPreference(Constants.USER_INFO, new Gson().toJson(databaseManager.getCurrentUser()));
                updateFragments();
            }

            @Override
            public void onUserNotFound() {
                removeAllPreferenceOnLogout();
            }
        });
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference(Constants.Table.USERS);

        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Timber.tag("user").e("null");
                // user auth state is changed - user is null
                // launch login activity
                onLogout();
            } else {
                databaseManager.getUser(user.getUid());
            }
        };
        firebaseAuth.addAuthStateListener(authListener);

        loadFragment(new HomeFragment());
        binding.floatingTopBarNavigation.setOnItemSelectedListener((OnItemSelectedListener) i -> {


            Fragment fragment;
            Class fragmentClass;
            switch (i) {
                case 0:
                    fragmentClass = HomeFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                        loadFragment(fragment, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    fragmentClass = ProfileFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                        loadFragment(fragment, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    fragmentClass = MeetingHistoryFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                        loadFragment(fragment, 2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case 2:
                    fragmentClass = ScheduleFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                        loadFragment(fragment, 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

            return false;
        });

        binding.floatingTopBarNavigation.setOnTouchListener(swipeTouchListener);


        //TODO
        //   setNotification();

    }

    private void changeFragmentOnSwipe(int position) {

        Fragment fragment;
        Class fragmentClass;
        switch (position) {
            case 0:
                fragmentClass = HomeFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    loadFragment(fragment, 0);
                    binding.floatingTopBarNavigation.setItemActiveIndex(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                fragmentClass = ProfileFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    loadFragment(fragment, 1);
                    binding.floatingTopBarNavigation.setItemActiveIndex(3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                fragmentClass = MeetingHistoryFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    loadFragment(fragment, 2);
                    binding.floatingTopBarNavigation.setItemActiveIndex(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case 2:
                fragmentClass = ScheduleFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    loadFragment(fragment, 3);
                    binding.floatingTopBarNavigation.setItemActiveIndex(2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (SharedObjectsAndAppController.isNetworkConnected(MainActivity.this)) {
            AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                    .withListener(new AppUpdaterUtils.UpdateListener() {
                        @Override
                        public void onSuccess(Update update, Boolean isUpdateAvailable) {
                            if (isUpdateAvailable) {
                                launchUpdateDialog(update.getLatestVersion());
                            }
                        }

                        @Override
                        public void onFailed(AppUpdaterError error) {

                        }
                    });
            appUpdaterUtils.start();
        }
    }

    private void launchUpdateDialog(String onlineVersion) {

        try {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
            materialAlertDialogBuilder.setMessage(getString(R.string.update) + onlineVersion + getString(R.string.newversionisavaliable) + getString(R.string.app_name));
            materialAlertDialogBuilder.setCancelable(false).setPositiveButton(getResources().getString(R.string.update_now), (dialog, which) -> {
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            });
            materialAlertDialogBuilder.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFragments() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setUserData();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.doubleBackToExitPressedOnce = false;
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            finish();
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);

    }

    public void loadFragment(Fragment fragment) {

        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.flContent, fragment, backStateName);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    public void loadFragment(Fragment fragment, int menuItem) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.flContent, fragment, backStateName);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(backStateName);
            ft.commit();
            // menuItem.setChecked(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onLogout() {
        sharedObjectsAndAppController.removeSinglePreference(Constants.USER_INFO);

        Intent intent = new Intent(MainActivity.this, LoginSignupSelectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    public void removeAllPreferenceOnLogout() {
        try {
            firebaseAuth.signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("ShortAlarm")
    public void setNotification() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 100, pendingIntent);
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, AlarmReceiver.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


}
