package com.gokaconsulting.notifyweb;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import javax.persistence.Transient;

@PersistenceCapable
public class Notification implements Serializable {
	@Transient
	private static final long serialVersionUID = -5660588353160363359L;

    public Notification(String fromAddress, String userEmail, Date sentDate,
			String messageBody, String subject) {
		super();
		this.fromAddress = fromAddress;
		this.userEmail = userEmail;
		this.sentDate = sentDate;
		this.messageBody = new Text(messageBody);
		this.subject = subject;
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
    @Expose
    private Date sentDate;
    
    @Persistent
    @Expose
    private Text messageBody;
       
    @Persistent
    @Expose
    private String subject;
    
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
 		return messageBody;
 	}

 	public void setMessageBody(String messageBody) {
 		this.messageBody = new Text(messageBody);
 	}

 	public String getSubject() {
 		return subject;
 	}

 	public void setSubject(String subject) {
 		this.subject = subject;
 	}

 	public Boolean getHighImportance() {
 		return highImportance;
 	}

 	public void setHighImportance(Boolean highImportance) {
 		this.highImportance = highImportance;
 	}

    
}
