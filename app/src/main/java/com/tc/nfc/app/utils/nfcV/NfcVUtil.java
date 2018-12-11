package com.tc.nfc.app.utils.nfcV;

import java.io.IOException;
import android.content.Intent;
import android.nfc.tech.NfcV;
import android.util.Log;

import com.basewin.utils.BCDHelper;
import com.pos.sdk.card.PosCardManager;
import com.pos.sdk.utils.PosByteArray;
import com.tc.nfc.app.utils.StringToOther;

/**
 * NfcV类型条码解析
 */
public class NfcVUtil {

	public static NfcV mNfcv;
	private final static String LOG_TAG = "NfcVUtil";
	private static String oldBarcode = "";// �ɵ�����
	private static String oldUid = "";// �ɵ�UID
    private static String p2000LOldBarcode = "";//暂时存放读到的旧的barcode
    private static boolean readCardRun = true;

    public static void setP2000LOldBarcode(String p2000LOldBarcode) {
        NfcVUtil.p2000LOldBarcode = p2000LOldBarcode;
    }

    public static boolean isReadCardRun() {
        return readCardRun;
    }

    public static void setReadCardRun(boolean readCardRun) {
        NfcVUtil.readCardRun = readCardRun;
    }

    // byte[]ת 16�����ַ�
	public static String byte2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

