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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;

import com.reddyetwo.hashmypass.app.IdenticonGenerationTask;

public class MasterKeyWatcher implements TextWatcher,
        IdenticonGenerationTask.OnIconGeneratedListener {

    private ImageView mIdenticonImageView;
    private IdenticonGenerationTask mTask;
    private Context mContext;

    public MasterKeyWatcher(Context context, ImageView identiconImageView) {
        mContext = context;
        mIdenticonImageView = identiconImageView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mIdenticonImageView.setVisibility(View.INVISIBLE);
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }
        if (s.toString().length() > 0) {
            mTask = new IdenticonGenerationTask(mContext, this);
            mTask.execute(SecurePassword.getPassword(s));
        }
    }

    @Override
    public void onIconGenerated(Bitmap bitmap) {
        mIdenticonImageView.setImageBitmap(bitmap);
        mIdenticonImageView.setVisibility(View.VISIBLE);
        mTask = null;
    }
}
