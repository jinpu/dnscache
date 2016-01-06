package com.sina.util.dnscache;

import java.util.ArrayList;

import com.sina.util.dnscache.dnsp.DnsConfig;
/**
 * 
 * @author xingyu10
 *
 */
public class ServerSelector {

    public static String usingServerApi;
    private ArrayList<String> serverApis;

    public ServerSelector() {
        serverApis = new ArrayList<String>();
        serverApis.addAll(DnsConfig.SINA_HTTPDNS_SERVER_API);
    }
    /**
     * 找到一个server
     * @return
     */
    public String next() {
        String api = "";
        if (hasNext()) {
            int index = serverApis.indexOf(usingServerApi);
            if (index != -1) {
                api = serverApis.remove(index);
            } else {
                api = serverApis.remove(0);
                usingServerApi = api;
            }
            if (api.endsWith("/")) {
                api = (String) api.subSequence(0, api.length() - 1);
            }
        }
        return api;
    }

    public boolean hasNext() {
        return serverApis.size() > 0;
    }

}
