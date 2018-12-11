package com.tc.nfc.model;

import java.io.Serializable;

/**
 * Created by tangjiarao on 16/7/1.
 */
public class LoanBook implements Serializable {

    private String bookTitle;
    private String bookBarcode;
    private String bookDate;
    private String Author;


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

    public String getBookDate() {
        return bookDate;
    }

    public void setBookDate(String bookDate) {
        this.bookDate = bookDate;
    }

    public String getAuthor() {return Author;}

    public void setAuthor(String author) {Author = author;}

}
