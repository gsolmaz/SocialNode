import java.util.ArrayList;
import java.util.List;


public class GlobalTrustValues {
	List<Double> trustValueList;

	public GlobalTrustValues(int numberOfSlaves) {
		super();
		this.trustValueList = new ArrayList<Double>();
		for(int i=0;i<numberOfSlaves;i++){
			trustValueList.add(0.0);
		}
	}
	
	public void addTrustValue(int nodeIndex, double value){
		double oldValue = trustValueList.get(nodeIndex);
		trustValueList.set(nodeIndex, oldValue+value);
	}

	public List<Double> getTrustValueList() {
		return trustValueList;
	}

	
}
