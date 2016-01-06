package com.sina.util.dnscache;

/**
 *
 * 项目名称: DNSCache <br>
 * 类名称: DomainInfo <br>
 * 类描述: 为使用者封装的数据模型 <br>
 * 创建人: fenglei <br>
 * 创建时间: 2015-4-21 下午5:26:04 <br>
 * 
 * 修改人: <br>
 * 修改时间: <br>
 * 修改备注: <br>
 * 
 * @version V1.0
 */
public class DomainInfo {

    /**
     * 工场方法
     *
     * @param ip
     * @param url
     * @param host
     * @return
     */
    public static DomainInfo DomainInfoFactory(String ip, String url, String host) {
        url = Tools.getIpUrl(url, host, ip);
        return new DomainInfo(ip, url, host);
    }

    /**
     * 工场方法
     * 
     * @param serverIpArray
     * @param url
     * @param host
     * @return
     */
    public static DomainInfo[] DomainInfoFactory(String[] serverIpArray, String url, String host) {
        DomainInfo[] domainArr = new DomainInfo[serverIpArray.length];
        for (int i = 0; i < serverIpArray.length; i++) {
            domainArr[i] = DomainInfoFactory(serverIpArray[i], url, host);
        }
        return domainArr;
    }

    /**
     * 构造函数
     * 
     * @param id
     * @param url
     * @param host
     */
    public DomainInfo(String ip, String url, String host) {
        this.ip = ip;
        this.url = url;
        this.host = host;
    }

    /**
     * 返回给业务方的最优IP
     */
    public String ip = null;

    /**
     * 可以直接使用的url 已经替换了host为ip
     */
    public String url = null;

    /**
     * 需要设置到 http head 里面的主机头
     */
    public String host = "";

    /**
     * 返回 url 信息
     * 
     * @return
     */
    public String toString() {
        String str = "DomainInfo: \n";
        str += "ip = " + ip + "\n";
        str += "url = " + url + "\n";
        str += "host = " + host + "\n";
        return str;
    }

}
