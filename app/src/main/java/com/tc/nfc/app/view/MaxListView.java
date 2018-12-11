package com.tc.nfc.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by tangjiarao on 16/12/27.
 */
public class MaxListView extends ListView {

    public MaxListView(Context context) {
        // TODO Auto-generated method stub
        super(context);
    }

    public MaxListView(Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        super(context, attrs);
    }

    public MaxListView(Context context, AttributeSet attrs, int defStyle) {
        // TODO Auto-generated method stub
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}