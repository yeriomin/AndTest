package com.github.yeriomin.andtest.model;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class Question {

    public static final String TYPE_MC = "multipleChoice";
    public static final String TYPE_OE = "openEnded";
    private static final String JSON_PROPERTY_QUESTION = "question";
    private static final String JSON_PROPERTY_EXPLANATION = "explanation";

    protected String type;
    private String question;
    private String explanation;
    private Object answer;
    private boolean hinted;

    public static Question of(JSONObject jsonQuestion) throws JSONException {
        final String type = jsonQuestion.getString("type");
        if (type.equals(TYPE_MC)) {
            return new QuestionMultipleChoice(jsonQuestion);
        } else if (type.equals(TYPE_OE)) {
            return new QuestionOpenEnded(jsonQuestion);
        } else {
            throw new JSONException("Unknown type: " + type);
        }
    }

    public String getType() {
        return type;
    }

    public String getQuestion() {
        return question;
    }

    public String getExplanation() {
        return explanation;
    }

    public Object getAnswer() {
        return answer;
    }

    public void setAnswer(Object answer) {
        this.answer = answer;
    }

    protected Question(JSONObject jsonQuestion) throws JSONException {
        this.question = jsonQuestion.getString(JSON_PROPERTY_QUESTION);
        this.explanation = jsonQuestion.getString(JSON_PROPERTY_EXPLANATION);
    }

    public boolean isHinted() {
        return hinted;
    }

    public void setHinted(boolean hinted) {
        this.hinted = hinted;
    }

    abstract public boolean isCorrect();
    abstract public boolean hasAnswer();
}
