package com.justek.h265player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.justek.h265player.utils.StringConvert;

/**
 * Created by Peter on 2019/6/14.
 */

public class PlayActivity extends Activity {

	private static final String TAG = "PlayActivity";
	public static final int MSG_INFOBAR_DISMISS = 0;
	public static final int INFOBAR_SHOWTIME = 5000;

	private SurfaceView play_surfaceview;
	private AlertDialog infoBarDialog;
	final Integer[] playStateImage = { R.drawable.media_play_white, R.drawable.media_pause_white, R.drawable.media_forward_white, R.drawable.media_backward_white};
	private ImageView playState;
	private TextView playDuration,playName,currentTime;
	private ProgressBar playProgressbar;

	private MediaPlayerUtils mediaPlayerUtils;
	private boolean isplay = false;
	String path ;

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case MSG_INFOBAR_DISMISS:
					infoBarDialog.dismiss();
					break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreate:  " );
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		path = getIntent().getStringExtra("path");
		Log.d(TAG, "onCreate: path = " + path );

		play_surfaceview = (SurfaceView)findViewById(R.id.play_surfaceview);
		mediaPlayerUtils = new MediaPlayerUtils(path,play_surfaceview);
		initDialog();
		mediaPlayerUtils.setPlayCallBack(new MediaPlayerUtils.PlayCallBack() {
			@Override
			public void isFinish() {
				PlayActivity.this.finish();
			}

			@Override
			public void onProgress(final int progress) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						playDuration.setText(StringConvert.SecToTime(progress) + "/" + StringConvert.SecToTime(mediaPlayerUtils.getTotalDuration()));
						playProgressbar.setProgress(progress);
					}
				});
			}
		});
		mediaPlayerUtils.prepare();
		mediaPlayerUtils.play();
		isplay = true;
	}

	private void initDialog() {
		infoBarDialog = new AlertDialog.Builder(PlayActivity.this).create();
		infoBarDialog.show();
		handler.sendEmptyMessageDelayed(MSG_INFOBAR_DISMISS,INFOBAR_SHOWTIME);
		Window window = infoBarDialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setContentView(R.layout.player_infobar);
		window.getAttributes().windowAnimations = R.style.PauseDialogAnimation;
		playState = (ImageView) window.findViewById(R.id.vod_play_state);
		playDuration = (TextView) window.findViewById(R.id.vod_play_duration);
		playName = (TextView) window.findViewById(R.id.vod_play_name);
		currentTime = (TextView) window.findViewById(R.id.vod_current_time);
		playProgressbar = (ProgressBar) window.findViewById(R.id.vod_play_progressbar);
		playState.setBackgroundResource(playStateImage[0]);

		playDuration.setText(StringConvert.SecToTime(mediaPlayerUtils.getmCurPlayTime()) + "/" + StringConvert.SecToTime(mediaPlayerUtils.getTotalDuration()));
		playProgressbar.setProgress(0);
		playProgressbar.setMax(mediaPlayerUtils.getTotalDuration());
		playName.setText(path.substring(path.lastIndexOf("/") + 1,path.length()));
		infoBarDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				dispatchKeyEvent(event);
				return true;
			}
		});
	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch (event.getKeyCode()){
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					infoBarDialog.show();
					handler.removeMessages(MSG_INFOBAR_DISMISS);
					handler.sendEmptyMessageDelayed(MSG_INFOBAR_DISMISS,INFOBAR_SHOWTIME);
					break;
				case KeyEvent.KEYCODE_DPAD_CENTER:
					infoBarDialog.show();
					handler.removeMessages(MSG_INFOBAR_DISMISS);
					handler.sendEmptyMessageDelayed(MSG_INFOBAR_DISMISS,INFOBAR_SHOWTIME);
					if(isplay) {
						mediaPlayerUtils.pause();
						playState.setBackgroundResource(playStateImage[1]);
					}else {
						mediaPlayerUtils.play();
						playState.setBackgroundResource(playStateImage[0
								]);
					}
					isplay = !isplay;
					break;
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
