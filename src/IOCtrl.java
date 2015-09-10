import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class IOCtrl {
	Path readFilePath;
	String outputDirectoryLocation;
	int nodeIndex;
	boolean isMaster;
	FileSystem fs;
	int numberOfSlaves;
	
	
	public IOCtrl(String outputDirectoryLocation, int nodeIndex, boolean isMaster, int numberOfSlaves) throws IOException {
		super();
		this.outputDirectoryLocation = outputDirectoryLocation;
		this.isMaster = isMaster;
		this.nodeIndex = nodeIndex;
		this.numberOfSlaves = numberOfSlaves;
	    if(isMaster){
	    	readFilePath = new Path(outputDirectoryLocation + "master-0");
	    }
	    else{
	    	readFilePath = new Path(outputDirectoryLocation + "slave-" + nodeIndex);
	    }
	    Configuration conf = new Configuration();
	    fs = FileSystem.get(conf);
	}

	public void sendMessages(List<Node> nodeList) throws IOException{
		for(int i=0;i<nodeList.size();i++){
			Node n = nodeList.get(i);
			if(n.getId() == nodeIndex) continue; // check if the same node location
			Path writeFileDirectory = new Path(outputDirectoryLocation + "slave-" + n.getId());
		    Configuration conf = new Configuration();
			fs = writeFileDirectory.getFileSystem(conf);

			
			if(!fs.exists(writeFileDirectory)){
				fs.mkdirs(writeFileDirectory);
			}
			Path writeFilePath = new Path(writeFileDirectory+ "//" + "Messages-" + nodeIndex + ".txt");
			fs = writeFilePath.getFileSystem(conf);
			FSDataOutputStream out = fs.create(writeFilePath);
			
			List<Interaction> interactionList = n.getInteractionList();
			
			for(int j=0;j<interactionList.size();j++){
				Interaction in = interactionList.get(j);
				if(in.isMaliciousInteraction()){
					out.writeBytes("Mallicious_Message");
				}
				else{
					out.writeBytes("Safe_Message");
				}

				out.writeBytes("   Time: " + in.getStartTime() + "   Duration: " + in.getDuration() +  "   Message_Type: "  + in.getInteractionType() + "\n"  );
			}
			out.close();
			
		}
	}
	
	public boolean checkDirectory() throws IOException{

		
		Configuration conf = new Configuration();
		fs = readFilePath.getFileSystem(conf);
	    FileStatus[] status = fs.listStatus(readFilePath);

        if(!isMaster){
        	if(status == null || status.length != numberOfSlaves-1) return false;
        }
		else{
			if(status == null ||status.length != numberOfSlaves) return false;
		}
        return true;

	}
	
	public List<Node> receiveMessages(List<Node> nodeList) throws IOException{
		Configuration conf = new Configuration();
		fs = readFilePath.getFileSystem(conf);
	    FileStatus[] status = fs.listStatus(readFilePath);
	
        for (int i=0;i<status.length;i++){
        		Node n = nodeList.get(i);
                BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
                String line=null;
                line=br.readLine();
                List<Interaction> tmpInteractionList = new ArrayList<Interaction>();
                while (line!=null && !line.equalsIgnoreCase("\n")){
            
	                line = line.replace("   ", " ");
	                String[] arr = line.split(" ");
	         
	                Interaction in = new Interaction();
	                
	                if(arr[0].equalsIgnoreCase("Mallicious_Message")){
	                	in.setMaliciousInteraction(true);
	                }
	                else if(arr[0].equalsIgnoreCase("Safe_Message")){
	                	in.setMaliciousInteraction(false);
	                }
	                else{
	                	System.out.println("Error while reading interaction");
	                }

	                in.setStartTime(Integer.parseInt(arr[2]));
	                in.setDuration(Integer.parseInt(arr[4]));
	                in.setInteractionType(Integer.parseInt(arr[6]));
	                in.setInteractedNodeId(nodeIndex);
	                in.setEndTime(in.getStartTime()+in.getDuration());
	                tmpInteractionList.add(in);
	                line=br.readLine();
                }
                n.setReceivedInteractionList(tmpInteractionList);
    			nodeList.set(i, n);
        }
		return nodeList;
	}


	public GlobalTrustValues receiveTrustValues(List<Node> nodeList) throws IOException{
		GlobalTrustValues global = new GlobalTrustValues(nodeList.size());
		Configuration conf = new Configuration();
		fs = readFilePath.getFileSystem(conf);
	    FileStatus[] status = fs.listStatus(readFilePath);
	
        for (int i=0;i<status.length;i++){
                BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
                String line=null;
                line=br.readLine();
                while (line!=null && !line.equalsIgnoreCase("\n")){
                     	
	                line = line.replace("  ", " ");
	                String[] arr = line.split(" ");
	                
	                int nodeIndex = Integer.parseInt(arr[1]);
	                double newTrustValue = Double.parseDouble(arr[3]);
	                
	                global.addTrustValue(nodeIndex-1, newTrustValue);

	                line=br.readLine();
                }
        }
		return global;
	}
	
	
	public void sendTrustValuesToMaster(List<Node> nodeList) throws IOException {
			// check if the same node location
			Path writeFileDirectory = new Path(outputDirectoryLocation + "master-0");
		    Configuration conf = new Configuration();
			fs = writeFileDirectory.getFileSystem(conf);
			
			if(!fs.exists(writeFileDirectory)){ // create the directory if it does not exist yet
				fs.mkdirs(writeFileDirectory);
			}
			Path writeFilePath = new Path(writeFileDirectory+ "//" + "TrustValues-" + nodeIndex + ".txt");
			fs = writeFilePath.getFileSystem(conf);
			FSDataOutputStream out = fs.create(writeFilePath);
			for(int i=0;i<nodeList.size();i++){
				Node n = nodeList.get(i);
				if(n.getId() == nodeIndex) continue; 
				out.writeBytes("Node_Index:  " + n.getId());
				out.writeBytes("  Trust_Value:  " + n.getLatestTrustValue() +  "\n");
			}
			out.close();
	}

	public void outputGlobaValues(GlobalTrustValues gtv) throws IOException {
		// check if the same node location
		List<Double> valueList = gtv.getTrustValueList();
		Path writeFileDirectory = new Path(outputDirectoryLocation + "master-0");
	    Configuration conf = new Configuration();
		fs = writeFileDirectory.getFileSystem(conf);
		
	
		Path writeFilePath = new Path(writeFileDirectory+ "//" + "GlobalValues" + ".txt");
		fs = writeFilePath.getFileSystem(conf);
		FSDataOutputStream out = fs.create(writeFilePath);
		for(int i=0;i<valueList.size();i++){
			out.writeBytes("Slave_Node_Index:  " + (i+1));
			out.writeBytes("   Global_Trust_Value:  " + valueList.get(i) +  "\n");
		}
		out.close();		
	}

}
