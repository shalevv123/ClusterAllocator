package org.optaplanner.allocatorbalancing.score;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sum;
import static org.optaplanner.core.api.score.stream.Joiners.equal;

import java.util.function.Function;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.allocatorbalancing.domain.Cluster;
import org.optaplanner.allocatorbalancing.domain.Server;

public class AllocatorBalancingConstraintProvider implements ConstraintProvider {
    //assuming CPU:RAM ratio is 4:1
    private static int cpuWeight = 4;
    private static int memoryWeight = 1;
    private static int networkBandwidthWeight = 0;

    private static double migrationPenalty = 4* (double)cpuWeight;      //4 CPU weight


    public static double getMigrationPenalty() {
        return migrationPenalty;
    }

    public static int getCpuWeight() {
        return cpuWeight;
    }

    public static int getMemoryWeight() {
        return memoryWeight;
    }

    public static int getNetworkBandwidth_weight() {
        return networkBandwidthWeight;
    }

    public static void setMigration_penalty(double migration_penalty) {
        AllocatorBalancingConstraintProvider.migrationPenalty = migration_penalty;
    }

    public static void setCpu_weight(int cpu_weight) {
        AllocatorBalancingConstraintProvider.cpuWeight = cpu_weight;
    }

    public static void setRam_weight(int ram_weight) {
        AllocatorBalancingConstraintProvider.memoryWeight = ram_weight;
    }

    public static void setNetworkBandwidth_weight(int networkBandwidth_weight) {
        AllocatorBalancingConstraintProvider.networkBandwidthWeight = networkBandwidth_weight;
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                unfilledCpuCoresTotal(constraintFactory),
                unfilledMemoryTotal(constraintFactory),
                //unfilledNetworkBandwidthTotal(constraintFactory),
                emptyClustersTotal(constraintFactory),
                migrationTotal(constraintFactory),
                overfillCpuCoresTotal(constraintFactory),
                overfillMemoryTotal(constraintFactory),
                overfillNetworkBandwidthTotal(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    Constraint unfilledCpuCoresTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Server.class)
                .groupBy(Server::getCluster, sum(Server::getCpuCores))
                .filter((cluster, cpuCores) -> cpuCores < cluster.getRequiredCpuCores())
                .penalize("unfilledCpuPowerTotal",
                        HardSoftScore.ONE_HARD,
                        (cluster, cpuCores) -> cluster.getRequiredCpuCores() - cpuCores);
    }

    Constraint unfilledMemoryTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Server.class)
                .groupBy(Server::getCluster, sum(Server::getMemory))
                .filter((cluster, memory) -> memory < cluster.getRequiredMemory())
                .penalize("unfilledMemoryTotal",
                        HardSoftScore.ONE_HARD,
                        (cluster, memory) -> cluster.getRequiredMemory() - memory);
    }

    Constraint unfilledNetworkBandwidthTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Server.class)
                .groupBy(Server::getCluster, sum(Server::getNetworkBandwidth))
                .filter((cluster, networkBandwidth) -> networkBandwidth < cluster.getRequiredNetworkBandwidth())
                .penalize("unfilledNetworkBandwidthTotal",
                        HardSoftScore.ONE_HARD,
                        (cluster, networkBandwidth) -> cluster.getRequiredNetworkBandwidth() - networkBandwidth);
    }

    Constraint emptyClustersTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Cluster.class)
                .ifNotExists(Server.class, equal(Function.identity(), Server::getCluster))
                .penalize("unusedComputers",
                        HardSoftScore.ONE_HARD,
                        cluster -> cluster.getRequiredCpuCores() + cluster.getRequiredMemory() + cluster.getRequiredNetworkBandwidth());
    }
    // ************************************************************************
    // Migration constraints
    // ************************************************************************

    Constraint migrationTotal(ConstraintFactory constraintFactory){
        return constraintFactory.forEachIncludingNullVars(Server.class)
                .filter(Server::isMoved)
                .penalize("movedComputer",
                        HardSoftScore.ONE_SOFT, x -> (int)migrationPenalty);
    }
    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint overfillCpuCoresTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Server.class)
                .groupBy(Server::getCluster, sum(Server::getCpuCores))
                .filter((cluster, cpuCores) -> cpuCores > cluster.getRequiredCpuCores())
                .penalize("requiredCpuPowerTotal",
                        HardSoftScore.ONE_SOFT,
                        (cluster, cpuCores) -> (cpuCores - cluster.getRequiredCpuCores())*cpuWeight);
    }

    Constraint overfillMemoryTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Server.class)
                .groupBy(Server::getCluster, sum(Server::getMemory))
                .filter((cluster, memory) -> memory > cluster.getRequiredMemory())
                .penalize("requiredMemoryTotal",
                        HardSoftScore.ONE_SOFT,
                        (cluster, memory) -> (memory - cluster.getRequiredMemory())* memoryWeight);
    }

    Constraint overfillNetworkBandwidthTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Server.class)
                .groupBy(Server::getCluster, sum(Server::getNetworkBandwidth))
                .filter((cluster, networkBandwidth) -> networkBandwidth > cluster.getRequiredNetworkBandwidth())
                .penalize("requiredNetworkBandwidthTotal",
                        HardSoftScore.ONE_SOFT,
                        (cluster, networkBandwidth) ->
                                (networkBandwidth - cluster.getRequiredNetworkBandwidth())*networkBandwidthWeight);
    }

}
