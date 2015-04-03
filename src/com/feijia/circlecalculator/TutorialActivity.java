package com.feijia.circlecalculator;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TutorialActivity extends Activity {
	private int displaywidth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Display display = getWindowManager().getDefaultDisplay();
    	Point sizep = new Point();
    	display.getSize(sizep);
    	displaywidth = sizep.x;
    	LinearLayout TB = (LinearLayout)findViewById(R.id.TutorialB);
    	LinearLayout T110 = (LinearLayout)findViewById(R.id.onetenth);
    	LinearLayout T310 = (LinearLayout)findViewById(R.id.threetenth);
    	TB.getLayoutParams().height = displaywidth*3/5;
    	T110.getLayoutParams().width = displaywidth*1/18;
    	T310.getLayoutParams().width = displaywidth*3/15;
    	//Toast.makeText(this, "width: "+displaywidth, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tutorial, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