	/**
	 * 解析条码
	 * @return
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 */
	public static String checkBarcode() throws IOException, IndexOutOfBoundsException {
		// Log.e("exe:", "return:"+mNfcv.isConnected());
		boolean isConnect = mNfcv.isConnected();
		// mNfcv.connect().
		byte[] UID = mNfcv.getTag().getId();
		Log.d("test","NfcV:"+"|" + isConnect + "|checkBarcode():" +oldBarcode + "|" + oldUid + "|" + UID.toString());
		if (oldBarcode != null && !oldBarcode.equals("") && oldUid.equals(UID.toString())) {
			if (isConnect) {
				return oldBarcode;
			} else {
				oldBarcode = "";
				return "";
			}
		}
		oldUid = UID.toString();
		if (oldUid == null || oldUid.equals("")) {
			return "";
		}

		int length = UID.length;
		byte[] UID_zan = new byte[8];
		byte[] uidShow = new byte[8];
		for (int i = 0; i < length; i++) {
			UID_zan[i] = UID[i];
		}
		for (int i = 0; i < length; i++) {
			uidShow[length - i - 1] = UID_zan[i];
		}

		/**
		 * 4个byte[]，分别存储前4个扇区读到的内容
		 */
		byte[] responsebyte0 = null;
		byte[] responsebyte1 = null;
		byte[] responsebyte2 = null;
		byte[] responsebyte3 = null;

		byte cmd0[] = new byte[11];
		cmd0[0] = (byte) 0x20;
		cmd0[1] = (byte) 0x20;
		System.arraycopy(UID, 0, cmd0, 2, UID.length); // UID
		cmd0[10] = (byte) 0x00;

		byte cmd1[] = new byte[11];
		cmd1[0] = (byte) 0x20;
		cmd1[1] = (byte) 0x20;
		System.arraycopy(UID, 0, cmd1, 2, UID.length); // UID
		cmd1[10] = (byte) 0x01;

		byte cmd2[] = new byte[11];
		cmd2[0] = (byte) 0x20;
		cmd2[1] = (byte) 0x20;
		System.arraycopy(UID, 0, cmd2, 2, UID.length); // UID
		cmd2[10] = (byte) 0x02;

		//广图增加
		byte cmd3[] = new byte[11];
		cmd3[0] = (byte) 0x20;
		cmd3[1] = (byte) 0x20;
		System.arraycopy(UID, 0, cmd3, 2, UID.length); // UID cmd3[10]
		cmd3[10]= (byte) 0x03;

		if (!mNfcv.isConnected()) {
			try {
				mNfcv.close();
				mNfcv.connect();
			} catch (Exception ex) {

				return "";
			}
		}
		/**
		 * 读取
		 */
		responsebyte0 = mNfcv.transceive(cmd0);
		responsebyte1 = mNfcv.transceive(cmd1);
		responsebyte2 = mNfcv.transceive(cmd2);
		//广图增加
		responsebyte3 = mNfcv.transceive(cmd3);


		// ����һ��Ļ�ȡ��ֵ������10λ�Ļ���
		// ��һ��ĵ�0λ��ֵΪѹ����ʽ����(2,1,4),
		// 2:���ִ�,�����������������д�뵽RFID��ǩ��
		// 1:���������������ǰ���ݱ����ʮ�������ݺ���д�뵽RFID��ǩ��
		// 4:��ĸ������
		// ��һ��ĵ�1λ��ֵ:�Ƿ�д����
		// ��һ���2��3λ��ֵ����ʾ���볤�ȣ���ݳ���λ����ٸ��ֽڱ����������
		// ��һ��ĵ�0λ��ֵΪѹ����ʽ����(2,1,4)��ʾ,ѹ��ģʽ ��һ��ĵ�0λ��ֵΪѹ����ʽ����(2,1,4)
		String sign = "1";
		// byte flag =responsebyte0[2];
		// ��ʾ���볤��
		int len = 0;
		String lenStr = "";
		String data = "";
		String tmp;
		String tmp3 = "";

		boolean isGt = false;

		if (responsebyte0 != null) {
			tmp = byte2HexStr(responsebyte0).substring(2);
			Log.d("test","NfcV"+"扇区0内容:"+byte2HexStr(responsebyte0));
			Log.d("test","NfcV"+"扇区0截取后内容:"+tmp);
			data += tmp;


			sign = tmp.substring(0, 1);
			lenStr = tmp.substring(2, 4);
			try {
				len = Integer.parseInt(lenStr) * 2 + 4;
			} catch (Exception ex) {
				len = 0;
			}
		}
		if (responsebyte1 != null) {
			tmp = byte2HexStr(responsebyte1).substring(2);
			Log.d("test","NfcV"+"扇区1内容:"+byte2HexStr(responsebyte1));
			Log.d("test","NfcV"+"扇区1截取后内容:" + tmp);
			data += tmp;
		}
		if (responsebyte2 != null) {
			try {
				tmp = byte2HexStr(responsebyte2).substring(2, 5);

				tmp3 = byte2HexStr(responsebyte2).substring(2);
				Log.d("test","NfcV"+"扇区2内容:" + byte2HexStr(responsebyte2));
				Log.d("test","NfcV"+"扇区2截取后内容:"+tmp3);
			}catch (StringIndexOutOfBoundsException e){
				Log.d("test","NfcV"+"StringIndexOutOfBoundsException");
				e.printStackTrace();
			}
			/*
			 * if(!isGt) data += tmp; else
			 */
			data += tmp3;
		}
		if (responsebyte3 != null) {

			tmp = byte2HexStr(responsebyte3).substring(2);
			Log.d("test","NfcV"+"扇区3内容:"+byte2HexStr(responsebyte3));
			Log.d("test","NfcV"+"扇区3截取后内容:"+tmp);

			/*
			 * if(!isGt) data += tmp; else
			 */
			data += tmp;
		}
		/*
		 * if(isGt){ if(data.length()>len){ data = data.substring(4,len);
		 * if(sign.equals("2")){ data =data.replace("F", "");//������һλΪF����ȥ��
		 * }else if(sign.equals("1")){//�����ʮ�����д��ȥ��Ҫת����ʮ���� data
		 * =""+Integer.parseInt(data,16); } } }else{ if(data.length()>6) data
		 * =data.substring(6); }
		 */
		// System.out.println(data);
		// ExCheBarcode exChbar = new ExCheBarcode(data);
		// System.out.println(exChbar.getEncodeBarcode());

		Log.d("test","NfcV"+"整段内容: "+data);
		boolean isLoanSuc = ExCheBarcode.loadRule(data);
		if (!isLoanSuc) {
			oldBarcode = "";
			return "";
		}
		oldBarcode = ExCheBarcode.getEncodeBarcode();
		Log.d("test","oldbarcode："+oldBarcode);
		data = ExCheBarcode.getEncodeBarcode();
		Log.d("test","NfcV"+"最后解析的条码:"+data);


		// mNfcv.close();
		return data;
	}

