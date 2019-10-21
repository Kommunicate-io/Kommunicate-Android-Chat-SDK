package com.applozic.mobicomkit.uiwidgets.conversation.activity;

/**
 * Created by akshat on 10-Dec-16.
 */

/**
 * RecyclerView position helper class for any LayoutManager.
 *
 * compile 'com.android.support:recyclerview-v7:22.0.0'
 */

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class RecyclerViewPositionHelper {

     RecyclerView recyclerView;
     LinearLayoutManager linearLayoutManager;


    public RecyclerViewPositionHelper(RecyclerView recyclerView)
    {
          this.recyclerView=recyclerView;
    }
    public RecyclerViewPositionHelper(RecyclerView recyclerView, LinearLayoutManager linearLayoutManager) {
        this.recyclerView = recyclerView;
        this.linearLayoutManager = linearLayoutManager;
    }



    public static RecyclerViewPositionHelper createHelper(RecyclerView recyclerView) {
        if (recyclerView == null) {
            throw new NullPointerException("Recycler View is null");
        }

        return new RecyclerViewPositionHelper(recyclerView);
    }


    /**
     * Returns the adapter item count.
     *
     * @return The total number on items in a layout manager
     */
    public int getItemCount() {
        return linearLayoutManager == null ? 0 : linearLayoutManager.getItemCount();
    }

    /**
     * Returns the adapter position of the first visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.
     *
     * @return The adapter position of the first visible item or {@link RecyclerView#NO_POSITION} if
     * there aren't any visible items.
     */
    public int findFirstVisibleItemPosition() {
        final View child = findOneVisibleChild(0, linearLayoutManager.getChildCount(), false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    /**
     * Returns the adapter position of the first fully visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.
     *
     * @return The adapter position of the first fully visible item or
     * {@link RecyclerView#NO_POSITION} if there aren't any visible items.
     */
    public int findFirstCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(0, linearLayoutManager.getChildCount(), true, false);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    /**
     * Returns the adapter position of the last visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.
     *
     * @return The adapter position of the last visible view or {@link RecyclerView#NO_POSITION} if
     * there aren't any visible items
     */
    public int findLastVisibleItemPosition() {
        final View child = findOneVisibleChild(linearLayoutManager.getChildCount() - 1, -1, false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    /**
     * Returns the adapter position of the last fully visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.
     *
     * @return The adapter position of the last fully visible view or
     * {@link RecyclerView#NO_POSITION} if there aren't any visible items.
     */
    public int findLastCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(linearLayoutManager.getChildCount() - 1, -1, true, false);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                             boolean acceptPartiallyVisible) {
        OrientationHelper helper;
        if (linearLayoutManager.canScrollVertically()) {
            helper = OrientationHelper.createVerticalHelper(linearLayoutManager);
        } else {
            helper = OrientationHelper.createHorizontalHelper(linearLayoutManager);
        }

        final int start = helper.getStartAfterPadding();
        final int end = helper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = linearLayoutManager.getChildAt(i);
            final int childStart = helper.getDecoratedStart(child);
            final int childEnd = helper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }
        return partiallyVisible;
    }
}