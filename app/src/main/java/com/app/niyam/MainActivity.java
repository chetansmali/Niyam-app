package com.app.niyam;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> appUsageList;
    ArrayAdapter<String> adapter;
    long totalUsageTimeToday = 0; // Variable to track total usage time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        appUsageList = new ArrayList<>();

        // Check if the user has granted usage access permission
        if (!hasUsageStatsPermission()) {
            // Direct the user to the Usage Access Settings screen
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        // Get the usage statistics for today and display
        getUsageStatistics();

        // Display total usage time at the top or bottom of the list
        String totalUsageTimeString = "Total time spent today: " + formatTime(totalUsageTimeToday);
        appUsageList.add(0, totalUsageTimeString); // Add the total usage time at the beginning of the list

        // Set up the ListView with the app names and usage times
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appUsageList);
        listView.setAdapter(adapter);
    }

    // Method to check if the app has permission to access usage stats
    private boolean hasUsageStatsPermission() {
        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long startTime = endTime - 1000 * 3600 * 24; // Last 24 hours
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // Method to retrieve and display the usage statistics for today
    private void getUsageStatistics() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return;
        }

        long endTime = System.currentTimeMillis();
        long startTime = getStartOfDay(); // Start of today

        // Get the usage stats for the day
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        if (usageStatsList == null || usageStatsList.isEmpty()) {
            Toast.makeText(this, "No usage stats available", Toast.LENGTH_LONG).show();
            return;
        }

        // HashMap to store the total usage time for each app
        HashMap<String, Long> appUsageMap = new HashMap<>();

        PackageManager packageManager = getPackageManager();
        for (UsageStats usageStats : usageStatsList) {
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(usageStats.getPackageName(), 0);

                // Filter only third-party apps (non-system apps)
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    long totalTimeInForeground = usageStats.getTotalTimeInForeground(); // Time in milliseconds

                    // Aggregate the time for each app in the HashMap
                    if (appUsageMap.containsKey(appName)) {
                        long currentTime = appUsageMap.get(appName);
                        appUsageMap.put(appName, currentTime + totalTimeInForeground);
                    } else {
                        appUsageMap.put(appName, totalTimeInForeground);
                    }

                    // Add to the total usage time for today
                    totalUsageTimeToday += totalTimeInForeground;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Convert the HashMap to a list and sort it by usage time in descending order
        List<Map.Entry<String, Long>> sortedAppUsageList = new ArrayList<>(appUsageMap.entrySet());
        Collections.sort(sortedAppUsageList, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> entry1, Map.Entry<String, Long> entry2) {
                return Long.compare(entry2.getValue(), entry1.getValue()); // Sort by value (usage time) in descending order
            }
        });

        // Add the sorted app usage data to the appUsageList
        for (Map.Entry<String, Long> entry : sortedAppUsageList) {
            String appName = entry.getKey();
            long totalTime = entry.getValue();
            String formattedTime = formatTime(totalTime);
            appUsageList.add(appName + " - Used for: " + formattedTime);
        }
    }

    // Helper method to get the start of the current day
    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // Helper method to format time in milliseconds to readable format (e.g., "10 min 30 sec")
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes + " min " + seconds + " sec";
    }
}