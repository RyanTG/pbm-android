package com.pbm;

import android.os.Bundle;
import android.view.Menu;

public class About extends PBMUtil {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}   

	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
} 