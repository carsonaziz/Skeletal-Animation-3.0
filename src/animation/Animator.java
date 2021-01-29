package animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import entities.GameItem;
import graphics.AnimatedModel;

public class Animator {
	private List<GameItem> gameItems;
	private Map<Integer, AnimatedFrame> animatedFrames;
	
	public Animator(List<GameItem> gameItems) {
		this.gameItems = gameItems;
		this.animatedFrames = new HashMap<Integer, AnimatedFrame>();
	}
	
	public void update(float elapsedTime) {
		for(GameItem gameItem : gameItems) {
			AnimatedModel model = gameItem.getModel();
			Animation currentAnimation = model.getCurrentAnimation();
			if(currentAnimation.isPlaying()) {
				model.getCurrentAnimation().increaseTime(elapsedTime);
				if(model.getTransition() != null) {
					if(model.getTransition().isInProgress()) {
						model.getTransition().increaseTime(elapsedTime);
					}
				}
			}

			List<MaskedAnimation> maskedAnimations = model.getMaskedAnimations();
			List<MaskedTransition> maskedTransitions = model.getMaskedTransitions();
			for(int i = 0; i < maskedAnimations.size(); i++) {
				if(maskedAnimations.get(i).getAnimation().isPlaying()) {
					maskedAnimations.get(i).getAnimation().increaseTime(elapsedTime);
					maskedTransitions.get(i).getTransition().increaseTime(elapsedTime);
				} else {
					MaskedTransition maskedExitTransition = new MaskedTransition(new Transition(0.5f, model.getPreviousKeyframe()), maskedTransitions.get(i).getMaskBoneNodes());
					maskedExitTransition.getTransition().startTransition();
					model.removeMaskedAnimation(i);
					model.removeMaskedTransition(i);
					model.addMaskedExitTransition(maskedExitTransition);
				}
			}
			
			for(int i = 0; i < model.getMaskedExitTransitions().size(); i++) {
				if(model.getMaskedExitTransitions().get(i).getTransition().isInProgress()) {
					model.getMaskedExitTransitions().get(i).getTransition().increaseTime(elapsedTime);
				} else {
					model.getMaskedExitTransitions().remove(i);
				}
			}
			animatedFrames.put(gameItem.getID(), buildCurrentFrame(gameItem));
		}
	}
	
	private AnimatedFrame buildCurrentFrame(GameItem gameItem) {
		AnimatedModel model = gameItem.getModel();
		Animation animation = model.getCurrentAnimation();
		List<MaskedAnimation> maskedAnimations = gameItem.getModel().getMaskedAnimations();
		
		if(animation == null) {
			return null;
		}
		
		Map<Integer, Matrix4f> currentLocalPose = calculateLocalPose(animation, maskedAnimations, model);
		Map<Integer, Matrix4f> currentGlobalPose = new HashMap<Integer, Matrix4f>();
		calculateGlobalPose(currentLocalPose, model.getRootNode(), model.getRootTransform(), currentGlobalPose);
		return buildAnimatedFrame(currentGlobalPose);
	}
	
	private Map<Integer, Matrix4f> calculateLocalPose(Animation animation, List<MaskedAnimation> maskedAnimations, AnimatedModel model) {
		Keyframe[] primaryKeyframes = animation.getPreviousAndNextKeyframes();
		PrimaryInterpolationData primaryInterpolationData = new PrimaryInterpolationData(primaryKeyframes, calculateProgression(primaryKeyframes[0], primaryKeyframes[1], animation.getElapsedTime()));
		List<MaskedInterpolationData> maskedInterpolationDatas = new ArrayList<MaskedInterpolationData>();
		
		if(maskedAnimations != null) {
			for(MaskedAnimation maskedAnimation : maskedAnimations) {
				Keyframe[] maskedKeyframes = maskedAnimation.getAnimation().getPreviousAndNextKeyframes();
				float progression = calculateProgression(maskedKeyframes[0], maskedKeyframes[1], maskedAnimation.getAnimation().getElapsedTime());
				maskedInterpolationDatas.add(new MaskedInterpolationData(maskedAnimation.getMaskedBones(), maskedKeyframes, progression));
			}
		}
		
		return interpolatePoses(primaryInterpolationData, maskedInterpolationDatas, model);
	}
	
