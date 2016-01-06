package com.sina.util.dnscache.net;
/**
 * 请求的回调接口
 * @author xingyu10
 *
 */
public interface IHttpReqeustCallBack {

        public void onStart();

        public void onUpdate(byte[] buffer, int offset, int length, int totalLength);

        public void onResult(HttpResult result);
    }