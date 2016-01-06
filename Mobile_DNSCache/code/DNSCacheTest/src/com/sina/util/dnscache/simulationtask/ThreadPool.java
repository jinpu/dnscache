package com.sina.util.dnscache.simulationtask;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sina.util.dnscache.DNSCache;
import com.sina.util.dnscache.DomainInfo;
import com.sina.util.dnscache.Tools;
import com.sina.util.dnscache.net.HttpClientNetworkRequests;
import com.sina.util.dnscache.net.HttpRequest;
import com.sina.util.dnscache.net.HttpResult;
import com.sina.util.dnscache.net.INetworkRequests;
import com.sina.util.dnscache.net.networktype.NetworkManager;

public class ThreadPool {

	public long ID_NUMBER = 0 ; 
	
	
	public final ExecutorService fixedThreadPool; 
	
	public ThreadPool(int size){
		fixedThreadPool = Executors.newFixedThreadPool(size);
	}
	
	public void runTask(final TaskModel model, final Callable<TaskModel> call){
		
    	fixedThreadPool.execute( new Runnable() {
			@Override
			public void run() {
				
				try {
					
					Thread.currentThread().setName("Task Pool"); 
					
					Tools.log("TAG", "任务开始");
					HttpResult result = null;
					model.taskStartTime = System.currentTimeMillis();   
					model.taskID = ++ID_NUMBER; 
					

					model.domainUrl = model.url ;
					model.domainIp = java.net.InetAddress.getByName(Tools.getHostName(model.domainUrl)).getHostAddress();
					INetworkRequests domainNetRequests = new HttpClientNetworkRequests() ;
					HashMap<String, String> lmapKey = null ;
					lmapKey = new HashMap<String, String>();
					lmapKey.put("Connection", "keep-alive");
					
					long startDomainRequests = System.currentTimeMillis(); 
					result = domainNetRequests.request(new HttpRequest(true, model.domainUrl, lmapKey));
					model.domainInputStreamResult = result.responseByteArray;
					model.domainResult = byteToString(model.domainInputStreamResult) ; 
					model.domainExpendTime = System.currentTimeMillis() - startDomainRequests ; 
					model.domainCode = model.domainResult == null ? 500 : 200 ;
					
					
					
					
					try {
						long startHttpDns = System.currentTimeMillis(); 
						DomainInfo[] infoList = DNSCache.getInstance().getDomainServerIp(model.url) ;
						model.httpDnsExpendTime = System.currentTimeMillis() - startHttpDns ; 
						if( infoList == null || infoList.length == 0 ){
							model.httpDnsResult = "null" ;
						}else{
							model.domainInfo = infoList; 
							for( DomainInfo info : infoList ) model.httpDnsResult += Tools.getHostName(info.url) + "、" ;
						
							HashMap<String, String> hmapKey = null ;
							hmapKey = new HashMap<String, String>();
							hmapKey.put("host", infoList[0].host);
							
							
							model.hostUrl = infoList[0].url ; 
							model.hostIp = Tools.getHostName(infoList[0].url) ; 
							INetworkRequests hostNetRequests = new HttpClientNetworkRequests() ;
							long startHostRequests = System.currentTimeMillis(); 
							result = hostNetRequests.request(new HttpRequest(true, infoList[0].url, hmapKey));
							model.hostInputStreamResult = result.responseByteArray;
							model.hostResult = byteToString(model.hostInputStreamResult) ; 
							
							model.hostExpendTime = System.currentTimeMillis() - startHostRequests ; 
							model.hostCode = model.hostResult == null ? 0 : 200 ;
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					try {
						model.netType = NetworkManager.getInstance().NETWORK_TYPE_STR ;
						model.spName = NetworkManager.getInstance().SP_TYPE_STR ; 
						
						model.status = 1 ; 
						model.taskStopTime = System.currentTimeMillis();   
						model.taskExpendTime = model.taskStopTime - model.taskStartTime ;
						
						Tools.log("TAG", "任务结束");
						call.call();
						
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

					
				} catch (Exception e) {
					Tools.log("TAG", "任务失败");
					e.printStackTrace();
				}
				
//				try {
//					Thread.sleep( (int) (Math.random() * 1000) );
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		} );
	}
	
	
	private String byteToString(byte[] data){
		return new String(data) ; 
	}
	
	public void reset(){
	    ID_NUMBER = 0;
	}
	
	public boolean isShutdown() {
	    return fixedThreadPool.isShutdown();
    }
	
	public void shutdownNow() {
	    fixedThreadPool.shutdownNow();
    }
	
	public void shutdown() {
	    fixedThreadPool.shutdown();	
    }
	
}
