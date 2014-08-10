package com.edaviessmith.mindcrack.data;

public class Reddit {

	int id;	//name is id
	String title;
	String url;
	public int image; //Resource id
	public int icon; //Resource id
	

    
	public Reddit (){}
	
	public Reddit(int id, String title, String url, int image, int icon) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.image = image;
		this.icon = icon;
		
	}

	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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


	
	public String toDatabaseString() {
		return "(" + getId() + ", " + getTitle() + ", " + getUrl() + ","+ getIcon() + ")";
    }
    
    
}

