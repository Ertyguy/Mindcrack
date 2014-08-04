package com.edaviessmith.mindcrack;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.edaviessmith.mindcrack.R;

public class About extends Activity {

	LinearLayout contactme, rateapp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		
		contactme = (LinearLayout)findViewById(R.id.contact_me);
		rateapp = (LinearLayout)findViewById(R.id.rate_app);
		
		contactme.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","erty.guy@gmail.com", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mindcrack App");

				startActivity(Intent.createChooser(emailIntent, "Send email..."));
				
			}
		});
		
		rateapp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("market://details?id=" + getPackageName()));
				startActivity(i);
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
