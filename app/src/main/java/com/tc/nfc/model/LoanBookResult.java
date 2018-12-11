package com.tc.nfc.model;

import java.io.Serializable;

/**
 * Created by tangjiarao on 16/7/1.
 */
public class LoanBookResult implements Serializable {

    private Boolean LoanResult;
    private LoanBook loanBook;
    private Boolean isCheck;
    private String message;

    public Boolean getLoanResult() {
        return LoanResult;
    }

    public void setLoanResult(Boolean loanResult) {
        LoanResult = loanResult;
    }

    public LoanBook getLoanBook() {
        return loanBook;
    }

    public void setLoanBook(LoanBook loanBook) {
        this.loanBook = loanBook;
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
}
