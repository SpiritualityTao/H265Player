package com.justek.h265player.utils;

/**
 * Created by Peter on 2019/6/15.
 */

public abstract class DecodeThread extends Thread {

	@Override
	public void run() {
		super.run();
	}

	public abstract void play() ;

	public abstract void pause();
}
