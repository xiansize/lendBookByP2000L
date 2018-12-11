package com.tc.nfc.app.utils;

import android.graphics.Bitmap;
import android.nfc.Tag;
import android.util.Base64;
import android.util.Log;

import com.tc.nfc.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringToOther {

	/**
	 * Ascii转十六进制
	 * @param str
	 * @return
	 */
	public static String convertStringToHex(String str){

		char[] chars = str.toCharArray();
		StringBuffer hex = new StringBuffer();
		for(int i = 0; i < chars.length; i++){
			hex.append(Integer.toHexString((int)chars[i]));
		}
		return hex.toString();
	}

	/**
	 * 十六进制转Ascii
	 * @param hex
	 * @return
	 */
	public static  String convertHexToString(String hex){

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		//49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for( int i=0; i<hex.length()-1; i+=2 ){

			//grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			//convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char)decimal);
			temp.append(decimal);
		}

		return sb.toString();
	}

	/*十六进去转成整数**/
	public static String getHexToLong(String str){
		return ""+Long.parseLong(str,16);
	}

	/**
	 * Ascii转十进制
	 * [仅限数字]
	 * @param str
	 * @return
	 */
	public static String convertStringToDec(String str){
		int i=0;
		String barcode="";
		while (!(i+1>str.length())) {

			String single =AsciiToDec(str.substring(i, i + 2));
			if (single==null){
				return null;
			}
			barcode+= single;
			i=i+2;
		}
		return barcode;
	}

	/**
	 * 该方法只能将数字解析
	 * 将数字的Ascii解析成十进制
	 * @param Ascii 传入的Ascii
	 * @return
	 */
	public static String AsciiToDec(String Ascii){
		int AsciiNum=0;
		try{
			AsciiNum=Integer.parseInt(Ascii);
		}catch (NumberFormatException e){
			return null;
		}

		//ascii 30 = dec 0
		int Ascii_0=30;
		//对比0的ascii码增量
		int increNum=0;
		String Dec="";
		if (AsciiNum>=30&&AsciiNum<=39){

			increNum=increNum+(AsciiNum-Ascii_0);
			Dec =String.valueOf(increNum);
		}
		return Dec;
	}

	/**二进制转换为十六进制的字符类型*/
    public static String byteToHex(String binaryString){

        HashMap map = new HashMap<String,String>();
        map.put("0000","0");
        map.put("0001","1");
        map.put("0010","2");
        map.put("0011","3");
        map.put("0100","4");
        map.put("0101","5");
        map.put("0110","6");
        map.put("0111","7");
        map.put("1000","8");
        map.put("1001","9");
        map.put("1010","A");
        map.put("1011","B");
        map.put("1100","C");
        map.put("1101","D");
        map.put("1110","E");
        map.put("1111","F");

        StringBuilder sb = new StringBuilder();

        //先补齐
        int zeroes = binaryString.length() % 4;
        for(int i = 0 ; zeroes >0 && i < 4 - zeroes ; i ++){
            binaryString = "0" + binaryString ;
        }
        //然后每四位替换

        for(int i = 0 ; i < binaryString.length() ; i += 4){
            String tempString = binaryString.substring(i, i + 4);
            sb.append(map.get(tempString));
        }

        return sb.toString();
    }

    /**将位图转换为Base64编码的形式*/
    public static String bitmap2StrByBase64(Bitmap bit){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 60, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * 将bcd码转换为string
     * @param bcdNum
     * @return
     */
    public static String bcdToString(byte[] bcdNum) {
        int len = bcdNum.length;

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            sb.append(Integer.toHexString((bcdNum[i] & 0xF0) >> 4));
            sb.append(Integer.toHexString(bcdNum[i] & 0x0F));
        }
        return sb.toString().toUpperCase();
    }

	//java 合并两个byte数组
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
		byte[] byte_3 = new byte[byte_1.length+byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}




}
