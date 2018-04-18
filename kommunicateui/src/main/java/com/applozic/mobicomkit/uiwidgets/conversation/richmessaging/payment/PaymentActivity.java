package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.payment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.applozic.mobicomkit.uiwidgets.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    WebView webView;
    Toolbar toolbar;
    private Map<String, String> txnData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        webView = findViewById(R.id.paymentWebView);

        String formDataJson = getIntent().getStringExtra("formData");
        String baseUrl = getIntent().getStringExtra("formAction");

        if (formDataJson != null) {
            setWebViewClient();

            txnData = new HashMap<>();

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
            webViewClientPost(webView, baseUrl, txnData.entrySet());
        } else {
            webView.loadUrl(baseUrl);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PaymentActivity.this);

        alertDialog.setTitle("Warning");
        alertDialog.setMessage("Do you want to cancel this transaction?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void setWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //finish();
                if (!txnData.isEmpty() && txnData.containsKey("surl") && url.equals(txnData.get("surl"))) {
                    finish();
                } else if (!txnData.isEmpty() && txnData.containsKey("furl") && url.equals(txnData.get("furl"))) {
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
        webView.addJavascriptInterface(new PaymentJsInterface(PaymentActivity.this), "PaymentScreen");
    }
}
