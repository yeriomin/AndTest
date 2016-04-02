package com.github.yeriomin.andtest.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;

import com.github.yeriomin.andtest.model.Question;
import com.github.yeriomin.andtest.model.QuestionMultipleChoice;

import java.util.HashSet;

public class QuestionLayoutMultipleChoice extends QuestionLayout {

    public QuestionLayoutMultipleChoice(Context context) {
        super(context);
    }

    public void setQuestion(Question question, int num) {
        super.setQuestion(question, num);

        int id = 0;
        for (String answer: ((QuestionMultipleChoice) this.question).getChoices()) {
            CheckBox answerCheckBox = new CheckBox(getContext());
            answerCheckBox.setId(id);
            answerCheckBox.setText(answer);
            answerCheckBox.setOnClickListener(new CheckboxListener(question));
            if (((HashSet<Integer>) question.getAnswer()).contains(id)) {
                answerCheckBox.setChecked(true);
            }
            this.addView(answerCheckBox);
            id++;
        }
    }

    public void drawAnswerAndExplanation() {
        super.drawAnswerAndExplanation();

        HashSet<Integer> correct = ((QuestionMultipleChoice) this.question).getCorrect();
        for (int id = 0; id < ((QuestionMultipleChoice) this.question).getChoices().size(); id++) {
            CheckBox answerCheckBox = (CheckBox) findViewById(id);
            answerCheckBox.setEnabled(false);
            if (correct.contains(id)) {
                answerCheckBox.setTextColor(Color.GREEN);
            }
        }
    }

    class CheckboxListener implements View.OnClickListener {

        private Question question;

        public CheckboxListener(Question question) {
            this.question = question;
        }

        @Override
        public void onClick(View v) {
            HashSet<Integer> givenAnswer = (HashSet<Integer>) question.getAnswer();
            CheckBox checkbox = (CheckBox) v;
            Integer id = checkbox.getId();
            if (checkbox.isChecked()) {
                givenAnswer.add(id);
            } else {
                givenAnswer.remove(id);
            }
        }
    }
}
