package com.sina.util.dnscache.dnsp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.sina.util.dnscache.DNSCacheConfig;
import com.sina.util.dnscache.Tools;
import com.sina.util.dnscache.dnsp.impl.HttpPodDns;
import com.sina.util.dnscache.dnsp.impl.LocalDns;
import com.sina.util.dnscache.dnsp.impl.SinaHttpDns;
import com.sina.util.dnscache.dnsp.impl.UdpDns;
import com.sina.util.dnscache.log.HttpDnsLogManager;
import com.sina.util.dnscache.model.HttpDnsPack;
import com.sina.util.dnscache.net.networktype.Constants;
import com.sina.util.dnscache.net.networktype.NetworkManager;

public class DnsManager implements IDns {

    ArrayList<IDnsProvider> mDnsProviders = new ArrayList<IDnsProvider>();
    private ArrayList<String> debugInfo = new ArrayList<String>();

    public DnsManager() {
        mDnsProviders.add(new SinaHttpDns());
        mDnsProviders.add(new HttpPodDns());
        mDnsProviders.add(new UdpDns());
        mDnsProviders.add(new LocalDns());
    }

    @Override
    public HttpDnsPack requestDns(String domain) {
        Collections.sort(mDnsProviders, new Comparator<IDnsProvider>() {
            @Override
            public int compare(IDnsProvider lhs, IDnsProvider rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                } else {
                    // 按照降序排序
                    return rhs.getPriority() - lhs.getPriority();
                }
            }
        });
        for (IDnsProvider dp : mDnsProviders) {
            Tools.log("TAG", "访问" + dp.getClass().getSimpleName() + "接口开始," + "\n优先级是：" + dp.getPriority() + "\n该模块是否开启：" + dp.isActivate()
                    + "\n该模块的API地址是：" + dp.getServerApi());
            if (dp.isActivate()) {
                long start = System.currentTimeMillis();
                HttpDnsPack dnsPack = dp.requestDns(domain);
                long end = System.currentTimeMillis();
                Tools.log("TAG", "访问" + dp.getClass().getSimpleName() + "接口结束,"+ "请求域名信息：" + domain +",消耗时间：" + (end - start) + "ms。" + "\n返回的结果是：" + dnsPack);
                if (null != dnsPack) {
                    if (DNSCacheConfig.DEBUG) {
                        if (null != debugInfo) {
                            debugInfo.add(dnsPack.rawResult + "[from:" + dp.getClass().getSimpleName() + "]");
                        }
                    }
                    
                    dnsPack.localhostSp = NetworkManager.getInstance().getSPID();
                    //非wifi网络环境下，如果本地sp与server返回sp不一致，则记log上传
                    if (!dnsPack.device_sp.equals(dnsPack.localhostSp)
                            && NetworkManager.Util.getNetworkType() != Constants.NETWORK_TYPE_WIFI) {
                        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_ERR_SPINFO,
                                dnsPack.toJson());
                    }
                    
                    return dnsPack;
                }
            }
        }

        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_ERR_DOMAININFO, "{\"domain\":" + "\"" + domain + "\"}" );
        
        return null;
    }

    @Override
    public ArrayList<String> getDebugInfo() {
        return debugInfo;
    }

    @Override
    public void initDebugInfo() {
        debugInfo = new ArrayList<String>();
    }
}
