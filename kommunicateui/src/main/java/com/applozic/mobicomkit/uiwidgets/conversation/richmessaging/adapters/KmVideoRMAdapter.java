package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Arrays;
import java.util.List;

public class KmVideoRMAdapter extends KmRichMessageAdapter {


    private List<KmRichMessageModel.KmPayloadModel> payloadList;

    KmVideoRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, AlCustomizationSettings alCustomizationSettings) {
        super(context, model, listener, message, KmThemeHelper.getInstance(context, alCustomizationSettings));
//        this.alCustomizationSettings = alCustomizationSettings;

        if (model.getPayload() != null) {
            this.payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                    GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));
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

    }

    @Override
    void bindItems(RecyclerView.ViewHolder viewHolder, int position) {
        super.bindItems(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return payloadList.size();
    }

    public class YouTubeVideoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView captionText;
        LinearLayout rootLayout;

        public YouTubeVideoViewHolder(View itemView) {
            super(itemView);

//            imageView = itemView.findViewById(R.id.alImageView);
//            captionText = itemView.findViewById(R.id.alCaptionText);
//            rootLayout = itemView.findViewById(R.id.rootLayout);
//
//            if (listener != null) {
//                imageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        listener.onAction(context, KmRichMessage.TEMPLATE_ID + model.getTemplateId(), message, payloadList != null ? payloadList.get(getLayoutPosition()) : null, null);
//                    }
//                });
//            }
        }
    }

    public class OtherSourceVideoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView captionText;
        LinearLayout rootLayout;
        VideoView videoView;

        public OtherSourceVideoViewHolder(View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.km_rm_video_view);
            MediaController mc = new MediaController(context);
            mc.setMediaPlayer(videoView);
            videoView.setMediaController(mc);
            videoView.setVideoURI(Uri.parse("https://s3-migration-dot-applozic.appspot.com/rest/ws/aws/file/AMIfv95BPKLnhNtiYz0SNJYXTDhUkvhPYQikSjFaoL1hbg1zCcK8n6KZZfZC5Z42bfxOLaNeKSbKrt4o9pI0_ICb1fKKY8HqtcCeEuUZU8b18u8sr"));
            videoView.start();
//            captionText = itemView.findViewById(R.id.alCaptionText);
//            rootLayout = itemView.findViewById(R.id.rootLayout);
//
//            if (listener != null) {
//                imageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        listener.onAction(context, KmRichMessage.TEMPLATE_ID + model.getTemplateId(), message, payloadList != null ? payloadList.get(getLayoutPosition()) : null, null);
//                    }
//                });
//            }
        }
    }
}
