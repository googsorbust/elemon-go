package com.frozendevs.periodictable.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.frozendevs.periodictable.model.adapter.PropertiesAdapter;
import org.jetbrains.annotations.NotNull;

public class DividerDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = {android.R.attr.listDivider};

    private Drawable mDivider;

    public DividerDecoration(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(ATTRS);
        mDivider = typedArray.getDrawable(0);
        typedArray.recycle();
    }

    @Override
    public void onDrawOver(@NotNull Canvas canvas, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            if (isDecorated(child, parent)) {
                View nextChild = null;
                if (i < childCount - 1) nextChild = parent.getChildAt(i + 1);
                if (nextChild != null && !isDecorated(nextChild, parent)) {
                    continue;
                }
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin +
                        (int) ViewCompat.getTranslationY(child);
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
        }
    }

    private boolean isDecorated(View view, RecyclerView parent) {
        RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);

        return !(holder instanceof PropertiesAdapter.ViewHolder &&
                holder.getItemViewType() == PropertiesAdapter.VIEW_TYPE_HEADER);
    }
}
