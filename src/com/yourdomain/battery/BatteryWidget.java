package com.yourdomain.battery;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.RemoteViews;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.BatteryManager;
import android.os.SystemClock;

public class BatteryWidget extends AppWidgetProvider {
    private static final String ACTION_BATTERY_UPDATE = "com.yourdomain.battery.action.UPDATE";
    private static final int REFRESH_TIMEOUT_SECONDS = 30;
    private int batteryLevel = 0;
    private final int BATTERY_STEPS = 10;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        LogFile.log("onEnabled()");

        turnAlarmOnOff(context, true);
        context.startService(new Intent(context, ScreenMonitorService.class));
    }

    public static void turnAlarmOnOff(Context context, boolean turnOn) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_BATTERY_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (turnOn) { // Add extra 1 sec because sometimes ACTION_BATTERY_CHANGED is called after the first alarm
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, REFRESH_TIMEOUT_SECONDS * 1000, pendingIntent);
            LogFile.log("Alarm set");
        } else {
            alarmManager.cancel(pendingIntent);
            LogFile.log("Alarm disabled");
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        LogFile.log("onUpdate()");

        // Sometimes when the phone is booting, onUpdate method gets called before onEnabled()
        int currentLevel = calculateBatteryLevel(context);
        if (batteryChanged(currentLevel)) {
            batteryLevel = currentLevel;
            LogFile.log("Battery changed");
        }
        updateViews(context);
    }

    private boolean batteryChanged(int currentLevelLeft) {
        return (batteryLevel != currentLevelLeft);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        LogFile.log("onReceive() " + intent.getAction());

        if (intent.getAction().equals(ACTION_BATTERY_UPDATE)) {
            int currentLevel = calculateBatteryLevel(context);
            if (batteryChanged(currentLevel)) {
                LogFile.log("Battery changed");
                batteryLevel = currentLevel;
                updateViews(context);
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        LogFile.log("onDisabled()");

        turnAlarmOnOff(context, false);
        context.stopService(new Intent(context, ScreenMonitorService.class));
    }

    private int calculateBatteryLevel(Context context) {
        LogFile.log("calculateBatteryLevel()");

        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        return level * 100 / scale;
    }

    private void updateViews(Context context) {
        LogFile.log("updateViews()");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.battery_widget);
        views.setTextViewText(R.id.text_level, batteryLevel + "%");

        showBars(views, batteryLevel);

        ComponentName componentName = new ComponentName(context, BatteryWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, views);

    }

    /*
     * Helper method to show levels using bar images
     * - takes the last read battery level as parameter
     */
    private void showBars(RemoteViews widgetViews, int currLevel) {
        if (currLevel >= 10) {
            widgetViews.setImageViewResource(R.id.bar1, R.drawable.level_shape_low);
            widgetViews.setViewVisibility(R.id.bar1, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar1, View.INVISIBLE);
        if (currLevel >= 20) {
            widgetViews.setImageViewResource(R.id.bar2, R.drawable.level_shape_low);
            widgetViews.setViewVisibility(R.id.bar2, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar2, View.INVISIBLE);
        if (currLevel >= 30) {
            widgetViews.setImageViewResource(R.id.bar3, R.drawable.level_shape_low);
            widgetViews.setViewVisibility(R.id.bar3, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar3, View.INVISIBLE);
        if (currLevel >= 40) {
            widgetViews.setImageViewResource(R.id.bar4, R.drawable.level_shape_mid);
            widgetViews.setViewVisibility(R.id.bar4, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar4, View.INVISIBLE);
        if (currLevel >= 50) {
            widgetViews.setImageViewResource(R.id.bar5, R.drawable.level_shape_mid);
            widgetViews.setViewVisibility(R.id.bar5, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar5, View.INVISIBLE);
        if (currLevel >= 60) {
            widgetViews.setImageViewResource(R.id.bar6, R.drawable.level_shape_mid);
            widgetViews.setViewVisibility(R.id.bar6, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar6, View.INVISIBLE);
        if (currLevel >= 70) {
            widgetViews.setImageViewResource(R.id.bar7, R.drawable.level_shape_high);
            widgetViews.setViewVisibility(R.id.bar7, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar7, View.INVISIBLE);
        if (currLevel >= 80) {
            widgetViews.setImageViewResource(R.id.bar8, R.drawable.level_shape_high);
            widgetViews.setViewVisibility(R.id.bar8, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar8, View.INVISIBLE);
        if (currLevel >= 90) {
            widgetViews.setImageViewResource(R.id.bar9, R.drawable.level_shape_high);
            widgetViews.setViewVisibility(R.id.bar9, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar9, View.INVISIBLE);
        if (currLevel >= 100) {
            widgetViews.setImageViewResource(R.id.bar10, R.drawable.level_shape_high);
            widgetViews.setViewVisibility(R.id.bar10, View.VISIBLE);
        } else
            widgetViews.setViewVisibility(R.id.bar10, View.INVISIBLE);
        //deal with the remainder
        int partLevel = currLevel % BATTERY_STEPS;
        //find out how many full levels we have
        int fullSteps = currLevel - partLevel;
        //find out which is the next level up and call helper method to set partially visible
        if (partLevel > 0) {
            setPartStep(widgetViews, (fullSteps / BATTERY_STEPS) + 1);
        }
        LogFile.log("showBars(): Battery level = " + batteryLevel + "; partLevel = " + partLevel);
    }

    /*
     * Helper method to set partial level step drawable
     * - takes the partial level to set visible
     */
    private void setPartStep(RemoteViews widgetViews, int partLevel) {

        switch (partLevel) {
            case 1:
                widgetViews.setImageViewResource(R.id.bar1, R.drawable.level_shape_low_alpha);
                widgetViews.setViewVisibility(R.id.bar1, View.VISIBLE);
                break;
            case 2:
                widgetViews.setImageViewResource(R.id.bar2, R.drawable.level_shape_low_alpha);
                widgetViews.setViewVisibility(R.id.bar2, View.VISIBLE);
                break;
            case 3:
                widgetViews.setImageViewResource(R.id.bar3, R.drawable.level_shape_low_alpha);
                widgetViews.setViewVisibility(R.id.bar3, View.VISIBLE);
                break;
            case 4:
                widgetViews.setImageViewResource(R.id.bar4, R.drawable.level_shape_mid_alpha);
                widgetViews.setViewVisibility(R.id.bar4, View.VISIBLE);
                break;
            case 5:
                widgetViews.setImageViewResource(R.id.bar5, R.drawable.level_shape_mid_alpha);
                widgetViews.setViewVisibility(R.id.bar5, View.VISIBLE);
                break;
            case 6:
                widgetViews.setImageViewResource(R.id.bar6, R.drawable.level_shape_mid_alpha);
                widgetViews.setViewVisibility(R.id.bar6, View.VISIBLE);
                break;
            case 7:
                widgetViews.setImageViewResource(R.id.bar7, R.drawable.level_shape_high_alpha);
                widgetViews.setViewVisibility(R.id.bar7, View.VISIBLE);
                break;
            case 8:
                widgetViews.setImageViewResource(R.id.bar8, R.drawable.level_shape_high_alpha);
                widgetViews.setViewVisibility(R.id.bar8, View.VISIBLE);
                break;
            case 9:
                widgetViews.setImageViewResource(R.id.bar9, R.drawable.level_shape_high_alpha);
                widgetViews.setViewVisibility(R.id.bar9, View.VISIBLE);
                break;
            case 10:
                widgetViews.setImageViewResource(R.id.bar10, R.drawable.level_shape_high_alpha);
                widgetViews.setViewVisibility(R.id.bar10, View.VISIBLE);
                break;
            default:
                break;
        }
    }
}
