package com.tc.nfc.api.interfaces;

import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.core.task.ThreadPoolTaskBitmap;

import org.json.JSONObject;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface BookApi {

    public String P2000LGetBarcode();
    public String isBookCanLoadn(String readerId, String barcode);
    public String loanBook(String readerId, String barcode);
    public String returnBook(String barcode);
    public String loanBookData(String readerId);
    public String reloanBook(String readerId, String barcode);
    public void checkBookSingle(String barcode, String countSignFlagNum, ThreadPoolManager poolManager, ThreadPoolTaskBitmap.CallBack callBack);
    public void shelvesBook(String barcode, String shelvesNum, ThreadPoolManager poolManager, ThreadPoolTaskBitmap.CallBack callBack);
    public void getBookData(String barcode, ThreadPoolManager poolManager, ThreadPoolTaskBitmap.CallBack callBack);
    public void addBookLog(String barcode, ThreadPoolManager poolManager, ThreadPoolTaskBitmap.CallBack callBack);
    public String showBookInfo(String readerId, String barcode);
    public String bookHolding(String bookrecno);
}
