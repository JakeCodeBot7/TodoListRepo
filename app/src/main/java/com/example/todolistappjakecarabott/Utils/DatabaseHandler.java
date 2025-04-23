package com.example.todolistappjakecarabott.Utils;

import static android.provider.Telephony.BaseMmsColumns.PRIORITY;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.todolistappjakecarabott.Model.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database constants
    private static final int VERSION = 3; // Updated version to include priority and due fields
    private static final String NAME = "toDoListDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String DUE_DATE = "dueDate";
    private static final String DUE_TIME = "dueTime";

    // SQL statement to create the table
    private static final String CREATE_TODO_TABLE =
            "CREATE TABLE " + TODO_TABLE + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TASK + " TEXT, " +
                    STATUS + " INTEGER, " +
                    PRIORITY + " INTEGER, " +
                    DUE_DATE + " TEXT, " +
                    DUE_TIME + " TEXT)";

    private SQLiteDatabase db;

    // Constructor initializes the database
    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    // Called when the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        onCreate(db);
    }

    // Opens the database for writing
    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    // Inserts a new task into the database
    public void insertTask(ToDoModel task) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, 0); // default to incomplete
        cv.put(PRIORITY, task.getPriority());
        cv.put(DUE_DATE, task.getDueDate());
        cv.put(DUE_TIME, task.getDueTime());
        db.insert(TODO_TABLE, null, cv);
    }

    // Fetches all tasks, sorted by priority and due date/time
    public List<ToDoModel> getAllTasks() {
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;

        db.beginTransaction();
        try {
            // Ordered so that high priority and closer deadlines appear first
            cur = db.query(
                    TODO_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    PRIORITY + " DESC, " + DUE_DATE + " ASC, " + DUE_TIME + " ASC"
            );

            if (cur != null && cur.moveToFirst()) {
                do {
                    ToDoModel task = new ToDoModel();
                    task.setId(cur.getInt(cur.getColumnIndexOrThrow(ID)));
                    task.setTask(cur.getString(cur.getColumnIndexOrThrow(TASK)));
                    task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(STATUS)));
                    task.setPriority(cur.getInt(cur.getColumnIndexOrThrow(PRIORITY)));
                    task.setDueDate(cur.getString(cur.getColumnIndexOrThrow(DUE_DATE)));
                    task.setDueTime(cur.getString(cur.getColumnIndexOrThrow(DUE_TIME)));
                    taskList.add(task);
                } while (cur.moveToNext());
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
        }

        return taskList;
    }

    // Updates the completion status of a task
    public void updateStatus(int id, int status) {
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)});
    }

    // Updates basic task fields (used for simple edit scenarios)
    public void updateTask(int id, String task, int priority, int status) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        cv.put(PRIORITY, priority);
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)});
    }

    // Updates all fields of a task using a ToDoModel object
    public void updateTask(ToDoModel task) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, task.getStatus());
        cv.put(PRIORITY, task.getPriority());
        cv.put(DUE_DATE, task.getDueDate());
        cv.put(DUE_TIME, task.getDueTime());
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(task.getId())});
    }

    // Deletes a task from the database using its ID
    public void deleteTask(int id) {
        db.delete(TODO_TABLE, ID + "=?", new String[]{String.valueOf(id)});
    }
}
