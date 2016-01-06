package com.sina.util.dnscache.dnsp.impl;

import java.util.HashMap;

import android.text.TextUtils;

import com.sina.util.dnscache.ServerSelector;
import com.sina.util.dnscache.dnsp.DnsConfig;
import com.sina.util.dnscache.dnsp.IDnsProvider;
import com.sina.util.dnscache.dnsp.IJsonParser;
import com.sina.util.dnscache.dnsp.IJsonParser.JavaJSON_SINAHTTPDNS;
import com.sina.util.dnscache.log.HttpDnsLogManager;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.net.HttpClientNetworkRequests;
import com.sina.util.dnscache.net.HttpResult;

public class SinaHttpDns implements IDnsProvider{

    private HttpClientNetworkRequests netWork;
    private JavaJSON_SINAHTTPDNS jsonObj;
    public SinaHttpDns() {
        netWork = new HttpClientNetworkRequests();
        jsonObj = new IJsonParser.JavaJSON_SINAHTTPDNS();
    }

    @Override
    public HttpDnsPack requestDns(String domain) {
        HttpDnsPack dnsPack = null;
        ServerSelector serverSelector = new ServerSelector();
        boolean succ = false;
        while (!succ && serverSelector.hasNext()) {
            try {
                String api = serverSelector.next();
                if (!TextUtils.isEmpty(api) && !(api.toLowerCase()).startsWith("http")) {
                    api = ("http://" + api);
                }
                String sina_httpdns_api_url = api + "/dns";
                HashMap<String, String> getParams = new HashMap<String, String>();
                getParams.put("domain", domain);
                HttpResult httpResult = netWork.get(sina_httpdns_api_url, getParams);
                if (null != httpResult && null != httpResult.responseStr) {
                    dnsPack = jsonObj.JsonStrToObj(httpResult.responseStr);
                    if (null != dnsPack) {
                        succ = true;
                        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_PACK, dnsPack.toJson(),
                                true, 10 );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_INFO_PACK, "{\"errMsg\":" + "\"" + e.getMessage() + "\"}",
                        true, 10 );
            }
        }
        return dnsPack;
    }

    @Override
    public boolean isActivate() {
        return DnsConfig.enableSinaHttpDns;
    }
    

    @Override
    public String getServerApi() {
        String serverApi = "";
        if (!TextUtils.isEmpty(ServerSelector.usingServerApi)) {
            serverApi = ServerSelector.usingServerApi;
        } else {
            boolean yes = DnsConfig.SINA_HTTPDNS_SERVER_API.size() > 0;
            if (yes) {
                serverApi = DnsConfig.SINA_HTTPDNS_SERVER_API.get(0);
            }
        }
        return serverApi;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
