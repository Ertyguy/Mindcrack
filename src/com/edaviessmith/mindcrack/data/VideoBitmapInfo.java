package com.edaviessmith.mindcrack.data;

import android.graphics.Bitmap;

import com.google.api.services.youtube.model.PlaylistItem;

public class VideoBitmapInfo {
	 public Bitmap bitmap;
	 public PlaylistItem playlistItem;
	 public VideoBitmapInfo(Bitmap bitmap, PlaylistItem playlistItem){
		 this.bitmap = bitmap;
		 this.playlistItem = playlistItem;
	 }
}