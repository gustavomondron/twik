package com.reddyetwo.hashmypass.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;


public class MainActivity extends Activity {

    private final static int ID_ADD_PROFILE = -1;
    private long mSelectedProfileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataOpenHelper helper = new DataOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DataOpenHelper.PROFILES_TABLE_NAME);
        Cursor cursor = queryBuilder.query(db,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_PROFILES_NAME}, null, null, null,
                null, null
        );

        MatrixCursor extras = new MatrixCursor(
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_PROFILES_NAME});
        extras.addRow(new String[]{Integer.toString(ID_ADD_PROFILE),
                getResources().getString(R.string.action_add_profile)});
        MergeCursor mergeCursor = new MergeCursor(new Cursor[]{cursor, extras});

        ProfileAdapter adapter = new ProfileAdapter(this, mergeCursor, 0);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter,
                new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition,
                                                            long itemId) {
                        mSelectedProfileID = itemId;
                        if (itemId == ID_ADD_PROFILE) {
                            Intent intent = new Intent(getBaseContext(),
                                    AddProfileActivity.class);
                            startActivity(intent);
                        }

                        return false;
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_edit_profile) {
            Intent intent = new Intent(getBaseContext(),
                    EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.EXTRA_PROFILE_ID,
                    mSelectedProfileID);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ProfileAdapter extends CursorAdapter {

        private ProfileAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(android.R.layout.simple_dropdown_item_1line,
                    parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String profileName = cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_PROFILES_NAME));
            ((TextView) view).setText(profileName);
        }
    }

}
