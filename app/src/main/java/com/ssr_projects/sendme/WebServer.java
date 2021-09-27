package com.ssr_projects.sendme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.ssr_projects.sendme.Fragments.DownloadsFragment;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

import static com.ssr_projects.sendme.Values.requestData;
import static java.net.HttpURLConnection.HTTP_OK;

public class WebServer extends NanoHTTPD {

    Context context;
    InputStream inputStream;
    InputStreamReader inputStreamReader;
    BufferedReader bufferReader;
    int i = 0;
    DownloadsFragment downloadsFragment;
    final String TAG = getClass().getName();

    @SuppressLint("ResourceType")
    public WebServer(int port, Context context, DownloadsFragment downloadsFragment) {
        super(port);
        this.context = context;
        this.downloadsFragment = downloadsFragment;
    }

    @Override
    public Response serve(IHTTPSession session) {

        inputStream = context.getResources().openRawResource(R.raw.index);
        inputStreamReader = new InputStreamReader(inputStream);
        bufferReader = new BufferedReader(inputStreamReader);


        if (session.getMethod() == Method.POST) {
            Log.e(getClass().getName(), "serve: " + session);
        }
        Log.e(getClass().getName(), "serve: " + session.getUri());

        if (session.getUri().equals("/background.jpg")) {
            InputStream mbuffer = null;
            try {
                Log.e(getClass().getName(), "serve: Served Image");
                mbuffer = context.getAssets().open("background.jpg");
                return newFixedLengthResponse(Response.Status.OK, "image/png", mbuffer, 29000);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
        } else if (session.getUri().equals("/")) {
            String readLine = "";
            String htmlPage = "";
            try {
                while ((readLine = bufferReader.readLine()) != null) {
                    htmlPage += (readLine + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            htmlPage = htmlPage.replace("MYKEY", "http://" + getWifiApIpAddress() + ":8000/");
            Response resp = newFixedLengthResponse(htmlPage);
            resp.setChunkedTransfer(true);
            return resp;
        } else if (session.getUri().equals("/list_data")) {
            i++;
            JSONArray jsArray = new JSONArray(Values.requestList);
            Response resp = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, String.valueOf(jsArray));
            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Max-Age", "3628800");
            resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
            resp.addHeader("Access-Control-Allow-Headers", "Authorization");

            resp.setChunkedTransfer(true);
            return resp;
        } else if (session.getUri().contains("/file")) {
            Log.e("URI", "serve: " + session.getUri() + "       " + session.getParms());

            String[] fileInfo = session.getUri().split("\\*");

            Map<String, String> files = new HashMap<>();
            try {
                session.parseBody(files);
                createDirIfNotExists("ShareMe");
                File dst = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/ShareMe/" + fileInfo[1]);
                dst.createNewFile();
                File src = new File(files.get("file"));
                try {

                    copy(src, dst);
                    HashMap<String, String> hashMap = new HashMap<>();
                    int fileSize = Integer.parseInt(String.valueOf(src.length()/1024));
                    hashMap.put("NAME", fileInfo[1]);
                    hashMap.put("DATE", DateFormat.getDateInstance().format(new Date()));
                    hashMap.put("SIZE", fileSize + " mb");
                    hashMap.put("URI", String.valueOf(src));
                    Log.d(TAG, "serve: FILE INFO " + hashMap.get("NAME") + " " + hashMap.get("SIZE"));
                    downloadsFragment.addData(hashMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("TAG", "serve: " + files.get("file"));
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }

            Response resp = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Received");
            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Max-Age", "3628800");
            resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
            resp.addHeader("Access-Control-Allow-Headers", "Authorization");
            resp.setChunkedTransfer(true);

            return resp;
        } else if (session.getUri().equals("/fonts.css")) {
            InputStream mbuffer = null;
            try {
                Log.e(getClass().getName(), "serve: Served Css");
                mbuffer = context.getAssets().open("fonts.css");
                return newFixedLengthResponse(Response.Status.OK, "text/css", mbuffer, 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (session.getUri().equals("/bootstrap.css")) {
            InputStream mbuffer = null;
            try {
                Log.e(getClass().getName(), "serve: Served Css");
                mbuffer = context.getAssets().open("bootstrap.css");
                return newFixedLengthResponse(Response.Status.OK, "text/css", mbuffer, 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (session.getUri().contains("/")) {
            String[] uri = session.getUri().split("/");
            Log.e("Web server", "serve: " + uri[1]);


            for (HashMap<String, String> hashMap : Values.requestList) {
                if (hashMap.get("NAME") != null) {
                    if (Objects.equals(hashMap.get("NAME"), uri[1])) {
                        try {
                            InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(hashMap.get("URI")));
                            String[] mimeType = hashMap.get("MIME").split("/");
                            if (inputStream != null) {
                                return newChunkedResponse(Response.Status.OK, "application/" + mimeType[1], inputStream);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return newFixedLengthResponse("Sorry, the file you requested was not found");
                        }

                        break;
                    }
                }
            }

        }

        return newFixedLengthResponse("404 not found");
    }

    public String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements(); ) {
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

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                ret = false;
            }
        }
        return ret;
    }

}
