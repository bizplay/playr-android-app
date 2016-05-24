package biz.playr;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PlayrService extends Service{

	@Override
	public void onCreate() {
		Log.i("biz.playr.PlayrService","override onCreate");
		super.onCreate();
	}
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Log.i("biz.playr.PlayrService","override onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		Log.i("biz.playr.PlayrService","override onDestroy");
		super.onDestroy();
	}
	@Override
	public IBinder onBind(Intent intent) {
		Log.i("biz.playr.PlayrService","override onBind");
		// TODO start MainActivity
		return null;
	}
}