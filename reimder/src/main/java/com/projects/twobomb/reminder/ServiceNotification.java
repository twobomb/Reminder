package com.projects.twobomb.reminder;

import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.GridLayout;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.projects.twobomb.reminder.MainActivity.log;

public class ServiceNotification extends Service {
    public static int wait_for = 5;
    BroadcastReceiver shadowing;
    private ArrayList<ItemInfo> items;
    final int FOREGROUND_ID = 228;
    public static final  String UPDATEDATA = " com.projects.twobomb.reminder.updatedata";
    public static final  String CHANGEVISION = " com.projects.twobomb.reminder.changevision";
    public static final  String UPDATE_NOTIFICATION= " com.projects.twobomb.reminder.updatenotif";
    public static final  String SOUND_MODE = " com.projects.twobomb.reminder.soundmode";
    NotificationCompat.Builder builder;
    Notification  notification;
    boolean neededNotifUpdate = true;
    public static enum typeNot{NOT_BEGIN,BEGIN,REST,PREV};
    RemoteViews expandedView;
    ExecutorService executor;
    int frequecy = 1;
    boolean serviceWork = true;
    String lastInfo = "";
    MyTimer soundNotif;
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if(serviceWork) {
            if (intent.getAction() != null)
                switch (intent.getAction()) {
                    case UPDATEDATA:
                        log("UPDATEDATA");
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                items.clear();
                                items = (ArrayList<ItemInfo>) SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.DATA_LESSONS);
                                Intent qq = new Intent(getApplicationContext(), ServiceNotification.class);
                                qq.setAction(UPDATE_NOTIFICATION);
                                startService(qq);
                                bindSoundNotification();
                            }

                        });

                        break;
                    case SOUND_MODE: {
                        log("SOUND_MODE");
                        if (intent.hasExtra("signal"))
                            SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.SIGNAL, intent.getIntExtra("signal", 0));
                        else
                            SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.SIGNAL, ((int) SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.SIGNAL) + 1) % 3);

                        Intent qq = new Intent(getApplicationContext(), ServiceNotification.class);
                        qq.setAction(UPDATE_NOTIFICATION);
                        startService(qq);
                        bindSoundNotification();
                        break;
                    }
                    case UPDATE_NOTIFICATION:
                        log("UPDATE_NOTIFICATION");
                        builder = null;
                        neededNotifUpdate = true;
                        doWork();
                        break;
                    case CHANGEVISION:
                        log("CHANGEVISION");
                        boolean vision = true;
                        for (ItemInfo ii : items)
                            if (ii.isActive) {
                                vision = false;
                                break;
                            }


                        changeVisionAll(vision);
                        break;
                    default:
                        log("NON PROCESSING INTENT");
                        break;
                }
        }
        else {
            log("SERVICE STOP BY NOT WORK");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public ServiceNotification() {

    }

    Notification buildNotif(String info,String shortInfo){
                if(builder == null) {
                    Intent resultIntent = new Intent(this, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                            0, PendingIntent.FLAG_UPDATE_CURRENT);

                    expandedView = new RemoteViews(this.getPackageName(),
                            R.layout.custrom_notif);
                    switch ((int)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.SIGNAL)){
                        case 0:
                            expandedView.setInt(R.id.notif_btn_sound, "setBackgroundResource",R.drawable.d_vibrate);
                        break;
                        case 1:
                            expandedView.setInt(R.id.notif_btn_sound, "setBackgroundResource",R.drawable.d_sound);
                        break;
                        case 2:
                            expandedView.setInt(R.id.notif_btn_sound, "setBackgroundResource",R.drawable.d_sound_off);
                        break;

                    }
                    expandedView.setInt(R.id.notif_btn_eye, "setBackgroundResource",R.drawable.d_eye_close);
                    for(int i = 0; i < items.size();i++)
                        if(items.get(i).isActive){
                            expandedView.setInt(R.id.notif_btn_eye, "setBackgroundResource",R.drawable.d_eye_open);
                            break;
                        }


                    expandedView.setTextViewText(R.id.txt_noti_info, info);

                    builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.remainder_ico)
                            .setAutoCancel(true)
                            .setContentIntent(resultPendingIntent)
                            .setContentTitle("Reminder")
                            .setContentText(shortInfo);


                    Intent tt = new Intent(this, SettingsActivity.class);
                    TaskStackBuilder ss = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(tt);
                    PendingIntent rr = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    expandedView.setOnClickPendingIntent(R.id.notif_btn_settings, rr);

                    tt = new Intent(this, ServiceNotification.class);
                    tt.setAction(SOUND_MODE);
                    rr = PendingIntent.getService(this,0,tt,PendingIntent.FLAG_UPDATE_CURRENT);
                    expandedView.setOnClickPendingIntent(R.id.notif_btn_sound, rr);


                    tt = new Intent(this, ServiceNotification.class);
                    tt.setAction(CHANGEVISION);
                    rr = PendingIntent.getService(this,0,tt,PendingIntent.FLAG_UPDATE_CURRENT);
                    expandedView.setOnClickPendingIntent(R.id.notif_btn_eye, rr);

                    builder.setCustomBigContentView(expandedView);

                }

        expandedView.setTextViewText(R.id.txt_noti_info,info);
        builder.setContentText(shortInfo);
        notification = builder.build();

        return notification;
    }

    void bindSoundNotification(){
        soundNotif.clearTasks();
        if((int)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.SIGNAL) == 2)
            return;
        final int notify_for = (int)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.NOTYFY_FOR)*60;
        Time now = new Time(System.currentTimeMillis());
        for(int i = 0; i < items.size();i++){
            if(!items.get(i).isActive)
                continue;
            ItemInfo ii = items.get(i);
            if(i == 0 && Time.isMoreFirst(ii.beginTime.remSeconds(notify_for),now))
                soundNotif.addTask(new Runnable() {
                    @Override
                    public void run() {
                        //За notify_for до начала занятий
                        notifySound(typeNot.PREV);
                    }
                },ii.beginTime.remSeconds(notify_for).toCurrentDate());
            if(Time.isMoreFirst(ii.beginTime,now))
                soundNotif.addTask(new Runnable() {
                    @Override
                    public void run() {
                        //Началопары\конец перемены
                        notifySound(typeNot.REST);
                    }
                },ii.beginTime.toCurrentDate());
            if(Time.isMoreFirst(ii.endTime.remSeconds(notify_for),now))
                soundNotif.addTask(new Runnable() {
                    @Override
                    public void run() {
                        //За Notify_for До конца пары
                        notifySound(typeNot.PREV);
                    }
                },ii.endTime.remSeconds(notify_for).toCurrentDate());

            if(Time.isMoreFirst(ii.endTime,now))
                soundNotif.addTask(new Runnable() {
                    @Override
                    public void run() {
                        //Конец пары
                        notifySound(typeNot.BEGIN);
                    }
                },ii.endTime.toCurrentDate());
        }
        log("NOTIFICATION BIND DONE!");
    }
    void notifySound(typeNot type){
                log("DZZZZZZZZZZZZZZZZZ");

                switch ((int)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.SIGNAL)) {
                    case 1:
                        RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
                    case 0: //vibrte
                        switch (type) {
                            case PREV:
                                ((Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE)).vibrate(new long[]{0, 100, 100, 100, 100, 200, 100}, -1);
                                break;
                            case REST:
                                ((Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE)).vibrate(new long[]{0, 700, 500, 100, 100, 100, 100, 100}, -1);
                                break;
                            case BEGIN:
                                long[] ln = new long[40];
                                for (int i = 0; i < 20; i++) {
                                    ln[i * 2] = i * 10;
                                    ln[i * 2 + 1] = i * 15;
                                }
                                ((Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE)).vibrate(ln, -1);
                                break;
                            case NOT_BEGIN:
                                ((Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE)).vibrate(new long[]{0, 2000, 1000, 1000, 1000, 500}, -1);
                                break;
                        }
                        break;
                }
    }
    boolean doWork(){
        String info = getInfo();
        if(!info.equals(lastInfo) || neededNotifUpdate) {
            String shortInfo = info;
            Pattern p = Pattern.compile("(.+)\r?\n?", Pattern.MULTILINE);
            Matcher m = p.matcher(info);
            if (m.find())
                shortInfo = m.group();
            startForeground(FOREGROUND_ID, buildNotif(info, shortInfo));
            lastInfo = info;
            neededNotifUpdate = false;
        }
        return true;
    }
    public void changeVisionAll(boolean act){
        for (int i = 0; i < items.size();i++)
            items.get(i).isActive = act;
        SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.DATA_LESSONS,items);
    }
    void init() {

            frequecy = (int)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.FREQUENCY);
            log("FREQUENCY:" + frequecy + "s");
            items = new ArrayList<ItemInfo>();
            wait_for = (int)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.NOTYFY_FOR);
            executor = new ScheduledThreadPoolExecutor(2);
            startForeground(FOREGROUND_ID, buildNotif("Нет данных", "Подробная информация"));
            final ExecutorService exec = executor;
            executor.submit(new Runnable() {
                @Override
                public void run() {

                    while (doWork() && !exec.isShutdown() && serviceWork)
                        try {
                            TimeUnit.SECONDS.sleep(frequecy);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    log("END_WORK");
                }
            });

        Intent upd = new Intent(getApplicationContext(),ServiceNotification.class);
        upd.setAction(UPDATEDATA);
        startService(upd);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        log("SERVICE CREATE");

        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
            case 1:case 7:log("SERVICE STOP BY Выходные");
                stopSelf();return;
        }
        serviceWork = (boolean)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.ON_NOTIFICATION);
        if (!serviceWork) {
            stopSelf();
            log("SERVICE STOP BY OFF_NOTIFICATION");
            return;
        }
        init();
        soundNotif =  new MyTimer();
        shadowing = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()){
                    case Intent.ACTION_SCREEN_OFF:
                        log("SCREEN_OFF:SERVICE_SLEEP");
                        if(executor != null && !executor.isShutdown())
                            executor.shutdown();
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        log("SCREEN_ONN:SERVICE_RUNNING");
                        init();
                    break;
                    case Intent.ACTION_TIME_CHANGED:
                        log("SYSTEM_TIME_CHANGED");
                        bindSoundNotification();
                    break;
                    case SettingsControl.ACTION_UPDATE_LESSON: case SettingsControl.ACTION_UPDATE_LESSON_DURATION: {
                        Intent upd = new Intent(getApplicationContext(), ServiceNotification.class);
                        upd.setAction(UPDATEDATA);
                        startService(upd);
                    }
                        break;
                    case SettingsControl.ACTION_UPDATE_SIGNAL: case SettingsControl.ACTION_UPDATE_NOTYFY_FOR:/*NOT_FOR!!!!*/ {
                        bindSoundNotification();
                        Intent upd = new Intent(getApplicationContext(),ServiceNotification.class);
                        upd.setAction(UPDATE_NOTIFICATION);
                        startService(upd);
                    }break;
                    case SettingsControl.ACTION_UPDATE_FREQUENCY:
                        frequecy = (int)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.FREQUENCY);
                        if(executor != null && !executor.isShutdown())
                            executor.shutdown();
                        init();
                        break;
                    case SettingsControl.ACTION_UPDATE_ON_NOTIFICATION:
                        serviceWork = (boolean)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.ON_NOTIFICATION);
                        if(!serviceWork)
                            stopSelf();
                        break;
                }
            }
        };
        IntentFilter inf = new IntentFilter();
