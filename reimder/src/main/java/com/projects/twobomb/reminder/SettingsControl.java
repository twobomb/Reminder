package com.projects.twobomb.reminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public  class SettingsControl {
    public static final String SETTINGS_NAME = "settings";
    public static enum SETTINGS{DATA_LESSONS,ON_NOTIFICATION,NOTYFY_FOR,THEME,SIGNAL,LESSON_DURATION,FREQUENCY};
    public static final String ACTION_UPDATE_LESSON = "com.projects.twobomb.reminder.ACTION_UPDATE_LESSON";
    public static final String ACTION_UPDATE_ON_NOTIFICATION = "com.projects.twobomb.reminder.ACTION_UPDATE_ON_NOTIFICATION";
    public static final String ACTION_UPDATE_THEME = "com.projects.twobomb.reminder.ACTION_UPDATE_THEME";
    public static final String ACTION_UPDATE_NOTYFY_FOR = "com.projects.twobomb.reminder.ACTION_UPDATE_NOTYFY_FOR";
    public static final String ACTION_UPDATE_LESSON_DURATION = "com.projects.twobomb.reminder.ACTION_UPDATE_LESSON_DURATION";
    public static final String ACTION_UPDATE_FREQUENCY = "com.projects.twobomb.reminder.ACTION_UPDATE_FREQUENCY";
    public static final String ACTION_UPDATE_SIGNAL = "com.projects.twobomb.reminder.ACTION_UPDATE_SIGNAL";
    public static Object getData(Context ctx,SETTINGS set){
        SharedPreferences sp = ctx.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        switch (set) {
            case DATA_LESSONS:
                ArrayList<ItemInfo> arr_sb = new ArrayList<ItemInfo>();
                if(sp.contains("data_lessons")) {
                    HashSet<String> setStr = (HashSet<String>)sp.getStringSet("data_lessons",new HashSet<String>());
                    Time lectTime = (Time)getData(ctx,SETTINGS.LESSON_DURATION);
                    for(String str:setStr) {
                        arr_sb.add(new ItemInfo(new Time(Integer.parseInt(str.split(":")[1]), Integer.parseInt(str.split(":")[2]), 0), lectTime, str.split(":")[0].equals("1")));
                    }
                }else {
                    arr_sb.add(new ItemInfo(new Time(7, 30, 0)));
                    arr_sb.add(new ItemInfo(new Time(9, 0, 0)));
                    arr_sb.add(new ItemInfo(new Time(10, 35, 0)));
                    arr_sb.add(new ItemInfo(new Time(12, 20, 0)));
                    arr_sb.add(new ItemInfo(new Time(15, 20, 0)));
                    arr_sb.add(new ItemInfo(new Time(13, 50, 0)));
                }
                ItemInfo.sortableFromEndTime(arr_sb);
                return arr_sb;
            case ON_NOTIFICATION:return sp.getBoolean("on_notification", true);
            case NOTYFY_FOR:return sp.getInt("notify_for", 5);
            case THEME: return sp.getInt("theme", R.style.MyLightTheme);
            case SIGNAL: return sp.getInt("signal", 0);
            case FREQUENCY: return sp.getInt("freq", 1);
            case LESSON_DURATION:
                String []str = sp.getString("lessonDuration","1:20").split(":");
                if(str.length < 2)
                    break;
                return new Time(Integer.parseInt(str[0]),Integer.parseInt(str[1]));
        }
        return null;
    }
    public static void clearData(Context ctx){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        Intent broadcaseNot = new Intent();
        broadcaseNot.setAction(ACTION_UPDATE_LESSON);
        ctx.sendBroadcast(broadcaseNot);
        broadcaseNot.setAction(ACTION_UPDATE_NOTYFY_FOR);
        ctx.sendBroadcast(broadcaseNot);
        broadcaseNot.setAction(ACTION_UPDATE_ON_NOTIFICATION);
        ctx.sendBroadcast(broadcaseNot);
        broadcaseNot.setAction(ACTION_UPDATE_THEME);
        ctx.sendBroadcast(broadcaseNot);
        broadcaseNot.setAction(ACTION_UPDATE_SIGNAL);
        ctx.sendBroadcast(broadcaseNot);
        broadcaseNot.setAction(ACTION_UPDATE_LESSON_DURATION);
        ctx.sendBroadcast(broadcaseNot);
    }
    public static void setData(Context ctx,SETTINGS set,Object val){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE).edit();
        Intent broadcaseNot = new Intent();
        switch (set) {
            case DATA_LESSONS://val == ArrayList<ItemInfo>
                Set<String> str = new HashSet<String>();
                for(ItemInfo ii:(ArrayList<ItemInfo>)val)
                    str.add((ii.isActive?"1":"0")+":"+ii.beginTime.getHours()+":"+ii.beginTime.getMinutes());
                editor.putStringSet("data_lessons",str);
                broadcaseNot.setAction(ACTION_UPDATE_LESSON);
                break;
            case ON_NOTIFICATION:
                editor.putBoolean("on_notification", (boolean)val);
                broadcaseNot.setAction(ACTION_UPDATE_ON_NOTIFICATION);
                break;
            case NOTYFY_FOR:
                editor.putInt("notify_for", (int)val);
                broadcaseNot.setAction(ACTION_UPDATE_NOTYFY_FOR);
                break;
            case THEME:
                editor.putInt("theme", (int)val);
                broadcaseNot.setAction(ACTION_UPDATE_THEME);
                break;
            case FREQUENCY:
                editor.putInt("freq", (int)val);
                broadcaseNot.setAction(ACTION_UPDATE_FREQUENCY);
                break;
            case SIGNAL:
                editor.putInt("signal", (int)val);
                broadcaseNot.setAction(ACTION_UPDATE_SIGNAL);
                break;
            case LESSON_DURATION://val = Time.class
                editor.putString("lessonDuration",((Time)val).getHours()+":"+((Time)val).getMinutes());
                broadcaseNot.setAction(ACTION_UPDATE_LESSON_DURATION);
                break;
        }
        editor.apply();
        ctx.sendBroadcast(broadcaseNot);
    }

}
