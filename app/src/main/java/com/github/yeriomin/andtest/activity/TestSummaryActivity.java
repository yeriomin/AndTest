package com.github.yeriomin.andtest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.yeriomin.andtest.DbHelper;
import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.model.Test;
import com.github.yeriomin.andtest.model.TestResult;

import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class TestSummaryActivity extends Activity {

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), TestListActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_summary);
        Test test = Test.getInstance();

        TextView descriptionView = (TextView) findViewById(R.id.text_description);
        descriptionView.setText(test.getDescription());
        TextView questionCountView = (TextView) findViewById(R.id.text_questions_count);
        questionCountView.setText(getString(R.string.summary_question_count, test.getQuestions().size()));
        TextView timeLimitView = (TextView) findViewById(R.id.text_time_limit);
        if (test.getTimeLimit() > 0) {
            int secondsOverall = (int) (test.getTimeLimit()/1000);
            int minutes = secondsOverall/60;
            int seconds = secondsOverall%60;
            String secondsString = (seconds < 10 ? "0" : "") + seconds;
            String timeLimit = getString(R.string.text_time, minutes, secondsString);
            timeLimitView.setText(getString(R.string.summary_time_limit, timeLimit));
        } else {
            timeLimitView.setVisibility(View.GONE);
        }

        String[] from = new String[] {"result", "timeCompleted"};
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
        ArrayList<HashMap<String, String>> pairs = new ArrayList<HashMap<String, String>>();
        for (TestResult result: getResults(test.md5())) {
            HashMap<String, String> map = new HashMap<String, String>();
            int total = test.getQuestions().size();
            int count = result.getCorrectAnswers();
            double percent = ((double) count)/((double) total)*100;
            String percentString = (new DecimalFormat("#.##")).format(percent);
            map.put("result", getString(R.string.result_count_format, count, total, percentString));
            map.put("timeCompleted", DateFormat.getDateTimeInstance().format(new Date(result.getTimestamp())));
            pairs.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, pairs, android.R.layout.simple_list_item_2, from, to);
        ListView resultList = (ListView) findViewById(R.id.result_list);
        resultList.setAdapter(adapter);
        TextView emptyView = (TextView) findViewById(android.R.id.empty);
        resultList.setEmptyView(emptyView);

        Button buttonStart = (Button) findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Test.getInstance().start();
                startActivity(new Intent(getApplicationContext(), QuestionActivity.class));
            }
        });
    }

    private ArrayList<TestResult> getResults(String hash) {
        ArrayList<TestResult> result = DbHelper.getDbHelper(this).get(hash);
        DbHelper.closeDbHelper();
        return result;
    }
}
