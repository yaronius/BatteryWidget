package com.yourdomain.battery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class BatteryWidget extends AppWidgetProvider {
    private static final String ACTION_BATTERY_UPDATE = "com.yourdomain.battery.action.UPDATE";
    private static final int REFRESH_TIMEOUT_SECONDS = 30;
    private int batteryLevel = 0;
    private final int BATTERY_STEPS = 10;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        turnAlarmOnOff(context, true);
        context.startService(new Intent(context, ScreenMonitorService.class));
    }

    public static void turnAlarmOnOff(Context context, boolean turnOn) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_BATTERY_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (turnOn) { // Add extra 1 sec because sometimes ACTION_BATTERY_CHANGED is called after the first alarm
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, REFRESH_TIMEOUT_SECONDS * 1000, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Sometimes when the phone is booting, onUpdate method gets called before onEnabled()
        int currentLevel = calculateBatteryLevel(context);
        if (batteryChanged(currentLevel)) {
            batteryLevel = currentLevel;
        }
        updateViews(context);
    }

    private boolean batteryChanged(int currentLevelLeft) {
        return (batteryLevel != currentLevelLeft);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(ACTION_BATTERY_UPDATE)) {
            int currentLevel = calculateBatteryLevel(context);
            if (batteryChanged(currentLevel)) {
                batteryLevel = currentLevel;
                updateViews(context);
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        turnAlarmOnOff(context, false);
        context.stopService(new Intent(context, ScreenMonitorService.class));
    }

    private int calculateBatteryLevel(Context context) {
        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        return level * 100 / scale;
    }

    private void updateViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.battery_widget);

        showBars(context, views, batteryLevel);
        Log.i("info", "updateViews(): battery level = " + batteryLevel);
        views.setTextViewText(R.id.text_level, batteryLevel + "%");

        ComponentName componentName = new ComponentName(context, BatteryWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, views);
        Log.i("info", "updateViews() end");
    }

    /*
     * Helper method to show levels using bar images
     * - takes the last read battery level as parameter
     */
    private void showBars(Context context, RemoteViews widgetViews, int currLevel) {
        //deal with the remainder
        int partLevel = currLevel % BATTERY_STEPS;
        //find out how many full levels we have
        int fullSteps = (currLevel - partLevel) / BATTERY_STEPS;
        int fullBarId = R.drawable.level_shape_low;
        int partBarId = R.drawable.level_shape_low_alpha;
        if (fullSteps > 5) {
            fullBarId = R.drawable.level_shape_high;
            partBarId = R.drawable.level_shape_high_alpha;
        } else if (fullSteps > 3) {
            fullBarId = R.drawable.level_shape_mid;
            partBarId = R.drawable.level_shape_mid_alpha;
        }

        Log.i("info", "showBars(): fullSteps = " + fullSteps + "; partLevel = " + partLevel);
        for (int i = 1; i <= BATTERY_STEPS; i++) {
            int barId = context.getResources().getIdentifier("bar"+i, "id", context.getPackageName());
            Log.i("info", "showBars(): Name = bar" + i + "; id = " + barId);
            if (i <= fullSteps) {
                widgetViews.setImageViewResource(barId, fullBarId);
                widgetViews.setViewVisibility(barId, View.VISIBLE);
                Log.i("info", "showBars(): Name = bar" + i + "; visible full");
            } else if (i == (fullSteps + 1) && partLevel > 0) {
                widgetViews.setImageViewResource(barId, partBarId);
                widgetViews.setViewVisibility(barId, View.VISIBLE);
                Log.i("info", "showBars(): Name = bar" + i + "; visible partial");
            } else {
                widgetViews.setViewVisibility(barId, View.INVISIBLE);
                Log.i("info", "showBars(): Name = bar" + i + "; invisible");
            }
        }
    }
}