	public static String readBarcode(Intent intent) throws IOException, IndexOutOfBoundsException {
		// Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		//
		// mNfcv = NfcV.get(tagFromIntent);
		byte[] UID = mNfcv.getTag().getId();

		Log.e("readBarcode:", "" + UID.toString());
		int length = UID.length;
		byte[] UID_zan = new byte[8];
		byte[] uidShow = new byte[8];
		for (int i = 0; i < length; i++) {
			UID_zan[i] = UID[i];
		}
		for (int i = 0; i < length; i++) {
			uidShow[length - i - 1] = UID_zan[i];
		}

		byte[] responsebyte0 = null; // ����ֵ
		byte[] responsebyte1 = null; // ����ֵ
		byte[] responsebyte2 = null; // ����ֵ

		byte cmd0[] = new byte[11];
		cmd0[0] = (byte) 0x20;
		cmd0[1] = (byte) 0x20;
		System.arraycopy(UID, 0, cmd0, 2, UID.length); // UID
		cmd0[10] = (byte) 0x00;

		byte cmd1[] = new byte[11];
		cmd1[0] = (byte) 0x20;
		cmd1[1] = (byte) 0x20;
		System.arraycopy(UID, 0, cmd1, 2, UID.length); // UID
		cmd1[10] = (byte) 0x01;

		byte cmd2[] = new byte[11];
		cmd2[0] = (byte) 0x20;
		cmd2[1] = (byte) 0x20;
		System.arraycopy(UID, 0, cmd2, 2, UID.length); // UID
		cmd2[10] = (byte) 0x02;

		if (!mNfcv.isConnected()) {
			mNfcv.connect();
		}
		responsebyte0 = mNfcv.transceive(cmd0);
		responsebyte1 = mNfcv.transceive(cmd1);
		responsebyte2 = mNfcv.transceive(cmd2);

		String data = "";
		String tmp;
		if (responsebyte0 != null) {
			tmp = byte2HexStr(responsebyte0).substring(8);
			data += tmp;
		}
		if (responsebyte1 != null) {
			tmp = byte2HexStr(responsebyte1).substring(2);
			data += tmp;
		}
		if (responsebyte2 != null) {
			tmp = byte2HexStr(responsebyte2).substring(2, 5);
			data += tmp;
		}
		// mNfcv.close();
		return data;
	}


	/**
	 * 更改标签里的安全位
	 * @param flag
	 * @return
	 * @throws IOException
	 */
	public static Boolean writeAFI(boolean flag) throws IOException {
		// TODO Auto-generated method stub

		byte[] UID = mNfcv.getTag().getId();

        Log.d("test","UID.length:"+UID.length+" | "+ "UID:"+UID+" | "+ "UIDString:"+StringToOther.bcdToString(UID));

		final byte cmdAFI[] = new byte[11];
		cmdAFI[0] = (byte) 0x20;
		cmdAFI[1] = (byte) 0x27;
		System.arraycopy(UID, 0, cmdAFI, 2, UID.length); // UID
		cmdAFI[10] = flag ? (byte) 0xc2 : (byte) 0x07;

		/*
		 * �㷴�� // ����EAS 0xA2�ܳ��� 0xA3���ܳ��� byte cmdSetEAS[] = new byte[11];
		 * cmdSetEAS[0] = (byte) 0x22; cmdSetEAS[1] = flag ? (byte)0xA2 :
		 * (byte)0xA3; cmdSetEAS[2]=(byte)0x04; System.arraycopy(UID, 0,
		 * cmdSetEAS, 3, UID.length); //UID
		 */

		// ����EAS 0xA3�ܳ��� 0xA2���ܳ���
		final byte cmdSetEAS[] = new byte[11];
		cmdSetEAS[0] = (byte) 0x22;
		cmdSetEAS[1] = flag ? (byte) 0xA3 : (byte) 0xA2;
		cmdSetEAS[2] = (byte) 0x04;
		System.arraycopy(UID, 0, cmdSetEAS, 3, UID.length);

		if (!mNfcv.isConnected()) {
			mNfcv.connect();
		}

		byte[] rsp = mNfcv.transceive(cmdAFI);
		byte[] rsp1 = mNfcv.transceive(cmdSetEAS);

		if (rsp[0] == 0x00 && rsp1[0] == 0x00) {
			return true;
		}
		return false;
	}


