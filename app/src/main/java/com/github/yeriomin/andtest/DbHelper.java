package com.github.yeriomin.andtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.yeriomin.andtest.core.Answer;
import com.github.yeriomin.andtest.model.TestState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper dbHelper;

    private static final String DATABASE_NAME = "AndTest.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TEST_STATE = "testState";
    public static final String TABLE_ANSWER = "answer";

    public static final String COLUMN_HASH = "hash";
    public static final String COLUMN_STARTED_AT = "startedAt";
    public static final String COLUMN_FINISHED_AT = "finishedAt";

    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_QUESTION_TYPE = "questionType";
    public static final String COLUMN_ANSWER = "answer";

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
        String sqlCreateState = "CREATE TABLE " + TABLE_TEST_STATE +
            "(" +
            COLUMN_HASH + " TEXT," +
            COLUMN_STARTED_AT + " INTEGER PRIMARY KEY," +
            COLUMN_FINISHED_AT + " INTEGER" +
            ")";
        String sqlIndexState = "CREATE INDEX index_" + TABLE_TEST_STATE + "_" + COLUMN_HASH
            + " ON " + TABLE_TEST_STATE + "(" + COLUMN_HASH + ");";
        String sqlCreateAnswer = "CREATE TABLE " + TABLE_ANSWER +
            "(" +
            COLUMN_HASH + " TEXT," +
            COLUMN_STARTED_AT + " INTEGER," +
            COLUMN_QUESTION + " INTEGER," +
            COLUMN_QUESTION_TYPE + " TEXT," +
            COLUMN_ANSWER + " TEXT," +
            "CONSTRAINT unique_s_q UNIQUE (" + COLUMN_STARTED_AT + ", " + COLUMN_QUESTION + ")" +
            ")";
        String sqlIndexAnswer = "CREATE INDEX index_" + TABLE_ANSWER + "_" + COLUMN_HASH
            + " ON " + TABLE_ANSWER + "(" + COLUMN_HASH + ");";

        db.beginTransaction();
        try {
            db.execSQL(sqlCreateState);
            db.execSQL(sqlIndexState);
            db.execSQL(sqlCreateAnswer);
            db.execSQL(sqlIndexAnswer);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEST_STATE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWER);
            onCreate(db);
        }
    }

    /**
     * Save the test general state
     * @param testState
     */
    public void save(TestState testState) {
        ContentValues valuesState = new ContentValues();
        valuesState.put(COLUMN_HASH, testState.getTestHash());
        valuesState.put(COLUMN_STARTED_AT, testState.getStartedAt());
        valuesState.put(COLUMN_FINISHED_AT, testState.getFinishedAt());
        dbHelper.getWritableDatabase().insertWithOnConflict(TABLE_TEST_STATE, null, valuesState, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Save a specific answer
     * @param testState
     * @param answerNum
     */
    public void save(TestState testState, int answerNum) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HASH, testState.getTestHash());
        values.put(COLUMN_STARTED_AT, testState.getStartedAt());
        values.put(COLUMN_QUESTION, answerNum);
        Answer answer = testState.getAnswers().get(answerNum);
        values.put(COLUMN_QUESTION_TYPE, answer.getType());
        values.put(COLUMN_ANSWER, answer.toJSONString());
        dbHelper.getWritableDatabase().insertWithOnConflict(TABLE_ANSWER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Get a list of attempts to take the test
     * @param hash
     * @param fillAnswers
     */
    public List<TestState> getStates(String hash, boolean fillAnswers) {
        Cursor cursorStates = getStateCursor(hash);
        if (cursorStates != null) {
            Map<Long, TestState> states = getStateObjects(cursorStates);
            cursorStates.close();
            if (fillAnswers) {
                String[] columns = new String[] { "*" };
                String selection = COLUMN_HASH + "=?";
                String[] selectionArgs = new String[] { hash };
                Cursor cursorAnswers = dbHelper.getReadableDatabase().query(TABLE_ANSWER, columns, selection, selectionArgs, null, null, null);

                int indexStartedAt = cursorAnswers.getColumnIndex(COLUMN_STARTED_AT);
                int indexQuestion = cursorAnswers.getColumnIndex(COLUMN_QUESTION);
                int indexType = cursorAnswers.getColumnIndex(COLUMN_QUESTION_TYPE);
                int indexAnswer = cursorAnswers.getColumnIndex(COLUMN_ANSWER);
                while (cursorAnswers.moveToNext()) {
                    long startedAt = cursorAnswers.getLong(indexStartedAt);
                    int questionNum = cursorAnswers.getInt(indexQuestion);
                    String type = cursorAnswers.getString(indexType);
                    String answerString = cursorAnswers.getString(indexAnswer);
                    try {
                        Answer answer = Answer.of(type);
                        answer.fill(answerString);
                        if (states.containsKey(startedAt)) {
                            states.get(startedAt).setAnswer(questionNum, answer);
                        }
                    } catch (Exception e) {
                        // Unknown type - unlikely
                    }
                }
                cursorAnswers.close();

            }
            List<TestState> result = new ArrayList<>(states.values());
            Collections.sort(result, new Comparator<TestState>() {
                @Override
                public int compare(TestState o1, TestState o2) {
                    return (o1.getStartedAt() < o2.getStartedAt()) ? 1 : ((o1.getStartedAt() == o2.getStartedAt()) ? 0 : -1);
                }
            });
            return result;
        }
        return null;
    }

    private Cursor getStateCursor(String hash) {
        String[] columns = new String[] { "*", COLUMN_HASH + " AS _id" };
        String selection = COLUMN_HASH + "=?";
        String[] selectionArgs = new String[] { hash };
        return dbHelper.getReadableDatabase().query(TABLE_TEST_STATE, columns, selection, selectionArgs, null, null, null);
    }

    private Map<Long, TestState> getStateObjects(Cursor cursor) {
        Map<Long, TestState> states = new HashMap<>();
        int indexHash = cursor.getColumnIndex(COLUMN_HASH);
        int indexStartedAt = cursor.getColumnIndex(COLUMN_STARTED_AT);
        int indexFinishedAt = cursor.getColumnIndex(COLUMN_FINISHED_AT);
        TestState object;
        while (cursor.moveToNext()) {
            object = new TestState();
            object.setTestHash(cursor.getString(indexHash));
            object.setStartedAt(cursor.getLong(indexStartedAt));
            object.setFinishedAt(cursor.getLong(indexFinishedAt));
            states.put(object.getStartedAt(), object);
        }
        return states;
    }
}