package telerik.academy.agora.updates;

import telerik.academy.agora.AgoraApplication;
import telerik.academy.agora.R;
import telerik.academy.agora.TimelineActivity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class UpdaterService extends IntentService {

	private static final String TAG = "UpdaterService";

	public static final String NEW_STATUS_INTENT = "telerik.academy.agora.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	static final String RECEIVE_TIMELINE_NOTIFICATIONS = "telerik.academy.agora.RECEIVE_TIMELINE_NOTIFICATIONS";

	private NotificationManager notificationManager;
	private Notification notification;

	public UpdaterService() {
		super(TAG);

		Log.d(TAG, "UpdaterService constructed");
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Intent intent;
		this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		this.notification = new Notification.Builder(this)
				.setContentTitle(getString(R.string.msgNotificationTitle))
				.setContentText(getString(R.string.msgNotificationMessage))
				.setSmallIcon(android.R.drawable.stat_notify_chat)
				.setWhen(System.currentTimeMillis()).build();

		Log.d(TAG, "onHandleIntent'ing");
		AgoraApplication agora = (AgoraApplication) getApplication();
		int newUpdates = agora.fetchStatusUpdates();
		if (newUpdates > 0) {
			Log.d(TAG, "We have a new status");
			intent = new Intent(NEW_STATUS_INTENT);
			intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
			sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
			sendTimelineNotification(newUpdates);
		}
	}

	/**
	 * Creates a notification in the notification bar telling user there are new
	 * messages
	 * 
	 * @param timelineUpdateCount
	 *            Number of new statuses
	 */
	private void sendTimelineNotification(int timelineUpdateCount) {
		Log.d(TAG, "sendTimelineNotification'ing");

		PendingIntent pendingIntent = PendingIntent.getActivity(this, -1,
				new Intent(this, TimelineActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		CharSequence notificationTitle = this
				.getText(R.string.msgNotificationTitle);
		CharSequence notificationSummary = this.getString(
				R.string.msgNotificationMessage, timelineUpdateCount);

		this.notification = new Notification.Builder(this)
				.setContentIntent(pendingIntent)
				.setContentTitle(notificationTitle)
				.setContentText(notificationSummary)
				.setSmallIcon(android.R.drawable.stat_notify_chat)
				.setWhen(System.currentTimeMillis()).build();

		this.notification.flags |= Notification.FLAG_AUTO_CANCEL;

		this.notificationManager.notify(0, this.notification);
		Log.d(TAG, "sendTimelineNotificationed");
	}
}