//        inf.addAction(Intent.ACTION_USER_PRESENT);//Когда разблокировал телефон(рисунок) и вышел на рабочий стол
        inf.addAction(Intent.ACTION_SCREEN_ON);
        inf.addAction(Intent.ACTION_SCREEN_OFF);
        inf.addAction(SettingsControl.ACTION_UPDATE_LESSON);
        inf.addAction(SettingsControl.ACTION_UPDATE_LESSON_DURATION);
        inf.addAction(SettingsControl.ACTION_UPDATE_FREQUENCY);
        inf.addAction(SettingsControl.ACTION_UPDATE_SIGNAL);
        inf.addAction(SettingsControl.ACTION_UPDATE_NOTYFY_FOR);
        inf.addAction(SettingsControl.ACTION_UPDATE_ON_NOTIFICATION);
        inf.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(shadowing,inf);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(executor != null && !executor.isShutdown())
            executor.shutdown();
        if(soundNotif!=null) {
            soundNotif.stop();
        }
        if(shadowing!=null)
          unregisterReceiver(shadowing);
        log("SERVICE DESTROY");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public int getIndexFirstLesson(){
        for (int i =0;i < items.size();i++)
            if(items.get(i).isActive)
                return i;
        return 0;
    }

    public int getIndexLastLesson(){
        for (int i = items.size()-1;i >= 0;i--)
            if(items.get(i).isActive)
                return i;
        return items.size()-1;
    }
    public String getInfo(){
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
            case 1:
                stopSelf();
                log("SERVICE STOP BY Сегодня воскресенье!Выходной.");
                return "Сегодня воскресенье!Выходной.";
            case 7:
                stopSelf();
                log("SERVICE STOP BY Сегодня суббота!Выходной.");
                return "Сегодня суббота!Выходной.";
        }
        int activeLess = 0;
        for(int i = 0; i < items.size();i++)
            if(items.get(i).isActive)
                activeLess++;
        String res = "";
        if(activeLess <= 0 || items.size() == 0) {
            return "Сегодня нет пар";
        }
        Time now = new Time(System.currentTimeMillis());
        for(int i = 0; i < items.size();i++){
            if(!items.get(i).isActive)
                continue;
            ItemInfo cur = items.get(i);
            if(Time.isMoreFirst(cur.beginTime,now)){
                if(i == getIndexFirstLesson())
                    res+= "До начала пар ";
                else
                    res+="До начала пары ";
                res+= ItemInfo.getTextDifference(cur.beginTime,now);
                if(i == getIndexFirstLesson())
                    res+= "\nПары еще не начались";
                else if(items.get(i-1).isActive)
                    res+="\nСейчас перерыв "+ItemInfo.getTextDifference(items.get(i).beginTime,items.get(i-1).endTime)+" между "+(i) +" и "+(i+1) + " парой";
                break;
            }
            else if(Time.isMoreFirst(cur.endTime,now)){
                res+="До конца пары " + ItemInfo.getTextDifference(cur.endTime,now);
                if(i == getIndexLastLesson())
                    res+="\nСейчас идет последняя пара("+(i+1)+"-я)";
                else
                    res+="\nСейчас идет "+(i+1)+ " пара из "+activeLess;
                break;
            }
            else if(i >= getIndexLastLesson()){
                res+="Пары закончились!";
                stopSelf();
                log("SERVICE STOP BY Пары закончились!");
                return res;
            }
        }
        if(Time.isMoreFirst(now,items.get(getIndexFirstLesson()).beginTime))
            res+="\nДо конца пар "+ ItemInfo.getTextDifference(items.get(getIndexLastLesson()).endTime,now);

        return res;
    }
}
