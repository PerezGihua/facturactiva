package com.facturactiva.app.dto;

public class BaseEntity {

	private String codRpta;
	private String msgRpta;
	
	public String getCodRpta() {
		return codRpta;
	}
	
	public void setCodRpta(String codRpta) {
		this.codRpta = codRpta;
	}
	
	public String getMsgRpta() {
		return msgRpta;
	}
	
	public void setMsgRpta(String msgRpta) {
		this.msgRpta = msgRpta;
	}
	
	public BaseEntity(String codRpta, String msgRpta) {
		super();
		this.codRpta = codRpta;
		this.msgRpta = msgRpta;
	}
}
