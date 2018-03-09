package es.guadaltech.odoo.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Partner {

	/*
	 * Generic model, res.partner
	 */

	@SerializedName("id")
	private Integer id;

	@SerializedName("name")
	private String name;

	@SerializedName("city")
	private String city;

	@SerializedName("function")
	private String function;

	@SerializedName("customer")
	private Boolean customer;

	@SerializedName("supplier")
	private Boolean supplier;

	@SerializedName("ref")
	private String reference;

	@SerializedName("emails")
	private String[] emails;

	@SerializedName("mobile")
	private String mobile;

	@SerializedName("phone")
	private String phone;

	@SerializedName("write_date")
	private Long writeDate;

	private transient List<PartnerAddress> adresses;

	public Partner() {
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

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public Boolean isCustomer() {
		return customer;
	}

	public void setCustomer(Boolean customer) {
		this.customer = customer;
	}

	public Boolean isSupplier() {
		return supplier;
	}

	public void setSupplier(Boolean supplier) {
		this.supplier = supplier;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Long getWriteDate() {
		return writeDate;
	}

	public void setWriteDate(Long writeDate) {
		this.writeDate = writeDate;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String[] getEmails() {
		return emails;
	}

	public void setEmails(String[] emails) {
		this.emails = emails;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public List<PartnerAddress> getAdresses() {
		return adresses;
	}

	public void setAdresses(List<PartnerAddress> adresses) {
		this.adresses = adresses;
	}

}
