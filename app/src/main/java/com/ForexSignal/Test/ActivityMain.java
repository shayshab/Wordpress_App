package com.ForexSignal.Test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.ForexSignal.Test.data.AppConfig;
import com.ForexSignal.Test.data.Constant;
import com.ForexSignal.Test.data.SharedPref;
import com.ForexSignal.Test.fragment.FragmentCategory;
import com.ForexSignal.Test.fragment.FragmentHome;
import com.ForexSignal.Test.fragment.FragmentLater;
import com.ForexSignal.Test.utils.Tools;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.test.signal.R;

public class ActivityMain extends AppCompatActivity {

    //for ads
    private InterstitialAd mInterstitialAd;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private NavigationView navigationView;
    private FloatingActionButton fab;
    private Fragment fragment = null;
    private TextView user_name, user_email;
    private SharedPref sharedPref;
    private boolean pendingIntroAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = new SharedPref(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        prepareAds();
        initToolbar();
        initDrawerMenu();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(i);
            }
        });
        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    private void initDrawerMenu() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                showInterstitial();
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                displayFragment(item.getItemId(), item.getTitle().toString());
                drawer.closeDrawers();
                return true;
            }
        });

        user_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);
        user_email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_email);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            doExitApp();
        }
    }

    @Override
    protected void onResume() {
        user_name.setText(sharedPref.getYourName());
        user_email.setText(sharedPref.getYourEmail());
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        if (pendingIntroAnimation) {
            pendingIntroAnimation = false;
            startIntroAnimation();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), ActivitySettings.class);
            startActivity(i);
        } else if (id == R.id.action_rate) {
            Tools.rateAction(ActivityMain.this);
        } else if (id == R.id.action_about) {
            Tools.aboutAction(ActivityMain.this);
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayFragment(int id, String title) {

        switch (id) {
            case R.id.nav_home:
                if (!actionBar.getTitle().toString().equals(title)) {
                    actionBar.setTitle(title);
                    fragment = new FragmentHome();
                }
                break;
            case R.id.nav_category:
                if (!actionBar.getTitle().toString().equals(title)) {
                    actionBar.setTitle(title);
                    fragment = new FragmentCategory();
                }
                break;
            case R.id.nav_later:
                if (!actionBar.getTitle().toString().equals(title)) {
                    actionBar.setTitle(title);
                    fragment = new FragmentLater();
                }
                break;
            case R.id.nav_setting:
                Intent i = new Intent(getApplicationContext(), ActivitySettings.class);
                startActivity(i);
                break;
            case R.id.nav_rate:
                Tools.rateAction(ActivityMain.this);
                break;

            case R.id.nav_about:
                Tools.aboutAction(ActivityMain.this);
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_content, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private void prepareAds() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest2);
    }

    /**
     * show ads
     */
    public void showInterstitial() {
        // Show the ad if it's ready
        if (AppConfig.ENABLE_ADSENSE && mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void startIntroAnimation() {
        fab.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_fab_size));

        int actionbarSize = Tools.dip2px(this, 56);
        toolbar.setTranslationY(-actionbarSize);
        toolbar.animate().translationY(0).setDuration(Constant.ANIM_DURATION_TOOLBAR)
                .setStartDelay(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startContentAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        startContentAnimation();
                    }
                });
    }

    private void startContentAnimation() {
        fab.animate().translationY(0).setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(300)
                .setDuration(Constant.ANIM_DURATION_FAB).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // first fragment to display
                displayFragment(R.id.nav_home, getString(R.string.title_nav_home));
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // first fragment to display
                displayFragment(R.id.nav_home, getString(R.string.title_nav_home));
            }
        }).start();
    }

    public static boolean active = false;
    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }
    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

}
