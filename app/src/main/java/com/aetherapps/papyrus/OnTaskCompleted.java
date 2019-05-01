package com.aetherapps.papyrus;

import org.json.JSONObject;

/**
 * Created by James on 27/04/2019.
 */

public interface OnTaskCompleted {
    void onTaskCompleted(String functionName, JSONObject results);


}