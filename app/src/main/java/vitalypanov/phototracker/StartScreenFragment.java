package vitalypanov.phototracker;

import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.Date;
import java.util.UUID;

import vitalypanov.phototracker.activity.AboutDialogActivity;
import vitalypanov.phototracker.activity.RunningTrackPagerActivity;
import vitalypanov.phototracker.activity.SettingsActivity;
import vitalypanov.phototracker.activity.TrackListActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.utilities.DateUtils;
import vitalypanov.phototracker.utilities.ListUtils;
import vitalypanov.phototracker.utilities.ServiceUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 29.08.2017.
 */

public class StartScreenFragment extends TrackerSupportMapFragment {
    // Main menu:
    Drawer mMenu;
    // menu items id's:
    private static final int MENU_ITEM_START_TRACK = 1;
    private static final int MENU_ITEM_TRACK_LIST = 2;
    private static final int MENU_ITEM_SETTINGS = 3;
    private static final int MENU_ITEM_SEND_FEEDBACK = 4;
    private static final int MENU_ITEM_RATE_PLAY_MARKET = 5;
    private static final int MENU_ITEM_ABOUT = 6;

    private Button mTrackResume;    // Resume existing track button
    private Button mTrackStart;     // Start new track button
    private Track mTrackToResume;   // Track which will resume

    private ServiceConnection mConnection ; // Service of recording track

    public static StartScreenFragment newInstance() {
        return new StartScreenFragment();
    }

