package com.justek.h265player;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.justek.h265player.utils.DecodeThread;

/**
 * Created by Peter on 2019/6/14.
 */

public class MediaPlayerUtils implements SurfaceHolder.Callback {

	private static final String TAG = "MediaPlayerUtils";

	private DecodeThread videoDecodeThread;
	private DecodeThread audioDecodeThread;
	private Thread timerThread ;

	public static boolean isCreate = false;
	private SurfaceView surfaceView ;
	private String path;
	private int totalDuration;
	private int mCurPlayTime = 0;
	private boolean isPLay = false;
	private PlayCallBack playCallBack;

	public void setPlayCallBack(PlayCallBack playCallBack) {
		this.playCallBack = playCallBack;
	}

	public interface PlayCallBack{
		void isFinish();
		void onProgress(int progress);
	}

	public int getTotalDuration() {
		return totalDuration;
	}

	public int getmCurPlayTime() {
		return mCurPlayTime;
	}

	public MediaPlayerUtils(String path, SurfaceView surfaceView){
		this.surfaceView = surfaceView;
		this.path = path;
		surfaceView.getHolder().addCallback(this);
		totalDuration = getPlayTime(path);
		Log.d(TAG, "MediaPlayerUtils: totalDuration = " + totalDuration);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "surfaceCreated: ");
		isCreate = true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.e(TAG, "surfaceChanged: ");
		if(videoDecodeThread == null){
			videoDecodeThread = new VideoDecodeThread(path,holder.getSurface());
			videoDecodeThread.start();
		}

		if(audioDecodeThread == null){
			audioDecodeThread = new AudioDecodeThread(path);
			audioDecodeThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "surfaceDestroyed: ");
		isCreate = false;
		if (videoDecodeThread != null) {
			videoDecodeThread.interrupt();
		}
		if (audioDecodeThread != null) {
			audioDecodeThread.interrupt();
		}
	}

	public void prepare(){
		Log.d(TAG, "prepare: ");
		videoDecodeThread = new VideoDecodeThread(path,surfaceView.getHolder().getSurface());
		audioDecodeThread = new AudioDecodeThread(path);
		timerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					if(!isPLay)
						continue;
					Log.d(TAG, "run: " + mCurPlayTime);
					try {
						playCallBack.onProgress(mCurPlayTime);
						Thread.sleep(1000);
						mCurPlayTime++;
						if(mCurPlayTime >= totalDuration) 	//when play is finish ,call back to PlayActivity
							playCallBack.isFinish();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		videoDecodeThread.start();
		audioDecodeThread.start();
		timerThread.start();
	}

	public void play(){
		Log.d(TAG, "start: ");
		isPLay = true;
		videoDecodeThread.play();
		audioDecodeThread.play();
	}

	public void pause(){
		Log.d(TAG, "pause: ");
		isPLay = false;
		videoDecodeThread.pause();
		audioDecodeThread.pause();
	}

	private int getPlayTime(String path)
	{
		Log.d(TAG, "getPlayTime: " + path);
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(path);
			return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;//时长(毫秒)
		}catch (Exception ex){
			Log.e(TAG, "MediaMetadataRetriever exception " + ex);
		} finally {
			mmr.release();
		}
		return 0;
	}
}
