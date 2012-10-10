package org.csgeeks.socialwork;

import java.util.ArrayList;
import java.util.Date;

import org.csgeeks.socialwork.db.Feed;
import org.csgeeks.socialwork.db.FeedTable;

import android.content.Context;
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

	@SuppressWarnings("unchecked")
	public void checkFreshness() {
		Date now = new Date();
		ArrayList<Feed> oldFeeds = new ArrayList<Feed>();
		FeedTable ft = new FeedTable(this);

		for (Feed feed : ft.getEnabledFeeds())
			if (feed.getRefresh() == null)
				oldFeeds.add(feed);
			else if ((now.getTime() - feed.getRefresh().getTime()) > 30 * 60 * 1000)
				oldFeeds.add(feed);

		new UpdateFeeds(mCtx).execute(oldFeeds);
	}
}
