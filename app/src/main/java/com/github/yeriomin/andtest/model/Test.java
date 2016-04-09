package com.github.yeriomin.andtest.model;

import android.os.Environment;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

public class Test extends com.github.yeriomin.andtest.core.Test {

    private static final String DIRECTORY_TESTS = "AndTest";
    private static final String EXT = ".json";

    private static Test instance;

    private File file;

    private long startedAt;
    private long finishedAt;
    private HashMap<Integer, Boolean> hintedQuestions;

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

    private Test() throws JSONException {
        super();
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

    public void setFile(String fileName) throws JSONException, FileNotFoundException {
        this.file = new File(getDirectory() + File.separator + fileName);
        this.startedAt = 0;
        this.finishedAt = 0;
        this.hintedQuestions = new HashMap<Integer, Boolean>();

        String content = new Scanner(file).useDelimiter("\\A").next();
        fill(new JSONObject(content));
    }

    public static Test getInstance() {
        if (null == instance) {
            try {
                instance = new Test();
            } catch (JSONException e) {
                // nothing to catch
            }
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
