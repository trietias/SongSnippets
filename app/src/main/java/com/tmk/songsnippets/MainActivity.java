package com.tmk.songsnippets;

import android.database.SQLException;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MediaPlayer songController;
    final private Integer[] intervals = new Integer[]{1,2,3,5,10,15};
    final private Integer[] points = new Integer[]{1000,800,600,400,200,100};
    private int intervalTracker = 0;
    private List<String> categoryList;
    private List<String> songList;
    private Spinner categorySpinner;
    private Spinner songSpinner;
    Integer team1 = 0;
    Integer team2 = 0;
    TextView intervalTextView;
    TextView songTitleTextView;
    TextView songArtistTextView;
    TextView songCategoryTextView;
    TextView team1Score;
    TextView team2Score;
    String songChoice = "";
    String songTitle;
    String songArtist;
    int songResource = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intervalTextView = (TextView) findViewById(R.id.intervalText);
        songTitleTextView = (TextView) findViewById(R.id.songTitle);
        songArtistTextView = (TextView) findViewById(R.id.artistName);
        songCategoryTextView = (TextView) findViewById(R.id.categoryName);
        Typeface dosis = Typeface.createFromAsset(getApplicationContext().getAssets(), "font/Dosis-Regular.ttf");
        Typeface dosis_bold = Typeface.createFromAsset(getApplicationContext().getAssets(), "font/Dosis-Bold.ttf");
        Typeface dosis_light = Typeface.createFromAsset(getApplicationContext().getAssets(), "font/Dosis-Light.ttf");
        team1Score = (TextView) findViewById(R.id.team1Points);
        team1Score.setText("0");
        team2Score = (TextView) findViewById(R.id.team2Points);
        team2Score.setText("0");
        initialSetUp();

        categorySpinner = (Spinner)findViewById(R.id.categorySpinner);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DBHandler handler = new DBHandler(getBaseContext());
                songList = handler.getCategoryData(categoryList.get(position));
                handler.close();
                ArrayAdapter<String> songAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, songList);
                songSpinner.setAdapter(songAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        songSpinner = (Spinner)findViewById(R.id.songSpinner);
        ArrayAdapter<String> songAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, songList);
        songSpinner.setAdapter(songAdapter);
        songSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setUp();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setUp();
    }

    public void play(View view) {
        if(songController != null) {
            if (songController.isPlaying()) {
                try {
                    songController.stop();
                    songController.release();
                    songController = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        songController = MediaPlayer.create(this,songResource);
        int playTime = intervals[intervalTracker];
        songController.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    songController.stop();
                    songController.release();
                    songController = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        songController.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (songController != null) {
                        if (songController.isPlaying()) {
                            songController.stop();
                            songController.release();
                            songController = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, playTime * 1000);
    }

    public void editInterval(View view) {
        if (view == findViewById(R.id.forwardButton)) {
            if (intervalTracker < 5) {
                intervalTracker++;
            }
        } else {
            if (intervalTracker > 0) {
                intervalTracker--;
            }
        }
        String s = Integer.toString(intervals[intervalTracker]);
        intervalTextView.setText(s);
    }

    public void initialSetUp() {
        DBHandler handler = new DBHandler(this);

        try {

            handler.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            handler.openDataBase();

        }catch(SQLException sqle){

            throw sqle;

        }

        categoryList = handler.getCategories();
        songList = handler.getAllData("SongTitle");
        handler.close();
    }

    public void setUp() {
        intervalTracker = 0;
        String s = Integer.toString(intervals[intervalTracker]);
        intervalTextView.setText(s);
        String artist = "By: ";
        String category = "Category: ";
        DBHandler handler = new DBHandler(this);
        songTitle = songSpinner.getSelectedItem().toString();
        songChoice = handler.getResourceValue(songTitle);
        songResource = getResourceId(songChoice, "raw", getPackageName());
        songTitleTextView.setText(songTitle);
        artist += handler.getArtist(songTitle);
        songArtistTextView.setText(artist);
        category += handler.getCategory(songTitle);
        songCategoryTextView.setText(category);
        handler.close();
    }

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void random(View view) {
        int rand = (int) Math.floor(Math.random()*songList.size());
        songSpinner.setSelection(rand);
        intervalTracker = 0;
        setUp();
    }

    public void increaseScore(View view) {
        int pointValue = points[intervalTracker];
        if(view == findViewById(R.id.team1Add)) {
            team1 += pointValue;
        } else {
            team2 += pointValue;
        }
        String t1points = team1.toString();
        String t2points = team2.toString();
        team1Score.setText(t1points);
        team2Score.setText(t2points);
    }

    public void resetScore(View view) {
        team1 = 0;
        team2 = 0;
        String t1points = team1.toString();
        String t2points = team2.toString();
        team1Score.setText(t1points);
        team2Score.setText(t2points);
    }

}
