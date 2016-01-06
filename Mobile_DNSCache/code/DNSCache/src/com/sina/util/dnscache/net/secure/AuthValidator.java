package com.sina.util.dnscache.net.secure;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class AuthValidator {
    public static boolean check(String result){
        try {
            if (TextUtils.isEmpty(result)) {
                return false;
            }
            JSONObject jsonObject = new JSONObject(result);
            int code = jsonObject.optInt("errno");
            if (code == -105) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}
