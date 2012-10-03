package org.csgeeks.socialwork;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class ItemTable implements BaseColumns {
	public static final String TAG = "ItemTable";
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
	private ContentResolver mResolver;
	
	public ItemTable(ContentResolver resolver) {
		mResolver = resolver;
	}
	
	public ItemTable(Context context) {
		mResolver = context.getContentResolver();
	}

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

		public long addItem(Feed feed, Item item) {
			return addItem(feed.getId(), item);
		}

		public long addItem(long feedId, Item item) {
			ContentValues values = item.toContentValues();
			values.put(ItemTable.COLUMN_FEED_ID, feedId);
			return addItem(values, item.getEnclosures());
		}

		public long addItem(ContentValues values, List<Enclosure> enclosures) {
			Uri itemUri = mResolver.insert(
					MyContentProvider.ITEM_CONTENT_URI, values);
			Item item = getItem(itemUri);
			EnclosureTable et = new EnclosureTable(mResolver);
			
			if (enclosures != null && itemUri != null) {
				for (Enclosure enclosure : enclosures) {
					et.addEnclosure(item, enclosure);
				}
			}

			return item.getId();

		}
		
		private Item getItem(Uri itemUri) {
			// TODO Auto-generated method stub
			return null;
		}

		public Item getFirstItem(long feedId) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasItem(long feedId, Item item) {
			// TODO Auto-generated method stub
			return false;
		}

		public Item getLastItem(long feedId) {
			// TODO Auto-generated method stub
			return null;
		}

		public void cleanDbItems(long feedId) {
			// TODO Auto-generated method stub

		}

		public void updateItem(long itemId, ContentValues values) {
			// TODO Auto-generated method stub
			
		}

}
