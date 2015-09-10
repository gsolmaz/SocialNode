


public class Interaction {
	

	int interactionType; 	
	int interactedNodeId;
	
	boolean isMaliciousInteraction;

	int duration;
	int startTime;
	int endTime;
	

	public boolean isMaliciousInteraction() {
		return isMaliciousInteraction;
	}


	public void setMaliciousInteraction(boolean isMaliciousInteraction) {
		this.isMaliciousInteraction = isMaliciousInteraction;
	}


	public int getInteractionType() {
		return interactionType;
	}

	
	public int getInteractedNodeId() {
		return interactedNodeId;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}


	public void setInteractionType(int interactionType) {
		this.interactionType = interactionType;
	}

	public void setInteractedNodeId(int interactedNodeId) {
		this.interactedNodeId = interactedNodeId;
	}


	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}


	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	

}
