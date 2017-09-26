package com.projects.twobomb.reminder;

import android.content.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    Timer tm;
     ExtendedAdapter ea;
    BroadcastReceiver br;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return true;
    }

    public static void log(Object obj){
        Log.d("myLogs",String.valueOf(obj));
    }
    public static void log(Object obj,String tag){
        Log.d(tag,String.valueOf(obj));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add:
                final View vv1 = getLayoutInflater().inflate(R.layout.add_dlg,null);
                ((NumberPicker)vv1.findViewById(R.id.np_hours)).setMaxValue(23);
                ((NumberPicker)vv1.findViewById(R.id.np_hours)).setMinValue(0);
                ((NumberPicker)vv1.findViewById(R.id.np_minutes)).setMaxValue(59);
                ((NumberPicker)vv1.findViewById(R.id.np_minutes)).setMinValue(0);

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setTitle("Добавить")
                        .setView(vv1);
                final AlertDialog ad = builder.show();

                ((Button)vv1.findViewById(R.id.btn_no)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ad.cancel();
                    }
                });

                ((Button)vv1.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int hours =((NumberPicker)vv1.findViewById(R.id.np_hours)).getValue();
                        int minutes =((NumberPicker)vv1.findViewById(R.id.np_minutes)).getValue();
                        ItemInfo ii = new ItemInfo(new Time(hours,minutes),(Time)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.LESSON_DURATION),true);
                        ea.getList().add(ii);
                        ad.cancel();
                        SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.DATA_LESSONS,ea.getList());
                    }
                });
                return true;
            case R.id.item_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_showAll:
                ea.changeVisionAll(true,this);
                return true;
            case R.id.item_hideAll:
                ea.changeVisionAll(false,this);
                return true;
         default:   return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        tm.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ea.isCanAnimate = true;
        log("OnResume");

        ((TextView)findViewById(R.id.mtxt_info)).setText(ea.getInfo());
        ea.notifyDataSetChanged();
        startUpdate();

    }
    void startUpdate(){
        final Context ctx = this.getApplicationContext();
        if(tm != null) {
            tm.cancel();
            tm.purge();
            tm = null;
        }
        tm = new Timer();
        final TextView tv = (TextView)findViewById(R.id.mtxt_info);
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       tv.setText(ea.getInfo());
                        ea.notifyDataSetChanged();
                    }
                });
            }
        };
        long delay = AnimationUtils.loadAnimation(this, R.anim.show_anim).getDuration()*ea.getCount()+300;
        tm.schedule(task,delay,1000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ea.isCanAnimate = false;
                cancel();
            }
        },delay);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("MainActivity:OnCreate");
        super.onCreate(savedInstanceState);
        setTheme((int)SettingsControl.getData(this, SettingsControl.SETTINGS.THEME));
        setContentView(R.layout.activity_main);
        final ListView lv = (ListView)findViewById(R.id.list_subjects);
         ea = new ExtendedAdapter(this,( ArrayList<ItemInfo>)SettingsControl.getData(this, SettingsControl.SETTINGS.DATA_LESSONS));

        lv.setAdapter(ea);
        Intent service = new Intent(this,ServiceNotification.class);
        startService(service);
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                log("MainActivity_broadcast_recieve:"+intent.getAction());
                switch (intent.getAction()){
                    case SettingsControl.ACTION_UPDATE_LESSON:
                        ea.setNewList((ArrayList<ItemInfo>)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.DATA_LESSONS));
                        break;
                    case SettingsControl.ACTION_UPDATE_LESSON_DURATION:
                        ea.setLengthLess((Time)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.LESSON_DURATION));
                        break;
                    case SettingsControl.ACTION_UPDATE_THEME:
//                            Intent res = new Intent(getApplicationContext(),MainActivity.class);
//                            finish();
//                            startActivity(res);
                            log("RESET");

                        break;
                    case Intent.ACTION_TIME_CHANGED:
                            startService(new Intent(getApplicationContext(),ServiceNotification.class));
                        break;
                    case SettingsControl.ACTION_UPDATE_ON_NOTIFICATION:
                        if((boolean)SettingsControl.getData(getApplicationContext(), SettingsControl.SETTINGS.ON_NOTIFICATION))
                            startService(new Intent(getApplicationContext(),ServiceNotification.class));
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(SettingsControl.ACTION_UPDATE_LESSON);
        filter.addAction(SettingsControl.ACTION_UPDATE_LESSON_DURATION);
        filter.addAction(SettingsControl.ACTION_UPDATE_THEME);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(SettingsControl.ACTION_UPDATE_ON_NOTIFICATION);
        registerReceiver(br,new IntentFilter(filter));

    }

}
