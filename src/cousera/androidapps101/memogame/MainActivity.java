package cousera.androidapps101.memogame;

import java.util.Random;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
	private int discovered;
	private boolean finished;

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

		mImages = new int[nFields / 2];
		for (i = 0; i < nFields / 2; i++) {
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

		int i, j, temp;

		for (i = 0; i < nFields; i++) {
			// initialise ordered positions
			mPositions[i] = i / 2;
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
		for (i = 0; i < nPlayers; i++) {
			mScores[i] = 0;
			tvScores[i].setText("0");
		}
		
		tvPlayers[0].setVisibility(View.VISIBLE);
		tvScores[0].setVisibility(View.VISIBLE);
		tvPlayers[1].setVisibility(View.GONE);
		tvScores[1].setVisibility(View.GONE);
		

		// no image opened yet
		mOpen = -1;
		// no pairs discovered
		discovered = 0;
		// Player 1's turn
		isPlaying = 0;
		
		finished = false;
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

	public void play(View v) {
		// button clicked
		
		// we're displaying a result right now, ignore clicks
		if (pausing)
			return;

		int pos = -1;

		for (pos = 0; pos < nFields; pos++) {
			if (v.getId() == mIds[pos])
				break;
		}

		mButtons[pos].setImageResource(mImages[mPositions[pos]]);
		mButtons[pos].setEnabled(false);
		mButtons[pos].post(new Scaler((View) mButtons[pos], 1.0f, 1.1f, 250));
		mButtons[pos].postDelayed(new Scaler((View) mButtons[pos], 1.1f, 1.0f,
				250), 250);

		if (mOpen == -1) {
			// first image revealed
			mOpen = pos;
		} else {
			// second image opened
			if (mPositions[pos] == mPositions[mOpen]) {
				// matching pair found
				flash(true);
				mScores[isPlaying]++;
				tvScores[isPlaying].setText("" + mScores[isPlaying]);
				discovered++;
				if (discovered == (nFields + 1) / 2)
					endGame();
			} else {
				// images are not matching
				pausing = true;
				flash(false);

				Animation outAnimation = AnimationUtils.makeOutAnimation(this,
						(isPlaying == 1));
				outAnimation.setDuration(500);
				Animation inAnimation = AnimationUtils.makeInAnimation(this,
						(isPlaying == 1));
				inAnimation.setDuration(500);

				tvPlayers[isPlaying].startAnimation(outAnimation);
				tvScores[isPlaying].startAnimation(outAnimation);
				tvPlayers[isPlaying].setVisibility(View.GONE);
				tvScores[isPlaying].setVisibility(View.GONE);

				isPlaying = (isPlaying + 1) % nPlayers;

				tvPlayers[isPlaying].startAnimation(inAnimation);
				tvScores[isPlaying].startAnimation(inAnimation);
				tvPlayers[isPlaying].setVisibility(View.VISIBLE);
				tvScores[isPlaying].setVisibility(View.VISIBLE);

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
				}, 500);
			}
			mOpen = -1;
		}
	}
	
	public void restart(View v) {
		if (!finished) return;
		init();
	}

	private void endGame() {

		String result;
		if (mScores[0] > mScores[1]) {
			result = String.format(getString(R.string.won),
					getString(R.string.player_1), mScores[0], mScores[1]);
			tvPlayers[0].setVisibility(View.VISIBLE);
			tvPlayers[1].setVisibility(View.GONE);
			tvScores[isPlaying].setText(""+mScores[0]+":"+mScores[1]);
		} else if (mScores[0] < mScores[1]) {
			result = String.format(getString(R.string.won),
					getString(R.string.player_2), mScores[1], mScores[0]);
			tvPlayers[1].setVisibility(View.VISIBLE);
			tvPlayers[0].setVisibility(View.GONE);
			tvScores[isPlaying].setText(""+mScores[1]+":"+mScores[0]);
		} else {
			result = String.format(getString(R.string.tie), mScores[0],
					mScores[1]);
			tvScores[isPlaying].setText(""+mScores[0]+":"+mScores[1]);
		}
		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
		finished = true;
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
		mainLayout.post(new ColorChanger(mainLayout, color));
		mainLayout
				.postDelayed(
						new ColorChanger(mainLayout, Color.argb(0xff, 0x00,
								0x80, 0x40)), 200);
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
		mView.setBackgroundColor(mColor);
	}

}

class Scaler implements Runnable {

	private View mView;
	private float mFrom;
	private float mTo;
	private int mDuration;

	public Scaler(View v, float from, float to, int duration) {
		mView = v;
		mFrom = from;
		mTo = to;
		mDuration = duration;
	}

	@Override
	public void run() {
		Animation anim = new ScaleAnimation(mFrom, mFrom, mTo, mTo,
				mView.getWidth() / 2, mView.getHeight() / 2);
		anim.setDuration(mDuration);
		mView.startAnimation(anim);
	}
}