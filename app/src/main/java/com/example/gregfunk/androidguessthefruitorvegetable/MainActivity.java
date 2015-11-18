package com.example.gregfunk.androidguessthefruitorvegetable;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader r = new BufferedReader(reader);
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                return total.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    ArrayList<String> foodURLs = new ArrayList<String>();
    ArrayList<String> foodNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute("http://www.greatgrubclub.com/a-z-fruit-veg").get();

            // cut out glossary images and text
            Pattern p = Pattern.compile("<dl class=\"glossary\">(.*?)</dl>");
            Matcher m = p.matcher(result);
            m.find();

            // for each row, cut out image URL
            Pattern p2 = Pattern.compile("img src=\"(.*?)\"");
            Matcher m2 = p2.matcher(m.group(1));
            while (m2.find()) {
                foodURLs.add(m2.group(1));
            }

            // for each row, cut out name
            Pattern p3 = Pattern.compile("alt=\"(.*?)\"");
            Matcher m3 = p3.matcher(m.group(1));
            while (m3.find()) {
                foodNames.add(m3.group(1));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
