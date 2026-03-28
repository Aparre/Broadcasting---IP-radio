package com.macape.r4adio;

/*
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.webkit.JavascriptInterface;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView myWebView = findViewById(R.id.webview);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // Added for better stability

        // Critical: prevents the music from stopping when you switch apps
        settings.setMediaPlaybackRequiresUserGesture(false);

        myWebView.setWebViewClient(new WebViewClient());
        //myWebView.loadUrl("http://192.168.84.101:5000");
        myWebView.loadUrl("http://192.168.43.41:5000");
        //myWebView.loadUrl("https://f408-2405-8d40-4c59-2abc-6f-748c-dc05-618.ngrok-free.app/");
        //myWebView.loadUrl("https://e753-2405-8d40-4c59-2abc-a519-1317-c0ca-a7ca.ngrok-free.app/");
    }
}
 */

/*
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        String icecastUrl = "https://wombat-finer-certainly.ngrok-free.app/live";
        MediaItem mediaItem = MediaItem.fromUri(icecastUrl);
        player.setMediaItem(mediaItem);

        // 2. Setup the Button
        Button playStopBtn = findViewById(R.id.btn_play_stop);

        playStopBtn.setOnClickListener(v -> {
            if (!isPlaying) {
                // START PLAYING
                player.prepare();
                player.play();
                playStopBtn.setText("Stop Stream");
                playStopBtn.setBackgroundColor(android.graphics.Color.RED);
                isPlaying = true;
            } else {
                // STOP PLAYING
                player.stop();
                playStopBtn.setText("Play Stream");
                playStopBtn.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
                isPlaying = false;
            }
        });

        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == ExoPlayer.STATE_BUFFERING) {
                    // Show a loading spinner if you have one
                    playStopBtn.setText("Connecting...");
                } else if (state == ExoPlayer.STATE_READY) {
                    playStopBtn.setText("Stop Stream");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release(); // Free up resources when app closes
        }
    }
}
 */

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import okhttp3.Dns;
import java.util.Arrays;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.dnsoverhttps.DnsOverHttps;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private boolean isPlaying = false;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Create a "Bootstrap" client to find the DNS server
        OkHttpClient bootstrapClient = new OkHttpClient.Builder().build();

        // 2. Build the DNS-over-HTTPS resolver (Using Google DNS)
        DnsOverHttps dns = null;
        try {
            dns = new DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(HttpUrl.get("https://dns.google/dns-query"))
                    .bootstrapDnsHosts(Collections.singletonList(InetAddress.getByName("8.8.8.8")))
                    .build();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // 3. Create the Main Client with DoH and ngrok bypass headers
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .dns(dns)
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                .header("ngrok-skip-browser-warning", "true") // Bypass ngrok landing page
                                .build()
                ))
                .build();

        // 4. Connect OkHttp to ExoPlayer
        OkHttpDataSource.Factory dataSourceFactory = new OkHttpDataSource.Factory(okHttpClient);

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();

        // 5. Use your URL
        String icecastUrl = "https://wombat-finer-certainly.ngrok-free.app/live";
        player.setMediaItem(MediaItem.fromUri(icecastUrl));

        // 2. Setup the Button
        Button playStopBtn = findViewById(R.id.btn_play_stop);

        playStopBtn.setOnClickListener(v -> {
            if (!isPlaying) {
                // START PLAYING
                player.prepare();
                player.play();
                playStopBtn.setText("Stop Stream");
                playStopBtn.setBackgroundColor(android.graphics.Color.RED);
                isPlaying = true;
            } else {
                // STOP PLAYING
                player.stop();
                playStopBtn.setText("Play Stream");
                playStopBtn.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
                isPlaying = false;
            }
        });

        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == ExoPlayer.STATE_BUFFERING) {
                    // Show a loading spinner if you have one
                    playStopBtn.setText("Connecting...");
                } else if (state == ExoPlayer.STATE_READY) {
                    playStopBtn.setText("Stop Stream");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release(); // Free up resources when app closes
        }
    }

}