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

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class ItemListFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "ItemListFragment";
	private SimpleCursorAdapter mCursorAdapter;
	private SimpleDateFormat mFormat = new SimpleDateFormat(
			"EEEE, MMMM d, yyyy");
	private long mFeedId;
	private String mTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

		Bundle b = getArguments();
		if (b != null) {
			mFeedId = getArguments().getLong("feedId");
			mTitle = getArguments().getString("title");
		} else {
			mFeedId = 1;
			mTitle = "None";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
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
		Intent intent = null;

		Log.d(TAG, "Item click: " + id);
		ItemTable db = new ItemTable(getActivity());
		Item item = db.getItem(id);

		// Mark as read
		ContentValues values = new ContentValues();
		values.put(ItemTable.COLUMN_READ, DatabaseHelper.ON);
		db.updateItem(id, values);
		// If there is content, display in a new view.
		// If not, ask someone else to handle the display of the item.
		String content = item.getContent();
		if (content == null || content.length() < 10) {
			try {
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toURI().toString()));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			intent = new Intent(getActivity(), ItemViewerActivity.class);
			intent.putExtra(ItemTable._ID, id);
		}
		
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] { ItemTable._ID,
				ItemTable.COLUMN_TITLE, ItemTable.COLUMN_PUBDATE };
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				Uri.parse(MyContentProvider.FEEDLIST_CONTENT_URI + "/"
						+ mFeedId), projection, null, null,
				ItemTable.COLUMN_PUBDATE + DatabaseHelper.SORT_DESC);
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

	private class MyCursorAdapter extends SimpleCursorAdapter {
		Context mContext;
		Cursor mCursor;
		int[] mTo;
		String[] mFrom;

		public MyCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			mContext = context;
			mCursor = c;
			mFrom = from;
			mTo = to;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null)
				row = View.inflate(mContext,
						android.R.layout.simple_list_item_2, null);
			else
				row = convertView;

			if (mCursor != null) {
				mCursor.moveToPosition(position);

				for (int i = 0; i < mFrom.length; i++) {
					TextView tv = (TextView) row.findViewById(mTo[i]);
					tv.setText(mCursor.getString(mCursor.getColumnIndex(mFrom[i])));
				}
			}
			return row;
		}

	}
}