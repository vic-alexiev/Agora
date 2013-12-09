package telerik.academy.agora.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 3;
	private static final String TAG = "DbHelper";
	private static final String DB_NAME = "timeline.db";

	public static final String TABLE = "timeline";
	public static final String C_TEXT = "txt";
	public static final String C_USER = "user";
	public static final String C_ID = BaseColumns._ID;
	public static final String C_CREATED_AT = "created_at";

	// Constructor
	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	// Called only once, first time the DB is created
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + " (" + C_ID
				+ " int primary key, " + C_CREATED_AT + " int, " + C_USER
				+ " text, " + C_TEXT + " text)";
		db.execSQL(sql);
		Log.d(TAG, "onCreated sql: " + sql);
	}

	// Called whenever newVersion != oldVersion
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Typically do ALTER TABLE statements, but...we're just in development,
		// so:
		db.execSQL("drop table if exists " + TABLE); // drops the old database
		Log.d(TAG, "onUpdated");
		onCreate(db); // run onCreate to get new database
	}
}
