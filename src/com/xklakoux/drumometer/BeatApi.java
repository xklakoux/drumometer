package com.xklakoux.drumometer;

import com.musicg.api.DetectionApi;
import com.musicg.wave.WaveHeader;

/**
 * Api for detecting clap
 * 
 * @author Jacquet Wong
 * 
 */
public class BeatApi extends DetectionApi{
	
	public BeatApi(WaveHeader waveHeader) {
		super(waveHeader);
	}

	protected void init(){
		// settings for detecting a beat
		minFrequency = 1000.0f;
		maxFrequency = Double.MAX_VALUE;
		
		// get the decay part of a beat
		minIntensity = 10000.0f;
		maxIntensity = 100000.0f;
		
		minStandardDeviation = 0.0f;
		maxStandardDeviation = 0.05f;
		
		highPass = 100;
		lowPass = 10000;
		
		minNumZeroCross = 100;
		maxNumZeroCross = 500;
		
		numRobust = 4;
	}
		
	public boolean isBeat(byte[] audioBytes){
		return isSpecificSound(audioBytes);
	}
}