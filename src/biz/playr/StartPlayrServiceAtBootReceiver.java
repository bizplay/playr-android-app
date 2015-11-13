package biz.playr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartPlayrServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, biz.playr.PlayrService.class);
            context.startService(serviceIntent);
        }
    }
}
