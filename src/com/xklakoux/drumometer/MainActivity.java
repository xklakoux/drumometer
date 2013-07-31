package com.xklakoux.drumometer;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xklakoux.drumometer.util.SystemUiHider;

import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements OnClickListener, OnSignalsDetectedListener {

	private TextView tvDrumometer;
	private RelativeLayout rlProgressBar;
	private HoloCircularProgressBar holoCircularProgressBar1;
	private Button bSubstract;
	private Button bAdd;
	private EditText etSeconds;
	private LinearLayout llCountingLayout;
	private TextView tvCount;
	private TextView tvBeatsCounted;
	private Button bStartCounting;
	private TextView tvKeepGoing;
	private int mShortAnimationDuration;
	private HoloCircularProgressBar progress;
	private ObjectAnimator progressBarAnimator;
	private Long initialValue;

	boolean firstTime = false;

	static MainActivity mainApp;
	public static final int DETECT_NONE = 0;
	public static final int DETECT_WHISTLE = 1;
	public static int selectedDetection = DETECT_NONE;
	private DetectorThread detectorThread;
	private RecorderThread recorderThread;
	private int numWhistleDetected = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		findViews();
		mainApp = this;

		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_mediumAnimTime);

		progress = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar1);
		animate(progress, new AnimatorListener() {

			@Override
			public void onAnimationCancel(final Animator animation) {
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				progress.setProgress(0.00001f);

			}			

			@Override
			public void onAnimationRepeat(final Animator animation) {
			}

			@Override
			public void onAnimationStart(final Animator animation) {

			}
		});
	}

	/**
	 * Animate.
	 * 
	 * @param progressBar
	 *            the progress bar
	 * @param listener
	 *            the listener
	 */
	private void animate(final HoloCircularProgressBar progressBar,
			final AnimatorListener listener) {
		final float progress = 1f;
		progressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress",
				progress);
		progressBar.setProgress(0.00001f);
		progressBarAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(final Animator animation) {
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				progressBar.setProgress(0f);
				crossfadeIn();
				detectorThread.stopDetection();

			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
			}

			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
		progressBarAnimator.addListener(listener);
		progressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				progressBar.setProgress((Float) animation.getAnimatedValue());
			}
		});

	}

	private void findViews() {
		tvDrumometer = (TextView) findViewById(R.id.tvDrumometer);
		rlProgressBar = (RelativeLayout) findViewById(R.id.rlProgressBar);
		holoCircularProgressBar1 = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar1);
		bSubstract = (Button) findViewById(R.id.bSubstract);
		bAdd = (Button) findViewById(R.id.bAdd);
		etSeconds = (EditText) findViewById(R.id.etSeconds);
		llCountingLayout = (LinearLayout) findViewById(R.id.llCountingLayout);
		tvCount = (TextView) findViewById(R.id.tvCount);
		tvBeatsCounted = (TextView) findViewById(R.id.tvBeatsCounted);
		bStartCounting = (Button) findViewById(R.id.bStartCounting);
		tvKeepGoing = (TextView) findViewById(R.id.tvKeepGoing);

		bSubstract.setOnClickListener(this);
		bAdd.setOnClickListener(this);
		bStartCounting.setOnClickListener(this);
	}

	/**
	 * Handle button click events<br />
	 * <br />
	 * Auto-created on 2013-07-31 01:48:49 by Android Layout Finder
	 * (http://www.buzzingandroid.com/tools/android-layout-finder)
	 */
	@Override
	public void onClick(View v) {
		if (v == bSubstract) {
			etSeconds.setText(""
					+ (Integer.valueOf(etSeconds.getText().toString()) - 1));
			// Handle clicks for bSubstract
		} else if (v == bAdd) {
			etSeconds.setText(""
					+ (Integer.valueOf(etSeconds.getText().toString()) + 1));
		} else if (v == bStartCounting) {
			// Handle clicks for bStartCounting
			crossfade();
			initialValue=Long.valueOf(etSeconds.getText()
					.toString()) * 1000;
			progressBarAnimator.setDuration(initialValue);
			progressBarAnimator.start();
			
			selectedDetection = DETECT_WHISTLE;
			recorderThread = new RecorderThread();
			recorderThread.start();
			detectorThread = new DetectorThread(recorderThread);
			detectorThread.setOnSignalsDetectedListener(MainActivity.mainApp);
			detectorThread.start();
			numWhistleDetected=0;
			tvCount.setText(""+0);
		}
	}

	private void crossfade() {
      
		if(!firstTime){
		// Set the content view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		llCountingLayout.setAlpha(0f);
		llCountingLayout.setVisibility(View.VISIBLE);
		tvKeepGoing.setVisibility(View.VISIBLE);

		// Animate the content view to 100% opacity, and clear any animation
		// listener set on the view.
		llCountingLayout.animate().alpha(1f)
				.setDuration(mShortAnimationDuration).setListener(null);
		}else{
			tvKeepGoing.setVisibility(View.VISIBLE);
			tvKeepGoing.animate().alpha(1f)
			.setDuration(mShortAnimationDuration).setListener(null);
		}
		// Animate the loading view to 0% opacity. After the animation ends,
		// set its visibility to GONE as an optimization step (it won't
		// participate in layout passes, etc.)
		bStartCounting.animate().alpha(0f).setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						bStartCounting.setVisibility(View.GONE);
					}
				});
		

	}

	private void crossfadeIn() {

		// Set the content view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		bStartCounting.setAlpha(0f);
		bStartCounting.setVisibility(View.VISIBLE);

		// Animate the content view to 100% opacity, and clear any animation
		// listener set on the view.
		bStartCounting.animate().alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);
		if (!firstTime) {
			bStartCounting.animate().yBy(llCountingLayout.getHeight())
					.setDuration(mShortAnimationDuration).setListener(null);
			firstTime = true;
		}
		// Animate the loading view to 0% opacity. After the animation ends,
		// set its visibility to GONE as an optimization step (it won't
		// participate in layout passes, etc.)
		tvKeepGoing.animate().alpha(0f).setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						tvKeepGoing.setVisibility(View.VISIBLE);
					}
				});
	}

	@Override
	public void onWhistleDetected() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
			public void run() {
				tvCount.setText(String.valueOf(numWhistleDetected++));
			}
		});
	}
}
