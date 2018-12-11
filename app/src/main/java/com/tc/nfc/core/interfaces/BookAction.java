package com.tc.nfc.core.interfaces;

import android.content.Intent;
import android.os.Handler;

import com.tc.nfc.core.listener.ActionCallbackListener;
import com.tc.nfc.core.task.ThreadPoolManager;
import com.tc.nfc.model.Book;
import com.tc.nfc.model.BookHolding;
import com.tc.nfc.model.ReturnBookResult;
import com.tc.nfc.model.ShelvesResult;

import java.util.List;
import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public interface BookAction {

    public void p2000LGetBarcode(ActionCallbackListener<String> listener);
    public void getNFCBarcode(Intent intent, ActionCallbackListener<String> listener);
    public void loanBook(String reader, String barcode, ActionCallbackListener<Map<String, Object>> listener);
    public void returnBook(String barcode, ActionCallbackListener<Map<String, Object>> listener);
    public void loanBookData(String readerId, ActionCallbackListener<List<Book>> listener);
    public void reloanBook(String readerId, String barcode, ActionCallbackListener<String> listener);
    public void reloanBookMany(String readerId, List<Book> book, ActionCallbackListener<List<ReturnBookResult>> listener);
    public void isBookCanLoadn(String readerId, String barcode, ActionCallbackListener<Map<String, Object>> listener);
    public void getBookData(String barcode, ThreadPoolManager poolManager, Handler handler);
    public void checkBookSingle(String barcode, String countSignFlagNum, ThreadPoolManager poolManager, Handler handler);
    public void shelvesBook(ShelvesResult shelvesResult, String shelvesNum, ThreadPoolManager poolManager, Handler handler);
    public void addBookLog(String barcode, ThreadPoolManager poolManager, Handler handler);
    public void showBookInfo(String readerId, String barcode, ActionCallbackListener<Map<String, Object>> listener);
    public void bookHolding(String bookrecno, ActionCallbackListener<List<BookHolding>> listener);
    public void shelveBookFast(String barcode, String shelvesNum, ThreadPoolManager poolManager, Handler handler);

}
