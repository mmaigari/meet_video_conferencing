package usama.utech.vidxa_video_conferencing.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.databinding.ActivityEmailVerificationBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class EmailVerificationActivity extends AppCompatActivity {


    SharedObjectsAndAppController sharedObjectsAndAppController;
    DatabaseManager databaseManager;
    private FirebaseAuth firebaseAuth;
    private ActivityEmailVerificationBinding binding;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailVerificationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        //Get Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        sharedObjectsAndAppController = new SharedObjectsAndAppController(EmailVerificationActivity.this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        databaseManager = new DatabaseManager(EmailVerificationActivity.this);

        verifyUser();

        binding.btnVerify.setOnClickListener(view1 -> {

            if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
                firebaseAuth.getCurrentUser().reload();

                SharedObjectsAndAppController.hideKeyboard(binding.btnVerify, EmailVerificationActivity.this);
                if (SharedObjectsAndAppController.isNetworkConnected(EmailVerificationActivity.this)) {
                    if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

                        Intent intent = new Intent(EmailVerificationActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                        finish();

                    } else {
                        Constants.showAlertDialog(getString(R.string.verifyemail), EmailVerificationActivity.this);
                    }
                } else {
                    Constants.showAlertDialog(getString(R.string.err_internet), EmailVerificationActivity.this);
                }
            }

        });

    }


    public void verifyUser() {

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        binding.txtEmail.setText(Objects.requireNonNull(user).getEmail());

        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EmailVerificationActivity.this,
                                getString(R.string.verificationemail) + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {

                        Constants.showAlertDialog(Objects.requireNonNull(task.getException()).getMessage(), EmailVerificationActivity.this);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
