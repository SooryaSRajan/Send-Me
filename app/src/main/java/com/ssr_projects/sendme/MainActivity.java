package com.ssr_projects.sendme;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.google.android.material.tabs.TabLayout;
import com.ssr_projects.sendme.Fragments.DownloadsFragment;
import com.ssr_projects.sendme.Fragments.UploadsFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;

import fi.iki.elonen.NanoHTTPD;

import static com.ssr_projects.sendme.Values.requestFilePickerCode;

public class MainActivity extends AppCompatActivity {

    NanoHTTPD webServer;
    UploadsFragment uploadsFragment;
    DownloadsFragment downloadsFragment;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        uploadsFragment = new UploadsFragment();
        downloadsFragment = new DownloadsFragment();

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(2);
        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager(), 0);
        viewPageAdapter.addFragment(uploadsFragment, "Upload");
        viewPageAdapter.addFragment(downloadsFragment, "Download");
        viewPager.setAdapter(viewPageAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.upload_selector);
        tabLayout.getTabAt(1).setIcon(R.drawable.download_selector);

        webServer = new WebServer(8000, MainActivity.this, downloadsFragment);
        //getSupportActionBar().setElevation(0);

        //TextView textView = findViewById(R.id.textView);
        //textView.setText("Connect to " + getWifiApIpAddress() + ":8000");

        Random rand = new Random();

        if(Values.requestList.isEmpty()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("KEY", String.valueOf(rand.nextInt()));
            Values.requestList.add(0, hashMap);
        }

        try {
            webServer.start(90000);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(webServer != null)
        webServer.stop();
    }

    public String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            Log.d(getClass().getName(), inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(getClass().getName(), ex.toString());
        }
        return null;
    }


    private static final float  MEGABYTE = 1024L * 1024L;

    public static float bytesToMeg(long bytes) {
        Log.e("LOG ", "bytesToMeg: " + bytes );
        return bytes / MEGABYTE ;
    }

    public String GetFileExtension(Uri uri)
    {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        // Return file Extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}