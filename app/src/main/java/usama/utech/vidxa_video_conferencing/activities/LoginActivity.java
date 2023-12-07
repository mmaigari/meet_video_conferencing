package usama.utech.vidxa_video_conferencing.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.databinding.ActivityLoginBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.UserProfile;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int SIGN_IN_REQUEST = 1;

    SharedObjectsAndAppController sharedObjectsAndAppController;
    DatabaseManager mDatabaseManager;
    DatabaseManager databaseManager;
    boolean isExist = false;
    UserProfile userProfileData = null;
    TextInputLayout inputLayoutFPEmail;
    TextInputEditText edtFPEmail;
    private DatabaseReference dfUser;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
    private ActivityLoginBinding binding;

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
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        //Get Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();
        databaseManager = new DatabaseManager(LoginActivity.this);

        sharedObjectsAndAppController = new SharedObjectsAndAppController(LoginActivity.this);

        setEdtListeners();
        initbtnclicklistner();

        mDatabaseManager = new DatabaseManager(LoginActivity.this);
        dfUser = FirebaseDatabase.getInstance().getReference(Constants.Table.USERS);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (SharedObjectsAndAppController.isNetworkConnected(LoginActivity.this)) {
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
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(LoginActivity.this);
            materialAlertDialogBuilder.setMessage("Update " + onlineVersion + " is available to download. Downloading the latest update you will get the latest features," +
                    "improvements and bug fixes of " + getString(R.string.app_name));
            materialAlertDialogBuilder.setCancelable(false).setPositiveButton(getResources().getString(R.string.update_now), (dialog, which) -> {
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            });
            materialAlertDialogBuilder.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initbtnclicklistner() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            binding.imgSignuphero.setBackgroundResource(R.drawable.ic_register_hero_xml);
        } else {
            binding.imgSignuphero.setBackgroundResource(R.drawable.ic_register_hero);

        }


        binding.btnLogin.setOnClickListener(view -> {
            SharedObjectsAndAppController.hideKeyboard(binding.btnLogin, LoginActivity.this);
            if (SharedObjectsAndAppController.isNetworkConnected(LoginActivity.this)) {

                if (!validateEmail()) {
                    return;
                }

                if (!validatePassword()) {
                    return;
                }

                binding.btnLogin.setEnabled(false);

                checkUserLogin();
            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), LoginActivity.this);
            }
        });

        binding.llCreateAccount.setOnClickListener(view -> {
            Intent intentLogin;
            intentLogin = new Intent(LoginActivity.this, RegisterActivity.class);
            intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentLogin);
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);

        });

        binding.txtForgotPassword.setOnClickListener(view -> {
            if (SharedObjectsAndAppController.isNetworkConnected(LoginActivity.this)) {
                showForgotPasswordDialog();
            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), LoginActivity.this);
            }
        });

        binding.btnGoogleSignIn.setOnClickListener(view -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, SIGN_IN_REQUEST);
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    checkEmailExists(account.getEmail());

                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    firebaseAuthWithGoogle(credential, account);
                }
            } catch (ApiException e) {
                Timber.w(":failed code=%s", e.getStatusCode());
            }
        }
    }

    private void checkEmailExists(final String email) {
        isExist = false;
        Query query = dfUser.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Timber.e("exists");
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(postSnapshot.getValue(UserProfile.class)).getEmail().equals(email)) {
                            isExist = true;
                            userProfileData = new UserProfile();
                            userProfileData = postSnapshot.getValue(UserProfile.class);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void firebaseAuthWithGoogle(AuthCredential credential, GoogleSignInAccount account) {

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Timber.tag("signInWith").d("Credential:onComplete:%s", task.isSuccessful());
                    if (task.isSuccessful()) {
                        UserProfile userProfile = new UserProfile();
                        if (userProfileData != null) {
                            userProfile = userProfileData;
                        }
                        userProfile.setId(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                        userProfile.setEmail(account.getEmail());
                        userProfile.setProfile_pic(Objects.requireNonNull(account.getPhotoUrl()).toString());
                        if (!isExist) {
                            userProfile.setName(account.getDisplayName());
                            databaseManager.addUser(userProfile);
                        } else {
                            databaseManager.updateUser(userProfile);
                        }
                        googleSignInClient.signOut();

                        Intent intentLogin;
                        intentLogin = new Intent(LoginActivity.this, MainActivity.class);
                        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentLogin);
                        finish();

                    } else {
                        Timber.tag("signInWith").w("Credential%s", Objects.requireNonNull(task.getException()).getMessage());
                        task.getException().printStackTrace();
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void checkUserLogin() {
        showProgressDialog();
        //authenticate user
        firebaseAuth.signInWithEmailAndPassword(Objects.requireNonNull(binding.edtEmail.getText()).toString().trim(), Objects.requireNonNull(binding.edtPassword.getText()).toString().trim())
                .addOnCompleteListener(LoginActivity.this, task -> {
                    binding.btnLogin.setEnabled(true);
                    dismissProgressDialog();


                    if (!task.isSuccessful()) {
                        // there was an error
                        Constants.showAlertDialog("Authentication failed, check your email and password or sign up", LoginActivity.this);
                    } else {

                        UserProfile userProfile = new UserProfile();
                        userProfile.setId(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                        userProfile.setName("");
                        userProfile.setEmail(binding.edtEmail.getText().toString());
                        userProfile.setProfile_pic("");
                        if (!isExist) {
                            databaseManager.addUser(userProfile);
                        }

                        Intent intentLogin;
                        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
                            intentLogin = new Intent(LoginActivity.this, MainActivity.class);
                        } else {
                            intentLogin = new Intent(LoginActivity.this, EmailVerificationActivity.class);
                        }
                        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentLogin);
                        finish();
                    }
                });
    }

    public void showForgotPasswordDialog() {
        final Dialog dialogDate = new Dialog(LoginActivity.this);
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDate.setContentView(R.layout.dialog_forgot_password);
        dialogDate.setCancelable(true);

        Window window = dialogDate.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.dimAmount = 0.8f;
        window.setAttributes(wlp);
        dialogDate.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        inputLayoutFPEmail = dialogDate.findViewById(R.id.inputLayoutFPEmail);

        edtFPEmail = dialogDate.findViewById(R.id.edtFPEmail);

        edtFPEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutFPEmail.setErrorEnabled(false);
                inputLayoutFPEmail.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Button btnAdd = dialogDate.findViewById(R.id.btnAdd);
        Button btnCancel = dialogDate.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(v -> {

            if (TextUtils.isEmpty(Objects.requireNonNull(edtFPEmail.getText()).toString().trim())) {
                inputLayoutFPEmail.setErrorEnabled(true);
                inputLayoutFPEmail.setError(getString(R.string.errEmailRequired));
                return;
            }

            if (!Constants.isValidEmail(edtFPEmail.getText().toString().trim())) {
                inputLayoutFPEmail.setErrorEnabled(true);
                inputLayoutFPEmail.setError(getString(R.string.errValidEmailRequired));
                return;
            }

            firebaseAuth.sendPasswordResetEmail(edtFPEmail.getText().toString().trim())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(LoginActivity.this);
                            materialAlertDialogBuilder.setMessage(getString(R.string.we_have_sent_instructions));
                            materialAlertDialogBuilder.setCancelable(false).setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                dialog.dismiss();
                                dialogDate.dismiss();
                            });
                            materialAlertDialogBuilder.show();
                        } else {
                            Constants.showAlertDialog(Objects.requireNonNull(task.getException()).getMessage(), LoginActivity.this);
                        }

                    });
        });

        btnCancel.setOnClickListener(view -> dialogDate.dismiss());

        if (!dialogDate.isShowing()) {
            dialogDate.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setEdtListeners() {

        binding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.inputLayoutEmail.setErrorEnabled(false);
                binding.inputLayoutEmail.setError("");
                if (!TextUtils.isEmpty(Objects.requireNonNull(binding.edtEmail.getText()).toString().trim())) {
                    checkEmailExists(binding.edtEmail.getText().toString().trim());
                } else {
                    isExist = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.inputLayoutPassword.setErrorEnabled(false);
                binding.inputLayoutPassword.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public boolean validateEmail() {
        if (TextUtils.isEmpty(Objects.requireNonNull(binding.edtEmail.getText()).toString().trim())) {
            binding.inputLayoutEmail.setErrorEnabled(true);
            binding.inputLayoutEmail.setError(getString(R.string.errEmailRequired));
            return false;
        } else if (!Constants.isValidEmail(binding.edtEmail.getText().toString().trim())) {
            binding.inputLayoutEmail.setErrorEnabled(true);
            binding.inputLayoutEmail.setError(getString(R.string.errValidEmailRequired));
            return false;
        }
        return true;
    }

    public boolean validatePassword() {
        if (TextUtils.isEmpty(Objects.requireNonNull(binding.edtPassword.getText()).toString().trim())) {
            binding.inputLayoutPassword.setErrorEnabled(true);
            binding.inputLayoutPassword.setError(getString(R.string.errPasswordRequired));
            return false;
        } else if (binding.edtPassword.getText().toString().trim().length() < 6) {
            binding.inputLayoutPassword.setErrorEnabled(true);
            binding.inputLayoutPassword.setError(getString(R.string.errPasswordTooShort));
            return false;
        }
        return true;
    }

    public void showProgressDialog() {
        try {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage(getString(R.string.authenticating));
            progressDialog.setCancelable(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if (!LoginActivity.this.isFinishing()) {
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onLoginClick(View view) {
        Intent intentLogin;
        intentLogin = new Intent(LoginActivity.this, RegisterActivity.class);
        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentLogin);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);


    }

}
