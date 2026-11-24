package io.kommunicate.ui.conversation.home;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.kommunicate.Kommunicate;
import io.kommunicate.devkit.broadcast.EventManager;
import io.kommunicate.services.KmClientService;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;

/**
 * FAQ tab of the home screen. Shows the help center inside the tab so the bottom navigation stays
 * available; links that leave the help center open in a custom tab.
 */
public class KmHomeFaqFragment extends KmHomeBaseFragment {

    private WebView webView;
    private ProgressBar progressBar;
    private String helpCenterHost;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_fragment_home_faq, container, false);
        webView = view.findViewById(R.id.kmHomeFaqWebView);
        progressBar = view.findViewById(R.id.kmHomeFaqProgress);

        CustomizationSettings settings = host != null ? host.getHomeCustomizationSettings() : null;
        boolean hideChat = settings == null || settings.isHideChatInHelpcenter();
        String helpCenterUrl = new KmClientService(requireContext(), Kommunicate.getFaqPageName()).getHelpCenterUrl(hideChat);
        helpCenterHost = Uri.parse(helpCenterUrl).getHost();

        webView.getSettings().setJavaScriptEnabled(settings == null || settings.isJavaScriptEnabled());
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(view, request.getUrl());
            }

            // The request overload above is only called from API 24.
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, Uri.parse(url));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        // A state that could not be restored leaves the web view empty, so load the page again.
        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            EventManager.getInstance().sendOnFaqClick(helpCenterUrl);
            webView.loadUrl(helpCenterUrl);
        }
        return view;
    }

    /**
     * @return true if the url left the help center and was handed to {@link KmHomeLinks}.
     */
    private boolean handleUrl(WebView view, Uri url) {
        if (helpCenterHost != null && url.getHost() != null && !helpCenterHost.equals(url.getHost())) {
            KmHomeLinks.open(view.getContext(), url);
            return true;
        }
        progressBar.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroyView();
    }

    /**
     * @return true if the back press was used to navigate back inside the help center.
     */
    public boolean handleBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }
}
