package de.danoeh.antennapod.storage;

import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MergeCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.danoeh.antennapod.AppConfig;
import de.danoeh.antennapod.feed.Chapter;
import de.danoeh.antennapod.feed.Feed;
import de.danoeh.antennapod.feed.FeedImage;
import de.danoeh.antennapod.feed.FeedItem;
import de.danoeh.antennapod.feed.FeedMedia;
import de.danoeh.antennapod.service.download.DownloadStatus;

/**
 * Implements methods for accessing the database
 * */
public class PodDBAdapter {
	private static final String TAG = "PodDBAdapter";
	private static final int DATABASE_VERSION = 8;
	private static final String DATABASE_NAME = "Antennapod.db";

	/** Maximum number of arguments for IN-operator. */
	public static final int IN_OPERATOR_MAXIMUM = 800;

	// ----------- Column indices
	// ----------- General indices
	public static final int KEY_ID_INDEX = 0;
	public static final int KEY_TITLE_INDEX = 1;
	public static final int KEY_FILE_URL_INDEX = 2;
	public static final int KEY_DOWNLOAD_URL_INDEX = 3;
	public static final int KEY_DOWNLOADED_INDEX = 4;
	public static final int KEY_LINK_INDEX = 5;
	public static final int KEY_DESCRIPTION_INDEX = 6;
	public static final int KEY_PAYMENT_LINK_INDEX = 7;
	// ----------- Feed indices
	public static final int KEY_LAST_UPDATE_INDEX = 8;
	public static final int KEY_LANGUAGE_INDEX = 9;
	public static final int KEY_AUTHOR_INDEX = 10;
	public static final int KEY_IMAGE_INDEX = 11;
	public static final int KEY_TYPE_INDEX = 12;
	public static final int KEY_FEED_IDENTIFIER_INDEX = 13;
	// ----------- FeedItem indices
	public static final int KEY_CONTENT_ENCODED_INDEX = 2;
	public static final int KEY_PUBDATE_INDEX = 3;
	public static final int KEY_READ_INDEX = 4;
	public static final int KEY_MEDIA_INDEX = 8;
	public static final int KEY_FEED_INDEX = 9;
	public static final int KEY_HAS_SIMPLECHAPTERS_INDEX = 10;
	public static final int KEY_ITEM_IDENTIFIER_INDEX = 11;
	// ---------- FeedMedia indices
	public static final int KEY_DURATION_INDEX = 1;
	public static final int KEY_POSITION_INDEX = 5;
	public static final int KEY_SIZE_INDEX = 6;
	public static final int KEY_MIME_TYPE_INDEX = 7;
	public static final int KEY_PLAYBACK_COMPLETION_DATE_INDEX = 8;
	// --------- Download log indices
	public static final int KEY_FEEDFILE_INDEX = 1;
	public static final int KEY_FEEDFILETYPE_INDEX = 2;
	public static final int KEY_REASON_INDEX = 3;
	public static final int KEY_SUCCESSFUL_INDEX = 4;
	public static final int KEY_COMPLETION_DATE_INDEX = 5;
	public static final int KEY_REASON_DETAILED_INDEX = 6;
	public static final int KEY_DOWNLOADSTATUS_TITLE_INDEX = 7;
	// --------- Queue indices
	public static final int KEY_FEEDITEM_INDEX = 1;
	public static final int KEY_QUEUE_FEED_INDEX = 2;
	// --------- Chapters indices
	public static final int KEY_CHAPTER_START_INDEX = 2;
	public static final int KEY_CHAPTER_FEEDITEM_INDEX = 3;
	public static final int KEY_CHAPTER_LINK_INDEX = 4;
	public static final int KEY_CHAPTER_TYPE_INDEX = 5;

	// Key-constants
	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_NAME = "name";
	public static final String KEY_LINK = "link";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_FILE_URL = "file_url";
	public static final String KEY_DOWNLOAD_URL = "download_url";
	public static final String KEY_PUBDATE = "pubDate";
	public static final String KEY_READ = "read";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_POSITION = "position";
	public static final String KEY_SIZE = "filesize";
	public static final String KEY_MIME_TYPE = "mime_type";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_FEED = "feed";
	public static final String KEY_MEDIA = "media";
	public static final String KEY_DOWNLOADED = "downloaded";
	public static final String KEY_LASTUPDATE = "last_update";
	public static final String KEY_FEEDFILE = "feedfile";
	public static final String KEY_REASON = "reason";
	public static final String KEY_SUCCESSFUL = "successful";
	public static final String KEY_FEEDFILETYPE = "feedfile_type";
	public static final String KEY_COMPLETION_DATE = "completion_date";
	public static final String KEY_FEEDITEM = "feeditem";
	public static final String KEY_CONTENT_ENCODED = "content_encoded";
	public static final String KEY_PAYMENT_LINK = "payment_link";
	public static final String KEY_START = "start";
	public static final String KEY_LANGUAGE = "language";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_HAS_CHAPTERS = "has_simple_chapters";
	public static final String KEY_TYPE = "type";
	public static final String KEY_ITEM_IDENTIFIER = "item_identifier";
	public static final String KEY_FEED_IDENTIFIER = "feed_identifier";
	public static final String KEY_REASON_DETAILED = "reason_detailed";
	public static final String KEY_DOWNLOADSTATUS_TITLE = "title";
	public static final String KEY_CHAPTER_TYPE = "type";
	public static final String KEY_PLAYBACK_COMPLETION_DATE = "playback_completion_date";

