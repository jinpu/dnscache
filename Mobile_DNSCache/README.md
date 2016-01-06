项目中有任何问题欢迎大家来吐槽，一起完善、一起提高、一起使用！<br>
email：gg.fenglei@gmail.com <br>
qq:272141888

<br>



接入说明：
-----------------------------------
目前还没有打包成 jar 文件， 大家测试使用的话可以直接将工程包含到自己的工程内即可。 

 
### 在使用 http dns前 需要初始化一次 
DNSCache.Init(this);


### 直接调用该方法获取 A记录对象
DomainInfo[] infoList = DNSCache.getInstance().getDomainServerIp( "http://api.weibo.cn/index.html" ) ; 

//DomainInfo 返回有可能为null，如果为空则使用域名直接请求数据吧~ 因为在http server 故障的时候会出现这个问题。 

    if( infoList != null ) { 
    //A记录可能会返回多个， 没有特殊需求直接使用第一个即可。  这个数组是经过排序的。 
	DomainInfo domainModel = infoList[0] ;  

    //这里是 android 请求网络。  只需要在http头里添加一个数据即可。 省下的请求数据和原先一样。
    HttpGet getMethod = new HttpGet( domainModel.url );  
    getMethod.setHeader("host", domainModel.host);
    HttpClient httpClient = new DefaultHttpClient();  
    long startDomainRequests = System.currentTimeMillis(); 
    HttpResponse response = httpClient.execute(getMethod); 
    String res = EntityUtils.toString(response.getEntity(), "utf-8") ; 
    Log.d("DINFO", res) ; 
	
	//在请求倒数据后，请配合将一部分信息传递给我。 lib库里面会对这个服务器进行评分计算，lib库永远会优先给你最快，最稳定的服务器地址。
    domainModel.code = String.valueOf( response.getStatusLine().getStatusCode() );
    domainModel.data = res ;
    domainModel.startTime = String.valueOf(startDomainRequests) ;
    DNSCache.getInstance().setDomainServerIpInfo( domainModel );
    }



HttpDns是什么？
-----------------------------------

