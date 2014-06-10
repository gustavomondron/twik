package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.data.PasswordLength;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;

public class AddProfileActivity extends Activity {

    // State bundle keys
    private static final String KEY_PASSWORD_LENGTH = "password_length";

    // UI Widgets
    private EditText mNameEditText;
    private EditText mPrivateKeyEditText;
    private Spinner mPasswordTypeSpinner;
    private Spinner mPasswordLengthSpinner;
    private Button mAddButton;
    private Button mDiscardButton;

    private ArrayAdapter<String> mPasswordLengthAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        /* Enable navigation in action bar */
        getActionBar().setDisplayHomeAsUpEnabled(true);

        /* Get UI widgets */
        mNameEditText = (EditText) findViewById(R.id.profile_name_text);
        mPrivateKeyEditText = (EditText) findViewById(R.id.private_key_text);
        mPasswordTypeSpinner =
                (Spinner) findViewById(R.id.password_type_spinner);
        mPasswordLengthSpinner =
                (Spinner) findViewById(R.id.password_length_spinner);
        mAddButton = (Button) findViewById(R.id.add_button);
        mDiscardButton = (Button) findViewById(R.id.discard_button);

        /* Populate password type spinner */
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mPasswordTypeSpinner.setAdapter(adapter);

        /* Populate password length spinner */
        int passwordLength;
        if (savedInstanceState != null) {
            passwordLength = savedInstanceState.getInt(KEY_PASSWORD_LENGTH);
        } else {
            passwordLength = PasswordLength.DEFAULT;
        }
        populatePasswordLengthSpinner(passwordLength);

        /* Open number picker dialog when the password length spinner is
        touched
         */
        mPasswordLengthSpinner.setOnTouchListener(
                new MovementTouchListener(this,
                        new MovementTouchListener.OnPressedListener() {
                            @Override
                            public void onPressed() {
                                showDialog();
                            }
                        })
        );

        /* Add profile to database when Add button is pressed */
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long profileID =
                        ProfileSettings.insertProfileSettings(AddProfileActivity
                                        .this,
                                mNameEditText.getText().toString(),
                                mPrivateKeyEditText.getText().toString(),
                                Integer.decode((String) mPasswordLengthSpinner
                                        .getSelectedItem()),
                                PasswordType.values()[mPasswordTypeSpinner
                                        .getSelectedItemPosition()]
                        );

                /* TODO Check that profileID != -1 */

                /* Navigate to previous activity */
                NavUtils.navigateUpFromSameTask(AddProfileActivity.this);
            }
        });

        /* Discard values when Discard button is pressed */
        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Navigate to previous activity */
                NavUtils.navigateUpFromSameTask(AddProfileActivity.this);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PASSWORD_LENGTH, Integer.parseInt(
                        (String) mPasswordLengthSpinner.getSelectedItem())
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showDialog() {
        PasswordLengthDialogFragment dialogFragment =
                new PasswordLengthDialogFragment();
        dialogFragment.setPasswordLength(Integer.parseInt(
                (String) mPasswordLengthSpinner.getSelectedItem()));
        dialogFragment.setOnSelectedListener(
                new PasswordLengthDialogFragment.OnSelectedListener() {
                    @Override
                    public void onPasswordLengthSelected(int length) {
                        populatePasswordLengthSpinner(length);
                    }
                }
        );

        dialogFragment.show(getFragmentManager(), "passwordLength");
    }

    private void populatePasswordLengthSpinner(int length) {
        mPasswordLengthAdapter =
                new ArrayAdapter<String>(AddProfileActivity.this,
                        android.R.layout.simple_spinner_item,
                        new String[]{String.valueOf(length)});
        mPasswordLengthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mPasswordLengthSpinner.setAdapter(mPasswordLengthAdapter);
    }

}