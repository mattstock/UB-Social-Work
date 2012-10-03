package org.csgeeks.socialwork;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class EnclosureTable implements BaseColumns {
	public static final String TABLE_NAME = "enclosures";
	public static final String COLUMN_ITEM_ID = "item_id";
	public static final String COLUMN_MIME = "mime";
	public static final String COLUMN_URL = "URL";
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_ITEM_ID + " INTEGER NOT NULL," + COLUMN_MIME + " TEXT NOT NULL," + COLUMN_URL + " TEXT NOT NULL);";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;	

	public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(CREATE_TABLE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(Enclosure.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL(DROP_TABLE);
	    onCreate(database);
	  }
}