如果你对 httpdns 还不了解他是什么！<br>
你可以阅读：[【鹅厂网事】全局精确流量调度新思路-HttpDNS服务详解](http://mp.weixin.qq.com/s?__biz=MzA3ODgyNzcwMw==&mid=201837080&idx=1&sn=b2a152b84df1c7dbd294ea66037cf262&scene=2&from=timeline&isappinstalled=0&utm_source=tuicool)<br />
<br>
<br>

传统DNS解析 和 HTTPDNS解析 本质的区别：
-----------------------------------
### 传统DNS解析
	客户端发送udp数据包到dns服务器,dns服务器返回该域名的相关A记录信息。
### HTTPDNS解析
	客户端发起http请求携带需要查询的域名,通过IP直接访问服务器,该Http服务器接倒请求后返回域名对应的A记录。
<br>
<br>

HttpDns sdk （android版本）
-----------------------------------
### 希望解决的问题：
	1.LocalDNS劫持
	2.平均访问延迟下降
	3.用户连接失败率下降


### 目录结构说明：
	HttpDns/code/DNSCache --- HttpDns lib库主工程。
	HttpDns/code/DNSCacheTest --- HttpDns库测试工程。
	HttpDns/doc --- 项目相关的一些文档、流程图、结构图等。
	HttpDns/ui/DNSCacheTest --- 存放HttpDns测试项目UI源文件以及切图文件。


### HttpDns lib库交互流程
[点击看图片](http://gitlab.weibo.cn/msre/Mobile_DNSCache/blob/master/doc/httpdns%20lib%E5%BA%93%E4%BA%A4%E4%BA%92%E6%B5%81%E7%A8%8B.png)<br />
<br>
<br>

### 查询模块  
    检测本地是否有相应的域名缓存
    没有记录则根据当前运营商返回内置的ip节点
    从httpdns查询域名相应A记录，缓存域名信息
    查询模块必须保证响应速度，基于已有设备测试平均在5毫秒左右


### 数据缓存模块  
    根据sp（或wifi名）缓存域名信息
    根据sp（或wifi名）缓存服务器ip信息、优先级
    记录服务器ip每次请求成功数、错误数
    记录服务器ip最后成功访问时间、最后测速
    添加 内存 -》数据库 之间的缓存层


### 评估模块
	根据本地数据，对一组ip排序
	处理用户反馈回来的请求明细，入库
	针对用户反馈是失败请求，进行分析上报预警
	给HttpDns服务端智能分配A记录提供数据依据


### 评估算法插件
	本次测速 - 对ip组的每个ip测速打分
	官方推荐 - HttpDns接口 A记录中返回的优先级
	历史成功 - 该ip7天内成功访问次数
	历史错误 - 该ip7天内访问错误次数
	最后成功时间 - 该ip最后一次成功时间，阈值24


### 打分比重权值分配
	对每个IP打分，总分100分。
	本次测速 - 40分
	官方推荐 - 30分
	历史成功 - 10分
	历史错误 - 10分
	最后成功时间 - 10分
	总分=本次测速+官方推荐+历史成功次数+历史错误次数+最后成功时间

目前权重分配完全基于主观认识，后期会根据建立的相应基线进行权重分配调整。 <br>
> 使用者需要自己权衡，有可能随机的ip速度都好于权重打分的ip。

<br>
<br>
PS:给出一副算法计算分数时的细节图，有兴趣的朋友可以一起探讨研究。 <br><br>
[点击看图片](http://gitlab.weibo.cn/msre/Mobile_DNSCache/blob/master/doc/%E7%AE%97%E6%B3%95%E6%8F%92%E4%BB%B6%E8%AE%A1%E7%AE%97%E5%9B%BE.png)<br />

<br><br>


你可能更需要“它” HttpHook（android版本）
-----------------------------------
[HttpHook](https://github.com/feglei/httphook)是一个转发http请求工具库。<br>
他可以让你在不修改工程源代码的情况下对网络层进行修改、替换、等更多的操作。 <br> <br>

由于我没有微博客户端的源码，为了测试微博客户端是否可以正常使用httpdns库，才诞生的这个项目。<br>
HttpHook 截取 api.weibo.cn 的所有请求，提取到url。<br>
将url中的host域名，传入httpdns库中，使用返回的a记录替换host，进行访问。<br>
访问服务器成功后，在将服务器返回的数据传给 客户端，从而完成一次访问请求。 <br>
这是在 httpdns 项目中使用场景。 大家如果感兴趣可以到httphook项目中详细查看。<br><br>

HttpDns Test （android版本）
-----------------------------------

测试工程主要最初为了模拟用户使用APP发出的网络请求，进行数据记录和对比。<br>
在页面中能很直观的看到每个任务的相关信息。<br>
比如：任务总耗时，httpdns lib库耗时、http请求耗时、以及设备当前环境信息 等。。<br>
由于UE，UI都是自己设计，对于表达信息的布局和美观可能还有欠缺，本程序猿的能力有限，大家多多包涵。 <br><br>

你未必会需要“它”，上传几张测试工程的截图，提供参考。<br>
[点击看图片](http://gitlab.weibo.cn/msre/Mobile_DNSCache/blob/master/doc/%E9%A6%96%E9%A1%B5.png)<br />
<br>
[点击看图片](http://gitlab.weibo.cn/msre/Mobile_DNSCache/blob/master/doc/%E8%AF%A6%E6%83%85%E9%A1%B5-1.png)<br />
[点击看图片](http://gitlab.weibo.cn/msre/Mobile_DNSCache/blob/master/doc/%E8%AF%A6%E6%83%85%E9%A1%B5-2.png)<br />
[点击看图片](http://gitlab.weibo.cn/msre/Mobile_DNSCache/blob/master/doc/%E8%AF%A6%E6%83%85%E9%A1%B5-3.png)<br />
<br>