	// Table names
	public static final String TABLE_NAME_FEEDS = "Feeds";
	public static final String TABLE_NAME_FEED_ITEMS = "FeedItems";
	public static final String TABLE_NAME_FEED_IMAGES = "FeedImages";
	public static final String TABLE_NAME_FEED_MEDIA = "FeedMedia";
	public static final String TABLE_NAME_DOWNLOAD_LOG = "DownloadLog";
	public static final String TABLE_NAME_QUEUE = "Queue";
	public static final String TABLE_NAME_SIMPLECHAPTERS = "SimpleChapters";

	// SQL Statements for creating new tables
	private static final String TABLE_PRIMARY_KEY = KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT ,";

	private static final String CREATE_TABLE_FEEDS = "CREATE TABLE "
			+ TABLE_NAME_FEEDS + " (" + TABLE_PRIMARY_KEY + KEY_TITLE
			+ " TEXT," + KEY_FILE_URL + " TEXT," + KEY_DOWNLOAD_URL + " TEXT,"
			+ KEY_DOWNLOADED + " INTEGER," + KEY_LINK + " TEXT,"
			+ KEY_DESCRIPTION + " TEXT," + KEY_PAYMENT_LINK + " TEXT,"
			+ KEY_LASTUPDATE + " TEXT," + KEY_LANGUAGE + " TEXT," + KEY_AUTHOR
			+ " TEXT," + KEY_IMAGE + " INTEGER," + KEY_TYPE + " TEXT,"
			+ KEY_FEED_IDENTIFIER + " TEXT)";;

	private static final String CREATE_TABLE_FEED_ITEMS = "CREATE TABLE "
			+ TABLE_NAME_FEED_ITEMS + " (" + TABLE_PRIMARY_KEY + KEY_TITLE
			+ " TEXT," + KEY_CONTENT_ENCODED + " TEXT," + KEY_PUBDATE
			+ " INTEGER," + KEY_READ + " INTEGER," + KEY_LINK + " TEXT,"
			+ KEY_DESCRIPTION + " TEXT," + KEY_PAYMENT_LINK + " TEXT,"
			+ KEY_MEDIA + " INTEGER," + KEY_FEED + " INTEGER,"
			+ KEY_HAS_CHAPTERS + " INTEGER," + KEY_ITEM_IDENTIFIER + " TEXT)";

	private static final String CREATE_TABLE_FEED_IMAGES = "CREATE TABLE "
			+ TABLE_NAME_FEED_IMAGES + " (" + TABLE_PRIMARY_KEY + KEY_TITLE
			+ " TEXT," + KEY_FILE_URL + " TEXT," + KEY_DOWNLOAD_URL + " TEXT,"
			+ KEY_DOWNLOADED + " INTEGER)";

	private static final String CREATE_TABLE_FEED_MEDIA = "CREATE TABLE "
			+ TABLE_NAME_FEED_MEDIA + " (" + TABLE_PRIMARY_KEY + KEY_DURATION
			+ " INTEGER," + KEY_FILE_URL + " TEXT," + KEY_DOWNLOAD_URL
			+ " TEXT," + KEY_DOWNLOADED + " INTEGER," + KEY_POSITION
			+ " INTEGER," + KEY_SIZE + " INTEGER," + KEY_MIME_TYPE + " TEXT,"
			+ KEY_PLAYBACK_COMPLETION_DATE + " INTEGER)";

	private static final String CREATE_TABLE_DOWNLOAD_LOG = "CREATE TABLE "
			+ TABLE_NAME_DOWNLOAD_LOG + " (" + TABLE_PRIMARY_KEY + KEY_FEEDFILE
			+ " INTEGER," + KEY_FEEDFILETYPE + " INTEGER," + KEY_REASON
			+ " INTEGER," + KEY_SUCCESSFUL + " INTEGER," + KEY_COMPLETION_DATE
			+ " INTEGER," + KEY_REASON_DETAILED + " TEXT,"
			+ KEY_DOWNLOADSTATUS_TITLE + " TEXT)";

