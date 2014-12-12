package com.hiqes.android.examples.basicloader;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks {
    private static final String         TAG = MainActivity.class.getName();
    private static final int            LOADER_MY_ID = 1;

    private static final String[]       IN_CALL_COLUMNS = {
        CallLog.Calls.CACHED_NAME,
        CallLog.Calls.DATE,
        CallLog.Calls.NUMBER,
        CallLog.Calls.DURATION
    };

    private static final int[]          IN_CALL_RES_IDS = {
        R.id.text_caller_name,
        R.id.text_call_date,
        R.id.text_ph_number,
        R.id.text_call_duration
    };

    private SimpleCursorAdapter         mAdapter;
    private ListView                    mInCallList;

    private SimpleCursorAdapter.ViewBinder MY_VIEW_BINDER = new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            boolean             ret = false;
            String              tmp;

            switch (view.getId()) {
                case R.id.text_caller_name:
                    tmp = cursor.getString(columnIndex);
                    if ((tmp == null) || (tmp.length() == 0)) {
                        ((TextView)view).setText(getString(R.string.text_name_unknown));
                        ret = true;
                    } else {
                        ret = false;
                    }

                    break;

                case R.id.text_call_date:
                    SimpleDateFormat df;
                    df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                    tmp = df.format(new Date(cursor.getLong(columnIndex)));
                    ((TextView)view).setText(tmp);
                    ret = true;
                    break;

                case R.id.text_ph_number:
                    tmp = PhoneNumberUtils.formatNumber(cursor.getString(columnIndex));
                    ((TextView)view).setText(tmp);
                    ret = true;
                    break;
            }
            return ret;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInCallList = (ListView)findViewById(R.id.list_in_calls);

        mAdapter = new SimpleCursorAdapter(this,
                                           R.layout.list_recent_in_call,
                                           null,
                                           IN_CALL_COLUMNS,
                                           IN_CALL_RES_IDS,
                                           0);
        mAdapter.setViewBinder(MY_VIEW_BINDER);
        mInCallList.setAdapter(mAdapter);

        Log.d(TAG, "onResume: initializing loader...");
        getLoaderManager().initLoader(LOADER_MY_ID, null, this);
    }


    //  The LoaderManager.LoaderCallbacks implementation for our app
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Loader                  ret = null;
        String                  sel;
        String[]                selArgs;

        Log.d(TAG, "onCreateLoader for id: " + Integer.toString(id));

        switch (id) {
            case LOADER_MY_ID:
                sel = CallLog.Calls.TYPE + " = ?";
                selArgs = new String[] { Integer.toString(CallLog.Calls.INCOMING_TYPE) };
                ret = new CursorLoader(this,
                                       CallLog.Calls.CONTENT_URI,
                                       null,
                                       sel,
                                       selArgs,
                                       CallLog.Calls.DEFAULT_SORT_ORDER);
                break;

            default:
                Log.w(TAG, "Unhandled id: " + Integer.toString(id));
                break;
        }

        return ret;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.d(TAG, "onLoadFinished for id: " + Integer.toString(loader.getId()));

        //  We only have the single Loader, so no need to verify ID
        mAdapter.swapCursor((Cursor)data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(TAG, "onLoaderReset for id: " + Integer.toString(loader.getId()));
        mAdapter.swapCursor(null);
    }
}
