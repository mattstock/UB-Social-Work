package org.csgeeks.socialwork;

import android.content.Intent;
import android.os.Bundle;
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
		tv = (TextView) findViewById(R.id.item_description);
		tv.setText(item.getDescription());
		tv = (TextView) findViewById(R.id.item_title);
		tv.setText(item.getTitle());
	}	
}
