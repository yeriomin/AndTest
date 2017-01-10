package com.github.yeriomin.andtest.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.core.Answer;
import com.github.yeriomin.andtest.core.AnswerOpenEnded;
import com.github.yeriomin.andtest.core.Question;
import com.github.yeriomin.andtest.core.QuestionOpenEnded;

public class QuestionLayoutOpenEnded extends QuestionLayout {

    public QuestionLayoutOpenEnded(Context context) {
        super(context);
    }

    @Override
    public void drawQuestion(Question question, final Answer answer) {
        super.drawQuestion(question, answer);

        EditText answerEditText = new EditText(getContext());
        answerEditText.setId(R.id.question_oe_answer);
        answerEditText.setText(((AnswerOpenEnded) answer).get());
        answerEditText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((AnswerOpenEnded) answer).set(s.toString());
                answerOnChangeListener.onChange();
            }
        });
        this.addView(answerEditText);
    }

    @Override
    public void drawAnswerAndExplanation() {
        super.drawAnswerAndExplanation();

        EditText answerEditText = (EditText) findViewById(R.id.question_oe_answer);
        answerEditText.setEnabled(false);
        if (!this.question.isCorrect(answer)) {
            answerEditText.setError(((QuestionOpenEnded) this.question).getCorrect());
        }
    }
}
