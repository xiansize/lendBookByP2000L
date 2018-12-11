package com.tc.nfc.model;

public class Purchase {

	private String isbn;
	private Integer orderNum;
	private String orderDate;
	private String imei;
	private String orderLibLocal;
	
	
	
	public Purchase() {
		super();
	}
	
	public Purchase(String isbn, Integer orderNum,String orderDate,String imei,String orderLibLocal) {
		super();
		this.isbn = isbn;
		this.orderNum = orderNum;
		this.orderDate = orderDate;
		this.imei = imei;
		this.orderLibLocal = orderLibLocal;
	}
	
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public Integer getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getOrderLibLocal() {
		return orderLibLocal;
	}

	public void setOrderLibLocal(String orderLibLocal) {
		this.orderLibLocal = orderLibLocal;
	}
	
	
	
}
