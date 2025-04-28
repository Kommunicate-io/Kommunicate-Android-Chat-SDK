package io.kommunicate.ui.conversation.richmessaging.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.conversation.richmessaging.models.KmRichMessageModel;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.commons.json.GsonUtils;


import java.util.Arrays;
import java.util.List;

public class KmVideoRMAdapter extends KmRichMessageAdapter {


    private List<KmRichMessageModel.KmPayloadModel> payloadList;
    double currentPos, totalDuration;
    private CustomizationSettings customizationSettings;
    private static final String PATH_ONE = "<iframe width=\"100%\" height=\"100%\" src=\"";
    private static final String PATH_TWO = "\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";
    private static final String text_html = "text/html";
    private static final String TIME_WITH_HR = "%02d:%02d:%02d";
    private static final String TIME_WITHOUT_HR = "%02d:%02d";


    KmVideoRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper, boolean isMessageProcessed, CustomizationSettings customizationSettings) {
        super(context, model, listener, message, themeHelper);
        this.customizationSettings = customizationSettings;
        if (model.getPayload() != null) {
            this.payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                    GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.km_rich_message_video_item, viewGroup, false);
        return new RichMessageVideoHolder(itemView);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        bindItems(viewHolder, i);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    void bindItems(RecyclerView.ViewHolder viewHolder, int position) {
        super.bindItems(viewHolder, position);
        final RichMessageVideoHolder holder = (RichMessageVideoHolder) viewHolder;
        if (payloadList != null) {
            final KmRichMessageModel.KmPayloadModel payloadModel = payloadList.get(position);
            WebSettings webSettings = holder.webview.getSettings();
            webSettings.setJavaScriptEnabled(customizationSettings.isJavaScriptEnabled());
            holder.webview.setWebChromeClient(new WebChromeClient());

            holder.webview.getSettings().setPluginState(WebSettings.PluginState.ON);
            if (!TextUtils.isEmpty(payloadModel.getSource())) {
                holder.videoViewRoot.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(payloadModel.getCaption())) {
                    holder.captionText.setVisibility(View.VISIBLE);
                    holder.captionText.setText(payloadModel.getCaption());

                } else {
                    holder.captionText.setVisibility(View.GONE);
                }

                holder.webViewRoot.setVisibility(View.VISIBLE);
                String currentUrl = PATH_ONE + payloadModel.getUrl() + PATH_TWO;
                holder.webview.loadData(currentUrl, text_html, "utf-8");
            } else if (!TextUtils.isEmpty(payloadModel.getUrl())) {
                holder.webViewRoot.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(payloadModel.getCaption())) {
                    holder.captionText.setVisibility(View.VISIBLE);
                    holder.captionText.setText(payloadModel.getCaption());

                } else {
                    holder.captionText.setVisibility(View.GONE);
                }

                ExoPlayer player = new ExoPlayer.Builder(context).build();
                holder.playerView.setPlayer(player);

                holder.playerView.setVisibility(View.VISIBLE);
                Uri videoUri = Uri.parse(payloadModel.getUrl());
                MediaItem mediaItem = MediaItem.fromUri(videoUri);

                player.setMediaItem(mediaItem);
                player.prepare();

                player.play();

                holder.playerView.setUseController(true);
                holder.playerView.setControllerShowTimeoutMs(1000);
                holder.playerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        player.release();
                    }

                    @Override
                    public void onViewAttachedToWindow(View v) { }
                });
            } else {
                holder.videoViewRoot.setVisibility(View.GONE);
                holder.captionText.setVisibility(View.GONE);
                holder.webViewRoot.setVisibility(View.GONE);

            }
        }
    }

    @Override
    public int getItemCount() {
        return payloadList.size();
    }


    public class RichMessageVideoHolder extends RecyclerView.ViewHolder {

        TextView captionText;
        WebView webview;
        FrameLayout webViewRoot;
        FrameLayout videoViewRoot;
        PlayerView playerView;


        public RichMessageVideoHolder(View itemView) {
            super(itemView);

            webview = itemView.findViewById(R.id.web_view);
            captionText = itemView.findViewById(R.id.tv_caption);
            webViewRoot = itemView.findViewById(R.id.web_frame_layout);
            videoViewRoot = itemView.findViewById(R.id.video_view_frame);
            playerView = itemView.findViewById(R.id.playerView);
        }
    }

    public String timeConversion(long value) {
        String videoTime;
        int duration = (int) value;
        int hrs = (duration / 3600000);
        int mns = (duration / 60000) % 60000;
        int scs = duration % 60000 / 1000;

        if (hrs > 0) {
            videoTime = String.format(TIME_WITH_HR, hrs, mns, scs);
        } else {
            videoTime = String.format(TIME_WITHOUT_HR, mns, scs);
        }
        return videoTime;
    }
}
