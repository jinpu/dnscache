package com.sina.util.dnscache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.sina.util.dnscache.cache.DnsCacheManager;
import com.sina.util.dnscache.cache.DnsCacheOverdueMgr;
import com.sina.util.dnscache.cache.IDnsCache;
import com.sina.util.dnscache.dnsp.DnsManager;
import com.sina.util.dnscache.dnsp.IDns;
import com.sina.util.dnscache.log.HttpDnsLogManager;
import com.sina.util.dnscache.model.DomainModel;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.model.IpModel;
import com.sina.util.dnscache.net.HttpClientNetworkRequests;
import com.sina.util.dnscache.net.HttpResult;
import com.sina.util.dnscache.net.INetworkRequests;
import com.sina.util.dnscache.net.RequestType;
import com.sina.util.dnscache.net.networktype.Constants;
import com.sina.util.dnscache.net.networktype.NetworkManager;
import com.sina.util.dnscache.net.networktype.NetworkStateReceiver;
import com.sina.util.dnscache.net.secure.AuthCenter;
import com.sina.util.dnscache.net.secure.AuthValidator;
import com.sina.util.dnscache.query.IQuery;
import com.sina.util.dnscache.query.QueryManager;
import com.sina.util.dnscache.score.IScore;
import com.sina.util.dnscache.score.ScoreManager;
import com.sina.util.dnscache.speedtest.ISpeedtest;
import com.sina.util.dnscache.speedtest.SpeedtestManager;
import com.sina.util.dnscache.thread.RealTimeThreadPool;

/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: DNSCache <br>
 * 类描述: Lib库全局 对外实例对象 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-3-26 下午5:26:04 <br>
 * 
 * 修改人: <br>
 * 修改时间: <br>
 * 修改备注: <br>
 * 
 * @version V1.0
 */
@SuppressLint("NewApi")
public class DNSCache {

    // ///////////////////////////////////////////////////////////////////////////////////

    public static boolean isEnable = true;
    public static int timer_interval = 60 * 1000;
    private static DNSCache Instance = null;
    private static Context sContext;
    private static Object lock = new Object();

    public IDnsCache dnsCacheManager = null;
    public IQuery queryManager = null;
    public IScore scoreManager = null;
    public IDns dnsManager = null;
    public ISpeedtest speedtestManager = null;
    public INetworkRequests networkRequests = null;
    public DnsCacheOverdueMgr<String> dnsCacheOverdueMgr;

    public DNSCache(Context ctx) {
        dnsCacheManager = new DnsCacheManager(ctx);
        queryManager = new QueryManager(dnsCacheManager);
        scoreManager = new ScoreManager();
        dnsManager = new DnsManager();
        speedtestManager = new SpeedtestManager();
        networkRequests = new HttpClientNetworkRequests();
        dnsCacheOverdueMgr = new DnsCacheOverdueMgr<String>();
        startTimer();
    }

    public static DNSCache getInstance() {
        if (null == Instance) {
            synchronized (lock) {
                if (Instance == null) {
                    Instance = new DNSCache(sContext);
                }
            }
        }
        return Instance;
    }

    public static void Init(Context ctx) {
        if (null == ctx){
            throw new RuntimeException("DNSCache Init; context can not be null!!!");
        }
        sContext = ctx.getApplicationContext();
        // 根据配置文件 初始化策略
        DNSCacheConfig.InitCfg(sContext);
        NetworkManager.CreateInstance(sContext);
        AppConfigUtil.init(sContext);
        AuthCenter.init();
        NetworkStateReceiver.register(sContext);
        Instance = null;
    }

    /**
     * 预加载逻辑
     * 
     * @param domains
     */
    public void preLoadDomains(final String[] domains) {
        for (String domain : domains) {
            dnsCacheOverdueMgr.schedule(domain);
            checkUpdates(domain, true);
        }
    }

