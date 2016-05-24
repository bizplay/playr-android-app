package biz.playr;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/*
 * see http://chintanrathod.com/auto-restart-application-after-crash-forceclose-in-android/
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler {
    public static int restartDelay = 10000; // 10 seconds
    Activity activity;
 
    public DefaultExceptionHandler(Activity activity) {
    	Log.i("biz.playr.DefaultExceptionHandler","constructor");
        this.activity = activity;
    }
    public Activity getActivity() {
    	return activity;
    }
 
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
 
    	Log.i("biz.playr.DefaultExceptionHandler","uncaughtException handling -> restart after delay");
        try {
    		Log.e("biz.playr.DefaultExceptionHandler","uncaughtException: Uncaught exception handling started. Exception message: " + ex.getMessage());
        	Intent intent = new Intent(activity, biz.playr.MainActivity.class);
 
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
 
            PendingIntent pendingIntent = PendingIntent.getActivity(
            		biz.playr.MainApplication.getInstance().getBaseContext(), 0, intent, intent.getFlags());
 
            //Following code will restart your application after <delay> seconds
            AlarmManager mgr = (AlarmManager) biz.playr.MainApplication.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + restartDelay, pendingIntent);
 
            //This will finish your activity manually
    		Log.e("biz.playr.DefaultExceptionHandler","uncaughtException: activity.finish() !!! About to restart application !!!");
            activity.finish();

            //This will stop your application and take out from it.
    		Log.e("biz.playr.DefaultExceptionHandler","uncaughtException: System.exit(2) !!! About to restart application !!!");
            System.exit(2);
        } catch (Exception e) {
    		Log.e("biz.playr.DefaultExceptionHandler.uncaughtException catch block:","Exception message: " + e.getMessage());
			e.printStackTrace();
        }
    }
}
