package com.tc.nfc.core.listener;


public class ActionCallbackListenerImpl {
    private ActionCallbackListener mActionCallbackListener;

    private static ActionCallbackListenerImpl instance = new ActionCallbackListenerImpl();

    private ActionCallbackListenerImpl() {}

    public static ActionCallbackListenerImpl getInstance() {
        return instance;
    }

    public void setOnListener(ActionCallbackListener listener) {
        mActionCallbackListener = listener;
    }

    public <T> void onSuccess(T data) {
        if (mActionCallbackListener != null) {
            mActionCallbackListener.onSuccess(data);
        }
    }
    public <T> void onFailure(String errorEvent, String message) {
        if (mActionCallbackListener != null) {
            mActionCallbackListener.onFailure(errorEvent, message);
        }
    }
}

