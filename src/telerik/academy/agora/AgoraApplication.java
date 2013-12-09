package telerik.academy.agora;

import telerik.academy.agora.reader.RssReader;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.URLConnectionHttpClient;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class AgoraApplication extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = AgoraApplication.class.getSimpleName();
	private boolean serviceRunning;
	public Twitter twitter;
	public RssReader rssReader;
	private SharedPreferences prefs;

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		Log.i(TAG, "onCreated");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
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
		this.rssReader = new RssReader("http://yamba.marakana.com/api/statuses/public_timeline.rss");
		return this.rssReader;
	}

	public synchronized void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		this.twitter = null;
	}
}
