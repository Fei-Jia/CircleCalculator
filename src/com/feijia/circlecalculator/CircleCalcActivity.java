package com.feijia.circlecalculator;

import java.io.IOException;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class CircleCalcActivity extends ActionBarActivity {
    private StringBuffer DisplayBuffer=new StringBuffer();
    private DBAdapter db = new DBAdapter(this);
	private SharedPreferences appPrefs;
	private int Offset;
	private boolean modified;
	private Display display;
	private Point sizep;
	private int displaywidth;
	private boolean Radian;
	private boolean BtnSound;
	private Vibrator vb;
	private boolean Vibrate;
	private String digits;
	private String histories;
	private String Ans;
	private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_calc); 
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment())
                .commit();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
        	    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);    // So that keyboard hide and cursor still work
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Offset=-1;
        modified=false;
        vb = (Vibrator)   getSystemService(Context.VIBRATOR_SERVICE);
        setTitle("CircleCalc");
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    	Radian=appPrefs.getBoolean("RadianPref", false);
    	BtnSound=appPrefs.getBoolean("SoundPref", false);
    	histories=appPrefs.getString("HistoryPrefNum", "20");
    	digits=appPrefs.getString("DecimalDigits", "6");
    	Ans="";
    	if(vb.hasVibrator()) 
    		Vibrate=appPrefs.getBoolean("VibrationPref", false);
    	else
    		Vibrate=false;
        TextView resview=(TextView) findViewById(R.id.ResultsView);
        TextView EqualSign = (TextView) findViewById(R.id.ResultEqual);
        boolean Err = false;
    	db.open();
    	Cursor cc=db.returnEquation();
    	if (cc.getCount()>0&&cc.getColumnCount()>0){
    		cc.moveToFirst();
    		DisplayBuffer.delete(0, DisplayBuffer.length());
    		DisplayBuffer.append(cc.getString(1));
    		Offset=Integer.parseInt(cc.getString(2));
    		modified=Boolean.parseBoolean(cc.getString(3));
    		resview.setText(cc.getString(4));
    		Err=Boolean.parseBoolean((cc.getString(5)));
    		Ans=cc.getString(6);
    		if(cc.getString(4).length()>16) {
    			resview.setTextSize(27);
    			EqualSign.setTextSize(27);
    		}
    		if(Err) {
        		resview.setTextColor(Color.RED);
        		resview.setMaxLines(1);
        		resview.setTextSize(22);
        	}
    		else {
    			if(cc.getString(4).length()>0) EqualSign.setText(" = ");
    		}
    	}
    	db.close();
    	setDisplay();
		
    	display = getWindowManager().getDefaultDisplay();
    	sizep = new Point();
    	display.getSize(sizep);
    	displaywidth = sizep.x;
    	LinearLayout vie = (LinearLayout)findViewById(R.id.Normal);
    	vie.getLayoutParams().height = displaywidth;
    	//Toast.makeText(this, "width: "+displaywidth+" Height: "+displayheight, Toast.LENGTH_LONG).show();
    	checkFirstRun();
    	
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
       super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
       super.onRestoreInstanceState(savedState);
    }
    
    @Override
    protected void onPause() {
      super.onPause();
      TextView resview=(TextView) findViewById(R.id.ResultsView);
      String equationdis = DisplayBuffer.toString();
      String offset = Integer.toString(Offset);
      String modify = Boolean.toString(modified);
      String answer=resview.getText().toString();
      String err;
      	  if(resview.getCurrentTextColor()==Color.RED){
      		  err="true";
      	  }  
      	  else
      		  err="false";
      db.open();
      	  db.DeleteLast();
      	  db.preserveEquation(equationdis, offset, modify, answer, err, Ans);
      db.close();
    }
    
    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
            // Place your dialog code here to display the dialog
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
              .edit()
              .putBoolean("isFirstRun", false)
              .apply();
            Intent i = new Intent("com.feijia.TutorialActivity"); 
        	startActivity(i);
        }
    }
    
    public void ShowHistory() { 
        AlertDialog.Builder recentlist = new AlertDialog.Builder(this);
        recentlist.setTitle("Recent History");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
        		CircleCalcActivity.this,
                android.R.layout.select_dialog_singlechoice);
        db.open();
        Cursor c = db.getAllEquationHistorys(histories);
        if (c.moveToFirst())
        {
        	do { 
        		arrayAdapter.add(c.getString(1));
        	} 	
        	while (c.moveToNext()); 
        }
        db.close();
        recentlist.setNegativeButton("Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	dialog.dismiss();
                }
        });
        recentlist.setAdapter(arrayAdapter,
        	new DialogInterface.OnClickListener() {
        		@Override
                public void onClick(DialogInterface dialog, int which) {
        			boolean dbinserted=false;
        			
        			if(Offset<=-1){
        				db.open();
        				if(DisplayBuffer.length()>0&&modified) 
        					db.insertEquationHistory(DisplayBuffer.toString()); //save screen
        				db.close();
        				dbinserted=true;
        	    	}
        			if(dbinserted) Offset=which+1;
        			else Offset=which;
        			String equationdisplay = arrayAdapter.getItem(which);
        			if(equationdisplay!="") modified=false;
                    DisplayBuffer.delete(0, DisplayBuffer.length());
                 	DisplayBuffer.append(equationdisplay);
                	setDisplay();
                    DataBean resb = Calculate.calc(equationdisplay, Integer.parseInt(digits), Radian);
            		DisplayResult(resb.data, resb.isErr);
                    }
                });
        recentlist.show();
    }

    protected void ShowPrevEquation(){
    	db.open();
    	if(db.getHistoryCount()==0) {
    		db.close();
    		return;
    	}
    	if(Offset<=-1){
    		if(DisplayBuffer.length()>0&&modified) {
    			db.insertEquationHistory(DisplayBuffer.toString());
    			modified=false;
    			++Offset;
    		}
    	}
    	if(Offset<db.getHistoryCount()-1)
    		++Offset;
    	else{
    		db.close();
    		return;
    	}
        Cursor c = db.getEquationOffset(Offset);
        if (c.moveToFirst())
        {
        	if(c.getString(1)!="") modified=false;
        	DisplayBuffer.delete(0, DisplayBuffer.length());
         	DisplayBuffer.append(c.getString(1));
        	setDisplay();
            DataBean resb = Calculate.calc(c.getString(1), Integer.parseInt(digits), Radian);
    		DisplayResult(resb.data, resb.isErr);
        }
        db.close();
    }
    
    protected void ShowNextEquation(){
    	if(Offset>0) {
    		--Offset;
    	}
    	else return;
    	db.open();
        Cursor c = db.getEquationOffset(Offset);
        if (c.moveToFirst())
        {
        	if(c.getString(1)!="") modified=false;
        	DisplayBuffer.delete(0, DisplayBuffer.length());
         	DisplayBuffer.append(c.getString(1));
        	setDisplay();
            DataBean resb = Calculate.calc(c.getString(1), Integer.parseInt(digits), Radian);
    		DisplayResult(resb.data, resb.isErr);
        }
        db.close();
    }
    
    protected void AnsOnView(){
    	modified=true;
    	Offset=-1;
        if(Vibrate) vb.vibrate(30);
    	if(BtnSound){
    		MediaPlayer clicksound = MediaPlayer.create(this, R.raw.buttonclick);
    		try {
    			clicksound.prepare();
    		} catch (IllegalStateException|IOException e) {
    			e.printStackTrace();
    		}
    		clicksound.start();		
    		clicksound.setOnCompletionListener(new OnCompletionListener() {
    			public void onCompletion(MediaPlayer clicksound) {
    				if(clicksound!=null) {
    					if(clicksound.isPlaying())
    						clicksound.stop();
    					clicksound.reset();
    					clicksound.release();
    					clicksound=null;
    				}
    			};
    		});
    	}
    	TextView AnsView = (TextView) findViewById(R.id.ResultsView);
    	EditText eqa = (EditText) findViewById(R.id.EquationView);
    	int p = eqa.getSelectionEnd();
    	DisplayBuffer.insert(p, Ans);
		setDisplay();
		eqa.setSelection(p+Ans.length());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.circle_calc, menu);
        return true;
    }
    
    // Call to share app
    /*private void ShareMe() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
            "Hey, check out this cool app at: https://play.google.com/store/apps/details?id=com.feijia.circlecalculator");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
        	case R.id.action_settings: 
        		SettingsClick();
        		return true;
        	case R.id.recent_history: 
        		ShowHistory();
        		return true;
        	case R.id.prev_equation: 
        		ShowPrevEquation();
        		return true;
        	case R.id.next_equation: 
        		ShowNextEquation();
        		return true;
        	case R.id.prev_ans: 
        		AnsOnView();
        		return true;
        	case R.id.action_tutorial: 
        		Intent i = new Intent("com.feijia.TutorialActivity"); 
            	startActivity(i);
        		return true;
        	case R.id.action_rate: 
        		Rate5Stars();
        		return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void SettingsClick(){
    	Intent i = new Intent("com.feijia.CalcPreferenceActivity"); 
    	startActivity(i);
    }
    
    protected void Rate5Stars(){
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse("market://details?id=" + getPackageName()));
    	//intent.setData(Uri.parse("amzn://apps/android?p=" + getPackageName()));
    	startActivity(intent);
    }
    
    public void OnEqualsClick(View view){
    	EditText edt = (EditText)findViewById(R.id.EquationView);
    	String equation = edt.getText().toString();
    	if(equation=="") return;
        DataBean resb = Calculate.calc(equation, Integer.parseInt(digits), Radian);
        if(!resb.isErr)
        	Ans=resb.data;
		DisplayResult(resb.data, resb.isErr);
    }
    	
    public void DisplayResult(String ResultDis, boolean isError){
    	TextView DisplayAns = (TextView) findViewById(R.id.ResultsView);
    	TextView EqualSign = (TextView) findViewById(R.id.ResultEqual);
    	if(isError) {
    		DisplayAns.setTextColor(Color.RED);
    		DisplayAns.setMaxLines(1);
    		DisplayAns.setTextSize(22);
    		EqualSign.setText(" ");
    	}
    	else {
    		DisplayAns.setTextColor(Color.BLACK);
    		DisplayAns.setMaxLines(10);
    		if(ResultDis.length()>16) {
    			DisplayAns.setTextSize(27);
    			EqualSign.setTextSize(27);
    			EqualSign.setText(" =");
    		}
    		else{
    			DisplayAns.setTextSize(35);
    			EqualSign.setTextSize(35);
    			EqualSign.setText(" = ");
    		}
    	}
		DisplayAns.setText(ResultDis);
    }
    
    public void setDisplay() {
        	TextView DisplayLCD = (TextView) findViewById(R.id.EquationView);
        	DisplayLCD.setText(DisplayBuffer.toString());
    }
    
    public void OnNumClick(View v){
    	modified=true;
    	Offset=-1;
        if(Vibrate) vb.vibrate(30);
    	if(BtnSound){
    		MediaPlayer clicksound = MediaPlayer.create(this, R.raw.buttonclick);
    		try {
    			clicksound.prepare();
    		} catch (IllegalStateException|IOException e) {
    			e.printStackTrace();
    		}
    		clicksound.start();		
    		clicksound.setOnCompletionListener(new OnCompletionListener() {
    			public void onCompletion(MediaPlayer clicksound) {
    				if(clicksound!=null) {
    					if(clicksound.isPlaying())
    						clicksound.stop();
    					clicksound.reset();
    					clicksound.release();
    					clicksound=null;
    				}
    			};
    		});
    	}
    	TextView AnsView = (TextView) findViewById(R.id.ResultsView);
    	EditText eqa = (EditText) findViewById(R.id.EquationView);
    	int p = eqa.getSelectionEnd();
    	int s = eqa.getSelectionStart();
    	int temp=p+1;
    	switch (v.getId()){
    	case R.id.ButtonN1 : 
    		DisplayBuffer.insert(p, "1");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN2 : 
    		DisplayBuffer.insert(p, "2");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN3 : 
    		DisplayBuffer.insert(p, "3");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN4 : 
    		DisplayBuffer.insert(p, "4");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN5 : 
    		DisplayBuffer.insert(p, "5");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN6 : 
    		DisplayBuffer.insert(p, "6");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN7 : 
    		DisplayBuffer.insert(p, "7");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN8 : 
    		DisplayBuffer.insert(p, "8");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN9 : 
    		DisplayBuffer.insert(p, "9");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN0 : 
    		DisplayBuffer.insert(p, "0");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	// 2d set of N0-N9
    	case R.id.ButtonN11 : 
    		DisplayBuffer.insert(p, "1");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN22 : 
    		DisplayBuffer.insert(p, "2");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN33 : 
    		DisplayBuffer.insert(p, "3");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN44 : 
    		DisplayBuffer.insert(p, "4");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN55 : 
    		DisplayBuffer.insert(p, "5");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN66 : 
    		DisplayBuffer.insert(p, "6");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN77 : 
    		DisplayBuffer.insert(p, "7");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN88 : 
    		DisplayBuffer.insert(p, "8");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN99 : 
    		DisplayBuffer.insert(p, "9");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonN00 : 
    		DisplayBuffer.insert(p, "0");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	// End of 2nd set of N0-N9
    	case R.id.ButtonMultiply : 
    		DisplayBuffer.insert(p, "×");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonDivide : 
    		DisplayBuffer.insert(p, "/");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonPlus : 
    		DisplayBuffer.insert(p, "+");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonMinus : 
    		DisplayBuffer.insert(p, "-");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonDot : 
    		DisplayBuffer.insert(p, ".");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	// Dot of 2nd page
    	case R.id.ButtonDotDot : 
    		DisplayBuffer.insert(p, ".");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonParS : 
    		DisplayBuffer.insert(p, "(");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonParF : 
    		DisplayBuffer.insert(p, ")");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonPower : 
    		DisplayBuffer.insert(p, "^()");
    		setDisplay();
    		eqa.setSelection(temp+1);
    		break;
    	case R.id.ButtonDel : 
    		if(p!=0){
    			if(s!=p){
    				DisplayBuffer.delete(s, p);
    				setDisplay();
        			eqa.setSelection(s);
    			}
    			else {
    				DisplayBuffer.deleteCharAt(p-1);
    				setDisplay();
        			eqa.setSelection(temp-2);
    			}	
    		}
    		break;
    	//Delete of 2nd Page
    	case R.id.ButtonDelDel : 
    		if(p!=0){
    			if(s!=p){
    				DisplayBuffer.delete(s, p);
    				setDisplay();
        			eqa.setSelection(s);
    			}
    			else {
    				DisplayBuffer.deleteCharAt(p-1);
    				setDisplay();
        			eqa.setSelection(temp-2);
    			}	
    		}
    		break;
    	case R.id.ButtonExp : 
    		DisplayBuffer.insert(p, "E");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonSqrt : 
    		DisplayBuffer.insert(p, "√()");
    		setDisplay();
    		eqa.setSelection(temp+1);
    		break;
    	case R.id.ButtonPercent : 
    		DisplayBuffer.insert(p, "%");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	// Function Buttons on 2nd Page
    	case R.id.ButtonSin : 
    		if(!Radian){
    			DisplayBuffer.insert(p, "sin(°)");
        		setDisplay();
        		eqa.setSelection(temp+3);
    		}
    		else{
    			DisplayBuffer.insert(p, "sin()");
        		setDisplay();
        		eqa.setSelection(temp+3);
    		}
    		break;
    	case R.id.ButtonCos : 
    		if(!Radian){
    			DisplayBuffer.insert(p, "cos(°)");
        		setDisplay();
        		eqa.setSelection(temp+3);
    		}
    		else{
    			DisplayBuffer.insert(p, "cos()");
        		setDisplay();
        		eqa.setSelection(temp+3);
    		}
    		break;
    	case R.id.ButtonTan : 
    		if(!Radian){
    			DisplayBuffer.insert(p, "tan(°)");
        		setDisplay();
        		eqa.setSelection(temp+3);
    		}
    		else{
    			DisplayBuffer.insert(p, "tan()");
        		setDisplay();
        		eqa.setSelection(temp+3);
    		}
    		break;
    	case R.id.ButtonAsin : 
    		DisplayBuffer.insert(p, "asin()");
        	setDisplay();
        	eqa.setSelection(temp+4);
    		break;
    	case R.id.ButtonAcos : 
    		DisplayBuffer.insert(p, "acos()");
        	setDisplay();
        	eqa.setSelection(temp+4);
    		break;
    	case R.id.ButtonAtan : 
    		DisplayBuffer.insert(p, "atan()");
        	setDisplay();
        	eqa.setSelection(temp+4);
    		break;
    	case R.id.ButtonLn : 
    		DisplayBuffer.insert(p, "ln()");
    		setDisplay();
    		eqa.setSelection(temp+2);
    		break;
    	case R.id.ButtonPie : 
    		DisplayBuffer.insert(p, "π");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonLg10 : 
    		DisplayBuffer.insert(p, "lg10()");
    		setDisplay();
    		eqa.setSelection(temp+4);
    		break;
    	case R.id.ButtonCbrt : 
    		DisplayBuffer.insert(p, "\u00B3\u221A()");
    		setDisplay();
    		eqa.setSelection(temp+2);
    		break;
    	case R.id.Buttonee : 
    		DisplayBuffer.insert(p, "e");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ButtonFact : 
    		DisplayBuffer.insert(p, "!");
    		setDisplay();
    		eqa.setSelection(temp);
    		break;
    	case R.id.ResultsView :
    		DisplayBuffer.insert(p, AnsView.getText());
    		setDisplay();
    		eqa.setSelection(temp+AnsView.getText().length()-1);
    		break;
    	case R.id.ResultEqual :
    		DisplayBuffer.insert(p, AnsView.getText());
    		setDisplay();
    		eqa.setSelection(temp+AnsView.getText().length()-1);
    		break;
		}
    }
    
    public void Shift (View v){
    	LinearLayout NormalLayout=(LinearLayout)this.findViewById(R.id.Normal);
    	NormalLayout.setVisibility(LinearLayout.GONE);
    	LinearLayout ShiftedLayout=(LinearLayout)this.findViewById(R.id.Shifted);
    	ShiftedLayout.setVisibility(LinearLayout.VISIBLE);
    	ShiftedLayout.getLayoutParams().height = displaywidth;
    }
    
    public void UnShift (View v){
    	LinearLayout NormalLayout=(LinearLayout)this.findViewById(R.id.Normal);
    	NormalLayout.setVisibility(LinearLayout.VISIBLE);
    	LinearLayout ShiftedLayout=(LinearLayout)this.findViewById(R.id.Shifted);
    	ShiftedLayout.setVisibility(LinearLayout.GONE);
    	NormalLayout.getLayoutParams().height = displaywidth;
    }
    
    public void OnClearClick(View v){
    	TextView DisplayAnswerClear = (TextView) findViewById(R.id.ResultsView);
    	db.open();
        	if(DisplayBuffer.length()>0&&modified) 
        		db.insertEquationHistory(DisplayBuffer.toString()); 
        db.close();
        DisplayBuffer.delete(0, DisplayBuffer.length());
        EditText Clear1 = (EditText) findViewById(R.id.EquationView);
        Clear1.setText(DisplayBuffer);
		TextView EqualsSignClear = (TextView) findViewById(R.id.ResultEqual);
		DisplayAnswerClear.setText("");
		DisplayAnswerClear.setTextColor(Color.BLACK);
		DisplayAnswerClear.setTextSize(35);
		EqualsSignClear.setTextSize(35);
		EqualsSignClear.setText(" ");
		Offset=-1;  // point to new and empty screen;
		modified=false;
    }
    
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_circle_calc, container, false);
            return rootView;
        }
    }
}