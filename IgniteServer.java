package com.fss.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.fss.domain.Employee;

public class IgniteServer {

	private static final String CacheNAME="EmployeeCache";
	private static  IgniteCache<Integer,Employee> cache=null;
	public static void main(String[] args) {
		try
		{
			int i=0;
			IgniteConfiguration config=multicastIgniteInstanceConfiguration();
			//Ignite ignite=Ignition.start(igniteInstanceConfiguration());
			Ignite ignite=Ignition.start(config);
			getClusterDetails(ignite);
			System.out.println("trying to print ignitename"+" "+ignite.configuration().getIgniteInstanceName());
			cache=ignite.getOrCreateCache(igniteCacheConfiguration());
			ExpiryPolicy policy=getExpiryPolicyConfig();
			int size=ignite.cluster().forServers().nodes().size();
			UUID serverNodeId=ignite.cluster().forServers().node().id();
			System.out.println("UUID Value Printing"+serverNodeId);
			//config.setNodeId(serverNodeId);
			//System.out.println("Trying to get server node  id...."+" "+ignite.cluster().forServers().node().id());
			System.out.println("Number of server node....."+size);
			IgniteCompute compute = ignite.compute();
			compute.broadcast(() -> System.out.println("Hello Server"+compute.clusterGroup().node()));
			IgniteCache<Object,Object> igniteCache=ignite.cache(CacheNAME).withExpiryPolicy(policy);
			//put(1,new Employee(123,"Ram","Chennai", 25));
			igniteCache.put(1,new Employee(123,"Ram","Chennai", 25));
			igniteCache.put(2,new Employee(124,"Raju","Delhi", 26));
			igniteCache.put(3,new Employee(125,"Rajan","Mumbai", 27));
			
			/*IgniteCache<Integer,String> cache=ignite.getOrCreateCache(CacheNAME);
			putGet(cache);*/
			System.out.println("cluster group started.............");
			//clusterGroup();
			/*Map<Integer,UUID> hMap=new HashMap<Integer, UUID>();
			for(i=0; i<size; i++)
			{
				hMap.put(i,serverNodeId);
			}
			for(Map.Entry<Integer,UUID> entry: hMap.entrySet())
			{
				System.out.println(entry.getValue());
			}*/
			/*if(config.getNodeId().equals(serverNodeId))
			{
				System.out.println("True.............................");
			}*/
			/*while(true)
			{
				Thread.currentThread().sleep(30000);
				System.out.println("running forever for stop press ctrl+c...");
			}*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static CacheConfiguration<Integer,Employee> igniteCacheConfiguration() {
		final CacheConfiguration<Integer,Employee> cacheConfiguration=new CacheConfiguration<Integer, Employee>(CacheNAME);
		cacheConfiguration.setCacheMode(CacheMode.PARTITIONED).setBackups(1);
		cacheConfiguration.setCopyOnRead(true);
		cacheConfiguration.setIndexedTypes(Long.class,Employee.class);
		cacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
		cacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
		return cacheConfiguration;
	}
	private static IgniteConfiguration igniteInstanceConfiguration() {
		IgniteConfiguration cfg=new IgniteConfiguration();
		DataStorageConfiguration storageCfg=getDataStorageConfiguration();
		TcpDiscoverySpi discoverySpi=new TcpDiscoverySpi();
		TcpDiscoveryVmIpFinder finder=new TcpDiscoveryVmIpFinder();
		finder.setAddresses(Arrays.asList("127.0.0.1:47500..47509","127.0.0.1:47500..47509"));
		discoverySpi.setIpFinder(finder);
		cfg.setDiscoverySpi(discoverySpi);
		cfg.setPeerClassLoadingEnabled(true);
		cfg.setDataStorageConfiguration(storageCfg);
		cfg.setIgniteInstanceName("hello");
		//UUID serverNodeId=ignite.cluster().forServers().node().id();
		//cfg.setNodeId(serverNodeId);
		return cfg;
	}
	
	
	private static IgniteConfiguration multicastIgniteInstanceConfiguration() {
		IgniteConfiguration cfg=new IgniteConfiguration();
		DataStorageConfiguration storageCfg=getDataStorageConfiguration();
		TcpDiscoverySpi discoverySpi=new TcpDiscoverySpi();
		TcpDiscoveryMulticastIpFinder finder=new TcpDiscoveryMulticastIpFinder();
		finder.setMulticastGroup("228.10.10.157");
		finder.setAddresses(Arrays.asList("127.0.0.1:47500..47509","127.0.0.1:47500..47509"));
		discoverySpi.setIpFinder(finder);
		cfg.setDiscoverySpi(discoverySpi);
		cfg.setPeerClassLoadingEnabled(true);
		cfg.setDataStorageConfiguration(storageCfg);
		//UUID serverNodeId=ignite.cluster().forServers().node().id();
		//cfg.setNodeId(serverNodeId);
		return cfg;
	}
	
	
	private static DataStorageConfiguration getDataStorageConfiguration()
	{
		final DataStorageConfiguration configuration=new DataStorageConfiguration();
		configuration.getDefaultDataRegionConfiguration().setMaxSize(4L * 1024 * 1024 * 1024);
		return configuration;
	}
	
	private static ExpiryPolicy getExpiryPolicyConfig()
	{
		ExpiryPolicy exPolicy=new CreatedExpiryPolicy(new javax.cache.expiry.Duration(TimeUnit.SECONDS,150));
		return exPolicy;
	}
	
	/*private static void putGet(IgniteCache<Integer, String> cache) throws IgniteException {
		System.out.println(">>> Cache put-get example started.");

		// Store keys in cache.
		for (int i = 0; i < 100; i++)
		  cache.put(i, Integer.toString(i));

		System.out.println(">>> Stored values in cache.");

		int size = 0;
		for (int i = 0; i < 100; i++) {
		  if (cache.get(i) != null)
		    size++;
		}
		System.out.println("Cache size:" + size);
		}*/
	
	/*private static void put(Integer key, Employee value)
	{
		cache.put(key,value);
	}*/
	
	private static List<UUID> getAllNodes()
	{
		return null;
	}
	
	private static void clusterGroup()
	{
		final Ignite ignite = Ignition.ignite();

		IgniteCluster cluster = ignite.cluster();

		// Get compute instance which will only execute
		// over remote nodes, i.e. not this node.
		IgniteCompute compute = ignite.compute(cluster.forRemotes());
		IgniteCluster cluster1 = ignite.cluster();
		ClusterGroup oldestGroup = cluster.forRemotes().forOldest();

		ClusterNode oldestNode = oldestGroup.node();

		// Cluster group with remote nodes, i.e. other than this node.
		ClusterGroup remoteGroup = cluster.forRemotes();

		// Broadcast to all remote nodes and print the ID of the node 
		// on which this closure is executing.
		compute.broadcast(() -> System.out.println("Hello Node: " + ignite.cluster().localNode().id()));

		// All cluster nodes in the group.
		Collection<ClusterNode> grpNodes = remoteGroup.nodes();

		// First node in the group (useful for groups with one node).
		ClusterNode node = remoteGroup.node();

		// And if you know a node ID, get node by ID.
		UUID myID =ignite.cluster().forServers().node().id();

		node = remoteGroup.node(myID);
		System.out.println("hellooooooo"+" "+node);
	}
	
	private static void getClusterDetails( Ignite ignite)
	{
		//activate the cluser
		ignite.cluster().active(true);
		Collection<ClusterNode> nodes=ignite.cluster().forServers().nodes();//getting all server node already up and running
		System.out.println("inside getcluster details..."+nodes.size());
		//set baseline topology that is represented by this nodes....
		ignite.cluster().setBaselineTopology(nodes);
		
		
	}
}
