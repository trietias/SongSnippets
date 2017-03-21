package com.tmk.songsnippets;

import android.database.SQLException;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    MediaPlayer songController;
    final private Integer[] intervals = new Integer[]{1,2,3,5,10,15};
    final private Integer[] points = new Integer[]{1000,800,600,400,200,100};
    private int intervalTracker = 0;
    private int songTracker = 0;
    private List<String> categoryList;
    private List<String> songList;
    private Stack<Integer> previousSongs = new Stack<Integer>();
    private Spinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;
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
    int songResource = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize TextView elements
        intervalTextView = (TextView) findViewById(R.id.intervalText);
        songTitleTextView = (TextView) findViewById(R.id.songTitle);
        songArtistTextView = (TextView) findViewById(R.id.artistName);
        songCategoryTextView = (TextView) findViewById(R.id.categoryName);
        team1Score = (TextView) findViewById(R.id.team1Points);
        team1Score.setText("0");
        team2Score = (TextView) findViewById(R.id.team2Points);
        team2Score.setText("0");

        initialSetUp(); // Generates categoryList and SongList for the spinners to update
                        // edit -- removed song spinner and replaced it with a random song selection

        categorySpinner = (Spinner)findViewById(R.id.categorySpinner);
        categoryAdapter = new ArrayAdapter<String>(this, R.layout.custom_center_spinner, categoryList);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DBHandler handler = new DBHandler(getBaseContext());
                songList = handler.getCategoryData(categoryList.get(position));
                handler.close();
                resetGame();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setUp(); // updates all UI elements for the first time -- a bit of redundancy here
    }

    public void play(View view) {
        // method to play the currently selected songResource
        stop(); // ensures nothing else is playing by trying to stop all sounds
        // creates a MediaPlayer with the currently selected songResource
        songController = MediaPlayer.create(this,songResource);
        // uses the current interval to set the play time
        int playTime = intervals[intervalTracker];
        // sets a listener for when the song is done playing
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
        songController.start(); // play song
        // handler used to stop the song after playTime seconds
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
                // since handler uses milliseconds, need to multiply playTime by 1000
            }
        }, playTime * 1000);
    }

    public void editInterval(View view) {
        // method to handle updating the interval tracker
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
        if (intervals[intervalTracker] < 10) {
            s = "0" + intervals[intervalTracker];
        }
        // updates interval UI element
        intervalTextView.setText(s);
    }

    public void stop() {
        //method to stop playing all songs
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
    }

    public void initialSetUp() {
        // initialize database handler
        DBHandler handler = new DBHandler(this);

        // try to create database and throw error on failure
        try {
            handler.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        // try to open database and throw exception on failure
        try {
            handler.openDataBase();
        }catch(SQLException sqle){
            throw sqle;
        }
        // list of all categories in the songlist file.
        categoryList = handler.getCategories();
        // list of all songs in the songlist file
        songList = handler.getAllData("SongTitle");
        handler.close(); // close database connection
    }

    public void setUp() {
        // this function is in charge of updating screen elements
        // stop any song that may be playing
        stop();
        // set the interval base (1 second) and turn it into a string
        intervalTracker = 0;
        String s = "";
        if (intervals[intervalTracker] < 10) {
            s = "0" + intervals[intervalTracker];
        } else {
            s = Integer.toString(intervals[intervalTracker]);
        }
        // set the TextView of the interval
        intervalTextView.setText(s);
        String artist = "";
        String category = "";
        DBHandler handler = new DBHandler(this);
        // uses the current songTracker index to get the song title and set the current song resource
        songTitle = songList.get(songTracker);
        songChoice = handler.getResourceValue(songTitle);
        songResource = getResourceId(songChoice, "raw", getPackageName());

        // Updates UI elements for title, artist, and category
        songTitleTextView.setText(songTitle);
        artist += handler.getArtist(songTitle);
        songArtistTextView.setText(artist);
        category += handler.getCategory(songTitle);
        if(categorySpinner.getFirstVisiblePosition() > 0) {
            category = "";
        }
        songCategoryTextView.setText(category);
        handler.close(); // close database connection
    }

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        // this method is in charge of getting the resourceID with the song title
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void random(View view) {
        // a method that utilizes the Math.random() function to choose the next song.
        int rand = (int) Math.floor(Math.random()*songList.size());
        // uses a previousSongs list to keep track of which song index have aleady been used
        // if the list of previous songs is as long as the songlist, previous songs is cleared
        if(songList.size() == previousSongs.size()) {
            previousSongs.clear();
        } else {
            // Makes sure not to play a song that has been played before
            while (previousSongs.contains(rand)) {
                rand = (int) Math.floor(Math.random()*songList.size());
            }
        }
        // updates the songTracker index
        songTracker = rand;
        // adds the index to the previousSongs list to ignore next time
        previousSongs.push(songTracker);
        intervalTracker = 0; // resets the interval to the 0 index (1 second)
        setUp(); // updates UI elements
    }

    public void increaseScore(View view) {
        // method to increase score for either team
        // sets the number of points getting the correct answer in this round will give
        int pointValue = points[intervalTracker];
        // uses the view to decide which team to add points to
        if(view == findViewById(R.id.team1Add)) {
            team1 += pointValue;
        } else {
            team2 += pointValue;
        }
        String t1points = team1.toString();
        String t2points = team2.toString();
        // updates score UI elements
        team1Score.setText(t1points);
        team2Score.setText(t2points);
    }

    public void resetScore(View view) {
        // method to reset score of both teams to 0
        team1 = 0;
        team2 = 0;
        String t1points = team1.toString();
        String t2points = team2.toString();
        team1Score.setText(t1points);
        team2Score.setText(t2points);
        // bring the category back to the initial selection
        categorySpinner.setSelection(0,true);
        resetGame(); // reset all game elements
    }

    public void resetGame() {
        // method to reset all of the game objects before updating UI
        previousSongs.clear();
        songTracker = 0;
        previousSongs.push(songTracker);
        setUp(); // Updates UI elements
    }

}