	private void calculateGlobalPose(Map<Integer, Matrix4f> currentLocalPose, Node node, Matrix4f parentTransformation, Map<Integer, Matrix4f> globalPose) {
		Matrix4f currentLocalTransformation = null;
		Matrix4f currentGlobalTransformation = null;
		
		Bone bone = node.getBone();
		if(bone != null) {
			currentLocalTransformation = currentLocalPose.get(bone.getID());
			currentGlobalTransformation = parentTransformation.mul(currentLocalTransformation);
		} else {
			currentGlobalTransformation = parentTransformation;
		}
		
		for(Node childNode : node.getChildren()) {
			calculateGlobalPose(currentLocalPose, childNode, new Matrix4f(currentGlobalTransformation), globalPose);
		}
		
		if(bone != null) {
			currentGlobalTransformation = currentGlobalTransformation.mul(bone.getOffsetMatrix());
			globalPose.put(bone.getID(), currentGlobalTransformation);
		}
	}
	
	private AnimatedFrame buildAnimatedFrame(Map<Integer, Matrix4f> currentGlobalPose) {
		AnimatedFrame animatedFrame = new AnimatedFrame();
		for(Map.Entry<Integer, Matrix4f> matrixEntry : currentGlobalPose.entrySet()) {
			int boneID = matrixEntry.getKey();
			Matrix4f matrix = matrixEntry.getValue();
			animatedFrame.setMatrix(boneID, matrix);
		}
		
		return animatedFrame;
	}
	
	private float calculateProgression(Keyframe previousKeyframe, Keyframe nextKeyframe, float elapsedTime) {
		float timeBetweenFrames = nextKeyframe.getTimeStamp() - previousKeyframe.getTimeStamp();
		float progressionTime = elapsedTime - previousKeyframe.getTimeStamp();
		return progressionTime / timeBetweenFrames;
	}
	
	private Map<Integer, Matrix4f> interpolatePoses(PrimaryInterpolationData primaryInterpolationData, List<MaskedInterpolationData> maskedInterpolationDatas, AnimatedModel model) {
		Map<Integer, Matrix4f> currentLocalPose = new HashMap<Integer, Matrix4f>();
		Keyframe previousAnimationKeyframe = new Keyframe();
		
		Keyframe previousPrimaryKeyframe = primaryInterpolationData.getKeyframes()[0];
		Keyframe nextPrimaryKeyframe = primaryInterpolationData.getKeyframes()[1];
		float primaryProgression = primaryInterpolationData.getProgression();
		Transition transition = model.getTransition();
		
		for(int i = 0; i < previousPrimaryKeyframe.getBoneTransformations().length; i++) {
			if(maskedInterpolationDatas.size() > 0) {
				for(int j = 0; j < maskedInterpolationDatas.size(); j++) {
					MaskedInterpolationData maskedInterpolationData = maskedInterpolationDatas.get(j);
					Keyframe previousMaskedKeyframe = maskedInterpolationData.getKeyframes()[0];
					Keyframe nextMaskedKeyframe = maskedInterpolationData.getKeyframes()[1];
					float maskedProgression = maskedInterpolationData.getProgression();
					if(maskedInterpolationData.getMaskedBones().contains(i)) {
						interpolatePose(previousMaskedKeyframe, nextMaskedKeyframe, maskedProgression, i, currentLocalPose, previousAnimationKeyframe, model.getMaskedTransitions().get(j).getTransition());
					} else {
						interpolatePose(previousPrimaryKeyframe, nextPrimaryKeyframe, primaryProgression, i, currentLocalPose, previousAnimationKeyframe, transition);
					}
				}
			} else {
				interpolatePose(previousPrimaryKeyframe, nextPrimaryKeyframe, primaryProgression, i, currentLocalPose, previousAnimationKeyframe, transition, model.getMaskedExitTransitions());
			}
		}
		
		model.setPreviousKeyframe(previousAnimationKeyframe);
		return currentLocalPose;
	}
	
