package com.justek.h265player;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.justek.h265player.utils.DecodeThread;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.MIMETYPE_VIDEO_HEVC;

/**
 * Created by Peter on 2019/6/13.
 */

public class VideoDecodeThread extends DecodeThread {

	private static final String TAG = "VideoDecodeThread";

	private MediaCodec mediaCodec;
	private String path;
	private Surface surface;
	private boolean isPlay = false;

	public VideoDecodeThread(String path,Surface surface){
		this.path = path;
		this.surface = surface;
	}

	@Override
	public void run() {
		Log.d(TAG, "run: ");
		MediaExtractor mediaExtractor = new MediaExtractor();
		try {
			mediaExtractor.setDataSource(path); // 设置数据源
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String mimeType = null;
		for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
			MediaFormat format = mediaExtractor.getTrackFormat(i);
			mimeType = format.getString(MediaFormat.KEY_MIME);
			if (mimeType.startsWith("video/")) { // video track
				mediaExtractor.selectTrack(i); // set video track
				try {
					mediaCodec = MediaCodec.createDecoderByType(MIMETYPE_VIDEO_HEVC); // create video decode
				} catch (IOException e) {
					e.printStackTrace();
				}

				//when surfaceView is created finishlly,configurate mediaCodec
				while (!MediaPlayerUtils.isCreate){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				mediaCodec.configure(format, surface, null, 0);
				break;
			}

		}
		if (mediaCodec == null) {
			Log.e(TAG, "Can't find video info!");
			return;
		}

		mediaCodec.start(); // start MediaCodec
		// 输入
		ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers(); // 用来存放目标文件的数据
		// 输出
		ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers(); // 解码后的数据
		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo(); // 用于描述解码得到的byte[]数据的相关信息
		boolean bIsEos = false;
		long startMs = System.currentTimeMillis();
		// start decoding
		while (!Thread.interrupted()) {
			if(!isPlay)
				continue;
			if (!bIsEos) {
				int inIndex = mediaCodec.dequeueInputBuffer(0);
				if (inIndex >= 0) {
					ByteBuffer buffer = inputBuffers[inIndex];
					int nSampleSize = mediaExtractor.readSampleData(buffer, 0); // 读取一帧数据至buffer中
					if (nSampleSize < 0) {
						Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
						mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						bIsEos = true;
					} else {
						// 填数据
						mediaCodec.queueInputBuffer(inIndex, 0, nSampleSize, mediaExtractor.getSampleTime(), 0); // 通知MediaDecode解码刚刚传入的数据
						mediaExtractor.advance(); // 继续下一取样
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

					//防止视频播放过快
					while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
						try {
							sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
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
		mediaExtractor.release();
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
