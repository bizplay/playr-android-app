package biz.playr;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PlayrService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO start MainActivity
		return null;
	}

}