package com.jams.music.player.Helpers;

/**
 * Audio Manager helper class. 
 * 
 * @author Saravan Pantham
 */
public class AudioManagerHelper {

	private int mOriginalVolume;
	private boolean mHasAudioFocus = false;
	private boolean mAudioDucked = false;
	private int mTargetVolume;
	private int mCurrentVolume;
	private int mStepDownIncrement;
	private int mStepUpIncrement;
	
	/*
	 * Getter methods.
	 */
	
	public int getOriginalVolume() {
		return mOriginalVolume;
	}
	
	public boolean hasAudioFocus() {
		return mHasAudioFocus;
	}
	
	public boolean isAudioDucked() {
		return mAudioDucked;
	}
	
	public int getTargetVolume() {
		return mTargetVolume;
	}
	
	public int getCurrentVolume() {
		return mCurrentVolume;
	}
	
	public int getStepDownIncrement() {
		return mStepDownIncrement;
	}
	
	public int getStepUpIncrement() {
		return mStepUpIncrement;
	}
	
	/*
	 * Setter methods.
	 */
	
	public void setHasAudioFocus(boolean hasAudioFocus) {
		mHasAudioFocus = hasAudioFocus;
	}
	
	public void setOriginalVolume(int originalVolume) {
		this.mOriginalVolume = originalVolume;
	}
	
	public void setAudioDucked(boolean audioDucked) {
		this.mAudioDucked = audioDucked;
	}
	
	public void setTargetVolume(int targetVolume) {
		this.mTargetVolume = targetVolume;
	}
	
	public void setCurrentVolume(int currentVolume) {
		this.mCurrentVolume = currentVolume;
	}
	
	public void setStepDownIncrement(int stepDownIncrement) {
		this.mStepDownIncrement = stepDownIncrement;
	}
	
	public void setStepUpIncrement(int stepUpIncrement) {
		this.mStepUpIncrement = stepUpIncrement;
	}
	
}
