package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.TrackImageFragment;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackBitmap;
import vitalypanov.phototracker.others.ViewPageUpdater;

/**
 * Images of the track pages
 * Created by Vitaly on 01.03.2018.
 */

public class TrackImagesPagerActivity extends AppCompatActivity {
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";
    private ViewPager mViewPager;
    private FragmentStatePagerAdapter mPagerAdapter;
    private List<TrackBitmap> mBitmaps;
    private TextView mCounterTextView;

    public static Intent newIntent(Context packageContext, UUID uuid){
        Intent intent = new Intent(packageContext, TrackImagesPagerActivity.class);
        intent.putExtra(EXTRA_TRACK_UUID, uuid);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar when showing images
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pager_images);

        UUID uuid = (UUID)getIntent().getSerializableExtra(EXTRA_TRACK_UUID);
        Track track = TrackDbHelper.get(this).getTrack(uuid);
        track.loadCashedBitmaps(this);
        mBitmaps = track.getCashedBitmaps();

        mViewPager = (ViewPager)findViewById(R.id.activity_pager_images_view_pager);
        mCounterTextView = (TextView) findViewById(R.id.activity_pager_counter_textview);
        mCounterTextView.bringToFront();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPagerAdapter =new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                TrackBitmap trackBitmap = mBitmaps.get(position);
                return TrackImageFragment.newInstance(trackBitmap.getTrackPhoto().getPhotoFileName());
            }

            @Override
            public int getCount() {
                return mBitmaps.size();
            }

            @Override
            public void startUpdate(ViewGroup container) {
                super.startUpdate(container);
            }
        };
        mViewPager.setAdapter(mPagerAdapter);

        // when page is changed we can update fragment UI
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i(TAG, "onPageScrolled position= " + position);
            }

            @Override
            public void onPageSelected(int position) {
                ViewPageUpdater fragment = (ViewPageUpdater) mPagerAdapter.instantiateItem(mViewPager, position);
                if (fragment != null) {
                    fragment.onPageSelected();
                }
                updateCounter(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int i = 0;
            }
        });

        int position = 0;
        mViewPager.setCurrentItem(position);
        updateCounter(position);
    }
    private void updateCounter(int position){
        //mCounterTextView.setVisibility(View.VISIBLE);
        mCounterTextView.setText(String.valueOf(position+1) + "/" + mPagerAdapter.getCount());
        /*
        // after some second - hide counter
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCounterTextView.setVisibility(View.GONE);
            }
        }, 2000);
        */
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}