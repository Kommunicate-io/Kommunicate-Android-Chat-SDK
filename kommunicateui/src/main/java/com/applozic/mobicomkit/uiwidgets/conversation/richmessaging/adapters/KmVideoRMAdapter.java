package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
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
        return new OtherSourceVideoViewHolder(itemView);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        bindItems(viewHolder, i);
    }

    @Override
    void bindItems(RecyclerView.ViewHolder viewHolder, int position) {
        super.bindItems(viewHolder, position);
        final OtherSourceVideoViewHolder holder = (OtherSourceVideoViewHolder) viewHolder;
        if (payloadList != null) {
            final KmRichMessageModel.KmPayloadModel payloadModel = payloadList.get(position);
            WebSettings webSettings = holder.webview.getSettings();
            webSettings.setJavaScriptEnabled(true);
            holder.webview.setWebChromeClient(new WebChromeClient());

            holder.webview.getSettings().setPluginState(WebSettings.PluginState.ON);
            if (!TextUtils.isEmpty(payloadModel.getSource())) {
                holder.player.setVisibility(View.GONE);
                holder.webview.setVisibility(View.VISIBLE);
                holder.captionText.setText("Youtube Video Item");
                String currentUrl = "<iframe width=\"100%\" height=\"100%\" src=\"" + payloadModel.getUrl() + "\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";
                holder.webview.loadData(currentUrl, "text/html", "utf-8");
            } else {
                holder.player.setVisibility(View.VISIBLE);
                holder.webview.setVisibility(View.GONE);
                holder.captionText.setText("Video Item");
//                try {
//                    MediaController mc = new MediaController(context);
//                    mc.setMediaPlayer(holder.videoView);
//                    holder.videoView.setMediaController(mc);
//                    mc.setAnchorView(holder.videoView);
//                    holder.videoView.setVideoURI(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"));
////
////                   holder.videoView.setVideoURI(Uri.parse("https://s3-migration-dot-applozic.appspot.com/
//      rest/ws/aws/file/AMIfv95BPKLnhNtiYz0SNJYXTDhUkvhPYQik
//      SjFaoL1hbg1zCcK8n6KZZfZC5Z42bfxOLaNeKSbKrt4o9pI0_ICb1fKK
//      Y8HqtcCeEuUZU8b18u8sr"));
//                    holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
////                            holder.videoView.start();
//
//// do something when video is ready to play, you want to start playing video here
//
//                        }
//                    });
//
//                } catch (Exception e) {
//                    Log.e("Error10", e.getMessage());
//                    e.printStackTrace();
////                }
                String tempUrl = "https://s3-migration-dot-applozic.appspot.com/rest/ws/aws/file/AMIfv95BPKLnhNtiYz0SNJYXTDhUkvhPYQikSjFaoL1hbg1zCcK8n6KZZfZC5Z42bfxOLaNeKSbKrt4o9pI0_ICb1fKKY8HqtcCeEuUZU8b18u8sr-4U5d4rFtmC79cn_0xaiV0MQfF01T0hAea8sQANoaYuvR5hQJHiFSB047yp1mkfqcwNb_ExaX1xutdJ8Qamkjp8xTKjlPiG0920T7qqLFIBxl1eGL7ikvTCtXG_f8Me8OKsEOhUXOP7LLuJLcOG_p8cijsp";
//

                ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(tempUrl));

                MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(mediaDataSourceFactory);


                simpleExoPlayer = new SimpleExoPlayer.Builder(context)
                        .setMediaSourceFactory(mediaSourceFactory)
                        .build();

                simpleExoPlayer.addMediaSource(mediaSource);

                simpleExoPlayer.setPlayWhenReady(true);
//                et.player = simpleExoPlayer
                holder.player.setPlayer(simpleExoPlayer);
//                holder.player.requestFocus();

//
//                SimpleMediaSource mediaSource = new SimpleMediaSource(tempUrl);//uri also supported
//               holder.exoVideoView.play(mediaSource);
//                holder.exoVideoView.play(mediaSource,0);

            }
        }
    }

    @Override
    public int getItemCount() {
        return payloadList.size();
    }


    public class OtherSourceVideoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView captionText;
        LinearLayout rootLayout;
        VideoView videoView;
        WebView webview;
        MediaController mediaController;
        PlayerView player;
//        ExoVideoView exoVideoView;


        public OtherSourceVideoViewHolder(View itemView) {
            super(itemView);

//            videoView = itemView.findViewById(R.id.km_rm_video_view);
            webview = itemView.findViewById(R.id.webview);
            captionText = itemView.findViewById(R.id.tv_caption);
            player = itemView.findViewById(R.id.playerView);
//            exoVideoView = itemView.findViewById(R.id.videoView);
        }
    }
}
