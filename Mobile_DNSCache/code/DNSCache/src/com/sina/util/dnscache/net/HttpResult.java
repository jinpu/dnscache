package com.sina.util.dnscache.net;

import java.util.List;
import java.util.Map;

/**
 * 网络请求响应封装类
 * 
 * @author zhaoxingyu
 */
public class HttpResult {
    /**
     * 请求的URL
     */
    public String requestUrl;
    /**
     * 请求的方式。GET/POST
     */
    public String requestMethod;
    /**
     * 请求的内容长度。
     */
    public long requestPostContentLength;
    /**
     * 请求的总大小，包括URL + header + postContent
     */
    public long requestTotalLength;
    /**
     * 请求的header头
     */
    public Map<String, List<String>> requestHeader;
    /**
     * get方式的请求参数
     */
    public Map<String, String> requestGetParams;
    /**
     * post的请求参数
     */
    public Map<String, String> requestPostParams;

    /**
     * HTTP响应状态码
     */
    public int responseStatusCode;
    /**
     * 响应的header头
     */
    public Map<String, List<String>> responseHeader;
    /**
     * 响应的原始文本。 <br/>
     * 可能为<B>NULL</>
     */
    public String responseStr;
    /**
     * 响应的原始数据。 <br/>
     * 可能为<B>NULL</>
     */
    public byte[] responseByteArray;
    /**
     * 响应的内容长度。
     */
    public long responseContentLength;
    /**
     * 请求的开始时间
     */
    public long requestTime; // 请求发起时间
    /**
     * 响应时间
     */
    public long responseTime; // 接收首包时间
    /**
     * 请求的完成时间 <br>
     * 可能为<B> NULL </B>
     */
    public long finishTime; // 结束请求，内容流读取完成

    /**
     * 请求出现异常时，该值不为空
     */
    public Throwable exception; // 异常

    public HttpResult() {
        super();
    }

    public HttpResult(String requestUrl, String requestMethod, long requestPostContentLength, long requestTotalLength,
            Map<String, List<String>> requestHeader, Map<String, String> requestGetParams, Map<String, String> requestPostParams,
            int responseStatusCode, Map<String, List<String>> responseHeader, String responseStr, byte[] responseByteArray,
            long responseContentLength, long requestTime, long responseTime, long finishTime, Throwable exception) {
        super();
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.requestPostContentLength = requestPostContentLength;
        this.requestTotalLength = requestTotalLength;
        this.requestHeader = requestHeader;
        this.requestGetParams = requestGetParams;
        this.requestPostParams = requestPostParams;
        this.responseStatusCode = responseStatusCode;
        this.responseHeader = responseHeader;
        this.responseStr = responseStr;
        this.responseByteArray = responseByteArray;
        this.responseContentLength = responseContentLength;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
        this.finishTime = finishTime;
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "HttpResult [requestUrl=" + requestUrl + ", requestMethod=" + requestMethod + ", requestPostContentLength="
                + requestPostContentLength + ", requestTotalLength=" + requestTotalLength + ", requestHeader=" + requestHeader
                + ", requestGetParams=" + requestGetParams + ", requestPostParams=" + requestPostParams + ", responseStatusCode="
                + responseStatusCode + ", responseHeader=" + responseHeader + ", responseStr=" + responseStr + ", responseByteArray="
                + responseByteArray + ", responseContentLength=" + responseContentLength + ", requestTime=" + requestTime
                + ", responseTime=" + responseTime + ", finishTime=" + finishTime + ", exception=" + exception + "]";
    }

}
