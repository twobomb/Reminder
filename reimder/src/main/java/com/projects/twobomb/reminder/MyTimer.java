package com.projects.twobomb.reminder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.projects.twobomb.reminder.MainActivity.log;

public class MyTimer {
    private  List<TimeItem> pool ;
    private Thread thread;
    private Runnable run;
    private boolean stop = true;
    public MyTimer() {
        pool = new ArrayList<TimeItem>();
        run = new Runnable() {
            @Override
            public synchronized void run() {
                while (!stop) {
                    log("Thread loop");
                    if (pool.size() > 0 && pool.get(0).start.getTime() - new Date().getTime() > 0)
                        try {
                                log("THREAD WAITING: " + ((pool.get(0).start.getTime() - new Date().getTime()) / 1000));
                                run.wait(pool.get(0).start.getTime() - new Date().getTime());
                                log("wait end");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    else if (pool.size() == 0) {
                        stop();
                    }

                    if (pool.size() > 0 && Math.abs(pool.get(0).start.getTime() - new Date().getTime()) <= 1000) {
                        pool.get(0).runnable.run();
                        pool.remove(0);
                    }
                    sortedByDate();

                }
                }
        };
        start();

    }
    public void stop(){
        synchronized (run) {
            stop = true;
            run.notify();
            log("THREAD STOPPED");
        }
    }
    public void start(){
        synchronized (run) {
            if (stop && thread == null || !thread.isAlive()) {
                thread = new Thread(run);
                thread.setDaemon(true);
                thread.start();
                log("THREAD STARTED");
                stop = false;
            }
        }
    }
    public synchronized void addTask(Runnable task,Date startDate) {
        synchronized (run) {
            pool.add(new TimeItem(task, startDate));
            sortedByDate();
            if (!stop){
                run.notify();
//                log("notify thread");
            }
            else
                start();
        }
    }
    public boolean isStop(){
        return stop;
    }
    public void clearTasks()  {
        synchronized (run) {
            pool.clear();
            log("THREAD CLEAR TASKS poolsize:"+pool.size());
            if (!stop)
                run.notify();

        }
    }
    void sortedByDate(){
        synchronized (run) {
            for (int j = 0; j < pool.size(); j++)
                for (int i = 0; i < pool.size() - 1; i++)
                    if (pool.get(i).start.getTime() > pool.get(i + 1).start.getTime()) {
                        TimeItem ti = pool.get(i);
                        pool.set(i, pool.get(i + 1));
                        pool.set(i + 1, ti);
                    }
            for (int i = 0; i < pool.size(); i++)
                if (pool.get(i).start.getTime() < new Date().getTime())
                    pool.remove(i--);
        }
    }

    class TimeItem{
        public TimeItem(Runnable r, Date s) {
            start=s;
            runnable = r;
        }

        Runnable runnable;
        Date start;
    }

}
