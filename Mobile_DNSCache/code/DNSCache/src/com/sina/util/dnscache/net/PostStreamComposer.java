package com.sina.util.dnscache.net;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

public class PostStreamComposer {

    /**
     * 将map对象格式化为get参数形式
     * 
     * @param params
     * @return
     */
    private static String formatMap2Params(Map<String, String> params) {
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
     * 将String字符串构建成post内容流
     * 
     * @param postContent
     * @return
     */
    public static InputStream buildStringInputStream(String postContent) {
        return new ByteArrayInputStream(postContent.getBytes());
    }

    /**
     * 将MAP对象构建成post内容流
     * 
     * @param content
     * @return
     */
    public static InputStream buildUrlEncodedFormInputStream(Map<String, String> content) {
        String postCotent = formatMap2Params(content);
        return buildStringInputStream(postCotent);
    }

    /**
     * 将字节数组构建成post内容流
     * 
     * @param byteArray
     * @return
     */
    public static InputStream buildByteArrayInputStream(byte[] byteArray) {
        return new ByteArrayInputStream(byteArray);
    }
}
