package biz.playr;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class CheckRestartService extends Service{
	private static final String className = "biz.playr.CheckRestartService";
	private static final int intervalBetweenRestartChecks = 60000; // 1 minute

	// see http://stackoverflow.com/questions/6446221/get-context-in-a-service
	// and http://stackoverflow.com/questions/7619917/how-to-get-context-in-android-service-class
	private static Context relevantContext;
	private static boolean stopTask;

	@Override
	public void onCreate() {
		Log.i(className,"override onCreate");
		super.onCreate();






		relevantContext = this;
		stopTask = false;
		

		// Start polling check for restart task
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Log.i(className,".onCreate TimerTask::run()");
				// If you wish to stop the task/polling
				if (stopTask){
					this.cancel();
				}

				// check the server if restart is needed

				// restart mainActivity
				getActivity().restartActivity()

				// The first in the list of RunningTasks is always the foreground task.
//				ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//				RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);
//				String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();

				// Check foreground app: If it is not in the foreground... bring it!
//				if (!foregroundTaskPackageName.equals(YOUR_APP_PACKAGE_NAME)){
//				if (!isForegroundApp(relevantContext)){
//					Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(relevantContext.getPackageName());
//					startActivity(LaunchIntent);
//				}
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, intervalBetweenRestartChecks);

		
		
		
		// To end the application
//		Log.e(className,"onCreate: System.exit(2) !!! End application !!!");
//		System.exit(2);
	}
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.i(className,"override onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		Log.i(className,"override onDestroy");
		stopTask = true;
		super.onDestroy();
	}
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(className,"override onBind");
		// TODO start MainActivity
		return null;
	}
	public stopCheck() {
		stopTask = true;
	}
}