package com.example.todolistappjakecarabott;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

// Settings screen allowing the user to toggle dark mode and reminders
public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat themeSwitch, notifSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set status bar to match accent color (for newer versions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.AccentColor, getTheme()));
        }

        // Set up the toolbar with back arrow and custom centered title
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.AccentColor));
        }

        // Add custom title text in the center of the toolbar
        TextView title = new TextView(this);
        title.setText("Settings");
        title.setTextColor(ContextCompat.getColor(this, R.color.AccentColor));
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);

        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        toolbar.addView(title, layoutParams);

        // Load switches and saved preferences
        themeSwitch = findViewById(R.id.themeSwitch);
        notifSwitch = findViewById(R.id.notifSwitch);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("darkMode", false);
        boolean canNotif = prefs.getBoolean("canNotif", true);

        // Set switch states based on saved preferences
        themeSwitch.setChecked(isDarkMode);
        notifSwitch.setChecked(canNotif);

        // Update dark mode preference and immediately apply the theme
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("darkMode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Update reminder toggle preference
        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("canNotif", isChecked).apply();
        });
    }

    // Handle back arrow click to return to the previous screen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
