package com.tasks.medicine.study.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {
    private ReminderDatabaseAdapter remindersDatabaseAdapter;
    private List<Reminder> reminderList = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if(extras != null){
            String rRTitle = intent.getExtras().getString("lReminderTitle");

            if(rRTitle != null){
                if(rRTitle.length() > 0){
                    Intent reminderSNIntent = new Intent(context, NotificationService.class);
                    reminderSNIntent.putExtra("rRTitle",rRTitle);
                    NotificationService.enqueueWork(context, reminderSNIntent);
                }
            }

        }

        setNextReminderAlarm(context);
    }

    private int getNextReminderAPosition(){
        int nRAPosition = 0;

        int i=0;
        while (i >= 0 && i <= (reminderList.size()-1)){
            Reminder bReminder = reminderList.get(i);
            long nowTIM = getNowTIM();
            long bReminderTIM = bReminder.getReminderTIM();

            if(bReminderTIM > nowTIM){
                nRAPosition = i;
                i = reminderList.size();
            }

            ++i;
        }

        return nRAPosition;
    }

    public void setNextReminderAlarm(Context context){

        remindersDatabaseAdapter = new ReminderDatabaseAdapter(context);
        remindersDatabaseAdapter.open();

        Runnable initializeReminderListRunnable = new Runnable() {
            @Override
            public void run() {
                reminderList = remindersDatabaseAdapter.fetchReminders();

            }
        };
        Thread initializeReminderListThread = new Thread(initializeReminderListRunnable);
        initializeReminderListThread.start();
        try {
            initializeReminderListThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (reminderList != null) {

            if (reminderList.size() != 0) {

                int nRAPosition = getNextReminderAPosition();
                Reminder latestReminder = reminderList.get(nRAPosition);

                long nowTIM = getNowTIM();
                long lReminderTIM = latestReminder.getReminderTIM();

                if (lReminderTIM >= nowTIM) {

                    Intent rARIntent = new Intent(context, NotificationReceiver.class);
                    rARIntent.putExtra("lReminderTitle", latestReminder.getReminderTitle());
                    PendingIntent rARPIntent = PendingIntent.getBroadcast(context, 100, rARIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager rAAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


                    int SDK_INT = Build.VERSION.SDK_INT;
                    if (SDK_INT < Build.VERSION_CODES.KITKAT) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.set(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);

                    } else if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);

                    } else if (SDK_INT >= Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    }

                }

            }

        }

    }

    protected long getNowTIM() {
        Date nowDate = new Date();
        long nowTIM = nowDate.getTime();
        return nowTIM;
    }

}
