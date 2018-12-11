package com.tc.nfc.core.interfaces;

import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.model.Book;
import com.tc.nfc.model.LibPurchase;
import com.tc.nfc.model.PurchaseNum;
import com.tc.nfc.model.SearchBookResult;
import com.tc.nfc.model.TotalPurchase;

import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/7/29.
 */
public interface PurchaseAction {
    public void getBookSingle(String isbn, String bookrecno, String IMEI,ActionCallbackListener<List<Book>> listener);
    public void getPurchaseData(String bookrecno, ActionCallbackListener<PurchaseNum> listener);
    public void getOtherLibPurchaseData(String isbn,String isPubLib, ActionCallbackListener<List<LibPurchase>> listener);
    public void getTotalPurchaseData(String IMEI,ActionCallbackListener<TotalPurchase> listener);
    public void getServerData(String imei,ActionCallbackListener<Map<String,String>> listener);
    public void purchaseBookOnline(Book book ,String copies,String action,String orderLibLocal,ActionCallbackListener<Boolean> listener);
}
