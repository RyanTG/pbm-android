package com.pbm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SplashScreen extends PinballMapActivity {
	private RegionsTab regionsTab;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.region_tab_container);

		final SharedPreferences settings = getSharedPreferences(PinballMapActivity.PREFS_NAME, 0);
		PreferenceManager.setDefaultValues(this, PinballMapActivity.PREFS_NAME, 0, R.xml.preferences, false);
		Integer prefRegion = settings.getInt("region", -1);

		PBMApplication app = getPBMApplication();
		app.setDbHelper(getBaseContext());
		app.setReadableDB(app.getDbHelper());
		app.setWriteableDB(app.getDbHelper());

		if (!haveInternet(getBaseContext())) {
			closeWithNoInternet();
			return;
		}

		try {
			if (!app.initializeRegions()) {
                closeOnMissingServer();
                return;
            }
		} catch (InterruptedException | ExecutionException | JSONException | IOException e) {
			e.printStackTrace();
		}

		if (!app.userIsAuthenticated() && prefRegion == -1 && (!getIntent().getBooleanExtra("isGuestLogin", false))) {
			Intent myIntent = new Intent();
			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			myIntent.setClassName("com.pbm", "com.pbm.Login");
			startActivityForResult(myIntent, PinballMapActivity.QUIT_RESULT);
		} else if (prefRegion != -1) {
			if (app.getRegion(prefRegion) != null) {
				Region region = app.getRegion(prefRegion);
				setRegionBase(httpBase + apiPath + "region/" + region.getName() + "/");

				Intent myIntent = new Intent();
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				myIntent.setClassName("com.pbm", "com.pbm.InitializingScreen");
				startActivityForResult(myIntent, PinballMapActivity.QUIT_RESULT);
			} else {
				regionsTab = new RegionsTab();
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.region_fragment, regionsTab);
				fragmentTransaction.commit();
			}
		} else {
			regionsTab = new RegionsTab();
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.region_fragment, regionsTab);
			fragmentTransaction.commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (getSupportFragmentManager().getFragments() != null) {
			for (Fragment f : getSupportFragmentManager().getFragments()) {
				if (f.getClass() == RegionsTab.class) {
					getSupportFragmentManager().putFragment(outState, RegionsTab.class.getName(),
							regionsTab);
					Log.d("com.pbm", "saving regionsTab" + regionsTab);
				}
			}
		}
	}

	@Override
	public void processLocation() {
		super.processLocation();
		if (regionsTab != null) {
			regionsTab.updateLocation();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
}