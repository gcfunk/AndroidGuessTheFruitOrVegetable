package com.example.gregfunk.androidguessthefruitorvegetable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
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

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    ImageView imageView;
    ArrayList<String> foodURLs = new ArrayList<String>();
    ArrayList<String> foodNames = new ArrayList<String>();
    int chosenFood = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

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

            Random random = new Random();
            chosenFood = random.nextInt(foodURLs.size());

            ImageDownloader imageTask = new ImageDownloader();
            Bitmap foodImage = imageTask.execute("http://www.greatgrubclub.com/" + foodURLs.get(chosenFood)).get();

            imageView.setImageBitmap(foodImage);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
