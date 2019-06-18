package com.justek.h265player.utils;

public class StringConvert {

	public static String SecToTime(int secondstr ){
		int hour,minute,second;
		if(secondstr >= 60){
			minute = secondstr / 60;
			second = secondstr % 60;
			if(minute > 60){
				hour = minute /60;
				minute = minute % 60;
				return (hour >= 10? hour:"0" + hour) + ":" + (minute >= 10? minute:"0" + minute) + ":" + (second >= 10? second:"0" + second);
			}else {

				return "00:" + (minute >= 10? minute:"0" + minute) + ":" + (second >= 10? second:"0" + second);
			}
		}else {
			return "00:00:" + (secondstr >= 10? secondstr:"0" + secondstr);
		}
	}
}