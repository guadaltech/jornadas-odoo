package es.guadaltech.odoo.model;

import com.google.gson.annotations.SerializedName;

public class Order {

	/*
	 * Generic model, pos.order
	 */

	@SerializedName("id")
	private Integer id;

	@SerializedName("name")
	private String name;

	@SerializedName("date_order")
	private long dateOrder;

	@SerializedName("partner_id")
	private Integer partnerID;

	@SerializedName("shop_id")
	private Integer shopID;

	@SerializedName("user_id")
	private Integer userID;

	@SerializedName("state")
	private Order.STATE state;

	@SerializedName("amount_total")
	private Float total;

	@SerializedName("write_date")
	private Integer writeDate;

	public enum STATE {
		NUEVO("draft"), CANCELADO("cancel"), PAGADO("paid"), FINALIZADO("done"), FACTURADO("invoiced"), ;

		private STATE(final String state) {
			this.state = state;
		}

		private final String state;

		@Override
		public String toString() {
			return state;
		}
	}

	public Order() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getDateOrder() {
		return dateOrder;
	}

	public void setDateOrder(long dateOrder) {
		this.dateOrder = dateOrder;
	}

	public Integer getPartnerID() {
		return partnerID;
	}

	public void setPartnerID(Integer partnerID) {
		this.partnerID = partnerID;
	}

	public Integer getShopID() {
		return shopID;
	}

	public void setShopID(Integer shopID) {
		this.shopID = shopID;
	}

	public Integer getUserID() {
		return userID;
	}

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public Order.STATE getState() {
		return state;
	}

	public void setState(Order.STATE state) {
		this.state = state;
	}

	public Float getTotal() {
		return total;
	}

	public void setTotal(Float total) {
		this.total = total;
	}

	public Integer getWriteDate() {
		return writeDate;
	}

	public void setWriteDate(Integer writeDate) {
		this.writeDate = writeDate;
	}

}
