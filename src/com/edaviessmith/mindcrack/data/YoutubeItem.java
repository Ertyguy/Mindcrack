package com.edaviessmith.mindcrack.data;

public class YoutubeItem {
	
	public int id;
	public int memberId;
	
	public String videoId;
	public String title;
	public long date;
	
	public String imageMed;
	public String imageHigh;
	
	public YoutubeItem() { }
	
	public YoutubeItem(String title, int date) {
		this.title = title;
		this.date = date;						
	}

	public YoutubeItem(int id, int memberId, String videoId, String title, int date) {
		this.id = id;
		this.memberId = memberId;
		this.videoId = videoId;
		this.title = title;
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

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getImageMed() {
		return imageMed;
	}

	public void setImageMed(String imageMed) {
		this.imageMed = imageMed;
	}

	public String getImageHigh() {
		return imageHigh;
	}

	public void setImageHigh(String imageHigh) {
		this.imageHigh = imageHigh;
	}
	
	public String toDatabaseString() {
		return id +", "+ memberId +", "+ videoId +", "+ title +", "+ date +", "+ imageMed +", "+ imageHigh;
	}

}