    /**
     * 更改P2000L标签里的安全位
     * @param flag
     * @return
     * @throws IOException
     */
	public static boolean p2000LWriteAFI(boolean flag) throws IOException{
        int ret = 0;
        PosCardManager mPosCardManager = PosCardManager.getDefault();
        PosByteArray b_data0 = new PosByteArray();
        ret = mPosCardManager.open(PosCardManager.POSCARD_READER_CATEGORY_PICC,  null);
        Log.d("json","open:"+ret);

        ret = mPosCardManager.ViccCardTransmitCmd(0x05, new byte[]{0x02, 0x01}, b_data0);
        Log.d("json","ViccCardTransmitCmd:" + ret);

        if (ret != 0) {
            mPosCardManager.close();
            Log.d("json","ViccCardTransmitCmd:error" );

        } else {
            String incer = StringToOther.bcdToString(b_data0.buffer).substring(4,StringToOther.bcdToString(b_data0.buffer).length());
            byte[] sneddata = new byte[]{0x20, 0x25};
            byte[] sendata = null;
            sendata = StringToOther.byteMerger(sneddata, BCDHelper.StrToBCD(incer));
            Log.d("json","b_data0:" +StringToOther.bcdToString(sendata));

            //Select
            ret = mPosCardManager.ViccCardTransmitCmd(0x05,sendata, b_data0);
            Log.d("json" ,"Select:"+ret);

			if(ret == 0){
                //writeAFI
                int afiRet;
                final byte cmdSetAFI[] = new byte[3];
                cmdSetAFI[0] = (byte) 0x10;
                cmdSetAFI[1] = (byte) 0x27;
                cmdSetAFI[2] = flag ? (byte) 0xc2 : (byte) 0x07;

                afiRet = mPosCardManager.ViccCardTransmitCmd(0x05,cmdSetAFI, b_data0);
                Log.d("json","AFIViccCardTransmitCmd:" + afiRet);

                //writeEAS
                int easRet;
                final byte cmdSetEAS[] = new byte[3];
                cmdSetEAS[0] = (byte) 0x10;
                cmdSetEAS[1] = flag ? (byte) 0xA3 : (byte) 0xA2;
                cmdSetEAS[2] = (byte) 0x04;

                easRet = mPosCardManager.ViccCardTransmitCmd(0x05,cmdSetEAS, b_data0);
                Log.d("json","EASViccCardTransmitCmd:" + easRet);

                //停止运行卡片
                byte[] quiet = new byte[]{0x20, 0x02};
                byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                ret = mPosCardManager.ViccCardTransmitCmd(0x05,quietdata, b_data0);
                Log.d("json","stopRun:" + ret);
                mPosCardManager.close();

                //判断是否修改成功
                if(afiRet == 0 && easRet == 0){
                    return true;
                }else{
                    return false;
                }


            }else{

                //停止运行卡片
                byte[] quiet = new byte[]{0x20, 0x02};
                byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                ret = mPosCardManager.ViccCardTransmitCmd(0x05,quietdata, b_data0);
                Log.d("json","stopRun:" + ret);
                mPosCardManager.close();

                //判断是否修改成功
                return false;

            }


        }
        return false;
    }


	public static int bytesToInt(byte[] bytes) {

		int addr = bytes[0] & 0xFF;

		addr |= ((bytes[1] << 8) & 0xFF00);

		addr |= ((bytes[2] << 16) & 0xFF0000);

		addr |= ((bytes[3] << 24) & 0xFF000000);

		return addr;

	}

