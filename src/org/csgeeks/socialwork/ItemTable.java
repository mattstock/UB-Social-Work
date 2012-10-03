package org.csgeeks.socialwork;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class ItemTable implements BaseColumns {
	public static final String TABLE_NAME = "items";
	public static final String COLUMN_FEED_ID = "feed_id";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_GUID = "guid";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description"; // not used
	public static final String COLUMN_CONTENT = "content";
	public static final String COLUMN_IMAGE = "image";
	public static final String COLUMN_PUBDATE = "pubdate";
	public static final String COLUMN_FAVORITE = "favorite";
	public static final String COLUMN_READ = "read";
	public static final String[] COLUMNS = { _ID, COLUMN_FEED_ID, COLUMN_LINK, COLUMN_GUID, COLUMN_TITLE,
		COLUMN_DESCRIPTION, COLUMN_CONTENT, COLUMN_IMAGE, COLUMN_PUBDATE, COLUMN_FAVORITE, COLUMN_READ };
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_FEED_ID + " INTEGER NOT NULL," + COLUMN_LINK + " TEXT NOT NULL," + COLUMN_GUID + " TEXT NOT NULL," + COLUMN_TITLE + " TEXT NOT NULL," + COLUMN_DESCRIPTION + " TEXT," + COLUMN_CONTENT + " TEXT," + COLUMN_IMAGE + " TEXT," + COLUMN_PUBDATE + " INTEGER NOT NULL," + COLUMN_FAVORITE + " INTEGER NOT NULL," + COLUMN_READ + " INTEGER NOT NULL);";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	
	public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(CREATE_TABLE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(Item.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL(DROP_TABLE);
	    onCreate(database);
	  }

}
