package animation;

import java.util.ArrayList;
import java.util.List;

public class MaskedTransition {
	private final List<Node> maskBoneNodes;
	private final Transition transition;

	public MaskedTransition(Transition transition, List<Node> maskBoneNodes) {
		this.transition = transition;
		this.maskBoneNodes = maskBoneNodes;
	}
	
	public Transition getTransition() {
		return transition;
	}
	
	public List<Node> getMaskBoneNodes() {
		return maskBoneNodes;
	}
	
	public List<Integer> getMaskBoneIDs() {
		List<Integer> maskBoneIDs = new ArrayList<Integer>();
		for(Node maskBoneNode : maskBoneNodes) {
			findMaskBoneChildren(maskBoneNode, maskBoneIDs);
		}
		
		return maskBoneIDs;
	}
	
	private void findMaskBoneChildren(Node node, List<Integer> maskBoneIDs) {
		maskBoneIDs.add(node.getBone().getID());
		
		for(Node childNode : node.getChildren()) {
			findMaskBoneChildren(childNode, maskBoneIDs);
		}
	}
}
