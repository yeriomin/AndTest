package com.github.yeriomin.andtest.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

public class QuestionMultipleChoice extends Question {

    private static final String JSON_PROPERTY_CHOICES = "choices";
    private static final String JSON_PROPERTY_CORRECT = "correct";

    private ArrayList<String> choices;
    private HashSet<Integer> correct;

    public ArrayList<String> getChoices() {
        return choices;
    }

    public HashSet<Integer> getCorrect() {
        return correct;
    }

    protected QuestionMultipleChoice(JSONObject jsonQuestion) throws JSONException {
        super(jsonQuestion);

        this.setAnswer(new HashSet<Integer>());
        this.type = Question.TYPE_MC;
        JSONArray choices = jsonQuestion.getJSONArray(JSON_PROPERTY_CHOICES);
        this.choices = new ArrayList<String>();
        for (int i = 0; i < choices.length(); i++){
            this.choices.add(choices.getString(i));
        }
        JSONArray correct = jsonQuestion.getJSONArray(JSON_PROPERTY_CORRECT);
        this.correct = new HashSet<Integer>();
        for (int i = 0; i < correct.length(); i++){
            this.correct.add(correct.getInt(i));
        }
    }

    public boolean isCorrect() {
        return this.correct.equals((HashSet<Integer>) this.getAnswer());
    }

    public boolean hasAnswer() {
        return null != this.getAnswer() && ((HashSet<Integer>) this.getAnswer()).size() > 0;
    }

}
