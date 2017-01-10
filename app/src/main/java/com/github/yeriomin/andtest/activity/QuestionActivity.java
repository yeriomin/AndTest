package com.github.yeriomin.andtest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.yeriomin.andtest.DbHelper;
import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.core.Answer;
import com.github.yeriomin.andtest.core.Question;
import com.github.yeriomin.andtest.model.Test;
import com.github.yeriomin.andtest.model.TestState;
import com.github.yeriomin.andtest.view.QuestionLayout;
import com.github.yeriomin.andtest.view.TimerView;

public class QuestionActivity extends Activity {

    private static final String INTENT_QUESTION_NUM = "questionNum";

    private TimerView timerView;

    public static void startQuestionActivity(Activity activity, int num) {
        Intent intent = new Intent(activity, QuestionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(QuestionActivity.INTENT_QUESTION_NUM, num);
        activity.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        setContentView(R.layout.activity_question);
        Bundle bundle = getIntent().getExtras();
        drawQuestion(null == bundle ? 0 : bundle.getInt(INTENT_QUESTION_NUM, 0));
    }

    private void drawQuestion(final int num) {
        Test test = Test.getInstance();
        final TestState state = test.getState();
        final QuestionLayout layout = getQuestionLayout(num, test);
        int questionsCount = test.getQuestions().size();

        ImageButton buttonPrevious = (ImageButton) findViewById(R.id.button_previous);
        buttonPrevious.setEnabled(num > 0);
        if (num > 0) {
            buttonPrevious.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    drawQuestion(num - 1);
                }
            });
        }

        ImageButton buttonResult = (ImageButton) findViewById(R.id.button_result);
        buttonResult.setOnClickListener(new TestResultListener(this));

        ImageButton buttonHint = (ImageButton) findViewById(R.id.button_hint);
        buttonHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state.setQuestionHinted(num);
                layout.drawAnswerAndExplanation();
                view.setEnabled(false);
            }
        });
        buttonHint.setEnabled(!state.isFinished() && !state.isQuestionHinted(num));

        ImageButton buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonNext.setOnClickListener((num < questionsCount - 1)
                        ? new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                drawQuestion(num + 1);
                            }
                        }
                        : new TestResultListener(this)
        );

        TextView textProgress = (TextView) findViewById(R.id.progress);
        textProgress.setText(getString(R.string.text_progress, num + 1, questionsCount));

        if (!state.isFinished()) {
            this.timerView = (TimerView) findViewById(R.id.time);
            this.timerView.setTest(test);
            this.timerView.launch();
        }
    }

    private QuestionLayout getQuestionLayout(final int num, final Test test) {
        ScrollView questionFrame = (ScrollView) findViewById(R.id.question_frame);
        questionFrame.removeAllViews();
        Question question = test.getQuestions().get(num);
        if (!test.getState().isAnswered(num)) {
            try {
                test.getState().setAnswer(num, Answer.of(question.getType()));
            } catch (Exception e) {
                // Won't happen - we are using type taken from question
            }
        }
        Answer answer = test.getState().getAnswer(num);
        QuestionLayout.Builder builder = new QuestionLayout.Builder();
        QuestionLayout layout = builder
            .setType(question.getType())
            .setQuestion(question)
            .setAnswer(answer)
            .setContext(this)
            .setAnswerOnChangeListener(new QuestionLayout.AnswerOnChangeListener() {
                @Override
                public void onChange() {
                    new AnswerSaveTask(getApplicationContext(), test.getState()).execute(num);
                }
            })
            .get();
        if (null != layout) {
            questionFrame.addView(layout);

            if (test.getState().isFinished() || test.getState().isQuestionHinted(num)) {
                layout.drawAnswerAndExplanation();
            }
        }
        return layout;
    }

    static class AnswerSaveTask extends AsyncTask<Integer, Void, Void> {

        private Context context;
        private TestState state;

        public AnswerSaveTask(Context context, TestState state) {
            this.context = context;
            this.state = state;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            DbHelper.getDbHelper(context).save(state, params[0]);
            DbHelper.closeDbHelper();
            return null;
        }
    }

    static class TestResultListener implements View.OnClickListener {

        private QuestionActivity questionActivity;

        public TestResultListener(QuestionActivity questionActivity) {
            this.questionActivity = questionActivity;
        }

        @Override
        public void onClick(View v) {
            this.questionActivity.startActivity(new Intent(v.getContext(), QuestionListActivity.class));
        }
    }
}

