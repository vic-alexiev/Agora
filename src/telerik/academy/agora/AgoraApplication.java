package telerik.academy.agora;

import java.util.List;

import telerik.academy.agora.database.StatusData;
import telerik.academy.agora.reader.RssItem;
import telerik.academy.agora.reader.RssReader;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.URLConnectionHttpClient;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class AgoraApplication extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = AgoraApplication.class.getSimpleName();
	public static final String LOCATION_PROVIDER_NONE = "NONE";
	public static final long INTERVAL_NEVER = 0;
	public Twitter twitter;
	private SharedPreferences prefs;
	public RssReader rssReader;
	private StatusData statusData;
	private boolean serviceRunning;
	private boolean inTimeline;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		this.statusData = new StatusData(this);
		Log.i(TAG, "Application started");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		this.statusData.close();
		Log.i(TAG, "Application terminated");
	}

	public synchronized void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		this.twitter = null;
	}

	public synchronized Twitter getTwitter() {
		if (this.twitter == null) {
			String username = this.prefs.getString("username", "");
			String password = this.prefs.getString("password", "");
			String apiRoot = prefs.getString("apiRoot",
					"http://yamba.marakana.com/api");
			if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)
					&& !TextUtils.isEmpty(apiRoot)) {
				this.twitter = new Twitter(username,
						new URLConnectionHttpClient(username, password));
				this.twitter.setAPIRootUrl(apiRoot);
			}
		}
		return this.twitter;
	}

	public synchronized RssReader getRssReader() {
		this.rssReader = new RssReader(
				"http://yamba.marakana.com/api/statuses/public_timeline.rss");
		return this.rssReader;
	}

	public synchronized SharedPreferences getPrefs() {
		return this.prefs;
	}

	public boolean startOnBoot() {
		return this.prefs.getBoolean("startOnBoot", false);
	}

	public StatusData getStatusData() {
		return this.statusData;
	}

	public boolean isServiceRunning() {
		return this.serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	public boolean isInTimeline() {
		return this.inTimeline;
	}

	public void setInTimeline(boolean inTimeline) {
		this.inTimeline = inTimeline;
	}

	public String getProvider() {
		return prefs.getString("provider", LOCATION_PROVIDER_NONE);
	}

	public long getInterval() {
		// For some reason storing interval as long doesn't work
		return Long.parseLong(prefs.getString("interval", "0"));
	}

	// Connects to the online service and puts the latest statuses into DB.
	// Returns the count of new statuses
	public synchronized int fetchStatusUpdates() {
		Log.d(TAG, "Fetching status updates");

		RssReader rssReader = this.getRssReader();

		try {
			List<RssItem> timeline = rssReader.getItems();
			long latestStatusCreatedAtTime = this.getStatusData()
					.getLatestStatusCreatedAtTime();
			int count = 0;
			ContentValues values = new ContentValues();
			for (RssItem item : timeline) {
				values.clear(); // sic
				values.put(StatusData.C_ID, item.getId());
				long createdAt = item.getCreatedAt();
				values.put(StatusData.C_CREATED_AT, createdAt);
				values.put(StatusData.C_TEXT, item.getText());
				values.put(StatusData.C_USER, item.getUsername());
				Log.d(TAG, "Got update with id " + item.getId() + ". Saving");
				this.getStatusData().insertOrIgnore(values);
				if (latestStatusCreatedAtTime < createdAt) {
					count++;
				}
			}

			Log.d(TAG, count > 0 ? "Got " + count + " status updates"
					: "No new status updates");
			return count;
		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch status updates", e);
			return 0;
		} catch (Exception e) {
			Log.e(TAG, "Failed to retrieve RSS Feed items", e);
			return 0;
		}
	}
}
