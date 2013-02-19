package com.gokaconsulting.notifyweb.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.Transient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;

@PersistenceCapable
public class User implements Serializable {
	@Transient
	private static final long serialVersionUID = -5660588353160363359L;
	
	@Transient
	private static final Logger logger = Logger.getLogger(User.class.getName());
	
	public User(String userAddress, String password, Date lastLogin) {
		super();
		this.userAddress = userAddress;
		setPassword(password);
		this.lastLogin = lastLogin;
	}

	@PrimaryKey
    @Persistent
    @Expose
    private String userAddress;
    
    @Persistent
    private Boolean validated;
    
    @Persistent
    private String salt;
    
    @Persistent
    private String password;
    
    @Persistent
    private Date lastLogin;
    
    @Persistent
    private int unRead;
    
	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public Boolean isValidated() {
		return validated;
	}

	public void setValidated(Boolean validated) {
		this.validated = validated;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	
	public void setPassword(String password)
	{
		if (password != null) {
			if (password.equals(this.password)) {
				return;
			}

			if (this.salt == null || this.salt.equals("")) {
				this.salt = RandomStringUtils.randomAscii(20);
			}

			this.password = DigestUtils.sha1Hex(password + this.salt);
		} else {
			this.password = null;
		}
	}

	public boolean checkPassword(String givenPassword)
	{
		if (this.password != null) {
			return (this.password.equals(DigestUtils.sha1Hex(givenPassword
					+ this.salt)));
		}
		return true;
	}
	
	public int getUnRead() {
		return unRead;
	}
	
	public boolean isReset(){
		
		if(this.password == null)
		{
			return true;
		}
		return false;
	}
	
	public void setUnRead(int unRead) {
		this.unRead = unRead;
	}
}
