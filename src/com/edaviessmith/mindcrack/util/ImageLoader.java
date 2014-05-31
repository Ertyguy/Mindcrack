package com.edaviessmith.mindcrack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
    private static String TAG = "ImageLoader";
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService; 
     
    public ImageLoader(Context context){
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }
     
    //final int stub_id = R.drawable.ic_launcher;
    
    public void DisplayImage(String url, ImageView imageView)
    {
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView);
            //imageView.setImageResource(stub_id);
        }
    }
         
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
     
    private Bitmap getBitmap(String url) 
    {
        File f = fileCache.getFile(url);
         
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
         
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(new FlushedInputStream(is), os);
            os.close();
            is.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }
 
    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
       // try {
    	Bitmap bitmap = null;
    	if(f.exists()) {
        	try {
        		final int IMAGE_MAX_SIZE = 600000; // 1.2MP
        		
	        	//Scale bitmap to maximum size first
	            BitmapFactory.Options options = new BitmapFactory.Options();
	            options.inJustDecodeBounds = true;
	            InputStream inputStream = null;	            
	            inputStream = new FileInputStream(f);
	            BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);
	            inputStream.close();
	            // here w and h are the desired width and height
	
	            int scale = 1;
	            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >  IMAGE_MAX_SIZE) {
	               scale++;
	            }
	            
	            inputStream = new FileInputStream(f);
	            if (scale > 1) {
	                scale--;
	                // scale to max possible inSampleSize that still yields an image
	                // larger than target
	                options = new BitmapFactory.Options();
	                options.inSampleSize = scale;
	                bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);
	
	                // resize to desired dimensions
	                int height = bitmap.getHeight();
	                int width = bitmap.getWidth();
	                //Log.d(TAG, "1th scale operation dimenions - width: " + width + ",height: " + height);
	
	                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
	                double x = (y / height) * width;
	
	                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) x, (int) y, true);
	                //bitmap.recycle();
	                bitmap = scaledBitmap;
	
	                System.gc();
	            } else {
	            	bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
	            }
        	
        	} catch (IOException e) {
        	    Log.e(TAG, e.getMessage(),e);
        	    return null;
        	}
    	}
        return bitmap;
        	
        	/*
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
             
            //Find the correct scale value. It should be the power of 2.
            
            //TODO figure out at what samplesize should be used (image size here?)
            // MAKES IMAGE LOOK LIKE GARBAGE
            
            final int REQUIRED_SIZE=70;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
             
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=1;//scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;*/
    	
    }
     
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url = u; 
            imageView = i;
        }
    }
     
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
         
        @Override
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp = getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
     
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag = imageViews.get(photoToLoad.imageView);
        if(tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
     
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){
        	bitmap = b;
        	photoToLoad = p;
        }
        
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageDrawable(null);
        }
    }
 
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
 
    
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}