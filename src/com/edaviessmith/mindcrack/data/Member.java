package com.edaviessmith.mindcrack.data;

import com.edaviessmith.mindcrack.Constants;

public class Member {
	public int id;	
	public int sort;
	
	public String name;
	public int status;

	public int image; //Resource id
	public int icon; //Resource id
	
	public String uploadsId;
	public String twitterId;
		
	public Member (){}
	
	public Member(int id, String name, int image, int icon, String uploadsId, String twitterId) {
		this.id = id;
		this.sort = id;
		this.name = name;
		this.image = image;
		this.icon = icon;
		
		this.uploadsId = uploadsId;
		this.twitterId = twitterId;				
		this.status = Constants.VISIBLE;			
	}

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getUploadsId() {
		return uploadsId;
	}

	public void setUploadsId(String uploadsId) {
		this.uploadsId = uploadsId;
	}

	public String getTwitterId() {
		return twitterId;
	}

	public void setTwitterId(String twitterId) {
		this.twitterId = twitterId;
	}
	
	
	
    public String toDatabaseString() {
		return "(" + getId() + ", " + getSort() + ", " + getName() + ", "
				+ getStatus() + ", " + getIcon() + ", " + getUploadsId() + ", "
				+ getTwitterId() + ")";
    }
}
