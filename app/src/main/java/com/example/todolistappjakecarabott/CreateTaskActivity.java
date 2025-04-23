package com.example.todolistappjakecarabott;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.todolistappjakecarabott.Model.ToDoModel;
import com.example.todolistappjakecarabott.Utils.DatabaseHandler;

import java.util.Calendar;

public class CreateTaskActivity extends AppCompatActivity {

    private EditText taskEditText;
    private CheckBox highPriorityCheckbox;
    private Button saveButton;
    private Button pickDateButton, pickTimeButton;

    private DatabaseHandler db;
    private int status = 0;
    private int taskId = -1;

    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // Set status bar color for aesthetic consistency
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.AccentColor, getTheme()));
        }

        // Toolbar setup with custom title and accent-colored back arrow
        Toolbar toolbar = findViewById(R.id.createTaskToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // Clear default title
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.AccentColor, getTheme()));
        }

        // Custom centered title added to toolbar
        TextView title = new TextView(this);
        title.setText("Add Task");
        title.setTextColor(getResources().getColor(R.color.AccentColor, getTheme()));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        toolbar.addView(title, layoutParams);

        // Initialize views and database
        taskEditText = findViewById(R.id.newTaskText);
        highPriorityCheckbox = findViewById(R.id.highPriorityCheckBox);
        saveButton = findViewById(R.id.newTaskButton);
        pickDateButton = findViewById(R.id.pickDateButton);
        pickTimeButton = findViewById(R.id.pickTimeButton);

        db = new DatabaseHandler(this);
        db.openDatabase();

        // If task is being edited, load its data into the form
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            taskId = bundle.getInt("id", -1);
            status = bundle.getInt("status", 0);
            String task = bundle.getString("task", "");
            int priority = bundle.getInt("priority", 0);
            selectedDate = bundle.getString("dueDate", "");
            selectedTime = bundle.getString("dueTime", "");

            taskEditText.setText(task);
            highPriorityCheckbox.setChecked(priority == 1);
            if (!selectedDate.isEmpty()) pickDateButton.setText(selectedDate);
            if (!selectedTime.isEmpty()) pickTimeButton.setText(selectedTime);
            saveButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // Open DatePicker when user clicks pickDateButton
        pickDateButton.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreateTaskActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        pickDateButton.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Open TimePicker when user clicks pickTimeButton
        pickTimeButton.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    CreateTaskActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        pickTimeButton.setText(selectedTime);
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Handle saving the task
        saveButton.setOnClickListener(v -> {
            String taskText = taskEditText.getText().toString().trim();
            boolean isHighPriority = highPriorityCheckbox.isChecked();

            if (taskText.isEmpty()) {
                Toast.makeText(CreateTaskActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
            } else {
                ToDoModel task = new ToDoModel();
                task.setTask(taskText);
                task.setStatus(taskId != -1 ? status : 0);
                task.setPriority(isHighPriority ? 1 : 0);
                task.setDueDate(selectedDate);
                task.setDueTime(selectedTime);

                // Update existing task or insert a new one
                if (taskId != -1) {
                    task.setId(taskId);
                    db.updateTask(task);
                } else {
                    db.insertTask(task);
                }

                // Handle reminders if enabled
                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                boolean canNotif = prefs.getBoolean("canNotif", true);

                if (canNotif && !selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                    String[] dateParts = selectedDate.split("/");
                    String[] timeParts = selectedTime.split(":");

                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1;
                    int year = Integer.parseInt(dateParts[2]);
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day, hour, minute, 0);

                    Intent intent = new Intent(CreateTaskActivity.this, ReminderBroadcast.class);
                    intent.putExtra("task", task.getTask());
                    intent.putExtra("notifId", task.getId());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            CreateTaskActivity.this,
                            task.getId(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    if (alarmManager != null) {
                        // For Android 12+ ask user to enable exact alarms
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            } else {
                                intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                                startActivity(intent);
                            }
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    }
                }

                finish(); // Close activity
            }
        });
    }

    // Handle toolbar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // back arrow
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
