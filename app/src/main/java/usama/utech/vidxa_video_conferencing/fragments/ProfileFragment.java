package usama.utech.vidxa_video_conferencing.fragments;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.activities.MainActivity;
import usama.utech.vidxa_video_conferencing.activities.RateUsAndPrivacyWebView;
import usama.utech.vidxa_video_conferencing.databinding.FragmentProfileSettingsBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.UserProfile;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class ProfileFragment extends Fragment implements DatabaseManager.OnUserAddedListener {

    SharedObjectsAndAppController sharedObjectsAndAppController;
    private FragmentProfileSettingsBinding binding;
    public static final int REQUEST_CODE_TAKE_PICTURE = 2222, SELECT_FILE_GALLERY = 1111;
    private static final int PERMISSION_REQUEST_CODE = 10001;
    private static final int SETTINGS_REQUEST_CODE = 10002;

    DatabaseManager mDatabaseManager;
    String[] appPermissions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    ProgressDialog progressDialog;
    UserProfile userProfile;
    Uri imageUri;
    private DatabaseReference dfUsers;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;


    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        sharedObjectsAndAppController = new SharedObjectsAndAppController(requireActivity());


        firebaseAuth = FirebaseAuth.getInstance();

        userProfile = sharedObjectsAndAppController.getUserInfo();
        dfUsers = FirebaseDatabase.getInstance().getReference(Constants.Table.USERS);
        mDatabaseManager = new DatabaseManager(requireActivity());
        mDatabaseManager.setOnUserAddedListener(this);
        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(requireActivity());


        setEdtListeners();

        setData();

        initBtnclicklistners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showLanguageDialog() {
        setDefaultLanguage(requireActivity());
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_language, null);
        builder.setView(view);

        ImageView en = view.findViewById(R.id.img_english);
        ImageView hi = view.findViewById(R.id.img_hindi);


        ImageView de = view.findViewById(R.id.img_german);
        ImageView fr = view.findViewById(R.id.img_french);

        ImageView es = view.findViewById(R.id.img_spanish);
        ImageView ar = view.findViewById(R.id.img_arabic);
        ar.setOnClickListener(view1 -> {

            LocaleHelper.setLocale(requireActivity(), "ar");
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

            Constants.setLanguageCode(requireActivity(), "ar");

            requireActivity().recreate();

            //              ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setCurrentActiveItem(0);
        });
        en.setOnClickListener(view12 -> {

            LocaleHelper.setLocale(requireActivity(), "en");
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            Constants.setLanguageCode(requireActivity(), "en");

            requireActivity().recreate();

            //              ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setCurrentActiveItem(0);
        });
        hi.setOnClickListener(view13 -> {
            LocaleHelper.setLocale(requireActivity(), "hi");
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            Constants.setLanguageCode(requireActivity(), "hi");

            requireActivity().recreate();

            //           ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setCurrentActiveItem(0);
        });

        de.setOnClickListener(view14 -> {

            LocaleHelper.setLocale(requireActivity(), "de");
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            Constants.setLanguageCode(requireActivity(), "de");

            requireActivity().recreate();

            //          ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setCurrentActiveItem(0);
        });
        fr.setOnClickListener(view15 -> {
            LocaleHelper.setLocale(requireActivity(), "fr");
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            Constants.setLanguageCode(requireActivity(), "fr");

            requireActivity().recreate();

            //      ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setCurrentActiveItem(0);
        });


        es.setOnClickListener(view16 -> {
            LocaleHelper.setLocale(requireActivity(), "es");
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            Constants.setLanguageCode(requireActivity(), "es");

            requireActivity().recreate();

//                ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setCurrentActiveItem(0);
        });


        final AlertDialog dialog = builder.create();
        dialog.show();


        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

    }

    public static void setDefaultLanguage(Activity activity) {
        Locale locale = new Locale(Constants.getLanguageCode(activity));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        activity.getBaseContext().getResources().updateConfiguration(config,
                activity.getBaseContext().getResources().getDisplayMetrics());
    }

    private void initBtnclicklistners() {


        String datrmodeON_Off = Constants.getDarkmodeStatus(requireActivity());

        binding.darkmodeSwitch.setChecked(datrmodeON_Off.equals("true"));


        binding.rlPickImage.setOnClickListener(view -> {
            if (SharedObjectsAndAppController.isNetworkConnected(requireActivity())) {
                if (checkAppPermissions(appPermissions)) {
                    showImagePickerDialog();
                } else {
                    requestAppPermissions(appPermissions);
                }
            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), requireActivity());
            }
        });

        binding.txtSave.setOnClickListener(view -> {
            SharedObjectsAndAppController.hideKeyboard(binding.txtSave, requireActivity());
            if (TextUtils.isEmpty(Objects.requireNonNull(binding.edtName.getText()).toString().trim())) {
                binding.inputLayoutName.setErrorEnabled(true);
                binding.inputLayoutName.setError(getString(R.string.err_name));
            } else {
                updateUser();
            }
        });

        binding.llRemoveAds.setOnClickListener(v -> {
            //TODO Remove ads
        });


        binding.llLogout.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).removeAllPreferenceOnLogout();
            ((MainActivity) requireActivity()).onLogout();
        });

        binding.llRateUs.setOnClickListener(view -> {
            if (SharedObjectsAndAppController.isNetworkConnected(requireActivity())) {
                final String appPackageName = requireActivity().getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            } else {
                Constants.showAlertDialog(getString(R.string.err_internet), requireActivity());
            }
        });

        binding.llLanguage.setOnClickListener(view -> showLanguageDialog());

        binding.darkmodeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {


            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                Constants.setDarkmodeStatus(requireActivity(), "true");


            } else {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Constants.setDarkmodeStatus(requireActivity(), "false");
            }
            requireActivity().recreate();
            ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setItemActiveIndex(0);

        });

        binding.llprivacy.setOnClickListener(view -> {

            startActivity(new Intent(requireActivity(), RateUsAndPrivacyWebView.class).putExtra("link", getString(R.string.privacy_url)));
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.stay);

        });

    }

    private void setData() {
        if (userProfile != null) {

            if (!TextUtils.isEmpty(userProfile.getProfile_pic())) {

                Picasso.get().load(userProfile.getProfile_pic()).error(R.drawable.user_avatar).into(binding.imgUser);
            }

            if (!TextUtils.isEmpty(userProfile.getName())) {
                binding.edtName.setText(userProfile.getName());
                binding.edtName.setSelection(userProfile.getName().length());
            }

            binding.inputLayoutEmail.setEnabled(false);

            if (!TextUtils.isEmpty(userProfile.getEmail())) {
                binding.txtEmail.setText(userProfile.getEmail());
            } else {
                binding.txtEmail.setText("");
            }

        }
    }

    private void updateUser() {
        userProfile.setName(Objects.requireNonNull(binding.edtName.getText()).toString());
        mDatabaseManager.updateUser(userProfile);
    }

    private void setEdtListeners() {

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

        binding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.inputLayoutEmail.setErrorEnabled(false);
                binding.inputLayoutEmail.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }


    public boolean checkAppPermissions(String[] appPermissions) {

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (ContextCompat.checkSelfPermission(requireActivity(), perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }


        return listPermissionsNeeded.isEmpty();

    }

    private void requestAppPermissions(String[] appPermissions) {
        ActivityCompat.requestPermissions(requireActivity(), appPermissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
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
                showImagePickerDialog();
            } else {
                //some permissions are denied
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();
                    //permission is denied and never asked is not checked
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permName)) {
                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireActivity());
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
                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireActivity());
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
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SETTINGS_REQUEST_CODE:

                if (resultCode == Activity.RESULT_OK) {
                    if (checkAppPermissions(appPermissions)) {
                        showImagePickerDialog();
                    } else {
                        requestAppPermissions(appPermissions);
                    }
                }
                break;
            case REQUEST_CODE_TAKE_PICTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        showCropImageDialog(imageUri);
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        Timber.tag("TAKE_PICTURE").e("RESULT_CANCELED");
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            case SELECT_FILE_GALLERY:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        if (data != null) {
                            Uri uri = data.getData();
                            showCropImageDialog(uri);
                        }
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        Timber.tag("FILE_GALLERY").e("RESULT_CANCELED");
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showCropImageDialog(Uri uri) {
        final Dialog dialogDate = new Dialog(requireActivity());
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDate.setContentView(R.layout.dialog_crop_image);
        dialogDate.setCancelable(true);

        Window window = dialogDate.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.dimAmount = 0.8f;
        window.setAttributes(wlp);
        dialogDate.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button btnCrop = dialogDate.findViewById(R.id.btnCrop);
        CropImageView cropImageView = dialogDate.findViewById(R.id.cropImageView);
        cropImageView.setImageUriAsync(uri);
        cropImageView.setGuidelines(CropImageView.Guidelines.ON);
        cropImageView.setAspectRatio(1, 1);

        btnCrop.setOnClickListener(v -> {
            dialogDate.dismiss();

            Bitmap bitmapNew = null;
            Bitmap croppedBitmap = cropImageView.getCroppedImage();

            final int maxSize = 600;
            int outWidth;
            int outHeight;
            int inWidth = croppedBitmap.getWidth();
            int inHeight = croppedBitmap.getHeight();
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }

            bitmapNew = Bitmap.createScaledBitmap(croppedBitmap, outWidth, outHeight, true);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File fileImageSend = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            try {
                FileOutputStream fo = new FileOutputStream(fileImageSend);
                bitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, fo);
                fo.flush();
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri uri1 = Uri.fromFile(fileImageSend);
            UploadImageFileToFirebaseStorage(uri1);


        });

        dialogDate.show();
    }

    public void UploadImageFileToFirebaseStorage(Uri uri) {

        try {
            if (uri != null) {
                progressDialog.setTitle(getString(R.string.uploading_image));
                progressDialog.show();


                StorageReference storageReference2nd = storageReference.child(Constants.Storage_Path + System.currentTimeMillis() + ".jpg");
                UploadTask uploadTask = storageReference2nd.putFile(uri);

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        // Hiding the progressDialog.
                        progressDialog.dismiss();
                        // Showing exception error message.
                        Constants.showSnackBar(Objects.requireNonNull(task.getException()).getMessage(), binding.edtName);

                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference2nd.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Picasso.get().load(new File(uri.getPath())).into(binding.imgUser);


                        userProfile.setProfile_pic(downloadUri.toString());
                        sharedObjectsAndAppController.setPreference(Constants.USER_INFO, new Gson().toJson(userProfile));
                        mDatabaseManager.updateUser(userProfile);
                    } else {
                        // Handle failures
                        // ...
                        // Hiding the progressDialog.
                        // Showing exception error message.
//                            Constants.showSnackBar(exception.getMessage(),edtName);
                    }
                });


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showImagePickerDialog() {
        final Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_image_picker);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.dimAmount = 0.8f;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final LinearLayout llGallery = dialog.findViewById(R.id.llGallery);
        final LinearLayout llCamera = dialog.findViewById(R.id.llCamera);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/Download/", Constants.IMAGE_DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
            Timber.tag("Dir").e("not exist");
        } else {
            Timber.tag("Dir").e("exist");
        }

        llGallery.setOnClickListener(view -> {
            dialog.dismiss();
            galleryIntent();
        });

        llCamera.setOnClickListener(view -> {
            dialog.dismiss();
            takePicture();
        });

        imgClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_FILE_GALLERY);
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/Download/", Constants.IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
            Timber.tag("Dir").e("not exist");
        } else {
            Timber.tag("Dir").e("exist");
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }


    @Override
    public void onSuccess() {
        Constants.showSnackBar("Profile updated successfully", binding.edtName);
    }

    @Override
    public void onFail() {
        Constants.showSnackBar("Couldn't updated profile", binding.edtName);
    }


}
