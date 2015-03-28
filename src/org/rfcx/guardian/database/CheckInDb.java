package org.rfcx.guardian.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.rfcx.guardian.utility.DateTimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class CheckInDb {

	private static final String NULL_EXC = "Exception thrown, but exception itself is null.";
	
	public CheckInDb(Context context) {
		this.dbQueued = new DbQueued(context);
	}
	
	private static final String TAG = "RfcxGuardian-"+CheckInDb.class.getSimpleName();
	public DateTimeUtils dateTimeUtils = new DateTimeUtils();
	static final int VERSION = 1;
	static final String DATABASE = "checkin";
	static final String C_CREATED_AT = "created_at";
	static final String C_AUDIO = "audio";
	static final String C_JSON = "json";
	static final String C_ATTEMPTS = "attempts";
	private static final String[] ALL_COLUMNS = new String[] { C_CREATED_AT, C_AUDIO, C_JSON, C_ATTEMPTS };
	
	private String createColumnString(String tableName) {
		StringBuilder sbOut = new StringBuilder();
		sbOut.append("CREATE TABLE ").append(tableName).append("(").append(C_CREATED_AT).append(" DATETIME");
		sbOut.append(", "+C_AUDIO+" TEXT");
		sbOut.append(", "+C_JSON+" TEXT");
		sbOut.append(", "+C_ATTEMPTS+" TEXT");
		return sbOut.append(")").toString();
	}
	
	public class DbQueued {
		private String TABLE = "queued";
		class DbHelper extends SQLiteOpenHelper {
			public DbHelper(Context context) {
				super(context, DATABASE+"-"+TABLE+".db", null, VERSION);
			}
			@Override
			public void onCreate(SQLiteDatabase db) {
				try {
					db.execSQL(createColumnString(TABLE));
				} catch (SQLException e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : NULL_EXC); }
			}
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				try { db.execSQL("DROP TABLE IF EXISTS " + TABLE); onCreate(db);
				} catch (SQLException e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : NULL_EXC); }
			}
		}
		final DbHelper dbHelper;
		public DbQueued(Context context) {
			this.dbHelper = new DbHelper(context);
		}
		public void close() {
			this.dbHelper.close();
		}
		public void insert(String audio, String json) {
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, dateTimeUtils.getDateTime());
			values.put(C_AUDIO, audio);
			values.put(C_JSON, json);
			values.put(C_ATTEMPTS, "0");
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try {
				db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			} finally {
				db.close();
			}
		}
		public List<String[]> getAllQueued() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			ArrayList<String[]> list = new ArrayList<String[]>();
			try { Cursor cursor = db.query(TABLE, ALL_COLUMNS, null, null, null, null, null, null);
				if (cursor.getCount() > 0) {
					try { if (cursor.moveToFirst()) { do { list.add(new String[] { cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3) });
					} while (cursor.moveToNext()); } } finally { cursor.close(); } }
			} catch (Exception e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : NULL_EXC); } finally { db.close(); }
			return list;
		}
		public String[] getLatestRow() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			String[] row = new String[] {null,null,null};
			try { 
				Cursor cursor = db.query(TABLE, ALL_COLUMNS, null, null, null, null, C_CREATED_AT+" DESC", "1");
				if (cursor.getCount() > 0) {
					try {
						if (cursor.moveToFirst()) { do { row = new String[] { cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3) };
					} while (cursor.moveToNext()); } } finally { cursor.close(); } }
			} catch (Exception e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : NULL_EXC); } finally { db.close(); }
			return row;
		}
		
		public void deleteSingleRow(String audioFile) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE "+C_AUDIO+"='"+audioFile+"'");
			} finally { db.close(); }
		}
		
		public void clearQueuedBefore(Date date) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE "+C_CREATED_AT+"<='"+dateTimeUtils.getDateTime(date)+"'");
			} finally { db.close(); }
		}
		
		public void incrementSingleRowAttempts(String audioFile) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("UPDATE "+TABLE+" SET "+C_ATTEMPTS+"=cast("+C_ATTEMPTS+" as INT)+1 WHERE "+C_AUDIO+"='"+audioFile+"'");
			} finally { db.close(); }
		}

	}
	public final DbQueued dbQueued;
	
}