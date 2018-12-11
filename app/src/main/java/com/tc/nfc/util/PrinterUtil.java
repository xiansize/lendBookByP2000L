package com.tc.nfc.util;

import com.basewin.aidl.OnPrinterListener;
import com.basewin.define.FontsType;
import com.basewin.services.ServiceManager;
import com.tc.nfc.model.ReturnBookResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by xiansize on 2017/5/16.
 */
public class PrinterUtil {

    /**
     * 打印借还书凭证
     * @param loanData
     * @param flag
     */
    public static void  printer(List<Map<String, Object>> loanData, List<ReturnBookResult> reloanList, String flag) {

        try {
            JSONArray jsonArray = new JSONArray();

            //空一行
            JSONObject jsonEmpty = new JSONObject();
            jsonEmpty.put("content-type","txt");
            jsonEmpty.put("size","2");
            jsonEmpty.put("content","                 ");
            jsonEmpty.put("position","center");
            jsonEmpty.put("Bold","1");
            // 添加文本打印,正常

            // add text printer
            JSONObject jsonTitle = new JSONObject();//数组里面的json
            jsonTitle.put("content-type","txt");
            jsonTitle.put("size","3");
            if("LoanActivity".equals(flag)){
                jsonTitle.put("content","***图书馆借书凭证***");
            }else if("ReturnActivity".equals(flag)){
                jsonTitle.put("content","***图书馆还书凭证***");
            }else{
                jsonTitle.put("content","***图书馆续借凭证***");
            }
            jsonTitle.put("position","center");
            jsonTitle.put("Bold","1");


            JSONObject jsonReaderID = new JSONObject();
            jsonReaderID.put("content-type","txt");
            jsonReaderID.put("size","30");
            jsonReaderID.put("content","读者账号:"+Constant.readerId);
            jsonReaderID.put("position","left");
            jsonReaderID.put("Bold","1");

            JSONObject jsonReaderName = new JSONObject();
            jsonReaderName.put("content-type","txt");
            jsonReaderName.put("size","30");
            jsonReaderName.put("content", "读者姓名:"+Constant.readerName);
            jsonReaderName.put("position","left");
            jsonReaderName.put("Bold","1");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            JSONObject jsonLoanData = new JSONObject();
            jsonLoanData.put("content-type","txt");
            jsonLoanData.put("size","30");
            jsonLoanData.put("content", "打印日期:"+sdf.format(new Date()));
            jsonLoanData.put("position","left");
            jsonLoanData.put("Bold","1");


            JSONObject jsonLine  = new JSONObject();
            jsonLine.put("content-type","txt");
            jsonLine.put("size","32");
            if("LoanActivity".equals(flag)){
                jsonLine.put("content", "--------借书清单--------");
            }else if("ReturnActivity".equals(flag)){
                jsonLine.put("content", "--------还书清单--------");
            }else{
                jsonLine.put("content", "--------续借清单--------");
            }
            jsonLine.put("position","center");
            jsonLine.put("Bold","1");


            ServiceManager.getInstence().getPrinter().setPrintGray(2000);
            ServiceManager.getInstence().getPrinter().setPrintFont(FontsType.simsun);

            jsonArray.put(jsonTitle);
            jsonArray.put(jsonEmpty);
            jsonArray.put(jsonReaderID);
            jsonArray.put(jsonReaderName);
            jsonArray.put(jsonLoanData);
            jsonArray.put(jsonEmpty);
            jsonArray.put(jsonLine);
            jsonArray.put(jsonEmpty);


            int num = 0;//操作的书的数量
            if("ReLoanResultActivity".equals(flag)){

                for(ReturnBookResult returnBookResult: reloanList){

                    JSONObject jsonBook = new JSONObject();
                    jsonBook.put("content-type", "txt");
                    jsonBook.put("size", "2");
                    jsonBook.put("content", "书名:" + returnBookResult.getBook().getBookTitle());
                    jsonBook.put("position", "left");
                    jsonBook.put("Bold", "1");

                    JSONObject jsonLoadTime = new JSONObject();
                    jsonLoadTime.put("content-type", "txt");
                    jsonLoadTime.put("size", "2");
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                    jsonLoadTime.put("content", "续借日期:" + sdf1.format(new Date()));
                    jsonLoadTime.put("position", "left");
                    jsonLoadTime.put("Bold", "1");

                    JSONObject jsonNeedRetureTime = new JSONObject();
                    jsonNeedRetureTime.put("content-type", "txt");
                    jsonNeedRetureTime.put("size", "2");
                    jsonNeedRetureTime.put("content", "须还日期:" + returnBookResult.getBook().getReturnDate());
                    jsonNeedRetureTime.put("position", "left");
                    jsonNeedRetureTime.put("Bold", "1");

                    jsonArray.put(jsonBook);
                    jsonArray.put(jsonLoadTime);
                    jsonArray.put(jsonNeedRetureTime);
                    jsonArray.put(jsonEmpty);

                    num++;
                }

            }else {

                for (Map<String, Object> map : loanData) {

                    if (map.get("LoanResult").equals(true)) {

                        JSONObject jsonBook = new JSONObject();
                        jsonBook.put("content-type", "txt");
                        jsonBook.put("size", "2");
                        jsonBook.put("content", "书名:" + map.get("title"));
                        jsonBook.put("position", "left");
                        jsonBook.put("Bold", "1");

                        JSONObject jsonLoadTime = new JSONObject();
                        jsonLoadTime.put("content-type", "txt");
                        jsonLoadTime.put("size", "2");
                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                        if ("LoanActivity".equals(flag)) {
                            jsonLoadTime.put("content", "借书日期:" + sdf1.format(new Date()));
                        } else {
                            jsonLoadTime.put("content", "还书日期:" + sdf1.format(new Date()));
                        }
                        jsonLoadTime.put("position", "left");
                        jsonLoadTime.put("Bold", "1");

                        JSONObject jsonNeedRetureTime = new JSONObject();
                        jsonNeedRetureTime.put("content-type", "txt");
                        jsonNeedRetureTime.put("size", "2");
                        jsonNeedRetureTime.put("content", "须还日期:" + map.get("returnDate"));
                        jsonNeedRetureTime.put("position", "left");
                        jsonNeedRetureTime.put("Bold", "1");

                        jsonArray.put(jsonBook);
                        jsonArray.put(jsonLoadTime);
                        jsonArray.put(jsonNeedRetureTime);
                        jsonArray.put(jsonEmpty);

                        num++;
                    }
                }
            }

            JSONObject jsonNum = new JSONObject();
            jsonNum.put("content-type","txt");
            jsonNum.put("size","28");
            if("LoanActivity".equals(flag)){
                jsonNum.put("content", "共借"+"["+num+"]"+"本书");
            }else if("ReturnActivity".equals(flag)){
                jsonNum.put("content", "共还"+"["+num+"]"+"本书");
            }else{
                jsonNum.put("content", "共续借"+"["+num+"]"+"本书");
            }
            jsonNum.put("position","right");
            jsonNum.put("Bold","1");

            jsonArray.put(jsonNum);
            jsonArray.put(jsonEmpty);

            JSONObject printJson = new JSONObject();

            printJson.put("spos", jsonArray);

            // 设置底部空5行
            // Set at the bottom of the empty 5 rows
            ServiceManager.getInstence().getPrinter().printBottomFeedLine(5);

            ServiceManager.getInstence().getPrinter().print(printJson.toString(), null, new OnPrinterListener() {
                @Override
                public void onError(int i, String s) {

                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onStart() {

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
