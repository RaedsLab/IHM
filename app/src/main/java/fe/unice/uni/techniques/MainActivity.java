package fe.unice.uni.techniques;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    ArrayAdapter<String> adapter;
    String[] names;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //GET ALL NAMES FROM stings.xml
        names = getResources().getStringArray(R.array.names_array);
        Collections.shuffle(Arrays.asList(names));


        //SET ARRAY ADAPTER
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

        ///set names in listView Via adapter
        listView = (ListView) findViewById(R.id.namesList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        /*Toast.makeText(getApplicationContext(), ((TextView) view).getText() + "",
                Toast.LENGTH_SHORT).show();
        */
        ////
        Intent intent = new Intent(this, DetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT, ((TextView) view).getText());
        startActivity(intent);
        ////
    }


}
