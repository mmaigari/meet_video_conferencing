package usama.utech.vidxa_video_conferencing.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.florent37.shapeofview.shapes.DiagonalView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.activities.MainActivity;
import usama.utech.vidxa_video_conferencing.activities.MeetingActivity;
import usama.utech.vidxa_video_conferencing.adapters.MeetingHistoryAdapter;
import usama.utech.vidxa_video_conferencing.databinding.FragmentHomeBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.MeetingHistory;
import usama.utech.vidxa_video_conferencing.models.UserProfile;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.DividerItemDecoration;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class HomeFragment extends Fragment implements DatabaseManager.OnDatabaseDataChanged {

    private static final int PERMISSION_REQUEST_CODE = 10001;
    private static final int SETTINGS_REQUEST_CODE = 10002;

    UserProfile userProfile;
    MeetingHistoryAdapter meetingHistoryAdapter;
    DatabaseManager databaseManager;

    String[] appPermissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    InterstitialAd mInterstitialAd;
    boolean isMeetingExist = false;
    TextInputLayout inputLayoutCode, inputLayoutName;
    TextInputEditText edtCode, edtName;
    private SharedObjectsAndAppController sharedObjectsAndAppController;
    private ArrayList<MeetingHistory> arrMeetingHistory = new ArrayList<>();
    private DatabaseReference databaseReferenceMeetingHistory;
    private AdView adView;
    private FragmentHomeBinding binding;
    private String imgUrl = "";

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedObjectsAndAppController = new SharedObjectsAndAppController(requireActivity());
        databaseManager = new DatabaseManager(requireActivity());
        databaseManager.setDatabaseManagerListener(this);
        setUserData();

        databaseReferenceMeetingHistory = FirebaseDatabase.getInstance().getReference(Constants.Table.MEETING_HISTORY);

        binding.rvHistory.setNestedScrollingEnabled(false);

        adView = view.findViewById(R.id.adView);


        bindAdvtView();

        if (!checkAppPermissions(appPermissions)) {
            requestAppPermissions(appPermissions);
        }

        try {
            String languagecode = Constants.getLanguageCode(requireActivity());
            if (languagecode.equals("ar")) {
                binding.DiagonalViewHome.setDiagonalDirection(DiagonalView.DIRECTION_RIGHT);
            } else {
                binding.DiagonalViewHome.setDiagonalDirection(DiagonalView.DIRECTION_LEFT);

            }
        } catch (Exception e) {
            System.out.println("erroris = " + e.getMessage());
        }


        if (!Constants.MEETING_ID.equals("")) {

            startActivity(new Intent(requireActivity(), MeetingActivity.class));

        }


        binding.llJoin.setOnClickListener(view14 -> {
            if (checkAppPermissions(appPermissions)) {
                showMeetingCodeDialog();
            } else {
                requestAppPermissions(appPermissions);
            }
        });
        binding.llNewMeeting.setOnClickListener(view1 -> {
            showAdmobAds();
            Constants.MEETING_ID = Constants.getMeetingCode(requireActivity());

            if (checkAppPermissions(appPermissions)) {
                showMeetingShareDialog();
            } else {
                requestAppPermissions(appPermissions);
            }
        });
        binding.llProfile.setOnClickListener(view12 -> {

            ((MainActivity) requireActivity()).loadFragment(new ProfileFragment());
            ((MainActivity) requireActivity()).binding.floatingTopBarNavigation.setItemActiveIndex(3);


        });

        binding.imgUser.setOnClickListener(view13 -> {
            if (!TextUtils.isEmpty(imgUrl)) {
                showImage(imgUrl);
            }
        });


        return view;
    }


    private void showAdmobAds() {

        if (mInterstitialAd != null) {
            mInterstitialAd.show(requireActivity());
        }

    }

    private void loadAdmobAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(requireActivity(), getResources().getString(R.string.Vidxa_interstitial), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });

    }


    public void showImage(String imageUri) {
        Dialog builder = new Dialog(requireActivity());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(dialogInterface -> {
            //nothing;
        });

        ImageView imageView = new ImageView(requireActivity());
        Picasso.get().load(imageUri).placeholder(R.drawable.user_avatar).into(imageView);

        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }


    private void bindAdvtView() {

        if (SharedObjectsAndAppController.isNetworkConnected(requireActivity())) {
            final AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice("23F1C653C3AF44D748738885C1F91FDA")
                    .build();

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdClosed() {
//                Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
                }

                public void onAdFailedToLoad(int errorCode) {
                    adView.setVisibility(View.GONE);
//                Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
                }

                public void onAdLeftApplication() {
//                Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
            });
            adView.loadAd(adRequest);
            adView.setVisibility(View.VISIBLE);

            loadAdmobAds();


        } else {
            adView.setVisibility(View.GONE);
        }
    }

    private boolean checkMeetingExists(final String meeting_id) {
        isMeetingExist = false;
        Query query = databaseReferenceMeetingHistory.orderByChild("meeting_id").equalTo(meeting_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

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
            public void onCancelled(@NotNull DatabaseError databaseError) {
                isMeetingExist = false;
            }
        });
        return isMeetingExist;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setUserData() {
        userProfile = sharedObjectsAndAppController.getUserInfo();

        if (userProfile != null) {

            imgUrl = userProfile.getProfile_pic();


            if (!TextUtils.isEmpty(userProfile.getProfile_pic())) {
                Picasso.get().load(userProfile.getProfile_pic())
                        .error(R.drawable.user_avatar).into(binding.imgUser);
            } else {
                binding.imgUser.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.user_avatar));
            }

            if (!TextUtils.isEmpty(userProfile.getName())) {
                binding.txtUserName.setText(String.format("%s%s", getString(R.string.Hi_txt), userProfile.getName()));
            } else {
                binding.txtUserName.setText(String.format("%s", getString(R.string.Hi_txt)));
            }
            databaseManager.getMeetingHistoryByUser(sharedObjectsAndAppController.getUserInfo().getId());
        } else {
            binding.txtUserName.setText(String.format("%s", getString(R.string.Hi_txt)));
            binding.imgUser.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.user_avatar));
        }
    }

    @Override
    public void onDataChanged(String url, DataSnapshot dataSnapshot) {
        if (url.equalsIgnoreCase(Constants.Table.MEETING_HISTORY)) {
            if (HomeFragment.this.isVisible()) {
                arrMeetingHistory = new ArrayList<>();
                if (databaseManager.getUserMeetingHistory().size() > 0) {
                    for (int i = 0; i < databaseManager.getUserMeetingHistory().size(); i++) {
                        MeetingHistory bean = databaseManager.getUserMeetingHistory().get(i);
                        if (!TextUtils.isEmpty(bean.getStartTime())) {
                            String date = SharedObjectsAndAppController.convertDateFormat(bean.getStartTime()
                                    , Constants.DateFormats.DATETIME_FORMAT_24, Constants.DateFormats.DATE_FORMAT_DD_MMM_YYYY);
                            if (date.equalsIgnoreCase(SharedObjectsAndAppController.getTodaysDate(Constants.DateFormats.DATE_FORMAT_DD_MMM_YYYY))) {
                                arrMeetingHistory.add(bean);
                            }
                        }
                    }
                }
                setMeetingHistoryAdapter();
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError error) {
        arrMeetingHistory = new ArrayList<>();
        setMeetingHistoryAdapter();
    }

    private void setMeetingHistoryAdapter() {
        if (arrMeetingHistory.size() > 0) {

            Collections.sort(arrMeetingHistory, (arg0, arg1) -> {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(Constants.DateFormats.DATETIME_FORMAT_24);
                int compareResult = 0;
                try {
                    Date arg0Date = format.parse(arg0.getStartTime());
                    Date arg1Date = format.parse(arg1.getStartTime());
                    compareResult = Objects.requireNonNull(arg1Date).compareTo(arg0Date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return compareResult;
            });

            meetingHistoryAdapter = new MeetingHistoryAdapter(arrMeetingHistory, requireActivity());
            binding.rvHistory.setAdapter(meetingHistoryAdapter);
            binding.rvHistory.addItemDecoration(new DividerItemDecoration(requireActivity()));

            meetingHistoryAdapter.setOnItemClickListener(new MeetingHistoryAdapter.OnItemClickListener() {
                @Override
                public void onItemClickListener(int position, MeetingHistory bean) {
                }

                @Override
                public void onDeleteClickListener(int position, MeetingHistory bean) {
                    databaseManager.deleteMeetingHistory(bean);
                }

                @Override
                public void onJoinClickListener(int position, MeetingHistory bean) {
                    Constants.MEETING_ID = bean.getMeeting_id();

                    showMeetingShareDialogForRejoin();


                }
            });

            binding.rvHistory.setVisibility(View.VISIBLE);
            binding.llError.setVisibility(View.GONE);
        } else {
            binding.rvHistory.setVisibility(View.GONE);
            binding.llError.setVisibility(View.VISIBLE);
        }
    }


    public void showMeetingShareDialog() {
        final Dialog dialogDate = new Dialog(requireActivity());
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDate.setContentView(R.layout.dialog_meeting_share);
        dialogDate.setCancelable(false);

        Window window = dialogDate.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.dimAmount = 0.8f;
        window.setAttributes(wlp);
        dialogDate.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView txtMeetingURL = dialogDate.findViewById(R.id.txtMeetingURL);
        ImageView imgCopy = dialogDate.findViewById(R.id.imgCopy);
        String androidmeetingcode = Constants.MEETING_ID;
        txtMeetingURL.setText(androidmeetingcode);

        TextView txtMeetingcodeurl = dialogDate.findViewById(R.id.txtMeetingcodeUrl);
        ImageView imgCopymeetingcodeurl = dialogDate.findViewById(R.id.imgCopycodeUrl);

        String webmeetingcodeurl = Constants.JITSI_SERVER_URL_WITH_SLASH + "meeting?code=" + Constants.MEETING_ID;
        txtMeetingcodeurl.setText(String.format("%s", webmeetingcodeurl));

        imgCopymeetingcodeurl.setOnClickListener(view -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingcodeurl.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });

        txtMeetingcodeurl.setOnClickListener(view -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingcodeurl.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });


        imgCopy.setOnClickListener(v -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingURL.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });

        txtMeetingURL.setOnClickListener(v -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingURL.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });

        ImageView imgClose = dialogDate.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(view -> dialogDate.dismiss());


        Button btnInviteEmail = dialogDate.findViewById(R.id.btnInviteEmail);
        btnInviteEmail.setOnClickListener(view -> {

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"enter recepant email"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Vidxa Meet Join");
            i.putExtra(Intent.EXTRA_TEXT, "Click on the below link or copy the meeting code and conmnect to the meeting.\n " + androidmeetingcode + "\nOr connect Through web via the link below\n" + webmeetingcodeurl);
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(requireActivity(), getString(R.string.noemailclient), Toast.LENGTH_SHORT).show();
            }

        });


        Button btnContinue = dialogDate.findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            dialogDate.dismiss();
            startActivity(new Intent(requireActivity(), MeetingActivity.class));
        });

        if (!dialogDate.isShowing()) {
            dialogDate.show();
        }
    }

    public void showMeetingShareDialogForRejoin() {
        final Dialog dialogDate = new Dialog(requireActivity());
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDate.setContentView(R.layout.dialog_meeting_share);
        dialogDate.setCancelable(true);

        Window window = dialogDate.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.dimAmount = 0.8f;
        window.setAttributes(wlp);
        dialogDate.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView txtMeetingURL = dialogDate.findViewById(R.id.txtMeetingURL);
        ImageView imgCopy = dialogDate.findViewById(R.id.imgCopy);
        txtMeetingURL.setText(Constants.MEETING_ID);

        TextView txtMeetingcodeurl = dialogDate.findViewById(R.id.txtMeetingcodeUrl);
        ImageView imgCopymeetingcodeurl = dialogDate.findViewById(R.id.imgCopycodeUrl);
        txtMeetingcodeurl.setText(String.format("%smeeting?code=%s", Constants.JITSI_SERVER_URL_WITH_SLASH, Constants.MEETING_ID));

        imgCopymeetingcodeurl.setOnClickListener(view -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingcodeurl.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });

        txtMeetingcodeurl.setOnClickListener(view -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingcodeurl.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });


        imgCopy.setOnClickListener(v -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingURL.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });

        txtMeetingURL.setOnClickListener(v -> {
            ClipboardManager myClipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData myClip;
            myClip = ClipData.newPlainText("text", txtMeetingURL.getText().toString());
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(requireActivity(), getString(R.string.meetinglinkcopied), Toast.LENGTH_SHORT).show();
        });

        Button btnContinue = dialogDate.findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            dialogDate.dismiss();
            if (checkAppPermissions(appPermissions)) {
                startActivity(new Intent(requireActivity(), MeetingActivity.class));
            } else {
                requestAppPermissions(appPermissions);
            }
        });

        if (!dialogDate.isShowing()) {
            dialogDate.show();
        }
    }


    public void showMeetingCodeDialog() {
        final Dialog dialogDate = new Dialog(requireActivity());
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

        inputLayoutName.setEnabled(false);
        edtName.setEnabled(false);

        edtName.setText(sharedObjectsAndAppController.getUserInfo().getName());

        edtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutCode.setErrorEnabled(false);
                inputLayoutCode.setError("");
                if (charSequence.length() == (Constants.getMeetingCode(requireActivity()).length())) {
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
                Constants.showAlertDialog(getResources().getString(R.string.meeting_not_exist), requireActivity());
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

            startActivity(new Intent(requireActivity(), MeetingActivity.class));
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
            if (ContextCompat.checkSelfPermission(requireActivity(), perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        //Ask for non granted permissions
        return listPermissionsNeeded.isEmpty();
        // App has all permissions
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
                //invoke ur method
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
        if (requestCode == SETTINGS_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                if (checkAppPermissions(appPermissions)) {

                } else {
                    requestAppPermissions(appPermissions);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
