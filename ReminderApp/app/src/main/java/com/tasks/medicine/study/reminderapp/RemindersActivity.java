package com.tasks.medicine.study.reminderapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class RemindersActivity extends AppCompatActivity implements ActionMode.Callback, ReminderInterface {

    private List<Reminder> reminderList = new ArrayList<>();
    private RecyclerView reminderRecyclerView;
    private ReminderAdapter reminderLAdapter;
    private ReminderDatabaseAdapter remindersDatabaseAdapter;
    private ActionMode rAActionMode;
    private boolean rAIsMultiSelect = false;
    private List<Integer> rASelectedPositions = new ArrayList<>();
    private FloatingActionButton rAActionButton;
    protected TextView rAESTitleTextView;
    protected TextView rAESContentTextView;
    protected LinearLayout rAESLinearLayout;
    View rARootLayout;

    private AdView rAAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        rAAdView = findViewById(R.id.ra_ad_view);
        AdRequest pAAdRequest = new AdRequest.Builder().build();
        rAAdView.loadAd(pAAdRequest);

        Float elevation = 0.0f;
        getSupportActionBar().setElevation(elevation);

        rARootLayout = findViewById(R.id.ch_root_layout);

        remindersDatabaseAdapter = new ReminderDatabaseAdapter(this);
        remindersDatabaseAdapter.open();

        initializeReminderList();

        reminderRecyclerView = (RecyclerView) findViewById(R.id.reminders_recycler_view);
        rAESTitleTextView = findViewById(R.id.ra_empty_state_title_text_view);
        rAESContentTextView = findViewById(R.id.ra_empty_state_text_view);
        rAESLinearLayout = findViewById(R.id.ra_empty_state_linear_layout);

        reminderLAdapter = new ReminderAdapter(this, reminderList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        reminderRecyclerView.setLayoutManager(layoutManager);
        reminderRecyclerView.setItemAnimator(new DefaultItemAnimator());
        reminderRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, reminderRecyclerView, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (rAIsMultiSelect) {
                    multiSelect(position);
                }

                else {
                    openEditDialog(position);
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!rAIsMultiSelect) {
                    rASelectedPositions = new ArrayList<>();
                    rAIsMultiSelect = true;

                    if (rAActionMode == null) {
                        rAActionButton.hide();
                        rAActionMode = startActionMode(RemindersActivity.this);
                    }
                }

                multiSelect(position);
            }
        }));
        reminderRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reminderRecyclerView.setAdapter(reminderLAdapter);

        setNextReminderAlarm();
        setRAEmptyState();

        rAActionButton = findViewById(R.id.new_reminder_fab);
        rAActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewReminderDialog();
            }
        });
    }

    private void openNewReminderDialog(){
        FragmentManager oNRFSD = getSupportFragmentManager();
        ReminderFSD newReminderFSD =  ReminderFSD.newInstance(0,"","","",0,0,false);
        FragmentTransaction transaction = oNRFSD.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newReminderFSD).addToBackStack(null).commit();
    }

    private int getNextReminderAPosition(){
        int nRAPosition = 0;

        int i = 0;
        while (i >= 0 && i<=reminderList.size()-1){
            Reminder bReminder = reminderList.get(i);
            long nowTIM = getNowTIM();
            long bReminderTIM = bReminder.getReminderTIM();

            if(bReminderTIM > nowTIM){
                nRAPosition=i;
                i=reminderList.size();
            }

            ++i;
        }

        return nRAPosition;
    }

    private void setNextReminderAlarm() {

        if (reminderList != null) {

            if (reminderList.size() != 0) {

                int nRAPosition = getNextReminderAPosition();

                Reminder latestReminder = reminderList.get(nRAPosition);

                long nowTIM = getNowTIM();
                long lReminderTIM = latestReminder.getReminderTIM();


                if (lReminderTIM >= nowTIM) {

                    Intent rARIntent = new Intent(this, NotificationReceiver.class);
                    rARIntent.putExtra("lReminderTitle", latestReminder.getReminderTitle());
                    PendingIntent rARPIntent = PendingIntent.getBroadcast(this, 100, rARIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager rAAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);


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

    private void openEditDialog(int reminderPosition){

        Reminder selectedReminder = reminderList.get(reminderPosition);
        int reminderId = selectedReminder.getReminderId();
        String reminderTitle = selectedReminder.getReminderTitle();
        String reminderDOF = selectedReminder.getReminderDOF();
        String reminderTOF = selectedReminder.getReminderTOF();
        long reminderTIM = selectedReminder.getReminderTIM();

        FragmentManager openERFSD = getSupportFragmentManager();
        ReminderFSD editReminderFSD =  ReminderFSD.newInstance(reminderId,reminderTitle,reminderDOF,reminderTOF,reminderTIM,reminderPosition,true);
        FragmentTransaction oEditRFSDTransaction = openERFSD.beginTransaction();
        oEditRFSDTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        oEditRFSDTransaction.add(android.R.id.content, editReminderFSD).addToBackStack(null).commit();
    }

    private int getNewReminderAddPosition(Reminder newReminder) {

        List<Reminder> remindersSortList = reminderList;
        remindersSortList.add(newReminder);

        ReminderComparator reminderComparator = new ReminderComparator();
        Collections.sort(remindersSortList, reminderComparator);

        int newReminderPosition = 0;

        int i = 0;
        while (i >= 0 && i <= (remindersSortList.size() - 1)) {

            int reminderId = reminderList.get(i).getReminderId();

            if (reminderId == newReminder.getReminderId()) {
                newReminderPosition = i;
            }

            ++i;
        }

        return newReminderPosition;
    }

    public void addReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM) {
        final int[] newReminderId = new int[1];

        Runnable addReminderRunnable = new Runnable() {
            @Override
            public void run() {
                newReminderId[0] = remindersDatabaseAdapter.createReminder(reminderTitle, reminderDTS);

            }
        };
        Thread addReminderThread = new Thread(addReminderRunnable);
        addReminderThread.start();
        try {
            addReminderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int reminderId = newReminderId[0];
        String reminderDOF = reminderDTS.substring(7, 17);
        String reminderTOF = reminderDTS.substring(0, 5);
        Reminder newReminder = new Reminder(reminderId, reminderTitle, reminderDOF, reminderTOF, reminderTIM);

        addReminderToList(newReminder);

        setRAEmptyState();
        setNextReminderAlarm();

        Snackbar.make(rARootLayout, R.string.reminder_set_c,Snackbar.LENGTH_SHORT).show();
    }

    public void updateReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM, final int reminderId, final int reminderPosition) {
        Runnable updateReminderRunnable = new Runnable() {
            @Override
            public void run() {
                remindersDatabaseAdapter.updateReminder(reminderId,reminderTitle,reminderDTS);
            }
        };
        Thread updateReminderThread = new Thread(updateReminderRunnable);
        updateReminderThread.start();
        try {
            updateReminderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        updateReminderListItem(reminderTitle,reminderDTS,reminderTIM,reminderPosition);

        setNextReminderAlarm();

        Snackbar.make(rARootLayout, "Reminder Updated",Snackbar.LENGTH_SHORT).show();
    }

    public void hideActionBar() {
        getSupportActionBar().hide();
    }

    public void showActionBar() {
        getSupportActionBar().show();
    }

    private void multiSelect(int position) {
        Reminder selectedReminder = reminderLAdapter.getItem(position);
        if (selectedReminder != null) {
            if (rAActionMode != null) {
                int previousPosition = -1;
                if (rASelectedPositions.size() > 0) {
                    previousPosition = rASelectedPositions.get(0);
                }
                rASelectedPositions.clear();
                rASelectedPositions.add(position);

                reminderLAdapter.setSelectedPositions(previousPosition, rASelectedPositions);
            }
        }
    }

    private void initializeReminderList() {
        Runnable initializeRListRunnable = new Runnable() {
            @Override
            public void run() {
                reminderList = remindersDatabaseAdapter.fetchReminders();
            }
        };
        Thread initializeRListThread = new Thread(initializeRListRunnable);
        initializeRListThread.start();
        try {
            initializeRListThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addReminderToList(Reminder newReminder) {
        int newReminderPosition = getNewReminderAddPosition(newReminder);
        reminderLAdapter.notifyDataSetChanged();

        if(newReminderPosition>=0 && newReminderPosition <= (reminderList.size()-1)){
            reminderRecyclerView.scrollToPosition(newReminderPosition);
        }

    }

    private long getNowTIM() {
        Date nowDate = new Date();
        long nowTIM = nowDate.getTime();
        return nowTIM;
    }

    private void deleteReminderListItem(int reminderPosition) {
        reminderList.remove(reminderPosition);
        reminderLAdapter.notifyItemRemoved(reminderPosition);
        setNextReminderAlarm();
    }

    private void updateReminderListItem(String reminderTitle, String reminderDTS, long reminderTIM, int reminderPosition) {
        String reminderDOF = reminderDTS.substring(7,17);
        String reminderTOF = reminderDTS.substring(0,5);

        reminderList.get(reminderPosition).setReminderTitle(reminderTitle);
        reminderList.get(reminderPosition).setReminderDOF(reminderDOF);
        reminderList.get(reminderPosition).setReminderTOF(reminderTOF);
        reminderList.get(reminderPosition).setReminderTIM(reminderTIM);

        ReminderComparator reminderComparator = new ReminderComparator();
        Collections.sort(reminderList,reminderComparator);

        reminderLAdapter.notifyDataSetChanged();
    }

    private void setRAEmptyState() {

        if (reminderList.size() == 0) {

            if (reminderRecyclerView.getVisibility() == View.VISIBLE) {
                reminderRecyclerView.setVisibility(GONE);
            }

            if (rAESLinearLayout.getVisibility() == GONE) {
                rAESLinearLayout.setVisibility(View.VISIBLE);

                String rAESTitle = getResources().getString(R.string.ra_empty_state_title);
                String rAESText = getResources().getString(R.string.ra_empty_state_text);

                rAESTitleTextView.setText(rAESTitle);
                rAESContentTextView.setText(rAESText);
            }


        } else {

            if (rAESLinearLayout.getVisibility() == View.VISIBLE) {
                rAESLinearLayout.setVisibility(GONE);
            }

            if (reminderRecyclerView.getVisibility() == GONE) {
                reminderRecyclerView.setVisibility(View.VISIBLE);
            }

        }


    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.ra_action_view_menu, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.ra_action_copy:
                if (rASelectedPositions.size() > 0) {

                    String selectedReminderTitle = reminderList.get(rASelectedPositions.get(0)).getReminderTitle();

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("l", selectedReminderTitle);
                    clipboard.setPrimaryClip(clip);
                }

                rAActionMode.finish();

                String rACopyConfirmationText = getResources().getString(R.string.ra_copy_confirmation_text);

                Snackbar.make(rARootLayout,rACopyConfirmationText,Snackbar.LENGTH_SHORT).show();

                return true;

            case R.id.ra_action_edit:

                if (rASelectedPositions.size() > 0) {
                    int selectedPosition = rASelectedPositions.get(0);
                    openEditDialog(selectedPosition);
                }

                rAActionMode.finish();
                return true;


            case R.id.ra_action_delete:
                AlertDialog.Builder deleteRDialogBuilder = new AlertDialog.Builder(this);
                deleteRDialogBuilder.setTitle(getResources().getString(R.string.delete_reminder_dialog_title));
                deleteRDialogBuilder.setMessage(getResources().getString(R.string.delete_reminder_dialog_message));
                deleteRDialogBuilder.setNegativeButton(getResources().getString(R.string.del_reminder_dialog_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        rAActionMode.finish();
                    }
                });

                deleteRDialogBuilder.setPositiveButton(getResources().getString(R.string.delete_reminder_dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (rASelectedPositions.size() > 0) {

                            int selectedPosition = rASelectedPositions.get(0);
                            final int sReminderId = reminderList.get(selectedPosition).getReminderId();

                            Runnable deleteRRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    remindersDatabaseAdapter.deleteReminder(sReminderId);
                                }
                            };
                            Thread deleteRThread = new Thread(deleteRRunnable);
                            deleteRThread.start();
                            try {
                                deleteRThread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            deleteReminderListItem(selectedPosition);
                            setRAEmptyState();
                        }

                        dialogInterface.dismiss();

                        rAActionMode.finish();
                    }
                });
                deleteRDialogBuilder.create().show();

                return true;


            default:

        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

        rAActionButton.show();
        rAActionMode = null;
        rAIsMultiSelect = false;

        int previousPosition = -1;
        if (rASelectedPositions.size() > 0) {
            previousPosition = rASelectedPositions.get(0);
        }
        rASelectedPositions = new ArrayList<>();

        reminderLAdapter.setSelectedPositions(previousPosition, new ArrayList<Integer>());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rem_activity_options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rem_activity_help:
                openHelp();
                return true;

            case R.id.rem_activity_sa:
                shareApp();
                return true;

            default:
                return false;
        }
    }

    protected void shareApp(){
        Intent sAIntent = new Intent();
        sAIntent.setAction(Intent.ACTION_SEND);
        sAIntent.putExtra(Intent.EXTRA_TEXT,"Reminder App is a fast and simple app that I use to schedule tasks. Get it from: https://play.google.com/store/apps/details?id="+getPackageName());
        sAIntent.setType("text/plain");
        Intent.createChooser(sAIntent,"Share via");
        startActivity(sAIntent);
    }

    protected void openHelp(){
        Intent remToHelpActivityIntent = new Intent(RemindersActivity.this,HelpActivity.class);
        startActivity(remToHelpActivityIntent);
    }

    protected static class ReminderComparator implements Comparator<Reminder> {

        public int compare(Reminder reminderOne, Reminder reminderTwo) {
            if (reminderOne.getReminderTIM() == reminderTwo.getReminderTIM()) {
                return 0;
            } else if (reminderOne.getReminderTIM() > reminderTwo.getReminderTIM()) {
                return 1;
            } else {
                return -1;
            }
        }


    }

}


