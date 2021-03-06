package com.pbm;

import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Location implements Serializable {
	@JsonProperty("id") private int id;
	@JsonProperty("name") private String name;
	@JsonProperty("lat") private String lat;
	@JsonProperty("lon") private String lon;
	@JsonProperty("city") private String city;
	@JsonProperty("last_updated_by_username") private String lastUpdatedByUsername;
	@JsonProperty("num_machines") private String numMachines;
	@JsonProperty("zone_id") private int zoneId;
	@JsonProperty("location_type_id") private int locationTypeId;
	@JsonProperty("operator_id") private int operatorId;
	private static final long serialVersionUID = 1L;
	private String street, state, zip, phone, website, milesInfo, dateLastUpdated, description;
	private float distanceFromYou;

	String getLat() {
		return lat;
	}
	String getLon() {
		return lon;
	}
	String getMilesInfo() {
		return milesInfo;
	}
	String getNumMachines() { return numMachines; }

	float getDistanceFromYou() { return distanceFromYou; }

	public Location() {}

	public Location(int id, String name, String lat, String lon, int zoneID, String street,
					String city, String state, String zip, String phone, int locationTypeID,
					String website, int operatorID, String dateLastUpdated,
					String lastUpdatedByUsername, String description, String numMachines
	) {
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.zoneId = zoneID;
		this.state = state;
		this.street = street;
		this.city = city;
		this.zip = zip;
		this.phone = phone;
		this.website = website;
		this.locationTypeId = locationTypeID;
		this.operatorId = operatorID;
		this.dateLastUpdated = dateLastUpdated;
		this.lastUpdatedByUsername = lastUpdatedByUsername;
		this.description = description;
		this.numMachines = numMachines;
	}

	@JsonCreator
	public Location(
		@JsonProperty("id") int id,
		@JsonProperty("name") String name,
		@JsonProperty("lat") String lat,
		@JsonProperty("lon") String lon,
		@JsonProperty("city") String city,
		@JsonProperty("last_updated_by_username") String lastUpdatedByUsername,
		@JsonProperty("num_machines") String numMachines,
		@JsonProperty("zone_id") int zoneId,
		@JsonProperty("location_type_id") int locationTypeId,
		@JsonProperty("operator_id") int operatorId
	) {
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.zoneId = zoneId;
		this.city = city;
		this.locationTypeId = locationTypeId;
		this.operatorId = operatorId;
		this.lastUpdatedByUsername = lastUpdatedByUsername;
		this.numMachines = numMachines;
	}

	static Comparator<Location> byNearestDistance = new Comparator<com.pbm.Location>() {
		public int compare(com.pbm.Location l1, com.pbm.Location l2) {
			Float distanceFromYou1 = l1.distanceFromYou;
			Float distanceFromYou2 = l2.distanceFromYou;
			return distanceFromYou1.compareTo(distanceFromYou2);
		}
	};

	public void setDescription(String description) { this.description = description; }

	public void setWebsite(String website) {
		this.website = website;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setNumMachines(String numMachines) { this.numMachines = numMachines; }

	void setLocationTypeID(int locationTypeID) {
		this.locationTypeId = locationTypeID;
	}

	void setOperatorID(int operatorID) { this.operatorId = operatorID; }

	public void setDistance(android.location.Location location) {
		float distance = location.distanceTo(toAndroidLocation());
		this.distanceFromYou = distance * PinballMapActivity.METERS_TO_MILES;

		NumberFormat formatter = new DecimalFormat(".00");
		setMilesInfo(formatter.format(this.distanceFromYou) + " miles");
	}

	private void setMilesInfo(String milesInfo) {
		this.milesInfo = milesInfo;
	}

	public String toString() {
		return milesInfo != null ? name + " " + milesInfo : name;
	}

	List<LocationMachineXref> getLmxes(PinballMapActivity activity) throws ParseException {
		List<LocationMachineXref> locationLmxes = new ArrayList<>();
		PBMApplication app = activity.getPBMApplication();

		for (LocationMachineXref lmx : app.getLmxes().values()) {
			if (lmx.getLocationID() == id) {
				locationLmxes.add(lmx);
			}
		}

		return locationLmxes;
	}

	List<Machine> getMachines(PinballMapActivity activity) throws ParseException {
		List<Machine> machinesFromLmxes = new ArrayList<>();
		PBMApplication app = activity.getPBMApplication();

		for (LocationMachineXref lmx : getLmxes(activity)) {
			machinesFromLmxes.add(app.getMachine(lmx.getMachineID()));
		}

		return machinesFromLmxes;
	}

	LocationType getLocationType(PinballMapActivity activity) {
		return activity.getPBMApplication().getLocationType(locationTypeId);
	}

	Operator getOperator(PinballMapActivity activity) {
		return activity.getPBMApplication().getOperator(operatorId);
	}

	void removeMachine(PinballMapActivity activity, LocationMachineXref lmx) {
		activity.getPBMApplication().removeLmx(lmx);
	}

	private android.location.Location toAndroidLocation() {
		android.location.Location mockLocation = new android.location.Location("");

		try {
			mockLocation.setLatitude(Double.valueOf(lat));
			mockLocation.setLongitude(Double.valueOf(lon));
		} catch (java.lang.NumberFormatException nfe) {
			nfe.printStackTrace();
		}

		return mockLocation;
	}

	static com.pbm.Location newFromDBCursor(Cursor cursor) {
		return new com.pbm.Location(
			cursor.getInt(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_ID)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_NAME)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_LAT)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_LON)),
			cursor.getInt(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_ZONE_ID)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_STREET)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_CITY)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_STATE)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_ZIP)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_PHONE)),
			cursor.getInt(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_LOCATION_TYPE_ID)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_WEBSITE)),
			cursor.getInt(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_OPERATOR_ID)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_DATE_LAST_UPDATED)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_LAST_UPDATED_BY_USERNAME)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_DESCRIPTION)),
			cursor.getString(cursor.getColumnIndexOrThrow(PBMContract.LocationContract.COLUMN_NUM_MACHINES))
		);
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	int getZoneID() {
		return zoneId;
	}

	int getOperatorID() {
		return operatorId;
	}

	int getLocationTypeID() {
		return locationTypeId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	String getState() {
		return state;
	}

	String getZip() {
		return zip;
	}

	public String getPhone() {
		return phone;
	}

	public String getWebsite() {
		return website;
	}

	String getLastUpdatedByUsername() {
		return lastUpdatedByUsername;
	}

	void setLastUpdatedByUsername(String lastUpdatedByUsername) {
		this.lastUpdatedByUsername = lastUpdatedByUsername;
	}

	String getDateLastUpdated() {
		return dateLastUpdated;
	}

	void setDateLastUpdated(String dateLastUpdated) {
		this.dateLastUpdated = dateLastUpdated;
	}

	public String getDescription() {
		return description;
	}
}