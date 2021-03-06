package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import vitalypanov.phototracker.StartScreenFragment;

/**
 * Created by Vitaly on 23.03.2018.
 */
public class StartScreenActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";

    public static Intent newIntent(Context context){
        return new Intent(context, StartScreenActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return StartScreenFragment.newInstance();
    }
}