    public IDnsCache getDnsCacheManager() {
        return dnsCacheManager;
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取 HttpDNS信息
     *
     * @param url
     *            传入的Url
     * @return 返回排序后的可直接使用接口
     */
    public DomainInfo[] getDomainServerIp(String url) {
        String host = Tools.getHostName(url);
        if (isEnable) {
            //对于不支持的域名，不再取localdns并缓存。因为这样会浪费资源
            if (!isSupport(host)) {
                DomainInfo[] info = new DomainInfo[1];
                info[0] = new DomainInfo("", url, host);
                return info;
            }
            //只为测试情况下使用
            if (!TextUtils.isEmpty(host) && Tools.isIPV4(host)) {
                DomainInfo[] info = new DomainInfo[1];
                info[0] = new DomainInfo("", url, "");
                return info;
            }
            // 查询domain对应的server ip数组
            final DomainModel domainModel = queryManager.queryDomainIp(String.valueOf(NetworkManager.getInstance().getSPID()), host);

            dnsCacheOverdueMgr.schedule(host);
            // 如果本地cache 和 内置数据都没有 返回null，然后马上查询数据
            if (null == domainModel || domainModel.id == -1) {
                this.checkUpdates(host, true);
                if (null == domainModel) {
                    return null;
                }
            }
            
            HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_DOMAIN,domainModel.tojson(), true);

            ArrayList<IpModel> result = filterInvalidIp(domainModel.ipModelArr);
            String[] scoreIpArray = scoreManager.ListToArr(result);

            if (scoreIpArray == null || scoreIpArray.length == 0) {
                return null; // 排序错误 终端后续流程
            }

            // 转换成需要返回的数据模型
            DomainInfo[] domainInfoList = DomainInfo.DomainInfoFactory(scoreIpArray, url, host);

            return domainInfoList;
        } else {
            return null;
        }
    }
    /**
     * 过滤无效ip数据
     * @param ipModelArr
     * @return
     */
    private ArrayList<IpModel> filterInvalidIp(ArrayList<IpModel> ipModelArr) {
        ArrayList<IpModel> result = new ArrayList<IpModel>();
        for (IpModel ipModel : ipModelArr) {
            if (!("" + SpeedtestManager.MAX_OVERTIME_RTT).equals(ipModel.rtt)) {
                result.add(ipModel);
            }
        }
        return result;
    }

