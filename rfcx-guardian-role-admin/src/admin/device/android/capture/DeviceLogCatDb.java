package admin.device.android.capture;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rfcx.utility.database.DbUtils;
import rfcx.utility.rfcx.RfcxLog;
import rfcx.utility.rfcx.RfcxRole;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import admin.RfcxGuardian;

public class DeviceLogCatDb {
	
	public DeviceLogCatDb(Context context, String appVersion) {
		this.VERSION = RfcxRole.getRoleVersionValue(appVersion);
		this.dbCaptured = new DbCaptured(context);
	}

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, DeviceLogCatDb.class);
	private int VERSION = 1;
	static final String DATABASE = "logs";
	static final String C_CREATED_AT = "created_at";
	static final String C_TIMESTAMP = "timestamp";
	static final String C_FORMAT = "format";
	static final String C_DIGEST = "digest";
	static final String C_FILEPATH = "filepath";
	static final String C_LAST_ACCESSED_AT = "last_accessed_at";
	private static final String[] ALL_COLUMNS = new String[] { C_CREATED_AT, C_TIMESTAMP, C_FORMAT, C_DIGEST, C_FILEPATH, C_LAST_ACCESSED_AT };
	
	private String createColumnString(String tableName) {
		StringBuilder sbOut = new StringBuilder();
		sbOut.append("CREATE TABLE ").append(tableName)
			.append("(").append(C_CREATED_AT).append(" INTEGER")
			.append(", ").append(C_TIMESTAMP).append(" TEXT")
			.append(", ").append(C_FORMAT).append(" TEXT")
			.append(", ").append(C_DIGEST).append(" TEXT")
			.append(", ").append(C_FILEPATH).append(" TEXT")
			.append(", ").append(C_LAST_ACCESSED_AT).append(" INTEGER")
			.append(")");
		return sbOut.toString();
	}
	
	public class DbCaptured {

		final DbUtils dbUtils;

		private String TABLE = "captured";
		
		public DbCaptured(Context context) {
			this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE));
		}
		
		public int insert(String timestamp, String format, String digest, String filepath) {
			
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, (new Date()).getTime());
			values.put(C_TIMESTAMP, timestamp);
			values.put(C_FORMAT, format);
			values.put(C_DIGEST, digest);
			values.put(C_FILEPATH, filepath);
			values.put(C_LAST_ACCESSED_AT, 0);
			
			return this.dbUtils.insertRow(TABLE, values);
		}
		
		public JSONArray getLatestRowAsJsonArray() {
			return this.dbUtils.getRowsAsJsonArray(TABLE, ALL_COLUMNS, null, null, null);
		}
		
		public int deleteSingleRowByTimestamp(String timestamp) {
			String timestampValue = timestamp.contains(".") ? timestamp.substring(0, timestamp.lastIndexOf(".")) : timestamp;
			this.dbUtils.deleteRowsWithinQueryByTimestamp(TABLE, C_TIMESTAMP, timestampValue);
			return 0;
		}
		
		public long updateLastAccessedAtByTimestamp(String timestamp) {
			String timestampValue = timestamp.contains(".") ? timestamp.substring(0, timestamp.lastIndexOf(".")) : timestamp;
			long rightNow = (new Date()).getTime();
			this.dbUtils.setDatetimeColumnValuesWithinQueryByTimestamp(TABLE, C_LAST_ACCESSED_AT, rightNow, C_TIMESTAMP, timestampValue);
			return rightNow;
		}

	}
	public final DbCaptured dbCaptured;
	
}
