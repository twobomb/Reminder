package com.projects.twobomb.reminder;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time {
        private Integer h = 0,m = 0 ,s = 0;
        public Time(Integer hour,Integer minute){
            addHour(hour,true);
            addMinute(minute,true);
        }
        public Time(Integer hour,Integer minute,Integer sec){
            addHour(hour,true);
            addMinute(minute,true);
            addSeconds(sec,true);
        }
        public Time(long date){
            h = Integer.parseInt(new SimpleDateFormat("HH").format(date));
            m = Integer.parseInt(new SimpleDateFormat("mm").format(date));
            s = Integer.parseInt(new SimpleDateFormat("ss").format(date));
        }
        public long getTime(){
            return new SimpleDateFormat("HH:mm:ss").parse(h.toString()+":"+m.toString()+":"+s.toString(),new ParsePosition(0)).getTime();
        }
        public int getHours(){
            return h;
        }
        public int getMinutes(){
            return m;
        }
        public int getSeconds(){
            return s;
        }
        @Override
        public String toString() {
            return h.toString()+":"+m.toString()+":"+s.toString();
        }

        public void setHour(int hour){
            h = hour%24;
        }
        public void setMinute(int minute){
            h = minute%60;
        }
        public void setSeconds(int second){
            h = second%60;
        }
        public Time addHour(int hour){
            return new Time((h+hour)%24,m,s);
        }
        public Time addMinute(int min) {
            int tmpM= m+min;
            int tmpH = h+(int)(Math.floor((float)tmpM/60f))%24;
            tmpM%=60;
            return new Time(tmpH,tmpM,s);
        }
        public Time addSeconds(int sec){
            int tmpS = (s+sec);

            int tmpM = m+(int)Math.floor((float)tmpS/60f);
            tmpS %=60;

            int tmpH = h+(int)Math.floor((float)tmpM/60f);
            tmpM %= 60;
            return new Time(tmpH,tmpM,tmpS);
        }
        public void addHour(int hour,boolean changeObject){
            if(changeObject)
                h = (h+hour)%24;
        }
        public void addMinute(int min,boolean changeObject){
            int tmpM= m+min;
            int tmpH = h+(int)(Math.floor((float)tmpM/60f))%24;
            tmpM%=60;
            if(changeObject){
                h=tmpH;
                m = tmpM;
            }
        }
        public void addSeconds(int sec,boolean changeObject){
            int tmpS = (s+sec);

            int tmpM = m+(int)Math.floor((float)tmpS/60f);
            tmpS %=60;

            int tmpH = h+(int)Math.floor((float)tmpM/60f);
            tmpM %= 60;
            if(changeObject){
                h = tmpH;
                m = tmpM;
                s = tmpS;
            }
        }
        public Time remHour(int hour){
            int tmp =h-hour;
            if(tmp < 0)
                tmp = 24-Math.abs(tmp)%24;
            return new Time(tmp%24,m,s);
        }
        public Time remMinute(int min){
            int tmpM = (m-min);
            int tmpH = h;
            if(tmpM < 0) {
                tmpH -= (int) Math.ceil(Math.abs((float)tmpM) / 60f);
                tmpM = (60 - (Math.abs(tmpM) % 60))%60;
                if(tmpH < 0)
                    tmpH = 24- Math.abs(tmpH)%24;
            }
            return new Time(tmpH,tmpM,s);
        }
        public Time remSeconds(int seconds){
            int tmpS = s - seconds;
            int tmpH = h;
            int tmpM = m;
            if(tmpS < 0){
                tmpM -= (int)Math.ceil(Math.abs((float)tmpS)/60f);
                tmpS = (60-Math.abs(tmpS)%60)%60;
                if(tmpM < 0) {
                    tmpH -= (int) Math.ceil(Math.abs((float)tmpM) / 60f);
                    tmpM = (60 - (Math.abs(tmpM) % 60))%60;
                    if(tmpH < 0)
                        tmpH = 24- Math.abs(tmpH)%24;
                }
            }
            return new Time(tmpH,tmpM,tmpS);
        }
        public void remHour(int hour,boolean changeObject){
            int tmp =h-hour;
            if(tmp < 0)
                tmp = 24-Math.abs(tmp)%24;
            if(changeObject)
                h = tmp%24;
        }
        public void remMinute(int min,boolean changeObject){
            int tmpM = (m-min);
            int tmpH = h;
            if(tmpM < 0) {
                tmpH -= (int) Math.ceil(Math.abs((float)tmpM) / 60f);
                tmpM = (60 - (Math.abs(tmpM) % 60))%60;
                if(tmpH < 0)
                    tmpH = 24- Math.abs(tmpH)%24;
            }
            if(changeObject){
                h= tmpH;
                m = tmpM;
            }
        }
        public void remSeconds(int seconds,boolean changeObject){
            int tmpS = s - seconds;
            int tmpH = h;
            int tmpM = m;
            if(tmpS < 0){
                tmpM -= (int)Math.ceil(Math.abs((float)tmpS)/60f);
                tmpS = (60-Math.abs(tmpS)%60)%60;
                if(tmpM < 0) {
                    tmpH -= (int) Math.ceil(Math.abs((float)tmpM) / 60f);
                    tmpM = (60 - (Math.abs(tmpM) % 60))%60;
                    if(tmpH < 0)
                        tmpH = 24- Math.abs(tmpH)%24;
                }
            }
            if(changeObject){
                h= tmpH;
                m = tmpM;
                s = tmpS;
            }
        }
        public int toSeconds(){
            return h*60*60+m*60+s;
        }

        @Override
        protected Time clone() throws CloneNotSupportedException {
            return new Time(h,m,s);
        }
        public Date toCurrentDate(){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR,h);
            cal.set(Calendar.MINUTE,m);
            cal.set(Calendar.SECOND,s);
            return cal.getTime();
        }
        public static Time foldTimes( Time t1,Time t2) {//Складывает время
            Time tmp = new Time(t1.h,t1.m,t1.s);
            tmp.addHour(t2.h,true);
            tmp.addMinute(t2.m,true);
            tmp.addSeconds(t2.s,true);
            return tmp;
        }
        public static Time subtractMoreSmall(Time t1, Time t2){//Вычитание из большего меньшее
            return isMoreFirst(t1,t2)?t1.remSeconds(t2.toSeconds()):t2.remSeconds(t1.toSeconds());
        }
        public static boolean isMoreFirst(Time first, Time second){//Первое время больше?
            return (first.h > second.h || first.h >= second.h && first.m > second.m || first.h >= second.h && first.m >= second.m && first.s > second.s);
        }


}
