package biz.playr;

import android.app.Application;

/** Initializes native components when the user starts the application. */
public class MainApplication extends Application {

	// Singleton instance
    private static MainApplication instance = null;

    @Override
    public void onCreate() {
    	super.onCreate();

    	// Setup singleton instance
        instance = this;
    }

    // Getter to access Singleton instance
    public static MainApplication getInstance() {
        return instance ; 
    }
}