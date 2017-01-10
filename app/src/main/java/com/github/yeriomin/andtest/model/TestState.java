package com.github.yeriomin.andtest.model;

import java.util.HashMap;
import java.util.Map;

public class TestState extends com.github.yeriomin.andtest.core.TestState {

    private String testHash;
    private Map<Integer, Boolean> hintedQuestions = new HashMap<>();

    public String getTestHash() {
        return testHash;
    }

    public void setTestHash(String testHash) {
        this.testHash = testHash;
    }

    public void setQuestionHinted(int questionNum) {
        this.setQuestionHinted(questionNum, true);
    }

    public void setQuestionHinted(int questionNum, boolean hinted) {
        this.hintedQuestions.put(questionNum, hinted);
    }

    public boolean isQuestionHinted(int questionNum) {
        return this.hintedQuestions.containsKey(questionNum) && this.hintedQuestions.get(questionNum);
    }

    @Override
    public void start() {
        super.start();
        setFinishedAt(0);
        getAnswers().clear();
        hintedQuestions.clear();
    }

    @Override
    public String toString() {
        return super.toString() + " testHash=" + testHash + ", startedAt=" + getStartedAt() + ", finishedAt=" + getFinishedAt() + ", getAnswers().size()=" + getAnswers().size() + ", hintedQuestions.size()=" + hintedQuestions.size();
    }
}
