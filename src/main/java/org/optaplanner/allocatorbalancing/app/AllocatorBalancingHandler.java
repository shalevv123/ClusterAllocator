package org.optaplanner.allocatorbalancing.app;

import java.time.Duration;
import java.util.*;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.xy.XYDataItem;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.allocatorbalancing.domain.AllocatorBalance;
import org.optaplanner.allocatorbalancing.domain.Cluster;
import org.optaplanner.allocatorbalancing.domain.Server;
import org.optaplanner.allocatorbalancing.score.AllocatorBalancingConstraintProvider;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.ChartUtilities;

public class AllocatorBalancingHandler {

    //this variable holds the softscore and time of all the problems that ran with "logResults" set to true.
    private static XYSeries runTimeData = new XYSeries("Runtime Data");

    public static XYSeries getRunTimeData() {
        return runTimeData;
    }

    public static void setRunTimeData(XYSeries runTimeData) {
        AllocatorBalancingHandler.runTimeData = runTimeData;
    }

    public static void addRunTimeData(int time, double score){
        runTimeData.add(time, score);
    }

    public static void addRunTimeData(XYSeries series){
        List<XYDataItem> items = new ArrayList<>(series.getItems());
        for (XYDataItem item : items){
            runTimeData.add(item);
        }
    }

    public static void clearRunTimeData(){
        runTimeData.clear();
    }


    public static AllocatorBalance Solve(AllocatorBalance unsolvedAllocatorBalance, Integer runTime, Boolean detailedPrint, Boolean saveResult,
                              String resultFileName, Boolean logResults)
            throws IOException, SAXException, ParserConfigurationException {
        /*
        :param unsolvedAllocatorBalance: An allocation state to be solved.
        :param runTime: Runtime cap for the solver.
        :param detailedPrint: True if you want a detailed print of the result.
        :param saveResult: True if you want to save the results in an xml file.
        :param resultFileName: if "saveResult" is True this is the name of the file.
        :param logResults: True if you want to log the results into the runTimeData.

        :return: A solved allocation state.
         */
        // Build the Solver
        double cpu_median = getServerMedian(unsolvedAllocatorBalance);

        //setting the migration penalty. In development stages it was decided to be the max between:
        //wasting 4 cpu cores or wasting half the median of the CPU cores of each server.
        AllocatorBalancingConstraintProvider.setMigration_penalty(Math.max(
                4* AllocatorBalancingConstraintProvider.getCpuWeight(),
                cpu_median* AllocatorBalancingConstraintProvider.getCpuWeight()/2));

        SolverFactory<AllocatorBalance> solverFactory = SolverFactory.create(new SolverConfig()
                    .withSolutionClass(AllocatorBalance.class)
                    .withEntityClasses(Server.class)
                    .withConstraintProviderClass(AllocatorBalancingConstraintProvider.class)
                    .withTerminationSpentLimit(Duration.ofSeconds(runTime)));

        Solver<AllocatorBalance> solver = solverFactory.buildSolver();
        AllocatorBalance solvedAllocatorBalance = solver.solve(unsolvedAllocatorBalance);


        // Display the result //
        System.out.println(
                        "\nSolution ran for " + runTime + " seconds\n"
                        + "Score is: "+ solvedAllocatorBalance.getScore() + "\n");
        if (detailedPrint) {
            System.out.println(
                    "\nDetails: " + toDisplayString(solvedAllocatorBalance) + "\n");
        }
        if (saveResult) {
            saveSolution(solvedAllocatorBalance, resultFileName);
        }
        if(solvedAllocatorBalance.getScore().getHardScore() == 0) {
            if (logResults) {
                runTimeData.add(runTime, Double.valueOf(-solvedAllocatorBalance.getScore().getSoftScore()));
            }
        }
        else{
            System.out.println("\n Logging this solution isn't valid.\n" +
                                "Either Run it for a longer time, or a valid solution might not exist\n");
        }
        return solvedAllocatorBalance;
    }


    // finds the median of the CPU cores among all servers.
    private static double getServerMedian(AllocatorBalance balance){
        List<Server> ServerList = balance.getServerList();
        List<Integer> cpuList = ServerList.stream().map(Server::getCpuCores).sorted().collect(Collectors.toList());
        if (cpuList.size() % 2 == 0)
            return ((double)cpuList.get(cpuList.size()/2) + (double)cpuList.get(cpuList.size()/2 - 1))/2;
        else
            return (double) cpuList.get(cpuList.size()/2);
    }


