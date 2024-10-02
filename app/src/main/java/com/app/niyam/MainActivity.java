package com.app.niyam;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> appList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        appList = new ArrayList<>();

        // Get the list of installed apps
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // Iterate through installed apps and add only third-party apps to the list
        for (ApplicationInfo appInfo : apps) {
            // Check if the app is NOT a system app
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                String appName = (String) packageManager.getApplicationLabel(appInfo);
                appList.add(appName);
            }
        }

        // Set up the ListView with the app names
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appList);
        listView.setAdapter(adapter);
    }
}