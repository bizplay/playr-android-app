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
//import android.content.pm.PackageInfo;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
//import android.os.PowerManager;
import android.util.Log;

public class CheckRestartService extends Service {
	private static final String className = "CheckRestartService";
	private static final int intervalBetweenRestartChecks = 300000; // 5 minutes
	private boolean stopTask;

	// see https://stackoverflow.com/a/23587641/813660 answer for https://stackoverflow.com/questions/23586031/calling-activity-class-method-from-service-class
	// on how to enable calling a method on an Activity from a Service
	// having a direct reference is considered very bad practise
	// Binder given to clients
	private final IBinder binder = new LocalBinder();
	// Registered callbacks
	private IServiceCallbacks serviceCallbacks;


	// Class used for the client Binder.
	public class LocalBinder extends Binder {
		CheckRestartService getService() {
			// Return this instance of CheckRestartService so clients can call public methods
			return CheckRestartService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(className, "override onBind");
		return binder;
	}

	public void setCallbacks(IServiceCallbacks callbacks) {
		serviceCallbacks = callbacks;
	}
	// end of code to link to Activity

	public CheckRestartService() {
		Log.i(className, "default constructor");
	}

	@Override
	public void onCreate() {
		Log.i(className, "override onCreate");
		super.onCreate();

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
				if (restartMainActivity) {
					Log.i(className, ".onCreate TimerTask: MainActivity has to be restarted");
					if (serviceCallbacks != null) {
						Log.i(className, ".onCreate TimerTask: restarting MainActivity");
						serviceCallbacks.restartActivityWithDelay();
					} else {
						Log.e(className, ".onCreate TimerTask: serviceCallbacks is null");
					}
				} else {
					Log.i(className, ".onCreate TimerTask: MainActivity does not need to be restarted");
				}

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

	public void stopCheck() {
		stopTask = true;
	}

	private boolean checkServerForRestart() {
		String reply = "";
		String playerId = "";
		HttpURLConnection urlConnection = null;

		if (serviceCallbacks != null) {
			playerId = serviceCallbacks.getPlayerId();
		} else {
			Log.e(className, "checkServerForRestart serviceCallbacks is null");
		}


		if (!playerId.isEmpty()) {
			try {
				URL url = new URL("http://ajax.playr.biz/watchdogs/" + playerId
						+ "/command");
				Log.i(className, "checkServerForRestart URL: " + url.toString());
				urlConnection = (HttpURLConnection) url.openConnection();
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());
				reply = readStream(in).trim();
			} catch (MalformedURLException e) {
				Log.e(className,
						"checkServerForRestart IO exception opening connection; " + e.getMessage());
			} catch (IOException e) {
				Log.e(className,
						"checkServerForRestart IO exception opening connection; " + e.getMessage());
			} finally {
				urlConnection.disconnect();
			}
			Log.i(className, "checkServerForRestart response: " + reply);
		} else {
			Log.e(className, "checkServerForRestart playerId is empty");
		}
		return ("1".equals(reply));
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
			Log.i(className, "readStream IO exception reading inputStream; " + e.getMessage());
		}
		return "";
	}
}