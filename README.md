![Screenshot](/pictures/ClusterAllocatorLogo.png)

# User Guide
Requirements: 

  Latest version of Apache Maven
  
**AllocatorBalancingHandler.getBalance(String path)**

:param path : A path to an xml file in the require format.

:return: An allocation state translation of the xml file.

## The format of the xml file should be as follows:
'''
 <AllocatorBalance id="1">
  <id> 0 </id>
  <clusterList id="2">
   <Cluster id="3">
     <id> 0 </id>
     <requiredCpuCores> <NUMBER> </requiredCpuCores>
     <requiredMemory> <NUMBER> </requiredMemory>
     <requiredNetworkBandwidth> <NUMBER> </requiredNetworkBandwidth>
   </Cluster>
  </clusterList>
  <serverList id="4">
   <Server id="5">
     <id> 0 </id>
     <cpuCores> <NUMBER> </cpuCores>
     <memory> <NUMBER> </memory>
     <networkBandwidth> <NUMBER> </networkBandwidth>
     <originalCluster> <NUMBER> </originalCluster> **This is an optional field if a sever was already allocated**
   </Server>
  </serverList>
 </AllocatorBalance>
'''
