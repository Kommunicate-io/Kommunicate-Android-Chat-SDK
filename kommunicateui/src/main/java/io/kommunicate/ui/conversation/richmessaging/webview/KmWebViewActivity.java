package io.kommunicate.ui.conversation.richmessaging.webview;

import static io.kommunicate.ui.utils.SentryUtils.configureSentryWithKommunicateUI;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.ui.utils.InsetHelper;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.kommunicate.utils.KmConstants;
import io.kommunicate.utils.KmUtils;

public class KmWebViewActivity extends AppCompatActivity {

    WebView webView;
    Toolbar toolbar;
    private Map<String, String> txnData;
    private boolean isPaymentRequest = false;
    CustomizationSettings customizationSettings;
    private ProgressBar loadingProgressBar;
    private static final String JS_INTERFACE_NAME = "AlWebViewScreen";
    public static final String SURL = "surl";
    public static final String FURL = "furl";
    public static final String DEFAULT_REQUEST_TYPE = "application/x-www-form-urlencoded";
    public static final String REQUEST_TYPE_JSON = "json";
    public static final String Al_WEB_VIEW_BUNDLE = "alWebViewBundle";
    private static final String BODY_ONLOAD = "<body onload='form1.submit()'>";
    private static final String HTML_HEAD_HEAD = "<html><head></head>";
    private static final String FORMID_ACTION = "<form id='form1' action='%s' method='%s'>";
    private static final String INPUT_NAME_HIDDEN = "<input name='%s' type='hidden' value='%s' />";
    private static final String FORM_BODY_HTML = "</form></body></html>";
    private static final String text_html = "text/html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.km_activity_payment);

        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
        } else {
            customizationSettings = new CustomizationSettings();
        }
        configureSentryWithKommunicateUI(this, customizationSettings.toString());

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        KmThemeHelper themeHelper = KmThemeHelper.getInstance(this, customizationSettings);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(themeHelper.getPrimaryColor()));
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        toolbar.setBackgroundColor(themeHelper.getToolbarColor());

        KmUtils.setStatusBarColor(this, themeHelper.getStatusBarColor());

        webView = findViewById(R.id.paymentWebView);
        loadingProgressBar = findViewById(R.id.loadingProgress);

        if (getIntent() != null) {
            Bundle alWebViewBundle = getIntent().getBundleExtra(Al_WEB_VIEW_BUNDLE);

            if (alWebViewBundle != null) {
                boolean isLinkType = alWebViewBundle.getBoolean(KmRichMessage.WEB_LINK, false);

                txnData = new HashMap<>();
                setWebViewClient();

                String helpCenterUrl = alWebViewBundle.getString(KmConstants.KM_HELPCENTER_URL);

                if (!TextUtils.isEmpty(helpCenterUrl)) {
                    loadUrl(helpCenterUrl);
                    webView.getSettings().setBuiltInZoomControls(false);
                    webView.getSettings().setDisplayZoomControls(false);
                } else if (isLinkType) {
                    String linkUrl = alWebViewBundle.getString(KmRichMessage.LINK_URL);
                    if (linkUrl != null && !TextUtils.isEmpty(linkUrl)) {
                        loadUrl(linkUrl.startsWith("http") ? linkUrl : "http://" + linkUrl);
                    }
                } else {
                    String formDataJson = alWebViewBundle.getString(KmRichMessage.KM_FORM_DATA);
                    String baseUrl = alWebViewBundle.getString(KmRichMessage.KM_FORM_ACTION);

                    if (formDataJson != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(formDataJson);

                            Iterator<String> iter = jsonObject.keys();

                            while (iter.hasNext()) {
                                String key = iter.next();
                                if (jsonObject.getString(key) != null) {
                                    txnData.put(key, jsonObject.getString(key));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        isPaymentRequest = true;
                        webViewClientPost(webView, baseUrl, txnData.entrySet());
                    }
                }
            }
        }
        setupDownloadListener(webView);
        setupInsets();
    }

    private void setupDownloadListener(WebView webView) {
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);

                request.setDescription("Downloading file...");
                request.setTitle(filename);

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) {
                    request.addRequestHeader("cookie", cookies);
                }
                request.addRequestHeader("User-Agent", userAgent);

                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);

                Toast.makeText(getApplicationContext(), "Downloading file: " + filename, Toast.LENGTH_LONG).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupInsets() {
        InsetHelper.configureSystemInsets(
                toolbar,
                -1,
                0,
                true
        );
    }

    public void webViewClientPost(WebView webView, String url,
                                  Collection<Map.Entry<String, String>> postData) {
        StringBuilder sb = new StringBuilder();

        sb.append(HTML_HEAD_HEAD);
        sb.append(BODY_ONLOAD);
        sb.append(String.format(FORMID_ACTION));

        for (Map.Entry<String, String> item : postData) {
            sb.append(String.format(INPUT_NAME_HIDDEN, item.getKey(), item.getValue()));
        }
        sb.append(FORM_BODY_HTML);

        webView.loadData(sb.toString(), text_html, "utf-8");
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(KmWebViewActivity.this);

            alertDialog.setTitle(getString(R.string.warning));
            alertDialog.setMessage(getString(isPaymentRequest ? R.string.cancel_transaction : R.string.go_back));

            alertDialog.setPositiveButton(getString(R.string.yes_alert), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.setNegativeButton(getString(R.string.no_alert), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    public void loadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            webView.getSettings().setJavaScriptEnabled(customizationSettings.isJavaScriptEnabled());
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (loadingProgressBar != null) {
                        loadingProgressBar.setVisibility(View.VISIBLE);
                    }
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, final String url) {
                    webView.setVisibility(View.VISIBLE);
                    if (loadingProgressBar != null) {
                        loadingProgressBar.setVisibility(View.GONE);
                    }
                }
            });
            webView.loadUrl(url);
        }
    }

    public void setWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webView.setVisibility(View.VISIBLE);
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                if (!txnData.isEmpty() && txnData.containsKey(SURL) && url.equals(txnData.get(FURL))) {
                    finish();
                } else if (!txnData.isEmpty() && txnData.containsKey(FURL) && url.equals(txnData.get(FURL))) {
                    finish();
                }
                super.onPageFinished(view, url);
            }
        });

        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setDomStorageEnabled(true);
        webView.clearHistory();
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(customizationSettings.isJavaScriptEnabled());
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.addJavascriptInterface(new KmWebViewJsInterface(KmWebViewActivity.this), JS_INTERFACE_NAME);
    }
}
