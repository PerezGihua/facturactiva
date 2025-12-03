package com.facturactiva.app.dto;

public class LoginResponse {

	private String idRol;
	private String nombreUser;
	private String message;
	
	public String getNombreUser() {
		return nombreUser;
	}

	public void setNombreUser(String nombreUser) {
		this.nombreUser = nombreUser;
	}

	public String getIdRol() {
		return idRol;
	}

	public void setIdRol(String idRol) {
		this.idRol = idRol;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LoginResponse(String idRol, String nombreUser, String message) {
		this.idRol = idRol;
		this.nombreUser = nombreUser;
		this.message = message;
	}
}
