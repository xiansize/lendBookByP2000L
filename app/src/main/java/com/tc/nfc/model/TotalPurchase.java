package com.tc.nfc.model;

/**
 * Created by tangjiarao on 16/12/28.
 */
public class TotalPurchase {
    private String totalCopies;
    private String totalPrice;
    private String totalAcqPrice;
    private String nowBatchnoSearchTotalNum;
    private String todayCopies;
    private String todayPrice;
    private String todayAcqPrice;
    private String todaySearchNum;

    public String getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(String totalCopies) {
        this.totalCopies = totalCopies;
    }

    public String getTotalAcqPrice() {
        return totalAcqPrice;
    }

    public void setTotalAcqPrice(String totalAcqPrice) {
        this.totalAcqPrice = totalAcqPrice;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getNowBatchnoSearchTotalNum() {
        return nowBatchnoSearchTotalNum;
    }

    public void setNowBatchnoSearchTotalNum(String nowBatchnoSearchTotalNum) {
        this.nowBatchnoSearchTotalNum = nowBatchnoSearchTotalNum;
    }

    public String getTodayCopies() {
        return todayCopies;
    }

    public void setTodayCopies(String todayCopies) {
        this.todayCopies = todayCopies;
    }

    public String getTodayPrice() {
        return todayPrice;
    }

    public void setTodayPrice(String todayPrice) {
        this.todayPrice = todayPrice;
    }

    public String getTodayAcqPrice() {
        return todayAcqPrice;
    }

    public void setTodayAcqPrice(String todayAcqPrice) {
        this.todayAcqPrice = todayAcqPrice;
    }

    public String getTodaySearchNum() {
        return todaySearchNum;
    }

    public void setTodaySearchNum(String todaySearchNum) {
        this.todaySearchNum = todaySearchNum;
    }
}
