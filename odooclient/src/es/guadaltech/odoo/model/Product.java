package es.guadaltech.odoo.model;

import com.google.gson.annotations.SerializedName;

public class Product {

	/*
	 * Generic model, product.product
	 */

	@SerializedName("id")
	private Integer id;

	@SerializedName("name")
	private String name;

	@SerializedName("default_code")
	private String defaultCode;

	@SerializedName("ean13")
	private String ean13;

	@SerializedName("sale_ok")
	private Boolean canBeSold;
	@SerializedName("purchase_ok")
	private Boolean canBePurchased;
	@SerializedName("hr_expense_ok")
	private Boolean canBeExpense;

	@SerializedName("product_image_small")
	private String productImageSmall;

	@SerializedName("list_price")
	private Double listPrice; // Precio de catálogo

	@SerializedName("standard_price")
	private Double costPrice; // Precio de coste

	@SerializedName("qty_available")
	private Float realStock;

	@SerializedName("virtual_available")
	private Float virtualStock;

	@SerializedName("description")
	private String description;

	@SerializedName("description_sale")
	private String descriptionSale;

	@SerializedName("write_date")
	private Long writeDate;

	public Product() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultCode() {
		return defaultCode;
	}

	public void setDefaultCode(String defaultCode) {
		this.defaultCode = defaultCode;
	}

	public String getEan13() {
		return ean13;
	}

	public void setEan13(String ean13) {
		this.ean13 = ean13;
	}

	public Boolean getCanBePurchased() {
		return canBePurchased;
	}

	public void setCanBePurchased(Boolean canBePurchased) {
		this.canBePurchased = canBePurchased;
	}

	public Boolean getCanBeExpense() {
		return canBeExpense;
	}

	public void setCanBeExpense(Boolean canBeExpense) {
		this.canBeExpense = canBeExpense;
	}

	public void setCanBeSold(Boolean canBeSold) {
		this.canBeSold = canBeSold;
	}

	public Boolean getCanBeSold() {
		return canBeSold;
	}

	public String getProductImageSmall() {
		return productImageSmall;
	}

	public void setProductImageSmall(String _productImageSmall) {
		this.productImageSmall = _productImageSmall;
	}

	public Double getListPrice() {
		return listPrice;
	}

	public void setListPrice(Double listPrice) {
		this.listPrice = listPrice;
	}

	public Double getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(Double costPrice) {
		this.costPrice = costPrice;
	}

	public Float getRealStock() {
		return realStock;
	}

	public void setRealStock(Float realStock) {
		this.realStock = realStock;
	}

	public Float getVirtualStock() {
		return virtualStock;
	}

	public void setVirtualStock(Float virtualStock) {
		this.virtualStock = virtualStock;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionSale() {
		return descriptionSale;
	}

	public void setDescriptionSale(String descriptionSale) {
		this.descriptionSale = descriptionSale;
	}

	public Long getWriteDate() {
		return writeDate;
	}

	public void setWriteDate(Long writeDate) {
		this.writeDate = writeDate;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
