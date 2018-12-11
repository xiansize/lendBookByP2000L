package com.tc.nfc.app.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by xiansize on 2017/3/7.
 */
public class ToastUtil {

    /**
     * 长时间展示吐司
     * @param context
     * @param info
     */
    public static void showToastLongTime(Context context, String info){
        Toast.makeText(context,info,Toast.LENGTH_SHORT).show();
    }


    /**
     * 短时间展示吐司
     * @param context
     * @param info
     */
    public static void showToastShortTime(Context context,String info){
        Toast.makeText(context,info,Toast.LENGTH_SHORT).show();
    }

}
