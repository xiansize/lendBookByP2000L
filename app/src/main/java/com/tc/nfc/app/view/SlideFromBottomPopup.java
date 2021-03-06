package com.tc.nfc.app.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import com.tc.nfc.R;

import razerdp.basepopup.BasePopupWindow;

public class SlideFromBottomPopup extends BasePopupWindow {

    private View.OnClickListener itemOnClickListener;
    private View popupView;
    public SlideFromBottomPopup(Activity context,View.OnClickListener itemOnClickListener) {
        super(context);
        this.itemOnClickListener =itemOnClickListener;
        bindEvent();
    }

    @Override
    protected Animation getShowAnimation() {
        return  getTranslateAnimation(250 * 2, 0, 300);
    }
    public Animation getExitAnimation(){
        return getTranslateAnimation(0,400*2,300);
    }

    @Override
    protected View getClickToDismissView() {
        return popupView.findViewById(R.id.click_to_dismiss);
    }

    @Override
    public View getInputView() {
        return null;
    }

    @Override
    public View getPopupView() {
        popupView= LayoutInflater.from(mContext).inflate(R.layout.popwindow_loanlayout,null);
        return popupView;
    }

    @Override
    public View getAnimaView() {
        return popupView.findViewById(R.id.popup_anima);
    }

    private void bindEvent() {
        if (popupView!=null){
            popupView.findViewById(R.id.change_btn).setOnClickListener(itemOnClickListener);
            popupView.findViewById(R.id.other_btn).setOnClickListener(itemOnClickListener);
        }

    }
}  