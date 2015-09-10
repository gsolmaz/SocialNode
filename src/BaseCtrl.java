/**
 * 
 */



import java.io.IOException;


/**
 * @author Gurkan Solmaz
 * 		   Department of EECS - University of Central Florida
 * 		   EEL6760 - Data Int. Computing and Cloud - Project - Fall 2013
 * 		   Instructor: Dr. Jun Wang
 */
public class BaseCtrl {

	// input parameters
	private static boolean isMasterNode;
	private static  int numberOfNodes;
	private static int nodeIndex;
	private static double maliciousnessLevel;
	private static String outputDirectoryLocation;
	private static int maliciousActivityStartTime;
	private static int totalOperationTime;
	
	public static void main(String[] args) throws Exception {
		// inputs
		driver(args);
		EgoNetworkCtrl egoNetworkCtrl = new EgoNetworkCtrl(numberOfNodes, nodeIndex, maliciousnessLevel, maliciousActivityStartTime, isMasterNode, totalOperationTime,outputDirectoryLocation);
		egoNetworkCtrl.hashCode();
		finalizeApplication();
	}


 	private static void finalizeApplication() {	
		System.exit(0);		
 	}
 	

 	
	private static void driver(String[] args) throws ClassNotFoundException, IOException {
		// this is for simplicity in working local environment
		boolean isMaster = false; 
		if(args[0].equalsIgnoreCase("master")) {
			isMaster=true;
		}
		isMasterNode = isMaster;
		//Configuration conf = new Configuration();
	    if(isMaster){
	    //	job = new Job(conf, "master-0"); // master node (=0)
	    	nodeIndex = 0;
	    }
	
		numberOfNodes = Integer.parseInt(args[1]);

	    //outputDirectoryLocation = "/user/gurkan/SocialNode/";
	    totalOperationTime = Integer.parseInt(args[2]);
		outputDirectoryLocation = args[3];
	    if(!isMaster){
	    	nodeIndex= Integer.parseInt(args[4]);
	    }
	    
/*	    
	    Path inputPath;
	    if(isMaster){
			inputPath = new Path(outputDirectoryLocation + "master-0");
	    }
	    else{
	    	inputPath = new Path(outputDirectoryLocation + "slave-" + nodeIndex);
	    }*/

		
		//FileInputFormat.addInputPath(job, inputPath);

		//FileOutputFormat.setOutputPath(job,inputPath);
	 
	    //	FileSystem fs = inputPath.getFileSystem(conf);
		//FSDataOutputStream fileOut = fs.create(inputPath);
		
		if(!isMaster){
			maliciousnessLevel = Double.parseDouble(args[5]);
			maliciousActivityStartTime = Integer.parseInt(args[6]);

		} else{
			maliciousnessLevel =0;
		}
	}


}
