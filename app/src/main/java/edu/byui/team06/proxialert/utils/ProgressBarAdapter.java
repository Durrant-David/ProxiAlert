package edu.byui.team06.proxialert.utils;

import android.os.AsyncTask;
import android.widget.ProgressBar;

public class ProgressBarAdapter extends AsyncTask<String, Integer, String> {
    private final ProgressBar _bar;
    private final long startClockTime;
    private final long endClockTime;
    public ProgressBarAdapter(ProgressBar pb) {
        _bar = pb;
        _bar.setProgress(0);
        startClockTime = System.currentTimeMillis();
        endClockTime = System.currentTimeMillis() + 10000; //10 seconds
    }

    // This is run in a background thread
    @Override
    protected String doInBackground(String... params) {

        while(System.currentTimeMillis() < endClockTime) {
            int progress = (int) (System.currentTimeMillis() - startClockTime) / 100;
            publishProgress(progress);
        }
        return "10Seconds";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        _bar.setProgress(values[0]);
        // Do things like update the progress bar
    }

}