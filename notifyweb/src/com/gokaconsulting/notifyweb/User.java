package com.gokaconsulting.notifyweb;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Transient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;

@PersistenceCapable
public class User implements Serializable {
	@Transient
	private static final long serialVersionUID = -5660588353160363359L;

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
    
	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public Boolean getValidated() {
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
	    if (password.equals(this.password))
	    {
	        return;
	    }

	    if (this.salt == null || this.salt.equals(""))
	    {
	        this.salt = RandomStringUtils.randomAscii(20);
	    }

	    this.password = DigestUtils.sha1Hex(password + this.salt);

	}

	public boolean checkPassword(String givenPassword)
	{
	    return (this.password.equals(DigestUtils.sha1Hex(givenPassword + this.salt)));
	}
}
