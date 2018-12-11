package com.tc.nfc.app.view;

/**
 * Created by tangjiarao on 16/7/4.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ScrollView;

public class FlexibleScrollView extends ScrollView {

    private Context mContext;
    private static int mMaxOverDistance = 50;

    public FlexibleScrollView(Context context, AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    public FlexibleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    public FlexibleScrollView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    private void initView() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float density = metrics.density;
        mMaxOverDistance = (int) (density * mMaxOverDistance);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX, mMaxOverDistance,
                isTouchEvent);
    }
}
