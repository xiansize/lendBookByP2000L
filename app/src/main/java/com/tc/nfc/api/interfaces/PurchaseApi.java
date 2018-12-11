package com.tc.nfc.api.interfaces;

import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.Book;
import com.tc.nfc.model.LibPurchase;

import java.util.List;

/**
 * Created by tangjiarao on 16/7/29.
 */
public interface PurchaseApi {

    public String getBookSingle(String isbn, String bookrecno, String IMEI);
    public String getPurchaseData(String bookrecno);
    public String getOtherLibPurchaseData(String isbn, String isPubLib);
    public String getTotalPurchaseData(String IMEI);
    public String getServerData(String imei);
    public String purchaseBookOnline(Book book ,String copies,String action,String orderLibLocal);
}
