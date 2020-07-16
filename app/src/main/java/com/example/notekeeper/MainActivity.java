package com.example.notekeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerItem;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCourseLayoutManager;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.general_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.sync_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.notifications_preferences, false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        drawer.setDrawerListener(toggle);
        toggle.syncState();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        initializeDisplayContent();
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.nav_notes){
                    displayNote();
                }
                else if (id == R.id.nav_courses){
                    displayCourses();
                }
                else if (id == R.id.nav_share){
                    handleShare();
//                    handleSelection(R.string.nav_share_message);
                }
                else if (id == R.id.nav_send){
                    handleSelection(R.string.nav_Send_message);
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);


                return false;
            }

            private void handleShare() {
                View view = findViewById(R.id.list_items);
                Snackbar.make(view, "Share to - " +
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).
                                getString("user_fav_social", ""), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        mNoteRecyclerAdapter.notifyDataSetChanged();
        updateNavHeader();
    }

    private void updateNavHeader() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userName = (TextView) headerView.findViewById(R.id.user_name);
        TextView email = (TextView) headerView.findViewById(R.id.email_address);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String localName = pref.getString("user_display_name", "");
        String emailAddress = pref.getString("email_address", "");
        userName.setText(localName);
        email.setText(emailAddress);

    }

    private void initializeDisplayContent() {
        mRecyclerItem = (RecyclerView) findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);

        mCourseLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.span_count));
        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this,notes);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

        displayNote();
    }

    private void displayNote() {
        mRecyclerItem.setLayoutManager(mNotesLayoutManager);
        mRecyclerItem.setAdapter(mNoteRecyclerAdapter);


        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    private void displayCourses(){
        mRecyclerItem.setLayoutManager(mCourseLayoutManager);
        mRecyclerItem.setAdapter(mCourseRecyclerAdapter);
        selectNavigationMenuItem(R.id.nav_courses);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void handleSelection(int message) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}