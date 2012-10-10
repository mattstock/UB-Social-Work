/*
 * Copyright 2012 Matthew Stock - http://www.bexkat.com/
 * Adapted from FeedGoal copyright 2010-2011 Mathieu Favez - http://mfavez.com
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FeedGoal.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.csgeeks.socialwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.csgeeks.socialwork.db.Feed;
import org.csgeeks.socialwork.db.FeedTable;
import org.csgeeks.socialwork.db.Item;
import org.csgeeks.socialwork.db.ItemTable;
import org.xml.sax.SAXException;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = "MainActivity";
	private static final int MENU_REFRESH = 0;
	private static final int MENU_READ = 1;
	private FeedPagerAdapter mAdapter;
	private ViewPager mPager;
	private Context mCtx;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCtx = this;
		mAdapter = new FeedPagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

	}

	@Override
	protected void onResume() {
		super.onResume();
		checkFreshness();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_REFRESH, 1, "Refresh").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(Menu.NONE, MENU_READ, 2, "Mark all as Read");
		
		return super.onCreateOptionsMenu(menu);
	}

	@SuppressWarnings("unchecked")
	public boolean onOptionsItemSelected(MenuItem item) {
		long feedId = mAdapter.getItem(mPager.getCurrentItem()).getArguments().getLong("feedId");
		FeedTable ft = new FeedTable(this);
		Feed f = ft.getFeed(feedId);

		Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + ", " + feedId);

		switch (item.getItemId()) {
		case MENU_REFRESH:
			ArrayList<Feed> array = new ArrayList<Feed>();
			array.add(f);
			new UpdateFeeds().execute(array);
			return true;
		case MENU_READ:
			ft.markAllAsRead(feedId);
			return true;
		}
		return false;
	}

	public class FeedPagerAdapter extends FragmentPagerAdapter {
		ArrayList<Feed> mFeeds;

		public FeedPagerAdapter(FragmentManager fm) {
			super(fm);
			mFeeds = (new FeedTable(mCtx)).getEnabledFeeds();
		}

		@Override
		public Fragment getItem(int position) {
			Bundle args = new Bundle();
			ItemListFragment f = new ItemListFragment();

			args.putLong("feedId", mFeeds.get(position).getId());
			args.putString("title", mFeeds.get(position).getTitle());
			f.setArguments(args);
			return f;
		}

		@Override
		public int getCount() {
			return mFeeds.size();
		}
	}

	@SuppressWarnings("unchecked")
	public void checkFreshness() {
		Date now = new Date();
		ArrayList<Feed> oldFeeds = new ArrayList<Feed>();
		FeedTable ft = new FeedTable(mCtx);

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

				FeedHandler feedHandler = new FeedHandler(MainActivity.this);

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
