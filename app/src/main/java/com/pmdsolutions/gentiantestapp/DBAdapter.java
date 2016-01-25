package com.pmdsolutions.gentiantestapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


// TO USE:
// Change the package (at top) to match your project.
// Search for "TODO", and make the appropriate changes.
public class DBAdapter {

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    public static final String KEY_TIMESTAMP = "time";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_Z = "z";
    public static final String KEY_REF = "ref";
    public static final String KEY_P1 = "p1";
    public static final String KEY_P2 = "p2";
    public static final String KEY_COUNT = "counter";
    public static final String KEY_BATT = "battery";


    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_TIMESTAMP = 1;
    public static final int COL_X = 2;
    public static final int COL_Y = 3;
    public static final int COL_Z = 4;
    public static final int COL_REF = 5;
    public static final int COL_P1 = 6;
    public static final int COL_P2 = 7;
    public static final int COL_COUNT = 8;
    public static final int COL_BATT = 9;


    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TIMESTAMP, KEY_X, KEY_Y, KEY_Z, KEY_REF, KEY_P1, KEY_P2, KEY_COUNT, KEY_BATT};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "LOGS";
    public static final String DATABASE_TABLE = "StreamLog";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "
			
			/*
			 * CHANGE 2:
			 */
                    // TODO: Place your fields here!
                    // + KEY_{...} + " {type} not null"
                    //	- Key is the column name you created above.
                    //	- {type} is one of: text, integer, real, blob
                    //		(http://www.sqlite.org/datatype3.html)
                    //  - "not null" means it is a required field (must be given a value).
                    // NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
                    + KEY_TIMESTAMP + " text not null, "
                    + KEY_X + " integer not null, "
                    + KEY_Y + " integer not null, "
                    + KEY_Z + " integer not null, "
                    + KEY_REF + " integer not null, "
                    + KEY_P1 + " integer not null, "
                    + KEY_P2 + " integer not null, "
                    + KEY_COUNT + " integer not null, "
                    + KEY_BATT + " integer not null"

                    // Rest  of creation:
                    + ");";

    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    public long getSize(){
        long numRows = DatabaseUtils.queryNumEntries(db, "StreamLog");
        Log.wtf("TABLE SIZE", "" + numRows);
        return numRows;
    }

    // Add a new set of values to the database.
    public long insertRow(String time, int x, int y, int z, int ref, int p1, int p2, int count, int batt) {
		/*r
		 * CHANGE 3:
		 */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TIMESTAMP, time);
        initialValues.put(KEY_X, x);
        initialValues.put(KEY_Y, y);
        initialValues.put(KEY_Z, z);
        initialValues.put(KEY_REF, ref);
        initialValues.put(KEY_P1, p1);
        initialValues.put(KEY_P2, p2);
        initialValues.put(KEY_COUNT, count);
        initialValues.put(KEY_BATT, batt);

        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String time, int x, int y, int z, int ref, int p1, int p2, int count, int batt) {
        String where = KEY_ROWID + "=" + rowId;

		/*
		 * CHANGE 4:
		 */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TIMESTAMP, time);
        newValues.put(KEY_X, x);
        newValues.put(KEY_Y, y);
        newValues.put(KEY_Z, z);
        newValues.put(KEY_REF, ref);
        newValues.put(KEY_P1, p1);
        newValues.put(KEY_P2, p2);
        newValues.put(KEY_COUNT, count);
        newValues.put(KEY_BATT, batt);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }



    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }
}
