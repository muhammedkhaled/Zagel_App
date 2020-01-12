package com.example.Zagel_App.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import com.example.Zagel_App.R;

public class ImageViewActivity extends AppCompatActivity {

//    private ImageView imageView;
    private String imageUrl;
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

//        imageView = findViewById(R.id.image_viewer);
        webView = findViewById(R.id.web_view);
        imageUrl = getIntent().getStringExtra("url");


        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl(imageUrl);
//        Picasso.get().load(imageUrl).resize(600,200)
//                .centerInside().into(imageView);

    }
}
