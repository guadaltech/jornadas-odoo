package es.guadaltech.odoo.model;

import com.google.gson.annotations.SerializedName;

public class PartnerAddress {

	/*
	 * Generic model, res.partner.address
	 */

	@SerializedName("id")
	private int id;

	@SerializedName("partner_id")
	private int partner_id;

	@SerializedName("function")
	private String function;

	@SerializedName("street")
	private String street;

	@SerializedName("street2")
	private String street2;

	@SerializedName("city")
	private String city;

	@SerializedName("name")
	private String name;

	@SerializedName("phone")
	private String mobile;

	@SerializedName("email")
	private String email;

	@SerializedName("write_date")
	private long writeDate;

	public PartnerAddress() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(int partner_id) {
		this.partner_id = partner_id;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreet2() {
		return street2;
	}

	public void setStreet2(String street2) {
		this.street2 = street2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getWriteDate() {
		return writeDate;
	}

	public void setWriteDate(long writeDate) {
		this.writeDate = writeDate;
	}

}
