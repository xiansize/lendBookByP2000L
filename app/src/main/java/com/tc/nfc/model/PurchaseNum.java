package com.tc.nfc.model;

/**
 * Created by tangjiarao on 16/12/26.
 */
public class PurchaseNum {
    /**
     * 馆藏
     */
    private String holdingSum;
    /**
     * 订购
     */
    private String orderSum;
    /**
     * 外借
     */
    private String circulateSum;
    /**
     * 荐购
     */
    private String commendSum;
    /**
     * 复本数
     */
    private String copies;

    private String copiesSumByBatchno;

    public String getHoldingSum() {
        return holdingSum;
    }

    public void setHoldingSum(String holdingSum) {
        this.holdingSum = holdingSum;
    }

    public String getOrderSum() {
        return orderSum;
    }

    public void setOrderSum(String orderSum) {
        this.orderSum = orderSum;
    }

    public String getCirculateSum() {
        return circulateSum;
    }

    public void setCirculateSum(String circulateSum) {
        this.circulateSum = circulateSum;
    }

    public String getCommendSum() {
        return commendSum;
    }

    public void setCommendSum(String commendSum) {
        this.commendSum = commendSum;
    }

    public String getCopies() {
        return copies;
    }

    public void setCopies(String copies) {
        this.copies = copies;
    }

    public String getCopiesSumByBatchno() {
        return copiesSumByBatchno;
    }

    public void setCopiesSumByBatchno(String copiesSumByBatchno) {
        this.copiesSumByBatchno = copiesSumByBatchno;
    }
}
