package com.gokaconsulting.notifyweb.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import java.util.Date;
import java.util.logging.Logger;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import javax.persistence.Transient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;


@PersistenceCapable
public class Notification implements Serializable {
	
	@Transient
	private final Logger logger = Logger.getLogger(Notification.class.getName());
	@Transient
	private static final long serialVersionUID = -5660588353160363359L;

    public Notification(String fromAddress, String userEmail, Date sentDate, String messageBody, String subject) {
		super();
		this.fromAddress = fromAddress;
		this.userEmail = userEmail;
		this.sentDate = sentDate;
		
		setMessageBody(messageBody);
		setSubject(subject);
	}

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    
	@Persistent
    @Expose
    private Long notifyId;
    
    @Persistent
    @Expose
    private String fromAddress;
    
    @Persistent
    @Expose
    private String userEmail;
    
    @Persistent
    private String salt;
    
    @Persistent
    @Expose
    private Date sentDate;
    
    @Persistent
    @Expose
    private Text messageBody;
       
    @Persistent
    @Expose
    private Text subject;
    
    @Persistent
    @Expose
    private Boolean highImportance;
    
    public Key getKey() {
 		return key;
 	}

 	public Long getId() {
 		return notifyId;
 	}

 	public void setId(Long id) {
 		this.notifyId = id;
 	}

 	public String getFromAddress() {
 		return fromAddress;
 	}

 	public void setFromAddress(String fromAddress) {
 		this.fromAddress = fromAddress;
 	}

 	public String getUserEmail() {
 		return userEmail;
 	}

 	public void setUserEmail(String userEmail) {
 		this.userEmail = userEmail;
 	}

 	public Date getSentDate() {
 		return sentDate;
 	}

 	public void setSentDate(Date sentDate) {
 		this.sentDate = sentDate;
 	}

 	public Text getMessageBody() {
 		return new Text(StringUtils.newStringUtf8(Base64.decodeBase64(this.messageBody.getValue())));
 	}

 	public void setMessageBody(String messageBody) {
 		this.messageBody = new Text(Base64.encodeBase64String(StringUtils.getBytesUtf8(messageBody)));
 	}

 	public String getSubject() {
 		return StringUtils.newStringUtf8(Base64.decodeBase64(this.subject.getValue()));
 	}

 	public void setSubject(String subject) {
 		this.subject = new Text(Base64.encodeBase64String(StringUtils.getBytesUtf8(subject)));
  	}

 	public Boolean getHighImportance() {
 		return highImportance;
 	}

 	public void setHighImportance(Boolean highImportance) {
 		this.highImportance = highImportance;
 	}
}
