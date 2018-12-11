package com.tc.nfc.app.view.gridview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by tangjiarao on 16/10/20.
 */
public class ButtonGridView extends GridView {

    public ButtonGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonGridView(Context context) {
        super(context);
    }

    public ButtonGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
