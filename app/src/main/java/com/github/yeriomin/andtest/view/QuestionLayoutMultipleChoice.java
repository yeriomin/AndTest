package com.github.yeriomin.andtest.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;

import com.github.yeriomin.andtest.core.Answer;
import com.github.yeriomin.andtest.core.AnswerMultipleChoice;
import com.github.yeriomin.andtest.core.Question;
import com.github.yeriomin.andtest.core.QuestionMultipleChoice;

import java.util.HashSet;
import java.util.Set;

public class QuestionLayoutMultipleChoice extends QuestionLayout {

    public QuestionLayoutMultipleChoice(Context context) {
        super(context);
    }

    @Override
    public void drawQuestion(Question question, Answer answer) {
        super.drawQuestion(question, answer);

        int id = 0;
        for (String choice: ((QuestionMultipleChoice) this.question).getChoices()) {
            CheckBox answerCheckBox = new CheckBox(getContext());
            answerCheckBox.setId(id);
            answerCheckBox.setText(choice);
            answerCheckBox.setOnClickListener(new CheckboxListener());
            if (null != answer && !answer.isEmpty()) {
                if (((AnswerMultipleChoice) answer).get().contains(id)) {
                    answerCheckBox.setChecked(true);
                }
            }
            this.addView(answerCheckBox);
            id++;
        }
    }

    @Override
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

        @Override
        public void onClick(View v) {
            Set<Integer> givenAnswer = ((AnswerMultipleChoice) answer).get();
            CheckBox checkbox = (CheckBox) v;
            Integer id = checkbox.getId();
            if (checkbox.isChecked()) {
                givenAnswer.add(id);
            } else {
                givenAnswer.remove(id);
            }
            answerOnChangeListener.onChange();
        }
    }
}
