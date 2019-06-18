package com.justek.h265player;

import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by Peter on 2019/6/13.
 */

public class AudioPlayer {

	private AudioTrack mAudioTrack;

	private int mSampleRateInHz;
	private int mChannelConfig;
	private int mAudioFormat;

	public AudioPlayer(int sampleRateInHz, int channelConfig, int audioFormat){
		mSampleRateInHz = sampleRateInHz;
		mChannelConfig = channelConfig;
		mAudioFormat = audioFormat;
	}

	public void init() {
		if(mAudioTrack != null)
			release();
		int minBufSize = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				mSampleRateInHz, mChannelConfig, mAudioFormat, minBufSize, AudioTrack.MODE_STREAM);
		mAudioTrack.play();
	}

	public void release() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
		}
	}

	public void play(byte[] data,int offset,int size ){
		if(data == null && data.length == 0)
			return;
		mAudioTrack.write(data,offset,size);
	}


}
