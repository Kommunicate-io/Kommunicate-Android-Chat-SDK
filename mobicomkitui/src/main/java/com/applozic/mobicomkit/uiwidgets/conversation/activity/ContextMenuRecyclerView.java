package com.applozic.mobicomkit.uiwidgets.conversation.activity;

/**
 * Created by akshat on 14-Dec-16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;


/**
 * Created by Adarsh on 12/16/16.
 */

public class ContextMenuRecyclerView extends RecyclerView {


    public ContextMenuRecyclerView(Context context) {
        super(context);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private RecyclerViewContextMenuInfo mContextMenuInfo;

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        try {
            final int longPressPosition = getChildAdapterPosition(originalView);
            ;          //getChildLayoutPosition(originalView);  //getChildPosition(newView) ;
            if (longPressPosition >= 0) {
                final long longPressId = getAdapter().getItemId(longPressPosition);
                mContextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId);
                return super.showContextMenuForChild(originalView);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public RecyclerViewContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }
}