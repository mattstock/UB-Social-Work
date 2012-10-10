package org.csgeeks.socialwork;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.csgeeks.socialwork.db.Feed;
import org.csgeeks.socialwork.db.FeedTable;
import org.csgeeks.socialwork.db.Item;
import org.csgeeks.socialwork.db.ItemTable;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UpdateFeeds extends AsyncTask<ArrayList<Feed>, Void, Boolean> {
	private static final String TAG = "UpdateFeeds";
	Context mCtx;
	
	public UpdateFeeds(Context context) {
		mCtx = context;
	}
	
	@Override
	protected Boolean doInBackground(ArrayList<Feed>... params) {
		long lastItemIdBeforeUpdate = -1;
		long lastItemIdAfterUpdate = -1;
		Boolean newitems = false;
		FeedTable ft = new FeedTable(mCtx);
		ItemTable it = new ItemTable(mCtx);

		for (Feed feed : params[0]) {
			long feedId = feed.getId();
			Item lastItem = it.getLastItem(feedId);
			if (lastItem != null)
				lastItemIdBeforeUpdate = lastItem.getId();

			FeedHandler feedHandler = new FeedHandler(mCtx);

			try {
				Feed handledFeed = feedHandler.handleFeed(feed.getURL());

				handledFeed.setId(feedId);

				ft.updateFeed(handledFeed);
				it.cleanDbItems(feedId);

			} catch (IOException ioe) {
				Log.e(TAG, "", ioe);
			} catch (SAXException se) {
				Log.e(TAG, "", se);
			} catch (ParserConfigurationException pce) {
				Log.e(TAG, "", pce);
			}

			lastItem = it.getLastItem(feedId);
			if (lastItem != null)
				lastItemIdAfterUpdate = lastItem.getId();
			if (lastItemIdAfterUpdate > lastItemIdBeforeUpdate)
				newitems = true;
		}
		return newitems;
	}
	
	protected void onPostExecute(Boolean newItems) {
		if (newItems)
			Toast.makeText(mCtx, "New items found", Toast.LENGTH_SHORT).show();
	}
}