	private static final String CREATE_TABLE_QUEUE = "CREATE TABLE "
			+ TABLE_NAME_QUEUE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
			+ KEY_FEEDITEM + " INTEGER," + KEY_FEED + " INTEGER)";

	private static final String CREATE_TABLE_SIMPLECHAPTERS = "CREATE TABLE "
			+ TABLE_NAME_SIMPLECHAPTERS + " (" + TABLE_PRIMARY_KEY + KEY_TITLE
			+ " TEXT," + KEY_START + " INTEGER," + KEY_FEEDITEM + " INTEGER,"
			+ KEY_LINK + " TEXT," + KEY_CHAPTER_TYPE + " INTEGER)";

	private SQLiteDatabase db;
	private final Context context;
	private PodDBHelper helper;

	/**
	 * Select all columns from the feeditems-table except description and
	 * content-encoded.
	 */
	private static final String[] SEL_FI_SMALL = { KEY_ID, KEY_TITLE,
			KEY_PUBDATE, KEY_READ, KEY_LINK, KEY_PAYMENT_LINK, KEY_MEDIA,
			KEY_FEED, KEY_HAS_CHAPTERS, KEY_ITEM_IDENTIFIER };

	// column indices for SEL_FI_SMALL

	public static final int IDX_FI_SMALL_ID = 0;
	public static final int IDX_FI_SMALL_TITLE = 1;
	public static final int IDX_FI_SMALL_PUBDATE = 2;
	public static final int IDX_FI_SMALL_READ = 3;
	public static final int IDX_FI_SMALL_LINK = 4;
	public static final int IDX_FI_SMALL_PAYMENT_LINK = 5;
	public static final int IDX_FI_SMALL_MEDIA = 6;
	public static final int IDX_FI_SMALL_FEED = 7;
	public static final int IDX_FI_SMALL_HAS_CHAPTERS = 8;
	public static final int IDX_FI_SMALL_ITEM_IDENTIFIER = 9;

	/** Select id, description and content-encoded column from feeditems. */
	public static final String[] SEL_FI_EXTRA = { KEY_ID, KEY_DESCRIPTION,
			KEY_CONTENT_ENCODED, KEY_FEED };

	// column indices for SEL_FI_EXTRA

	public static final int IDX_FI_EXTRA_ID = 0;
	public static final int IDX_FI_EXTRA_DESCRIPTION = 1;
	public static final int IDX_FI_EXTRA_CONTENT_ENCODED = 2;
	public static final int IDX_FI_EXTRA_FEED = 3;

	public PodDBAdapter(Context c) {
		this.context = c;
		helper = new PodDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public PodDBAdapter open() {
		if (db == null || !db.isOpen() || db.isReadOnly()) {
			if (AppConfig.DEBUG)
				Log.d(TAG, "Opening DB");
			try {
				db = helper.getWritableDatabase();
			} catch (SQLException ex) {
				ex.printStackTrace();
				db = helper.getReadableDatabase();
			}
		}
		return this;
	}

	public void close() {
		if (AppConfig.DEBUG)
			Log.d(TAG, "Closing DB");
		db.close();
	}

	/**
	 * Inserts or updates a feed entry
	 * 
	 * @return the id of the entry
	 * */
	public long setFeed(Feed feed) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, feed.getTitle());
		values.put(KEY_LINK, feed.getLink());
		values.put(KEY_DESCRIPTION, feed.getDescription());
		values.put(KEY_PAYMENT_LINK, feed.getPaymentLink());
		values.put(KEY_AUTHOR, feed.getAuthor());
		values.put(KEY_LANGUAGE, feed.getLanguage());
		if (feed.getImage() != null) {
			if (feed.getImage().getId() == 0) {
				setImage(feed.getImage());
			}
			values.put(KEY_IMAGE, feed.getImage().getId());
		}

