package com.tc.nfc.model;

/**
 * Created by tangjiarao on 17/1/19.
 */
public class InventoryReport
{
    private String barcode;
    private String uidStr;
    private String TagTypeStr;
    private long findCnt = 0;

    public InventoryReport()
    {
        super();
    }

    public InventoryReport(String uid, String tayType,String barcode)
    {
        super();
        this.setUidStr(uid);
        this.setTagTypeStr(tayType);
        this.setFindCnt(1);
        this.setBarcode(barcode);
    }

    public String getUidStr()
    {
        return uidStr;
    }

    public void setUidStr(String uidStr)
    {
        this.uidStr = uidStr;
    }

    public String getTagTypeStr()
    {
        return TagTypeStr;
    }

    public void setTagTypeStr(String tagTypeStr)
    {
        TagTypeStr = tagTypeStr;
    }

    public long getFindCnt()
    {
        return findCnt;
    }

    public void setFindCnt(long findCnt)
    {
        this.findCnt = findCnt;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
