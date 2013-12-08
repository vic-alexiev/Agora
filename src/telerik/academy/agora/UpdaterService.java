package telerik.academy.agora;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Status;
import winterwell.jtwitter.TwitterException;
import winterwell.jtwitter.User;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	static final String TAG = "UpdaterService";

	static final int DELAY = 60000; // wait a minute
	private boolean runFlag = false;
	private Updater updater;
	private AgoraApplication agora;

	DbHelper dbHelper;
	SQLiteDatabase db;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		this.agora = (AgoraApplication) getApplication();
		this.updater = new Updater();

		dbHelper = new DbHelper(this);

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
		List<winterwell.jtwitter.Status> timeline;

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
						//timeline = agora.getTwitter().getHomeTimeline();
						timeline = getHomeTimeline();

						// Open the database for writing
						db = dbHelper.getWritableDatabase();

						// Loop over the timeline and print it out
						ContentValues values = new ContentValues();
						// Loop over the timeline
						for (winterwell.jtwitter.Status status : timeline) {
							// Insert into database
							values.clear();
							values.put(DbHelper.C_ID, status.id.intValue());
							values.put(DbHelper.C_CREATED_AT,
									status.createdAt.getTime());
							values.put(DbHelper.C_SOURCE, status.source);
							values.put(DbHelper.C_TEXT, status.text);
							values.put(DbHelper.C_USER, status.user.name);

							db.insertOrThrow(DbHelper.TABLE, null, values);
							Log.d(TAG, String.format("%s: %s",
									status.user.name, status.text));
						}
						// Close the database
						db.close();

					} catch (TwitterException e) {
						Log.e(TAG, "Failed to connect to twitter service", e);
					}

					Log.d(TAG, "Updater ran");
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}

		private List<winterwell.jtwitter.Status> getHomeTimeline() {
			List<winterwell.jtwitter.Status> timeline = new ArrayList<winterwell.jtwitter.Status>();
			timeline.add(new winterwell.jtwitter.Status(new User("steven"),
					"This is a test", 12345, new Date()));
			return timeline;
		}
	} // Updater
}
