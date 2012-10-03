package org.csgeeks.socialwork;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import android.os.Bundle;
import android.database.Cursor;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = "MainActivity";
	private FeedPagerAdapter mAdapter;
	private ViewPager mPager;
	private DatabaseAccess mDb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDb = new DatabaseAccess(this);
		mAdapter = new FeedPagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
	}

	public class FeedPagerAdapter extends FragmentPagerAdapter {
		ArrayList<Feed> mFeeds;
		
		public FeedPagerAdapter(FragmentManager fm) {
			super(fm);
			mFeeds = mDb.getEnabledFeeds();
		}

		@Override
		public Fragment getItem(int position) {
			return ItemListFragment.getInstance(position, mFeeds.get(position).getId(), mFeeds.get(position).getTitle());
		}

		@Override
		public int getCount() {
			return mFeeds.size();
		}
	}

	public static class ItemListFragment extends SherlockListFragment implements
			LoaderManager.LoaderCallbacks<Cursor> {

		SimpleCursorAdapter mCursorAdapter;
		SimpleDateFormat mFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
		long mFeedId;
		int mNum;
		String mTitle;

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

		public static Fragment getInstance(int position, long feedId, String title) {
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
			View v = inflater.inflate(R.layout.fragment_pager_list, container, false);
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
						String date = mFormat.format(new Date(Long.parseLong(c.getString(index))));
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
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String[] projection = new String[] { ItemTable._ID,
					ItemTable.COLUMN_TITLE, ItemTable.COLUMN_PUBDATE };
			CursorLoader cursorLoader = new CursorLoader(getActivity(),
					Uri.parse(MyContentProvider.LINK_CONTENT_URI + "/" + mFeedId), projection, null, null,
					null);
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

}
