package com.example.niren.slice;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.niren.slice.data.Contract.TaskEntry;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    private final String[] PROJECTION = new String[]{TaskEntry._ID, TaskEntry.COL_DATE_START, TaskEntry.COL_DATE_END};
    private final int COL_IDX_ID = 0;
    private final int COL_IDX_DATE_START = 1;
    private final int COL_IDX_DATE_END = 2;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private WeekView mWeekView;
    private ItemListActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());



        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        mWeekView = (WeekView) findViewById(R.id.weekView);

        MonthLoader.MonthChangeListener monthChangeListener = new MonthLoader.MonthChangeListener() {
            @Override
            public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                List<WeekViewEvent> events = new ArrayList<>();
                GregorianCalendar cal = new GregorianCalendar(newYear, newMonth, 1);
                GregorianCalendar cal2 = new GregorianCalendar(newYear, newMonth + 1, 1);
                String selection = TaskEntry.COL_DATE_START + ">= ? AND " + TaskEntry.COL_DATE_END + " < ?";
                String[] args = new String[]{Long.toString(cal.getTimeInMillis()), Long.toString(cal2.getTimeInMillis())};
                Cursor cur = getContentResolver().query(TaskEntry.BASE_URI, PROJECTION, selection, args, null);
                int count = 0;
                GregorianCalendar eventStart = new GregorianCalendar();
                GregorianCalendar eventEnd = new GregorianCalendar();
                long eventId;
                while (cur.moveToNext()) {
                    eventStart.setTimeInMillis(cur.getLong(COL_IDX_DATE_START));
                    eventEnd.setTimeInMillis(cur.getLong(COL_IDX_DATE_END));
                    eventId = cur.getLong(COL_IDX_ID);
                    String s = "the quick brown fox jumps over the lazy dog just to show off his genetic superiority";
                    WeekViewEvent e = new WeekViewEvent(eventId, s, eventStart, eventEnd);
                    events.add(e);
                }

                if (newMonth == 3) {
                    events.add(new WeekViewEvent(++count, "testing 123", 2016, 3, 16, 0, 0, 2016, 3, 16, 1, 0));
                }
                return events;
            }
        };

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, event.getId());
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Intent intent = new Intent(mContext, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, event.getId());

                    mContext.startActivity(intent);
                }
            }
        });

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(monthChangeListener);

        // Set long press listener for events.
//        mWeekView.setEventLongPressListener(mEventLongPressListener);

        ContentValues cvs = new ContentValues();
        GregorianCalendar cal = new GregorianCalendar();
        cvs.put(TaskEntry.COL_DATE_START, cal.getTimeInMillis());
        cal.add(GregorianCalendar.HOUR_OF_DAY, 1);
        cvs.put(TaskEntry.COL_DATE_END, cal.getTimeInMillis());
//        getContentResolver().insert(Contract.TaskEntry.BASE_URI, cvs);
    }
}
