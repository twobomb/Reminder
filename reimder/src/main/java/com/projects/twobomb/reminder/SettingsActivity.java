package com.projects.twobomb.reminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import static com.projects.twobomb.reminder.MainActivity.log;

public class SettingsActivity extends Activity {
    boolean themeChecker = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme((int)SettingsControl.getData(this, SettingsControl.SETTINGS.THEME));
        setContentView(R.layout.settings_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//Отключить клаву
        ((Switch)findViewById(R.id.sw_notifOn)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.ON_NOTIFICATION,b);
            }
        });
        ((SeekBar)findViewById(R.id.sb_prevSignal)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ((TextView)findViewById(R.id.txt_prevSignal)).setText("Уведомлять за "+i+" мин.");
                SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.NOTYFY_FOR,i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ArrayAdapter<CharSequence> aa= ArrayAdapter.createFromResource(this,R.array.themes_array,R.layout.support_simple_spinner_dropdown_item);
        aa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.sp_theme)).setAdapter(aa);
        ((Spinner)findViewById(R.id.sp_theme)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                int theme = i == 0?R.style.MyLightTheme:R.style.MyDarkTheme;
                SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.THEME,theme);
                if(themeChecker) {
                    finish();
                    Intent int1 = new Intent(getApplicationContext(), SettingsActivity.class);
                    int1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(int1);
                }
                themeChecker =  true;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        aa= ArrayAdapter.createFromResource(this,R.array.signals_array,R.layout.support_simple_spinner_dropdown_item);
        aa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.sp_signal)).setAdapter(aa);
        ((Spinner)findViewById(R.id.sp_signal)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.SIGNAL,i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        NumberPicker np_hour = ((NumberPicker)findViewById(R.id.np_hourLess));
        np_hour.setMinValue(0);
        np_hour.setMaxValue(23);
        np_hour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.LESSON_DURATION,new Time(i1,((NumberPicker)findViewById(R.id.np_minLess)).getValue()));
            }
        });

        NumberPicker np_minutes = ((NumberPicker)findViewById(R.id.np_minLess));
        np_minutes.setMinValue(0);
        np_minutes.setMaxValue(59);
        np_minutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.LESSON_DURATION,new Time(((NumberPicker)findViewById(R.id.np_hourLess)).getValue(),i1));
            }
        });
        NumberPicker np_freq = ((NumberPicker)findViewById(R.id.np_freq));
        np_freq.setMinValue(1);
        np_freq.setMaxValue(60);
        np_freq.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                SettingsControl.setData(getApplicationContext(), SettingsControl.SETTINGS.FREQUENCY,i1);
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 16;
                Bitmap bmp = BitmapFactory.decodeResource(view.getResources(),R.drawable.confirmation,options);
                builder.setTitle("Подтверждение")
                        .setMessage("Вы уверены что хотите сбросить все настройки включая расписание?")
                        .setIcon(new BitmapDrawable(view.getResources(),bmp))
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SettingsControl.clearData(getApplicationContext());
                                        updateUI();
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

        updateUI();
    }

    @Override
    public void finish() {
        super.finish();
        Intent int1 = new Intent(getApplicationContext(),MainActivity.class);
        int1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(int1);
    }

    void updateUI(){
        Context ctx = getApplicationContext();
        ((Switch)findViewById(R.id.sw_notifOn)).setChecked((boolean)SettingsControl.getData(ctx, SettingsControl.SETTINGS.ON_NOTIFICATION));

        ((SeekBar)findViewById(R.id.sb_prevSignal)).setProgress((int)SettingsControl.getData(ctx, SettingsControl.SETTINGS.NOTYFY_FOR));
        ((TextView)findViewById(R.id.txt_prevSignal)).setText("Уведомлять за "+((SeekBar)findViewById(R.id.sb_prevSignal)).getProgress()+" мин.");

        int themeId = (int)SettingsControl.getData(ctx, SettingsControl.SETTINGS.THEME);
        switch (themeId){
            case R.style.MyLightTheme:
                ((Spinner)findViewById(R.id.sp_theme)).setSelection(0);
                    break;
            case R.style.MyDarkTheme:
                ((Spinner)findViewById(R.id.sp_theme)).setSelection(1);
                break;
        }
        ((Spinner)findViewById(R.id.sp_signal)).setSelection((int)SettingsControl.getData(ctx, SettingsControl.SETTINGS.SIGNAL));
        ((NumberPicker)findViewById(R.id.np_hourLess)).setValue(((Time)SettingsControl.getData(ctx, SettingsControl.SETTINGS.LESSON_DURATION)).getHours());
        ((NumberPicker)findViewById(R.id.np_minLess)).setValue(((Time)SettingsControl.getData(ctx, SettingsControl.SETTINGS.LESSON_DURATION)).getMinutes());
        ((NumberPicker)findViewById(R.id.np_freq)).setValue((int)SettingsControl.getData(ctx, SettingsControl.SETTINGS.FREQUENCY));


    }
}