    private static List<Cluster> getClusterlist(NodeList rawClusterList){
        List<Cluster> clusterList = new ArrayList<>();
        for (int i = 0; i < rawClusterList.getLength(); i++){
            Node cluster = rawClusterList.item(i);
            if (cluster.getNodeType() == Node.ELEMENT_NODE) {
                Element eCluster = (Element) cluster;
                clusterList.add(new Cluster(Integer.parseInt(eCluster.getElementsByTagName("id").item(0).getTextContent()),
                        Integer.parseInt(eCluster.getElementsByTagName("requiredCpuCores").item(0).getTextContent()),
                        Integer.parseInt(eCluster.getElementsByTagName("requiredMemory").item(0).getTextContent()),
                        Integer.parseInt(eCluster.getElementsByTagName("requiredNetworkBandwidth").item(0).getTextContent())
                ));
            }
        }
        return clusterList;
    }


    private static List<Server> getServerList(NodeList rawServerList, List<Cluster> clusterList){
        List<Server> ServerList = new ArrayList<>();
        for (int i = 0; i < rawServerList.getLength(); i++){
            Node server = rawServerList.item(i);
            if (server.getNodeType() == Node.ELEMENT_NODE) {
                Element eServer = (Element) server;
                Cluster originalCluster = null;
                try{
                    int clusterId = Integer.parseInt(eServer.getElementsByTagName("originalCluster").item(0).getTextContent());
                    for (Cluster cluster : clusterList) {
                        if (clusterId == cluster.getId()) {
                            originalCluster = cluster;
                        }
                    }
                } catch (Exception ignored){

                }
                ServerList.add(new Server(Integer.parseInt(eServer.getElementsByTagName("id").item(0).getTextContent()),
                        Integer.parseInt(eServer.getElementsByTagName("cpuCores").item(0).getTextContent()),
                        Integer.parseInt(eServer.getElementsByTagName("memory").item(0).getTextContent()),
                        Integer.parseInt(eServer.getElementsByTagName("networkBandwidth").item(0).getTextContent()),
                        originalCluster));
            }
        }
        return ServerList;
    }


    public static AllocatorBalance getBalance(String path) throws ParserConfigurationException, IOException, SAXException {
        /*
        :param path : A path to an xml file in the require format.

        :return: An allocation state translation of the xml file.
         */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File file = new File(path);
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        List<Cluster> clusterList = getClusterlist(document.getElementsByTagName("Cluster"));
        List<Server> serverList =  getServerList(document.getElementsByTagName("Server"), clusterList);
        return new AllocatorBalance(0, clusterList, serverList);
    }


    //displays the allocation state in a readable way.
    public static String toDisplayString(AllocatorBalance allocatorBalance) {
        StringBuilder displayString = new StringBuilder();
        Map<Cluster,List<Server>> balanceList = new TreeMap<>();
        Cluster nullCluster = new Cluster(-1, 0, 0, 0);
        balanceList.put(nullCluster, new ArrayList<>());
        for (Server server : allocatorBalance.getServerList()) {
            Cluster cluster = server.getCluster();
            if (cluster == null){
                balanceList.get(nullCluster).add(server);
            }
            else {
                if (!balanceList.containsKey(cluster)) {
                    balanceList.put(cluster, new ArrayList<>());
                }
                balanceList.get(cluster).add((server));
            }
        }
        displayString.append("\n  ").append(nullCluster.getLabel()).append(" -> ");
        for (Server server : balanceList.get(nullCluster)){
            displayString.append(server.getLabel()).append("  ");
        }
        displayString.append("\n");
        for (Map.Entry<Cluster,List<Server>> entry: balanceList.entrySet()){
            if (entry.getKey() != nullCluster) {
                displayString.append("\n  ").append(entry.getKey().getLabel()).append(" -> ");
                int cpu = 0;
                int memory = 0;
                int network = 0;
                for (Server server : entry.getValue()) {
                    displayString.append(server.getLabel()).append("  ");
                    cpu += server.getCpuCores();
                    memory += server.getMemory();
                    network += server.getNetworkBandwidth();
                }
                displayString.append("\n\t");
                displayString.append("CPU usage: ").append(cpu).append("/").append(entry.getKey().getRequiredCpuCores()).append(" Cores ");
                displayString.append("\n\t");
                displayString.append("RAM usage: ").append(memory).append("/").append(entry.getKey().getRequiredMemory()).append(" GB");
                displayString.append("\n\t");
                displayString.append("Network usage: ").append(network).append("/").append(entry.getKey().getRequiredNetworkBandwidth()).append(" ");
                displayString.append("\n");
            }
        }
        return displayString.toString();
    }


