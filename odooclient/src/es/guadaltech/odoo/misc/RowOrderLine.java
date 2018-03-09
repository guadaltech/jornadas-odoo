package es.guadaltech.odoo.misc;

public class RowOrderLine {

	private String image;// think it's better
	private Integer productId;
	private String productName;
	private Double productPrice;
	private Float productDiscount;
	private Float productQty;

	public RowOrderLine(String image, Integer productId, String productName, Double productPrice,
			Float productDiscount, Float productQty) {
		super();
		this.image = image;
		this.productId = productId;
		this.productName = productName;
		this.productPrice = productPrice;
		this.productDiscount = productDiscount;
		this.productQty = productQty;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Double getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(Double productPrice) {
		this.productPrice = productPrice;
	}

	public Float getProductDiscount() {
		return productDiscount;
	}

	public void setProductDiscount(Float productDiscount) {
		this.productDiscount = productDiscount;
	}

	public Float getProductQty() {
		return productQty;
	}

	public void setProductQty(Float productQty) {
		this.productQty = productQty;
	}

}
