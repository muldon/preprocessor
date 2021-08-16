package com.ufu.preprocessor.to;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "commentsmin")
public class Comment {
	private static final long serialVersionUID = -111652190111115641L;
	@Id
    private Integer id;
	
	@Column(name="postid")
	private Integer postId;
	
	@Column(name="score")
	private Integer score;
	
	private String text;
	
	@Column(name="processedtextlemma")
	private String processedTextLemma; //stemmed,stopped
	
	@Column(name="creationdate")
	private Timestamp creationDate;
	
	@Column(name="userid")
	private Integer userId;
	
	@Transient
	private User user;
	

	public Comment() {
	}
	
	public Comment(Integer id) {
		this.id = id;
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
		Comment other = (Comment) obj;
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

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	
	@Override
	public String toString() {
		return "Comment [id=" + id + ", postId=" + postId + ", score=" + score + ", text=" + text + ", processedText="
				+ processedTextLemma + ", creationDate=" + creationDate + ", userId=" + userId + ", user=" + user + "]";
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getProcessedTextLemma() {
		return processedTextLemma;
	}

	public void setProcessedTextLemma(String processedText) {
		this.processedTextLemma = processedText;
	}

	

	
	

	
	
    
}