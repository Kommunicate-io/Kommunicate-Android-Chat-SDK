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
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALRichMessageModel;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    WebView webView;
    Toolbar toolbar;
    private static boolean isProdEnv = false;
    private String basePayUrl = "https://secure.payu.in/_payment";
    private String saltKey = "8KddPRlg";
    private Map<String, String> txnData;
    private ALRichMessageModel.AlFormDataModel formData;
    private String hashKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Payment");

        webView = findViewById(R.id.paymentWebView);

        if (!isProdEnv) {
            basePayUrl = "https://test.payu.in/_payment";
            saltKey = "8KddPRlg";
        }

        setWebViewClient();

        String formDataJson = getIntent().getStringExtra("formData");
        formData = (ALRichMessageModel.AlFormDataModel) GsonUtils.getObjectFromJson(formDataJson, ALRichMessageModel.AlFormDataModel.class);

        hashKey = calculateHash("SHA-512", formData.getKey() + "|" +
                formData.getTxnid() + "|" +
                formData.getAmount() + "|" +
                formData.getProductinfo() + "|" +
                formData.getFirstname() + "|" +
                formData.getEmail() + "|||||||||||" +
                saltKey);

        Utils.printLog(this, "PayTest", "Generated Hash Key : " + hashKey);
        Utils.printLog(this, "PayTest", "Server Hash Key    : " + formData.getHASH());

        txnData = new HashMap<>();

        txnData.put("key", formData.getKey());
        txnData.put("txnid", formData.getTxnid());
        txnData.put("amount", formData.getAmount());
        txnData.put("productinfo", formData.getProductinfo());
        txnData.put("firstname", formData.getFirstname());
        txnData.put("email", formData.getEmail());
        txnData.put("phone", formData.getPhone());
        txnData.put("surl", formData.getSurl());
        txnData.put("furl", formData.getFurl());
        txnData.put("hash", hashKey);

        webViewClientPost(webView, basePayUrl, txnData.entrySet());
    }

    public String calculateHash(String type, String str) {
        byte[] hashSequence = str.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashSequence);
            byte messageDigest[] = algorithm.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1)
                    hexString.append("0");
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException NSAE) {
        }
        return hexString.toString();
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
                if (url.equals(formData.getSurl())) {
                    //webView.loadUrl(url);
                    //webViewClientPost(webView, url, null);
                    //Utils.printLog(PaymentActivity.this, "PayTest", "Success response url : " + url);
                    finish();
                } else if (url.equals(formData.getSurl())) {
                    finish();
                    //webView.loadUrl(url);
                    //webViewClientPost(webView, url, null);
                    //Utils.printLog(PaymentActivity.this, "PayTest", "Failure response url : " + url);
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
