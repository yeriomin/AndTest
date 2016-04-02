package com.github.yeriomin.andtest.model;

import org.json.JSONException;
import org.json.JSONObject;

public class QuestionOpenEnded extends Question {

    private static final String JSON_PROPERTY_CORRECT = "correct";

    private String correct;

    public String getCorrect() {
        return correct;
    }

    protected QuestionOpenEnded(JSONObject jsonQuestion) throws JSONException {
        super(jsonQuestion);

        this.setAnswer(new String());
        this.type = Question.TYPE_OE;
        this.correct = jsonQuestion.getString(JSON_PROPERTY_CORRECT);
    }

    public boolean isCorrect() {
        return this.correct.equals(this.getAnswer().toString().trim());
    }

    public boolean hasAnswer() {
        return null != this.getAnswer() && this.getAnswer().toString().length() > 0;
    }
}
