package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.json.GsonUtils;


import java.util.Arrays;
import java.util.List;

public class KmVideoRMAdapter extends KmRichMessageAdapter {


    private List<KmRichMessageModel.KmPayloadModel> payloadList;
    double currentPos, totalDuration;


    KmVideoRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper, boolean isMessageProcessed) {
        super(context, model, listener, message, themeHelper);
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
                holder.videoViewRoot.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(payloadModel.getCaption())) {
                    holder.captionText.setVisibility(View.VISIBLE);
                    holder.captionText.setText(payloadModel.getCaption());

                } else {
                    holder.captionText.setVisibility(View.GONE);
                }

                holder.webViewRoot.setVisibility(View.VISIBLE);
                String currentUrl = "<iframe width=\"100%\" height=\"100%\" src=\"" + payloadModel.getUrl() + "\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";
                holder.webview.loadData(currentUrl, "text/html", "utf-8");
            } else if (!TextUtils.isEmpty(payloadModel.getUrl())) {
                holder.webViewRoot.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(payloadModel.getCaption())) {
                    holder.captionText.setVisibility(View.VISIBLE);
                    holder.captionText.setText(payloadModel.getCaption());

                } else {
                    holder.captionText.setVisibility(View.GONE);
                }

                holder.videoView.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse(payloadModel.getUrl());

                holder.videoView.setVideoURI(uri);

                holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // Play From here
                        holder.videoProgress.setVisibility(View.GONE);
                        holder.mediaController.setVisibility(View.VISIBLE);
                        currentPos = holder.videoView.getCurrentPosition();
                        totalDuration = holder.videoView.getDuration();
                        holder.tvCurrentSeconds.setText((timeConversion((long) currentPos)));
                        holder.tvTotalTime.setText((timeConversion((long) totalDuration)));
                        holder.seekBar.setMax((int) totalDuration);
                        final Handler handler = new Handler();

                        // To Update Seek bar
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    currentPos = holder.videoView.getCurrentPosition();
                                    holder.tvCurrentSeconds.setText(timeConversion((long) currentPos));
                                    holder.seekBar.setProgress((int) currentPos);
                                    handler.postDelayed(this, 1000);
                                } catch (IllegalStateException ed) {
                                    ed.printStackTrace();
                                }
                            }
                        };
                        handler.postDelayed(runnable, 1000);
                        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                currentPos = seekBar.getProgress();
                                holder.videoView.seekTo((int) currentPos);
                            }
                        });
                    }
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    holder.videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
                                holder.videoProgress.setVisibility(View.GONE);
                            }
                            if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                                holder.videoProgress.setVisibility(View.VISIBLE);
                            }
                            if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                                holder.videoProgress.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }
                    });
                }
                holder.ivPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.videoView.isPlaying()) {
                            holder.videoView.pause();
                            holder.ivPlay.setImageResource(R.drawable.ic_play_video);

                        } else {
                            holder.videoView.start();
                            holder.ivPlay.setImageResource(R.drawable.ic_pause_video);
                        }
                    }
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
        VideoView videoView;
        FrameLayout videoViewRoot;
        ProgressBar videoProgress;
        LinearLayout mediaController;
        ImageView ivPlay;
        TextView tvCurrentSeconds;
        TextView tvTotalTime;
        SeekBar seekBar;


        public RichMessageVideoHolder(View itemView) {
            super(itemView);

            webview = itemView.findViewById(R.id.web_view);
            captionText = itemView.findViewById(R.id.tv_caption);
            webViewRoot = itemView.findViewById(R.id.web_frame_layout);
            videoView = itemView.findViewById(R.id.video_view);
            videoViewRoot = itemView.findViewById(R.id.video_view_frame);
            videoProgress = itemView.findViewById(R.id.video_progress);
            mediaController = itemView.findViewById(R.id.ll_player);
            ivPlay = itemView.findViewById(R.id.iv_play_pause);
            tvCurrentSeconds = itemView.findViewById(R.id.current);
            tvTotalTime = itemView.findViewById(R.id.total);
            seekBar = itemView.findViewById(R.id.seekbar);
        }
    }

    public String timeConversion(long value) {
        String videoTime;
        int duration = (int) value;
        int hrs = (duration / 3600000);
        int mns = (duration / 60000) % 60000;
        int scs = duration % 60000 / 1000;

        if (hrs > 0) {
            videoTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            videoTime = String.format("%02d:%02d", mns, scs);
        }
        return videoTime;
    }
}