    private static void writeComputerList(AllocatorBalance solution, FileWriter writer, AtomicInteger outerId) throws IOException {
        int innerId = 0;
        writer.write("\t<clusterList id=\""+outerId+"\">\n");
        outerId.set(outerId.get()+1);
        List<Cluster> computerList = solution.getClusterList();
        for (Cluster cluster : computerList){
            writer.write("\t\t<Cluster id=\""+outerId+"\">\n");
            outerId.set(outerId.get()+1);
            writer.write("\t\t\t<id>"+innerId+"</id>\n");
            innerId = innerId + 1;
            writer.write("\t\t\t<requiredCpuCores>"+cluster.getRequiredCpuCores()+"</requiredCpuCores>\n");
            writer.write("\t\t\t<requiredMemory>"+cluster.getRequiredMemory()+"</requiredMemory>\n");
            writer.write("\t\t\t<requiredNetworkBandwidth>"+cluster.getRequiredNetworkBandwidth()+"</requiredNetworkBandwidth>\n");
            writer.write("\t\t</Cluster>\n");
        }
        writer.write("\t</clusterList>\n");
    }


    private static void writeProcessList(AllocatorBalance solution, FileWriter writer, AtomicInteger outerId) throws IOException{
        int innerId = 0;
        writer.write("\t<serverList id=\""+outerId+"\">\n");
        outerId.set(outerId.get()+1);
        List<Server> serverList = solution.getServerList();
        for (Server server : serverList){
            writer.write("\t\t<Server id=\""+outerId+"\">\n");
            outerId.set(outerId.get()+1);
            writer.write("\t\t\t<id>"+innerId+"</id>\n");
            innerId = innerId + 1;
            writer.write("\t\t\t<cpuCores>"+server.getCpuCores()+"</cpuCores>\n");
            writer.write("\t\t\t<memory>"+server.getMemory()+"</memory>\n");
            writer.write("\t\t\t<networkBandwidth>"+server.getNetworkBandwidth()+"</networkBandwidth>\n");
            if (server.getCluster() != null)
                writer.write("\t\t\t<originalCluster>"+server.getCluster().getId()+"</originalCluster>\n");
            writer.write("\t\t</Server>\n");
        }

        writer.write("\t</serverList>\n");
    }


    public static void saveSolution(AllocatorBalance solution, String savePath){
        /*
        :param solution: An allocation state to save in an xml file.
        :param savePath: The name of the file that will be created, if an absolute path isn't specified it will be
        created in the cwd.

        :return: Saves the allocation state as an xml file in the savePath.
         */
        AtomicInteger outerId = new AtomicInteger(0);
        try{
            File file = new File(savePath);
            if(!file.createNewFile()){
                file.delete();
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(savePath);
            writer.write("<AllocatorBalance id=\""+outerId+"\">\n");
            outerId.set(outerId.get()+1);
            writer.write("\t<id>0</id>\n");
            writeComputerList(solution, writer,outerId);
            writeProcessList(solution, writer,outerId);
            writer.write("</AllocatorBalance>");
            writer.close();
        }catch (IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public static void graphResults(boolean saveGraph, String savePath){
        /*
        :param saveGraph: True if you want the image of the graph to be saved.
        :param savePath: The name of the file that will be created, if an absolute path isn't specified it will be
        created in the cwd.

        :return: Displays the graph of score/runtime and saves it if specified
         */
        class GraphResults extends ApplicationFrame {
            private final JFreeChart chart;

            private GraphResults(final String title) {

                super(title);
                final XYSeriesCollection data = new XYSeriesCollection(runTimeData);
                chart = ChartFactory.createXYLineChart(
                        "Optimization Depending On Runtime",
                        "Runtime (seconds)",
                        "SoftScore",
                        data,
                        PlotOrientation.VERTICAL,
                        false,
                        true,
                        false
                );

                final ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setPreferredSize(new java.awt.Dimension(1000, 540));
                setContentPane(chartPanel);
            }
            public JFreeChart getChart() {
                return chart;
            }
        }
        final GraphResults graph = new GraphResults("Optimization depending on runtime");
        graph.pack();
        RefineryUtilities.centerFrameOnScreen(graph);
        graph.setVisible(true);
        if (saveGraph) {
            try {
                ChartUtilities.saveChartAsPNG(new File(savePath), graph.getChart(), 1000, 540);
            } catch (Exception e) {
                System.out.println("An error has occurred\n");
                e.printStackTrace();
            }
        }
    }
}
