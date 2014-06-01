package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.PasswordLength;

public class AddProfileActivity extends Activity {

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
        mNameEditText = (EditText) findViewById(R.id.add_profile_name);
        mPrivateKeyEditText = (EditText) findViewById(R.id
                .add_profile_private_key);
        mPasswordTypeSpinner = (Spinner) findViewById(R.id
                .add_profile_password_type);
        mPasswordLengthSpinner = (Spinner) findViewById(R.id
                .add_profile_password_length);
        mAddButton = (Button) findViewById(R.id.add_profile_add);
        mDiscardButton = (Button) findViewById(R.id.add_profile_discard);

        /* Populate password type spinner */
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPasswordTypeSpinner.setAdapter(adapter);

        /* Populate password length spinner */
        populatePasswordLengthSpinner(PasswordLength.DEFAULT);

        /* Open number picker dialog when the password length spinner is
        touched
         */
        mPasswordLengthSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showDialog();
                    return true;
                }

                return false;
            }
        });

        /* Add profile to database when Add button is pressed */
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataOpenHelper helper = new DataOpenHelper(AddProfileActivity
                        .this);
                SQLiteDatabase db = helper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DataOpenHelper.COLUMN_PROFILES_NAME,
                        mNameEditText.getText().toString());
                values.put(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY,
                        mPrivateKeyEditText.getText().toString());
                values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                        Integer.decode((String) mPasswordLengthSpinner
                                .getSelectedItem()));
                values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE,
                        mPasswordTypeSpinner.getSelectedItemPosition());

                db.insert(DataOpenHelper.PROFILES_TABLE_NAME,
                        null, values);

                /* TODO Check insert return value */

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