package animation;

public class Transition {
	private float elapsedTime;
	
	private final float duration;
	private final Keyframe previousAnimationKeyframe;
	
	private boolean inProgress = false;
	
	public Transition(float duration, Keyframe previousAnimationKeyframe) {
		this.elapsedTime = 0.0f;
		this.duration = duration;
		this.previousAnimationKeyframe = previousAnimationKeyframe;
	}
	
	public void startTransition() {
		if(!inProgress) {
			elapsedTime = 0.0f;
			inProgress = true;
		}
	}
	
	public void increaseTime(float delta) {
		if(inProgress) {
			if(elapsedTime + delta > duration) {
				inProgress = false;
			} else {
				elapsedTime += delta;
			}
		}
	}
	
	public float getElapsedTime() {
		return elapsedTime;
	}
	
	public float getProgression() {
		return elapsedTime / duration;
	}
	
	public float getDuration() {
		return duration;
	}
	
	public Keyframe getPreviousAnimationKeyframe() {
		return previousAnimationKeyframe;
	}
	
	public boolean isInProgress() {
		return inProgress;
	}
}
