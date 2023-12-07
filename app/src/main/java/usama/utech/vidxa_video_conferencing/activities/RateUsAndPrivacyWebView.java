package usama.utech.vidxa_video_conferencing.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import usama.utech.vidxa_video_conferencing.databinding.ActivityRateUsWebViewBinding;
import usama.utech.vidxa_video_conferencing.utils.LocaleHelper;

public class RateUsAndPrivacyWebView extends AppCompatActivity {

    private ActivityRateUsWebViewBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityRateUsWebViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent i = getIntent();
        String url = i.getStringExtra("link");


        WebSettings webSettings = binding.webviewItem.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        // webView.setWebViewClient(new WebViewClient());
        binding.webviewItem.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && binding.pB1.getVisibility() == ProgressBar.GONE) {
                    binding.pB1.setVisibility(ProgressBar.VISIBLE);

                }

                binding.pB1.setProgress(progress);
                if (progress == 100) {
                    binding.pB1.setVisibility(ProgressBar.GONE);

                }
            }
        });

        binding.webviewItem.loadUrl(url);

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleHelper.onAttach(newBase);

        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(RateUsAndPrivacyWebView.this, MainActivity.class));
        finish();
        super.onBackPressed();

    }
}