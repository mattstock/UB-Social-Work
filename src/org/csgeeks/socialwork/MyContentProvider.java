package org.csgeeks.socialwork;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyContentProvider extends ContentProvider {
	private DatabaseHelper database;
	private static final String TAG = "MyContentProvider";
	
	private static final int FEEDS = 10;
	private static final int FEED_ID = 20;
	private static final int ITEMS = 30;
	private static final int ITEM_ID = 40;
	private static final int LINK_ID = 50;

	private static final String AUTHORITY = "org.csgeeks.socialwork.contentprovider";
	private static final String FEED_BASE_PATH = "feeds";
	private static final String ITEM_BASE_PATH = "items";
	private static final String LINK_BASE_PATH = "itemsinfeed";
	public static final Uri FEED_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + FEED_BASE_PATH);
	public static final Uri ITEM_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + ITEM_BASE_PATH);
	public static final Uri LINK_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + LINK_BASE_PATH);
	public static final String FEED_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/feeds";
	public static final String FEED_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/feed";
	public static final String ITEM_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/items";
	public static final String ITEM_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/item";
	public static final String LINK_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/links";
	public static final String LINK_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/link";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, FEED_BASE_PATH, FEEDS);
		sURIMatcher.addURI(AUTHORITY, FEED_BASE_PATH + "/#", FEED_ID);
		sURIMatcher.addURI(AUTHORITY, ITEM_BASE_PATH, ITEMS);
		sURIMatcher.addURI(AUTHORITY, ITEM_BASE_PATH + "/#", ITEM_ID);
		sURIMatcher.addURI(AUTHORITY, LINK_BASE_PATH + "/#", LINK_ID);
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext());
		Log.d(TAG, "onCreate()");
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		Log.d(TAG, "query: " + uri.toString());
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case FEEDS:
			queryBuilder.setTables(FeedTable.TABLE_NAME);
			break;
		case FEED_ID:
			checkColumns(FeedTable.COLUMNS, projection);
			queryBuilder.setTables(FeedTable.TABLE_NAME);
			queryBuilder.appendWhere(FeedTable._ID + "=" + uri.getLastPathSegment());
			break;
		case ITEMS:
			queryBuilder.setTables(ItemTable.TABLE_NAME);
			break;
		case ITEM_ID:
			checkColumns(ItemTable.COLUMNS, projection);
			queryBuilder.setTables(ItemTable.TABLE_NAME);
			queryBuilder.appendWhere(ItemTable._ID + "=" + uri.getLastPathSegment());
			break;
		case LINK_ID:
			checkColumns(ItemTable.COLUMNS, projection);
			queryBuilder.setTables(ItemTable.TABLE_NAME);
			queryBuilder.appendWhere(ItemTable.COLUMN_FEED_ID + "=" + uri.getLastPathSegment());			
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	private void checkColumns(String[] available, String[] projection) {
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		Uri result;
		switch (uriType) {
		case FEEDS:
			id = sqlDB.insert(FeedTable.TABLE_NAME, null, values);
			result = Uri.parse(FEED_BASE_PATH + "/" + id);
			break;
		case ITEMS:
			id = sqlDB.insert(ItemTable.TABLE_NAME, null, values);
			result = Uri.parse(ITEM_BASE_PATH + "/" + id);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		String id;
		
		switch (uriType) {
		case FEEDS:
			rowsDeleted = sqlDB.delete(FeedTable.TABLE_NAME, selection,
					selectionArgs);
			break;
		case FEED_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection))
				rowsDeleted = sqlDB.delete(FeedTable.TABLE_NAME,
						FeedTable._ID + "=" + id, null);
			else
				rowsDeleted = sqlDB.delete(FeedTable.TABLE_NAME, FeedTable._ID + "=" + id
						+ " and " + selection, selectionArgs);
			break;
		case ITEMS:
			rowsDeleted = sqlDB.delete(ItemTable.TABLE_NAME, selection,
					selectionArgs);
			break;
		case ITEM_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection))
				rowsDeleted = sqlDB.delete(ItemTable.TABLE_NAME,
						ItemTable._ID + "=" + id, null);
			else
				rowsDeleted = sqlDB.delete(ItemTable.TABLE_NAME, ItemTable._ID + "=" + id
						+ " and " + selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		String id;
		switch (uriType) {
		case FEEDS:
			rowsUpdated = sqlDB.update(FeedTable.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case FEED_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection))
				rowsUpdated = sqlDB.update(FeedTable.TABLE_NAME, values, FeedTable._ID
						+ "=" + id, null);
			else
				rowsUpdated = sqlDB.update(FeedTable.TABLE_NAME, values, FeedTable._ID
						+ "=" + id + " and " + selection, selectionArgs);
			break;
		case ITEMS:
			rowsUpdated = sqlDB.update(ItemTable.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case ITEM_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection))
				rowsUpdated = sqlDB.update(ItemTable.TABLE_NAME, values, ItemTable._ID
						+ "=" + id, null);
			else
				rowsUpdated = sqlDB.update(ItemTable.TABLE_NAME, values, ItemTable._ID
						+ "=" + id + " and " + selection, selectionArgs);
		
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
}
