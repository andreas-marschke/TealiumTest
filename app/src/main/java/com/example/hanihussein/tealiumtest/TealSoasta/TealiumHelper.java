// TODO: Update this with your package name, and uncomment this line
package com.example.hanihussein.tealiumtest.TealSoasta;

/**
 * Created by craigrouse on 06/03/2016.
 */

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.WebView;

import com.tealium.library.Tealium;
import com.tealium.lifecycle.LifeCycle;

import java.util.Iterator;
import java.util.Map;

import com.tealium.library.BuildConfig;

public final class TealiumHelper {

    // Identifier for the main instance
    public static final String TEALIUM_INSTANCENAME = "vodafone";

    @SuppressLint("NewApi")
    public static void initialize(Application application) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            // To connect to WebView if Tag Management is enabled
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // TODO: Change profile name to the profile name for your specific app. Change profile name to "prod" if this is a production build
        Tealium.Config config = Tealium.Config.create(application, "vodafone", "eg-myvfapp-android", "dev");
        // specifies if lifecycle auto tracking should be enbabled or not
        boolean isAutoTracking = true;
        // setup the lifecycle tracking instance
        LifeCycle.setupInstance(TEALIUM_INSTANCENAME, config, isAutoTracking);
        // create a new Tealium instance with the specified instance name
        Tealium.createInstance(TEALIUM_INSTANCENAME, config);
        // Add a remote command for SOASTA to enable SOASTA integration. Uncomment line to use SOASTA via Tealium IQ.
        Tealium.getInstance(TEALIUM_INSTANCENAME).addRemoteCommand(new SoastaRemoteCommand(application, application.getApplicationContext()));
        // To be used in Phase 2 for Qualtrics
//        Tealium.getInstance(TEALIUM_INSTANCENAME).addRemoteCommand(new QualtricsRemoteCommand(application));
    }

    // intended to be called from one of the tracking methods (trackView or trackEvent)
    /* Example:

            Map<String,String> tealiumMap = new HashMap(1);
            tealiumMap.put("somekey","somevalue");
            addVolatileDataString(instance, tealiumMap);
    */
    private static void addVolatileDataString(Tealium instance, Map<String, String> data) {
        // instance can be remotely destroyed by publish settings
        if (instance != null && data != null) {
            Iterator it = data.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry prop = (Map.Entry) it.next();
                String key = (String) prop.getKey();
                String val = (String) prop.getValue();
                instance.getDataSources().getVolatileDataSources().put(key, val);
            }
        }
    }

    // intended to be called from one of the tracking methods (trackView or trackEvent)
    /* Example:

            Map<String,String> tealiumMap = new HashMap(1);
            tealiumMap.put("somekey","somevalue");
            addPersistentDataString(instance, tealiumMap);
    */
    private static void addPersistentDataString(Tealium instance, Map<String, String> data) {
        // instance can be remotely destroyed by publish settings
        if (instance != null && data != null) {
            Iterator it = data.entrySet().iterator();
            SharedPreferences.Editor persistentData = instance.getDataSources().getPersistentDataSources().edit();
            while (it.hasNext()) {
                Map.Entry prop = (Map.Entry) it.next();
                String key = (String) prop.getKey();
                String val = (String) prop.getValue();
                persistentData.putString(key, val);
            }
            persistentData.commit();
        }
    }

    public static void trackEvent(String eventName, Map<String, Object> data) {
        final Tealium instance = Tealium.getInstance(TEALIUM_INSTANCENAME);

        // instance can be remotely destroyed by publish settings
        if (instance != null) {
            instance.trackEvent(eventName, data);
        }
    }

    public static void trackView(String viewName, Map<String, Object> data) {
        final Tealium instance = Tealium.getInstance(TEALIUM_INSTANCENAME);
        if (instance != null) {
            instance.trackView(viewName, data);
        }
    }

    // returns the current Tealium instance for later use if required
    public static Tealium getInstance() {
        return Tealium.getInstance(TEALIUM_INSTANCENAME);
    }
}
