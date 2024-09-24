/*-
 * #%L
 * OBKV Table Client Framework
 * %%
 * Copyright (C) 2021 OceanBase
 * %%
 * OBKV Table Client Framework is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * #L%
 */

package com.alipay.oceanbase.rpc.location.model.partition;

import com.alipay.oceanbase.rpc.location.model.ObServerLdcLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ObPartitionEntry {
    private Map<Long, ObPartitionLocation> partitionLocation = new HashMap<Long, ObPartitionLocation>();

    // mapping from tablet id to ls id, and the part id to tablet id mapping is in ObPartitionInfo
    private Map<Long, Long> tabletLsIdMap = new HashMap<>();
    
    // tabelt id -> (PartitionLocation, LsId)
    // tablet id 作为索引管理PartitionInfo 其中包含了 PartitionLocation 和LSID
    // 外部会通过tablet id并发的读写ObPartitionLocationInfo
    // 写的场景就是更新，读的场景是正常的请求执行，需要保证读写的安全性，更新的时候一方面是保证线程安全，另一方面还需要保证不能频繁更新
    private ConcurrentHashMap<Long, ObPartitionLocationInfo> partitionInfos = new ConcurrentHashMap<>();

    
    public ObPartitionLocationInfo getPartitionInfo(long tabletId) {
        if (!partitionInfos.containsKey(tabletId)) {
            ObPartitionLocationInfo partitionInfo = new ObPartitionLocationInfo();
            partitionInfos.put(tabletId, partitionInfo);
        }
        return partitionInfos.get(tabletId);
    }
    
    public Map<Long, ObPartitionLocation> getPartitionLocation() {
        return partitionLocation;
    }

    /*
     * Set partition location.
     */
    public void setPartitionLocation(Map<Long, ObPartitionLocation> partitionLocation) {
        this.partitionLocation = partitionLocation;
    }

    public Map<Long, Long> getTabletLsIdMap() {
        return tabletLsIdMap;
    }

    public void setTabletLsIdMap(Map<Long, Long> tabletLsIdMap) {
        this.tabletLsIdMap = tabletLsIdMap;
    }

    public long getLsId(long tabletId) { return tabletLsIdMap.get(tabletId); }
    
    /*
     * Get partition location with part id.
     */
    public ObPartitionLocation getPartitionLocationWithPartId(long partId) {
        return partitionLocation.get(partId);
    }

    /*
     * Put partition location with part id.
     */
    public ObPartitionLocation putPartitionLocationWithPartId(long partId,
                                                              ObPartitionLocation ObpartitionLocation) {
        return partitionLocation.put(partId, ObpartitionLocation);
    }

    /*
     * Get partition location with tablet id.
     */
    public ObPartitionLocation getPartitionLocationWithTabletId(long tabletId) {
        return partitionLocation.get(tabletId);
    }

    /*
     * Put partition location with part id.
     */
    public ObPartitionLocation putPartitionLocationWithTabletId(long tabletId,
                                                                ObPartitionLocation ObpartitionLocation) {
        return partitionLocation.put(tabletId, ObpartitionLocation);
    }

    /*
     * Prepare for weak read.
     * @param ldcLocation
     */
    public void prepareForWeakRead(ObServerLdcLocation ldcLocation) {
        for (Map.Entry<Long, ObPartitionLocation> entry : partitionLocation.entrySet()) {
            entry.getValue().prepareForWeakRead(ldcLocation);
        }
    }

    /*
     * To string.
     */
    @Override
    public String toString() {
        return "ObPartitionEntry{" + "partitionLocation=" + partitionLocation + '}';
    }
}
