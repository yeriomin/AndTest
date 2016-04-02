package com.github.yeriomin.andtest.view;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.yeriomin.andtest.model.Question;

abstract public class QuestionLayout extends LinearLayout {

    protected Question question;
    protected int num;

    public static QuestionLayout of(Context context, String type) {
        if (type == Question.TYPE_MC) {
            return new QuestionLayoutMultipleChoice(context);
        } else if (type == Question.TYPE_OE) {
            return new QuestionLayoutOpenEnded(context);
        } else {
            return null;
        }
    }

    public QuestionLayout(Context context) {
        super(context, null);

        this.setOrientation(VERTICAL);
    }

    public void setQuestion(Question question, int num) {
        this.question = question;
        this.num = num;

        TextView questionTextView = new TextView(getContext());
        questionTextView.setText(question.getQuestion());
        this.addView(questionTextView);
    }

    public void drawAnswerAndExplanation() {
        TextView explanationTextView = new TextView(getContext());
        explanationTextView.setText(this.question.getExplanation());
        this.addView(explanationTextView);
    }
}
