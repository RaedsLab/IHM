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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.Parse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SensorEventListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //GET ALL NAMES FROM stings.xml
        names = getResources().getStringArray(R.array.names_array);
        Collections.shuffle(Arrays.asList(names));

        /// INSTRUCTIONS
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

        final Button clickButton = (Button) findViewById(R.id.btnStart);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView instructionsTextView = (TextView) findViewById(R.id.txtInstructions);
                instructionsTextView.setVisibility(View.GONE);
                clickButton.setVisibility(View.GONE);

                listView = (ListView) findViewById(R.id.namesList);
                listView.setVisibility(View.VISIBLE);
                startTime = System.currentTimeMillis();
            }
        });


        //SET ARRAY ADAPTER
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

        ///set names in listView Via adapter
        listView = (ListView) findViewById(R.id.namesList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        ///ACCEL
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        listView.setVisibility(View.GONE); // or View.INVISIBLE as Jason Leung wrote

        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        /*Toast.makeText(getApplicationContext(), ((TextView) view).getText() + "",
                Toast.LENGTH_SHORT).show();
        */
        // listView.smoothScrollToPositionFromTop(position + 5, 0, 500);

        ////
        Intent intent = new Intent(this, DetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT, ((TextView) view).getText() + ":" + startTime + ":" + randomName);
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
