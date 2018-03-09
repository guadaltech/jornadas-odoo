package es.guadaltech.odoo.model;

import com.google.gson.annotations.SerializedName;

public class OrderLine {

	/*
	 * Generic model, pos.order.line
	 */

	@SerializedName("id")
	private Integer id;

	@SerializedName("product_id")
	private Integer productID;

	@SerializedName("order_id")
	private Integer orderID;

	@SerializedName("discount")
	private Float discount;

	@SerializedName("qty")
	private Float quantity;

	@SerializedName("price_unit")
	private Float priceUnit;

	@SerializedName("notice")
	private String notice;

	@SerializedName("write_date")
	private Integer writeDate;

	public OrderLine() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getProductID() {
		return productID;
	}

	public void setProductID(Integer productID) {
		this.productID = productID;
	}

	public Integer getOrderID() {
		return orderID;
	}

	public void setOrderID(Integer orderID) {
		this.orderID = orderID;
	}

	public Float getDiscount() {
		return discount;
	}

	public void setDiscount(Float discount) {
		this.discount = discount;
	}

	public Float getQuantity() {
		return quantity;
	}

	public void setQuantity(Float quantity) {
		this.quantity = quantity;
	}

	public Float getPriceUnit() {
		return priceUnit;
	}

	public void setPriceUnit(Float priceUnit) {
		this.priceUnit = priceUnit;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public Integer getWriteDate() {
		return writeDate;
	}

	public void setWriteDate(Integer writeDate) {
		this.writeDate = writeDate;
	}

}
