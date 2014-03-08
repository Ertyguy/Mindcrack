package com.edaviessmith.mindcrack.data;

public class MemberActivity {
	public int id;
	public String video;
	public String latestVideo;
	
	public MemberActivity(int id, String video) {
		this.id = id;
		this.video = video;
		this.latestVideo = video;
	}
	
	public boolean newVideo() {
		if(!latestVideo.equals(video)) {
			video = latestVideo;
			return true;
		}
		return false;
	}
}