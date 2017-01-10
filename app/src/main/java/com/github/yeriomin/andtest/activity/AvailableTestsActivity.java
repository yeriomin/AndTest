package com.github.yeriomin.andtest.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import java.util.Collections;

public class AvailableTestsActivity extends ListActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 356;

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

        if (checkPermission()) {
            fillList();
        } else {
            requestPermission();
        }
    }

    private void fillList() {
        final AvailableTestsActivity activity = this;
        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getTests()));
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Test test = Test.getInstance();
                String file = (String) parent.getItemAtPosition(position);
                if (test.getState().isStarted() && !test.getState().isFinished()) {
                    DialogInterface.OnClickListener listener = new UnfinishedDialogListener(activity, test, file);
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder
                        .setMessage(getString(R.string.unfinished_test_question))
                        .setPositiveButton(getString(R.string.unfinished_test_yes), listener)
                        .setNegativeButton(getString(R.string.unfinished_test_no), listener)
                        .show();
                } else {
                    startTest(test, file);
                }
            }
        });
    }

    public void startTest(Test test, String file) {
        try {
            test.setFile(file);
        } catch (FileNotFoundException e) {
            System.out.println("Internal error. Test file not found: " + e.getMessage());
        } catch (JSONException e) {
            System.out.println("Test file is not a valid JSON file: " + e.getMessage());
        }
        if (Test.getInstance().getQuestions().size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_invalid_test), Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(getApplicationContext(), TestAttemptsActivity.class));
        }
    }

    private ArrayList<String> getTests() {
        ArrayList<String> tests = new ArrayList<>();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            File[] jsonFiles = Test.getDirectory().listFiles(Test.getFileFilter());
            if (jsonFiles != null && jsonFiles.length > 0) {
                for (File file: jsonFiles) {
                    tests.add(file.getName());
                }
            }
        }
        Collections.sort(tests);
        return tests;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            return this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE
            && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fillList();
        }
    }

    private class UnfinishedDialogListener implements DialogInterface.OnClickListener {

        private AvailableTestsActivity activity;
        private Test test;
        private String file;

        public UnfinishedDialogListener(AvailableTestsActivity activity, Test test, String file) {
            this.activity = activity;
            this.test = test;
            this.file = file;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    this.activity.startTest(test, file);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    Intent intent = new Intent(this.activity, QuestionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    break;
            }
        }
    }
}
