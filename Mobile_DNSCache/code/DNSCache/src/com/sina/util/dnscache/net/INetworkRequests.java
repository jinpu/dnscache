/**
 * 
 */
package com.sina.util.dnscache.net;

import java.io.InputStream;
import java.util.HashMap;

/**
 * 网络模块的接口定义
 * 
 * @author xingyu10
 *
 */
public interface INetworkRequests {

    public HttpResult get(String url);

    public HttpResult get(String url, HashMap<String, String> getParams);

    public HttpResult get(String url, HashMap<String, String> header, HashMap<String, String> getParams);

    public HttpResult post(String url);

    public HttpResult post(String url, HashMap<String, String> postParams);

    public HttpResult post(String url, HashMap<String, String> header, HashMap<String, String> postParams);

    public HttpResult request(String url, HashMap<String, String> header, HashMap<String, String> getParams,
            HashMap<String, String> postParams, RequestType type);

    public HttpResult request(String url, HashMap<String, String> header, HashMap<String, String> getParams, InputStream postStream,
            RequestType type);

    public HttpResult request(String url, HashMap<String, String> header, HashMap<String, String> getParams, InputStream postStream,
            RequestType type, IHttpReqeustCallBack callBack);
    
    public HttpResult request(HttpRequest httpRequest);

    

}
