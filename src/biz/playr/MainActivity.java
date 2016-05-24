package biz.playr;

import java.util.UUID;

import biz.playr.R;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.os.Handler;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
	private WebView webView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Setup restarting of the app when it crashes
		Log.i("biz.playr.MainActivity","onCreate: setup restarting of app on crash");
		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));

		// Setup visibility of system bars    	
		View decorView = getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener
		(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				// Note that system bars will only be "visible" if none of the
				// LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
				if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
					// bars are visible => user touched the screen, make the bars disappear again in 2 seconds 
					Handler handler = new Handler(); 
					handler.postDelayed(new Runnable() { 
						public void run() { 
							hideBars(); 
						} 
					}, 2000); 
				} else {
					// The system bars are NOT visible => do nothing
				}
			}
		});
		setContentView(R.layout.activity_main);

		String playerId = getStoredPlayerId();
		if (playerId == null || playerId.length() == 0) {
			playerId = UUID.randomUUID().toString();
			storePlayerId(playerId);
			Log.i("biz.playr.MainActivity","generated and stored playerId: " + playerId);
		} else {
			Log.i("biz.playr.MainActivity","retrieved stored playerId: " + playerId);
		}

		// Setup webView
		webView = (WebView)findViewById(R.id.mainUiView);
		Log.i("biz.playr.MainActivity","webView is " + (webView == null ? "null" : "not null"));
		setupWebView(webView);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onShowCustomView(View view, CustomViewCallback callback) {
				Log.i("biz.playr.WebChromeClient","override setWebChromeClient");
				super.onShowCustomView(view, callback);
			}
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				Log.i("biz.playr.WebChromeClient","override onConsoleMessage: " + consoleMessage.message());
				return super.onConsoleMessage(consoleMessage);
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				//Return false from the callback instead of calling view.loadUrl 
				//instead. Calling loadUrl introduces a subtle bug where if you 
				//have any iframe within the page with a custom scheme URL 
				//(say <iframe src="tel:123"/>) it will navigate your app's 
				//main frame to that URL most likely breaking the app as a side effect.
				//http://stackoverflow.com/questions/4066438/android-webview-how-to-handle-redirects-in-app-instead-of-opening-a-browser
				return false; // then it is not handled by default action
			}
		});
	
		if (savedInstanceState == null) {
			webView.loadDataWithBaseURL("file:///android_asset/", "<html><head><script type=\"text/javascript\" charset=\"utf-8\">window.location = \"playr_loader.html?player_id="+ playerId + "\"</script><head><body/></html>", "text/html", "UTF-8", null );
		}
	}

	public void handleUncaughtException(Thread paramThread, Throwable paramThrowable){
		Log.e("biz.playr.MainActivity","handleUncaughtException; paramThread: " + paramThread + ", paramThrowable: " + paramThrowable);
		// the context of the activityIntent might need to be the running PlayrService
		// keep the Intent in synch with the Manifest and DefaultExceptionHandler
		Intent activityIntent = new Intent(this.getBaseContext(), biz.playr.MainActivity.class);
		activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		activityIntent.setAction(Intent.ACTION_MAIN);
		activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		startActivity(activityIntent);

		PendingIntent pendingIntent = PendingIntent.getActivity(
				this.getBaseContext(), 0, activityIntent, activityIntent.getFlags());
		//Following code will restart application after <delay> seconds
		AlarmManager mgr = (AlarmManager) biz.playr.MainApplication.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DefaultExceptionHandler.restartDelay, pendingIntent);

		finish();
		android.os.Process.killProcess(android.os.Process.myPid()); 
	}

	@SuppressLint("SetJavaScriptEnabled")
	/** Configure the Webview for usage as the application's window. */
	private void setupWebView(WebView webView) {
		Log.i("biz.playr.MainActivity","setupWebView");
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
			webSettings.setMediaPlaybackRequiresUserGesture(false);
		}
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setAllowFileAccess(true);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			webSettings.setAllowUniversalAccessFromFileURLs(true);
		}
		webSettings.setBuiltInZoomControls(false);
		webSettings.setSupportZoom(false);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
			webSettings.setPluginState(PluginState.ON);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i("biz.playr.MainActivity","override onSaveInstanceState");
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i("biz.playr.MainActivity","override onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		webView.restoreState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		Log.i("biz.playr.MainActivity","override onResume");
		super.onResume();

		hideBars();
		webView.onResume();
	}

	@Override
	protected void onStart() {
		Log.i("biz.playr.MainActivity","override onStart");
		super.onRestart();
	}

	@Override
	protected void onRestart() {
		Log.i("biz.playr.MainActivity","override onRestart");
		super.onRestart();
	}

	@Override
	protected void onPause() {
		Log.i("biz.playr.MainActivity","override onPause");
		webView.onPause();
		super.onPause();
	}

	protected void onStop() {
		Log.i("biz.playr.MainActivity","override onStop");
		// The application is pushed into the background
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		Log.i("biz.playr.MainActivity","override onDestroy");
	
		Log.e("biz.playr.MainActivity",".onDestroy: Prepare to restart the app.");
		Intent intent = new Intent(this, biz.playr.MainActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(
			biz.playr.MainApplication.getInstance().getBaseContext(), 0, intent, intent.getFlags());

		//Following code will restart your application after <delay> seconds
		AlarmManager mgr = (AlarmManager) biz.playr.MainApplication.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + DefaultExceptionHandler.restartDelay, pendingIntent);

		Log.e("biz.playr.MainActivity",".onDestroy: super.onDestroy() !!! About to restart application !!!");
		super.onDestroy();
	}

	@SuppressLint("InlinedApi")
	protected void hideBars() {
		if (getWindow() != null) {
			View decorView = getWindow().getDecorView();
			// Hide both the navigation bar and the status bar.
			// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
			// a general rule, you should design your app to hide the status bar whenever you
			// hide the navigation bar.
			int uiOptions =  View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			decorView.setSystemUiVisibility(uiOptions);
		}
		// Remember that you should never show the action bar if the
		// status bar is hidden, so hide that too if necessary.
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
	}

	@Override
	/** Navigate the WebView's history when the user presses the Back key. */
	public void onBackPressed() {
		if (webView != null) {
			if (webView.canGoBack()) {
				webView.goBack();
			} else {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}

	/*
	 * PRIVATE methods  
	 */
	private String getStoredPlayerId() {
		SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		return sharedPreferences.getString(getString(R.string.player_id_store), "");
	}
	private void storePlayerId(String value) {
		SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(getString(R.string.player_id_store), value);
		editor.commit();
	}
}