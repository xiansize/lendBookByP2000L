package com.tc.nfc.model;

import java.io.Serializable;

/**
 * Created by tangjiarao on 16/7/1.
 */
public class Book implements Serializable {

    private String bookTitle;
    private String bookBarcode;
    private String bookDate;
    private String Author;
    private String bookrecno;
    private String booktype;
    private String classNo;
    private String isbn;
    private String publisher;
    private String callno;
    private String loanCount;
    private String loanDate;
    private String returnDate;
    private String price;
    private Boolean isCheck;

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

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public Boolean getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(Boolean isCheck) {
        this.isCheck = isCheck;
    }

    public String getBookrecno() {
        return bookrecno;
    }

    public void setBookrecno(String bookrecno) {
        this.bookrecno = bookrecno;
    }

    public String getBooktype() {
        return booktype;
    }

    public void setBooktype(String booktype) {
        this.booktype = booktype;
    }

    public String getClassNo() {
        return classNo;
    }

    public void setClassNo(String classNo) {
        this.classNo = classNo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCallno() {
        return callno;
    }

    public void setCallno(String callno) {
        this.callno = callno;
    }

    public String getLoanCount() {
        return loanCount;
    }

    public void setLoanCount(String loanCount) {
        this.loanCount = loanCount;
    }

    public String getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(String loanDate) {
        this.loanDate = loanDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
