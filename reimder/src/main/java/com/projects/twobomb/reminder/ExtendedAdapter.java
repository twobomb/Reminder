package com.projects.twobomb.reminder;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ExtendedAdapter extends BaseAdapter {
    private static ArrayList<ItemInfo> items;
    private LayoutInflater l_Inflater;
    private Context context;
    private enum eventType  {DATACHANGED};

    public ArrayList<ItemInfo>getList(){
        return items;
    }
    public boolean isCanAnimate = false;
    public void setNewList(ArrayList<ItemInfo> newList){
        items.clear();
        items = null;
        items = newList;
        notifyDataSetChanged();
    }
    public ExtendedAdapter(Context context, ArrayList<ItemInfo> results) {
        items = results;

        this.context = context;
        l_Inflater = LayoutInflater.from(context);
    }
    public void addItem(ItemInfo ii){
        items.add(ii);
        ItemInfo.sortableFromEndTime(items);
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return items.size();
    }
    public void setLengthLess(Time t){
        for(ItemInfo ii:items)
            ii.setLengthLection(t);
        notifyDataSetChanged();
    }
    @Override
    public Object getItem(int i) {
        return items.get(i);
    }
    public String getInfo(){
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
            case 1:
                return "Сегодня воскресенье!Выходной.";
            case 7:
                return "Сегодня суббота!Выходной.";
        }
        int activeLess = 0;
        for(int i = 0; i < items.size();i++)
            if(items.get(i).isActive)
                activeLess++;
        String res = "";
        if(activeLess <= 0 || items.size() == 0)
            return "Сегодня нет пар";
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
                return res;
            }
        }
        if(Time.isMoreFirst(now,items.get(getIndexFirstLesson()).beginTime))
            res+="\nДо конца пар "+ ItemInfo.getTextDifference(items.get(getIndexLastLesson()).endTime,now);

        return res;
    }
    public void changeVisionAll(boolean act,Context ctx){
        for (int i = 0; i < items.size();i++)
            items.get(i).isActive = act;
        notifyDataSetChanged();
        SettingsControl.setData(ctx, SettingsControl.SETTINGS.DATA_LESSONS,items);
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
    @Override
    public long getItemId(int i) {
        return i;
    }
    public void deleteItem(int i,Context ctx){
        items.remove(i);
        notifyDataSetChanged();
        SettingsControl.setData(ctx, SettingsControl.SETTINGS.DATA_LESSONS,items);
    }
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder vh;
        if (view == null) {
            view = l_Inflater.inflate(R.layout.list_layout,null);
            ((Button)view.findViewById(R.id.btn_show)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    items.get(i).isActive= !items.get(i).isActive;
                    notifyDataSetChanged();
                    SettingsControl.setData(v.getContext(), SettingsControl.SETTINGS.DATA_LESSONS,items);
                }
            });
            ((Button)view.findViewById(R.id.btn_del)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 16;
                    Bitmap bmp = BitmapFactory.decodeResource(view.getResources(),R.drawable.confirmation,options);
                    builder.setTitle("Подтверждение")
                            .setMessage("Вы уверены что хотите удалить пару?")
                            .setIcon(new BitmapDrawable(view.getResources(),bmp))
                            .setCancelable(false)
                            .setPositiveButton("Да",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            deleteItem(i,view.getContext());
                                            dialog.cancel();
                                        }
                                    })
                            .setNegativeButton("Нет",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
            vh = new ViewHolder();
            vh.number= (TextView)view.findViewById(R.id.txt_number);
            vh.beginTime = (TextView)view.findViewById(R.id.txt_beginTime);
            vh.timeToEnd = (TextView)view.findViewById(R.id.txt_timeToEnd);
            vh.endTime= (TextView)view.findViewById(R.id.txt_endTIme);
            view.setTag(vh);
        }
        else
            vh = (ViewHolder) view.getTag();

        vh.timeToEnd.setTextColor(items.get(i).getColor(view.getContext()));
        vh.number.setText(String.valueOf(i+1));
        vh.timeToEnd.setText(items.get(i).getTimeToEnd());
        vh.beginTime.setText(items.get(i).getBeginTime());
        vh.endTime.setText(items.get(i).getEndTime());
        if(items.get(i).isActive) {
            view.setBackgroundColor(Color.TRANSPARENT);
            view.findViewById(R.id.img_dis).setVisibility(View.INVISIBLE);
        }
        else{
            view.setBackgroundColor(Color.DKGRAY);
            view.findViewById(R.id.img_dis).setVisibility(View.VISIBLE);
        }
        if(isCanAnimate){
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.show_anim);

            anim.setStartOffset(i*anim.getDuration()+300);
            anim.setInterpolator(new AccelerateInterpolator(0.4f));
            view.startAnimation(anim );
        }
        return view;

    }
    static class ViewHolder{
        TextView number;
        TextView beginTime;
        TextView timeToEnd;
        TextView endTime;
    }
}
