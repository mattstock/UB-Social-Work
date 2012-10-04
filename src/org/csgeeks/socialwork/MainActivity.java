package org.csgeeks.socialwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;

public class MainActivity extends SherlockFragmentActivity {
	static final String TAG = "MainActivity";
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

			args.putInt("num", position);
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

	private void checkFreshness() {
		Date now = new Date();
		ArrayList<Feed> oldFeeds = new ArrayList<Feed>();
		FeedTable ft = new FeedTable(mCtx);
		
		for (Feed feed: ft.getEnabledFeeds())
			if (feed.getRefresh() == null)
				oldFeeds.add(feed);
			else
				if ((now.getTime() - feed.getRefresh().getTime()) > 30 * 60 * 1000)
					oldFeeds.add(feed);
		
		new UpdateFeeds().execute(oldFeeds);
	}
	
	// Check all of the active feeds to see if any of them have new elements.  If so, add them to the DB.
	private class UpdateFeeds extends AsyncTask<ArrayList<Feed>, Void, Boolean> {

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
