package com.edaviessmith.mindcrack.data;

public class RedditPost {

	String id;	//name is id
	String title;
    String author;
    int points;
    int numComments;
    String permalink;
    String url;    
    String domain;
    
    String titleFlair;
    
    String textHtml;  //Null if video
    //boolean stickied; //Good for indication
    long created;
    
    boolean isSelf; //image video or post
    
    //Youtube from url ?v=######
    String videoId;
    public String imageMed;
	public String imageHigh;
    
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public int getNumComments() {
		return numComments;
	}
	public void setNumComments(int numComments) {
		this.numComments = numComments;
	}
	public String getPermalink() {
		return permalink;
	}
	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getTitleFlair() {
		return titleFlair;
	}
	public void setTitleFlair(String titleFlair) {
		this.titleFlair = titleFlair;
	}
	public String getTextHtml() {
		return textHtml;
	}
	public void setTextHtml(String textHtml) {
		this.textHtml = textHtml;
	}
	/*public boolean isStickied() {
		return stickied;
	}
	public void setStickied(boolean stickied) {
		this.stickied = stickied;
	}*/
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	public boolean isSelf() {
		return isSelf;
	}
	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}
	public String getVideoId() {
		return videoId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
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
		return "(" + getId() + ", " + getTitle() + ", " + getAuthor() + ", "
				+ getPoints() + ", " + getNumComments() + ", " + getPermalink() + ", "
				+ getUrl() + ")";
    }
    
    
}

