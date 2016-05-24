package biz.playr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartPlayrServiceAtBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	Log.i("biz.playr.StartPlayrServiceAtBootReceiver", "overide onReceive");
	if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
		Intent serviceIntent = new Intent(context, biz.playr.PlayrService.class);
		context.startService(serviceIntent);
		Log.i("biz.playr.StartPlayrServiceAtBootReceiver", "onReceive: started PlayrService");
		}
	}
}
