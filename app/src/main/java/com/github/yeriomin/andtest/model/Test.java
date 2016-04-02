package com.github.yeriomin.andtest.model;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class Test {

    private static final String DIRECTORY_TESTS = "AndTest";
    private static final String EXT = ".json";
    private static final String JSON_PROPERTY_QUESTIONS = "questions";
    private static final String JSON_PROPERTY_TIMELIMIT = "timeLimit";
    private static final String JSON_PROPERTY_DESCRIPTION = "description";

    private static Test instance;

    private File file;

    private String description;
    private long timeLimit;
    private long startedAt;
    private long finishedAt;
    private ArrayList<Question> questions;

    public boolean isFinished() {
        return finishedAt > 0;
    }

    public void finish() {
        this.finishedAt = Calendar.getInstance().getTimeInMillis();
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public void start() {
        this.startedAt = Calendar.getInstance().getTimeInMillis();
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public int getCorrectCount() {
        int count = 0;
        for (Question question: this.getQuestions()) {
            if (this.isFinished() ? question.isCorrect() : question.hasAnswer()) {
                count++;
            }
        }
        return count;
    }

    public String md5(){
        String checksum = null;
        try {
            FileInputStream fis = new FileInputStream(this.file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int numOfBytesRead;
            while((numOfBytesRead = fis.read(buffer)) > 0){
                md.update(buffer, 0, numOfBytesRead);
            }
            byte[] hash = md.digest();
            checksum = new BigInteger(1, hash).toString();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return checksum;
    }

    private Test() {

    }

    public void setFile(String fileName) throws JSONException, FileNotFoundException {
        this.file = new File(getDirectory() + File.separator + fileName);
        this.questions = new ArrayList<Question>();
        this.startedAt = 0;
        this.finishedAt = 0;
        this.timeLimit = 0;
        this.description = "";
        this.fill(this.file);
    }

    public void fill(File file) throws JSONException, FileNotFoundException {
        String content = new Scanner(file).useDelimiter("\\A").next();
        JSONObject test = new JSONObject(content);
        JSONArray questions = test.getJSONArray(JSON_PROPERTY_QUESTIONS);
        for (int i = 0; i < questions.length(); i++) {
            this.questions.add(Question.of((JSONObject) questions.get(i)));
        }
        if (test.has(JSON_PROPERTY_TIMELIMIT)) {
            this.timeLimit = test.getInt(JSON_PROPERTY_TIMELIMIT);
        }
        if (test.has(JSON_PROPERTY_DESCRIPTION)) {
            this.description = test.getString(JSON_PROPERTY_DESCRIPTION);
        }
    }

    public static Test getInstance() {
        if (null == instance) {
            instance = new Test();
        }
        return instance;
    }

    public static File getDirectory() {
        return Environment.getExternalStoragePublicDirectory(DIRECTORY_TESTS);
    }

    public static FilenameFilter getFileFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(EXT);
            }
        };
    }
}
