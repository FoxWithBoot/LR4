package com.example.testwidgetconfig;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class WidgetActivity extends AppWidgetProvider {
    final static String LOG_TAG = "myLogs";
    private static ArrayList<Integer> idList;
    private static ArrayList<Long> dateList;
    private final static String MY_ACTION = "my_action";
    private static AlarmManager am;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");
        idList = new ArrayList<>();
        dateList = new ArrayList<>();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        idList.add(appWidgetIds[0]);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));

        // Удаляем Preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(
                MainActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(MainActivity.WIDGET_DATE + widgetID);
        }
        editor.commit();

        Iterator<Integer> it = idList.iterator();
        int i=-1;
        while (it.hasNext()){
            i++;
            if(it.next()==appWidgetIds[0]){
                it.remove();
                if(dateList.size()>0) dateList.remove(i);
                break;
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
        idList.clear();
        dateList.clear();
        alarmOFF(context);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context, intent);
        if(intent.getAction().equalsIgnoreCase(MY_ACTION)){
            Log.d(LOG_TAG, "MY_ACTION");
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            updateAll(context, awm);
        }
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp, int widgetID) {
        Log.d(LOG_TAG, "updateWidget " + widgetID);

        // Читаем параметры Preferences
        long date = sp.getLong(MainActivity.WIDGET_DATE + widgetID, 0);

        dateList.add(date);

        if(idList.contains(widgetID)){
            Log.d(LOG_TAG, "DATE: "+ idList.indexOf(widgetID));
            dateList.set(idList.indexOf(widgetID),date);
        }

        // Настраиваем внешний вид виджета
        RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                R.layout.widget_activity);
        widgetView.setTextViewText(R.id.tv, getPeriod(date));

        //нажатие на виджет
        Intent conIntent = new Intent(context, MainActivity.class);
        conIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        conIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, conIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tv, pIntent);

        //для уведомлений
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date, pi);

        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetID, long date){
        RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                R.layout.widget_activity);
        widgetView.setTextViewText(R.id.tv, getPeriod(date));
        // Обновляем виджет
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    private void updateAll(Context context, AppWidgetManager appWidgetManager){
        Integer[] ids = idList.toArray(new Integer[idList.size()]);
        Long[] dates = dateList.toArray(new Long[idList.size()]);
        for (int i=0; i<idList.size(); i++) {
            Log.d(LOG_TAG, "UP "+dateList.get(i));
            updateAppWidget(context, appWidgetManager, ids[i], dates[i]);
        }
    }

    static void alarmON(Context context){
        if(am==null) {
            Log.d(LOG_TAG, "ALARM_ON");
            am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); //инициализируем Аларм
            Intent intent = new Intent(context, WidgetActivity.class); //создаем интент с текущим контекстом и классом помощником
            intent.setAction(MY_ACTION);
            int ids[] = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(new ComponentName(context, WidgetActivity.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0); //создаем пандингинтент типа бродкаст с нашим интентом
            am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pendingIntent); //устанавливаем аларм на системное время, на повтор каждые 60 сек с нашим пандингинтент
        }
    }

    static void alarmOFF(Context context){
        Log.d(LOG_TAG, "ALARM_OFF");
        Intent intentD = new Intent(context, WidgetActivity.class);
        intentD.setAction(MY_ACTION);
        int ids[] = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetActivity.class));
        intentD.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        PendingIntent pendingIntentD = PendingIntent.getBroadcast(context, 0, intentD, 0);
        AlarmManager amStop = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        amStop.cancel(pendingIntentD);
        am=null;
    }

    private static String getPeriod(long l){
        long diff = TimeUnit.DAYS.convert(l-new Date().getTime(), TimeUnit.MILLISECONDS);
        return Long.toString(diff);
    }
}
