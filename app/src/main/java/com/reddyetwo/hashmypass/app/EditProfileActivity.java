package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.PasswordLength;


public class EditProfileActivity extends Activity {

    public static final String EXTRA_PROFILE_ID = "profile_id";
    private EditText mNameEditText;
    private EditText mPrivateKeyEditText;
    private Spinner mPasswordLengthSpinner;
    private Spinner mPasswordTypeSpinner;
    private Button mDiscardButton;
    private Button mSaveButton;
    private ArrayAdapter<String> mPasswordLengthAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final long profileID = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);

        DataOpenHelper helper = new DataOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        String query = "SELECT * FROM " + DataOpenHelper.PROFILES_TABLE_NAME
                + " WHERE _id=" + profileID;
        Cursor cursor = db.rawQuery(query, null);
        if (!cursor.moveToFirst()) {
            // TODO This should be an error
        }

        getActionBar().setSubtitle(cursor.getString(cursor.getColumnIndex
                (DataOpenHelper.COLUMN_PROFILES_NAME)));


        /* Get UI widgets */
        mNameEditText = (EditText) findViewById(R.id.edit_profile_name);
        mPrivateKeyEditText = (EditText) findViewById(R.id
                .edit_profile_private_key);
        mPasswordLengthSpinner = (Spinner) findViewById(R.id
                .edit_profile_password_length);
        mPasswordTypeSpinner = (Spinner) findViewById(R.id
                .edit_profile_password_type);
        mDiscardButton = (Button) findViewById(R.id.edit_profile_discard);
        mSaveButton = (Button) findViewById(R.id.edit_profile_save);

        /* Populate text fields */
        mNameEditText.setText(cursor.getString(cursor.getColumnIndex
                (DataOpenHelper.COLUMN_PROFILES_NAME)));

        mPrivateKeyEditText.setText(cursor.getString(cursor.getColumnIndex
                (DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY)));

        /* Populate password type spinner */
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPasswordTypeSpinner.setAdapter(adapter);
        mPasswordTypeSpinner.setSelection(cursor.getInt(cursor.getColumnIndex
                (DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE)));

        /* Populate password length spinner */
        mPasswordLengthAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                new String[] { Integer.toString(cursor.getInt(cursor
                        .getColumnIndex(
                                DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH))) });
        mPasswordLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPasswordLengthSpinner.setAdapter(mPasswordLengthAdapter);

        /* Open number picker dialog when the password length spinner is
           touched */
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
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataOpenHelper helper = new DataOpenHelper(EditProfileActivity
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

                db.update(DataOpenHelper.PROFILES_TABLE_NAME, values,
                        "_id=" + profileID, null);
                /* TODO Check that update return value == 1 */

                /* Navigate to previous activity */
                NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
            }
        });


        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showDialog() {
        final Dialog d = new Dialog(EditProfileActivity.this);
        d.setTitle(getString(R.string.password_length));
        d.setContentView(R.layout.dialog_number_picker);

        Button bDiscard = (Button) d.findViewById(R.id.number_picker_discard);
        Button bOk = (Button) d.findViewById(R.id.number_picker_ok);
        final NumberPicker picker = (NumberPicker) d.findViewById(R.id
                .numberPicker);
        picker.setMinValue(PasswordLength.MIN_LENGTH);
        picker.setMaxValue(PasswordLength.MAX_LENGTH);
        picker.setValue(Integer.parseInt(
                (String) mPasswordLengthSpinner.getSelectedItem()));

        bDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordLengthAdapter = new ArrayAdapter<String>
                        (EditProfileActivity.this,
                                android.R.layout.simple_spinner_item,
                                new String[] { String.valueOf(picker.getValue()) });
                mPasswordLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mPasswordLengthSpinner.setAdapter(mPasswordLengthAdapter);
                d.dismiss();
            }
        });

        d.show();
    }
}
