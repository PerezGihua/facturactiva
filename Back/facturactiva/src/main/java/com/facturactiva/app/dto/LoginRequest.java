package com.facturactiva.app.dto;

public class LoginRequest extends BaseEntity{
	
    private String username;
    private String password;

	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public LoginRequest(String codRpta, String msgRpta, String username, String password) {
		super(codRpta, msgRpta);
		this.username = username;
		this.password = password;
	}
}
