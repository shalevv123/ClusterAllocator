![Screenshot](/pictures/ClusterAllocatorLogo.png)

# User guide

## Requirements:

Latest version of Apache Maven

## API:

**AllocatorBalance AllocatorBalancingHandler.getBalance(String path)**

  &emsp; :param path : A path to an xml file in the require format.

  &emsp; :return: An allocation state translation of the xml file.

  &emsp; The format of the xml file should be as follows:

﻿![Screenshot](/pictures/xmlExample.png)
 
 &emsp; Example: AllocatorBalancingHandler.getBalance("data/unsolved/2clusters-9servers.xml")



**AllocatorBalance Solve(AllocatorBalance unsolvedAllocatorBalance, Integer runTime, Boolean detailedPrint, Boolean saveResult, String resultFileName, Boolean logResults)**

&emsp;  :param unsolvedAllocatorBalance: An allocation state to be solved.

&emsp;  :param runTime: Runtime cap for the solver (in seconds) .

&emsp;  :param detailedPrint: True if you want a detailed print of the result. 

&emsp; (See toDisplayString for more information)

&emsp;  :param saveResult: True if you want to save the results in an xml file.

&emsp; (See saveSolution for more information)

&emsp;  :param resultFileName: if "saveResult" is True this is the name of the file.

&emsp;  :param logResults: True if you want to log the results into the runTimeData.

&emsp;  :return: A solved allocation state.

&emsp;  Example: AllocatorBalancingHandler.Solve(balance, time, true, true, "data/solved/test.xml", true);



**String toDisplayString(AllocatorBalance allocatorBalance)**

&emsp;  :param allocatorBalance : An allocation state to print in a meaningful way.

&emsp;  :return: A string that represents the allocation state.


**void saveSolution(AllocatorBalance solution, String savePath)**

 &emsp; :param solution: An allocation state to save in an xml file.

 &emsp; :param savePath: The name of the file that will be created, if an absolute path isn't specified it will be created

&emsp; in the cwd.

 &emsp; :sideEffect: Saves the allocation state as an xml file in the savePath.

&emsp;  Example: AllocatorBalancingHandler.saveSolution(balance, "data/solved/solution")



Short explanation: An XYSeries is practically an array of two dimentional points ( X, Y coordiantes) where X is time and Y is score.



**XYSeries getRunTimeData()**

 &emsp; :return: An XYSeries of the current logged solutions.



**void setRunTimeData(XYSeries runTimeData)**

 &emsp; :param runTimeData: An XYSeries to put into the logger (replaces current one).


**void addRunTimeData(int time, double score)**

&emsp;  :param time: The time of the solution.

&emsp;  :param score:  The score of the solution.



**void addRunTimeData(XYSeries series)**

&emsp;  :param series: XYSeries to add to the current logger state.



**void clearRunTimeData()**
&emsp;  Clears the runtime logger.



**void graphResults(boolean saveGraph, String savePath)**

&emsp;  :param saveGraph: True if you want the image of the graph to be saved.

&emsp;  :param savePath: The name of the file that will be created, if an absolute path isn't specified it will be created

&emsp; in the cwd.

&emsp;  :sideEffect: Displays the graph of score/runtime and saves it if specified.

&emsp;  Example: AllocatorBalancingHandler.graphResults(true,"pictures/graphTest.png")



