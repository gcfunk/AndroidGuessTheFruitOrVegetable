package com.example.gregfunk.androidguessthefruitorvegetable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    ArrayList<String> foodURLs = new ArrayList<String>();
    ArrayList<String> foodNames = new ArrayList<String>();
    int chosenFood = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    public void foodChosen(View view) {
        String message = "";
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            message = "Correct!";
        } else {
            message = "Wrong. It's " + foodNames.get(chosenFood);
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        getNextFood();
    }

    public void getNextFood() {
        Random random = new Random();
        chosenFood = random.nextInt(foodURLs.size());

        try {
            ImageDownloader imageTask = new ImageDownloader();
            Bitmap foodImage = imageTask.execute("http://www.greatgrubclub.com/" + foodURLs.get(chosenFood)).get();

            imageView.setImageBitmap(foodImage);

            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation = 0;
            for(int i=0; i<4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = foodNames.get(chosenFood);
                } else {
                    incorrectAnswerLocation = random.nextInt(foodURLs.size());
                    while (incorrectAnswerLocation == chosenFood) {
                        incorrectAnswerLocation = random.nextInt(foodURLs.size());
                    }
                    answers[i] = foodNames.get(incorrectAnswerLocation);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute("http://www.greatgrubclub.com/a-z-fruit-veg").get();

            // cut out glossary images and text
            Pattern p = Pattern.compile("<dl class=\"glossary\">(.*?)</dl>");
            Matcher m = p.matcher(result);
            m.find();
            String htmlFragment = m.group(1);

            // for each row, cut out image URL
            Pattern p2 = Pattern.compile("img src=\"(.*?)\"");
            Matcher m2 = p2.matcher(htmlFragment);
            while (m2.find()) {
                foodURLs.add(m2.group(1));
            }

            // for each row, cut out name
            Pattern p3 = Pattern.compile("alt=\"(.*?)\"");
            Matcher m3 = p3.matcher(htmlFragment);
            while (m3.find()) {
                foodNames.add(m3.group(1));
            }

            getNextFood();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
