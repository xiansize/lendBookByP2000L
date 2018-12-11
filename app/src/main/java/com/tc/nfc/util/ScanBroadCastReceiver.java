package com.tc.nfc.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by tangjiarao on 16/12/22.
 */
public class ScanBroadCastReceiver extends BroadcastReceiver
{

    public interface sendIsbnListener{
        void sendIsbn(String isbn);
    }
    private sendIsbnListener listener;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (action.equals("com.android.scancontext")){
            String isbn = intent.getStringExtra("Scan_context");
            Log.d("XX", "----- isbn ----->"+isbn);
            if(listener!=null)
                listener.sendIsbn(isbn);
        }

    }
    public void setSendIsbnListener(sendIsbnListener listener){
        this.listener =listener;
    }

}