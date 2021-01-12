package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.webview;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

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
    AlCustomizationSettings alCustomizationSettings;
    private ProgressBar loadingProgressBar;
    private static final String JS_INTERFACE_NAME = "AlWebViewScreen";
    public static final String SURL = "surl";
    public static final String FURL = "furl";
    public static final String DEFAULT_REQUEST_TYPE = "application/x-www-form-urlencoded";
    public static final String REQUEST_TYPE_JSON = "json";
    public static final String Al_WEB_VIEW_BUNDLE = "alWebViewBundle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.km_activity_payment);

        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        KmUtils.setStatusBarColor(this, KmThemeHelper.getInstance(this, alCustomizationSettings).getStatusBarColor());

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
    }

    public void webViewClientPost(WebView webView, String url,
                                  Collection<Map.Entry<String, String>> postData) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head></head>");
        sb.append("<body onload='form1.submit()'>");
        sb.append(String.format("<form id='form1' action='%s' method='%s'>", url, "post"));

        for (Map.Entry<String, String> item : postData) {
            sb.append(String.format("<input name='%s' type='hidden' value='%s' />", item.getKey(), item.getValue()));
        }
        sb.append("</form></body></html>");

        webView.loadData(sb.toString(), "text/html", "utf-8");
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
            webView.getSettings().setJavaScriptEnabled(true);
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
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.addJavascriptInterface(new KmWebViewJsInterface(KmWebViewActivity.this), JS_INTERFACE_NAME);
    }
}
