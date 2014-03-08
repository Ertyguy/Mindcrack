package com.edaviessmith.mindcrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.edaviessmith.mindcrack.R;


public class TestFragment extends SherlockFragment {
	 private String mContent = "???";
	 private static String KEY_TAB_NUM;
     
	 
	 public static TestFragment newInstance(String text) {
        TestFragment fragment = new TestFragment();
         
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(KEY_TAB_NUM, text);
        fragment.setArguments(args);
 
        return fragment;
    }
  
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.member_list, null);
        //String text = getString(R.string.tab_page_num) + mContent;
        //((TextView)view.findViewById(R.id.text)).setText(text);

        
        return view;
 }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContent =  getArguments() != null ? getArguments().getString(KEY_TAB_NUM) : "???";
        
     }
      
     
}