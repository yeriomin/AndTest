package com.github.yeriomin.andtest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.model.Question;
import com.github.yeriomin.andtest.model.Test;
import com.github.yeriomin.andtest.view.QuestionLayout;
import com.github.yeriomin.andtest.view.TimerView;

public class QuestionActivity extends Activity {

    private static final String INTENT_QUESTION_NUM = "questionNum";

    public static void startQuestionActivity(Context context, int num) {
        Intent intent = new Intent(context, QuestionActivity.class);
        intent.putExtra(QuestionActivity.INTENT_QUESTION_NUM, num);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Bundle bundle = getIntent().getExtras();
        drawQuestion(null == bundle ? 0 : bundle.getInt(INTENT_QUESTION_NUM, 0));
    }

    private void drawQuestion(final int num) {
        final Test test = Test.getInstance();
        final QuestionLayout layout = getQuestionLayout(num, test);
        int questionsCount = test.getQuestions().size();

        ImageButton buttonPrevious = (ImageButton) findViewById(R.id.button_previous);
        boolean enabled;
        if (num > 0) {
            buttonPrevious.setOnClickListener(new QuestionListener(num - 1));
            enabled = true;
        } else {
            buttonPrevious.setOnClickListener(null);
            enabled = false;
        }
        buttonPrevious.setEnabled(enabled);

        ImageButton buttonResult = (ImageButton) findViewById(R.id.button_result);
        buttonResult.setOnClickListener(new TestResultListener());

        ImageButton buttonHint = (ImageButton) findViewById(R.id.button_hint);
        buttonHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test.getQuestions().get(num).setHinted(true);
                layout.drawAnswerAndExplanation();
            }
        });

        ImageButton buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonNext.setOnClickListener((num < questionsCount - 1)
                        ? new QuestionListener(num + 1)
                        : new TestResultListener()
        );

        TextView textProgress = (TextView) findViewById(R.id.progress);
        textProgress.setText(getString(R.string.text_progress, num + 1, questionsCount));

        if (!test.isFinished()) {
            TimerView timerView = (TimerView) findViewById(R.id.time);
            timerView.launch(test);
        }
    }

    private QuestionLayout getQuestionLayout(int num, Test test) {
        ScrollView questionFrame = (ScrollView) findViewById(R.id.question_frame);
        questionFrame.removeAllViews();
        Question question = test.getQuestions().get(num);
        QuestionLayout layout = QuestionLayout.of(this, question.getType());
        if (null != layout) {
            layout.setQuestion(question, num);
            questionFrame.addView(layout);

            if (test.isFinished() || question.isHinted()) {
                layout.drawAnswerAndExplanation();
            }
        }
        return layout;
    }

    class TestResultListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(v.getContext(), TestResultActivity.class));
        }
    }
}

class QuestionListener implements View.OnClickListener {

    private int questionNum;

    public QuestionListener(int questionNum) {
        this.questionNum = questionNum;
    }

    @Override
    public void onClick(View v) {
        QuestionActivity.startQuestionActivity(v.getContext(), this.questionNum);
    }
}
