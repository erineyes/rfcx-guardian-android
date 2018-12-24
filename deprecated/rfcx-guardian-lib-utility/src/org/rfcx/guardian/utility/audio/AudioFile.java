package org.rfcx.guardian.utility.audio;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.rfcx.guardian.utility.FileUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class AudioFile {

	private static final String logTag = "Rfcx-Utils-"+AudioFile.class.getSimpleName();
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM/dd-a", Locale.US);
	
	public static final int AUDIO_SAMPLE_SIZE = 16;
	public static final int AUDIO_CHANNEL_COUNT = 1;
	
	public static void initializeAudioDirectories(Context context) {
		(new File(captureDir(context))).mkdirs(); FileUtils.chmod(captureDir(context), 0777);
		(new File(cacheFilesDir())).mkdirs(); FileUtils.chmod(cacheFilesDir(), 0777);
		(new File(encodeDir())).mkdirs(); FileUtils.chmod(encodeDir(), 0777);
		(new File(sdCardFilesDir())).mkdirs(); FileUtils.chmod(sdCardFilesDir(), 0777);
		(new File(finalFilesDir())).mkdirs(); FileUtils.chmod(finalFilesDir(), 0777);
		(new File(postZipDir())).mkdirs(); FileUtils.chmod(postZipDir(), 0777);
	}
	
	public static String appFilesDir(Context context) {
		return context.getFilesDir().toString();
	}
	
	public static String sdCardFilesDir() {
		return (new StringBuilder()).append(Environment.getExternalStorageDirectory().toString()).append("/rfcx").toString(); 
	}
	
	public static String cacheFilesDir() {
		return (new StringBuilder()).append(Environment.getDownloadCacheDirectory().toString()).append("/rfcx").toString(); 
	}
	
	public static String captureDir(Context context) {
		return (new StringBuilder()).append(appFilesDir(context)).append("/capture").toString(); 
	}

	public static String encodeDir() {
		return (new StringBuilder()).append(cacheFilesDir()).append("/encode").toString(); 
	}
	
	public static String finalFilesDir() {
		String filesDir = (new StringBuilder()).append(cacheFilesDir()).append("/rfcx").toString(); 
		if ((new File(sdCardFilesDir())).isDirectory()) { filesDir = sdCardFilesDir(); }
		return filesDir;
	}

	public static String postZipDir() {
		return (new StringBuilder()).append(finalFilesDir()).append("/audio").toString(); 
	}
	
	public static String getAudioFileLocation_Capture(Context context, long timestamp, String fileExtension) {
		return (new StringBuilder()).append(captureDir(context)).append("/").append(timestamp).append(".").append(fileExtension).toString(); 
	}
	
	public static String getAudioFileLocation_PreEncode(long timestamp, String fileExtension) {
		return (new StringBuilder()).append(encodeDir()).append("/").append(timestamp).append(".").append(fileExtension).toString(); 
	}
	
	public static String getAudioFileLocation_PostEncode(long timestamp, String audioCodec) {
		return (new StringBuilder()).append(encodeDir()).append("/_").append(timestamp).append(".").append(getFileExtension(audioCodec)).toString(); 
	}

	public static String getAudioFileLocation_Complete_PostZip(long timestamp, String audioCodec) {
		return (new StringBuilder()).append(postZipDir()).append("/").append(dateFormat.format(new Date(timestamp))).append("/").append(timestamp).append(".").append(getFileExtension(audioCodec)).append(".gz").toString(); 
	}
	
	public static void purgeSingleAudioAssetFromDisk(String audioTimestamp, String audioFileExtension) {
		try {
			(new File(getAudioFileLocation_Complete_PostZip((long) Long.parseLong(audioTimestamp),audioFileExtension))).delete();
			Log.d(logTag, "Purging audio asset: "+audioTimestamp+"."+audioFileExtension);
		} catch (Exception e) {
			RfcxLog.logExc(logTag, e);
		}
	}
	
	public static String getFileExtension(String audioCodecOrFileExtension) {

		if 	(		audioCodecOrFileExtension.equalsIgnoreCase("opus")
				||	audioCodecOrFileExtension.equalsIgnoreCase("flac")
				||	audioCodecOrFileExtension.equalsIgnoreCase("mp3")
			) {
			
			return audioCodecOrFileExtension;
			
		} else if (	audioCodecOrFileExtension.equalsIgnoreCase("aac")
				||	audioCodecOrFileExtension.equalsIgnoreCase("m4a")
				) {
			
			return "m4a";
			
		} else {
			
			return "wav";
			
		}
	}
	
	public static boolean isEncodedWithVbr(String audioCodecOrFileExtension) {
		return (	audioCodecOrFileExtension.equalsIgnoreCase("opus")
				||	audioCodecOrFileExtension.equalsIgnoreCase("flac")
				||	audioCodecOrFileExtension.equalsIgnoreCase("mp3")
				);
	}
	
}
