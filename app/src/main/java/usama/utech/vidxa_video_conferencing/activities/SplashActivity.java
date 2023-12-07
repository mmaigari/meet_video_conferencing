package usama.utech.vidxa_video_conferencing.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.databinding.ActivitySplashBinding;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

/**
 *
 */
@Keep
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;

    SharedObjectsAndAppController sharedObjectsAndAppController;

    private FirebaseAuth firebaseAuth;
    private ActivitySplashBinding binding;

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleHelper.onAttach(newBase);

        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null) {
            handleIntent(getIntent());
        }
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        String datrmodeON_Off = Constants.getDarkmodeStatus(SplashActivity.this);

        if (datrmodeON_Off.equals("true")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        }else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }

        //Get Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        sharedObjectsAndAppController = new SharedObjectsAndAppController(SplashActivity.this);


        binding.txtVersionName.setText(getString(R.string.version, SharedObjectsAndAppController.getVersion(SplashActivity.this)));


        if (getIntent().getData() != null) {
            handleIntent(getIntent());
        }


        new CountDownTimer(SPLASH_TIME_OUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                Intent intentLogin;

                if (firebaseAuth.getCurrentUser() != null) {
                    if (checkIfEmailVerified()) {
                        intentLogin = new Intent(SplashActivity.this, MainActivity.class);
                        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentLogin);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                        finish();
                    } else {


                        intentLogin = new Intent(SplashActivity.this, LoginSignupSelectActivity.class);
                        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentLogin);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                        finish();
                    }
                } else {
                    intentLogin = new Intent(SplashActivity.this, LoginSignupSelectActivity.class);
                    intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentLogin);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                    finish();
                }
            }
        }.start();

    }


    void handleIntent(Intent intent) {

        System.out.println("mydagdhagjas " + intent.getData().toString());

        Uri uri = Uri.parse(intent.getData().toString());
        String code = uri.getQueryParameter("code");

        System.out.println("mydagdhagjas code " + code);
        Constants.MEETING_ID = code;

    }


    private boolean checkIfEmailVerified() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (Objects.requireNonNull(user).isEmailVerified()) {
            // user is verified
            return true;
        } else {
            // email is not verified
            // NOTE: don't forget to log out the user.
            firebaseAuth.signOut();
            return false;
        }
    }
}
