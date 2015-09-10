/**
 * 
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EgoNetworkCtrl {
	
	// interaction weights
	private double type0; 
	private double type1;
	private double type2; 
	private double maliciousInteractionWeightEffect;
	
	// program arguments
	private boolean isMasterNode;
	private int totalNumberOfSlaves;
	private int nodeIndex;
	private double maliciousnessLevel;
	private String outputDirectoryLocation;
	private int maliciousActivityStartTime;
	private int totalOperationTime;
	
	
	// trust structure parameters 
	private double trustIncreaseProb;
	private double trustDecayValuePerSeconds; 
	private double trustDecayThresholdInSeconds;
	private double maxTrust;
	private double maxCR;
	private double minCR;
	private boolean isLinearDecay;
	private double minWaningPerSecond;


	// calculated parameters (not the arguments;
	private double compressionInterval;

	
	public EgoNetworkCtrl(int numberOfNodes, int nodeIndex,
			double maliciousnessLevel, int maliciousActivityStartTime,
			boolean isMasterNode, int totalOperationTime,
			String outputDirectoryLocation) throws IOException, InterruptedException {
		// TODO Auto-generated constructor stub
		
		this.type0 = 1;
		this.type1 = 2;
		this.type2 = 3;
		this.maliciousInteractionWeightEffect = -0.5;

		this.trustIncreaseProb = 0.9; // can be 1 if evaluation of every interaction is assumed as correct
		this.trustDecayValuePerSeconds = 0.01; // the constant decay value
		this.trustDecayThresholdInSeconds = 9; // no-interactions for less than this number of seconds will be assumed natural and neglected
		
		// the three new parameteres
		this.maxTrust = 5000;
		this.maxCR = 5; 
		this.minCR = 3;
		this.isLinearDecay = true; //is linear or logarithmic decrease in trust;
		this.minWaningPerSecond = 0.001;
		this.compressionInterval = (maxCR - minCR)/maxTrust;

		// DO NOT change the values below this line
		this.totalNumberOfSlaves= numberOfNodes -1 ;
		this.outputDirectoryLocation = outputDirectoryLocation;
		this.isMasterNode= isMasterNode;
		this.nodeIndex = nodeIndex;
		this.maliciousnessLevel= maliciousnessLevel;
		this.maliciousActivityStartTime = maliciousActivityStartTime;
		this.totalOperationTime = totalOperationTime;
		interactionProcessor();
	}
	


	private List<Node> createNodeList() {
		List<Node> nodeList = new ArrayList<Node>();
		for(int i=1;i<=totalNumberOfSlaves;i++){
			if(i== nodeIndex) continue;
			Node p = new Node(i);
			p.setTotalActivityTime(totalOperationTime);
			nodeList.add(p);			
			}
			return nodeList;
	}



	public void interactionProcessor() throws IOException, InterruptedException {
		
		
		
		List<Node> nodeList = createNodeList();
		if(!isMasterNode){
			nodeList = generateInteractions(nodeList);
		}
		IOCtrl ioCtrl = new IOCtrl(outputDirectoryLocation, nodeIndex, isMasterNode,totalNumberOfSlaves);
		if(!isMasterNode){
			ioCtrl.sendMessages(nodeList);
		}
		if(!isMasterNode){
			boolean flag=false;
			while(!flag){
				flag= ioCtrl.checkDirectory();	
				if(flag){
					nodeList = ioCtrl.receiveMessages(nodeList);
					nodeList = computeTrustValues(nodeList);
					flag=true;
				}
				else{
					Thread.sleep(1000);
				}
			}	
			
			ioCtrl.sendTrustValuesToMaster(nodeList);
		}
		else{ // the master node behavior
			boolean flag=false;
			GlobalTrustValues gtv=null;
			while(!flag){
				flag= ioCtrl.checkDirectory();	
				if(flag){ // gather values
					gtv = ioCtrl.receiveTrustValues(nodeList);
					flag=true;
				}
				else{
					Thread.sleep(1000);
				}
			}	// output results		
			ioCtrl.outputGlobaValues(gtv);
		}
		
	}


	private List<Node> computeTrustValues(List<Node> nodeList) {
		for(int i=0;i<nodeList.size();i++){
			Node p = nodeList.get(i);
		
			
			List<Interaction> interactionList = p.getReceivedInteractionList();
			for(int j=0;j<interactionList.size();j++){
				Interaction in = interactionList.get(j);

				// calculate the trust decay according to latest values with the contact
				// DECREASE BECAUSE OF TIME
				p = performTrustDecay(p, in);
				// calculate the trust increase
				p = changeTrustValueByInteraction(p, in);
				p.setLatestInteractionTime(in.getEndTime());
			}
			// all interactions and trust values of a node is computed right now, store the node and
			// go ahead with the next node
			nodeList.set(i, p);
		}
		return nodeList;
	}


	private Node changeTrustValueByInteraction(Node p, Interaction in) {
		double latestTrustValue = p.getLatestTrustValue();
		double interactionWeightValue = computeValueOfInteraction(in);
		double newTrustValue;
		// there are two possibilities: collaboration or no collaboration
		Random r = new Random();
		double randomValue = r.nextDouble();
		// the INCREASE formula starts here
		double deltaTrust = maxCR - (compressionInterval * latestTrustValue);
		if(randomValue < trustIncreaseProb){ // trust increase (CASE 1)
			newTrustValue =  latestTrustValue +  (deltaTrust*interactionWeightValue);
			if(newTrustValue> maxTrust){ // the trust value cannot exceed the max trust value
				newTrustValue = maxTrust;
			}
		}
		else{//DECREASE BECAUSE OF NOT COLLABORATION (CASE 2)
			newTrustValue =  latestTrustValue - (deltaTrust*interactionWeightValue);
			if(newTrustValue<0){ // a trust value cannot be negative
				newTrustValue = 0;
			}
		}
		p.setLatestTrustValue(newTrustValue);
		
		return p;
	}


	private double computeValueOfInteraction(Interaction in) {
		// Interaction type: type 1, type 2 or type 3
		int type= in.getInteractionType();
		double weight=0;
		if(type==0){
			weight =type0;
		}
		else if (type==1){
			weight= type1;
		}
		else if (type==2){
			weight= type2;
		}
		else{
			System.out.println("Error in method computeValueOfInteraction: The interaction type does not exist !! ");
			return 0;
		}
		if(in.isMaliciousInteraction){
			return weight * maliciousInteractionWeightEffect;
		}
		else{
			return weight;
		}
	}


	private Node performTrustDecay(Node p, Interaction in) {
		// TWO TYPES: LINEAR OR LOGARITMIC WANING
		
		if(p.getLatestTrustValue() == 0.0 ) {
			return p;
		}

		double latestTrustValue = p.getLatestTrustValue();	
		int latestInteractionTime = p.getLatestInteractionTime();
		// now we know all the information that we need, do the calculation according to linear or log decays
		
		int interactionTimeDifferenceInSeconds =  in.getStartTime() -  latestInteractionTime;
		int numberOfDecayTimes = (int) Math.floor((int) (interactionTimeDifferenceInSeconds/trustDecayThresholdInSeconds));
		
		double decreaseValuePerSeconds = (interactionTimeDifferenceInSeconds * trustDecayValuePerSeconds);
		
		double newTrustValue=0;
		if(isLinearDecay){	// LINEAR DECAY (CASE 3)
			newTrustValue = latestTrustValue - numberOfDecayTimes * decreaseValuePerSeconds;
		}else{ // LOGARITMIC WANING STARTS HERE (CASE 4)
			newTrustValue = findNewTrustValueByLogaritmicDecayFormula( latestTrustValue, numberOfDecayTimes,decreaseValuePerSeconds);
		}
		if(newTrustValue<0){
			newTrustValue=0;
		}
		p.setLatestTrustValue(newTrustValue);
		return p;
	}


	private double findNewTrustValueByLogaritmicDecayFormula(double latestTrustValue, int numberOfDecayTimes, double decreaseValuePerDecay) {
		// LOGARITMIC WANING	
		
		double minimumWainPerDecay = (trustDecayThresholdInSeconds* minWaningPerSecond);
		double currentTrustValue = latestTrustValue;
		double previousTrustValue = latestTrustValue;
		double newTrustValue=0;
		for(int i=0;i<numberOfDecayTimes;i++){
			newTrustValue = currentTrustValue - minimumWainPerDecay * ( 1 + decreaseValuePerDecay - (previousTrustValue/maxTrust));
			previousTrustValue = currentTrustValue;
			currentTrustValue = newTrustValue;
		}	
		return newTrustValue;
	}


		
	private List<Node> generateInteractions(List<Node> nodeList) {
		List<Interaction> interactionList;
		for(int i=0;i<nodeList.size();i++){
			Node p = nodeList.get(i);
			if(p.getId()!= nodeIndex){
				interactionList = createInteractionsWithNode(p.getId());
			}
			else{
				interactionList = new ArrayList<Interaction>();
			}
			p.setInteractionList(interactionList);
		
			nodeList.set(i, p);
		}
		return nodeList;
	}






	private List<Interaction> createInteractionsWithNode(int interactedNodeId) {
		// TODO Auto-generated method stub
		List<Interaction> interactionList = new ArrayList<>();
		for(int i=0;i<totalOperationTime;i=i+10){
			Random r =new Random();
			boolean doesInteractionExist = r.nextBoolean();
			if(doesInteractionExist){
				Interaction in = new Interaction();
				int duration =r.nextInt(10); // min 0 seconds interaction, max 10 seconds
				duration = duration +1;
				in.setDuration(duration);
				in.setStartTime(i);
				int type = r.nextInt(3); 
				in.setInteractionType(type);
				in.setEndTime(i+duration);
				if(maliciousActivityStartTime<=i && maliciousnessLevel >0 ){
					double skipProb = r.nextDouble();
					if(skipProb < 0.666){
						continue;
					}
					
					double prob = r.nextDouble();
					if(prob<=maliciousnessLevel){
						in.setMaliciousInteraction(true);
					}
					else{
						in.setMaliciousInteraction(false);
					}
				}else{
					in.setMaliciousInteraction(false);
				}
				in.setInteractedNodeId(interactedNodeId);
				interactionList.add(in);
			}
		}
		return interactionList;
	}

}
