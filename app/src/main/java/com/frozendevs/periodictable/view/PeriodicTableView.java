package com.frozendevs.periodictable.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.BaseAdapter;
import org.tensorflow.lite.examples.detection.R;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class PeriodicTableView extends ZoomableScrollView {

    private final float DEFAULT_SPACING = 1f;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Adapter mAdapter;
    private Matrix mMatrix = new Matrix();
    private OnItemClickListener mOnItemClickListener;
    private View mActiveView;
    private LruCache<Integer, Bitmap> mBitmapCache;
    private int mTileSize;
    private GenerateBitmapsTask mGenerateBitmapsTask;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateEmptyStatus(true);

            if (mAdapter.isEmpty()) {
                return;
            }

            if (mBitmapCache.size() < mAdapter.getCount()) {
                if (mGenerateBitmapsTask != null) {
                    mGenerateBitmapsTask.cancel(true);
                }

                mGenerateBitmapsTask = new GenerateBitmapsTask();
                mGenerateBitmapsTask.execute();
            } else {
                onGenerateComplete();
            }
        }
    };
    private OnClickConfirmedListener mOnSingleTapConfirmed = new OnClickConfirmedListener() {
        @Override
        void onClickConfirmed(int position) {
            playSoundEffect(SoundEffectConstants.CLICK);

            mOnItemClickListener.onItemClick(PeriodicTableView.this, mActiveView, position);

            if (mActiveView != null) {
                mActiveView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
        }
    };
    private OnClickConfirmedListener mOnDownConfirmed;

    public PeriodicTableView(Context context) {
        super(context);

        initPeriodicTableView();
    }

    public PeriodicTableView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPeriodicTableView();
    }

    public PeriodicTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPeriodicTableView();
    }

    private void initPeriodicTableView() {
        mOnDownConfirmed = new OnClickConfirmedListener() {
            @Override
            void onClickConfirmed(int position) {
                addActiveView(position);
            }
        };
    }

    public void setEmptyView(View view) {
        mEmptyView = view;

        updateEmptyStatus(mAdapter == null || mAdapter.isEmpty() || mBitmapCache == null ||
                mBitmapCache.size() < mAdapter.getCount());
    }

    private void updateEmptyStatus(boolean empty) {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(empty ? VISIBLE : GONE);
        }
    }

    private float getScaledTileSize() {
        return getZoom() * mTileSize;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return mOnItemClickListener != null && processClick(event, mOnSingleTapConfirmed);
    }

    @Override
    protected int getScaledWidth() {
        int groups = mAdapter != null ? mAdapter.getGroupsCount() : 0;

        return Math.round((getScaledTileSize() * groups) + ((groups - 1) * DEFAULT_SPACING));
    }

    @Override
    protected int getScaledHeight() {
        int periods = mAdapter != null ? mAdapter.getPeriodsCount() : 0;

        return Math.round((getScaledTileSize() * periods) +
                ((periods - 1) * DEFAULT_SPACING));
    }

    public void setAdapter(Adapter adapter) {
        if (adapter != null && mBitmapCache == null) {
            throw new IllegalStateException("Initialize bitmap cache using setBitmapCache() first");
        }

        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;

        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public float getMinimalZoom() {
        final int groups = mAdapter.getGroupsCount();
        final int periods = mAdapter.getPeriodsCount();
        final int tileSize = mTileSize;

        return Math.min((getWidth() - ((groups - 1) * DEFAULT_SPACING)) / groups,
                (getHeight() - ((periods - 1) * DEFAULT_SPACING)) / periods) / tileSize;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        adjustActiveView();

        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mBitmapCache != null && mAdapter != null && !mAdapter.isEmpty()) {
            float tileSize = getScaledTileSize();

            float y = (getHeight() - getScaledHeight()) / 2f;

            for (int row = 0; row < mAdapter.getPeriodsCount(); row++) {
                float x = (getWidth() - getScaledWidth()) / 2f;

                for (int column = 0; column < mAdapter.getGroupsCount(); column++) {
                    if (x + tileSize > getScrollX() && x < getScrollX() + getWidth() &&
                            y + tileSize > getScrollY() && y < getScrollY() + getHeight()) {
                        int position = (row * mAdapter.getGroupsCount()) + column;

                        if (mActiveView != null && indexOfChild(mActiveView) >= 0 &&
                                position == (int) mActiveView.getTag(R.id.active_view_position)) {
                            adjustActiveView();
                        } else {
                            Bitmap bitmap = mBitmapCache.get(position);

                            if (bitmap != null && !bitmap.isRecycled()) {
                                mMatrix.reset();
                                mMatrix.postScale(getZoom(), getZoom());
                                mMatrix.postTranslate(x, y);

                                canvas.drawBitmap(bitmap, mMatrix, mPaint);
                            }
                        }
                    }

                    x += tileSize + DEFAULT_SPACING;
                }

                y += tileSize + DEFAULT_SPACING;
            }
        }

        super.dispatchDraw(canvas);
    }

    private void adjustActiveView() {
        if (mActiveView != null) {
            int position = (int) mActiveView.getTag(R.id.active_view_position);

            float tileSize = getScaledTileSize();

            mActiveView.setScaleX(getZoom());
            mActiveView.setScaleY(getZoom());
            mActiveView.setTranslationX(((getWidth() - getScaledWidth()) / 2f) +
                    ((position % mAdapter.getGroupsCount()) * (tileSize + DEFAULT_SPACING)));
            mActiveView.setTranslationY(((getHeight() - getScaledHeight()) / 2f) +
                    ((position / mAdapter.getGroupsCount()) * (tileSize + DEFAULT_SPACING)));
        }
    }

    private void addActiveView(int position) {
        if (mActiveView != null) {
            if (position == (int) mActiveView.getTag(R.id.active_view_position)) {
                adjustActiveView();

                return;
            }

            removeView(mActiveView);
        }

        mActiveView = mAdapter.getActiveView(mBitmapCache.get(position), mActiveView, this);

        if (mActiveView != null) {
            mActiveView.setTag(R.id.active_view_position, position);
            mActiveView.setPivotX(0f);
            mActiveView.setPivotY(0f);

            adjustActiveView();

            addView(mActiveView);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());

        savedState.tileSize = mTileSize;

        if (mActiveView != null) {
            savedState.activeViewPosition = (int) mActiveView.getTag(R.id.active_view_position);
        }

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;

            mTileSize = savedState.tileSize;

            if (!mAdapter.isEmpty() && savedState.activeViewPosition > -1) {
                addActiveView(savedState.activeViewPosition);
            }

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        adjustActiveView();
    }

    public View getActiveView() {
        return mActiveView;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        super.onDown(event);

        return processClick(event, mOnDownConfirmed);
    }

    private boolean processClick(MotionEvent event, OnClickConfirmedListener listener) {
        if (listener != null && mAdapter != null && !mAdapter.isEmpty()) {
            final float rawX = event.getX() + getScrollX();
            final float rawY = event.getY() + getScrollY();
            final float tileSize = getScaledTileSize();
            final int scaledWidth = getScaledWidth();
            final int scaledHeight = getScaledHeight();
            final float startY = (getHeight() - scaledHeight) / 2f;
            final float startX = (getWidth() - scaledWidth) / 2f;

            if (rawX >= startX && rawX <= startX + scaledWidth &&
                    rawY >= startY && rawY <= startY + scaledHeight) {
                final int position = ((int) ((rawY - startY) / (tileSize + DEFAULT_SPACING)) *
                        mAdapter.getGroupsCount()) + (int) ((rawX - startX) / (tileSize + DEFAULT_SPACING));

                final int size = mAdapter.getGroupsCount() * mAdapter.getPeriodsCount();

                if (position >= 0 && position < size && mAdapter.isEnabled(position)) {
                    listener.onClickConfirmed(position);

                    return true;
                }
            }
        }

        return false;
    }

    public void setBitmapCache(LruCache<Integer, Bitmap> bitmapCache) {
        mBitmapCache = bitmapCache;
    }

    private void onGenerateComplete() {
        if (mActiveView == null) {
            final int size = mAdapter.getGroupsCount() * mAdapter.getPeriodsCount();

            for (int i = 0; i < size; i++) {
                if (mAdapter.getItem(i) != null && mAdapter.isEnabled(i)) {
                    addActiveView(i);

                    break;
                }
            }
        }

        invalidate();

        updateEmptyStatus(false);
    }

    public interface OnItemClickListener {
        void onItemClick(PeriodicTableView parent, View view, int position);
    }

    public static abstract class Adapter extends BaseAdapter {
        public abstract View getActiveView(Bitmap bitmap, View convertView, ViewGroup parent);

        public abstract int getGroupsCount();

        public abstract int getPeriodsCount();
    }

    private static class SavedState extends View.BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        int activeViewPosition = -1;
        int tileSize;

        public SavedState(Parcel source) {
            super(source);

            activeViewPosition = source.readInt();
            tileSize = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(activeViewPosition);
            out.writeInt(tileSize);
        }
    }

    private abstract class OnClickConfirmedListener {
        abstract void onClickConfirmed(int position);
    }

    private class GenerateBitmapsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final int size = mAdapter.getGroupsCount() * mAdapter.getPeriodsCount();

            final Map<Integer, SoftReference<View>> convertViews = new HashMap<>();

            for (int position = 0; position < size; position++) {
                if (isCancelled()) {
                    return null;
                }

                if (mBitmapCache.get(position) != null) {
                    continue;
                }

                final int viewType = mAdapter.getItemViewType(position);

                View convertView = null;

                final SoftReference<View> softReference = convertViews.get(viewType);

                if (softReference != null) {
                    convertView = softReference.get();
                }

                convertView = mAdapter.getView(position, convertView, PeriodicTableView.this);

                if (convertView != null) {
                    final Bitmap bitmap = generateBitmap(convertView);

                    if (bitmap != null) {
                        mBitmapCache.put(position, bitmap);
                    }
                }

                if (softReference == null || softReference.get() == null) {
                    convertViews.put(viewType, new SoftReference<>(convertView));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!isCancelled()) {
                onGenerateComplete();
            }
        }

        private Bitmap generateBitmap(View view) {
            Bitmap bitmap = null;

            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

            mTileSize = Math.max(mTileSize, Math.max(layoutParams.width,
                    layoutParams.height));

            view.measure(View.MeasureSpec.makeMeasureSpec(layoutParams.width,
                    View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                    layoutParams.height, View.MeasureSpec.EXACTLY));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            view.buildDrawingCache();

            final Bitmap drawingCache = view.getDrawingCache();

            if (drawingCache != null) {
                bitmap = Bitmap.createBitmap(drawingCache);
            }

            view.destroyDrawingCache();

            return bitmap;
        }
    }
}
