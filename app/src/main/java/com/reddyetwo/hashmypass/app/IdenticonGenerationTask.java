/*
 * Copyright 2014 Red Dye No. 2
 * Copyright 2014 David Hamp-Gonsalves
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

package com.reddyetwo.hashmypass.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.reddyetwo.hashmypass.app.util.IdenticonGenerator;

/**
 * {@link android.os.AsyncTask} for generating identicons in background
 */
public class IdenticonGenerationTask extends AsyncTask<char[], Void, Void> {

    private final Context mContext;
    private final OnIconGeneratedListener mListener;
    private Bitmap mBitmap;

    /**
     * Constructor
     *
     * @param context  the {@link android.content.Context} instance
     * @param listener the {@link com.reddyetwo.hashmypass.app.IdenticonGenerationTask.OnIconGeneratedListener} listener
     */
    public IdenticonGenerationTask(Context context, OnIconGeneratedListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(char[]... params) {
        if (params.length > 0 && params[0].length > 0) {
            mBitmap = IdenticonGenerator.generate(mContext, params[0]);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.onIconGenerated(mBitmap);
    }

    /**
     * Interface which can be implemented to listen to icon generated events
     */
    public interface OnIconGeneratedListener {

        /**
         * Method called when the icon has been generated
         *
         * @param bitmap the icon {@link android.graphics.Bitmap}
         */
        void onIconGenerated(Bitmap bitmap);
    }
}
