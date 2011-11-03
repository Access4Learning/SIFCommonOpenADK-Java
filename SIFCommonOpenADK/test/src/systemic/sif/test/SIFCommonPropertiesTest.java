package systemic.sif.test;

import systemic.sif.sifcommon.utils.SIFCommonProperties;

public class SIFCommonPropertiesTest
{
	public static void main(String[] args)
	{
		String agentID = "SIDRefDataAgent";
		System.out.println("============================= Test SIFCommonProperties ===========================");
		try
		{
			SIFCommonProperties frmaeworkProperties = new SIFCommonProperties("SIFAgent");
			System.out.println("\nProperties for Agent: "+agentID);
			System.out.println("ADKConfigDir: "+frmaeworkProperties.getADKConfigDir(agentID));
			System.out.println("ADKConfigFileName: "+frmaeworkProperties.getADKConfigFileName(agentID));
			System.out.println("ADKConfigPathAndFileName: "+frmaeworkProperties.getADKConfigPathAndFileName(agentID));
			System.out.println("CustonObjBasePackageName: "+frmaeworkProperties.getCustonObjBasePackageName(agentID));
			System.out.println("CustomObjects: "+frmaeworkProperties.getCustomObjects(agentID));
			System.out.println("DebugLevel: "+frmaeworkProperties.getDebugLevel(agentID));
			System.out.println("EventBatchSize: "+frmaeworkProperties.getEventBatchSize());
			System.out.println("EventFrequencyInSeconds (agent level): "+frmaeworkProperties.getEventFrequencyInSeconds(agentID, 500));
			System.out.println("EventFrequencyInSeconds (Publisher StudentPersonalPublisher):"+frmaeworkProperties.getEventFrequencyInSeconds(agentID, "StudentPersonalPublisher", 600));
			System.out.println("LogDir: "+frmaeworkProperties.getLogDir(agentID));
			System.out.println("LogFileName: "+frmaeworkProperties.getLogFileName(agentID));
			System.out.println("LogPathAndFileName: "+frmaeworkProperties.getLogPathAndFileName(agentID));
			System.out.println("PublisherBasePackageName: "+frmaeworkProperties.getPublisherBasePackageName(agentID));
			System.out.println("Publishers: "+frmaeworkProperties.getPublishers(agentID));
			System.out.println("TestDir: "+frmaeworkProperties.getTestDir(agentID));
			System.out.println("WorkDir: "+frmaeworkProperties.getWorkDir(agentID));
			System.out.println("MultiThreaded: "+frmaeworkProperties.getMultiThreaded(agentID, true));
			System.out.println("TestMode: "+frmaeworkProperties.getTestMode(agentID));

			System.out.println("\nProperties for Agent: OTLSRefDataAgent");
			System.out.println("SubscriberBasePackageName: "+frmaeworkProperties.getSubscriberBasePackageName("OTLSRefDataAgent"));
			System.out.println("Subscribers: "+frmaeworkProperties.getSubscribers("OTLSRefDataAgent"));
			System.out.println("SyncFrequencyInSeconds (aggent level): "+frmaeworkProperties.getSyncFrequencyInSeconds("OTLSRefDataAgent", 60));
			System.out.println("SyncFrequencyInSeconds (Subscriber SchoolCourseInfoSubscriber): "+frmaeworkProperties.getSyncFrequencyInSeconds("OTLSRefDataAgent", "SchoolCourseInfoSubscriber", 10));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		System.out.println("============================= End Test SIFCommonProperties ===========================");
	}
}
