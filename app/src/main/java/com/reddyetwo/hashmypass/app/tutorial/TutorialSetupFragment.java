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

package com.reddyetwo.hashmypass.app.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.util.RandomPrivateKeyGenerator;

/**
 * Fragment containing the tutorial private key setup screen
 */
public class TutorialSetupFragment extends Fragment {

    private EditText mPrivateKeyText;
    private PrivateKeyChangedListener mPrivateKeyChangedListener;

    /**
     * Set the listener for private key changed events.
     *
     * @param listener the {@link com.reddyetwo.hashmypass.app.tutorial.TutorialSetupFragment.PrivateKeyChangedListener} instance
     */
    public void setPrivateKeyChangedListener(PrivateKeyChangedListener listener) {
        mPrivateKeyChangedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView =
                (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_setup, container, false);
        mPrivateKeyText = (EditText) rootView.findViewById(R.id.private_key_text);
        mPrivateKeyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mPrivateKeyChangedListener != null) {
                    mPrivateKeyChangedListener
                            .onPrivateKeyChanged(mPrivateKeyText.getText().toString());
                }
            }
        });
        mPrivateKeyText.setText(RandomPrivateKeyGenerator.generate());
        return rootView;
    }

    /**
     * Interface which can be implemented to listen to private key changed events.
     */
    public interface PrivateKeyChangedListener {

        /**
         * Method called when the private key has changed
         *
         * @param privateKey the private key
         */
        public void onPrivateKeyChanged(String privateKey);
    }
}
