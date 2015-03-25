package com.huge.puzzle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huge.view.GameLayout;
import com.huge.view.GameLayout.GamePuzzleListener;

public class MainActivity extends ActionBarActivity
{
	
	private GameLayout mGameLayout;
	
	private TextView mLevel;
	private TextView mTime;
	
	private Button mStart;
	//private Button mPause;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//mStart = (Button)findViewById(R.id.id_start);
		//mPause = (Button)findViewById(R.id.id_pause);
		mLevel = (TextView)findViewById(R.id.id_level);
		mTime = (TextView)findViewById(R.id.id_time);
		mGameLayout = (GameLayout)findViewById(R.id.id_game);
		mGameLayout.setTimeEnabled(true);
		/*mStart.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v)
			{
				mGameLayout.setTimeEnabled(true);
			}
			
		});*/
		
		/*mPause.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onPause();
			}
		});*/
	
		mGameLayout.setOnGamePuzzleListener(new GamePuzzleListener()
		{
			
			@Override
			public void timeChanged(int currentTime)
			{
				mTime.setText("" + currentTime);
			}
			
			@Override
			public void nextLevel(final int nextLevel)
			{
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("Game Info").setMessage("LEVEL UP !!!")
				.setPositiveButton("NEXT LEVEL", new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						mGameLayout.nextLevel();
						mLevel.setText("" + nextLevel);
					}
				}).show();
			}
			
			@Override
			public void gameOver()
			{
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("Game Info").setMessage("Game over !!!")
				.setPositiveButton("RESTART", new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,
							int which)
					{
						mGameLayout.restart();
					}
				}).setNegativeButton("QUIT",new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						finish();
					}
				}).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	protected void onPause()
	{
		super.onPause();
		mGameLayout.pause();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mGameLayout.resume();
	}
	
}
