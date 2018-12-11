package com.tc.nfc.app.view.popupwindow;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.PopupWindow;

/**
 * Created by tangjiarao on 16/7/20.
 *
 * 操作员模式下，显示读者信息的 logout按钮
 */
public class LogoutWindow extends PopupWindow {


    public LogoutWindow(View view,int width,int height) {

        super(view,width,height);

        initViews();
    }
    private void initViews(){

        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new BitmapDrawable());
        //this.setAnimationStyle(R.style.mypopwindow_anim_style);
    }

    public void showWindow(View v){
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        showAsDropDown(v);
    }

}
