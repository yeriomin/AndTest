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
import com.github.yeriomin.andtest.core.Question;
import com.github.yeriomin.andtest.model.Test;
import com.github.yeriomin.andtest.model.TestState;
import com.github.yeriomin.andtest.view.TimerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class QuestionListActivity extends Activity {

    private TimerView timerView;

    @Override
    protected void onResume() {
        super.onResume();

        Test test = Test.getInstance();
        fillHeader(test);
        buildResultTable(test);
        fillNavigation(test);

        if (null != this.timerView) {
            this.timerView.launch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != this.timerView) {
            this.timerView.pause();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);
    }

    private String getResultString(Test test) {
        int total = test.getQuestions().size();
        int count = test.getState().getCorrectAnswerCount(test);
        double percent = ((double) count)/((double) total)*100;
        String percentString = (new DecimalFormat("#.##")).format(percent);
        return getString(R.string.result_count_format, count, total, percentString);
    }

    private ArrayList<String> getResults(Test test) {
        ArrayList<String> results = new ArrayList<>();
        TestState state = test.getState();
        int num = 0;
        for (Question question: test.getQuestions()) {
            int statusId;
            if (test.getState().isFinished() || test.getState().isQuestionHinted(num)) {
                statusId = question.isCorrect(state.getAnswer(num))
                        ? R.string.question_table_status_correct
                        : R.string.question_table_status_incorrect;
            } else {
                statusId = state.isAnswered(num)
                        ? R.string.question_table_status_answered
                        : R.string.question_table_status_skipped;
            }
            results.add(getString(R.string.question_table_number, (num + 1)) + " " + getString(statusId));
            num++;
        }
        return results;
    }

    private void buildResultTable(Test test) {
        final Activity activity = this;
        ListView listView = (ListView) findViewById(R.id.result_list);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResults(test)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QuestionActivity.startQuestionActivity(activity, position);
            }
        });
    }

    private void fillHeader(Test test) {
        TextView resultCountLabel = (TextView) findViewById(R.id.result_count_label);
        resultCountLabel.setText(getString(test.getState().isFinished()
                        ? R.string.result_count_correct
                        : R.string.result_count_answered
        ));
        TextView resultCount = (TextView) findViewById(R.id.result_count);
        resultCount.setText(getResultString(test));

        TextView resultTimeLabel = (TextView) findViewById(R.id.result_time_label);
        timerView = (TimerView) findViewById(R.id.result_time);
        timerView.setTest(test);
        if (test.getState().isFinished() && test.getTimeLimit() > 0) {
            resultTimeLabel.setText(getString(R.string.result_time_remaining));
            timerView.launch();
        } else {
            resultTimeLabel.setText(getString(R.string.result_time_elapsed));
            timerView.setTime(test.getTime());
        }
    }

    private void fillNavigation(Test test) {
        ImageButton buttonPrevious = (ImageButton) findViewById(R.id.button_previous);
        final ImageButton buttonNext = (ImageButton) findViewById(R.id.button_next);
        Button buttonFinish = (Button) findViewById(R.id.button_finish);
        buttonFinish.setEnabled(!test.getState().isFinished());
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        buttonNext.setEnabled(!test.getState().isFinished());
        if (!test.getState().isFinished()) {
            buttonFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    buttonNext.setEnabled(true);
                    Test test = Test.getInstance();
                    test.getState().finish();
                    DbHelper.getDbHelper(getApplicationContext()).save(test.getState());
                    DbHelper.closeDbHelper();
                    fillHeader(test);
                    buildResultTable(test);
                }
            });
        }
        buttonNext.setEnabled(test.getState().isFinished());
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), TestAttemptsActivity.class));
            }
        });
    }
}
