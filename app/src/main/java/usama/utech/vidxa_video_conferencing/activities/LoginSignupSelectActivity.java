package usama.utech.vidxa_video_conferencing.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.databinding.ActivityLoginsignupselectBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.MeetingHistory;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;


public class LoginSignupSelectActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 10001;
    private static final int SETTINGS_REQUEST_CODE = 10002;
    SharedObjectsAndAppController sharedObjectsAndAppController;

    String[] appPermissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    DatabaseManager databaseManager;
    boolean isMeetingExist = false;
    TextInputLayout inputLayoutCode, inputLayoutName;
    TextInputEditText edtCode, edtName;
    private DatabaseReference databaseReferenceMeetingHistory;
    private ActivityLoginsignupselectBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginsignupselectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        sharedObjectsAndAppController = new SharedObjectsAndAppController(LoginSignupSelectActivity.this);

        databaseManager = new DatabaseManager(LoginSignupSelectActivity.this);
        databaseReferenceMeetingHistory = FirebaseDatabase.getInstance().getReference(Constants.Table.MEETING_HISTORY);


        initbtnclickListner();
    }

    void initbtnclickListner() {


        binding.btnLogin.setOnClickListener(view -> {
            if (SharedObjectsAndAppController.isNetworkConnected(LoginSignupSelectActivity.this)) {
                startActivity(new Intent(LoginSignupSelectActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), LoginSignupSelectActivity.this);
            }
        });

        binding.btnSignUp.setOnClickListener(view -> {
            if (SharedObjectsAndAppController.isNetworkConnected(LoginSignupSelectActivity.this)) {
                startActivity(new Intent(LoginSignupSelectActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), LoginSignupSelectActivity.this);
            }
        });
        binding.btnJoin.setOnClickListener(view -> {
            if (SharedObjectsAndAppController.isNetworkConnected(LoginSignupSelectActivity.this)) {
                if (checkAppPermissions(appPermissions)) {
                    showMeetingCodeDialog();
                } else {
                    requestAppPermissions(appPermissions);
                }
            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), LoginSignupSelectActivity.this);
            }
        });

    }


    private void checkMeetingExists(final String meeting_id) {
        isMeetingExist = false;
        Query query = databaseReferenceMeetingHistory.orderByChild("meeting_id").equalTo(meeting_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Timber.tag("Meeting").e("exists");
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(postSnapshot.getValue(MeetingHistory.class)).getMeeting_id().equals(meeting_id)) {
                            isMeetingExist = true;
                        }
                    }
                } else {
                    isMeetingExist = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isMeetingExist = false;
            }
        });
    }

    public void showMeetingCodeDialog() {
        final Dialog dialogDate = new Dialog(LoginSignupSelectActivity.this);
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDate.setContentView(R.layout.dialog_meeting_code);
        dialogDate.setCancelable(true);

        Window window = dialogDate.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.dimAmount = 0.8f;
        window.setAttributes(wlp);
        dialogDate.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        inputLayoutCode = dialogDate.findViewById(R.id.inputLayoutCode);
        inputLayoutName = dialogDate.findViewById(R.id.inputLayoutName);
        edtCode = dialogDate.findViewById(R.id.edtCode);
        edtName = dialogDate.findViewById(R.id.edtName);

        edtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutCode.setErrorEnabled(false);
                inputLayoutCode.setError("");
                if (charSequence.length() == (Constants.getMeetingCode(LoginSignupSelectActivity.this).length())) {
                    checkMeetingExists(charSequence.toString());
                } else {
                    isMeetingExist = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutName.setErrorEnabled(false);
                inputLayoutName.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Button btnAdd = dialogDate.findViewById(R.id.btnAdd);
        Button btnCancel = dialogDate.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(v -> {

            if (TextUtils.isEmpty(Objects.requireNonNull(edtCode.getText()).toString().trim())) {
                inputLayoutCode.setErrorEnabled(true);
                inputLayoutCode.setError(getString(R.string.errMeetingCode));
                return;
            }
            if (edtCode.getText().toString().length() < 11) {
                inputLayoutCode.setErrorEnabled(true);
                inputLayoutCode.setError(getString(R.string.errMeetingCodeInValid));
                return;
            }
            if (!isMeetingExist) {
                Constants.showAlertDialog(getResources().getString(R.string.meeting_not_exist), LoginSignupSelectActivity.this);
                return;
            }
            if (TextUtils.isEmpty(Objects.requireNonNull(edtName.getText()).toString().trim())) {
                inputLayoutName.setErrorEnabled(true);
                inputLayoutName.setError(getString(R.string.err_name));
                return;
            }

            Constants.MEETING_ID = edtCode.getText().toString().trim();
            Constants.NAME = edtName.getText().toString().trim();

            dialogDate.dismiss();

            startActivity(new Intent(LoginSignupSelectActivity.this, MeetingActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        });

        btnCancel.setOnClickListener(view -> dialogDate.dismiss());

        if (!dialogDate.isShowing()) {
            dialogDate.show();
        }
    }

    public boolean checkAppPermissions(String[] appPermissions) {
        //check which permissions are granted
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }


        return listPermissionsNeeded.isEmpty();
    }

    private void requestAppPermissions(String[] appPermissions) {
        ActivityCompat.requestPermissions(this, appPermissions, PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }
            if (deniedCount == 0) {
                Timber.tag("Permissions").e("All permissions are granted!");
                showMeetingCodeDialog();
            } else {
                //some permissions are denied
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();
                    //permission is denied and never asked is not checked
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(LoginSignupSelectActivity.this);
                        materialAlertDialogBuilder.setMessage(getString(R.string.permission_msg));
                        materialAlertDialogBuilder.setCancelable(false)
                                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.cancel())
                                .setPositiveButton(getString(R.string.yes_grant_permission), (dialog, id) -> {
                                    dialog.cancel();
                                    if (!checkAppPermissions(appPermissions)) {
                                        requestAppPermissions(appPermissions);
                                    }
                                });
                        materialAlertDialogBuilder.show();

                        break;
                    } else {//permission is denied and never asked is checked
                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(LoginSignupSelectActivity.this);
                        materialAlertDialogBuilder.setMessage(getString(R.string.permission_msg_never_checked));
                        materialAlertDialogBuilder.setCancelable(false)
                                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.cancel())
                                .setPositiveButton(getString(R.string.go_to_settings), (dialog, id) -> {
                                    dialog.cancel();
                                    openSettings();
                                });
                        materialAlertDialogBuilder.show();

                        break;
                    }

                }
            }
        }
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", LoginSignupSelectActivity.this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (checkAppPermissions(appPermissions)) {
                    showMeetingCodeDialog();
                } else {
                    requestAppPermissions(appPermissions);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


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
}
