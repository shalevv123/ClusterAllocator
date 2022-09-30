package org.optaplanner.allocatorbalancing.domain;

import org.optaplanner.common.domain.AbstractPersistable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("CloudComputer")
public class Cluster extends AbstractPersistable implements  Comparable<Cluster> {

    private int requiredCpuCores; // in gigahertz
    private int requiredMemory; // in gigabyte RAM
    private int requiredNetworkBandwidth; // in gigabyte per hour

    public Cluster(long id, int requiredCpuCores, int requiredMemory, int requiredNetworkBandwidth) {
        super(id);
        this.requiredCpuCores = requiredCpuCores;
        this.requiredMemory = requiredMemory;
        this.requiredNetworkBandwidth = requiredNetworkBandwidth;
    }

    public int getRequiredCpuCores() {
        return requiredCpuCores;
    }

    public void setRequiredCpuCores(int requiredCpuCores) {
        this.requiredCpuCores = requiredCpuCores;
    }

    public int getRequiredMemory() {
        return requiredMemory;
    }

    public void setRequiredMemory(int requiredMemory) {
        this.requiredMemory = requiredMemory;
    }

    public int getRequiredNetworkBandwidth() {
        return requiredNetworkBandwidth;
    }

    public void setRequiredNetworkBandwidth(int requiredNetworkBandwidth) {
        this.requiredNetworkBandwidth = requiredNetworkBandwidth;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public String getLabel() {
        if (id < 0)
            return "Unallocated";

        return "Computer " + id;
    }

    @Override
    public int compareTo(Cluster cluster){
        return Long.compare(id, cluster.getId());
    }

}
