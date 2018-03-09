package es.guadaltech.odoo.misc;

public class ListCellItem {

	private String image;
	private String text1;
	private String text2;
	private String text3;
	private double qty;
	private String openerpid;

	public ListCellItem(String image, String text1, String text2, String text3, double qty, String openerpid) {
		this.image = image;
		this.text1 = text1;
		this.text2 = text2;
		this.text3 = text3;
		this.qty = qty;
		this.openerpid = openerpid;
	}

	public ListCellItem() {
	}

	/*
	 * public ListCellItem(Parcel p) { String[] fields = new String[4];
	 * p.readStringArray(fields); this.text1 = fields[0]; this.text2 =
	 * fields[1]; this.text3 = fields[2]; this.openerpid = fields[3]; try {
	 * this.image = Bitmap.CREATOR.createFromParcel(p); } catch
	 * (RuntimeException e) { Log.e(Constants.TAG, "No hay imagen"); } }
	 */

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getText1() {
		return text1;
	}

	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}

	public String getText3() {
		return text3;
	}

	public void setText3(String text3) {
		this.text3 = text3;
	}

	public String getOpenerpid() {
		return openerpid;
	}

	public void setOpenerpid(String openerpid) {
		this.openerpid = openerpid;
	}

	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((openerpid == null) ? 0 : openerpid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListCellItem other = (ListCellItem) obj;
		if (openerpid == null) {
			if (other.openerpid != null)
				return false;
		} else if (!openerpid.equals(other.openerpid))
			return false;
		return true;
	}

}
