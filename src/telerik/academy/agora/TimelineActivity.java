package telerik.academy.agora;

import telerik.academy.agora.database.StatusData;
import telerik.academy.agora.preferences.PrefsActivity;
import telerik.academy.agora.updates.UpdaterService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineActivity extends BaseActivity {

	static final String SEND_TIMELINE_NOTIFICATIONS = "telerik.academy.agora.SEND_TIMELINE_NOTIFICATIONS";
	Cursor cursor;
	ListView listTimeline;
	SimpleCursorAdapter adapter;
	static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER,
			StatusData.C_TEXT };
	static final int[] TO = { R.id.textCreatedAt, R.id.textUser, R.id.textText };
	TimelineReceiver receiver;
	IntentFilter filter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);

		// Check if preferences have been set
		if (agora.getPrefs().getString("username", null) == null) {
			startActivity(new Intent(this, PrefsActivity.class));
			Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG)
					.show();
		}

		// Find your views
		listTimeline = (ListView) findViewById(R.id.listTimeline);

		// Create the receiver
		receiver = new TimelineReceiver();
		filter = new IntentFilter( UpdaterService.NEW_STATUS_INTENT );
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Close the database
		cursor.close();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the receiver
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setup List
		this.setupList();

		// Register the receiver
		super.registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS,
				null);
	}

	// Responsible for fetching data and setting up the list and the adapter
	private void setupList() {
		// Get the data
		cursor = agora.getStatusData().getStatusUpdates();

		// Setup Adapter
		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO,
				0);
		adapter.setViewBinder(VIEW_BINDER);
		listTimeline.setAdapter(adapter);
	}

	// View binder constant to inject business logic for timestamp to relative
	// time conversion
	static final ViewBinder VIEW_BINDER = new ViewBinder() {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() != R.id.textCreatedAt)
				return false;

			// Update the created at text to relative time
			long timestamp = cursor.getLong(columnIndex);
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(
					view.getContext(), timestamp);
			((TextView) view).setText(relTime);
			return true;
		}
	};

	// Receiver to wake up when UpdaterService gets a new status
	// It refreshes the timeline list by requerying the cursor
	class TimelineReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			setupList();
			Log.d("TimelineReceiver", "onReceived");
		}
	}
}
