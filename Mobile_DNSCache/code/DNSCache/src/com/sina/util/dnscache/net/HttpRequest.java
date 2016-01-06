package com.sina.util.dnscache.net;

import java.io.InputStream;
import java.util.HashMap;

/**
 * 请求的实体封装
 * 
 * @author xingyu10
 *
 */
public class HttpRequest {

    /**
     * 是否强制获取字符串结果
     */
    public boolean forceString;

    /**
     * 是否强制获取字节数组结果
     */
    public boolean forceByteArr;

    /**
     * 设置请求的url
     */
    public String url;

    /**
     * 设置请求的header
     */
    public HashMap<String, String> header;

    /**
     * 设置GET请求的参数
     */
    public HashMap<String, String> getParams;

    /**
     * 设置POST请求的输入流（网络层会读取该流，作为输出内容）
     */
    public InputStream postStream;

    /**
     * 设置请求的类型。GET/POST
     */
    public RequestType type = RequestType.GET;

    /**
     * 设置请求的回调
     */
    public IHttpReqeustCallBack callBack;

    public HttpRequest(boolean forceString, boolean forceByteArr, String url, HashMap<String, String> header,
            HashMap<String, String> getParams, InputStream postStream, RequestType type, IHttpReqeustCallBack callBack) {
        super();
        this.forceString = forceString;
        this.forceByteArr = forceByteArr;
        this.url = url;
        this.header = header;
        this.getParams = getParams;
        this.postStream = postStream;
        this.type = type;
        this.callBack = callBack;
    }

    public HttpRequest(boolean forceByteArr, String url, HashMap<String, String> header) {
        super();
        this.forceByteArr = forceByteArr;
        this.url = url;
        this.header = header;
    }

    public HttpRequest(String url) {
        super();
        this.url = url;
    }

    
    @Override
    public String toString() {
        return "HttpRequest [forceString=" + forceString + ", forceByteArr=" + forceByteArr + ", url=" + url + ", header=" + header
                + ", getParams=" + getParams + ", postStream=" + postStream + ", type=" + type + ", callBack=" + callBack + "]";
    }


    public static class HttpRequestFactory {

        public static HttpRequest createDefault(String url) {
            return new HttpRequest(url);
        }
    }
}
