package com.example.myapplication.threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView ipTextView;
    private ImageView randomImageView;
    private ExecutorService executor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipTextView = findViewById(R.id.ipTextView);
        randomImageView = findViewById(R.id.randomImageView);
        Button fetchButton = findViewById(R.id.fetchButton);

        // Inicialitzar ExecutorService
        executor = Executors.newSingleThreadExecutor();

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });
    }

    private void fetchData() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Obtenir IP
                String ip = getDataFromUrl("https://api.myip.com");
                // Actualitzar el TextView amb la IP
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ipTextView.setText("La teva IP: " + ip);
                    }
                });

                // Obtenir una imatge aleatòria
                String imageUrl = "https://randomfox.ca/floof/";
                String imageUrlFromAPI = getImageUrlFromAPI(imageUrl);
                if (imageUrlFromAPI != null) {
                    // Transformar la imatge en Bitmap
                    Bitmap bitmap = downloadImage(imageUrlFromAPI);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            randomImageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        });
    }

    private String getDataFromUrl(String demoIdUrl) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(demoIdUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                in.close();
            } else {
                Log.e("Error", "Response Code: " + responseCode);
            }
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
        return result.toString();
    }

    private String getImageUrlFromAPI(String imageUrl) {
        String result = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            // Aquí hem d'analitzar el JSON per obtenir la URL de la imatge
            // Suponem que la resposta és JSON de la forma {"image": "url"}
            result = sb.toString();
            int startIndex = result.indexOf("\"image\":\"") + 9;
            int endIndex = result.indexOf("\"", startIndex);
            result = result.substring(startIndex, endIndex);

        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
        return result;
    }

    private Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        try {
            InputStream in = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
