package com.wanztudio.iak.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.wanztudio.iak.stockhawk.R;
import com.wanztudio.iak.stockhawk.data.Contract;
import com.wanztudio.iak.stockhawk.utils.Constants;

/**
 * For LEARNING
 * Updated by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.wanztudio.iak.stockhawk.widget
 */

public class QuoteWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {

            private Cursor data = null;
            @Override
            public void onCreate() {
                // do nothing
            }

            @Override
            public void onDataSetChanged() {
                if (data !=null){
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        Contract.Quote.URI,
                        new String[]{},
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                data.close();
            }

            @Override
            public int getCount() {
                return data == null ? 0: data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_item);
                if (data.moveToPosition(position)){
                    float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                    if (rawAbsoluteChange > 0) {
                        views.setViewVisibility(R.id.iv_trending_up, View.VISIBLE);
                        views.setViewVisibility(R.id.iv_trending_down, View.GONE);

                    } else {
                        views.setViewVisibility(R.id.iv_trending_down, View.VISIBLE);
                        views.setViewVisibility(R.id.iv_trending_up, View.GONE);

                    }

                    views.setTextViewText(R.id.tv_widget_symbol, data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                    views.setTextViewText(R.id.tv_widget_price, "$"+data.getString(data.getColumnIndex(Contract.Quote.COLUMN_PRICE)));
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Constants.EXTRA_SYMBOL, data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                views.setOnClickFillInIntent(R.id.widget, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data !=null && data.moveToPosition(position)){
                    return data.getLong(Contract.Quote.POSITION_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
