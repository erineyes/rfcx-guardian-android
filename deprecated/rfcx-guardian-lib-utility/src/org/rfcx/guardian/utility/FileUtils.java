package org.rfcx.guardian.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.rfcx.guardian.utility.rfcx.RfcxLog;

public class FileUtils {
	
	private static final String logTag = "Rfcx-Utils-"+FileUtils.class.getSimpleName();
	
	public static String sha1Hash(String filePath) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
		    FileInputStream fileInputStream = new FileInputStream(filePath);
			byte[] dataBytes = new byte[1024];
		    int nread = 0;
		    while ((nread = fileInputStream.read(dataBytes)) != -1) {
		    	messageDigest.update(dataBytes, 0, nread);
		    };
		    fileInputStream.close();
		    byte[] mdbytes = messageDigest.digest();
		    StringBuffer stringBuilder = new StringBuffer("");
		    for (int i = 0; i < mdbytes.length; i++) {
		    	stringBuilder.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
			return stringBuilder.toString();
		} catch (NoSuchAlgorithmException e) {
			RfcxLog.logExc(logTag, e);
		} catch (FileNotFoundException e) {
			RfcxLog.logExc(logTag, e);
		} catch (IOException e) {
			RfcxLog.logExc(logTag, e);
		} catch (Exception e) {
			RfcxLog.logExc(logTag, e);
		}
		return null;
	}
	
	public static int chmod(File file, int mode) {
		try {
			Class fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
			return (Integer) setPermissions.invoke(null, file.getAbsolutePath(), mode, -1, -1);
		} catch (ClassNotFoundException e) {
			RfcxLog.logExc(logTag, e);
		} catch (SecurityException e) {
			RfcxLog.logExc(logTag, e);
		} catch (NoSuchMethodException e) {
			RfcxLog.logExc(logTag, e);
		} catch (IllegalArgumentException e) {
			RfcxLog.logExc(logTag, e);
		} catch (IllegalAccessException e) {
			RfcxLog.logExc(logTag, e);
		} catch (InvocationTargetException e) {
			RfcxLog.logExc(logTag, e);
		} catch (Exception e) {
			RfcxLog.logExc(logTag, e);
		}
		return 0;
	}
	
	public static int chmod(String filePath, int mode) {
		return chmod(new File(filePath), mode);
	}
	
	public static void copy(File srcFile, File dstFile) throws IOException {
		
		(new File(dstFile.getAbsolutePath().substring(0,dstFile.getAbsolutePath().lastIndexOf("/")))).mkdirs();
		
	    InputStream inputStream = new FileInputStream(srcFile);
	    OutputStream outputStream = new FileOutputStream(dstFile);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = inputStream.read(buf)) > 0) {
	        outputStream.write(buf, 0, len);
	    }
	    inputStream.close();
	    outputStream.close();
	}
	
	
	public static boolean delete(String filePath, boolean recursive) {
		File file = new File(filePath);
		
		if (!file.exists()) { return true; }
		if (!recursive || !file.isDirectory()) { return file.delete(); }

		String[] list = file.list();
		for (int i = 0; i < list.length; i++) {
			if (!delete(filePath + File.separator + list[i], true))
				return false;
		}
		return file.delete();
	}
	
}
