package biz.playr;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class CheckRestartService extends Service {
	private static final String className = "biz.playr.CheckRestartService";
	private static final int intervalBetweenRestartChecks = 300000; // 5 minutes

	// see http://stackoverflow.com/questions/6446221/get-context-in-a-service
	// and
	// http://stackoverflow.com/questions/7619917/how-to-get-context-in-android-service-class
	private Context self;
	private Context relevantApplicationContext;
	private boolean stopTask;

	public CheckRestartService(Context context) {
		relevantApplicationContext = context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		Log.i(className, "override onCreate");
		super.onCreate();

		self = this;
		stopTask = false;

		// Start polling check for restart task
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Log.i(className, ".onCreate TimerTask::run()");
				// If you wish to stop the task/polling
				if (stopTask) {
					this.cancel();
				}

				// check the server if restart is needed
				boolean restartMainActivity = checkServerForRestart();
				// restart mainActivity
//				relevantApplicationContext.
				if (restartMainActivity) {
//					getBaseContext().getActivity().restartActivity();
				}

				// The first in the list of RunningTasks is always the
				// foreground task.
				// ActivityManager activityManager = (ActivityManager)
				// getSystemService(ACTIVITY_SERVICE);
				// RunningTaskInfo foregroundTaskInfo =
				// activityManager.getRunningTasks(1).get(0);
				// String foregroundTaskPackageName =
				// foregroundTaskInfo.topActivity.getPackageName();

				// Check foreground app: If it is not in the foreground... bring
				// it!
				// if
				// (!foregroundTaskPackageName.equals(YOUR_APP_PACKAGE_NAME)){
				// if (!isForegroundApp(self)){
				// Intent LaunchIntent =
				// getPackageManager().getLaunchIntentForPackage(self.getPackageName());
				// startActivity(LaunchIntent);
				// }
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, intervalBetweenRestartChecks);

		// To end the application
		// Log.e(className,"onCreate: System.exit(2) !!! End application !!!");
		// System.exit(2);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(className, "override onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.i(className, "override onDestroy");
		stopTask = true;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(className, "override onBind");
		// TODO start MainActivity
		return null;
	}

	public void stopCheck() {
		stopTask = true;
	}

	private boolean checkServerForRestart() {
		String reply = "";
		HttpURLConnection urlConnection = null;
		String playerId = relevantApplicationContext.getSharedPreferences(
				STORAGE_SERVICE, MODE_PRIVATE).getString(
				getString(R.string.player_id_store), "");

		try {
			URL url = new URL("http://ajax.playr.biz/watchdogs/" + playerId
					+ "/command");
			Log.i(className, "override onCreate URL: " + url.toString());
			urlConnection = (HttpURLConnection) url.openConnection();
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			reply = readStream(in);
		} catch (MalformedURLException e) {
			Log.i(className,
					"checkServerForRestart IO exception opening connection; "
							+ e.getMessage());
		} catch (IOException e) {
			Log.i(className,
					"checkServerForRestart IO exception opening connection; "
							+ e.getMessage());
		} finally {
			urlConnection.disconnect();
		}
		return (reply == "1");
	}

	private String readStream(InputStream inputStream) {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					inputStream));
			StringBuilder total = new StringBuilder(inputStream.available());
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line).append('\n');
			}

			return total.toString();
		} catch (IOException e) {
			Log.i(className, "readStream IO exception reading inputStream; "
					+ e.getMessage());
		}
		return "";
	}
}