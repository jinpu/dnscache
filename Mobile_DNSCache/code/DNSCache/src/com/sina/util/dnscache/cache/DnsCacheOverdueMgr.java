package com.sina.util.dnscache.cache;

import java.util.HashMap;
import java.util.Map;

public class DnsCacheOverdueMgr<K> {
    private final Map<K, Long> ready2Remove = new HashMap<K, Long>();
    /** 最长可存活的时间 ，默认设置为20分钟*/
    private static final long MAX_ALIVE = 20 * 60 * 1000;

    /**
     * 更新最后调用时间，用于过期剔除{@link DnsCacheOverdueMgr#isOverdue()}
     * 
     * @param domain
     */
    public void schedule(K domain) {
        ready2Remove.put(domain, System.currentTimeMillis());
    }

    /**
     * 该域名是否已经过期
     * 
     * @param domain
     * @return
     */
    public boolean isOverdue(Object domain) {
        long now = System.currentTimeMillis();
        Long updateTime = ready2Remove.get(domain);
        boolean shouldRemove = false;
        if (null != updateTime) {
            shouldRemove = (now - updateTime) > MAX_ALIVE;
        }
        return shouldRemove;
    }
}