	private void interpolatePose(Keyframe previousKeyframe, Keyframe nextKeyframe, float progression, int boneID, Map<Integer, Matrix4f> currentLocalPose, Keyframe previousAnimationKeyframe, Transition transition) {
		BoneTransformation previousBoneTransformation = previousKeyframe.getBoneTransformations()[boneID];
		BoneTransformation nextBoneTransformation = nextKeyframe.getBoneTransformations()[boneID];
		BoneTransformation currentBoneTransformation = BoneTransformation.interpolate(previousBoneTransformation, nextBoneTransformation, progression);
		if(transition != null) {
			currentBoneTransformation = BoneTransformation.interpolate(transition.getPreviousAnimationKeyframe().getBoneTransformations()[boneID], currentBoneTransformation, transition.getProgression());
		}
		currentLocalPose.put(boneID, currentBoneTransformation.convertToMatrix());
		previousAnimationKeyframe.getBoneTransformations()[boneID] = currentBoneTransformation;
	}
	
	private void interpolatePose(Keyframe previousKeyframe, Keyframe nextKeyframe, float progression, int boneID, Map<Integer, Matrix4f> currentLocalPose, Keyframe previousAnimationKeyframe, Transition transition, List<MaskedTransition> maskedExitTransitions) {
		BoneTransformation previousBoneTransformation = previousKeyframe.getBoneTransformations()[boneID];
		BoneTransformation nextBoneTransformation = nextKeyframe.getBoneTransformations()[boneID];
		BoneTransformation currentBoneTransformation = BoneTransformation.interpolate(previousBoneTransformation, nextBoneTransformation, progression);
		
		if(maskedExitTransitions.size() > 0 && transition != null) {
			for(MaskedTransition maskedExitTransition : maskedExitTransitions) {
				if(maskedExitTransition.getMaskBoneIDs().contains(boneID)) {
					currentBoneTransformation = BoneTransformation.interpolate(maskedExitTransition.getTransition().getPreviousAnimationKeyframe().getBoneTransformations()[boneID], currentBoneTransformation, maskedExitTransition.getTransition().getProgression());
				} else {
					currentBoneTransformation = BoneTransformation.interpolate(transition.getPreviousAnimationKeyframe().getBoneTransformations()[boneID], currentBoneTransformation, transition.getProgression());
				}
			}
		} else if(maskedExitTransitions.size() > 0 && transition == null) { 
			for(MaskedTransition maskedExitTransition : maskedExitTransitions) {
				if(maskedExitTransition.getMaskBoneIDs().contains(boneID)) {
					currentBoneTransformation = BoneTransformation.interpolate(maskedExitTransition.getTransition().getPreviousAnimationKeyframe().getBoneTransformations()[boneID], currentBoneTransformation, maskedExitTransition.getTransition().getProgression());
				}
			}
		} else if(maskedExitTransitions.size() == 0 && transition != null) {
			currentBoneTransformation = BoneTransformation.interpolate(transition.getPreviousAnimationKeyframe().getBoneTransformations()[boneID], currentBoneTransformation, transition.getProgression());
		}
		currentLocalPose.put(boneID, currentBoneTransformation.convertToMatrix());
		previousAnimationKeyframe.getBoneTransformations()[boneID] = currentBoneTransformation;
	}
	
	public AnimatedFrame getAnimatedFrame(int gameItemID) {
		return animatedFrames.get(gameItemID);
	}
	
	class MaskedInterpolationData {
		private List<Integer> maskedBones;
		private Keyframe[] keyframes;
		private float progression;
		
		public MaskedInterpolationData(List<Integer> maskedBones, Keyframe[] keyframes, float progression) {
			this.maskedBones = maskedBones;
			this.keyframes = keyframes;
			this.progression = progression;
		}
		
		public List<Integer> getMaskedBones() {
			return maskedBones;
		}
		
		public Keyframe[] getKeyframes() {
			return keyframes;
		}
		
		public float getProgression() {
			return progression;
		}
	}
	
	class PrimaryInterpolationData {
		private Keyframe[] keyframes;
		private float progression;
		
		public PrimaryInterpolationData(Keyframe[] keyframes, float progression) {
			this.keyframes = keyframes;
			this.progression = progression;
		}
		
		public Keyframe[] getKeyframes() {
			return keyframes;
		}
		
		public float getProgression() {
			return progression;
		}
	}
	
}
