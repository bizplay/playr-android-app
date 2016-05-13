package biz.playr;

import java.lang.Thread.UncaughtExceptionHandler;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
 
public class DefaultExceptionHandler implements UncaughtExceptionHandler {
    private int delay = 2000; // 2 seconds
    Activity activity;
 
    public DefaultExceptionHandler(Activity activity) {
        this.activity = activity;
    }
 
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
 
        try {
        	Intent intent = new Intent(activity, biz.playr.MainActivity.class);
 
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
 
            PendingIntent pendingIntent = PendingIntent.getActivity(
            		biz.playr.MainApplication.getInstance().getBaseContext(), 0, intent, intent.getFlags());
 
            //Following code will restart your application after <delay> seconds
            AlarmManager mgr = (AlarmManager) biz.playr.MainApplication.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
 
            //This will finish your activity manually
            activity.finish();
 
            //This will stop your application and take out from it.
            System.exit(2);
        } catch (Exception e) {
			e.printStackTrace();
        }
    }
}
