package graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import animation.Animation;
import animation.Keyframe;
import animation.MaskedAnimation;
import animation.MaskedTransition;
import animation.Node;
import animation.Transition;

public class AnimatedModel {
	private final Node rootNode;
	private final Matrix4f rootTransformation;
	
	private List<Animation> animations;
	private Animation currentAnimation;
	private List<MaskedAnimation> maskedAnimations;
	private Transition transition = null; //this is null by default. could cause a graphical issue. check animator method where poses are interpolated and it checks whether there are maskedExitTransitions and a transition
	private List<MaskedTransition> maskedTransitions;
	private List<MaskedTransition> maskedExitTransitions;
	private Keyframe previousKeyframe = null;
	
	private final Mesh[] meshes;
	
	public AnimatedModel(Mesh[] meshes, List<Animation> animations, Node rootNode, Matrix4f rootTransformation) {
		this.meshes = meshes;
		this.animations = animations;
		this.rootNode = rootNode;
		this.rootTransformation = rootTransformation;
		this.currentAnimation = animations.size() > 0 ? animations.get(4) : null;
		this.currentAnimation.setLoop(true);
		this.maskedAnimations = new ArrayList<MaskedAnimation>();
		this.maskedTransitions = new ArrayList<MaskedTransition>();
		this.maskedExitTransitions = new ArrayList<MaskedTransition>();
	}
	
	public void transitionToMaskedAnimation(int animationID, List<String> maskBoneNames, boolean loop, float transitionTime) {
		Animation animation = animations.get(animationID);
		
		if(!animation.isPlaying()) {
			animation.setLoop(loop);
			animation.playAnimation();
			
			List<Node> nodes = new ArrayList<Node>();
			for(String maskBoneName : maskBoneNames) {
				nodes.add(rootNode.findByName(maskBoneName));
			}
			
			MaskedTransition maskedTransition = new MaskedTransition(new Transition(transitionTime, previousKeyframe), nodes);
			maskedTransition.getTransition().startTransition();
			maskedTransitions.add(maskedTransition);
			
			maskedAnimations.add(new MaskedAnimation(nodes, animation));
		}
	}
	
	public void transitionToAnimation(int animationIdx, float transitionTime) {
		transition = new Transition(transitionTime, previousKeyframe);
		transition.startTransition();
		currentAnimation.stopAnimation();
		currentAnimation = animations.get(animationIdx);
		currentAnimation.playAnimation();
	}
	
	public void addMaskedExitTransition(MaskedTransition maskedExitTransition) {
		maskedExitTransitions.add(maskedExitTransition);
	}
	
	public void removeMaskedAnimation(int idx) {
		maskedAnimations.remove(idx);
	}
	
	public void removeMaskedTransition(int idx) {
		maskedTransitions.remove(idx);
	}
	
	public Animation getCurrentAnimation() {
		return currentAnimation;
	}
	
	public List<MaskedAnimation> getMaskedAnimations() {
		return maskedAnimations;
	}
	
	public List<MaskedTransition> getMaskedTransitions() {
		return maskedTransitions;
	}
	
	public List<MaskedTransition> getMaskedExitTransitions() {
		return maskedExitTransitions;
	}
	
	public Node getRootNode() {
		return rootNode;
	}
	
	public Matrix4f getRootTransform() {
		return rootTransformation;
	}
	
	public Mesh[] getMeshes() {
		return meshes;
	}
	
	public Transition getTransition() {
		return transition;
	}
	
	public void setPreviousKeyframe(Keyframe keyframe) {
		this.previousKeyframe = keyframe;
	}
	
	public Keyframe getPreviousKeyframe() {
		return previousKeyframe;
	}
}
