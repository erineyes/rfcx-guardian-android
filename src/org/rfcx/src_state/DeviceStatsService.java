package org.rfcx.src_state;

import org.rfcx.rfcx_src_android.RfcxSource;
import org.rfcx.src_util.DeviceCpuUsage;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DeviceStatsService extends Service {

	private static final String TAG = DeviceStatsService.class.getSimpleName();
	
	private static final boolean DEVICE_STATS_ENABLED = true;
	
	static final int DELAY = 500;
	private boolean runFlag = false;
	private CpuServiceCheck cpuServiceCheck;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (RfcxSource.verboseLog()) { Log.d(TAG, "onCreate()"); }
		this.cpuServiceCheck = new CpuServiceCheck();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (RfcxSource.verboseLog()) { Log.d(TAG, "onStart()"); }
		this.runFlag = true;
		this.cpuServiceCheck.start();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (RfcxSource.verboseLog()) { Log.d(TAG, "onDestroy()"); }
		this.runFlag = false;
		this.cpuServiceCheck.interrupt();
		this.cpuServiceCheck = null;
	}
	
	private class CpuServiceCheck extends Thread {
		
		public CpuServiceCheck() {
			super("CpuServiceCheck-CpuService");
		}
		
		@Override
		public void run() {
			DeviceStatsService cpuService = DeviceStatsService.this;
			DeviceCpuUsage deviceCpuUsage = ((RfcxSource) getApplication()).deviceCpuUsage;
			while (cpuService.runFlag) {
				try {
					deviceCpuUsage.updateCpuUsage();
					Log.d(TAG, "CPU: "+deviceCpuUsage.getCpuUsageAvg()+"/"+deviceCpuUsage.getCpuUsageNow());
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					cpuService.runFlag = false;
				}
			}
		}		
	}

	public static boolean areDeviceStatsEnabled() {
		return DEVICE_STATS_ENABLED;
	}
}
