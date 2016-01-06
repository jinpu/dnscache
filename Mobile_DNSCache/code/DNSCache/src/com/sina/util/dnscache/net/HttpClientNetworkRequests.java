/**
 * 
 */
package com.sina.util.dnscache.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import com.sina.util.dnscache.Tools;

/**
 * {@link HttpURLConnection}的轻量级网络请求引擎
 * 
 * @author xingyu10
 *
 */
public class HttpClientNetworkRequests implements INetworkRequests {
    private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
    private static final int CONNECTION_TIMEOUT = 30 * 1000;
    private static final int BUFFER_SIZE = 16 * 1024;

    /**
     * 将map对象转换成流格式
     * 
     * @param params
     * @return
     */
    private InputStream convertPostParams2Stream(HashMap<String, String> params) {
        InputStream inputStream = null;
        if (null != params && params.size() > 0) {
            String postContent = formatMap2Params(params);
            inputStream = new ByteArrayInputStream(postContent.getBytes());
        }
        return inputStream;
    }

    /**
     * 将map对象格式化为get参数形式
     * 
     * @param params
     * @return
     */
    private String formatMap2Params(HashMap<String, String> params) {
        String postContent = null;
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey())//
                    .append("=")//
                    .append(entry.getValue())//
                    .append("&")//
            ;
        }
        if (sb.length() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        postContent = sb.toString();
        return postContent;
    }

    /**
     * 计算请求的总大小(非精确计算)
     * @param url
     * @param type 
     * @param header
     * @param contentLen
     * @return
     */
    public long calculateRequestTotalLength(URL url, RequestType type, Map<String, String> header, long contentLen) {
        int urlLen = type.getMethod().length() + 1 + url.toString().length() + 1 + "HTTP/1.1".length();
        int headerLen = 0;
        if (null != header) {
            Set<Entry<String, String>> entrySet = header.entrySet();
            for (Entry<String, String> entry : entrySet) {
                headerLen = (headerLen + 1 + entry.getKey().length() + entry.getValue().length());
            }
        }
        return urlLen + headerLen + contentLen;
    }

    @Override
    public HttpResult get(String url) {
        return request(url, null, null, (InputStream) null, RequestType.GET);
    }

    @Override
    public HttpResult get(String url, HashMap<String, String> getParams) {
        return request(url, null, getParams, (InputStream) null, RequestType.GET);
    }

    @Override
    public HttpResult get(String url, HashMap<String, String> head, HashMap<String, String> getParams) {
        return request(url, head, getParams, (InputStream) null, RequestType.GET);
    }

    @Override
    public HttpResult post(String url) {
        return request(url, null, null, (InputStream) null, RequestType.POST);
    }

    @Override
    public HttpResult post(String url, HashMap<String, String> postParams) {
        return request(url, null, null, postParams, RequestType.POST);
    }

    @Override
    public HttpResult post(String url, HashMap<String, String> head, HashMap<String, String> postParams) {
        return request(url, head, null, postParams, RequestType.POST);
    }

    @Override
    public HttpResult request(String url, HashMap<String, String> head, HashMap<String, String> getParams,
            HashMap<String, String> postParams, RequestType type) {
        InputStream postStream = convertPostParams2Stream(postParams);
        HttpResult httpResult = request(url, head, getParams, postStream, type);
        return httpResult;
    }

    @Override
    public HttpResult request(String url, HashMap<String, String> header, HashMap<String, String> getParams, InputStream postStream,
            RequestType type) {
        return request(url, header, getParams, postStream, type, null);
    }

    @Override
    public HttpResult request(String url, HashMap<String, String> header, HashMap<String, String> getParams, InputStream postStream,
            RequestType type, IHttpReqeustCallBack callBack) {
        HttpRequest httpRequest = new HttpRequest(false, false, url, header, getParams, postStream, type, callBack);
        return request(httpRequest);
    }

    @Override
    public HttpResult request(HttpRequest httpRequest) {
        HttpResult httpResult = new HttpResult();
        /**
         * 是否强制获取字符串结果
         */
        boolean forceString = httpRequest.forceString;

        /**
         * 是否强制获取字节数组结果
         */
        boolean forceByteArr = httpRequest.forceByteArr;

        /**
         * 设置请求的url
         */
        String url = httpRequest.url;

        /**
         * 设置请求的header
         */
        HashMap<String, String> header = httpRequest.header;

        /**
         * 设置GET请求的参数
         */
        HashMap<String, String> getParams = httpRequest.getParams;

        /**
         * 设置POST请求的输入流（网络层会读取该流，作为输出内容）
         */
        InputStream postStream = httpRequest.postStream;

        /**
         * 设置请求的类型。GET/POST
         */
        RequestType type = httpRequest.type;

        /**
         * 设置请求的回调
         */
        IHttpReqeustCallBack callBack = httpRequest.callBack;
        HttpURLConnection urlConnection = null;
        try {
            if (null != getParams && getParams.size() > 0) {
                String params = formatMap2Params(getParams);
                if (!url.contains("?")) {
                    url = url + "?";
                }
                url = url + params;
            }
            httpResult.requestGetParams = getParams;
            httpResult.requestTime = System.currentTimeMillis();
            httpResult.requestMethod = type.getMethod();
            httpResult.requestUrl = url;
            URL requestUrl = new URL(url);
            if (null != callBack) {
                callBack.onStart();
            }
            urlConnection = (HttpURLConnection) requestUrl.openConnection();
            if ("https".equalsIgnoreCase(requestUrl.getProtocol())) {
                HttpsURLConnection cons = (HttpsURLConnection) urlConnection;
                cons.setHostnameVerifier(new AllowAllHostnameVerifier());
            }
            urlConnection.setRequestMethod(type.getMethod());
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(SOCKET_OPERATION_TIMEOUT);

            urlConnection.setRequestProperty("Connection", "keep-alive");

            if (null != header && header.size() > 0) {
                for (Entry<String, String> entry : header.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            httpResult.requestHeader = urlConnection.getRequestProperties();
            if (null != postStream) {
                urlConnection.setDoOutput(true);
                OutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                byte[] buffer = new byte[BUFFER_SIZE];
                int len = -1;
                long totalLen = 0;
                while ((len = postStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                    totalLen += len;
                }
                out.close();
                httpResult.requestPostContentLength = totalLen;
            }
            httpResult.requestTotalLength = calculateRequestTotalLength(requestUrl, type, header, httpResult.requestPostContentLength);
            int responseCode = urlConnection.getResponseCode();
            httpResult.responseStatusCode = responseCode;
            httpResult.responseTime = System.currentTimeMillis();
            Map<String, List<String>> responseHeader = urlConnection.getHeaderFields();
            httpResult.responseHeader = responseHeader;
            int responseContentLength = urlConnection.getContentLength();
            httpResult.responseContentLength = responseContentLength;
            InputStream responseStream = new BufferedInputStream(urlConnection.getInputStream());
            if (null != responseStream) {
                // set default charset
                String charset = "UTF-8";
                // should convert inputstream to a string ？
                boolean shouldConvert2Str = false;
                if (null != responseHeader) {
                    List<String> contentTypes = responseHeader.get("Content-Type");
                    if (null != contentTypes && contentTypes.size() > 0) {
                        try {
                            String contentType = contentTypes.get(0);
                            contentType = contentType.toLowerCase();
                            int index = -1;
                            String target = "charset=";
                            if ((index = contentType.indexOf(target)) != -1) {
                                charset = contentType.substring(index + target.length(), contentType.length());
                                charset = charset.trim();
                            }
                            shouldConvert2Str = (contentType.contains(target) || contentType.contains("text")
                                    || contentType.contains("json") || contentType.contains("xml"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            charset = "UTF-8";
                        }
                    }
                }

                boolean shouldReadStream2Memory = shouldConvert2Str || forceString || forceByteArr;
                boolean shouldCallBack = (null != callBack);
                boolean shouldReadStream = shouldReadStream2Memory || shouldCallBack;
                /**
                 * 判断是否应该读取响应流
                 */
                if (shouldReadStream) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len = 0;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int contentLength = 0;
                    while ((len = responseStream.read(buffer)) != -1) {
                        if (shouldReadStream2Memory) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        if (shouldCallBack) {
                            callBack.onUpdate(buffer, 0, len, responseContentLength);
                        }
                        contentLength += len;
                    }
                    //这里再重新给响应内容大小 赋值
                    httpResult.responseContentLength = contentLength;
                    /**
                     * 强制获取原始数据
                     */
                    if (forceByteArr) {
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        httpResult.responseByteArray = byteArray;
                    }

                    /**
                     * 如果返回的内容本身就是TEXT格式 || 强制获取字符串形式结果
                     */
                    if (shouldConvert2Str || forceString) {
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        httpResult.responseStr = new String(byteArray, charset);
                    }
                    byteArrayOutputStream.close();
                }
            }
            httpResult.finishTime = System.currentTimeMillis();
        } catch (Exception e) {
            httpResult.exception = e;
        } catch (OutOfMemoryError e) {
            httpResult.exception = e;
        } finally {
            try {
                if (null != postStream) {
                    postStream.close();
                }
                if (null != urlConnection) {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                httpResult.exception = e;
            }

            if (null != callBack) {
                callBack.onResult(httpResult);
            }
        }
        Tools.log("TAG", httpResult.toString());
        return httpResult;
    }

}
