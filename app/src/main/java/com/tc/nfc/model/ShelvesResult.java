package com.tc.nfc.model;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by tangjiarao on 16/9/28.
 */
public class ShelvesResult  implements Serializable {
    private Boolean checkResult;
    private CheckBook checkBook;
    private Boolean isCheck;
    private String message;
    private Boolean notice;

    public Boolean getNotice() {
        return notice;
    }

    public void setNotice(Boolean notice) {
        this.notice = notice;
    }

    public Boolean getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(Boolean checkResult) {
        this.checkResult = checkResult;
    }

    public CheckBook getCheckBook() {
        return checkBook;
    }

    public void setCheckBook(CheckBook checkBook) {
        this.checkBook = checkBook;
    }

    public Boolean getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(Boolean isCheck) {
        this.isCheck = isCheck;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public int hashCode(){

        if (checkBook.getBookBarcode().length()>10){

            return checkBook.getBookBarcode().length();
        }
        else{

            return Integer.parseInt(checkBook.getBookBarcode());
        }

    }
    //重写equals方法，通过id值判断是否相同
    public boolean equals(Object obj){
        ShelvesResult p =(ShelvesResult)obj;
        return this.checkBook.getBookBarcode().equals(p.checkBook.getBookBarcode());
    }

}
