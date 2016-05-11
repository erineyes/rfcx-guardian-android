package org.rfcx.guardian.system.service;

import java.util.Date;

import org.rfcx.guardian.system.RfcxGuardian;
import org.rfcx.guardian.system.device.DeviceCpuUsage;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DeviceStateService extends Service {

	private static final String TAG = "Rfcx-"+RfcxGuardian.APP_ROLE+"-"+DeviceStateService.class.getSimpleName();
	
	RfcxGuardian app;
	
	private boolean runFlag = false;
	private DeviceStateSvc deviceStateSvc;

	private int recordingIncrement = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.deviceStateSvc = new DeviceStateSvc();
		
		if (app == null) { app = (RfcxGuardian) getApplication(); }
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (app == null) { app = (RfcxGuardian) getApplication(); }
		Log.v(TAG, "Starting service: "+TAG);
		this.runFlag = true;
		app.serviceHandler.setRunState("DeviceState", true);
		this.deviceStateSvc.start();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		app.serviceHandler.setRunState("DeviceState", false);
		this.deviceStateSvc.interrupt();
		this.deviceStateSvc = null;
	}
	
	
	private class DeviceStateSvc extends Thread {
		
		public DeviceStateSvc() {
			super("DeviceStateService-DeviceStateSvc");
		}
		
		@Override
		public void run() {
			DeviceStateService deviceStateService = DeviceStateService.this;
			
			if (app == null) { app = (RfcxGuardian) getApplication(); }
			
			long CYCLE_DELAY_MS = (long) ( Math.round( DeviceCpuUsage.STATS_REPORTING_CYCLE_DURATION_MS / DeviceCpuUsage.REPORTING_SAMPLE_COUNT ) - DeviceCpuUsage.SAMPLE_LENGTH_MS );
			
			while (deviceStateService.runFlag) {
				try {
					
					app.deviceCpuUsage.updateCpuUsage();
					
					recordingIncrement++;
					
					if (recordingIncrement < DeviceCpuUsage.REPORTING_SAMPLE_COUNT) {
						
						Thread.sleep(CYCLE_DELAY_MS);
						
					} else {
						
						app.deviceStateDb.dbCPU.insert(new Date(), app.deviceCpuUsage.getCpuUsageAvg(), app.deviceCpuUsage.getCpuClockAvg());
						
						int[] batteryStats = app.deviceBattery.getBatteryState(app.getApplicationContext(), null);
						app.deviceStateDb.dbBattery.insert(new Date(), batteryStats[0], batteryStats[1]);
						app.deviceStateDb.dbPower.insert(new Date(), batteryStats[2], batteryStats[3]);
						
						long[] networkStats = app.deviceNetworkStats.getDataTransferStatsSnapshot();
						// before saving, make sure this isn't the first time the stats are being generated (that throws off the net change figures)
						if (networkStats[6] == 0) {
							app.dataTransferDb.dbTransferred.insert(new Date(), new Date(networkStats[0]), new Date(networkStats[1]), networkStats[2], networkStats[3], networkStats[4], networkStats[5]);
						}
						
						recordingIncrement = 0;
					}
					
				} catch (InterruptedException e) {
					deviceStateService.runFlag = false;
					app.serviceHandler.setRunState("DeviceState", false);
					RfcxLog.logExc(TAG, e);
				}
			}
			Log.v(TAG, "Stopping service: "+TAG);
		}		
	}

}
