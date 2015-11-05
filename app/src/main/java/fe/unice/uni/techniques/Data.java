package fe.unice.uni.techniques;

/**
 * Created by ben on 05/11/2015.
 */
import android.app.Application;

import java.util.ArrayList;

public class Data extends Application {
    private ArrayList<String> namesList;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public ArrayList<String> getNamesList() {return namesList;}
    public void setNamesList(ArrayList<String> data) {this.namesList = data;}
}