package com.justek.h265player;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "H265Player";

	public static final String MOVIES_DIR = Environment.getExternalStorageDirectory().getPath() + "/Movies";

	private String[] fileList;
	private ListView lvFile;
	private ArrayAdapter arrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lvFile = findViewById(R.id.lvVideo);
		loadMediaFile();
	}

	public void loadMediaFile() {
		File file = new File(MOVIES_DIR);
		fileList = file.list();
		arrayAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,fileList);
		lvFile.setAdapter(arrayAdapter);
		lvFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startPlayVideo(position);
			}
		});
	}

	private void startPlayVideo(int position) {
		String path = MOVIES_DIR + "/" + fileList[position];
		Log.d(TAG, "startPlayVideo: path = " + path);
		Intent intent = new Intent(MainActivity.this,PlayActivity.class);
		intent.putExtra("path",path);
		startActivity(intent);
	}
}
