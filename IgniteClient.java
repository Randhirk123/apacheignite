package com.fss.client;

import java.util.Arrays;
import java.util.UUID;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.fss.domain.Employee;

public class IgniteClient {

	private static IgniteCache<Integer, Employee> ignitecache=null;
	public static void main(String[] args) throws InterruptedException {
		Ignite ignite=startIgnite();
		IgniteCompute compute = ignite.compute();
		compute.broadcast(() -> System.out.println("Hello Server"+compute.clusterGroup().node()));
		Employee e=getCacheData(ignite,1);
		UUID nodeId=ignite.cluster().forClients().node().id();
		System.out.println("Trying to get client node id...."+" "+ignite.cluster().forClients().node().id());
		System.out.println("values are in server cache Before Time expired"+" "+e);
		System.out.println("===============================================================================");
		
		//after expired time
		Thread.sleep(8000);
		Employee e1=getCacheData(ignite,1);
		System.out.println("trying to fetch data after expiry time from server cache"+" "+e1);
		System.out.println("===============================================================================");
		stopIgnite();
		
	}
	
	public static Ignite startIgnite()
	{
		final IgniteConfiguration ignConfiguration=new IgniteConfiguration();
		ignConfiguration.setClientMode(true);
		ignConfiguration.setPeerClassLoadingEnabled(true);
		
		final TcpCommunicationSpi spi=new TcpCommunicationSpi();
		spi.setSocketWriteTimeout(60000);
		final TcpDiscoverySpi tcSpi=new TcpDiscoverySpi();
		final TcpDiscoveryVmIpFinder finder=new TcpDiscoveryVmIpFinder();
		finder.setAddresses(Arrays.asList("127.0.0.1:47500..47509","127.0.0.1:47500..47509"));
		tcSpi.setIpFinder(finder);
		ignConfiguration.setDiscoverySpi(tcSpi);
		ignConfiguration.setCommunicationSpi(spi);
		System.out.println("trying to print server node id"+ignConfiguration.getNodeId());
		Ignite ignite=Ignition.start(ignConfiguration);
		return ignite;
	}
	
	
	 public static void stopIgnite()
	 {
         Ignition.stop(true);
	 } 
	 
	 public static Employee getCacheData(Ignite ignite,Integer id) throws InterruptedException
	 {
		 ignitecache=ignite.cache("EmployeeCache");
		
		/*//before removing from cache
			System.out.println("trying to fetch data before expiry time from cache"+ignitecache.get(1));
			System.out.println("trying to fetch data before expiry time from cache"+ignitecache.get(2));
			System.out.println("trying to fetch data before expiry time from cache"+ignitecache.get(3));*/
			
			System.out.println("===============================================================================");
			 /*//after expired time
			 Thread.sleep(5000);
			 System.out.println("trying to fetch data after expiry time from cache"+ignitecache.get(1));
			 System.out.println("trying to fetch data after expiry time from cache"+ignitecache.get(2));
			 System.out.println("trying to fetch data after expiry time from cache"+ignitecache.get(3));*/
			 
			 return ignitecache.get(id);
         
	 }
	 
	private static boolean switchingServerOnDelayTime()
	{
		
		return false;
	}
		
	
}
