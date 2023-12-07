package usama.utech.vidxa_video_conferencing.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import it.sephiroth.android.library.numberpicker.NumberPicker;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.databinding.ActivityScheduleMeetingBinding;
import usama.utech.vidxa_video_conferencing.firebase_database_manager.DatabaseManager;
import usama.utech.vidxa_video_conferencing.models.Schedule;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class MeetingSchedulerActivity extends AppCompatActivity {


    SharedObjectsAndAppController sharedObjectsAndAppController;

    DatabaseManager mDatabaseManager;
    Schedule scheduleBean;
    String meetingCode = "";
    private DatabaseReference databaseReferenceSchedule;
    private Calendar calendar;
    private ProgressDialog progressDialog;
    private ActivityScheduleMeetingBinding binding;
    private Calendar selectedDate;
    private int SelectedDay, Selectedmonth, Selectedyear;

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
        binding = ActivityScheduleMeetingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            binding.toolbar.setNavigationOnClickListener(view1 -> onBackPressed());
        }

        sharedObjectsAndAppController = new SharedObjectsAndAppController(MeetingSchedulerActivity.this);
        calendar = Calendar.getInstance();

        mDatabaseManager = new DatabaseManager(MeetingSchedulerActivity.this);
        databaseReferenceSchedule = FirebaseDatabase.getInstance().getReference(Constants.Table.SCHEDULE);

        mDatabaseManager.setOnScheduleListener(new DatabaseManager.OnScheduleListener() {
            @Override
            public void onAddSuccess() {
                Constants.showSnackBar(getString(R.string.scheduleadded), binding.imgBack);
                showSuccessDialog();
                dismissProgressDialog();
            }

            @Override
            public void onUpdateSuccess() {
                Constants.showSnackBar(getString(R.string.scheduleupdated), binding.imgBack);
                dismissProgressDialog();
                showSuccessDialog();
            }

            @Override
            public void onDeleteSuccess() {
            }

            @Override
            public void onAddFail() {
                dismissProgressDialog();
                Constants.showSnackBar(getString(R.string.couldnotadd), binding.imgBack);
            }

            @Override
            public void onUpdateFail() {
                dismissProgressDialog();
                Constants.showSnackBar(getString(R.string.couldnotupdate), binding.imgBack);
            }

            @Override
            public void onDeleteFail() {
            }
        });
        setEdtListeners();
        initBtnclicklistners();

        if (getIntent().hasExtra(Constants.INTENT_BEAN)) {
            scheduleBean = (Schedule) getIntent().getSerializableExtra(Constants.INTENT_BEAN);
            setData();
        } else {
            meetingCode = Constants.getMeetingCode(MeetingSchedulerActivity.this);
        }

        binding.numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //disable input via keyboard
    }


    private void setData() {

        meetingCode = scheduleBean.getMeeetingId();

        binding.edtTitle.setText(scheduleBean.getTitle());
        binding.edtDate.setText(scheduleBean.getDate());

        binding.numberPicker.setProgress(Integer.parseInt(scheduleBean.getDuration()));

        String startTime = SharedObjectsAndAppController.convertDateFormat(scheduleBean.getStartTime(), Constants.DateFormats.TIME_FORMAT_24
                , Constants.DateFormats.TIME_FORMAT_12);

        binding.edtStartTime.setText(startTime);
    }

    @SuppressLint("SetTextI18n")
    public void showSuccessDialog() {
        final Dialog dialogDate = new Dialog(MeetingSchedulerActivity.this);
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDate.setContentView(R.layout.dialog_meeting_info);
        dialogDate.setCancelable(true);

        Window window = dialogDate.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.dimAmount = 0.8f;
        window.setAttributes(wlp);
        dialogDate.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView txtName = dialogDate.findViewById(R.id.txtName);
        TextView txtDate = dialogDate.findViewById(R.id.txtDate);
        TextView txtTime = dialogDate.findViewById(R.id.txtTime);

        if (!TextUtils.isEmpty(scheduleBean.getTitle())) {
            txtName.setText(scheduleBean.getTitle());
        }

        if (!TextUtils.isEmpty(scheduleBean.getDate())) {
            txtDate.setText(scheduleBean.getDate());
        }

        if (!TextUtils.isEmpty(scheduleBean.getStartTime()) && !TextUtils.isEmpty(scheduleBean.getDuration())) {
            String startTime = SharedObjectsAndAppController.convertDateFormat(scheduleBean.getStartTime(), Constants.DateFormats.TIME_FORMAT_24
                    , Constants.DateFormats.TIME_FORMAT_12);

            txtTime.setText("Starts at " + startTime + " (" + scheduleBean.getDuration() + " Mins)");
        }

        Button btnCalendar = dialogDate.findViewById(R.id.btnCalendar);
        Button btnCancel = dialogDate.findViewById(R.id.btnCancel);

        btnCalendar.setOnClickListener(v -> {
            dialogDate.dismiss();
            addMeetingToCalendar();
        });

        btnCancel.setOnClickListener(view -> {
            dialogDate.dismiss();
            onBackPressed();
        });

        if (!dialogDate.isShowing()) {
            dialogDate.show();
        }
    }

    private void setEdtListeners() {
        binding.edtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.inputLayoutTitle.setErrorEnabled(false);
                binding.inputLayoutTitle.setError("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    private void initBtnclicklistners() {


        binding.imgBack.setOnClickListener(view -> onBackPressed());

        binding.txtSave.setOnClickListener(view -> {

            binding.txtSave.setEnabled(false);
            new Handler().postDelayed(() -> binding.txtSave.setEnabled(true), 1500);

            validateAndSaveMeeting();
        });

        binding.edtDate.setOnClickListener(view -> {
            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(MeetingSchedulerActivity.this,
                    (view1, year, monthOfYear, dayOfMonth) -> {
                        binding.edtDate.setText(SharedObjectsAndAppController.pad(dayOfMonth) + "-" + SharedObjectsAndAppController.pad((monthOfYear + 1)) + "-" + year);
                        selectedDate = Calendar.getInstance();

                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, monthOfYear);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SelectedDay = dayOfMonth;
                        Selectedmonth = (monthOfYear + 1);
                        Selectedyear = year;


                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.setCancelable(false);
            datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
            datePickerDialog.show();
        });
        binding.edtStartTime.setOnClickListener(view -> {

            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;

            mTimePicker = new TimePickerDialog(MeetingSchedulerActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                binding.edtStartTime.setText(SharedObjectsAndAppController.convertDateFormat(SharedObjectsAndAppController.pad(selectedHour) + ":" + SharedObjectsAndAppController.pad(selectedMinute),
                        Constants.DateFormats.TIME_FORMAT_24,
                        Constants.DateFormats.TIME_FORMAT_12));


                try {

                    selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedDate.set(Calendar.MINUTE, selectedMinute);

                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("hh:mm_yyyy.MM.dd");
                    Date date1 = new Date();
                    Date date2 = new Date(selectedDate.getTimeInMillis());

                    long diff = date2.getTime() - date1.getTime();

                } catch (Exception e) {

                    e.printStackTrace();
                }


            }, hour, minute, false);
            mTimePicker.show();
        });
        binding.edtEndTime.setOnClickListener(view -> {


            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;


            mTimePicker = new TimePickerDialog(MeetingSchedulerActivity.this, (timePicker, selectedHour, selectedMinute) -> binding.edtEndTime.setText(SharedObjectsAndAppController.convertDateFormat(SharedObjectsAndAppController.pad(selectedHour) + ":" + SharedObjectsAndAppController.pad(selectedMinute),
                    Constants.DateFormats.TIME_FORMAT_24,
                    Constants.DateFormats.TIME_FORMAT_12)), hour, minute, false);

            mTimePicker.show();
        });


    }


    private void validateAndSaveMeeting() {

        SharedObjectsAndAppController.hideKeyboard(binding.txtSave, MeetingSchedulerActivity.this);

        if (TextUtils.isEmpty(Objects.requireNonNull(binding.edtTitle.getText()).toString().trim())) {
            binding.inputLayoutTitle.setErrorEnabled(true);
            binding.inputLayoutTitle.setError(getString(R.string.err_title));
        } else if (TextUtils.isEmpty(Objects.requireNonNull(binding.edtDate.getText()).toString().trim())) {
            Constants.showSnackBar(getString(R.string.select_date), binding.edtDate);
        } else if (TextUtils.isEmpty(Objects.requireNonNull(binding.edtStartTime.getText()).toString().trim())) {
            Constants.showSnackBar(getString(R.string.select_start_time), binding.edtStartTime);
        } else if (binding.numberPicker.getProgress() == 0) {
            Constants.showSnackBar(getString(R.string.select_duration), binding.numberPicker);
        } else {

            showProgressDialog();

//            Schedule schedule = new Schedule();
            if (getIntent().hasExtra(Constants.INTENT_BEAN)) {
//                schedule = scheduleBean;
            } else {
                scheduleBean = new Schedule();
                scheduleBean.setUserId(sharedObjectsAndAppController.getUserInfo().getId());
                scheduleBean.setMeeetingId(meetingCode);
            }

            scheduleBean.setDuration(binding.numberPicker.getProgress() + "");

            scheduleBean.setTitle(binding.edtTitle.getText().toString().trim());
            scheduleBean.setDate(binding.edtDate.getText().toString().trim());
            scheduleBean.setStartTime(SharedObjectsAndAppController.convertDateFormat(binding.edtStartTime.getText().toString().trim(),
                    Constants.DateFormats.TIME_FORMAT_12, Constants.DateFormats.TIME_FORMAT_24));

            if (getIntent().hasExtra(Constants.INTENT_BEAN)) {

                int randonID = Constants.generateRandomInt(9999999);

                scheduleBean.setReminderIdandrequestcode(String.valueOf(randonID));

                mDatabaseManager.updateSchedule(scheduleBean);
                //TODO Fix Reminder
                // Constants.setReminderAlarm(MeetingSchedulerActivity.this, selectedDate.getTimeInMillis(),randonID);
                //  Log.d("alardhsgdj","alaram done is "+selectedDate.getTimeInMillis());
                //   Constants.setReminderTime(getApplicationContext(), String.valueOf(selectedDate.getTimeInMillis()),randonID);

            } else {

                int randonID = Constants.generateRandomInt(9999999);

                scheduleBean.setReminderIdandrequestcode(String.valueOf(randonID));


                mDatabaseManager.addSchedule(scheduleBean);

                // Log.d("alardhsgdj","alaram done is "+selectedDate.getTimeInMillis());

                // Constants.setReminderTime(getApplicationContext(), String.valueOf(selectedDate.getTimeInMillis()),randonID);

                // Constants.setReminderAlarm(MeetingSchedulerActivity.this, selectedDate.getTimeInMillis(),randonID);

            }
        }
    }

    private void addMeetingToCalendar() {

        String date = "", startTime = "";

        date = Objects.requireNonNull(binding.edtDate.getText()).toString().trim();
        String[] splitDate = date.split("-");

        startTime = SharedObjectsAndAppController.convertDateFormat(Objects.requireNonNull(binding.edtStartTime.getText()).toString().trim(),
                Constants.DateFormats.TIME_FORMAT_12, Constants.DateFormats.TIME_FORMAT_24);
        String[] splitStartTime = startTime.split(":");

        Calendar calStartTime = Calendar.getInstance();
        calStartTime.set(Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[1]) - 1, Integer.parseInt(splitDate[0]),
                Integer.parseInt(splitStartTime[0]), Integer.parseInt(splitStartTime[1]));

        //open intent
        Intent i = new Intent(Intent.ACTION_EDIT);
        i.setType("vnd.android.cursor.item/event");
        i.putExtra("beginTime", calStartTime.getTimeInMillis());
        calStartTime.add(Calendar.MINUTE, binding.numberPicker.getProgress());

        i.putExtra("endTime", calStartTime.getTimeInMillis());
        i.putExtra("title", getResources().getString(R.string.app_name) + "-" + Objects.requireNonNull(binding.edtTitle.getText()).toString().trim());
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(MeetingSchedulerActivity.this);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (!MeetingSchedulerActivity.this.isFinishing()) {
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
