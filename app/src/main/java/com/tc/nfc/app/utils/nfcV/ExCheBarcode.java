package com.tc.nfc.app.utils.nfcV;

import android.util.Log;

import com.tc.nfc.app.utils.StringToOther;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExCheBarcode {

	private static String barStr = "";
	// 条码类型标识
	private static char sign = '0';
	private static char barSign = '0';
	// 条码起始位置
	private static int startPosition = 0;
	// 条码长度
	private static int barLen = 0;
	// 结果条码
	private static String enCodeBar = "";
	//扇区重新排序的值
	private static String endSecDex = "";
		
	// private static StringToOther strToHex = null;
	// 编码方式数字 0,数字加字母 1，不变 2,ACSII:3（1 9 A 2 数字 4 3 B C 字母加数字 , 6 E ACSII, D E 7
	// 5不变）
	// 编码方式 数字十六进制转成十进制:0,数字不变:1,字母加数字 的ACSII：2，
	private static String enCodeType = "";

	public ExCheBarcode(String barcode) {
		// strToHex = new StringToOther();
		barStr = barcode;
		if (barcode == null || barcode.length() < 5) {
			return;
		}
		char[] barArr = barcode.toCharArray();
		sign = barArr[0];
		setBarcodeType(sign);// 设置压缩类型
		int signNum = Integer.parseInt(barArr[0] + "", 16);
        Log.d("test1","开始的位置："+signNum);


		String lenCode = "";
		if (signNum < 8) {// 如果小于8 从第二个字符取条码
			startPosition = 4;
			System.out.println(barArr[2] + "|" + barArr[3] + "");
			lenCode = barArr[3] + "";
			barLen = Integer.parseInt(lenCode);
            Log.d("test1","短度："+barLen);

		} else {
			startPosition = 6;
			lenCode = barArr[4] + "" + barArr[5] + "";
			barLen = Integer.parseInt(lenCode);
            Log.d("test1","长度："+barLen);
		}
		// 不解释直接返回
		if (barLen == 0) {
			enCodeBar = barcode;
			return;
		}
		barLen = barLen * 2;
		if (barLen > (barcode.length() - startPosition)) {
			barLen = (barcode.length() - startPosition);
		}
		if(startPosition ==0 || barLen== 0){
			enCodeBar = getBarcodes(barcode, false);
			endSecDex = getBarcodes(barcode, true);
			enCodeType = "3";
		}else{
		enCodeBar = barcode.substring(startPosition, startPosition + barLen);
		}
		/*
		 * switch(sign){ case '1':startPosition =4;barLen = getBarLen(barArr);
		 * break; case '2':startPosition =4;barLen = 13; break; case
		 * '4':startPosition =4;barLen = 13; break; case '6':startPosition
		 * =4;barLen = 16; break; case 'A':startPosition =7;barLen = 13; break;
		 * case 'E':startPosition =7;barLen = 8; break;
		 * 
		 * 
		 * 
		 * }
		 */
	}


    /**
     * 条码解析规则
     * @param barcode
     * @return
     */
    public static boolean loadRule(String barcode) {
		// strToHex = new StringToOther();
		barStr = barcode;
        Log.d("test1","原始："+barStr);

		if (barcode == null || barcode.length() < 5) {
			return false;
		}
		char[] barArr = barcode.toCharArray();
		sign = barArr[0];

		setBarcodeType(sign);// 设置压缩类型

		int signNum = Integer.parseInt(barArr[0] + "", 16);
		String lenCode = "";
		try {

			if (signNum < 8) {// 如果小于8 从第二个字符取条码
				startPosition = 4;
				if(signNum==0){
					startPosition=0;
				}

				System.out.println(barArr[2] + "|" + barArr[3] + "");

				lenCode = barArr[3] + "";
				Log.d("test1","startPosition"+startPosition);

				if (Character.isDigit(barArr[3])) {
					barLen = Integer.parseInt(lenCode);
				} else if (Character.isLetter(barArr[3])) {
					barLen = Integer.parseInt(lenCode, 16);
//					if (barLen==14){
//							barLen=13;
//					}
				} else {
					barLen = 0;
				}

			} else {
				startPosition = 6;
				lenCode = barArr[4] + ""+barArr[5] + "";
                barLen = Integer.parseInt(lenCode, 16);
                Log.d("test1","长度："+barLen);

			}
		} catch (Exception ex) {
			barLen = 0;
		}
		// 不解析直接返回
		if (barLen < 0) {
			enCodeBar = barcode;
			return false;
		}
            barLen = barLen * 2;


		if (barLen > (barcode.length() - startPosition)) {
			barLen = (barcode.length() - startPosition);

		}

		if(startPosition ==0 || barLen== 0){
			enCodeBar = getBarcodes(barcode, false);
			endSecDex = getBarcodes(barcode, true);
			enCodeType = "3";
		}else{
			enCodeBar = barcode.substring(startPosition, startPosition + barLen);
		}

		return true;

	}

	// 获取条码的长度
	private int getBarLen(char[] arr) {
		int signNum = Integer.parseInt(arr[0] + "", 16);
		if (signNum < 8) {
			return Integer.parseInt(arr[2] + arr[3] + "", 16);
		} else {
			return Integer.parseInt(arr[4] + arr[5] + "", 16);
		}
	}

	// 获取条码号
	public String getBarcode() {
		return enCodeBar;
	}

	// 获取已经解码的条码
	public static String getEncodeBarcode() {

		if(enCodeType.equals("4")){
            return biaryToCutSixAndAddNum(hexString2binaryString(enCodeBar));//九龙坡图书馆需求
        }else if(enCodeType.equals("3")){
			return getEncodeBarcode(endSecDex, enCodeBar);
		}
		else if (enCodeType.equals("2")) {
            return StringToOther.convertHexToString(enCodeBar);
		} else if (enCodeType.equals("1")) {
			return StringToOther.getHexToLong(enCodeBar);
		} else {
			char[] arrays = enCodeBar.toCharArray();
			if (!isNumeric(arrays[arrays.length - 1] + "")) {
				// arrays
				enCodeBar = enCodeBar.substring(0, arrays.length - 1);
			}

			return enCodeBar;
		}
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	// 设备压缩类型0：数字不变， 1：数字十六进制转成十进制， 2：字母加数字 ,ACSII
	private static void setBarcodeType(char sign) {
		switch (sign) {
		case '1':
			enCodeType = "1";
			break;
		case '2':
			enCodeType = "0";
			break;
		case '3':
			enCodeType = "2";
			break;
		case '4':
			enCodeType = "4";
			break;
		case '5':
			enCodeType = "0";
			break;
		case '6':
			enCodeType = "2";
			break;
		case '7':
			enCodeType = "0";
			break;
		case '8':
			enCodeType = "0";
			break;
		case '9':
			enCodeType = "1";
			break;
		case 'A':
			enCodeType = "0";
			break;
		case 'B':
			enCodeType = "2";
			break;
		case 'C':
			enCodeType = "2";
			break;
		case 'D':
			enCodeType = "0";
			break;
		case 'E':
			enCodeType = "2";
			break;
		default:
			enCodeType = "0";
			break;
		}
	}
	
	//对扇区里的值 按深圳图书进行编码处理
	public static  String getBarcodes(String orgBarcode,boolean isRetAll){
		char[] arrs =orgBarcode.toCharArray();
		//重新排序字符串
		StringBuffer strBuf = new StringBuffer();
		StringBuffer barBuf = new StringBuffer();
		if(arrs.length>14){
			//重组后的组：
		strBuf.append(arrs[6]).append(arrs[7]).append(arrs[4]).append(arrs[5])
		.append(arrs[2]).append(arrs[3]).append(arrs[0]).append(arrs[1])
		.append(arrs[14]).append(arrs[15]).append(arrs[12]).append(arrs[13])
		.append(arrs[10]).append(arrs[11]).append(arrs[8]).append(arrs[9]);
		//条码的值：
		barBuf
		.append(arrs[2]).append(arrs[3]).append(arrs[0]).append(arrs[1])
		.append(arrs[14]).append(arrs[15]).append(arrs[12]).append(arrs[13])
		.append(arrs[10]).append(arrs[11]).append(arrs[8]).append(arrs[9]);
		}
		 if(isRetAll)
			 return strBuf.toString();
		return barBuf.toString();
	}

	//获取最终的条码值
	public static String getEncodeBarcode(String newSecDex,String newBarcodeDex)throws StringIndexOutOfBoundsException{
		String newTmpToBin = hexString2binaryString(newSecDex);
		String bartoBin = hexString2binaryString(newBarcodeDex);
		bartoBin =newTmpToBin.substring(12,16) +""+bartoBin;
		BigInteger newBarcInt = new BigInteger(bartoBin, 2); 
		String newBarcStr =""+newBarcInt;
		newTmpToBin.substring(0,4);
		String typeSign = newTmpToBin.substring(4,7);
		String sufBar = newTmpToBin.substring(7, 12);
		BigInteger suf = new BigInteger(sufBar, 2);
		newBarcStr =suf+""+newBarcStr;
		return newBarcStr;
	}

	/**十六进制转换成二制的字符类型*/
	public static String hexString2binaryString(String hexString)
	{
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++)
		{
			tmp = "0000"
					+ Integer.toBinaryString(Integer.parseInt(hexString
							.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}

	/**
	 * 去掉每个八位的前两位,最后得到一个新的十六进制*/
	public static String binaryStringToCutTwoBit(String binaryString){

        StringBuilder stringBuilder = new StringBuilder();
        int[] intArray = new int[binaryString.length()];// 新建一个数组用来保存str每一位的数字
        for (int i = 0; i < binaryString.length(); i++) {
            // 遍历str将每个字节的后两位添加如intArray,
            if((i % 8 == 0 ) || ( (i-1) % 8 == 0 ) || i == 0 || i == 1){
                //Log.d("test","去掉的字节"+intArray[i]);
            }else{
                Character ch = binaryString.charAt(i);
                intArray[i] = Integer.parseInt(ch.toString());
                stringBuilder.append(intArray[i]);
            }
        }

        String lastNum = StringToOther.byteToHex(stringBuilder.toString());
        return lastNum;
    }

	/**将十六进制转换为二进制之后，每六位截取，前两个六位前面加01，后面的加00，最后转换为十六进制，然后ascll码*/
    public static String biaryToCutSixAndAddNum(String binaryString){

        StringBuilder stringBuilder = new StringBuilder();//放二进制


        StringBuilder stringBuilder1 = new StringBuilder();//放十六进制
        StringBuilder stringBuilder2 = new StringBuilder();//放ascll码

        //StringBuilder stringBuilder1 = new StringBuilder();


        int[] intArray = new int[binaryString.length()];// 新建一个数组用来保存str每一位的数字
        for (int i = 0; i < binaryString.length(); i++) {
            // 前两个六位前面加01，后面的加00

                Character ch = binaryString.charAt(i);
                intArray[i] = Integer.parseInt(ch.toString());
                if((i == 6) || (i == 0) ){
                    stringBuilder.append(0);
                    stringBuilder.append(1);
                }
                else if((i % 6 == 0) && (i != 6)){
                    stringBuilder.append(0);
                    stringBuilder.append(0);
                }
                stringBuilder.append(intArray[i]);

        }
        String lastNum = stringBuilder.toString();
        String hexNum = StringToOther.byteToHex(lastNum);
        String ascll = StringToOther.convertHexToString(hexNum);
        return ascll;

    }


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// ExCheBarcode exCh = new ExCheBarcode("E10108533032363335363500");
		// String encStr = exCh.getBarcode();
		// StringToOther strToOther =new StringToOther();
		// System.out.println("en:"+encStr);
		// System.out.println("getEncodeBarcode:"+exCh.getEncodeBarcode());
		// String str ="210702000888888001F";
		// char [] arrays = str.toCharArray();
		// if(!isNumeric(arrays[arrays.length]+"")){
		// System.out.println(str.substring(arrays.length-1, arrays.length));
		// }
		System.out.println(Integer.parseInt("E", 16) + "");
	}

}
