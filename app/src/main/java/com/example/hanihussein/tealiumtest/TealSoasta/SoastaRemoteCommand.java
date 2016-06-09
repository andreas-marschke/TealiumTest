package com.example.hanihussein.tealiumtest.TealSoasta;// TODO: Update to the correct package name for your package
// package com.tealium.helper;


import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.soasta.mpulse.android.MPulse;
import com.tealium.internal.tagbridge.RemoteCommand;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by craigrouse on 16/02/2016.
 */
public final class SoastaRemoteCommand extends RemoteCommand {

    private static boolean isSoastaInitialized = false;
    private static Map<String, String> timerMap = new HashMap<>(5);

    public SoastaRemoteCommand (Application application, Context mContext) {
        super("soasta", "Soasta Remote Command");
    }
    @Override
    protected void onInvoke(RemoteCommand.Response response){
        Log.v("Tealium SOASTA", "SOASTA remote command invoked");
        String command = response.getRequestPayload().optString("api_command", null);
        String viewGroup = response.getRequestPayload().optString("soasta_view_group", null);
        Long timer_value;
        try {
            timer_value = Long.parseLong(response.getRequestPayload().optString("soasta_timer_value", null), 10);
        } catch (NumberFormatException e) {
            timer_value = null;
            Log.v("Tealium SOASTA", "timer value could not be parsed as a LONG number.");
        }
        String timer_name = response.getRequestPayload().optString("soasta_timer_name", null);
        String dimension_name = response.getRequestPayload().optString("soasta_dimension_name", null);
        String dimension_value = response.getRequestPayload().optString("soasta_dimension_value", null);
        JSONObject soasta_dimensions = response.getRequestPayload().optJSONObject("soasta_custom_dimensions");
        JSONArray soasta_reset_dimensions = response.getRequestPayload().optJSONArray("soasta_reset_dimensions");
        Integer soasta_metric_value;
        try {
            soasta_metric_value = Integer.parseInt(response.getRequestPayload().optString("soasta_metric_value", null));
        } catch (NumberFormatException e) {
            soasta_metric_value = null;
            Log.v("Tealium SOASTA", "metric value could not be parsed as an INTEGER number.");
        }
        String soasta_metric_name = response.getRequestPayload().optString("soasta_metric_name", null);
        String soasta_api_key = response.getRequestPayload().optString("soasta_api_key", null);
        String timerId;
        String[] commandArray;
        MPulse mpulse;
        // split the commands into an array
        commandArray = command.split(",");

        for (int j = 0, commandlen = commandArray.length; j < commandlen; j++) {
            command = commandArray[j];

            // init Soasta if the API key is provided or the command is initSoasta
            if ("initSoasta".equals(command) || (soasta_api_key != null && !isSoastaInitialized)) {
                if (soasta_api_key != null) {
                    MPulse.sharedInstance().initializeWithAPIKey(soasta_api_key);
                    isSoastaInitialized = true;
                } else {
                    Log.v("Tealium SOASTA", "No API key provided for SOASTA");
                    response.setBody("SOASTA API KEY NOT PROVIDED");
                    response.setStatus(404);
                }
            }

            mpulse = MPulse.sharedInstance();


            if (!"initSoasta".equals(command) && !isSoastaInitialized) {
                Log.v("Tealium SOASTA", "SOASTA not initialised correctly. Exiting");
            } else if (mpulse == null) {
                Log.v("Tealium SOASTA", "SOASTA not initialised correctly. MPulse sharedInstance is null.");
            } else if ("setViewGroup".equals(command)) {
                // Set a value using setViewGroup and the group name
                if (viewGroup != null) {
                    mpulse.setViewGroup(viewGroup);
                } else {
                    Log.v("Tealium SOASTA", "View group returned was null");
                }
            } else if ("resetViewGroup".equals(command)) {
                // Reset the currently set View Group value. Does not take any arguments.
                mpulse.resetViewGroup();
            } else if ("startTimer".equals(command)) {
                // SEND A TIMER USING START AND STOP
                if (timer_name != null) {
                    timerId = mpulse.startTimer(timer_name);
                    // return the timer ID for later storage in the IQ webview
                    response.setBody("soasta_timer_id=" + timerId);
                    timerMap.put(timer_name, timerId);
                } else {
                    Log.v("Tealium SOASTA", "Timer start was called, but no timer name was called");
                }
            } else if ("stopTimer".equals(command)) {
                // SEND A TIMER USING START AND STOP
                if (timer_name != null) {
                    timerId = timerMap.get(timer_name);
                    if (timerId != null) {
                        mpulse.stopTimer(timerId);
                    }
                } else {
                    Log.v("Tealium SOASTA", "Timer stop was called, but no timer ID was passed in");
                }
            } else if ("sendCustomTimer".equals(command)) {
                //  SEND A TIMER BY NAME AND VALUE
                if (timer_name != null && timer_value != null) {
                    mpulse.sendTimer(timer_name, timer_value);
                } else {
                    Log.v("Tealium SOASTA", "Custom timer was called, but no arguments passed in (name and value).");
                }
            } else if ("sendMetric".equals(command)) {
                // SEND A METRIC USING METRIC NAME AND VALUE
                if (soasta_metric_name != null && soasta_metric_value != null) {
                    mpulse.sendMetric(soasta_metric_name, soasta_metric_value);
                } else {
                    Log.v("Tealium SOASTA", "Send metric was called, but no metric name or value were passed in");
                }
            } else if ("setSingleDimension".equals(command)) {
                // accepts 2 properties - dimension name and dimension value as strings
                // Set or reset a value using setDimension and the dimension name:
                if (dimension_name != null && dimension_value != null) {
                    mpulse.setDimension(dimension_name, dimension_value);
                } else {
                    Log.v("Tealium SOASTA", "setSingleDimension was called, but no dimension name/value was passed in.");
                }
            } else if ("resetSingleDimension".equals(command)) {
                // accepts a string containing the single dimension name to be reset
                if (dimension_name != null) {
                    mpulse.resetDimension(dimension_name);
                } else {
                    Log.v("Tealium SOASTA", "Reset Dimension was called, but no dimension name was passed in.");
                }
            } else if ("setMultipleDimensions".equals(command)) {
                // accepts an object containing dimensions to be set, loop through object and set each dimension individually
                Iterator<String> iterator = soasta_dimensions.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String val = soasta_dimensions.optString(key, null);
                    if (val != null) {
                        mpulse.setDimension(key, val);
                    } else {
                        Log.v("Tealium SOASTA", "Value provided to setDimension was empty.");
                    }
                }
            } else if ("resetMultipleDimensions".equals(command)) {
                // accepts an array of dimension names to be reset
                int len = soasta_reset_dimensions.length();
                for (int i = 0; i < len; i++) {
                    mpulse.resetDimension(soasta_reset_dimensions.optString(i, null));
                }
            } else if ("disableSoasta".equals(command)) {
                mpulse.disable();
            } else if ("enableSoasta".equals(command)) {
                mpulse.enable();
            }
        }
        response.send();
    }
}