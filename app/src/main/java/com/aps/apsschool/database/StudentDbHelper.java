package com.aps.apsschool.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StudentDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + StudentContract.StudentEntry.TABLE_NAME + " (" +
                    StudentContract.StudentEntry.COLUMN_NAME_ROLL_NO + " TEXT PRIMARY KEY," +
                    StudentContract.StudentEntry.COLUMN_NAME_PASSWORD + " TEXT," +
                    StudentContract.StudentEntry.COLUMN_NAME_MEDIUM + " TEXT," +
                    StudentContract.StudentEntry.COLUMN_NAME_CLASS + " TEXT," +
                    StudentContract.StudentEntry.COLUMN_NAME_SECTION + " TEXT," +
                    StudentContract.StudentEntry.COLUMN_NAME_NAME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StudentContract.StudentEntry.TABLE_NAME;
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StudentDetails.db";

    public StudentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
//        db.execSQL(SQL_DELETE_ENTRIES);
    }
}
