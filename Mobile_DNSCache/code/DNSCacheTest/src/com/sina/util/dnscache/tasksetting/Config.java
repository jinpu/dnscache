package com.sina.util.dnscache.tasksetting;

import java.util.ArrayList;

import com.sina.util.dnscache.R;
import com.sina.util.dnscache.R.id;

public class Config {
	
    public static final int DEFCONCURRENCYNUM = 3 ; 
    
    public static final int DEFREQUESTSNUM = 50 ; 
	/**
	 * 配置的url列表文件路径
	 */
	public static String urlFilePath = "" ; 
	
	/**
	 * 配置的线程池并发线程数
	 */
	public static int concurrencyNum = SpfConfig.getInstance().getInt(R.id.config_threadpool_concurrent_num + "", Config.DEFCONCURRENCYNUM); ; 
	
	/**
	 * 配置的总请求数量
	 */
	public static int requestsNum = SpfConfig.getInstance().getInt(R.id.config_threadpool_request_num + "", Config.DEFREQUESTSNUM);
	
	
	/**
	 * 文本文件中的Url
	 */
	public static ArrayList<String> fileUrlList = null ; 
	
	public static void updateThreadpoolConfig() {
	    concurrencyNum = SpfConfig.getInstance().getInt(R.id.config_threadpool_concurrent_num + "", Config.DEFCONCURRENCYNUM);
	    requestsNum = SpfConfig.getInstance().getInt(R.id.config_threadpool_request_num + "", Config.DEFREQUESTSNUM);
    }
	
}
