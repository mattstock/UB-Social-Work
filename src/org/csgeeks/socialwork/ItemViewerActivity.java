/*
 * Copyright 2012 Matthew Stock - http://www.bexkat.com/
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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ItemViewerActivity extends SherlockFragmentActivity {
	ItemTable mItemTable;
	long mItemId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itemviewer);
		
		// Pull up the item to display
		Intent intent = getIntent();
		mItemId = intent.getLongExtra(ItemTable._ID, 0);

		mItemTable = new ItemTable(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Item item = mItemTable.getItem(mItemId);
		
		TextView tv = (TextView) findViewById(R.id.item_content);
		tv.setText(item.getContent());
		tv = (TextView) findViewById(R.id.item_pubdate);
		tv.setText(item.getPubdate().toString());
		tv = (TextView) findViewById(R.id.item_title);
		tv.setText(item.getTitle());
	}	
	
	public void onClick(View v) {
		Item item = mItemTable.getItem(mItemId);

		if (v.getId() == R.id.web_view) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toURI().toString()));
				startActivity(intent);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			finish();
		}
	}
}
