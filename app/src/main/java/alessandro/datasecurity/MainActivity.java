package alessandro.datasecurity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import alessandro.datasecurity.auth.Login;
import alessandro.datasecurity.utils.GlideApp;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    public static android.support.v4.app.FragmentManager sFm;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        final ImageView profile = header.findViewById(R.id.user_profile_photo);
        final TextView iconText = header.findViewById(R.id.icon_text);
        ;
        final TextView fullname = header.findViewById(R.id.nav_fullname);
        final TextView email = header.findViewById(R.id.nav_email);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getDisplayName() != null) iconText.setText(user.getDisplayName().substring(0, 1));
        fullname.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        email.setText(user.getEmail());
        if (user.getPhotoUrl() != null) {
            GlideApp.with(getApplicationContext())
                    .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString())
                    .into(profile);
            profile.setColorFilter(null);
            iconText.setVisibility(View.GONE);
        } else {
            profile.setImageResource(R.drawable.bg_circle);
            profile.setColorFilter((getRandomMaterialColor("400")));
            iconText.setVisibility(View.VISIBLE);
        }


        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                if (user.getPhotoUrl() == null) {
                    profile.setColorFilter((getRandomMaterialColor("400")));
                }

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, new InboxFragment()).commit();
        }


    }


    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

/*
        if (id == R.id.save_exam) {
            return true;
        }*/


        return super.onOptionsItemSelected(item);

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        sFm = getSupportFragmentManager();
        int id = item.getItemId();

       /* if (id == R.id.nav_camera) {

            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_main);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new InboxFragment()).commit();


        } else if (id == R.id.nav_slideshow) {

            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_main);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new InboxFragment()).commit();

        } else */
        if (id == R.id.logout) {

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
        setTitle(item.getTitle());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


   /* public void onListFragmentInteraction(DummyContent.DummyItem uri) {
        //you can leave it empty
    }*/

    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }
}
