package com.ufu.preprocessor.to;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "usersmin")
public class User {
	private static final long serialVersionUID = -111252190111815641L;
	@Id
    private Integer id;
	
	private Integer reputation;
	
	@Column(name="creationdate")
	private Timestamp creationDate;
	
	@Column(name="displayname")
    private String displayName;
	
	@Column(name="lastaccessdate")
	private Timestamp lastAccessDate;
	
	@Column(name="websiteurl")
    private String webSiteUrl;
	
	private String location;
	
	@Column(name="aboutme")
    private String aboutMe;
	
	private Integer views;
	
	@Column(name="upvotes")
	private Integer upVotes;
	
	@Column(name="downvotes")
	private Integer downVotes;
	
	@Column(name="profileimageurl")
    private String profileImageUrl;
	
	private Integer age;
	
	@Column(name="accountid")
	private Integer accountId;
	

	public User() {
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getReputation() {
		return reputation;
	}


	public void setReputation(Integer reputation) {
		this.reputation = reputation;
	}


	public Timestamp getCreationDate() {
		return creationDate;
	}


	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}


	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public Timestamp getLastAccessDate() {
		return lastAccessDate;
	}


	public void setLastAccessDate(Timestamp lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}


	public String getWebSiteUrl() {
		return webSiteUrl;
	}


	public void setWebSiteUrl(String webSiteUrl) {
		this.webSiteUrl = webSiteUrl;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public String getAboutMe() {
		return aboutMe;
	}


	public void setAboutMe(String aboutMe) {
		this.aboutMe = aboutMe;
	}


	public Integer getViews() {
		return views;
	}


	public void setViews(Integer views) {
		this.views = views;
	}


	public Integer getUpVotes() {
		return upVotes;
	}


	public void setUpVotes(Integer upVotes) {
		this.upVotes = upVotes;
	}


	public Integer getDownVotes() {
		return downVotes;
	}


	public void setDownVotes(Integer downVotes) {
		this.downVotes = downVotes;
	}


	public String getProfileImageUrl() {
		return profileImageUrl;
	}


	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}


	public Integer getAge() {
		return age;
	}


	public void setAge(Integer age) {
		this.age = age;
	}


	public Integer getAccountId() {
		return accountId;
	}


	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", reputation=" + reputation + ", creationDate=" + creationDate + ", displayName=" + displayName + ", lastAccessDate=" + lastAccessDate + ", webSiteUrl=" + webSiteUrl
				+ ", location=" + location + ", aboutMe=" + aboutMe + ", views=" + views + ", upVotes=" + upVotes + ", downVotes=" + downVotes + ", profileImageUrl=" + profileImageUrl + ", age=" + age
				+ ", accountId=" + accountId + "]";
	}

	
	
	

	
	
    
}