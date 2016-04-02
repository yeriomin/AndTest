package com.github.yeriomin.andtest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.github.yeriomin.andtest.DbHelper;
import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.model.Question;
import com.github.yeriomin.andtest.model.Test;
import com.github.yeriomin.andtest.view.TimerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TestResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);
        Test test = Test.getInstance();
        fillHeader(test);
        buildResultTable(test);

        Button buttonFinish = (Button) findViewById(R.id.button_finish);
        if (test.isFinished()) {
            buttonFinish.setEnabled(false);
        } else {
            buttonFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    Test test = Test.getInstance();
                    test.finish();
                    DbHelper.getDbHelper(getApplicationContext()).save(test);
                    DbHelper.closeDbHelper();
                    fillHeader(test);
                    buildResultTable(test);
                }
            });
        }
        ImageButton buttonPrevious = (ImageButton) findViewById(R.id.button_previous);
        buttonPrevious.setOnClickListener(new QuestionListener(test.getQuestions().size() - 1));
        ImageButton buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), TestSummaryActivity.class));
            }
        });
    }

    private String getResultString(Test test) {
        int total = test.getQuestions().size();
        int count = test.getCorrectCount();
        double percent = ((double) count)/((double) total)*100;
        String percentString = (new DecimalFormat("#.##")).format(percent);
        return getString(R.string.result_count_format, count, total, percentString);
    }

    private ArrayList<String> getResults(Test test) {
        ArrayList<String> results = new ArrayList<String>();
        int num = 0;
        for (Question question: test.getQuestions()) {
            int statusId;
            if (test.isFinished()) {
                statusId = question.isCorrect()
                        ? R.string.question_table_status_correct
                        : R.string.question_table_status_incorrect;
            } else {
                statusId = question.hasAnswer()
                        ? R.string.question_table_status_answered
                        : R.string.question_table_status_skipped;
            }
            results.add(getString(R.string.question_table_number, num + 1) + " " + getString(statusId));
            num++;
        }
        return results;
    }

    private void buildResultTable(Test test) {
        ListView listView = (ListView) findViewById(R.id.result_list);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResults(test)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QuestionActivity.startQuestionActivity(view.getContext(), position);
            }
        });
    }

    private void fillHeader(Test test) {
        TextView resultCountLabel = (TextView) findViewById(R.id.result_count_label);
        resultCountLabel.setText(getString(test.isFinished()
                        ? R.string.result_count_correct
                        : R.string.result_count_answered
        ));
        TextView resultCount = (TextView) findViewById(R.id.result_count);
        resultCount.setText(getResultString(test));
        TextView resultTimeLabel = (TextView) findViewById(R.id.result_time_label);
        TimerView resultTime = (TimerView) findViewById(R.id.result_time);
        if (test.isFinished() && test.getTimeLimit() > 0) {
            resultTimeLabel.setText(getString(R.string.result_time_remaining));
            resultTime.launch(test);
        } else {
            resultTimeLabel.setText(getString(R.string.result_time_elapsed));
            resultTime.setTime(test.getFinishedAt() - test.getStartedAt());
        }
    }
}
