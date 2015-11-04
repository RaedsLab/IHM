package fe.unice.uni.techniques;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.concurrent.TimeUnit;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private long startTime, curTime, duration;
    private String targetName, localName;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Enable Local Datastore.
        //   Parse.enableLocalDatastore(getActivity());
        //   Parse.initialize(getActivity(), "FIH7TxNEMBdwRjqMMFbU6MPKisRtch5MNx4wsJ3C", "srmPIy9DK1SV4u3m2XsGFZSy8MSR3sTJp9rL7oxk");


        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String intenStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            String[] values = intenStr.split(":");

            localName = values[0];
            startTime = Long.parseLong(values[1], 10);
            targetName = values[2];

            ((TextView) rootView.findViewById(R.id.detail_text))
                    .setText(localName);
        }


        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Do what you want
                    if (targetName.contains(localName)) {

                        curTime = System.currentTimeMillis();

                        long duration = curTime - startTime;

                        String durationStr = String.format("%02d min, %02d sec",
                                TimeUnit.MILLISECONDS.toMinutes(duration),
                                TimeUnit.MILLISECONDS.toSeconds(duration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                        );
                        fab.setVisibility(View.GONE);
                        /*
                        @TODO : set detail_text to "DELETED"
                         */
                        //PARSE
                        // ParseObject testObject = new ParseObject("Interractions");
                        // testObject.put("time", duration);
                        //testObject.put("method", "click");

                        // testObject.saveInBackground();
                        ///

                        Toast.makeText(getActivity(), "Congrats, it only took you " + durationStr + "!",
                                Toast.LENGTH_LONG).show();

                        Log.d("Details", "Dur " + durationStr);
                    } else {
                        Toast.makeText(getActivity(), "Deleted wrong person, try again!",
                                Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
                return true; // consume the event
            }
        });
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                    // handle back button
                    Log.d("BACK", "BACK CLICKED");

                    return true;

                }

                return false;
            }
        });

    }
}
