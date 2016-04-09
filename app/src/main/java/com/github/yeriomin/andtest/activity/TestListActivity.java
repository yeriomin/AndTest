package com.github.yeriomin.andtest.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.model.Test;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class TestListActivity extends ListActivity {

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                onResume();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);
        File directory = Test.getDirectory();
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getTests()));
        final ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                Test.getInstance().setFile((String) parent.getItemAtPosition(position));
            } catch (FileNotFoundException e) {
                System.out.println("Internal error. Test file not found: " + e.getMessage());
            } catch (JSONException e) {
                System.out.println("Test file is not a valid JSON file: " + e.getMessage());
            }
            if (Test.getInstance().getQuestions().size() == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_invalid_test), Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(getApplicationContext(), TestSummaryActivity.class));
            }
            }
        });
    }

    private ArrayList<String> getTests() {
        ArrayList<String> tests = new ArrayList<String>();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            File[] jsonFiles = Test.getDirectory().listFiles(Test.getFileFilter());
            if (jsonFiles != null && jsonFiles.length > 0) {
                for (File file: jsonFiles) {
                    tests.add(file.getName());
                }
            }
        }
        return tests;
    }
}
