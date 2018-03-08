package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Created by devashish on 3/2/14.
 */
public class ConversationListView extends ListView {
    private boolean scrollToBottomOnSizeChange;
    private EditText messageEditText;

    public ConversationListView(Context context) {
        super(context);
    }

    public ConversationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConversationListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setScrollToBottomOnSizeChange(boolean scrollToBottomOnSizeChange) {
        this.scrollToBottomOnSizeChange = scrollToBottomOnSizeChange;
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scrollToBottomOnSizeChange) {
                    smoothScrollToPosition(getCount() - 1);
                    setSelection(getCount());
                    //Note: Setting text to messageEditText, it is required to fix the problem with list scroll not
                    //working properly on window keypad open
                    int selectionStart = messageEditText.getSelectionStart();
                    messageEditText.setText(messageEditText.getText().toString());
                    messageEditText.setSelection(selectionStart);
                }
            }
        }, 200L);
    }

    public void setMessageEditText(EditText messageEditText) {
        this.messageEditText = messageEditText;
    }
}