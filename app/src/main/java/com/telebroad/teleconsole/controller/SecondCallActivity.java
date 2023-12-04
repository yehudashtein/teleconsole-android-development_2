package com.telebroad.teleconsole.controller;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.telebroad.teleconsole.DialPadView;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.dashboard.ContactFragment;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.pjsip.CallController;
import com.telebroad.teleconsole.pjsip.CallManager;
import com.telebroad.teleconsole.pjsip.TeleConsoleCall;

import static com.telebroad.teleconsole.pjsip.SipService.EXTRA_PJSIP_ID;

public class SecondCallActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private int type = NEW_CALL;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_call);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        ActiveCallViewModel activeCallViewModel = ViewModelProviders.of(this).get(ActiveCallViewModel.class);
//        activeCallViewModel.hasActiveCall.observe(this,(state) ->{
//            if (state == SipManager.UIState.FINISHED){
//                finish();
//            }
//        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        type = getIntent().getIntExtra(TYPE_EXTRA, 0);
        switch (type) {
            case NEW_CALL:
                getSupportActionBar().setTitle(R.string.add_call);
                break;
            case TRANSFER:
                getSupportActionBar().setTitle(R.string.forward);
                break;
            case CONFERENCE:
                getSupportActionBar().setTitle(R.string.conference);
                break;
        }
    }

    void apply(String phoneNumber) {
        switch (type) {
            case NEW_CALL:
                CallController callController = CallManager.getInstance().getActiveCallGroup().getCallController();
                //android.util.Log.d("PJSIPConnectionService", "placing new call, active call controller " + callController);
                if (TeleConsoleCall.useConnectionService) {
                    callController.hold(true);
                }
                SipManager.getInstance(this).call(phoneNumber, this);
                finish();
                break;
            case TRANSFER:
                SipManager.getInstance(this).transfer(phoneNumber);
                finish();
                break;
            case CONFERENCE:
                int thisCall = getIntent().getIntExtra(EXTRA_PJSIP_ID, 0);
                SipManager.getInstance(this).conference(thisCall, phoneNumber);
                finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * email.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_second_call, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: {
                    int callAction = -1;
                    switch (type) {
                        case NEW_CALL:
                            callAction = DialPadView.SECOND_CALL;
                            break;
                        case TRANSFER:
                            callAction = DialPadView.TRANSFER;
                            break;
                        case CONFERENCE:
                            callAction = DialPadView.CONFERENCE;
                            break;
                        default:
                            //Log.w("SecondCallActivity", "No Known Action", new IllegalArgumentException("Incorrect calling type"));
                            break;
                    }
                    return DialPadSegment.newInstance(callAction);
                }
                case 1:
                    return ContactFragment.newInstance(contact -> {
                        if (contact.getTelephoneLines().isEmpty()) {
                            return;
                        }
                        if (contact.getTelephoneLines().size() == 1) {
                            apply(contact.getTelephoneLines().get(0).fixed());
                        } else {
                            ContactPhoneChooserListDialog.getInstance(contact.getTelephoneLines(), v -> {apply(v.fixed());
                            }).show(getSupportFragmentManager(), "choose email");
                        }
                    },"SecondCallActivity");
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
    public static final String TYPE_EXTRA = "com.telebroad.second.call.type";
    public static final int NEW_CALL = 0;
    public static final int TRANSFER = 1;
    public static final int CONFERENCE = 2;

}
