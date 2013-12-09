package telerik.academy.agora;

import java.util.List;

import telerik.academy.agora.database.DbHelper;
import telerik.academy.agora.reader.RssItem;
import telerik.academy.agora.reader.RssReader;
import winterwell.jtwitter.TwitterException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {

	private static final String TAG = "UpdaterService";

	private static final int DELAY = 60000; // wait a minute
	private boolean runFlag = false;
	private Updater updater;
	private AgoraApplication agora;

	private DbHelper dbHelper;
	private SQLiteDatabase db;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		this.agora = (AgoraApplication) getApplication();
		this.updater = new Updater();

		this.dbHelper = new DbHelper(this);

		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flag, int startId) {
		if (!runFlag) {
			this.runFlag = true;
			this.updater.start();
			((AgoraApplication) super.getApplication()).setServiceRunning(true);
			Log.d(TAG, "onStarted");
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		this.agora.setServiceRunning(false);

		Log.d(TAG, "onDestroyed");
	}

	/**
	 * Thread that performs the actual update from the online service
	 */
	private class Updater extends Thread {
		List<RssItem> timeline;

		public Updater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;
			while (updaterService.runFlag) {
				Log.d(TAG, "Updater running");
				try {
					// Get the timeline from the cloud
					try {
						RssReader rssReader = agora.getRssReader();
						timeline = rssReader.getItems();

						// Open the database for writing
						db = dbHelper.getWritableDatabase();

						// Loop over the timeline and print it out
						ContentValues values = new ContentValues();
						// Loop over the timeline
						for (RssItem item : timeline) {
							// Insert into database
							values.clear();
							values.put(DbHelper.C_ID, item.getId());
							values.put(DbHelper.C_CREATED_AT,
									item.getCreatedAt());
							values.put(DbHelper.C_TEXT, item.getText());
							values.put(DbHelper.C_USER, item.getUsername());

							db.insertOrThrow(DbHelper.TABLE, null, values);
							Log.d(TAG, String.format("%s: %s",
									item.getUsername(), item.getText()));
						}
						// Close the database
						db.close();

					} catch (TwitterException e) {
						Log.e(TAG, "Failed to connect to twitter service", e);
					} catch (Exception e) {
						Log.e(TAG, "Failed to retrieve RSS Feed items", e);
					}

					Log.d(TAG, "Updater ran");
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	} // Updater
}