		values.put(KEY_FILE_URL, feed.getFile_url());
		values.put(KEY_DOWNLOAD_URL, feed.getDownload_url());
		values.put(KEY_DOWNLOADED, feed.isDownloaded());
		values.put(KEY_LASTUPDATE, feed.getLastUpdate().getTime());
		values.put(KEY_TYPE, feed.getType());
		values.put(KEY_FEED_IDENTIFIER, feed.getFeedIdentifier());
		if (feed.getId() == 0) {
			// Create new entry
			if (AppConfig.DEBUG)
				Log.d(this.toString(), "Inserting new Feed into db");
			feed.setId(db.insert(TABLE_NAME_FEEDS, null, values));
		} else {
			if (AppConfig.DEBUG)
				Log.d(this.toString(), "Updating existing Feed in db");
			db.update(TABLE_NAME_FEEDS, values, KEY_ID + "=?",
					new String[] { Long.toString(feed.getId()) });
		}
		return feed.getId();
	}

	/**
	 * Inserts or updates an image entry
	 * 
	 * @return the id of the entry
	 * */
	public long setImage(FeedImage image) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, image.getTitle());
		values.put(KEY_DOWNLOAD_URL, image.getDownload_url());
		values.put(KEY_DOWNLOADED, image.isDownloaded());
		values.put(KEY_FILE_URL, image.getFile_url());
		if (image.getId() == 0) {
			image.setId(db.insert(TABLE_NAME_FEED_IMAGES, null, values));
		} else {
			db.update(TABLE_NAME_FEED_IMAGES, values, KEY_ID + "=?",
					new String[] { String.valueOf(image.getId()) });
		}
		return image.getId();
	}

	/**
	 * Inserts or updates an image entry
	 * 
	 * @return the id of the entry
	 */
	public long setMedia(FeedMedia media) {
		ContentValues values = new ContentValues();
		values.put(KEY_DURATION, media.getDuration());
		values.put(KEY_POSITION, media.getPosition());
		values.put(KEY_SIZE, media.getSize());
		values.put(KEY_MIME_TYPE, media.getMime_type());
		values.put(KEY_DOWNLOAD_URL, media.getDownload_url());
		values.put(KEY_DOWNLOADED, media.isDownloaded());
		values.put(KEY_FILE_URL, media.getFile_url());
		if (media.getPlaybackCompletionDate() != null) {
			values.put(KEY_PLAYBACK_COMPLETION_DATE, media
					.getPlaybackCompletionDate().getTime());
		} else {
			values.put(KEY_PLAYBACK_COMPLETION_DATE, 0);
		}
		if (media.getId() == 0) {
			media.setId(db.insert(TABLE_NAME_FEED_MEDIA, null, values));
		} else {
			db.update(TABLE_NAME_FEED_MEDIA, values, KEY_ID + "=?",
					new String[] { String.valueOf(media.getId()) });
		}
		return media.getId();
	}

	/**
	 * Insert all FeedItems of a feed and the feed object itself in a single
	 * transaction
	 */
	public void setCompleteFeed(Feed feed) {
		db.beginTransaction();
		setFeed(feed);
		for (FeedItem item : feed.getItemsArray()) {
			setFeedItem(item);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void setFeedItemlist(List<FeedItem> items) {
		db.beginTransaction();
		for (FeedItem item : items) {
			setFeedItem(item);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public long setSingleFeedItem(FeedItem item) {
		db.beginTransaction();
		long result = setFeedItem(item);
		db.setTransactionSuccessful();
		db.endTransaction();
		return result;
	}

	/**
	 * Inserts or updates a feeditem entry
	 * 
	 * @return the id of the entry
	 */
	private long setFeedItem(FeedItem item) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, item.getTitle());
		values.put(KEY_LINK, item.getLink());
		if (item.getDescription() != null) {
			values.put(KEY_DESCRIPTION, item.getDescription());
		}
		if (item.getContentEncoded() != null) {
			values.put(KEY_CONTENT_ENCODED, item.getContentEncoded());
		}
		values.put(KEY_PUBDATE, item.getPubDate().getTime());
		values.put(KEY_PAYMENT_LINK, item.getPaymentLink());
		if (item.getMedia() != null) {
			if (item.getMedia().getId() == 0) {
				setMedia(item.getMedia());
			}
			values.put(KEY_MEDIA, item.getMedia().getId());
		}
		if (item.getFeed().getId() == 0) {
			setFeed(item.getFeed());
		}
		values.put(KEY_FEED, item.getFeed().getId());
		values.put(KEY_READ, item.isRead());
		values.put(KEY_HAS_CHAPTERS, item.getChapters() != null);
		values.put(KEY_ITEM_IDENTIFIER, item.getItemIdentifier());
		if (item.getId() == 0) {
			item.setId(db.insert(TABLE_NAME_FEED_ITEMS, null, values));
		} else {
			db.update(TABLE_NAME_FEED_ITEMS, values, KEY_ID + "=?",
					new String[] { String.valueOf(item.getId()) });
		}
		if (item.getChapters() != null) {
			setChapters(item);
		}
		return item.getId();
	}

	public void setFeedItemRead(boolean read, long itemId, long mediaId,
			boolean resetMediaPosition) {
		db.beginTransaction();
		ContentValues values = new ContentValues();

		values.put(KEY_READ, read);
		db.update(TABLE_NAME_FEED_ITEMS, values, "?=?", new String[] { KEY_ID,
				Long.toString(itemId) });

		if (resetMediaPosition) {
			values.clear();
			values.put(KEY_POSITION, 0);
			db.update(TABLE_NAME_FEED_MEDIA, values, "?=?", new String[] {
					KEY_ID, Long.toString(mediaId) });
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void setFeedItemRead(boolean read, long... itemIds) {
		db.beginTransaction();
		ContentValues values = new ContentValues();
		for (long id : itemIds) {
			values.clear();
			values.put(KEY_READ, read);
			db.update(TABLE_NAME_FEED_ITEMS, values, "?=?", new String[] {
					KEY_ID, Long.toString(id) });
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void setChapters(FeedItem item) {
		ContentValues values = new ContentValues();
		for (Chapter chapter : item.getChapters()) {
			values.put(KEY_TITLE, chapter.getTitle());
			values.put(KEY_START, chapter.getStart());
			values.put(KEY_FEEDITEM, item.getId());
			values.put(KEY_LINK, chapter.getLink());
			values.put(KEY_CHAPTER_TYPE, chapter.getChapterType());
			if (chapter.getId() == 0) {
				chapter.setId(db
						.insert(TABLE_NAME_SIMPLECHAPTERS, null, values));
			} else {
				db.update(TABLE_NAME_SIMPLECHAPTERS, values, KEY_ID + "=?",
						new String[] { String.valueOf(chapter.getId()) });
			}
		}
	}

	/**
	 * Inserts or updates a download status.
	 * */
	public long setDownloadStatus(DownloadStatus status) {
		ContentValues values = new ContentValues();
		values.put(KEY_FEEDFILE, status.getFeedfileId());
		values.put(KEY_FEEDFILETYPE, status.getFeedfileType());
		values.put(KEY_REASON, status.getReason());
		values.put(KEY_SUCCESSFUL, status.isSuccessful());
		values.put(KEY_COMPLETION_DATE, status.getCompletionDate().getTime());
		values.put(KEY_REASON_DETAILED, status.getReasonDetailed());
		values.put(KEY_DOWNLOADSTATUS_TITLE, status.getTitle());
		if (status.getId() == 0) {
			status.setId(db.insert(TABLE_NAME_DOWNLOAD_LOG, null, values));
		} else {
			db.update(TABLE_NAME_DOWNLOAD_LOG, values, KEY_ID + "=?",
					new String[] { String.valueOf(status.getId()) });
		}

		return status.getId();
	}

	public long getDownloadLogSize() {
		Cursor result = db.rawQuery("SELECT COUNT(?) AS ? FROM ?",
				new String[] { KEY_ID, KEY_ID, TABLE_NAME_DOWNLOAD_LOG });
		long count = result.getLong(KEY_ID_INDEX);
		result.close();
		return count;
	}

	public void removeDownloadLogItems(long count) {
		if (count > 0) {
			db.rawQuery("DELETE FROM ? ORDER BY ? ASC LIMIT ?",
					new String[] { TABLE_NAME_DOWNLOAD_LOG,
							KEY_COMPLETION_DATE, Long.toString(count) });
		}
	}

	public void setQueue(List<FeedItem> queue) {
		ContentValues values = new ContentValues();
		db.beginTransaction();
		db.delete(TABLE_NAME_QUEUE, null, null);
		for (int i = 0; i < queue.size(); i++) {
			FeedItem item = queue.get(i);
			values.put(KEY_ID, i);
			values.put(KEY_FEEDITEM, item.getId());
			values.put(KEY_FEED, item.getFeed().getId());
			db.insertWithOnConflict(TABLE_NAME_QUEUE, null, values,
					SQLiteDatabase.CONFLICT_REPLACE);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void clearQueue() {
		db.delete(TABLE_NAME_QUEUE, null, null);
	}

	public void removeFeedMedia(FeedMedia media) {
		db.delete(TABLE_NAME_FEED_MEDIA, KEY_ID + "=?",
				new String[] { String.valueOf(media.getId()) });
	}

	public void removeChaptersOfItem(FeedItem item) {
		db.delete(TABLE_NAME_SIMPLECHAPTERS, KEY_FEEDITEM + "=?",
				new String[] { String.valueOf(item.getId()) });
	}

	public void removeFeedImage(FeedImage image) {
		db.delete(TABLE_NAME_FEED_IMAGES, KEY_ID + "=?",
				new String[] { String.valueOf(image.getId()) });
	}

	/** Remove a FeedItem and its FeedMedia entry. */
	public void removeFeedItem(FeedItem item) {
		if (item.getMedia() != null) {
			removeFeedMedia(item.getMedia());
		}
		if (item.getChapters() != null) {
			removeChaptersOfItem(item);
		}
		db.delete(TABLE_NAME_FEED_ITEMS, KEY_ID + "=?",
				new String[] { String.valueOf(item.getId()) });
	}

	/** Remove a feed with all its FeedItems and Media entries. */
	public void removeFeed(Feed feed) {
		db.beginTransaction();
		if (feed.getImage() != null) {
			removeFeedImage(feed.getImage());
		}
		for (FeedItem item : feed.getItemsArray()) {
			removeFeedItem(item);
		}
		db.delete(TABLE_NAME_FEEDS, KEY_ID + "=?",
				new String[] { String.valueOf(feed.getId()) });
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void removeDownloadStatus(DownloadStatus remove) {
		db.delete(TABLE_NAME_DOWNLOAD_LOG, KEY_ID + "=?",
				new String[] { String.valueOf(remove.getId()) });
	}

	public void clearPlaybackHistory() {
		ContentValues values = new ContentValues();
		values.put(KEY_PLAYBACK_COMPLETION_DATE, 0);
		db.update(TABLE_NAME_FEED_MEDIA, values, null, null);
	}

	/**
	 * Get all Feeds from the Feed Table.
	 * 
	 * @return The cursor of the query
	 * */
	public final Cursor getAllFeedsCursor() {
		open();
		Cursor c = db.query(TABLE_NAME_FEEDS, null, null, null, null, null,
				null);
		return c;
	}

	public final Cursor getExpiredFeedsCursor(long expirationTime) {
		open();
		Cursor c = db.query(TABLE_NAME_FEEDS, null, "?<?", new String[] {
				KEY_LASTUPDATE, Long.toString(expirationTime) }, null, null,
				null);
		return c;
	}

	/**
	 * Returns a cursor with all FeedItems of a Feed. Uses SEL_FI_SMALL
	 * 
	 * @param feed
	 *            The feed you want to get the FeedItems from.
	 * @return The cursor of the query
	 * */
	public final Cursor getAllItemsOfFeedCursor(final Feed feed) {
		return getAllItemsOfFeedCursor(feed.getId());
	}

	public final Cursor getAllItemsOfFeedCursor(final long feedId) {
		open();
		Cursor c = db.query(TABLE_NAME_FEED_ITEMS, SEL_FI_SMALL, KEY_FEED
				+ "=?", new String[] { String.valueOf(feedId) }, null, null,
				null);
		return c;
	}

	/** Return a cursor with the SEL_FI_EXTRA selection of a single feeditem. */
	public final Cursor getExtraInformationOfItem(final FeedItem item) {
		open();
		Cursor c = db
				.query(TABLE_NAME_FEED_ITEMS, SEL_FI_EXTRA, KEY_ID + "=?",
						new String[] { String.valueOf(item.getId()) }, null,
						null, null);
		return c;
	}

	/**
	 * Returns a cursor for a DB query in the FeedMedia table for a given ID.
	 * 
	 * @param item
	 *            The item you want to get the FeedMedia from
	 * @return The cursor of the query
	 * */
	public final Cursor getFeedMediaOfItemCursor(final FeedItem item) {
		open();
		Cursor c = db.query(TABLE_NAME_FEED_MEDIA, null, KEY_ID + "=?",
				new String[] { String.valueOf(item.getMedia().getId()) }, null,
				null, null);
		return c;
	}

	/**
	 * Returns a cursor for a DB query in the FeedImages table for a given ID.
	 * 
	 * @param id
	 *            ID of the FeedImage
	 * @return The cursor of the query
	 * */
	public final Cursor getImageOfFeedCursor(final long id) {
		open();
		Cursor c = db.query(TABLE_NAME_FEED_IMAGES, null, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null);
		return c;
	}

	public final Cursor getSimpleChaptersOfFeedItemCursor(final FeedItem item) {
		open();
		Cursor c = db.query(TABLE_NAME_SIMPLECHAPTERS, null, KEY_FEEDITEM
				+ "=?", new String[] { String.valueOf(item.getId()) }, null,
				null, null);
		return c;
	}

	public final Cursor getDownloadLogCursor() {
		open();
		Cursor c = db.query(TABLE_NAME_DOWNLOAD_LOG, null, null, null, null,
				null, null);
		return c;
	}

	/**
	 * Returns a cursor which contains all feed items in the queue. The returned
	 * cursor uses the SEL_FI_SMALL selection.
	 */
	public final Cursor getQueueCursor() {
		open();
		Cursor c = db.query(TABLE_NAME_FEED_ITEMS, SEL_FI_SMALL,
				"INNER JOIN ? ON ?=?", new String[] { TABLE_NAME_QUEUE,
						TABLE_NAME_FEED_ITEMS + "." + KEY_ID,
						TABLE_NAME_QUEUE + "." + KEY_FEEDITEM }, null, null,
				TABLE_NAME_QUEUE + "." + KEY_FEEDITEM);
		return c;
	}

	/**
	 * Returns a cursor which contains all feed items in the unread items list.
	 * The returned cursor uses the SEL_FI_SMALL selection.
	 */
	public final Cursor getUnreadItemsCursor() {
		open();
		Cursor c = db.query(TABLE_NAME_FEED_ITEMS, SEL_FI_SMALL, KEY_READ
				+ "=0", null, null, null, KEY_PUBDATE + " DESC");
		return c;
	}

	public final Cursor getUnreadItemIdsCursor() {
		open();
		Cursor c = db.query(TABLE_NAME_FEED_ITEMS, new String[] { KEY_ID },
				KEY_READ + "=0", null, null, null, KEY_PUBDATE + " DESC");
		return c;

	}

	public Cursor getDownloadedItemsCursor() {
		open();
		Cursor c = db.rawQuery("SELECT ? FROM " + TABLE_NAME_FEED_ITEMS
				+ "FULL JOIN " + TABLE_NAME_FEED_MEDIA + " ON "
				+ TABLE_NAME_FEED_ITEMS + "." + KEY_ID + "="
				+ TABLE_NAME_FEED_MEDIA + "." + KEY_ID + " WHERE "
				+ TABLE_NAME_FEED_MEDIA + "." + KEY_DOWNLOADED + ">0",
				SEL_FI_SMALL);
		return c;
	}

	/**
	 * Returns a cursor which contains feed media objects with a playback
	 * completion date in descending order.
	 * 
	 * @param limit
	 *            The maximum row count of the returned cursor. Must be an
	 *            integer >= 0.
	 * @throws IllegalArgumentException
	 *             if limit < 0
	 */
	public final Cursor getCompletedMediaCursor(int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException("Limit must be >= 0");
		}
		open();
		Cursor c = db.query(CREATE_TABLE_FEED_MEDIA, null,
				KEY_PLAYBACK_COMPLETION_DATE + " IS NOT NULL", null, null,
				null, KEY_PLAYBACK_COMPLETION_DATE + " DESC LIMIT " + limit);
		return c;
	}

	public final Cursor getFeedMediaCursor(String... mediaIds) {
		int length = mediaIds.length;
		if (length > IN_OPERATOR_MAXIMUM) {
			Log.w(TAG, "Length of id array is larger than "
					+ IN_OPERATOR_MAXIMUM + ". Creating multiple cursors");
			int numCursors = (int) (((double) length) / (IN_OPERATOR_MAXIMUM)) + 1;
			Cursor[] cursors = new Cursor[numCursors];
			for (int i = 0; i < numCursors; i++) {
				int neededLength = 0;
				String[] parts = null;
				final int elementsLeft = length - i * IN_OPERATOR_MAXIMUM;

				if (elementsLeft >= IN_OPERATOR_MAXIMUM) {
					neededLength = IN_OPERATOR_MAXIMUM;
					parts = Arrays.copyOfRange(mediaIds, i
							* IN_OPERATOR_MAXIMUM, (i + 1)
							* IN_OPERATOR_MAXIMUM);
				} else {
					neededLength = elementsLeft;
					parts = Arrays.copyOfRange(mediaIds, i
							* IN_OPERATOR_MAXIMUM, (i * IN_OPERATOR_MAXIMUM)
							+ neededLength);
				}

				cursors[i] = db.rawQuery("SELECT * FROM "
						+ TABLE_NAME_FEED_MEDIA + " WHERE " + KEY_ID + " IN "
						+ buildInOperator(neededLength), parts);
			}
			return new MergeCursor(cursors);
		} else {
			return db.query(TABLE_NAME_FEED_MEDIA, null, KEY_ID + " IN "
					+ buildInOperator(length), mediaIds, null, null, null);
		}
	}

	/** Builds an IN-operator argument depending on the number of items. */
	private String buildInOperator(int size) {
		StringBuffer buffer = new StringBuffer("(");
		for (int i = 0; i <= size; i++) {
			buffer.append("?,");
		}
		buffer.append("?)");
		return buffer.toString();
	}

	public final Cursor getFeedCursor(final long id) {
		open();
		Cursor c = db.query(TABLE_NAME_FEEDS, null, KEY_ID + "=" + id, null,
				null, null, null);
		return c;
	}

	public final Cursor getFeedItemCursor(final String... ids) {
		if (ids.length > IN_OPERATOR_MAXIMUM) {
			throw new IllegalArgumentException(
					"number of IDs must not be larger than "
							+ IN_OPERATOR_MAXIMUM);
		}

		open();
		return db.query(TABLE_NAME_FEED_ITEMS, null, KEY_ID + " IN "
				+ buildInOperator(ids.length), ids, null, null, null);

	}

	public final int getNumberOfDownloadedEpisodes() {

		Cursor c = db.rawQuery(
				"SELECT COUNT(DISTINCT ?) AS count FROM ? WHERE ?>0",
				new String[] { KEY_ID, TABLE_NAME_FEED_MEDIA, KEY_DOWNLOADED });
		final int result = c.getInt(0);
		c.close();
		return result;
	}

	/**
	 * Uses DatabaseUtils to escape a search query and removes ' at the
	 * beginning and the end of the string returned by the escape method.
	 */
	private String prepareSearchQuery(String query) {
		StringBuilder builder = new StringBuilder();
		DatabaseUtils.appendEscapedSQLString(builder, query);
		builder.deleteCharAt(0);
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}

	/**
	 * Searches for the given query in the description of all items or the items
	 * of a specified feed.
	 * 
	 * @return A cursor with all search results in SEL_FI_EXTRA selection.
	 * */
	public Cursor searchItemDescriptions(Feed feed, String query) {
		if (feed != null) {
			// search items in specific feed
			return db.query(TABLE_NAME_FEED_ITEMS, SEL_FI_EXTRA, KEY_FEED
					+ "=? AND " + KEY_DESCRIPTION + " LIKE '%"
					+ prepareSearchQuery(query) + "%'",
					new String[] { String.valueOf(feed.getId()) }, null, null,
					null);
		} else {
			// search through all items
			return db.query(TABLE_NAME_FEED_ITEMS, SEL_FI_EXTRA,
					KEY_DESCRIPTION + " LIKE '%" + prepareSearchQuery(query)
							+ "%'", null, null, null, null);
		}
	}

	/**
	 * Searches for the given query in the content-encoded field of all items or
	 * the items of a specified feed.
	 * 
	 * @return A cursor with all search results in SEL_FI_EXTRA selection.
	 * */
	public Cursor searchItemContentEncoded(Feed feed, String query) {
		if (feed != null) {
			// search items in specific feed
			return db.query(TABLE_NAME_FEED_ITEMS, SEL_FI_EXTRA, KEY_FEED
					+ "=? AND " + KEY_CONTENT_ENCODED + " LIKE '%"
					+ prepareSearchQuery(query) + "%'",
					new String[] { String.valueOf(feed.getId()) }, null, null,
					null);
		} else {
			// search through all items
			return db.query(TABLE_NAME_FEED_ITEMS, SEL_FI_EXTRA,
					KEY_CONTENT_ENCODED + " LIKE '%"
							+ prepareSearchQuery(query) + "%'", null, null,
					null, null);
		}
	}

	/** Helper class for opening the Antennapod database. */
	private static class PodDBHelper extends SQLiteOpenHelper {
		/**
		 * Constructor.
		 * 
		 * @param context
		 *            Context to use
		 * @param name
		 *            Name of the database
		 * @param factory
		 *            to use for creating cursor objects
		 * @param version
		 *            number of the database
		 * */
		public PodDBHelper(final Context context, final String name,
				final CursorFactory factory, final int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_FEEDS);
			db.execSQL(CREATE_TABLE_FEED_ITEMS);
			db.execSQL(CREATE_TABLE_FEED_IMAGES);
			db.execSQL(CREATE_TABLE_FEED_MEDIA);
			db.execSQL(CREATE_TABLE_DOWNLOAD_LOG);
			db.execSQL(CREATE_TABLE_QUEUE);
			db.execSQL(CREATE_TABLE_SIMPLECHAPTERS);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			Log.w("DBAdapter", "Upgrading from version " + oldVersion + " to "
					+ newVersion + ".");
			if (oldVersion <= 1) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_FEEDS + " ADD COLUMN "
						+ KEY_TYPE + " TEXT");
			}
			if (oldVersion <= 2) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_SIMPLECHAPTERS
						+ " ADD COLUMN " + KEY_LINK + " TEXT");
			}
			if (oldVersion <= 3) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_FEED_ITEMS
						+ " ADD COLUMN " + KEY_ITEM_IDENTIFIER + " TEXT");
			}
			if (oldVersion <= 4) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_FEEDS + " ADD COLUMN "
						+ KEY_FEED_IDENTIFIER + " TEXT");
			}
			if (oldVersion <= 5) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_DOWNLOAD_LOG
						+ " ADD COLUMN " + KEY_REASON_DETAILED + " TEXT");
				db.execSQL("ALTER TABLE " + TABLE_NAME_DOWNLOAD_LOG
						+ " ADD COLUMN " + KEY_DOWNLOADSTATUS_TITLE + " TEXT");
			}
			if (oldVersion <= 6) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_SIMPLECHAPTERS
						+ " ADD COLUMN " + KEY_CHAPTER_TYPE + " INTEGER");
			}
			if (oldVersion <= 7) {
				db.execSQL("ALTER TABLE " + TABLE_NAME_FEED_MEDIA
						+ " ADD COLUMN " + KEY_PLAYBACK_COMPLETION_DATE
						+ " INTEGER");
			}
		}
	}

}
