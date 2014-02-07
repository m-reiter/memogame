package cousera.androidapps101.memogame;

import java.util.Random;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	// number of players
	private final int nPlayers = 2;
	// number of playing fields
	private final int nFields = 16;
	
	// image resource ids
	private int[] mImages;

	// handles on and ids of UI elements
	private TextView[] tvPlayers;
	private TextView[] tvScores;
	private int[] mIds;
	private ImageButton[] mButtons;

	// game state variables
	private int[] mPositions;
	private int[] mScores;
	private int mOpen;
	private int isPlaying;
	
	// misc helpers
	private Random random;
	private boolean pausing = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		int i;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tvPlayers = new TextView[nPlayers];
		tvScores = new TextView[nPlayers];
		
		tvPlayers[0] = (TextView) findViewById(R.id.p1Name);
		tvPlayers[1] = (TextView) findViewById(R.id.p2Name);
		tvScores[0] = (TextView) findViewById(R.id.p1Score);
		tvScores[1] = (TextView) findViewById(R.id.p2Score);
		
		Resources res = getResources();
		TypedArray images = res.obtainTypedArray(R.array.images);
		
		mImages = new int[nFields/2];
		for (i = 0; i < nFields/2; i++) {
			mImages[i] = images.getResourceId(i, -1);
		}
		images.recycle();
		
		TypedArray buttons = res.obtainTypedArray(R.array.buttons);
		
		mIds = new int[nFields];
		mButtons = new ImageButton[nFields];
		for (i = 0; i < nFields; i++) {
			mIds[i] = buttons.getResourceId(i, -1);
			mButtons[i] = (ImageButton) findViewById(mIds[i]);
		}
		buttons.recycle();
		
		mScores = new int[nPlayers];
		mPositions = new int[nFields];
		
		random = new Random();
		
		init();
	}
	
	private void init() {
		
		int i,j,temp;
		
		for (i = 0; i < nFields; i++) {
			// initialise ordered positions
			mPositions[i] = i/2;
			// reset button states
			mButtons[i].setVisibility(View.VISIBLE);
			mButtons[i].setImageResource(R.drawable.background);
			mButtons[i].setEnabled(true);
		}
		// shuffle the playing deck
		for (i = 0; i < nFields; i++) {
			j = random.nextInt(nFields - i);
			temp = mPositions[j];
			mPositions[j] = mPositions[nFields - i - 1];
			mPositions[nFields - i - 1] = temp;
		}
		// reset scores
		for (i=0; i < nPlayers; i++) {
			mScores[i] = 0;
		}
		
		// no image opened yet
		mOpen = -1;
		// Player 1's turn
		isPlaying = 0;
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
	
	public void play(View v) {
		// button clicked
		
		// we're displaying a result right now, ignore clicks
		if (pausing) return;

		int pos = -1;
		
		for (pos = 0; pos < nFields; pos++) {
			if (v.getId() == mIds[pos]) break;
		}
		
		mButtons[pos].setImageResource(mImages[mPositions[pos]]);
		mButtons[pos].setEnabled(false);

		if (mOpen == -1) {
			// first image revealed
			mOpen = pos;
		} else {
			// second image opened
			if (mPositions[pos] == mPositions[mOpen]) {
				// matching pair found
				flash(true);
				mScores[isPlaying]++;
				tvScores[isPlaying].setText(""+mScores[isPlaying]);
			} else {
				// images are not matching
				pausing = true;
				flash(false);
				
				final int pos1 = pos;
				final int pos2 = mOpen;
				mButtons[pos].postDelayed(new Runnable() {
					public void run() {
				mButtons[pos1].setImageResource(R.drawable.background);
				mButtons[pos1].setEnabled(true);
				mButtons[pos2].setImageResource(R.drawable.background);
				mButtons[pos2].setEnabled(true);
				pausing = false;
					}
				},500);
			}
			mOpen = -1;
		}
	}

private void flash(boolean matching) {
	int color;
	
	View mainLayout = findViewById(R.id.mainLayout);
	// flash screen and pause
	if (matching) {
		color = Color.argb(0xff, 0x00, 0xff, 0x00);
	} else {
		color = Color.argb(0xff, 0xff, 0x00, 0x00);
	}
	mainLayout.post(new ColorChanger(mainLayout,color));
	mainLayout.postDelayed(new ColorChanger(mainLayout,Color.argb(0xff, 0x00, 0x80, 0x40)),200);
	}
	
}

class ColorChanger implements Runnable {

	private View mView;
	private int mColor;
	
	public ColorChanger(View v, int color) {
		mView = v;
		mColor = color;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mView.setBackgroundColor(mColor);
	}
	
}

