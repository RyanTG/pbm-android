package com.pbm;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class SuggestLocation extends PinballMapActivity {
	private TreeMap<String, Integer> operators, locationTypes, zones;
	private String[] operatorNames, locationTypeNames, zoneNames;
	private Integer[] operatorIDs, locationTypeIDs, zoneIDs;
	private Spinner locationTypeSpinner, operatorSpinner, zoneSpinner;
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggest_location);
		TextView suggestLocationText = (TextView) findViewById(R.id.submitLocationId);
		suggestLocationText.setText("Submit a location to the " + getPBMApplication().getRegion().getFormalName() + " Pinball Map");
		logAnalyticsHit("com.pbm.SuggestLocation");

		PBMApplication app = getPBMApplication();
		String[] machineNames = app.getMachineNamesWithMetadata();
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_list_item_1, machineNames);
		MultiAutoCompleteTextView autoCompleteMachinesTextView = (MultiAutoCompleteTextView) findViewById(R.id.autoCompleteMachinesTextView);
		autoCompleteMachinesTextView.setAdapter(adapter);
		autoCompleteMachinesTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		initializeLocationTypes();

		if (!getPBMApplication().getOperators().isEmpty()) {
			initializeOperators();
		}

		if (!getPBMApplication().getZones().isEmpty()) {
			initializeZones();
		}

		loadSuggestLocationData();
	}

	public void initializeZones() {
		zones = new TreeMap<>();

		Zone blankZone = Zone.blankZone();
		zones.put(blankZone.getName(), blankZone.getId());
		for (Object element : getPBMApplication().getZones().values()) {
			Zone zone = (Zone) element;

			zones.put(zone.getName(), zone.getId());
		}

		zoneNames = zones.keySet().toArray(new String[zones.size()]);
		zoneIDs = zones.values().toArray(new Integer[zones.size()]);
	}

	public void initializeOperators() {
		operators = new TreeMap<>();

		Operator blankOperator = Operator.blankOperator();
		operators.put(blankOperator.getName(), blankOperator.getId());
		for (Object element : getPBMApplication().getOperators().values()) {
			Operator operator = (Operator) element;

			operators.put(operator.getName(), operator.getId());
		}

		operatorNames = operators.keySet().toArray(new String[operators.size()]);
		operatorIDs = operators.values().toArray(new Integer[operators.size()]);
	}

	public void initializeLocationTypes() {
		locationTypes = new TreeMap<>();

		LocationType blankLocationType = LocationType.blankLocationType();
		locationTypes.put(blankLocationType.getName(), blankLocationType.getId());
		for (Object element : getPBMApplication().getLocationTypes().values()) {
			LocationType locationType = (LocationType) element;

			locationTypes.put(locationType.getName(), locationType.getId());
		}

		locationTypeNames = locationTypes.keySet().toArray(new String[locationTypes.size()]);
		locationTypeIDs = locationTypes.values().toArray(new Integer[locationTypes.size()]);
	}

	private void loadSuggestLocationData() {
		new Thread(new Runnable() {
			public void run() {
				SuggestLocation.super.runOnUiThread(new Runnable() {
					public void run() {

						locationTypeSpinner = (Spinner) findViewById(R.id.locationTypeSpinner);
						ArrayAdapter<String> locationTypeAdapter = new ArrayAdapter<>(
								SuggestLocation.this,
								android.R.layout.simple_spinner_item,
								locationTypeNames
						);
						locationTypeSpinner.setAdapter(locationTypeAdapter);

						if (!getPBMApplication().getOperators().isEmpty()) {
							operatorSpinner = (Spinner) findViewById(R.id.operatorSpinner);
							ArrayAdapter<String> operatorAdapter = new ArrayAdapter<>(
									SuggestLocation.this,
									android.R.layout.simple_spinner_item,
									operatorNames
							);
							operatorSpinner.setAdapter(operatorAdapter);
						} else {
							operatorSpinner = (Spinner) findViewById(R.id.operatorSpinner);
							operatorSpinner.setVisibility(View.GONE);

							View operatorLabel = findViewById(R.id.operatorTextView);
							operatorLabel.setVisibility(View.GONE);
						}

						if (!getPBMApplication().getZones().isEmpty()) {
							zoneSpinner = (Spinner) findViewById(R.id.zoneSpinner);
							ArrayAdapter<String> zoneAdapter = new ArrayAdapter<>(
									SuggestLocation.this,
									android.R.layout.simple_spinner_item,
									zoneNames
							);
							zoneSpinner.setAdapter(zoneAdapter);
						} else {
							zoneSpinner = (Spinner) findViewById(R.id.zoneSpinner);
							zoneSpinner.setVisibility(View.GONE);

							View zoneLabel = findViewById(R.id.zoneTextView);
							zoneLabel.setVisibility(View.GONE);
						}
					}
				});
			}
		}).start();
	}

	public void buttonOnClick(View view) throws UnsupportedEncodingException {
		String locationName = ((EditText) findViewById(R.id.nameField)).getText().toString();
		String machineNames = ((MultiAutoCompleteTextView) findViewById(R.id.autoCompleteMachinesTextView)).getText().toString();

		if (!locationName.isEmpty() && !machineNames.isEmpty()) {
			PBMApplication app = getPBMApplication();
			Region region = app.getRegion(getSharedPreferences(PREFS_NAME, 0).getInt("region", -1));

			String operatorValue = getPBMApplication().getOperators().isEmpty() ? "" : URLEncoder.encode(((Spinner) findViewById(R.id.operatorSpinner)).getSelectedItem().toString(), "UTF-8");
			String zoneValue = getPBMApplication().getZones().isEmpty() ? "" : URLEncoder.encode(((Spinner) findViewById(R.id.zoneSpinner)).getSelectedItem().toString(), "UTF-8");

			String url = String.format(
				"%slocations/suggest.json?region_id=%d;location_name=%s;location_street=%s;location_city=%s;location_state=%s;location_zip=%s;location_phone=%s;location_website=%s;location_operator=%s;location_zone=%s;location_type=%s;location_machines=%s;location_comments=%s",
				regionlessBase,
				region.getId(),
				URLEncoder.encode(locationName, "UTF-8"),
				URLEncoder.encode(((EditText) findViewById(R.id.streetField)).getText().toString(), "UTF-8"),
				URLEncoder.encode(((EditText) findViewById(R.id.cityField)).getText().toString(), "UTF-8"),
				URLEncoder.encode(((EditText) findViewById(R.id.stateField)).getText().toString(), "UTF-8"),
				URLEncoder.encode(((EditText) findViewById(R.id.zipField)).getText().toString(), "UTF-8"),
				URLEncoder.encode(((EditText) findViewById(R.id.phoneField)).getText().toString(), "UTF-8"),
				URLEncoder.encode(((EditText) findViewById(R.id.websiteField)).getText().toString(), "UTF-8"),
				operatorValue,
				zoneValue,
				URLEncoder.encode(((Spinner) findViewById(R.id.locationTypeSpinner)).getSelectedItem().toString(), "UTF-8"),
				URLEncoder.encode(machineNames, "UTF-8"),
				URLEncoder.encode(((EditText) findViewById(R.id.commentsField)).getText().toString(), "UTF-8")
			);
			try {
				new RetrieveJsonTask().execute(app.requestWithAuthDetails(url), "POST").get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

			Toast.makeText(getBaseContext(), "Thank you for that submission! A region administrator will enter this location into the database shortly.", Toast.LENGTH_LONG).show();
			setResult(REFRESH_RESULT);
			SuggestLocation.this.finish();
		} else {
			Toast.makeText(getBaseContext(), "Location name and a list of machines are required fields.", Toast.LENGTH_LONG).show();
		}
	}
}

