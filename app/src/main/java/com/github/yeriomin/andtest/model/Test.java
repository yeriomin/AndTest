package com.github.yeriomin.andtest.model;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class Test extends com.github.yeriomin.andtest.core.Test {

    private static final String DIRECTORY_TESTS = "AndTest";
    private static final String EXT = ".json";

    private static Test instance;

    protected TestState state = new TestState();

    private Test() {
        super();
    }

    private Test(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public TestState getState() {
        return this.state;
    }

    public void setState(TestState state) {
        this.state = state;
    }

    public void setFile(String fileName) throws JSONException, FileNotFoundException {
        File file = new File(getDirectory() + File.separator + fileName);
        Test test = new Test(new JSONObject(getString(file)));
        test.state = new TestState();
        test.state.setTestHash(md5(file));

        Test.instance = test;
    }

    public long getTime() {
        long now = Calendar.getInstance().getTimeInMillis();
        return this.getTimeLimit() > 0
            ? (this.getTimeLimit() + this.getState().getStartedAt() - now)
            : (now - this.getState().getStartedAt());
    }

    static public Test getInstance() {
        if (null == instance) {
            instance = new Test();
        }
        return instance;
    }

    static public File getDirectory() {
        return Environment.getExternalStoragePublicDirectory(DIRECTORY_TESTS);
    }

    static public FilenameFilter getFileFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(EXT);
            }
        };
    }

    static private String md5(File file){
        String checksum = null;
        try {
            FileInputStream fis = new FileInputStream(file);
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

    static private String getString(File file) {
        StringBuilder content = new StringBuilder();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String readString = bufferedReader.readLine();
            while (readString != null) {
                content.append(readString);
                readString = bufferedReader.readLine();
            }
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
