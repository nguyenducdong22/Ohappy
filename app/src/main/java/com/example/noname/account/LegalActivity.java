package com.example.noname.account;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.widget.Toolbar;
import com.example.noname.R;

public class LegalActivity extends BaseActivity {

    private Toolbar toolbar;
    private WebView webView;
    private ProgressBar progressBar;

    // THAY THẾ BẰNG URL TRANG ĐIỀU KHOẢN CỦA BẠN
    private static final String LEGAL_URL = "https://docs.google.com/forms/d/e/1FAIpQLSdXZ7m_8j6dACy6EPeyycmqYX56LK4_PrxET7mazI0joJJvEg/viewform?usp=header"; // <<<<---- URL MẪU

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        initializeViews();
        setupToolbar();
        setupWebView();

        webView.loadUrl(LEGAL_URL);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_legal);
        webView = findViewById(R.id.webview_legal);
        progressBar = findViewById(R.id.progressBar_legal);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true); // Cho phép JavaScript (tùy chọn)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE); // Hiển thị thanh tải
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE); // Ẩn thanh tải khi đã xong
            }
        });
    }

    // Cho phép nhấn nút Back của điện thoại để quay lại trang trước trong WebView
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}