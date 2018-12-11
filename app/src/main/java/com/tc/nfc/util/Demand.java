package com.tc.nfc.util;

import android.util.Log;

/**
 * Created by xiansize on 2017/4/23.每个图书馆的具体需求
 */
public class Demand {

    /**
     * 九龙坡图书馆将得到的书架号转换为中文显示
     * @param barcode
     * @return
     */
    public static String DisplayShelfNumberJL(String barcode){
        StringBuffer sBuffer = new StringBuffer();

        if(barcode.equals("") || barcode == null){
            return "";
        }
        try{
            //馆藏地点
            String address = barcode.substring(0,4);
            switch (address){
                case "MAHF":    sBuffer.append("民安华福社区");
                    break;
                case "BGCG":    sBuffer.append("巴国城");
                    break;
                case "JLPG":    sBuffer.append("九龙坡区");
                    break;
                case "SRFG":    sBuffer.append("科园四路少儿分馆");
                    break;
                default:        sBuffer.append("无此馆藏");
                    break;

            }

            //成人区OR少儿区
            String area = barcode.substring(6,8);
            if("SE".equals(area)){
                sBuffer.append("少儿区");
            }else{
                sBuffer.append("成人区");
            }
            //列
            String column = barcode.substring(9,12);
            sBuffer.append(column + "列");

            //面
            String surface = barcode.substring(12,13);
            sBuffer.append(surface + "面");

            //架
            String shelf = barcode.substring(13,15);
            sBuffer.append(shelf + "架");

            // 层
            String layer = barcode.substring(15,17);
            sBuffer.append(layer + "层");


        }catch (IndexOutOfBoundsException e){

            Log.d("test","数据长度有误:"+e);

        }

        return sBuffer.toString();
    }


    /**
     * 洛阳少图需求，通过barcode前面的三位数来判断在哪个科室，通过扫书和扫书架的形式判断当前扫到的是书还是书架号
     * @param shelvNumBarcode
     * @return
     */
    public static String judgeNumToKnow(String shelvNumBarcode){
        String roomBarcode = shelvNumBarcode.substring(0,3);
        String shelfNumber = "";

        switch (roomBarcode) {
            case "501":shelfNumber = shelvNumBarcode+"社科";
                break;
            case "502":shelfNumber = shelvNumBarcode+"绘本";
                break ;
            case "503":shelfNumber = shelvNumBarcode+"自然";
                break;
        }

        return shelfNumber;
    }


}
