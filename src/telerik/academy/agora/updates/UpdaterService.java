package telerik.academy.agora.updates;

import telerik.academy.agora.AgoraApplication;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {

	public static final String NEW_STATUS_INTENT = "telerik.academy.agora.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";

	private static final String TAG = "UpdaterService";

	private static final int DELAY = 60000; // wait a minute
	private boolean runFlag = false;
	private Updater updater;
	private AgoraApplication agora;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		this.agora = (AgoraApplication) getApplication();
		this.updater = new Updater();

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

		static final String RECEIVE_TIMELINE_NOTIFICATIONS = "telerik.academy.agora.RECEIVE_TIMELINE_NOTIFICATIONS";
		Intent intent;

		public Updater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;
			while (updaterService.runFlag) {
				Log.d(TAG, "Running background thread");
				try {
					AgoraApplication agora = (AgoraApplication) updaterService
							.getApplication();
					int newUpdates = agora.fetchStatusUpdates();
					if (newUpdates > 0) {
						Log.d(TAG, "We have a new status");
						intent = new Intent(NEW_STATUS_INTENT);
						intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
						updaterService.sendBroadcast(intent,
								RECEIVE_TIMELINE_NOTIFICATIONS);
					}
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	} // Updater
}
