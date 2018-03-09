package es.guadaltech.odoo.misc;

public class AssociativeSalesLine {

	private String name;
	private int product_id;// mandatory
	private int order_id;// mandatory
	private double price_unit;// mandatory
	private double price_subtotal;
	private double price_subtotal_incl;// Subtotal con impuestos
	private double discount;
	private double qty;// mandatory

	public AssociativeSalesLine(int product_id, int order_id, double price_unit, double qty) {
		super();
		this.product_id = product_id;
		this.order_id = order_id;
		this.price_unit = price_unit;
		this.qty = qty;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getProductId() {
		return product_id;
	}

	public void setProductId(int product_id) {
		this.product_id = product_id;
	}

	public int getOrderId() {
		return order_id;
	}

	public void setOrderId(int order_id) {
		this.order_id = order_id;
	}

	public double getPriceUnit() {
		return price_unit;
	}

	public void setPriceUnit(double price_unit) {
		this.price_unit = price_unit;
	}

	public double getPriceSubtotal() {
		return price_subtotal;
	}

	public void setPriceSubtotal(double price_subtotal) {
		this.price_subtotal = price_subtotal;
	}

	public double getPriceSubtotalWithTaxes() {
		return price_subtotal_incl;
	}

	public void setPriceSubtotalWithTaxes(double price_subtotal_incl) {
		this.price_subtotal_incl = price_subtotal_incl;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}

}
