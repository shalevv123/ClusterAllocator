﻿![Screenshot](/pictures/ClusterAllocatorLogo.png)

# User guide

## Requirements:

Latest version of Apache Maven

## API:

**AllocatorBalance AllocatorBalancingHandler.getBalance(String path)**

:param path : A path to an xml file in the require format.

:return: An allocation state translation of the xml file.

The format of the xml file should be as follows:

<AllocatorBalance id="0">

`    `<id>0</id>

`    `<clusterList id="1">

`        `<Cluster id="2">

`            `<id>0</id>

`            `<requiredCpuCores>6</requiredCpuCores>

`            `<requiredMemory>4</requiredMemory>

`            `<requiredNetworkBandwidth>6</requiredNetworkBandwidth>

`        `</Cluster>

`    `</clusterList>

`    `<serverList id="3">  

`        `<Server id="4">

`            `<id>7</id>

`            `<cpuCores>1</cpuCores>

`            `<memory>27</memory>

`            `<networkBandwidth>1</networkBandwidth>

`            `<originalCluster>0</originalCluster> **This is an optional field if the sever was already assigned**

`        `</Server>

`    `</serverList>

</AllocatorBalance>

Example: AllocatorBalancingHandler.getBalance("data/unsolved/2clusters-9servers.xml")



**AllocatorBalance Solve(AllocatorBalance unsolvedAllocatorBalance, Integer runTime, Boolean detailedPrint, Boolean saveResult, String resultFileName, Boolean logResults)**

:param unsolvedAllocatorBalance: An allocation state to be solved.

:param runTime: Runtime cap for the solver (in seconds) .

:param detailedPrint: True if you want a detailed print of the result. (See toDisplayString for more information)

:param saveResult: True if you want to save the results in an xml file. (See saveSolution for more information)

:param resultFileName: if "saveResult" is True this is the name of the file.

:param logResults: True if you want to log the results into the runTimeData.

:return: A solved allocation state.

Example: AllocatorBalancingHandler.Solve(AllocatorBalancingHandler.getBalance("data/unsolved/2clusters-9servers.xml"), time, true, true, "data/solved/test.xml", true);



**String toDisplayString(AllocatorBalance allocatorBalance)**

:param allocatorBalance : An allocation state to print in a meaningful way.

:return: A string that represents the allocation state.


**void saveSolution(AllocatorBalance solution, String savePath)**

:param solution: An allocation state to save in an xml file.

:param savePath: The name of the file that will be created, if an absolute path isn't specified it will be created in the cwd.

:sideEffect: Saves the allocation state as an xml file in the savePath.

Example: AllocatorBalancingHandler.saveSolution(balance, "data/solved/solution")



Short explanation: An XYSeries is practically an array of two dimentional points ( X, Y coordiantes) where X is time and Y is score.



**XYSeries getRunTimeData()**

:return: An XYSeries of the current logged solutions.



**void setRunTimeData(XYSeries runTimeData)**

:param runTimeData: An XYSeries to put into the logger (replaces current one).


**void addRunTimeData(int time, double score)**

:param time: The time of the solution.

:param score:  The score of the solution.



**void addRunTimeData(XYSeries series)**

:param series: XYSeries to add to the current logger state.



**void clearRunTimeData()**
Clears the runtime logger.



**void graphResults(boolean saveGraph, String savePath)**

:param saveGraph: True if you want the image of the graph to be saved.

:param savePath: The name of the file that will be created, if an absolute path isn't specified it will be created in the cwd.

:sideEffect: Displays the graph of score/runtime and saves it if specified.

Example: AllocatorBalancingHandler.graphResults(true,"pictures/graphTest.png")



