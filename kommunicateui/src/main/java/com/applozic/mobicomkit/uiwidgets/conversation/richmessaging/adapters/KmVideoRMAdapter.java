package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


import java.util.Arrays;
import java.util.List;

public class KmVideoRMAdapter extends KmRichMessageAdapter {


    private List<KmRichMessageModel.KmPayloadModel> payloadList;
    SimpleExoPlayer simpleExoPlayer;

    DataSource.Factory mediaDataSourceFactory;


    KmVideoRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper, boolean isMessageProcessed) {
        super(context, model, listener, message, themeHelper);
        if (model.getPayload() != null) {
            this.payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                    GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));
            mediaDataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "mediaPlayerSample"));
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

    @Override
    void bindItems(RecyclerView.ViewHolder viewHolder, int position) {
        super.bindItems(viewHolder, position);
        final RichMessageVideoHolder holder = (RichMessageVideoHolder) viewHolder;
        if (payloadList != null) {
            final KmRichMessageModel.KmPayloadModel payloadModel = payloadList.get(position);
            WebSettings webSettings = holder.webview.getSettings();
            webSettings.setJavaScriptEnabled(true);
            holder.webview.setWebChromeClient(new WebChromeClient());

            holder.webview.getSettings().setPluginState(WebSettings.PluginState.ON);
            if (!TextUtils.isEmpty(payloadModel.getSource())) {
                holder.player.setVisibility(View.GONE);
                holder.captionText.setVisibility(View.GONE);
                holder.webview.setVisibility(View.VISIBLE);
                String currentUrl = "<iframe width=\"100%\" height=\"100%\" src=\"" + payloadModel.getUrl() + "\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";
                holder.webview.loadData(currentUrl, "text/html", "utf-8");
            } else if (!TextUtils.isEmpty(payloadModel.getUrl())) {
                holder.player.setVisibility(View.VISIBLE);
                holder.webview.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(payloadModel.getCaption())){
                    holder.captionText.setVisibility(View.VISIBLE);
                    holder.captionText.setText(payloadModel.getCaption());

                }else{
                    holder.captionText.setVisibility(View.GONE);
                }

                ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(payloadModel.getUrl()));

                MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(mediaDataSourceFactory);


                simpleExoPlayer = new SimpleExoPlayer.Builder(context)
                        .setMediaSourceFactory(mediaSourceFactory)
                        .build();

                simpleExoPlayer.addMediaSource(mediaSource);
                holder.player.setShutterBackgroundColor(Color.TRANSPARENT);

                simpleExoPlayer.setPlayWhenReady(true);
                holder.player.setPlayer(simpleExoPlayer);
                holder.player.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        holder.player.getPlayer().release();

                    }
                });

            }else{
                holder.player.setVisibility(View.GONE);
                holder.captionText.setVisibility(View.GONE);
                holder.webview.setVisibility(View.GONE);

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
        PlayerView player;


        public RichMessageVideoHolder(View itemView) {
            super(itemView);

            webview = itemView.findViewById(R.id.webview);
            captionText = itemView.findViewById(R.id.tv_caption);
            player = itemView.findViewById(R.id.playerView);
        }
    }
}
