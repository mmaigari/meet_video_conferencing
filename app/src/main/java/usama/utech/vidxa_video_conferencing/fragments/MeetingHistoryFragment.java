package usama.utech.vidxa_video_conferencing.fragments;


import static android.content.Context.CLIPBOARD_SERVICE;

import android.Manifest;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.activities.MeetingActivity;
import usama.utech.vidxa_video_conferencing.adapters.MeetingHistoryAdapter;
import usama.utech.vidxa_video_conferencing.databinding.FragmentMeetingHistoryBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.MeetingHistory;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.DividerItemDecoration;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class MeetingHistoryFragment extends Fragment implements DatabaseManager.OnDatabaseDataChanged {

    private static final int PERMISSION_REQUEST_CODE = 10001;
    private static final int SETTINGS_REQUEST_CODE = 10002;

    DatabaseManager databaseManager;
    MeetingHistoryAdapter meetingHistoryAdapter;
    SharedObjectsAndAppController sharedObjectsAndAppController;
    String[] appPermissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private ArrayList<MeetingHistory> arrMeetingHistory = new ArrayList<>();
    private FragmentMeetingHistoryBinding binding;

    public MeetingHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMeetingHistoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        sharedObjectsAndAppController = new SharedObjectsAndAppController(requireActivity());
        databaseManager = new DatabaseManager(requireActivity());
        databaseManager.setDatabaseManagerListener(this);

        getData();

        if (!checkAppPermissions(appPermissions)) {
            requestAppPermissions(appPermissions);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void getData() {
        if (sharedObjectsAndAppController.getUserInfo() != null) {
            databaseManager.getMeetingHistoryByUser(sharedObjectsAndAppController.getUserInfo().getId());
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setMeetingHistoryAdapter() {
        if (arrMeetingHistory.size() > 0) {

            meetingHistoryAdapter = new MeetingHistoryAdapter(arrMeetingHistory, requireActivity());
            binding.rvHistory.setAdapter(meetingHistoryAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            binding.rvHistory.setLayoutManager(linearLayoutManager);

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


    @Override
    public void onDataChanged(String url, DataSnapshot dataSnapshot) {
        if (url.equalsIgnoreCase(Constants.Table.MEETING_HISTORY)) {
            if (MeetingHistoryFragment.this.isVisible()) {
                arrMeetingHistory = new ArrayList<>();
                arrMeetingHistory.addAll(databaseManager.getUserMeetingHistory());
                setMeetingHistoryAdapter();
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError error) {
        if (MeetingHistoryFragment.this.isVisible()) {
            arrMeetingHistory = new ArrayList<>();
            setMeetingHistoryAdapter();
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
                    } else {
                        //permission is denied and never asked is checked
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

}
