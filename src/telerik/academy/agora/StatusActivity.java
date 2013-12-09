package telerik.academy.agora;

import telerik.academy.agora.preferences.PrefsActivity;
import winterwell.jtwitter.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends Activity implements OnClickListener,
		TextWatcher {

	private static final String TAG = "StatusActivity";
	private EditText editText;
	private Button updateButton;
	private TextView textCount;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);

		// Find views
		editText = (EditText) findViewById(R.id.editText);
		editText.addTextChangedListener(this);

		updateButton = (Button) findViewById(R.id.buttonUpdate);
		updateButton.setOnClickListener(this);

		textCount = (TextView) findViewById(R.id.textCount);
		textCount.setText(Integer.toString(140));
		textCount.setTextColor(Color.rgb(178, 168, 26)); // green
	}

	// Called first time user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemServiceStart:
			startService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemServiceStop:
			stopService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemPrefs:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		}
		return true;
	}

	// Asynchronously posts to twitter
	private class PostToTwitter extends AsyncTask<String, Integer, String> {

		// Called to initiate the background activity
		@Override
		protected String doInBackground(String... statuses) {
			try {
				AgoraApplication agora = ((AgoraApplication) getApplication());
				winterwell.jtwitter.Status status = agora.getTwitter()
						.updateStatus(statuses[0]);
				return status.text;
			} catch (TwitterException e) {
				Log.e(TAG, "Failed to connect to twitter service", e);
				return "Failed to post";
			}
		}

		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// Not used in this case
		}

		// Called once the background activity has completed
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG)
					.show();
		}
	}

	// Called when button is clicked
	public void onClick(View view) {

		// Update twitter status
		String status = editText.getText().toString();
		new PostToTwitter().execute(status);
		Log.d(TAG, "onClicked");
	}

	// TextWatcher methods
	public void afterTextChanged(Editable statusText) {
		int count = 140 - statusText.length();
		textCount.setText(Integer.toString(count));
		textCount.setTextColor(Color.rgb(178, 168, 26)); // green
		if (count < 10)
			textCount.setTextColor(Color.rgb(204, 191, 20)); // yellow
		if (count < 0)
			textCount.setTextColor(Color.rgb(255, 53, 110)); // red
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
