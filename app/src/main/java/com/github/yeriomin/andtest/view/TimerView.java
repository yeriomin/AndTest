package com.github.yeriomin.andtest.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.yeriomin.andtest.DbHelper;
import com.github.yeriomin.andtest.R;
import com.github.yeriomin.andtest.activity.QuestionListActivity;
import com.github.yeriomin.andtest.model.Test;

import java.util.Timer;
import java.util.TimerTask;

public class TimerView extends TextView {

    private Timer timer;
    private Test test;

    public void pause() {
        this.timer.cancel();
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public void setTime(long millis) {
        int secondsOverall = (int) (millis/1000);
        int minutes = secondsOverall/60;
        int seconds = secondsOverall%60;
        String secondsString = (seconds < 10 ? "0" : "") + seconds;

        this.setText(this.getContext().getString(R.string.text_time, minutes, secondsString));
    }

    public TimerView(Context context) {
        this(context, null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void launch() {
        this.timer = new Timer();
        final Handler handler = new ClockHandler(this);
        TimerTask task = new TimerTask() {
            public void run() {
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.setData(data);
                handler.sendMessage(msg);
            }
        };
        int period = 500;
        timer.scheduleAtFixedRate(task, 0, period);
    }

    class ClockHandler extends Handler {

        private TimerView view;

        public ClockHandler(TimerView view) {
            this.view = view;
        }

        @Override
        public void handleMessage(Message msg) {
            long time = test.getTime();
            if (time < 0) {
                test.getState().finish();
                DbHelper.getDbHelper(this.view.getContext()).save(test.getState());
                DbHelper.closeDbHelper();
                timer.cancel();
                this.view.getContext().startActivity(new Intent(this.view.getContext(), QuestionListActivity.class));
            } else if (test.getState().isFinished()) {
                timer.cancel();
            }

            this.view.setTime(time);
        }
    }
}

