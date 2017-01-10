package com.github.yeriomin.andtest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.yeriomin.andtest.DbHelper;
import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.model.Test;
import com.github.yeriomin.andtest.model.TestState;

import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestAttemptsActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();

        final Test test = Test.getInstance();

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

        new TestAttemptsListTask(this, test).execute(test.getState().getTestHash());

        Button buttonStart = (Button) findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestState state = test.getState();
                state.start();
                DbHelper.getDbHelper(getApplicationContext()).save(state);
                DbHelper.closeDbHelper();
                startQuestionActivity();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_summary);
    }

    private void startQuestionActivity() {
        Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    class TestAttemptsListTask extends AsyncTask<String, Void, List<TestState>> {

        private Activity activity;
        private Test test;

        public TestAttemptsListTask(Activity activity, Test test) {
            this.activity = activity;
            this.test = test;
        }

        @Override
        protected List<TestState> doInBackground(String... hash) {
            List<TestState> result = DbHelper.getDbHelper(activity).getStates(hash[0], true);
            DbHelper.closeDbHelper();
            return result;
        }

        @Override
        protected void onPostExecute(final List<TestState> testStates) {
            TextView emptyView = (TextView) activity.findViewById(android.R.id.empty);
            ListView resultList = (ListView) activity.findViewById(R.id.result_list);
            resultList.setAdapter(getAdapter(testStates));
            resultList.setEmptyView(emptyView);
            resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TestState state = testStates.get(position);
                    test.setState(state);
                    startQuestionActivity();
                }
            });
        }

        private SimpleAdapter getAdapter(List<TestState> testStates) {
            String[] from = new String[] {"result", "startedAt"};
            int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
            List<Map<String, String>> pairs = new ArrayList<>();
            for (TestState state: testStates) {
                Map<String, String> map = new HashMap<>();
                int total = test.getQuestions().size();
                int count = state.getCorrectAnswerCount(test);
                double percent = ((double) count)/((double) total)*100;
                String percentString = (new DecimalFormat("#.##")).format(percent);
                map.put("result", activity.getString(R.string.result_count_format, count, total, percentString));
                String startedAt = DateFormat.getDateTimeInstance().format(new Date(state.getStartedAt()));
                map.put("startedAt", startedAt + (state.getFinishedAt() > 0 ? "" : activity.getString(R.string.result_not_finished_yet)));
                pairs.add(map);
            }

            return new SimpleAdapter(activity, pairs, android.R.layout.simple_list_item_2, from, to);
        }
    }
}
