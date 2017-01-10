package com.github.yeriomin.andtest.view;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.yeriomin.andtest.core.Answer;
import com.github.yeriomin.andtest.core.Question;

abstract public class QuestionLayout extends LinearLayout {

    protected Question question;
    protected Answer answer;
    protected AnswerOnChangeListener answerOnChangeListener;

    public interface AnswerOnChangeListener {

        void onChange();
    }

    static public class Builder {

        private Context context;
        private String type;
        private Question question;
        private Answer answer;
        private AnswerOnChangeListener answerOnChangeListener;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setAnswer(Answer answer) {
            this.answer = answer;
            return this;
        }

        public Builder setAnswerOnChangeListener(AnswerOnChangeListener answerOnChangeListener) {
            this.answerOnChangeListener = answerOnChangeListener;
            return this;
        }

        public Builder setQuestion(Question question) {
            this.question = question;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public QuestionLayout get() {
            QuestionLayout layout;
            if (type == Question.TYPE_MC) {
                layout = new QuestionLayoutMultipleChoice(context);
            } else if (type == Question.TYPE_OE) {
                layout = new QuestionLayoutOpenEnded(context);
            } else {
                return null;
            }
            layout.drawQuestion(question, answer);
            layout.setAnswerOnChangeListener(answerOnChangeListener);
            return layout;
        }
    }

    public QuestionLayout(Context context) {
        super(context, null);

        this.setOrientation(VERTICAL);
    }

    public void setAnswerOnChangeListener(AnswerOnChangeListener answerOnChangeListener) {
        this.answerOnChangeListener = answerOnChangeListener;
    }

    public void drawQuestion(Question question, Answer answer) {
        this.question = question;
        this.answer = answer;

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