	/**
	 * 判断输入的字符串参数是否为空
	 *
	 * @return boolean 空则返回true,非空则flase
	 */
	public static boolean isEmpty(String input) {
		return null == input || 0 == input.length() || 0 == input.replaceAll("\\s", "").length();
	}


	public static void main(String[] args) {
		byte[] test = "21070100101784943F0".getBytes();
		byte[] lenBt = new byte[2];
		for (int i = 0; i < 2; i++) {
			lenBt[i] = test[i];
		}
		int len = bytesToInt(lenBt);
		System.out.println("len:" + len);
	}

    //p2000l设备读卡
	public static String p2000LGetBarcode(){

        String barcode = "";
        int ret = 0;

        while (readCardRun) {
            try {
                PosCardManager mPosCardManager = PosCardManager.getDefault();
                PosByteArray b_data0 = new PosByteArray();
                ret = mPosCardManager.open(PosCardManager.POSCARD_READER_CATEGORY_PICC, null);
                Log.d("test3", "open:" + ret);

                ret = mPosCardManager.ViccCardTransmitCmd(0x05, new byte[]{0x02, 0x01}, b_data0);
                Log.d("test3", "ViccCardTransmitCmd:" + ret);
                if (ret != 0) {
                    mPosCardManager.close();
                    Log.d("test3", "ViccCardTransmitCmd:error  AND  close");

                } else {
                    String incer = StringToOther.bcdToString(b_data0.buffer).substring(4, StringToOther.bcdToString(b_data0.buffer).length());
                    byte[] sneddata = new byte[]{0x20, 0x25};
                    byte[] sendata = null;
                    sendata = StringToOther.byteMerger(sneddata, BCDHelper.StrToBCD(incer));
                    Log.d("test3", "b_data0:" + StringToOther.bcdToString(sendata));

                    //Select
                    ret = mPosCardManager.ViccCardTransmitCmd(0x05, sendata, b_data0);
                    Log.d("test3", "Select:" + ret);

					if(ret == 0){

                        //读取前四个扇区数据
                        ret = mPosCardManager.ViccCardTransmitCmd(0x05, new byte[]{0x10, 0x23, 0x00, 0x03}, b_data0);
                        Log.d("test3", "ReadCardBlock:" + ret);

                        if(ret == 0 && b_data0.buffer != null) {
                            //得到barcdoe号
                            barcode = StringToOther.bcdToString(b_data0.buffer).substring(2);

                            //停止运行卡片
                            byte[] quiet = new byte[]{0x20, 0x02};
                            byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                            ret = mPosCardManager.ViccCardTransmitCmd(0x05, quietdata, b_data0);
                            Log.d("test3", "stopRun:" + ret);
                            mPosCardManager.close();

                            Log.d("test3", "barcode:" + barcode);
                            boolean isLoanSuc = ExCheBarcode.loadRule(barcode);
                            if (!isLoanSuc) {
                                Log.d("test3", "失败转换barcode数据");
                            } else {
                                barcode = ExCheBarcode.getEncodeBarcode();
                                Log.d("test3", "barcode:" + barcode);
                            }

                            if (!barcode.equals(p2000LOldBarcode)) {
                                p2000LOldBarcode = barcode;
                                mPosCardManager.close();
                                return barcode;
                            }

                        }else{

                            //停止运行卡片
                            byte[] quiet = new byte[]{0x20, 0x02};
                            byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                            ret = mPosCardManager.ViccCardTransmitCmd(0x05, quietdata, b_data0);
                            Log.d("test3", "stopRun:" + ret);
                            mPosCardManager.close();

                        }

                    }else{

                        //停止运行卡片
                        byte[] quiet = new byte[]{0x20, 0x02};
                        byte[] quietdata = StringToOther.byteMerger(quiet, BCDHelper.StrToBCD(incer));
                        ret = mPosCardManager.ViccCardTransmitCmd(0x05, quietdata, b_data0);
                        Log.d("test3", "stopRun:" + ret);
                        mPosCardManager.close();

                    }

                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return "";
    }
}