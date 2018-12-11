package com.tc.nfc.model;

/**
 * Created by tangjiarao on 16/9/28.
 */

import java.io.Serializable;

/**
 * Created by tangjiarao on 16/7/1.
 */
public class CheckBook implements Serializable {
    //书名
    private String bookTitle;
    //条形码
    private String bookBarcode;
    //出版社
    private String Publisher;
    //出版年份
    private String pubdate;
    //索引号
    private String ReferenceNum;
    //isbn
    private String Isbn;
    //书架号
    private String shelfno;

    private String imageUrl;
    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookBarcode() {
        return bookBarcode;
    }

    public void setBookBarcode(String bookBarcode) {
        this.bookBarcode = bookBarcode;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public String getReferenceNum() {
        return ReferenceNum;
    }

    public void setReferenceNum(String referenceNum) {
        ReferenceNum = referenceNum;
    }

    public String getIsbn() {
        return Isbn;
    }

    public void setIsbn(String isbn) {
        Isbn = isbn;
    }

    public String getShelfno() {
        return shelfno;
    }

    public void setShelfno(String shelfno) {
        this.shelfno = shelfno;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}