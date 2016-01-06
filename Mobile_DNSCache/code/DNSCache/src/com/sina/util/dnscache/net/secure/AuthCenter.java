package com.sina.util.dnscache.net.secure;

import java.util.HashMap;

import android.text.TextUtils;

import com.sina.util.dnscache.AppConfigUtil;

public class AuthCenter {
    /**
     * 客户端标示
     */
    private static final String c = "httpdns";
    /**
     * 客户端标示
     */
    private static final String k = "iheRFsFhLE9h9TRHVRLLBD6eS9ccQdLe";
    
    private static final HashMap<String, String> authParams = new HashMap<String, String>();
    
    public static void init() {
        String did = AppConfigUtil.getDeviceId();
        if (!TextUtils.isEmpty(did)) {
            String ciphertext = MD5.hexdigest(did + k);
            if (!TextUtils.isEmpty(ciphertext) && ciphertext.length() == 32) {
                String s = "" + ciphertext.charAt(1)//
                        + ciphertext.charAt(5)//
                        + ciphertext.charAt(2)//
                        + ciphertext.charAt(10)//
                        + ciphertext.charAt(17)//
                        + ciphertext.charAt(9)//
                        + ciphertext.charAt(25)//
                        + ciphertext.charAt(27)//
                ;
                authParams.put("c", c);
                authParams.put("s", s);
                authParams.put("did", did);
            }
        }
    }
    
    
    public static HashMap<String, String> getAuthParmas(){
        if (authParams.size() == 0) {
            init();
        }
        return authParams;
    }
}
