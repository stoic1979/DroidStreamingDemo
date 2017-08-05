package com.wb.audiostream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.IOException;

/**
 * MainActivity performing audio streaming of audio file hosted on kiwi server and providing play/pause toggle button.
 */

public class MainActivity extends Activity {

    //button on UI screen
    private Button btn;

    //link of audio file
    private String fileLink = "http://k003.kiwi6.com/hotlink/lzxjwero5l/rahman-25.mp3";

    //help to toggle between play and pause.
    private boolean playPause;

    private MediaPlayer mediaPlayer;

    //remain false till media is not completed, inside OnCompletionListener make it true.
    private boolean initialStage = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing button
        btn = findViewById(R.id.play);

        //initializing mediaPlayer object
        mediaPlayer = new MediaPlayer();

        //setting streaming type
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //setting clickListener on button
        btn.setOnClickListener(pausePlay);

    }//onCreate

    /**
     * onClickListener for play/pause button handling streaming accordingly option choosed.
     */

    private OnClickListener pausePlay = new OnClickListener() {

        @Override
        public void onClick(View v) {

            //logic to handle pause/play
            if (!playPause) {
                btn.setText(R.string.pause);
                if (initialStage)
                    new Player()
                            .execute(fileLink);
                else {
                    if (!mediaPlayer.isPlaying())
                        mediaPlayer.start();
                }
                playPause = true;
            } else {
                btn.setText(R.string.play);
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                playPause = false;
            }

        }//onClick
    };

    /**
     * preparing media-player will take some time to buffer the content so prepare it inside the background thread and starting it on UI thread.
     */

    class Player extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progress;

        @Override
        protected Boolean doInBackground(String... params) {

            Boolean prepared;

            try {

                mediaPlayer.setDataSource(params[0]);

                mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        initialStage = true;
                        playPause = false;

                        btn.setText(R.string.play);

                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });

                mediaPlayer.prepare();
                prepared = true;

            } catch (IllegalArgumentException e) {

                Log.d("IllegalArgument", e.getMessage());

                prepared = false;
                e.printStackTrace();

            } catch (SecurityException | IllegalStateException | IOException e) {

                prepared = false;
                e.printStackTrace();

            }

            return prepared;

        }//doInBackground

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (progress.isShowing()) {
                progress.cancel();
            }

            Log.d("Prepared", "//" + result);

            mediaPlayer.start();

            initialStage = false;
        }

        public Player() {
            progress = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            this.progress.setMessage("Buffering...");
            this.progress.show();

        }

    }//player async

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}//MainActivity
