package com.example.testwidgetconfig;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    final String LOG_TAG = "myLogs";

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_DATE = "widget_date_";

    private CalendarView calendar;
    private Button button;
    //private TextView text;
    private Date selectDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "CCCCCC");

        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.activity_main);

        calendar = findViewById(R.id.calendarView);
        button = findViewById(R.id.button);
        //text = findViewById(R.id.textView);
        button.setEnabled(false);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Date nowDate = new Date(); //системное время
                Date calendarDate = null;
                String calDate = Integer.toString(dayOfMonth) + "."
                        + Integer.toString(month + 1) + "."
                        + Integer.toString(year)+" 9:00:00"; //дата выбранная пользователем
                DateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss"); //формат даты

                try {
                    nowDate = format.parse(format.format(nowDate));
                    calendarDate = format.parse(calDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                button.setEnabled(true);

                if (calendarDate.before(nowDate)) {
                    button.setEnabled(false);
                }

                try {
                    selectDate = format.parse(calDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void onClick(View v) {
        // Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(WIDGET_DATE + widgetID, selectDate.getTime());
        editor.commit();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        WidgetActivity.updateWidget(this, appWidgetManager, sp, widgetID);
        WidgetActivity.alarmON(this);

        // положительный ответ
        setResult(RESULT_OK, resultValue);

        Log.d(LOG_TAG, "finish config " + widgetID);
        finish();
    }
}