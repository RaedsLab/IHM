package fe.unice.uni.techniques;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ListView listView;
    ArrayAdapter<String> adapter;
    String[] names;

    private int initScrollPos = 0;

    private float initialY;
    private boolean isfirstRun = true;
    private float yThreshold = 2;

    long startTime;

    private boolean isReternedToOriginalPosition = true;
    private int reachedScrollPosition = 0;
    // SWIPE


    ///Accel
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate, lastScroll = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
    //

    //Instructions
    private String randomName, instructionText;
    private boolean hadInstructions = false;

    //

    // SWIPE

    StableArrayAdapter mAdapter;
    //ListView mListView;
    BackgroundContainer mBackgroundContainer;
    boolean mSwiping = false;
    boolean mItemPressed = false;
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;


    private boolean imBack = false;


    ////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// CHECK IF GETTING BACK FROM ACTIVITY
        Intent intent = this.getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String intenStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            String[] values = intenStr.split(":");

            randomName = values[0];
            startTime = Long.parseLong(values[1], 10);

            android.util.Log.d("I'm BACK !! ", "name " + randomName + " startTime " + startTime);
            imBack = true;
        }
        /////


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //GET ALL NAMES FROM stings.xml
        names = getResources().getStringArray(R.array.names_array);
        Collections.shuffle(Arrays.asList(names));

        /// SWIPE
        mBackgroundContainer = (BackgroundContainer) findViewById(R.id.listViewBackground);
        listView = (ListView) findViewById(R.id.namesList);

        android.util.Log.d("Debug", "d=" + listView.getDivider());
        final ArrayList<String> namesList = new ArrayList<String>();
        for (int i = 0; i < names.length; ++i) {
            namesList.add(names[i]);
        }
        mAdapter = new StableArrayAdapter(this, R.layout.opaque_text_view, namesList,
                mTouchListener, this);
        listView.setAdapter(mAdapter);
        final Button clickButton = (Button) findViewById(R.id.btnStart);


        if (imBack == false) {
            // SI 1ere fois cherche un nom random
            int idx = new Random().nextInt(names.length);
            randomName = (names[idx]);
            instructionText = "You need to find and delete '" + randomName + "' from the contact list. \n";
            // if lucky get instructions
            if (new Random().nextInt() % 2 == 0) {
                hadInstructions = true;
                instructionText += "You can scroll, or tilt the device to go through the list. \n";
                instructionText += "You can click or swipe a contact name to delete it.\n";
            }

                TextView instructionsTextView = (TextView) findViewById(R.id.txtInstructions);
                instructionsTextView.setText(instructionText);

                listView.setVisibility(View.GONE);


            clickButton.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   TextView instructionsTextView = (TextView) findViewById(R.id.txtInstructions);
                                                   instructionsTextView.setVisibility(View.GONE);
                                                   clickButton.setVisibility(View.GONE);
                                                   listView.setVisibility(View.VISIBLE);
                                                   startTime = System.currentTimeMillis();
                                               }
                                           }

            );
        } else {
            // I'm getting back to the list
            TextView instructionsTextView = (TextView) findViewById(R.id.txtInstructions);
            instructionsTextView.setVisibility(View.GONE);
            clickButton.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }

        listView.setOnItemClickListener(this);

        ///ACCEL
        senSensorManager = (SensorManager)

                getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    // SWIPE
    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        float mDownX;
        private int mSwipeSlop = -1;

        private static final int MAX_CLICK_DURATION = 100;
        private long startClickTime;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(MainActivity.this).
                        getScaledTouchSlop();
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startClickTime = Calendar.getInstance().getTimeInMillis();

                    if (mItemPressed) {
                        // Multi-item swipes not handled
                        return false;
                    }
                    mItemPressed = true;
                    mDownX = event.getX();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1);
                    v.setTranslationX(0);
                    mItemPressed = false;
                    break;
                case MotionEvent.ACTION_MOVE: {

                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    if (!mSwiping) {
                        if (deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            listView.requestDisallowInterceptTouchEvent(true);
                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX((x - mDownX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {

                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    if (clickDuration < MAX_CLICK_DURATION) {
                        //click event has occurred

                        Log.d("Click ", "This is a click- ");
                        if (v instanceof TextView) {
                            TextView textView = (TextView) v;
                            String chosenName = textView.getText().toString();
                            goToDetails(chosenName);
                            //Do your stuff
                        } else {
                            goToDetails("Ali");

                        }


                    }

                    // User let go - figure out whether to animate the view out, or back into place
                    if (mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final boolean remove;
                        if (deltaXAbs > v.getWidth() / 4) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                            remove = true;
                        } else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = 1;
                            remove = false;
                        }
                        // Animate position and alpha of swiped item
                        // NOTE: This is a simplified version of swipe behavior, for the
                        // purposes of this demo about animation. A real version should use
                        // velocity (via the VelocityTracker class) to send the item off or
                        // back at an appropriate speed.
                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                        listView.setEnabled(false);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Restore animated values
                                        v.setAlpha(1);
                                        v.setTranslationX(0);
                                        if (remove) {
                                            animateRemoval(listView, v);
                                        } else {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                            listView.setEnabled(true);
                                        }
                                    }
                                });
                    }
                }
                mItemPressed = false;
                break;
                default:
                    return false;
            }

            return true;
        }
    };

    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(final ListView listview, View viewToRemove) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = mAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = listview.getPositionForView(viewToRemove);

        /// CHECK IF SWIPED THE RIGHT NAME
        if (mAdapter.getItem(position).toString() == randomName.toString()) {
            // FOUND THE RIGHT NAME => DELETE
            mAdapter.remove(mAdapter.getItem(position));
            Log.d("SWIPE", "Removed : " + mAdapter.getItem(position).toString() + " # " + position);


            // CHECK TIME IT TOOK TO COMPLETE
            long curTime = System.currentTimeMillis();
            long duration = curTime - startTime;

            String durationStr = String.format("%02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );

            Toast.makeText(this, "Congrats, it only took you " + durationStr + "!",
                    Toast.LENGTH_LONG).show();

            listView.setVisibility(View.GONE);

        } else {
            Toast.makeText(this, "Deleted wrong person, try again!",
                    Toast.LENGTH_SHORT).show();
        }


        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapter.getItemId(position);
                    Integer startTop = mItemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        mBackgroundContainer.hideBackground();
                                        mSwiping = false;
                                        listview.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {
                                    mBackgroundContainer.hideBackground();
                                    mSwiping = false;
                                    listview.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
    }


    public void goToDetails(String clikedName) {
        Intent intent = new Intent(this, DetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT, clikedName + ":" + startTime + ":" + randomName);
        startActivity(intent);
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                last_x = x;
                last_y = y;
                last_z = z;

                if (isfirstRun) {
                    initialY = last_y;
                    isfirstRun = false;
                }


                Log.d("ACCEL", "INIT : " + initialY + " Diff : " + (Math.abs(initialY - last_y)));


                if ((Math.abs(initialY - last_y) > yThreshold)) {
                    if (isReternedToOriginalPosition && (curTime - lastScroll) > 100) {
                        lastScroll = curTime;

                        if (initialY - last_y > 0) {
                            if (listView.canScrollList(1)) {
                                initScrollPos += 3;
                                Log.d("ACCEL", "+3 | POS " + initScrollPos);
                            }
                        } else {
                            if (listView.canScrollList(-1)) {
                                initScrollPos -= 3;
                                Log.d("ACCEL", "-3 | POS " + initScrollPos);
                            }
                        }
                        listView.smoothScrollToPositionFromTop(initScrollPos, 0, 100);
                    }
                    isReternedToOriginalPosition = false;
                } else {
                    Log.d("ACCEL", "NOT SCROLLING");
                    isReternedToOriginalPosition = true;
                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
