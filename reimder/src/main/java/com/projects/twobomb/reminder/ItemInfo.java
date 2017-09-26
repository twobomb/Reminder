package com.projects.twobomb.reminder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ItemInfo {
    Time beginTime;
    Time endTime;
    public boolean isNeedAnimate = false;
    public boolean isActive = true;
    public static Time lengthLection = new Time(1,20);//длина пары в мин
    public  ItemInfo(Time bt){
        beginTime = bt;
        endTime =  Time.foldTimes(beginTime,lengthLection);
    }
    public  ItemInfo(Time bt, Time lect, boolean isAct){
        beginTime = bt;
        lengthLection = lect;
        isActive = isAct;
        endTime =  Time.foldTimes(beginTime,lengthLection);
    }
    public String getBeginTime(){
        return new SimpleDateFormat("H:mm").format(new Date(beginTime.getTime()));
    }
    public String getEndTime(){
        return new SimpleDateFormat("H:mm").format(new Date(endTime.getTime()));
    }
    public void setBeginTime(Time dt){
        beginTime = dt;
        endTime =  Time.foldTimes(beginTime,lengthLection);
    }
    public void setLengthLection(Time tm){
        lengthLection = tm;
        endTime =  Time.foldTimes(beginTime,lengthLection);
    }
    public static String getTextDifference(Time first,Time second){
        Time diff = Time.subtractMoreSmall(first,second);

        String res = "";
        if(diff.getHours() > 0)
            res+=diff.getHours()+"час.";
        if(diff.getMinutes() > 0)
            res+=diff.getMinutes()+"мин.";
        if(diff.getHours() <= 0 && diff.getMinutes() <= 0)
            res+=diff.getSeconds()+"сек. ";
        return res;
    }
    public int getColor(Context ctx) {
        Time now = new Time(System.currentTimeMillis());
        if(Time.isMoreFirst(now, beginTime) && Time.isMoreFirst(new Time(beginTime.getTime()+lengthLection.getTime()),now))
            return ContextCompat.getColor(ctx,R.color.cur_less);
        else if(Time.isMoreFirst(beginTime,now))
            return ContextCompat.getColor(ctx,R.color.next_less);
        else
            return ContextCompat.getColor(ctx,R.color.last_less);
    }

    public String getTimeToEnd(){//Через сколько конец
        Time now = new Time(System.currentTimeMillis());

        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
            case 1:case 7:return "";
        }
        if(!isActive)
            return "";
        String res;
        if(Time.isMoreFirst(now, beginTime) && Time.isMoreFirst(endTime,now))
            res = "Конец через "+getTextDifference(new Time(beginTime.getTime()+lengthLection.getTime()),now);
        else if(Time.isMoreFirst(beginTime,now))
            res = "Начало через "+getTextDifference(beginTime,now);
        else
            res = "Закончилась "+getTextDifference(now, beginTime)+"назад";

        return res;
    }
    static void sortableFromEndTime(ArrayList<ItemInfo> arr){//Сортирует по времени началу пар
        for(int i = 0; i < arr.size();i++)
            for(int j = 0; j < arr.size() - 1;j++)
                if(isFirstMore(arr.get(j),arr.get(j+1))) {
                    ItemInfo tmp = arr.get(j);
                    arr.set(j,arr.get(j+1));
                    arr.set(j+1,tmp);
                }
    }
    static boolean isFirstMore(ItemInfo first, ItemInfo second){
        if(first.beginTime.getHours() - second.beginTime.getHours() > 0 || first.beginTime.getHours() - second.beginTime.getHours() == 0 && first.beginTime.getMinutes() - second.beginTime.getMinutes() > 0)
            return true;
        return false;
    }
}
