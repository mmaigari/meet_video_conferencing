package usama.utech.vidxa_video_conferencing.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.databinding.ActivityRegisterBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.UserProfile;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class RegisterActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST = 1;

    SharedObjectsAndAppController sharedObjectsAndAppController;
    DatabaseManager databaseManager;

    boolean isExist = false;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference dfUser;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
    private ActivityRegisterBinding binding;

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
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Get Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();
        databaseManager = new DatabaseManager(RegisterActivity.this);
        dfUser = FirebaseDatabase.getInstance().getReference(Constants.Table.USERS);

        sharedObjectsAndAppController = new SharedObjectsAndAppController(RegisterActivity.this);


        setEdtListeners();
        initbtnclicklistner();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void initbtnclicklistner() {


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            binding.imgSigninhero.setBackgroundResource(R.drawable.ic_login_hero_xml);
        } else {
            binding.imgSigninhero.setBackgroundResource(R.drawable.ic_login_hero);

        }

        binding.btnRegister.setOnClickListener(view -> {


            SharedObjectsAndAppController.hideKeyboard(binding.btnRegister, RegisterActivity.this);
            if (SharedObjectsAndAppController.isNetworkConnected(RegisterActivity.this)) {

                if (!validateName()) {
                    return;
                }
                if (!validateEmail()) {
                    return;
                }
                if (isExist) {
                    binding.inputLayoutEmail.setErrorEnabled(true);
                    binding.inputLayoutEmail.setError(getString(R.string.email_exists));
                    return;
                }
                if (!validatePassword()) {
                    return;
                }

                binding.btnRegister.setEnabled(false);

                binding.btnRegister.startAnimation();

                checkUserLogin();

            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), RegisterActivity.this);
            }
        });
        binding.btnGoogleSignIn.setOnClickListener(view -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, SIGN_IN_REQUEST);
        });
        binding.llLogin.setOnClickListener(view -> {
            Intent intentLogin;
            intentLogin = new Intent(RegisterActivity.this, LoginActivity.class);
            intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentLogin);
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void checkUserLogin() {
        showProgressDialog();
        //create user
        firebaseAuth.createUserWithEmailAndPassword(Objects.requireNonNull(binding.edtEmail.getText()).toString().trim(), Objects.requireNonNull(binding.edtPassword.getText()).toString().trim())
                .addOnCompleteListener(this, task -> {
                    dismissProgressDialog();
                    binding.btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {


                            UserProfile userProfile = new UserProfile();
                            userProfile.setId(user.getUid());
                            userProfile.setName(Objects.requireNonNull(binding.edtName.getText()).toString().trim());
                            userProfile.setEmail(binding.edtEmail.getText().toString().trim());

                            addUser(userProfile);

                            Intent intentLogin;
                            intentLogin = new Intent(RegisterActivity.this, EmailVerificationActivity.class);
                            intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentLogin);
                            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                            finish();

                        }
                        binding.btnRegister.revertAnimation();
                    } else {
                        binding.btnRegister.revertAnimation();
                        binding.btnRegister.setEnabled(true);

                        Constants.showAlertDialog(getString(R.string.regfailed) + Objects.requireNonNull(task.getException()).getMessage(), RegisterActivity.this);
                    }
                });
    }

    private void addUser(UserProfile userProfile) {
        databaseManager.addUser(userProfile);
    }

    public boolean validateName() {
        if (TextUtils.isEmpty(Objects.requireNonNull(binding.edtName.getText()).toString().trim())) {
            binding.inputLayoutName.setErrorEnabled(true);
            binding.inputLayoutName.setError(getString(R.string.err_name));
            return false;
        }
        return true;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        finish();
    }

    private boolean checkEmailExists(final String email) {
        isExist = false;
        Query query = dfUser.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(postSnapshot.getValue(UserProfile.class)).getEmail().equals(email)) {
                            isExist = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return isExist;
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
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(AuthCredential credential, GoogleSignInAccount account) {

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        UserProfile userProfile = new UserProfile();
                        userProfile.setId(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                        userProfile.setName(account.getDisplayName());

                        userProfile.setEmail(account.getEmail());
                        userProfile.setProfile_pic(Objects.requireNonNull(account.getPhotoUrl()).toString());
                        if (!isExist) {
                            databaseManager.addUser(userProfile);
                        } else {
                            databaseManager.updateUser(userProfile);
                        }
                        googleSignInClient.signOut();

                        Intent intentLogin;
                        intentLogin = new Intent(RegisterActivity.this, MainActivity.class);
                        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentLogin);
                        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);

                        finish();


                    } else {
                        Constants.showAlertDialog("Registration failed, " + Objects.requireNonNull(task.getException()).getMessage(), RegisterActivity.this);
                    }

                });
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
                if (charSequence.toString().length() > 0) {
                    checkEmailExists(charSequence.toString());
                } else if (charSequence.toString().length() == 0) {
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
        binding.edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.inputLayoutName.setErrorEnabled(false);
                binding.inputLayoutName.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void showProgressDialog() {
        try {
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setCancelable(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if (!RegisterActivity.this.isFinishing()) {
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

    public void onLoginClick(View view) {
        Intent intentLogin;
        intentLogin = new Intent(RegisterActivity.this, LoginActivity.class);
        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentLogin);
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
