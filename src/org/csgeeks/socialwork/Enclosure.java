/*
 * Copyright (C) 2010-2011 Mathieu Favez - http://mfavez.com
 *
 *
 * This file is part of FeedGoal.
 * 
 * FeedGoal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeedGoal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FeedGoal.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.csgeeks.socialwork;

import java.net.URL;

import android.content.ContentValues;

/**
 * A class for creating and managing item enclosures.
 * @author Mathieu Favez
 * Created 29/06/2010
 */
public class Enclosure {
	private long mId = -1;
	private String mMime;
	private URL mURL;
	
	public Enclosure() {}
	
	public Enclosure(long id, String mime, URL url) {
		this.mId = id;
		this.mMime = mime;
		this.mURL = url;
	}
	
	public void setId(long id) {
		this.mId = id;
	}
	
	public long getId() {
		return mId;
	}
	
	public void setMime(String mime) {
		this.mMime = mime;
	}
	
	public String getMime() {
		return this.mMime;
	}
	
	public void setURL(URL url) {
		this.mURL = url;
	}

	public URL getURL() {
		return this.mURL;
	}
	
	public String toString() {
		return "{ID=" + this.mId + " mime=" + this.mMime + " URL=" + this.mURL.toString() + "}";
	}

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.put(EnclosureTable._ID, mId);
		values.put(EnclosureTable.COLUMN_MIME, mMime);
		values.put(EnclosureTable.COLUMN_URL, mURL.toString());
		return values;
	}
	
}
