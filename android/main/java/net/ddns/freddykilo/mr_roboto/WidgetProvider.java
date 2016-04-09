package net.ddns.freddykilo.mr_roboto;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

    private static final String MOTION_TRACKER = "net.ddns.freddykilo.mr_roboto.service.MOTION_TRACKER";
    private static final String TAG = "test";

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "WidgetProvider.onEnabled()");
        prepareWidget(context);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "WidgetProvider.onDisabled()");
        Intent motionTracker = new Intent(context, MotionTracker.class);
        context.stopService(motionTracker);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "WidgetProvider.onReceive()");
        super.onReceive(context, intent);
        if (intent.getAction().equals(MOTION_TRACKER)) {
            Intent lights = new Intent(context, MotionTracker.class);
            context.startService(lights);
        }
    }

    /**
     * This gets called for each widget added to either the home screen or lock screen and
     * sets the layout accordingly.
     *
     * @param context
     */
    private void prepareWidget(Context context) {
        Log.d(TAG, "WidgetProvider.prepareWidget()");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, WidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        for (int widgetId : allWidgetIds) {
            int baseLayout = R.layout.widget_layout;
            appWidgetManager.updateAppWidget(widgetId, setIntentToRemoteView(context, baseLayout));
        }
    }

    /**
     * Set the Intent (functionality) for each button. Defines which service to run.
     *
     * @param context
     * @param layout
     * @return RemoteView with attached OnClickListener (OnClickPendingIntent) used to pass into
     * AppWidgetManager.updateAppWidget(int appWidgetId, RemoteViews views)
     */
    public RemoteViews setIntentToRemoteView(Context context, int layout) {
        Log.d(TAG, "WidgetProvider.setIntentToRemoteView()");
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(MOTION_TRACKER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layout);
        remoteViews.setOnClickPendingIntent(R.id.button_1, pendingIntent);
        return remoteViews;
    }

}
