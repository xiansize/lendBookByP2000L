package com.tc.nfc.app.view;

/**
 * Created by tangjiarao on 16/7/13.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.tc.nfc.R;

public class SelectPopupWindow extends PopupWindow {


    private Button change_btn,other_btn;
    private View window;

    public SelectPopupWindow(Activity context,OnClickListener itemOnClickListener) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        window = inflater.inflate(R.layout.popwindow_loanlayout, null);
        change_btn = (Button) window.findViewById(R.id.change_btn);
        other_btn =(Button) window.findViewById(R.id.other_btn);
        change_btn.setOnClickListener(itemOnClickListener);
        other_btn.setOnClickListener(itemOnClickListener);
        //取消按钮
//        first.setOnClickListener(new OnClickListener() {
//
//            public void onClick(View v) {
//                //销毁弹出框
//                dismiss();
//            }
//        });

        //设置SelectPicPopupWindow的View
        this.setContentView(window);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.mypopwindow_anim_style);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
//        window.setOnTouchListener(new OnTouchListener() {
//
//            public boolean onTouch(View v, MotionEvent event) {
//
//                int height = window.findViewById(R.id.pop_layout).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });

    }

}
