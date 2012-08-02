package org.csgeeks.socialwork;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.mfavez.android.feedgoal.FeedHandler;
import com.mfavez.android.feedgoal.common.Feed;
import com.mfavez.android.feedgoal.common.Item;
import com.mfavez.android.feedgoal.storage.DbFeedAdapter;

import android.app.TabActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

public class ReaderActivity extends TabActivity {
	private static final String LOG_TAG = "ReaderActivity";
	private DbFeedAdapter mDbFeedAdapter;
	private static final String TAB_CHANNEL_TAG = "tab_tag_channel";
	private boolean mIsOnline = true;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        mDbFeedAdapter = new DbFeedAdapter(this);
        mDbFeedAdapter.open();

        
        setContentView(R.layout.main);

        long feedId = mDbFeedAdapter.getFirstFeed().getId();

        Feed currentTabFeed = mDbFeedAdapter.getFeed(feedId);
        setTabs(TAB_CHANNEL_TAG, currentTabFeed.getTitle());

        getTabHost().setOnTabChangedListener(new OnTabChangeListener(){
        	@Override
        	public void onTabChanged(String tabId) {
        	    Feed currentTabFeed = mDbFeedAdapter.getFeed(mDbFeedAdapter.getFirstFeed().getId());
 	        	refreshFeed(currentTabFeed,false);
        	    setTabsBackgroundColor();
        	}
        });

	}

	@Override
    protected void onResume() {
    	super.onResume();
		fillListData(R.id.feedlist);
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mDbFeedAdapter.close();
    }
    
    private void setTabs(String activeTab, String title) {
    	getTabHost().addTab(getTabHost().newTabSpec(TAB_CHANNEL_TAG).setIndicator(title).setContent(R.id.feedlist));
    	getTabHost().setCurrentTabByTag(activeTab);
    	setTabsBackgroundColor();
    }

    // Set TabWidget color background
    private void setTabsBackgroundColor() {
    	for(int i=0;i<getTabHost().getTabWidget().getChildCount();i++) {
    		getTabHost().getTabWidget().getChildAt(i).setBackgroundColor(Color.DKGRAY); //unselected
        }
    	getTabHost().getTabWidget().getChildAt(getTabHost().getCurrentTab()).setBackgroundColor(Color.TRANSPARENT); // selected
    }
    
    private class FeedArrayAdapter extends ArrayAdapter<Item> {
    	private LayoutInflater mInflater;

    	public FeedArrayAdapter(Context context, int textViewResourceId, List<Item> objects) {
    		super(context, textViewResourceId, objects);
    		// Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
    	}
    	
    	@Override
    	public long getItemId(int position) {
    		return getItem(position).getId();
    	}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	int[] item_rows = {R.layout.channel_item_row_notselected_notfavorite, R.layout.channel_item_row_selected_notfavorite,R.layout.channel_item_row_notselected_favorite,R.layout.channel_item_row_selected_favorite,R.layout.fav_item_row_notselected_favorite,R.layout.fav_item_row_selected_favorite,};
        	int item_row = item_rows[0]; // Default initialization
        	
        	Item item = getItem(position);
            
        	View view = convertView;
        	// Always inflate view, in order to display correctly the 'read' and 'favorite' states of the items => to apply the right layout+style.
            //if (view == null) {
	            //LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	            if (item.isRead())
	            		item_row = item_rows[1];
	            else
	            		item_row = item_rows[0];
	            view = mInflater.inflate(item_row, null);
            //}
            
            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView channelView = (TextView) view.findViewById(R.id.channel); // only displayed in favorite view
            TextView pubdateView = (TextView) view.findViewById(R.id.pubdate);
            if (titleView != null)
            	titleView.setText(item.getTitle());
            if (channelView != null) {
            	Feed feed = mDbFeedAdapter.getFeed(mDbFeedAdapter.getItemFeedId(item.getId()));
            	if (feed != null)
            		channelView.setText(feed.getTitle());
            } if (pubdateView != null) {
            	//DateFormat df = new SimpleDateFormat(getResources().getText(R.string.pubdate_format_pattern);
            	//pubdateView.setText(df.format(item.getPubdate()));
            	CharSequence formattedPubdate = DateFormat.format(getResources().getText(R.string.pubdate_format_pattern), item.getPubdate());
            	pubdateView.setText(formattedPubdate);
            }
            
            return view;
        }
    }
    
    private void refreshFeed(Feed feed, boolean alwaysDisplayOfflineDialog) {
    		mIsOnline = true;
    		new UpdateFeedTask().execute(feed);
    }

    private class UpdateFeedTask extends AsyncTask<Feed, Void, Boolean> {
    	private long feedId = -1;
    	private long lastItemIdBeforeUpdate = -1;
    	
    	public UpdateFeedTask() {
    		super();
    	}
    	
        protected Boolean doInBackground(Feed...params) {
        	feedId = params[0].getId();
        	Item lastItem = mDbFeedAdapter.getLastItem(feedId);
        	if (lastItem != null)
        		lastItemIdBeforeUpdate = lastItem.getId();
        	
        	FeedHandler feedHandler = new FeedHandler(ReaderActivity.this);
        	
        	try {
	        	Feed handledFeed = feedHandler.handleFeed(params[0].getURL());
	
	        	handledFeed.setId(feedId);
	        	
	        	mDbFeedAdapter.updateFeed(handledFeed);
	        	//mDbFeedAdapter.updateFeed(handledFeed.getId(), mDbFeedAdapter.getUpdateContentValues(handledFeed), handledFeed.getItems());
	        	mDbFeedAdapter.cleanDbItems(feedId);
	
        	} catch (IOException ioe) {
        		Log.e(LOG_TAG,"",ioe);
        		return Boolean.valueOf(false);
            } catch (SAXException se) {
            	Log.e(LOG_TAG,"",se);
            	return Boolean.valueOf(false);
            } catch (ParserConfigurationException pce) {
            	Log.e(LOG_TAG,"",pce);
            	return Boolean.valueOf(false);
            }
            
            return Boolean.valueOf(true);
        }
        
        protected void onPostExecute(Boolean result) {				
        	fillListData(R.id.feedlist);
        	
        	long lastItemIdAfterUpdate = -1;
        	Item lastItem = mDbFeedAdapter.getLastItem(feedId);
        	if (lastItem != null)
        		lastItemIdAfterUpdate = lastItem.getId();
        	if (lastItemIdAfterUpdate > lastItemIdBeforeUpdate)
        		Toast.makeText(ReaderActivity.this, R.string.new_item_msg, Toast.LENGTH_LONG).show();
        	else
        		Toast.makeText(ReaderActivity.this, R.string.no_new_item_msg, Toast.LENGTH_LONG).show();
        }
    }
    private static class ChannelsArrayAdapter extends ArrayAdapter<Feed> {
    	private LayoutInflater mInflater;
    	
    	public ChannelsArrayAdapter(Context context, int textViewResourceId, List<Feed> objects) {
    		super(context, textViewResourceId, objects);
    		
    		// Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
    	}
    	
    	@Override
    	public long getItemId(int position) {
    		return getItem(position).getId();
    	}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	// A ViewHolder keeps references to children views to avoid unneccessary calls to findViewById() on each row.
            ViewHolder holder;
    
        	// When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied is null.
            if (convertView == null) {
            	convertView = mInflater.inflate(R.layout.channel, null);
	            
	            // Creates a ViewHolder and store references to the children views we want to bind data to.
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.button = (Button) convertView.findViewById(R.id.button_delete_channel);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView and Button.
                holder = (ViewHolder) convertView.getTag();
            }
          
            Feed feed = getItem(position);
            
            // Bind the data efficiently with the holder.
            holder.title.setText(feed.getTitle());
            holder.button.setTag(feed.getId());
            
            return convertView;
        }
        
        static class ViewHolder {
            TextView title;
            Button button;
        }
    }
 
    private List<Item> fillListData(int listResource) {
		ListView feedListView = (ListView)findViewById(listResource);
		
		List<Item> items = null;
        	items = mDbFeedAdapter.getItems(mDbFeedAdapter.getFirstFeed().getId(),1,20);

		FeedArrayAdapter arrayAdapter = new FeedArrayAdapter(this, R.id.title, items);
		feedListView.setAdapter(arrayAdapter);
		
		//feedListView.setSelection(0);
		
		return items;
    }
}
