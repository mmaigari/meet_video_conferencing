package usama.utech.vidxa_video_conferencing.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import usama.utech.vidxa_video_conferencing.R;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    @SuppressLint("UseCompatLoadingForDrawables")
    public DividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.item_divider);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, RecyclerView parent, RecyclerView.@NotNull State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

}
