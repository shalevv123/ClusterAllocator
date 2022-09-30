package org.optaplanner.allocatorbalancing.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.common.domain.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@PlanningEntity
@XStreamAlias("CloudProcess")
public class Server extends AbstractPersistable {

    private int cpuCores; // in gigahertz
    private int memory; // in gigabyte RAM
    private int networkBandwidth; // in gigabyte per hour
    private Cluster originalCluster;
    // Planning variables: changes during planning, between score calculations.
    private Cluster cluster;

    public Server() {
    }
    public Server(long id, int cpuCores, int memory, int NetworkBandwidth) {
        super(id);
        this.cpuCores = cpuCores;
        this.memory = memory;
        this.networkBandwidth = NetworkBandwidth;
        this.originalCluster = null;
    }

    public Server(long id, int cpuCores, int memory, int NetworkBandwidth,
                  Cluster originalCluster) {
        super(id);
        this.cpuCores = cpuCores;
        this.memory = memory;
        this.networkBandwidth = NetworkBandwidth;
        this.originalCluster = originalCluster;
        this.cluster = originalCluster;
    }


    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getNetworkBandwidth() {
        return networkBandwidth;
    }

    public void setNetworkBandwidth(int networkBandwidth) {
        this.networkBandwidth = networkBandwidth;
    }

    public Cluster getOriginalCluster() { return originalCluster; }

    public void setOriginalCluster(Cluster originalCluster) {
        this.originalCluster = originalCluster;
    }

    @PlanningVariable(valueRangeProviderRefs = {"computerRange"}, nullable = true)
    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public boolean isMoved() {
        return  originalCluster != null && originalCluster != cluster;
    }

    public String getLabel() {
        return "Process " + id;
    }


}
