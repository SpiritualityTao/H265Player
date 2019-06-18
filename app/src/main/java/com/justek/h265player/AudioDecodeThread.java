package com.justek.h265player;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.justek.h265player.utils.DecodeThread;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Peter on 2019/6/13.
 */

public class AudioDecodeThread extends DecodeThread {

	private static final String TAG = "AudioDecodeThread";

	private String path;
	private AudioPlayer mPlayer;
	private MediaCodec mediaCodec;
	private boolean isPlay = false;

	public AudioDecodeThread(String path){
		this.path = path;
	}

	@Override
	public void run() {
		Log.d(TAG, "run: ");
		MediaExtractor audioExtractor = new MediaExtractor();
		try {
			audioExtractor.setDataSource(path);		//set data source
		} catch (IOException e) {
			e.printStackTrace();
		}

		String mimeType;
		for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
			MediaFormat format = audioExtractor.getTrackFormat(i);
			mimeType = format.getString(MediaFormat.KEY_MIME);
			if (mimeType.startsWith("audio/")) { // audio track
				audioExtractor.selectTrack(i); // select the audio track
				try {
					mediaCodec = MediaCodec.createDecoderByType(mimeType); // create decoder,for data output
				} catch (IOException e) {
					e.printStackTrace();
				}
				mediaCodec.configure(format, null, null, 0);
				mPlayer = new AudioPlayer(format.getInteger(MediaFormat.KEY_SAMPLE_RATE), AudioFormat
						.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
				mPlayer.init();
				break;
			}
		}
		if (mediaCodec == null)
			return;

		mediaCodec.start();
		ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers(); // data source   input
		ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers(); // Decoded data   output

		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo(); // 用于描述解码得到的byte[]数据的相关信息
		boolean bIsEos = false;
		long startMs = System.currentTimeMillis();
		isPlay = true;
		// start decoding !!!!!!!!
		while (!Thread.interrupted()) {
			if(!isPlay)
				continue;
			if (!bIsEos) {
				int inIndex = mediaCodec.dequeueInputBuffer(0);
				if (inIndex >= 0) {
					ByteBuffer buffer = inputBuffers[inIndex];
					int nSampleSize = audioExtractor.readSampleData(buffer, 0); // 读取一帧数据至buffer中
					if (nSampleSize < 0) {
						Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
						mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						bIsEos = true;
					} else {
						// 填数据
						mediaCodec.queueInputBuffer(inIndex, 0, nSampleSize, audioExtractor.getSampleTime(), 0); // 通知MediaDecode解码刚刚传入的数据
						audioExtractor.advance(); // 继续下一取样
					}
				}
			}

			int outIndex = mediaCodec.dequeueOutputBuffer(info, 0);
			switch (outIndex) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
					outputBuffers = mediaCodec.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					Log.d(TAG, "New format " + mediaCodec.getOutputFormat());
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					Log.d(TAG, "dequeueOutputBuffer timed out!");
					break;
				default:
					ByteBuffer buffer = outputBuffers[outIndex];
					Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);

					while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
						try {
							sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
					//用来保存解码后的数据
					byte[] outData = new byte[info.size];
					buffer.get(outData);
					//清空缓存
					buffer.clear();
					//播放解码后的数据
					mPlayer.play(outData, 0, info.size);
					mediaCodec.releaseOutputBuffer(outIndex, true);
					break;
			}

			// All decoded frames have been rendered, we can stop playing
			// now
			if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				break;
			}
		}
		mediaCodec.stop();
		mediaCodec.release();
		audioExtractor.release();
		mPlayer.release();
	}

	@Override
	public void play() {
		isPlay = true;
	}
	@Override
	public void pause() {
		isPlay = false;
	}


}
