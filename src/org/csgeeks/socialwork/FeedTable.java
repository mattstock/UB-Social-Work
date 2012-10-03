package org.csgeeks.socialwork;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class FeedTable implements BaseColumns {
	public static final String TABLE_NAME = "feeds";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_HOMEPAGE = "homepage";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_REFRESH = "refresh";
	public static final String COLUMN_ENABLE = "enable";
	public static final String[] COLUMNS = { COLUMN_URL, COLUMN_HOMEPAGE,
			COLUMN_DESCRIPTION, COLUMN_TITLE, COLUMN_TYPE,
			COLUMN_REFRESH, COLUMN_ENABLE, _ID };
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_URL + " TEXT NOT NULL," + COLUMN_HOMEPAGE + " TEXT NOT NULL," + COLUMN_TITLE + " TEXT NOT NULL," + COLUMN_DESCRIPTION + " TEXT," + COLUMN_TYPE + " TEXT," + COLUMN_REFRESH + " INTEGER," + COLUMN_ENABLE + " INTEGER NOT NULL);";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	
	public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(CREATE_TABLE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(Feed.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL(DROP_TABLE);
	    onCreate(database);
	  }


}
