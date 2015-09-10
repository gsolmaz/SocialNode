/**
 * 
 */

import java.util.ArrayList;
import java.util.List;


public class Node {
	
	int id;
	double totalActivityTime;
	
	List<Interaction> interactionList;
	
	List<Interaction> receivedInteractionList;


	List<Double> interactionValuesList;
	
	double latestTrustValue;
	int latestInteractionTime;
	
	double trustValue;
	
	double previousTrustValue;
	
	
	public Node(int id) {
		super();
		this.id = id;
		initialize();
	}
	
	private void initialize() {
		this.trustValue = 0;
		this.previousTrustValue=0;
		this.latestInteractionTime = 0;
		this.interactionValuesList = new ArrayList<Double>();
		this.latestTrustValue = 0;
		this.previousTrustValue = 0;

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getTotalActivityTime() {
		return totalActivityTime;
	}

	public void setTotalActivityTime(double totalActivityTime) {
		this.totalActivityTime = totalActivityTime;
	}

	public List<Interaction> getInteractionList() {
		return interactionList;
	}

	public void setInteractionList(List<Interaction> interactionList) {
		this.interactionList = interactionList;
	}

	

	public List<Interaction> getReceivedInteractionList() {
		return receivedInteractionList;
	}

	public void setReceivedInteractionList(List<Interaction> receivedInteractionList) {
		this.receivedInteractionList = receivedInteractionList;
	}

	public double getPreviousTrustValue() {
		return previousTrustValue;
	}

	public void setPreviousTrustValue(double previousTrustValue) {
		this.previousTrustValue = previousTrustValue;
	}

	public void addInteractionValue(double i){
		interactionValuesList.add(i);
	}
	public List<Double> getInteractionValuesList() {
		return interactionValuesList;
	}
	public void clearInteractionValueList(){
		interactionValuesList.clear();
	}
	public void clearInteractionList(){
		interactionList.clear();
	}

	public double getLatestTrustValue() {
		return latestTrustValue;
	}

	public void setLatestTrustValue(double latestTrustValue) {
		this.latestTrustValue = latestTrustValue;
	}

	public int getLatestInteractionTime() {
		return latestInteractionTime;
	}

	public void setLatestInteractionTime(int latestInteractionTime) {
		this.latestInteractionTime = latestInteractionTime;
	}

	public void setInteractionValuesList(List<Double> interactionValuesList) {
		this.interactionValuesList = interactionValuesList;
	}
	

}
