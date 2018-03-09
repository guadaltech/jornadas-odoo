package es.guadaltech.odoo.model;

public class CompanyParams {
	
	private int id,idOERP;
	private String type, name;
	
	public CompanyParams(int id, int idOERP, String type, String name) {
		super();
		this.id = id;
		this.idOERP = idOERP;
		this.type = type;
		this.name = name;
	}

	public CompanyParams() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdOERP() {
		return idOERP;
	}

	public void setIdOERP(int idOERP) {
		this.idOERP = idOERP;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
