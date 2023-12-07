package usama.utech.vidxa_video_conferencing.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.onesignal.OneSignal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.models.UserProfile;

public class SharedObjectsAndAppController extends MultiDexApplication {

    public static Context context;
    public static int PRIVATE_MODE = 0;
    public static String PREF_NAME = "Vidxa";
    private static SharedObjectsAndAppController instance;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;

    public SharedObjectsAndAppController() {
    }

    public SharedObjectsAndAppController(Context context) {
        SharedObjectsAndAppController.context = context;
        initializeStetho();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        sharedPreference = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreference.edit();

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/montserrat_regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    public static SharedObjectsAndAppController getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
        // or return instance.getApplicationContext();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static void hideKeyboard(View view, Context c) {
        InputMethodManager inputMethodManager = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + c;
    }

    public static String getVersion(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static String getDeviceVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getTodaysDate(String dateFormat) {
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        return df.format(c.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static String convertDateFormat(String dateString, String originalDateFormat, String outputDateFormat) {
        String finalDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(originalDateFormat);
        try {
            Date date = simpleDateFormat.parse(dateString);
            simpleDateFormat = new SimpleDateFormat(outputDateFormat);
            finalDate = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    @Override
    public void onCreate() {
        instance = new SharedObjectsAndAppController(this);
        super.onCreate();
        context = getApplicationContext();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.onsignalappid));

        LocaleHelper.setLocale(getApplicationContext(), Constants.getLanguageCode(context));

    }

    public UserProfile getUserInfo() {
        return new Gson().fromJson(getPreference(Constants.USER_INFO), UserProfile.class);
    }

    public void initializeStetho() {
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(context);
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context));
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(context));
        Stetho.Initializer initializer = initializerBuilder.build();
        Stetho.initialize(initializer);
    }

    public void setPreference(String key, String value) {
        editor = sharedPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getPreference(String key) {
        try {
            return sharedPreference.getString(key, "");
        } catch (Exception exception) {
            return "";
        }
    }

    public void removeSinglePreference(String pref) {
        if (sharedPreference.contains(pref)) {
            editor = sharedPreference.edit();
            editor.remove(pref);
            editor.commit();
        }
    }

    public void clear() {
        editor = sharedPreference.edit();
        editor.clear();
        editor.commit();
    }



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

}
