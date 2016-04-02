package com.github.yeriomin.andtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.yeriomin.andtest.model.Test;
import com.github.yeriomin.andtest.model.TestResult;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper dbHelper;

    private static final String DATABASE_NAME = "AndTest.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TEST_RESULT = "testResult";

    public static final String COLUMN_HASH = "hash";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CORRECT_ANSWERS = "correctAnswers";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DbHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    static public DbHelper getDbHelper(Context context) {
        if (null == dbHelper) {
            dbHelper = new DbHelper(context);
        }
        return dbHelper;
    }

    static public void closeDbHelper() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TEST_RESULT_TABLE = "CREATE TABLE " + TABLE_TEST_RESULT +
                "(" +
                COLUMN_TIMESTAMP + " BIGINTEGER PRIMARY KEY," +
                COLUMN_HASH + " TEXT," +
                COLUMN_CORRECT_ANSWERS + " INTEGER" +
                ")";
        db.execSQL(CREATE_TEST_RESULT_TABLE);
        String INDEX_TEST_RESULT_TABLE = "CREATE INDEX mytest_id_idx ON " + TABLE_TEST_RESULT
                + "(" + COLUMN_HASH + ");";
        db.execSQL(INDEX_TEST_RESULT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEST_RESULT);
            onCreate(db);
        }
    }

    public void save(Test test) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HASH, test.md5());
        values.put(COLUMN_TIMESTAMP, test.getFinishedAt());
        values.put(COLUMN_CORRECT_ANSWERS, test.getCorrectCount());
        dbHelper.getWritableDatabase().insert(TABLE_TEST_RESULT, null, values);
    }

    public Cursor getCursor(String hash) {
        String[] columns = new String[] { "*", COLUMN_HASH + " AS _id" };
        String selection = COLUMN_HASH + "=?";
        String[] selectionArgs = new String[] {hash};
        String orderBy = COLUMN_TIMESTAMP + " DESC";
        return dbHelper.getReadableDatabase().query(TABLE_TEST_RESULT, columns, selection, selectionArgs, null, null, orderBy);
    }

    public ArrayList<TestResult> get(String hash) {
        Cursor cursor = getCursor(hash);
        if (cursor != null) {
            ArrayList<TestResult> results = getObjects(cursor);
            cursor.close();
            return results;
        }
        return null;
    }

    private ArrayList<TestResult> getObjects(Cursor cursor) {
        ArrayList<TestResult> result = new ArrayList<TestResult>();
        int indexHash = cursor.getColumnIndex(COLUMN_HASH);
        int indexTimestamp = cursor.getColumnIndex(COLUMN_TIMESTAMP);
        int indexCorrectAnswers = cursor.getColumnIndex(COLUMN_CORRECT_ANSWERS);
        TestResult object;
        while (cursor.moveToNext()) {
            object = new TestResult();
            object.setHash(cursor.getString(indexHash));
            object.setTimestamp(cursor.getLong(indexTimestamp));
            object.setCorrectAnswers(cursor.getInt(indexCorrectAnswers));
            result.add(object);
        }
        return result;
    }
}