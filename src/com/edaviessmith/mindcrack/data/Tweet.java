package com.edaviessmith.mindcrack.data;

public class Tweet {

	public int id;
	public int memberId;
	
	public long tweetId;
	public String text;
	public long date;
	
	
	
	public Tweet() { }
	
	public Tweet(String text, int date) {
		this.text = text;
		this.date = date;						
	}

	public Tweet(int id, int memberId, long tweetId, String text, int date) {
		this.id = id;
		this.memberId = memberId;
		this.tweetId = tweetId;
		this.text = text;
		this.date = date;						
	}
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public long getTweetId() {
		return tweetId;
	}

	public void setTweetId(long tweetId) {
		this.tweetId = tweetId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	
	public String toDatabaseString() {
		return id +", "+ memberId +", "+ tweetId +", "+ text +", "+ date;
	}
}
