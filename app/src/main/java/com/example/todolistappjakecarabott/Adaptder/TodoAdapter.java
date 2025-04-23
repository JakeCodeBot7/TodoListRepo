package com.example.todolistappjakecarabott.Adaptder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistappjakecarabott.CreateTaskActivity;
import com.example.todolistappjakecarabott.MainActivity;
import com.example.todolistappjakecarabott.Model.ToDoModel;
import com.example.todolistappjakecarabott.R;
import com.example.todolistappjakecarabott.Utils.DatabaseHandler;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private MainActivity activity;
    private DatabaseHandler db;

    // Constructor: Takes a reference to the DatabaseHandler and the MainActivity for context
    public TodoAdapter(DatabaseHandler db, MainActivity activity){
        this.db = db;
        this.activity = activity;
    }

    // Inflates the task layout for each item in the RecyclerView
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tasklayout, parent, false);
        return new ViewHolder(itemView);
    }

    // Binds task data to each view holder (checkbox, color, date/time, etc.)
    public void onBindViewHolder(ViewHolder holder, int position){
        db.openDatabase();
        ToDoModel item = todoList.get(position);
        holder.task.setText(item.getTask());
        holder.task.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        holder.task.setChecked(toBoolean(item.getStatus()));

        // Update DB when checkbox is toggled
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    db.updateStatus(item.getId(), 1);
                } else{
                    db.updateStatus(item.getId(), 0);
                }
            }
        });

        // Apply accent color if high priority
        if (item.getPriority() == 1) {
            holder.task.setTextColor(ContextCompat.getColor(activity, R.color.AccentColor));
        } else {
            holder.task.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        }

        // Display due date/time if available
        String dueDate = item.getDueDate();
        String dueTime = item.getDueTime();
        if (dueDate != null && !dueDate.isEmpty() && dueTime != null && !dueTime.isEmpty()) {
            holder.dueDateTime.setText("Due: " + dueDate + " " + dueTime);
            holder.dueDateTime.setVisibility(View.VISIBLE);
        } else {
            holder.dueDateTime.setVisibility(View.GONE);
        }
    }

    // Returns number of tasks
    public int getItemCount(){
        return todoList.size();
    }

    // Helper method: Convert int status to boolean
    private boolean toBoolean(int n){
        return n != 0;
    }

    // ViewHolder class for caching task UI components
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView dueDateTime;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.checkbox);
            dueDateTime = view.findViewById(R.id.dueDateTimeText);
        }
    }

    // Get current context (MainActivity)
    public Context getContext(){ return activity; }

    // Get specific task model at a position
    public ToDoModel getTaskAt(int position) {
        return todoList.get(position);
    }

    // Delete task from DB and update UI
    public void deleteItem(int position){
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    // Open task in CreateTaskActivity for editing
    public void editItem(int position) {
        ToDoModel item = todoList.get(position);
        Intent intent = new Intent(activity, CreateTaskActivity.class);
        intent.putExtra("id", item.getId());
        intent.putExtra("task", item.getTask());
        intent.putExtra("priority", item.getPriority());
        intent.putExtra("status", item.getStatus());
        intent.putExtra("dueDate", item.getDueDate());
        intent.putExtra("dueTime", item.getDueTime());
        activity.startActivity(intent);
    }

    // Assign list of tasks to adapter and refresh the RecyclerView
    public void setTasks(List<ToDoModel> todoList){
        this.todoList = todoList;
        notifyDataSetChanged();
    }

}
