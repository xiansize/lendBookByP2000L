package com.tc.nfc.model;

/**
 * Created by tangjiarao on 17/2/14.
 */
public class BookHolding {
    String recno ;
    String state;
    String lib;
    String shelfno;

    public String getRecno() {
        return recno==null?"--":recno;
    }

    public void setRecno(String recno) {
        this.recno = recno;
    }

    public String getState() {
        return state==null?"--":state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLib() {
        return lib==null?"--":lib;
    }

    public void setLib(String lib) {
        this.lib = lib;
    }

    public String getShelfno() {
        return shelfno==null?"--":shelfno;
    }

    public void setShelfno(String shelfno) {
        this.shelfno = shelfno;
    }
}
