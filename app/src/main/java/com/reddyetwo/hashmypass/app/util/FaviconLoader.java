/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Twik.
 *
 * Twik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twik is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Twik.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.reddyetwo.hashmypass.app.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.HashMyPassApplication;
import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.Favicon;
import com.reddyetwo.hashmypass.app.data.FaviconSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader which gets the favicon of a website and shows it in a {@link android.widget.TextView}
 */
public class FaviconLoader {

    private static final long LOAD_TIMEOUT = 3000;
    private static final int PROGRESS_STOPPED = 10;
    private static final int MASK_LOW_NIBBLE = 15;
    private final Context mContext;
    private List<String> mTouchIconUrlList;
    private String mUrl;
    private OnFaviconLoaded mOnFaviconLoaded;

    /**
     * Constructor
     *
     * @param context the context
     */
    public FaviconLoader(Context context) {
        mContext = context;
    }

    /**
     * Set a {@link com.reddyetwo.hashmypass.app.data.Tag} favicon as the background of a {@link android.widget.TextView}
     *
     * @param context  the {@link android.content.Context} instance
     * @param textView the text view
     * @param tag      the tag
     */
    public static void setAsBackground(Context context, TextView textView, Tag tag) {
        if (tag == null || tag.getName().length() == 0) {
            setTextViewBackground(textView, null);
            return;
        }

        Drawable faviconDrawable = null;
        if (tag.getSite() != null) {
            Favicon favicon = FaviconSettings.getFavicon(context, tag.getSite());
            if (favicon != null) {
                faviconDrawable = new BitmapDrawable(context.getResources(), favicon.getIcon());
            }
        }
        boolean writeInitial = faviconDrawable == null;
        if (faviconDrawable == null) {
            faviconDrawable = context.getResources().getDrawable(R.drawable.favicon_background);

            ((GradientDrawable) faviconDrawable)
                    .setColor(getBackgroundColor(context, tag.getName().toCharArray()));
        }

        setTextViewBackground(textView, faviconDrawable);

        if (writeInitial) {
            textView.setText(tag.getName().substring(0, 1));
        }
    }

    private static void setTextViewBackground(TextView textView, Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            textView.setBackgroundDrawable(background);
        } else {
            textView.setBackground(background);
            textView.setText("");
        }
    }

    private static int getBackgroundColor(Context context, char[] input) {
        int[] colors = context.getResources().getIntArray(R.array.color_palette_normal);
        byte[] digest = PasswordHasher.calculateDigest(input);

        // Unsigned int, module colors length
        int color = (digest[0] & MASK_LOW_NIBBLE) % colors.length;
        return colors[color];
    }

    /**
     * Get the favicon of a website
     *
     * @param url             the site URL
     * @param onFaviconLoaded the {@link com.reddyetwo.hashmypass.app.util.FaviconLoader.OnFaviconLoaded} listener
     */
    public void load(String url, OnFaviconLoaded onFaviconLoaded) {
        mUrl = url;
        mOnFaviconLoaded = onFaviconLoaded;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            CookieSyncManager.createInstance(mContext);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);

        final WebView webView = new WebView(mContext);
        webView.getSettings().setLoadsImagesAutomatically(false);
        webView.getSettings().setJavaScriptEnabled(false);
        final FaviconChromeClient chromeClient = new FaviconChromeClient();

        // Prevent page load from getting stuck
        final Handler handler = new Handler();
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView.getProgress() == PROGRESS_STOPPED) {
                    // Stuck at 10%: Connection established but not loading
                    // Thank you, AOSP!
                    webView.reload();
                    handler.postDelayed(this, LOAD_TIMEOUT);
                }
            }
        };

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                                        String failingUrl) {
                // Nothing to do
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                handler.removeCallbacks(timeoutRunnable);
                loadFavicon(mTouchIconUrlList);
                view.clearCache(true);
                super.onPageFinished(view, url);
            }
        });


        webView.setWebChromeClient(chromeClient);
        webView.loadUrl(url);

        // Set timeout
        handler.postDelayed(timeoutRunnable, LOAD_TIMEOUT);
    }

    private void loadFavicon(List<String> touchIconUrlList) {
        URL faviconURL;
        URL fallbackURL = null;
        try {
            // Generate URL
            if (!touchIconUrlList.isEmpty()) {
                faviconURL = new URL(touchIconUrlList.get(touchIconUrlList.size() - 1));
            } else {
                // Look for favicon
                URL inputUrl = new URL(mUrl);
                faviconURL = new URL(inputUrl.getProtocol() + "://" +
                        inputUrl.getHost() +
                        "/favicon.ico");
                fallbackURL = new URL("http://www.google.com/s2/favicons?domain=" + mUrl);
            }

            // Get bitmap from URL
            new RetrieveImageTask().execute(faviconURL, fallbackURL);
        } catch (Exception e) {
            Log.e(HashMyPassApplication.LOG_TAG, "Error loading favicon: " + e);
        }
    }

    /* Follow redirections. Returns null if not exists */
    private URL getFinalURL(URL url) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                // Paged moved permanently, get the new URL
                con.disconnect();
                HttpURLConnection.setFollowRedirects(true);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                responseCode = con.getResponseCode();
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                URL finalURL = con.getURL();
                con.disconnect();
                return finalURL;
            } else {
                con.disconnect();
                return null;
            }
        } catch (Exception e) {
            Log.e(HashMyPassApplication.LOG_TAG, "Error getting favicon URL: " + e);
            return null;
        }
    }

    /**
     * Interface which can be implemented to listen to favicon loaded events
     */
    public interface OnFaviconLoaded {

        /**
         * Method called when the favicon has been loaded
         *
         * @param icon the favicon {@link android.graphics.drawable.BitmapDrawable}
         */
        public void onFaviconLoaded(BitmapDrawable icon);
    }

    private class FaviconChromeClient extends WebChromeClient {

        /**
         * Constructor
         */
        public FaviconChromeClient() {
            super();
            mTouchIconUrlList = new ArrayList<>();
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            /* Awesome Android feature: sometimes we get an url which is just
             the host name... */
            if (url.endsWith(".png")) {
                // Sometimes, the web domain is received as a touch icon Url.
                // Thank you, AOSP.
                mTouchIconUrlList.add(url);
            }
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }
    }

    private class RetrieveImageTask extends AsyncTask<URL, Void, BitmapDrawable> {

        /**
         * Constructor
         */
        public RetrieveImageTask() {
            super();
        }

        @Override
        protected BitmapDrawable doInBackground(URL... params) {
            URL url;
            if (params[1] != null) {
                url = getFinalURL(params[0]);
                if (url == null) {
                    url = params[1];
                }
            } else {
                url = params[0];
            }

            try {
                return (BitmapDrawable) BitmapDrawable
                        .createFromStream((InputStream) url.getContent(), "favicon");
            } catch (IOException e) {
                Log.e(HashMyPassApplication.LOG_TAG, "Error downloading favicon: " + e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(BitmapDrawable image) {
            if (image != null) {
                mOnFaviconLoaded.onFaviconLoaded(image);
            }
        }
    }
}
