package org.rfcx.guardian.utility.device;

import android.location.LocationManager;

public class DeviceGeoLocation {
	
	public DeviceGeoLocation(String appRole) {
		this.logTag = "Rfcx-"+appRole+"-"+DeviceGeoLocation.class.getSimpleName();
	}
	
	private String logTag = "Rfcx-Utils-"+DeviceGeoLocation.class.getSimpleName();
	
	private LocationManager locationManager;
	private double geoLocationLatitude = 3.6141375; //0;
	private double geoLocationLongitude = 14.2108033; //0;
	private double geoLocationPrecision = 1.000001; //0;
	
	public double[] getGeoLocation() {
		return new double[] { this.geoLocationLatitude, this.geoLocationLongitude, this.geoLocationPrecision };
	}
	
	public String getSerializedGeoLocation() {
		double[] geoLocation = getGeoLocation();
		return (new StringBuilder())
			.append(System.currentTimeMillis())
			.append("*").append(geoLocation[0])
			.append("*").append(geoLocation[1])
			.append("*").append(geoLocation[2])
			.toString();
	}
	
	private void updateGeoLocation() {
//		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//		Criteria criteria = new Criteria();
//		String bestProvider = locationManager.getBestProvider(criteria, false);
//		Location location = locationManager.getLastKnownLocation(bestProvider);
//		try {
//			this.geoLocationLatitude = (double) location.getLatitude();
//			this.geoLocationLongitude = (double) location.getLongitude();
//			this.geoLocationPrecision = 0;
//		} catch (Exception e) {
//			RfcxLog.logExc(TAG, e);
//		}
	}

	
	
	
}