    private boolean isSupport(String host) {
        if (null == host) {
            return false;
        }
        ArrayList<String> list = DNSCacheConfig.domainSupportList;
        if (null != list && 0 != list.size()) {
            boolean find = false;
            for (int i = 0; i < list.size(); i++) {
                String domain = list.get(i);
                if (host.endsWith(domain)) {
                    find = true;
                    break;
                }
            }
            return find;
        } else {
            return true;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    private ConcurrentHashMap<String, UpdateTask> mRunningTasks = new ConcurrentHashMap<String, UpdateTask>();

    /**
     * 从httpdns 服务器重新拉取数据
     * 
     * @param domainModel
     */
    private void checkUpdates(String domain, boolean speedTest) {
        if (isSupport(domain)) {
            final String host = domain;
            final boolean needSpeedTest = speedTest;
            UpdateTask task = mRunningTasks.get(host);
            if (null == task) {
                UpdateTask updateTask = new UpdateTask(new Runnable() {
                    @Override
                    public void run() {
                        Thread.currentThread().setName("Get Http Dns Data");
                        getHttpDnsData(host);
                        mRunningTasks.remove(host);
                        if (needSpeedTest) {
                            RealTimeThreadPool.getInstance().execute(new SpeedTestTask());
                        }
                    }
                });
                mRunningTasks.put(host, updateTask);
                updateTask.start();
            } else {
                long beginTime = task.getBeginTime();
                long now = System.currentTimeMillis();
                // 上次拉取超时，这次再开一个线程继续
                if (now - beginTime > 30 * 1000) {
                    task.start();
                }
            }
        }
    }

    static class UpdateTask {

        public Runnable runnable;

        public long beginTime;

        public UpdateTask(Runnable runnable) {
            super();
            this.runnable = runnable;
            this.beginTime = System.currentTimeMillis();
        }

        public void start() {
            Thread thread = new Thread(runnable);
            thread.start();
        }

        public long getBeginTime() {
            return beginTime;
        }
    }

    /**
     * 根据 host 更新数据
     * 
     * @param host
     */
    private final DomainModel getHttpDnsData(String host) {

        // 获取 httpdns 数据
        HttpDnsPack httpDnsPack = dnsManager.requestDns(host);

        if (httpDnsPack == null) {
            return null; // 没有从htppdns服务器获取正确的数据。必须中断下面流程
        }

        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_DOMAIN, httpDnsPack.toJson(),
                true);
        // 插入本地 cache
        DomainModel domainModel = dnsCacheManager.insertDnsCache(httpDnsPack);

        return domainModel;
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    /**
     * 定时器休眠时间
     */
    public final int sleepTime = timer_interval;

    /**
     * 启动定时器
     */
    private void startTimer() {
        timer = new Timer();
        timer.schedule(task, 0, sleepTime);
    }

    /**
     * 定时器Obj
     */
    private Timer timer = null;

    /**
     * TimerTask 运行时间
     */
    public long TimerTaskOldRunTime = 0;
    /**
     * 上次测速时间
     */
    private long lastSpeedTime;
    /**
     * 上次日志上传时间
     */
    private long lastLogTime;

    /**
     * 定时器还多久启动
     */
    public long getTimerDelayedStartTime() {
        return (sleepTime - (System.currentTimeMillis() - TimerTaskOldRunTime)) / 1000;
    }

    /**
     * 定时器任务
     */
    private TimerTask task = new TimerTask() {

        @Override
        public void run() {
            //FOR TEST !!!
            //RealTimeThreadPool.getInstance().execute(new LogUpLoadTask());
            TimerTaskOldRunTime = System.currentTimeMillis();
            //删除20分钟内无操作的数据
            ArrayList<DomainModel> allMemoryCache = dnsCacheManager.getAllMemoryCache();
            for (DomainModel domainModel : allMemoryCache) {
                if (null != allMemoryCache) {
                    String domain = domainModel.domain;
                    if (dnsCacheOverdueMgr.isOverdue(domain)) {
                        dnsCacheManager.removeMemoryCache(domain);
                    }
                }
            }
            //无网络情况下不执行任何后台任务操作
            if (NetworkManager.Util.getNetworkType() == Constants.NETWORK_TYPE_UNCONNECTED || NetworkManager.Util.getNetworkType() == Constants.MOBILE_UNKNOWN) {
                return;
            }
            /************************* 更新过期数据 ********************************/
            Thread.currentThread().setName("HTTP DNS TimerTask");
            final ArrayList<DomainModel> list = dnsCacheManager.getExpireDnsCache();
            for (DomainModel model : list) {
                checkUpdates(model.domain, false);
            }

            long now = System.currentTimeMillis();
            /************************* 测速逻辑 ********************************/
            if (now - lastSpeedTime > SpeedtestManager.time_interval - 3) {
                lastSpeedTime = now;
                RealTimeThreadPool.getInstance().execute(new SpeedTestTask());
            }

            /************************* 日志上报相关 ********************************/
            now = System.currentTimeMillis();
            if (now - lastLogTime > HttpDnsLogManager.time_interval) {
                lastLogTime = now;
                // 判断当前是wifi网络才能上传
                if (NetworkManager.Util.getNetworkType() == Constants.NETWORK_TYPE_WIFI) {
                    RealTimeThreadPool.getInstance().execute(new LogUpLoadTask());
                }
            }
        }
    };

    class SpeedTestTask implements Runnable {

        public void run() {
            try {
                ArrayList<DomainModel> list = dnsCacheManager.getAllMemoryCache();
                updateSpeedInfo(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateSpeedInfo(List<DomainModel> list) {
            //过滤漏斗
            if (null != list && list.size() > SpeedtestManager.MAX_CAPACITY) {
                Collections.sort(list, new Comparator<DomainModel>() {

                    @Override
                    public int compare(DomainModel o1, DomainModel o2) {
                        long l = 0;
                        long r = 0;
                        try {
                            l = Long.valueOf(o1.time);
                            r = Long.valueOf(o2.time);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        return (int) (r - l);
                    }
                });
                list = list.subList(0, SpeedtestManager.MAX_CAPACITY);
            }
            for (DomainModel domainModel : list) {
                if (null != domainModel) {
                    ArrayList<IpModel> ipArray = domainModel.ipModelArr;
                    if (ipArray == null || ipArray.size() < 1) {
                        continue;
                    }
                    for (IpModel ipModel : ipArray) {
                        int rtt = speedtestManager.speedTest(ipModel.ip, domainModel.domain);
                        boolean succ = rtt > SpeedtestManager.OCUR_ERROR;
                        if (succ) {
                            ipModel.rtt = String.valueOf(rtt);
                            ipModel.success_num = String.valueOf((Integer.valueOf(ipModel.success_num) + 1));
                            ipModel.finally_success_time = String.valueOf(System.currentTimeMillis());
                        } else {
                            ipModel.rtt = String.valueOf(SpeedtestManager.MAX_OVERTIME_RTT);
                            ipModel.err_num = String.valueOf((Integer.valueOf(ipModel.err_num) + 1));
                            ipModel.finally_fail_time = String.valueOf(System.currentTimeMillis());
                        }
                    }
                    scoreManager.serverIpScore(domainModel);
                    dnsCacheManager.setSpeedInfo(ipArray);
                }
            }
        }
    }

    class LogUpLoadTask implements Runnable {

        public void run() {
            if (HttpDnsLogManager.ENABLE_UPLOAD_LOG) {
                synchronized (HttpDnsLogManager.lock) {
                    File logFile = HttpDnsLogManager.getInstance().getLogFile();
                    if (logFile != null && logFile.exists() && logFile.length() > 0) {
                        // upload
                        try {
                            FileReader in = new FileReader(logFile);
                            BufferedReader bufferedReader = new BufferedReader(in);
                            StringBuilder sb = new StringBuilder();
                            String line = null;
                            String prefix = "[";
                            String suffix = "]";
                            sb.append(prefix);
                            while ((line = bufferedReader.readLine()) != null) {
                                line = line + ",";
                                sb.append(line);
                            }
                            bufferedReader.close();
                            if (sb.length() > 0) {
                                sb = sb.deleteCharAt(sb.length() - 1);
                            }
                            sb.append(suffix);
                            HashMap<String, String> getParams = new HashMap<String, String>();
                            getParams.putAll(AuthCenter.getAuthParmas());
                            HashMap<String, String> postParams = new HashMap<String, String>();
                            postParams.put("log", sb.toString());
                            String logServerApi = HttpDnsLogManager.LOG_SERVER_API;
                            HttpResult httpResult = networkRequests.request(logServerApi, null, getParams, postParams, RequestType.POST);
                            boolean succ = AuthValidator.check(httpResult.responseStr);
                            if (succ) {
                                HttpDnsLogManager.getInstance().deleteLogFile();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络环境发生变化 刷新缓存数据 暂时先不需要 预处理逻辑。 用户直接请求的时候会更新数据。 （会有一次走本地dns ，
     * 后期优化这个方法，主动请求缓存的数据）
     * 
     * @param networkInfo
     */
    public void onNetworkStatusChanged(NetworkInfo networkInfo) {
        if (null != dnsCacheManager) {
            dnsCacheManager.clearMemoryCache();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    /**
     * 获取 ip对应的域名，如果获取失败，返回原值。
     * @param ip
     * @return
     */
    public String getDomainByIp(String ip) {
        String host = ip;
        if (Tools.isIPV4(ip)) {
            ArrayList<DomainModel> allMemoryCache = dnsCacheManager.getAllMemoryCache();
            if (null != allMemoryCache) {
                for (DomainModel domainModel : allMemoryCache) {
                    if (null != domainModel) {
                        ArrayList<IpModel> ipModels = domainModel.ipModelArr;
                        if (null != ipModels) {
                            for (IpModel ipModel : ipModels) {
                                if (null != ipModel && null != ipModel.ip && ipModel.ip.equals(ip)) {
                                    host = domainModel.domain;
                                    return host;
                                }
                            }
                        }
                    }
                }
            }
        }
        return host;
    }

}
