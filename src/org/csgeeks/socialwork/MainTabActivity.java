package org.csgeeks.socialwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.csgeeks.socialwork.MainActivity.UpdateFeeds;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MainTabActivity extends SherlockFragmentActivity {
	private static final String TAG = "MainTabActivity";
	private Context mCtx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCtx = this;

		final ActionBar actionBar = getSupportActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		MyTabListener tabListener = new MyTabListener();

		FeedTable ft = new FeedTable(this);
		for (Feed feed : ft.getEnabledFeeds()) {
			Tab tab = actionBar.newTab();
			tab.setText(feed.getTitle());
			tab.setTag(feed);
			tab.setTabListener(tabListener);
			actionBar.addTab(tab);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkFreshness();
	}

	private class MyTabListener implements ActionBar.TabListener {

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Feed feed = (Feed) tab.getTag();
			Log.d(TAG, "onTabSelected(" + tab.getText() + "): " + feed.getId());
			Fragment f;
			FragmentManager fm = getSupportFragmentManager();
			f = fm.findFragmentByTag((String) tab.getText());

			if (f == null) {
				Bundle args = new Bundle();
				f = new ItemListFragment();
				args.putLong("feedId", feed.getId());
				args.putString("title", feed.getTitle());
				f.setArguments(args);
				ft.add(android.R.id.content, f, (String) tab.getText());
			} else {
				if (f.isDetached())
					ft.attach(f);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			Feed feed = (Feed) tab.getTag();
			Log.d(TAG,
					"onTabUnselected(" + tab.getText() + "): " + feed.getId());
			Fragment f;
			FragmentManager fm = getSupportFragmentManager();
			f = fm.findFragmentByTag((String) tab.getText());
			if (f != null) {
				ft.detach(f);
			}
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			Feed feed = (Feed) tab.getTag();
			Log.d(TAG,
					"onTabReselected(" + tab.getText() + "): " + feed.getId());
			// TODO Auto-generated method stub

		}

	}

	public void checkFreshness() {
		Date now = new Date();
		ArrayList<Feed> oldFeeds = new ArrayList<Feed>();
		FeedTable ft = new FeedTable(this);

		for (Feed feed : ft.getEnabledFeeds())
			if (feed.getRefresh() == null)
				oldFeeds.add(feed);
			else if ((now.getTime() - feed.getRefresh().getTime()) > 30 * 60 * 1000)
				oldFeeds.add(feed);

		new UpdateFeeds().execute(oldFeeds);
	}

	// Check all of the active feeds to see if any of them have new elements. If
	// so, add them to the DB.
	public class UpdateFeeds extends AsyncTask<ArrayList<Feed>, Void, Boolean> {

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

				FeedHandler feedHandler = new FeedHandler(MainTabActivity.this);

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
	}

}
