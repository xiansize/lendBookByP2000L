package com.tc.nfc.api.implement;


import com.tc.nfc.api.interfaces.PurchaseApi;
import com.tc.nfc.api.interfaces.SearchApi;
import com.tc.nfc.api.utils.NetworkHelp;
import com.tc.nfc.model.Book;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangjiarao on 16/6/23.
 */
public class PurchaseApiImpl implements PurchaseApi{

    private final String TAG = "PurchaseApiImpl";

    @Override
    public String getBookSingle(String isbn, String bookrecno, String IMEI) {
        Map<String, String> params =new HashMap<>();
        params.put("isbn",isbn);
        params.put("bookrecno",bookrecno);
        params.put("IMEI",IMEI);

        return NetworkHelp.sendDataByPost2(params, "UTF-8", "http://192.168.0.169:28081/interlib3/service/purchase/getBibliosByISBN");

    }

    @Override
    public String getPurchaseData(String bookrecno) {

        Map<String, String> params =new HashMap<>();
        params.put("bookrecno",bookrecno);

        return NetworkHelp.sendDataByPost2(params, "UTF-8", "http://192.168.0.169:28081/interlib3/service/purchase/getCountByLong");
    }

    @Override
    public String getOtherLibPurchaseData(String isbn,String isPubLib) {


        Map<String, String> params =new HashMap<>();
        params.put("isbn",isbn);
        params.put("isPubLib",isPubLib);

        return NetworkHelp.sendDataByPost2(params, "UTF-8", "http://192.168.0.169:28081/interlib3/common/consult/api/list1");

    }

    @Override
    public String getTotalPurchaseData(String IMEI) {
        Map<String, String> params =new HashMap<>();
        params.put("IMEI",IMEI);

        return NetworkHelp.sendDataByPost2(params, "UTF-8", "http://192.168.0.169:28081/interlib3/service/purchase/statisticsOrderInfo");
    }

    @Override
    public String getServerData(String IMEI) {
        Map<String, String> params =new HashMap<>();
        params.put("imei",IMEI);
        return NetworkHelp.sendDataByPost2(params, "UTF-8", "http://192.168.0.169:28085/ATMCenter_test/service/bibliosMiddle/getOffLineDataSum");
    }

    @Override
    public String purchaseBookOnline(Book book, String copies, String action, String orderLibLocal) {
        Map<String, String> params =new HashMap<>();
        params.put("bookrecno",book.getBookrecno());
        params.put("isbn",book.getIsbn());
        params.put("copies",copies);
        params.put("title",book.getBookTitle());
        params.put("author",book.getAuthor());
        params.put("publisher",book.getPublisher());
        params.put("pubdate",book.getBookDate());
        params.put("classno",book.getClassNo());
        params.put("price",book.getPrice());
        params.put("action",action);
        params.put("orderLibLocal",orderLibLocal);
        return NetworkHelp.sendDataByPost2(params, "UTF-8", "http://192.168.0.169:28081/interlib3/service/purchase/insBibliosAndDirectOrder");
    }


}