    @Override
    public Track getTrack() {
        return null;
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_start_screen;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initNavigationDrawer(view);

        mTrackResume = (Button) view.findViewById(R.id.track_continue);
        mTrackResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utils.isNull(mTrackToResume)) {
                    startTrack(mTrackToResume.getUUID());
                }
            }
        });

        mTrackStart =  (Button) view.findViewById(R.id.track_start);
        mTrackStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTrack(null);
            }
        });

        return view;
    }

    /**
     * Init Navigation drawer main menu.
     * @param view - main view of the current fragment
     */
    private void initNavigationDrawer(View view ){
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.track_toolbar);
        AppCompatActivity parentActivity =(AppCompatActivity)getActivity();
        parentActivity.setSupportActionBar(toolbar);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Main menu
        mMenu = new DrawerBuilder()
            .withActivity(parentActivity)
            .withToolbar(toolbar)
            .withActionBarDrawerToggle(true)
            .withHeader(R.layout.drawer_header)
            .addDrawerItems(

                    new PrimaryDrawerItem().withName(R.string.menu_start).withIcon(R.mipmap.ic_steps).withSelectable(false).withIdentifier(MENU_ITEM_START_TRACK),
                    new PrimaryDrawerItem().withName(R.string.menu_track_list).withIcon(R.mipmap.ic_list).withSelectable(false).withIdentifier(MENU_ITEM_TRACK_LIST),
                    new PrimaryDrawerItem().withName(R.string.menu_settings).withIcon(R.mipmap.ic_settings).withSelectable(false).withIdentifier(MENU_ITEM_SETTINGS),

                    new DividerDrawerItem(),

                    new PrimaryDrawerItem().withName(R.string.menu_send_feedback).withIcon(R.mipmap.ic_feedback).withSelectable(false).withIdentifier(MENU_ITEM_SEND_FEEDBACK),
                    new PrimaryDrawerItem().withName(R.string.menu_rate_play_market).withIcon(R.mipmap.ic_playmarket).withSelectable(false).withIdentifier(MENU_ITEM_RATE_PLAY_MARKET),

                    new DividerDrawerItem(),

                    new PrimaryDrawerItem().withName(R.string.menu_about).withIcon(R.mipmap.ic_about).withSelectable(false).withIdentifier(MENU_ITEM_ABOUT)

                    ).withOnDrawerItemClickListener(
                            new Drawer.OnDrawerItemClickListener() {
                                @Override
                                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                    switch ((int)drawerItem.getIdentifier()) {
                                        case MENU_ITEM_START_TRACK:
                                            startTrack(null);
                                            break;
                                        case MENU_ITEM_TRACK_LIST:
                                            showTrackList();
                                            break;
                                        case MENU_ITEM_SETTINGS:
                                            showSettingsDialog();
                                            break;
                                        case MENU_ITEM_SEND_FEEDBACK:
                                            sendFeedback();
                                            break;
                                        case MENU_ITEM_RATE_PLAY_MARKET:
                                            ratePlayMarket();
                                            break;
                                        case MENU_ITEM_ABOUT:
                                            showAboutDialog();
                                            break;
                                    }
                                    return false;
                                }
                            }
                    ).withSelectedItem(-1) // to hide selection of any menu item
            .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        // if service already running we should start running activity above current
        if (ServiceUtils.isServiceRunning(
                  getActivity().getApplicationContext(),
                  TrackerGPSService.class)){
            startActivity(RunningTrackPagerActivity.newIntent(getActivity()));
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTrackListCounterUI();
        checkNotEndedTrackAndUpdateUI();
    }

    /**
     * Rate at Play market functionallity
     */
    private void ratePlayMarket(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getResources().getString(R.string.app_playmarket_link)));
        startActivity(intent);
    }

    /**
     * Update tracks count value on button and other UI
     */
    private void updateTrackListCounterUI(){
        long tracksCount =  TrackDbHelper.get(getContext()).getTracksCount();
        PrimaryDrawerItem menuTrackList = (PrimaryDrawerItem)mMenu.getDrawerItem(MENU_ITEM_TRACK_LIST);
        menuTrackList.withEnabled(tracksCount > 0);
        menuTrackList.withBadge(tracksCount > 0 ? String.valueOf(tracksCount) :"");
        mMenu.updateItem(menuTrackList);
    }

    /**
     * Check not ended tracks and update "continue" button
     */
    private void checkNotEndedTrackAndUpdateUI(){
        mTrackToResume = TrackDbHelper.get(getActivity()).getNotEndedTrack();
        if (Utils.isNull(mTrackToResume)){
            mTrackResume.setVisibility(View.GONE);
        } else {
            Date lastTimeStamp = null;
            // first trying to get last activity from the track
            if (!Utils.isNull(mTrackToResume.getTrackData()) && !Utils.isNull(ListUtils.getLast(mTrackToResume.getTrackData()))) {
                lastTimeStamp = ListUtils.getLast(mTrackToResume.getTrackData()).getTimeStamp();
            }
            // if not found yet - get start time of the track simply
            if (Utils.isNull(lastTimeStamp)){
                lastTimeStamp = mTrackToResume.getStartTime();
            }
            mTrackResume.setText(getResources().getText(R.string.action_resume) + ": " + DateUtils.getShortTimeFormatted(lastTimeStamp) + " " + mTrackToResume.getDistanceShortFormatted() + " " + getResources().getString(R.string.distance_metrics));
            mTrackResume.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Send feedback
     */
    private void sendFeedback(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getResources().getString(R.string.app_feedback_email_text)});
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_feedback_email_title));
        try {
            startActivity(Intent.createChooser(i, getResources().getString(R.string.app_feedback_send_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), getResources().getString(R.string.app_feedback_send_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show Settings dialog
     */
    private void showSettingsDialog(){
        startActivity(SettingsActivity.newIntent(getActivity()));
    }

    /**
     * Show About dialog
     */
    private void showAboutDialog(){
        startActivity(AboutDialogActivity.newIntent(getActivity()));
    }

    /**
     * Show track list
     */
    private void showTrackList(){
        startActivity(TrackListActivity.newIntent(getActivity()));
    }

    /**
     * Start recording track
     * @param trackUUID - If we want to resume existing track - provide it's uuid
     */
    private void startTrack(UUID trackUUID) {
        // first check location services
        if (!TrackLocationServices.checkLocaionServices(getActivity())){
            return;
        }
        Intent i = null;
        if (Utils.isNull(trackUUID)) {
            // start new Tracker GPS service
            i = TrackerGPSService.newIntent(getActivity());
        } else {
            // continue existing track and run Tracker GPS service
            i = TrackerGPSService.newIntent(getActivity(), trackUUID);
        }
        getActivity().startService(i);
        startActivity(RunningTrackPagerActivity.newIntent(getActivity()));
    }
}