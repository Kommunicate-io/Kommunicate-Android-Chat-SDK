package io.kommunicate.ui.kommunicate.adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.database.KmAutoSuggestionDatabase;
import io.kommunicate.database.KmDatabaseHelper;
import io.kommunicate.models.KmAutoSuggestionModel;

public class KmAutoSuggestionAdapter extends RecyclerView.Adapter {

    private Context context;
    private KmRichMessageListener listener;
    public static final String KM_AUTO_SUGGESTION_ACTION = "KM_AUTO_SUGGESTION_ACTION";
    public static final String KM_AUTO_SUGGESTION_TYPED_TEXT = "TYPED_TEXT";
    private static final String CANT_MOVE_CURSOR = "Could not move cursor to position ";
    private static final String BINDING_VIEWHOLDER = " when trying to bind viewholder";
    private static final String GET_ITEM_ID = " when trying to get an item id";
    private static final String CANT_BIND_INVALID_CURSOR_STATE = "Cannot bind viewholder when cursor is in invalid state.";
    private static final String CANT_LOOKUP_INVALID_CURSOR_STATE = "Cannot lookup item id when cursor is in invalid state.";
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;

    public KmAutoSuggestionAdapter(Context context, KmRichMessageListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_quick_reply_item_layout, parent, false);
        return new KmQuickReplyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException(CANT_BIND_INVALID_CURSOR_STATE);
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException(CANT_MOVE_CURSOR + position + BINDING_VIEWHOLDER);
        }

        KmQuickReplyViewHolder mViewHolder = (KmQuickReplyViewHolder) holder;
        KmAutoSuggestionModel quickReplyModel = KmAutoSuggestionDatabase.getAutoSuggestion(mCursor);

        if (quickReplyModel != null) {
            if (!TextUtils.isEmpty(quickReplyModel.getCategory())) {
                mViewHolder.kmQuickReplyTitle.setVisibility(View.VISIBLE);
                mViewHolder.kmQuickReplyTitle.setText("/" + quickReplyModel.getCategory().trim());
            } else {
                mViewHolder.kmQuickReplyTitle.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(quickReplyModel.getContent())) {
                mViewHolder.kmQuickReplyMessage.setVisibility(View.VISIBLE);
                mViewHolder.kmQuickReplyMessage.setText(quickReplyModel.getContent().trim());
            } else {
                mViewHolder.kmQuickReplyMessage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDataValid) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (!mDataValid) {
            throw new IllegalStateException(CANT_LOOKUP_INVALID_CURSOR_STATE);
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException(CANT_MOVE_CURSOR + position + GET_ITEM_ID);
        }

        return mCursor.getLong(mRowIDColumn);
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        if (newCursor != null) {
            mCursor = newCursor;
            mRowIDColumn = mCursor.getColumnIndexOrThrow(KmDatabaseHelper.ID);
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            mCursor = null;
            mRowIDColumn = -1;
            mDataValid = false;
        }
    }

    private class KmQuickReplyViewHolder extends RecyclerView.ViewHolder {

        private TextView kmQuickReplyTitle;
        private TextView kmQuickReplyMessage;

        public KmQuickReplyViewHolder(View itemView) {
            super(itemView);

            kmQuickReplyTitle = itemView.findViewById(R.id.kmAutoSuggestionTitle);
            kmQuickReplyMessage = itemView.findViewById(R.id.kmAutoSuggestionMessage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        if (mCursor != null) {
                            mCursor.moveToPosition(getAdapterPosition());
                            KmAutoSuggestionModel autoSuggestion = KmAutoSuggestionDatabase.getAutoSuggestion(mCursor);
                            if (autoSuggestion != null) {
                                listener.onAction(context, KM_AUTO_SUGGESTION_ACTION, null, autoSuggestion.getContent(), null);
                            }
                        }
                    }
                }
            });
        }
    }
}
