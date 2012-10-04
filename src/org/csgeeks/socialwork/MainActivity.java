package org.csgeeks.socialwork;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = "MainActivity";
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

	private void markItemRead(long itemId) {
		
	}
	
	public class FeedPagerAdapter extends FragmentPagerAdapter {
		ArrayList<Feed> mFeeds;

		public FeedPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO this might not work.
			mFeeds = (new FeedTable(mCtx)).getEnabledFeeds();
		}

		@Override
		public Fragment getItem(int position) {
			return ItemListFragment.getInstance(position, mFeeds.get(position)
					.getId(), mFeeds.get(position).getTitle());
		}

		@Override
		public int getCount() {
			return mFeeds.size();
		}
	}

	public static class ItemListFragment extends SherlockListFragment implements
			LoaderManager.LoaderCallbacks<Cursor> {
		Context mCtx;
		SimpleCursorAdapter mCursorAdapter;
		SimpleDateFormat mFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
		long mFeedId;
		int mNum;
		String mTitle;

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			mCtx = activity;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			Bundle b = getArguments();
			if (b != null) {
				mNum = getArguments().getInt("num");
				mFeedId = getArguments().getLong("feedId");
				mTitle = getArguments().getString("title");
			} else {
				mNum = 1;
				mFeedId = 1;
				mTitle = "None";
			}
			Log.d(TAG, "onCreate()" + mNum);
		}

		public static Fragment getInstance(int position, long feedId,
				String title) {
			Bundle args = new Bundle();
			ItemListFragment f = new ItemListFragment();

			Log.d(TAG, "getInstance()" + position + " " + feedId);
			args.putInt("num", position);
			args.putLong("feedId", feedId);
			args.putString("title", title);
			f.setArguments(args);
			return f;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Log.d(TAG, "onCreateView()" + mNum);
			View v = inflater.inflate(R.layout.fragment_pager_list, container,
					false);
			TextView tv = (TextView) v.findViewById(R.id.channel_name);
			tv.setText(mTitle);
			return v;
		}

		@Override
		public void onActivityCreated(Bundle saveInstanceState) {
			String[] from = new String[] { ItemTable.COLUMN_TITLE,
					ItemTable.COLUMN_PUBDATE };
			int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

			Log.d(TAG, "onActivityCreated()");
			super.onActivityCreated(saveInstanceState);

			mCursorAdapter = new SimpleCursorAdapter(getActivity(),
					android.R.layout.simple_list_item_2, null, from, to, 0);
			mCursorAdapter.setViewBinder(new ViewBinder() {

				@Override
				public boolean setViewValue(View v, Cursor c, int index) {
					if (index == 2) {
						String date = mFormat.format(new Date(Long.parseLong(c
								.getString(index))));
						TextView tv = (TextView) v;
						tv.setText(date);
						return true;
					}
					return false;
				}

			});
			setListAdapter(mCursorAdapter);
			getLoaderManager().initLoader(0, null, this);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Log.d(TAG, "Item click: " + id);
			// Mark as read
			ItemTable db = new ItemTable(mCtx);
			ContentValues values = new ContentValues();
			values.put(ItemTable.COLUMN_READ, DatabaseHelper.ON);
			db.updateItem(id, values);
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String[] projection = new String[] { ItemTable._ID,
					ItemTable.COLUMN_TITLE, ItemTable.COLUMN_PUBDATE };
			CursorLoader cursorLoader = new CursorLoader(getActivity(),
					Uri.parse(MyContentProvider.FEEDLIST_CONTENT_URI + "/"
							+ mFeedId), projection, null, null, ItemTable.COLUMN_PUBDATE + DatabaseHelper.SORT_DESC);
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			mCursorAdapter.swapCursor(data);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mCursorAdapter.swapCursor(null);
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
