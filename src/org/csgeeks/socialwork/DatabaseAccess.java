
package org.csgeeks.socialwork;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DatabaseAccess {
	private static final String TAG = "DatabaseAccess";
	final Context mCtx;

	public DatabaseAccess(Context ctx) {
		this.mCtx = ctx;
	}
	
	public long addFeed(Feed feed) {
		return addFeed(feed.toContentValues(), feed.getItems());
	}

	public long addFeed(ContentValues values, List<Item> items) {
		Uri feedUri = mCtx.getContentResolver().insert(
				MyContentProvider.FEED_CONTENT_URI, values);

		// TODO
/*		if (items != null && feedUri != null) {
			for (Item item : items) {
				addItem(feedUri, item);
			}
		}
*/
		return getFeed(feedUri).getId();
	}

	public Feed getFeed(Uri feedUri) {
		Feed feed = null;
		Cursor cursor = mCtx.getContentResolver().query(feedUri, null, null,
				null, null);

		try {
			if (cursor.moveToFirst()) {
				feed = new Feed();
				feed.setId(cursor.getLong(cursor.getColumnIndex(FeedTable._ID)));
				feed.setURL(new URL(cursor.getString(cursor
						.getColumnIndex(FeedTable.COLUMN_URL))));
				feed.setHomePage(new URL(cursor.getString(cursor
						.getColumnIndex(FeedTable.COLUMN_HOMEPAGE))));
				feed.setTitle(cursor.getString(cursor
						.getColumnIndex(FeedTable.COLUMN_TITLE)));
				feed.setDescription(cursor.getString(cursor
						.getColumnIndex(FeedTable.COLUMN_DESCRIPTION)));
				if (!cursor
						.isNull(cursor.getColumnIndex(FeedTable.COLUMN_TYPE)))
					feed.setType(cursor.getString(cursor
							.getColumnIndex(FeedTable.COLUMN_TYPE)));
				if (!cursor.isNull(cursor
						.getColumnIndex(FeedTable.COLUMN_REFRESH)))
					feed.setRefresh(new Date(cursor.getLong(cursor
							.getColumnIndex(FeedTable.COLUMN_REFRESH))));
				feed.setEnabled(cursor.getInt(cursor
						.getColumnIndex(FeedTable.COLUMN_ENABLE)));
// TODO				feed.setItems(getItems(feed.getId(), 1, -1));
			}
		} catch (MalformedURLException mue) {
			Log.e(TAG, "", mue);
		}

		if (cursor != null)
			cursor.close();

		return feed;
	}

	public Feed getFeed(long feedId) {
		return getFeed(Uri.parse(MyContentProvider.FEED_CONTENT_URI + "/"
				+ feedId));
	}

	public Feed getFirstFeed() {
		String[] projection = { FeedTable._ID };
		Cursor cursor = mCtx.getContentResolver().query(
				MyContentProvider.FEED_CONTENT_URI, projection, null, null,
				FeedTable._ID + DatabaseHelper.SORT_ASC);
		Feed firstFeed = null;
		
		if (cursor.moveToFirst())
			firstFeed = getFeed(cursor.getLong(cursor
					.getColumnIndex(FeedTable._ID)));
		
		if (cursor != null)
			cursor.close();
		return firstFeed;
	}

	public ContentValues getUpdateContentValues(Feed feed) {
		ContentValues values = new ContentValues();

		if (feed.getRefresh() == null)
			values.putNull(FeedTable.COLUMN_REFRESH);
		else
			values.put(FeedTable.COLUMN_REFRESH, feed.getRefresh().getTime());
		int state = DatabaseHelper.ON;
		if (!feed.isEnabled())
			state = DatabaseHelper.OFF;
		values.put(FeedTable.COLUMN_ENABLE, state);
		return values;
	}

	public boolean updateFeed(Feed feed) {
		return updateFeed(feed.getId(), getUpdateContentValues(feed),
				feed.getItems());
	}

	public boolean updateFeed(long feedId, ContentValues values,
			List<Item> items) {
		int changed = mCtx.getContentResolver().update(
				Uri.parse(MyContentProvider.FEED_CONTENT_URI + "/" + feedId),
				values, null, null);
/* TODO
		if (changed > 0 && items != null) {
			Item firstDbItem = getFirstItem(feedId);
			for (Item item : items) {
				if (!hasItem(feedId, item)) {
					if (firstDbItem == null)
						addItem(feedId, item); // Db is empty
					else {
						if (item.getPubdate().after(firstDbItem.getPubdate()))
							addItem(feedId, item);
					}
				}

			}
		}
*/
		return (changed > 0);
	}

	public ArrayList<Feed> getEnabledFeeds() {
		String[] projection = { FeedTable._ID };
		ArrayList<Feed> feeds = new ArrayList<Feed>();
		Cursor cursor = mCtx.getContentResolver().query(MyContentProvider.FEED_CONTENT_URI, projection,
				FeedTable.COLUMN_ENABLE + "=?", new String[] { Long.toString(DatabaseHelper.ON) },
				FeedTable._ID + DatabaseHelper.SORT_ASC);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Feed feed = getFeed(cursor.getLong(cursor.getColumnIndex(FeedTable._ID)));
			if (feed != null)
				feeds.add(feed);
			cursor.moveToNext();
		}
		if (cursor != null)
			cursor.close();
		return feeds;
	}

}