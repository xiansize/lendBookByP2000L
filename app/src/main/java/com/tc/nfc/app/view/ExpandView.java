package com.tc.nfc.app.view;

/**
 * Created by tangjiarao on 16/7/14.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tc.nfc.R;

public class ExpandView extends FrameLayout{


    private Animation mExpandAnimation;
    private Animation mCollapseAnimation;
    private boolean mIsExpand;
    private View view = null;
    private int expandViewId;

    public ExpandView(Context context) {
        this(context,null);
        // TODO Auto-generated constructor stub
    }
    public ExpandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }
    public ExpandView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initExpandView();
    }
    private void initExpandView() {
        //LayoutInflater.from(getContext()).inflate(R.layout.view_expand, this, true);

        mExpandAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.view_expand);
        mExpandAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setContentView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.VISIBLE);
            }
        });

        mCollapseAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.view_collapse);
        mCollapseAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //setVisibility(View.INVISIBLE);
                removeView(view);
            }
        });

    }
    public void collapse() {
        if (mIsExpand) {
            mIsExpand = false;
            clearAnimation();
            startAnimation(mCollapseAnimation);
        }
    }

    public void expand() {
        if (!mIsExpand) {
            mIsExpand = true;
            clearAnimation();
            startAnimation(mExpandAnimation);
        }
    }

    public boolean isExpand() {
        return mIsExpand;
    }

    public void setContentView(){

        view = LayoutInflater.from(getContext()).inflate(expandViewId, null);
        addView(view);
    }
    public void setExpandView(int expandViewId){
        this.expandViewId = expandViewId;
    }

}