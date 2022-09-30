package org.optaplanner.allocatorbalancing.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.common.domain.AbstractPersistable;
import org.optaplanner.persistence.xstream.api.score.buildin.hardmediumsoft.HardMediumSoftScoreXStreamConverter;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@PlanningSolution
@XStreamAlias("CloudBalance")
public class AllocatorBalance extends AbstractPersistable {

    private List<Cluster> clusterList;

    private List<Server> serverList;

    @XStreamConverter(HardMediumSoftScoreXStreamConverter.class)
    private HardSoftScore score;

    public AllocatorBalance() {
    }

    public AllocatorBalance(long id, List<Cluster> clusterList, List<Server> serverList) {
        super(id);
        this.clusterList = clusterList;
        this.serverList = serverList;
    }

    @ValueRangeProvider(id = "computerRange")
    @ProblemFactCollectionProperty
    public List<Cluster> getClusterList() {
        return clusterList;
    }

    public void setClusterList(List<Cluster> clusterList) {
        this.clusterList = clusterList;
    }

    @PlanningEntityCollectionProperty
    public List<Server> getServerList() {
        return serverList;
    }

    public void setServerList(List<Server> serverList) {
        this.serverList = serverList;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
