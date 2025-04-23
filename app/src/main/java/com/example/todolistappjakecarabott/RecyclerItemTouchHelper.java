package com.example.todolistappjakecarabott;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistappjakecarabott.Adaptder.TodoAdapter;
import com.example.todolistappjakecarabott.Model.ToDoModel;

// Custom class for handling swipe gestures on RecyclerView items
public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final TodoAdapter adapter;

    // Constructor accepts the adapter to manipulate items
    public RecyclerItemTouchHelper(TodoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    // We don't support moving items up/down in the list, so return false
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // Called when a swipe is detected (either left or right)
    @Override
    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getBindingAdapterPosition();

        // Swipe left to delete with confirmation dialog
        if (direction == ItemTouchHelper.LEFT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete this task?");
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.deleteItem(position);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(position);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        // Swipe right to edit the task by launching CreateTaskActivity
        else if (direction == ItemTouchHelper.RIGHT) {
            ToDoModel task = adapter.getTaskAt(position);
            Intent intent = new Intent(adapter.getContext(), CreateTaskActivity.class);
            intent.putExtra("id", task.getId());
            intent.putExtra("task", task.getTask());
            intent.putExtra("priority", task.getPriority());
            intent.putExtra("status", task.getStatus());
            adapter.getContext().startActivity(intent);
            adapter.notifyItemChanged(position); // visually reset item
        }
    }

    // Draw background color and icon while user swipes the item
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon;
        ColorDrawable background;

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        // Determine swipe direction and assign appropriate icon/color
        if (dX > 0) {
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.editart);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.AccentColor));
        } else {
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.delete);
            background = new ColorDrawable(Color.RED);
        }

        // Position the icon vertically centered
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + iconMargin;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        // Draw icons and background according to swipe direction
        if (dX > 0) {
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = iconLeft + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) {
            int iconRight = itemView.getRight() - iconMargin;
            int iconLeft = iconRight - icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            background.setBounds(0, 0, 0, 0);
        }

        // Draw the visuals on the canvas
        background.draw(c);
        icon.draw(c);
    }
}
