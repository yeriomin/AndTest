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
import com.github.yeriomin.andtest.activity.TestResultActivity;
import com.github.yeriomin.andtest.model.Test;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class TimerView extends TextView {

    public void setTime(long millis) {
        int secondsOverall = (int) (millis/1000);
        int minutes = secondsOverall/60;
        int seconds = secondsOverall%60;
        String secondsString = (seconds < 10 ? "0" : "") + seconds;

        this.setText(this.getContext().getString(R.string.text_time, minutes, secondsString));
    }

    public TimerView(Context context) {
        this(context, (AttributeSet) null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void launch(Test test) {
        final Timer timer = new Timer();
        final Handler handler = new ClockHandler(timer, test, this);
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
}

class ClockHandler extends Handler {

    private Timer timer;
    private Test test;
    private TimerView view;

    public ClockHandler(Timer timer, Test test, TimerView view) {
        this.timer = timer;
        this.test = test;
        this.view = view;
    }

    @Override
    public void handleMessage(Message msg) {
        long now = Calendar.getInstance().getTimeInMillis();
        long time =  this.test.getTimeLimit() > 0
                ? (this.test.getTimeLimit() + this.test.getStartedAt() - now)
                : (now - this.test.getStartedAt());
        if (time < 0) {
            this.test.finish();
            DbHelper.getDbHelper(this.view.getContext()).save(this.test);
            DbHelper.closeDbHelper();
            this.timer.cancel();
            this.view.getContext().startActivity(new Intent(this.view.getContext(), TestResultActivity.class));
        } else if (this.test.isFinished()) {
            this.timer.cancel();
        }

        this.view.setTime(time);
    }
}

