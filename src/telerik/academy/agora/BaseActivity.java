package telerik.academy.agora;

import telerik.academy.agora.preferences.PrefsActivity;
import telerik.academy.agora.updates.UpdaterService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * The base activity with common features shared by TimelineActivity and
 * StatusActivity
 */
public class BaseActivity extends Activity {
	AgoraApplication agora;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		agora = (AgoraApplication) getApplication();
	}

	// Called only once first time menu is clicked on
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	// Called every time user clicks on a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemPrefs:
			startActivity(new Intent(this, PrefsActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.itemRefresh:
			startService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemPurge:
			((AgoraApplication) getApplication()).getStatusData().delete();
			Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG)
					.show();
			// Let the world know status has changed - Timeline cares
			sendBroadcast(new Intent(UpdaterService.NEW_STATUS_INTENT));
			break;
		case R.id.itemTimeline:
			startActivity(new Intent(this, TimelineActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(
					Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.itemStatus:
			startActivity(new Intent(this, StatusActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		}
		return true;
	}
}
