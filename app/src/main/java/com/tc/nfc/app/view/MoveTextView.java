package com.tc.nfc.app.view;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tc.nfc.R;

/**
 * Created by tangjiarao on 16/9/21.
 */
public class MoveTextView extends RelativeLayout{
    private Context context;
    private LayoutInflater flater ;
    private EditText editText;
    private RelativeLayout relativeLayout;
    private ImageView imageView;
    //平移动画
    private Animation translateIn;
    //默认偏移量，距离左边的距离
    private int offsetofLeft =30;
    //edittext X坐标的偏移点
    private int px1, px2;
    //动画持续时间 默认400毫秒
    private int durationMillis =400;
    private int nowleft;
    private Handler handler =new Handler();
    public interface ImageListener{
        void ImageOnClick();

        void doneOnClick();
    }
    private ImageListener imageListener;
    public MoveTextView(Context context) {
        super(context);
        flater= LayoutInflater.from(context);
        this.context =context;
        initViews();
    }
    public MoveTextView(Context context,AttributeSet attrs){

        super(context, attrs);
        flater= LayoutInflater.from(context);
        this.context =context;
        initViews();

    }
    private void initViews(){
        View view = flater.inflate(R.layout.move_layout, null);
        editText =(EditText)view.findViewById(R.id.t1);
        imageView=(ImageView)view.findViewById(R.id.image);
        relativeLayout=(RelativeLayout)view.findViewById(R.id.view);
        addView(view);
        setListener();
    }
    private void setListener(){
        editText.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if (left > offsetofLeft && left != oldLeft && oldLeft != 0) {
                    Log.d("XX", left + " " + v.getId() + " " + oldLeft);
                    nowleft = left;
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    imageView.setVisibility(VISIBLE);
                } else {
                    imageView.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE){
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    imageListener.doneOnClick();
                    return true;
                }
                return false;
            }
        });
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    imageListener.doneOnClick();
            }
        });

    }

    public void startAnimation(){

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (nowleft==0){
//                }
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        move();
//                    }
//                });
//            }
//        }).start();
        imageView.setVisibility(View.VISIBLE);

    }
    private void move(){
        //用最左边的坐标加上留空白的偏移量-editText的左坐标得出一个负的坐标
        px1 =relativeLayout.getLeft()+offsetofLeft -nowleft;
        Log.d("XX", relativeLayout.getLeft() + " " + nowleft + " " + px1);
        //默认以当前位置为起始位置
        px2 =0;

        translateIn = new TranslateAnimation(px2, px1, 0, 0);
        translateIn.setDuration(durationMillis);

        //动画效果器
        DecelerateInterpolator decelerateInterpolator =new DecelerateInterpolator();
        translateIn.setInterpolator(decelerateInterpolator);

        translateIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //动画开始时，显示image
                imageView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束后改变位置
                int left = offsetofLeft;
                int top = editText.getTop();
                int width = editText.getWidth();
                int height = editText.getHeight();
                editText.clearAnimation();
                editText.layout(left, top, left + width, top + height);
            }
        });

        editText.startAnimation(translateIn);
    }
    public void setText(String str){
        editText.setText(str);
    }
    public String getText(){
        return editText.getText().toString();
    }
    public Boolean isHasText(){
        String text =editText.getText().toString();
        return !text.equals("")? true:false;
    }
    public void setOffsetofLeft(int offsetofLeft){
        this.offsetofLeft =offsetofLeft;
    }
    public void setDurationMillis(int durationMillis){
        this.durationMillis =durationMillis;
    }
    public void setImageListener(ImageListener listener){
        imageListener =listener;
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(INVISIBLE);
                imageListener.ImageOnClick();
            }
        });
    }

    public void setHint(String hint){
        editText.setHint(hint);
    }


}
