package com.wanztudio.iak.stockhawk.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.wanztudio.iak.stockhawk.R;
import com.wanztudio.iak.stockhawk.data.Contract;
import com.wanztudio.iak.stockhawk.utils.Constants;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * For LEARNING
 * Updated by Ridwan Ismail on 20 Mei 2017
 * You can contact me at : ismail.ridwan98@gmail.com
 * -------------------------------------------------
 * STOCK HAWK
 * com.wanztudio.iak.stockhawkui
 */


public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_symbol)
    TextView tvSymbol;
    @BindView(R.id.tv_price_detail)
    TextView tvPrice;
    @BindView(R.id.tv_change_detail)
    TextView tvChange;
    @BindView(R.id.chart)
    LineChart mChart;

    private String symbol;

    //Colors
    private int axisColor;
    private int chartColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        symbol = getIntent().getExtras().getString(Constants.EXTRA_SYMBOL);

        updateView();
        setToolBar(symbol);

        axisColor = ContextCompat.getColor(getApplicationContext(),R.color.white);
        chartColor = ContextCompat.getColor(getApplicationContext(),R.color.chart_color);

        new DrawStockAsyncTask().execute(symbol);


    }

    private void updateView(){

        tvName.setText(symbol);
        //  tvSymbol.setText(symbol);
//        tvPrice.setText("21");
//        tvChange.setText("18");
    }




    public void setToolBar(String title) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // AsyncTask that retrieve data and draw chart in a separate thread
    class DrawStockAsyncTask extends AsyncTask<String, Void, Cursor>{


        @Override
        protected Cursor doInBackground(String... strings) {
            String symbol = strings[0];

            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            Cursor cursor = contentResolver.query(Contract.Quote.makeUriForStock(symbol),
                    null,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL
            );
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            Cursor mData = cursor;
            drawChart(mData);
        }

        private void drawChart(Cursor cursor) {

            assert cursor != null;
            cursor.moveToFirst();

            String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
            String history = cursor.getString(Contract.Quote.POSITION_HISTORY);

            List<Entry> entries = new ArrayList<>();

            CSVReader reader = new CSVReader(new StringReader(history));
            String[] nextLine;
            final List<Long> xAxisValues = new ArrayList<>();
            int xAxisPosition = 0;


            cursor.close();

            try {
                while ((nextLine = reader.readNext()) != null) {
                    xAxisValues.add(Long.valueOf(nextLine[0]));
                    Entry entry = new Entry(
                            xAxisPosition, // timestamp
                            Float.valueOf(nextLine[1])  // symbol value
                    );
                    entries.add(entry);
                    xAxisPosition++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //style
            LineDataSet dataSet = new LineDataSet(entries, symbol);
            dataSet.setColors(chartColor);
            dataSet.setDrawCircles(false);
            dataSet.setLineWidth(2f);
            //dataSet.setCircleColor(chartColor);

            LineData lineData = new LineData(dataSet);
            mChart.fitScreen();
            mChart.setClickable(false);
            mChart.setData(lineData);
            mChart.setDescription(null);
            mChart.getLegend().setTextColor(chartColor);


            //mChart.getDescription().setTextColor(chartColor);
            mChart.animateX(2000, Easing.EasingOption.Linear);

            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setTextColor(axisColor);
            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setTextColor(axisColor);

            XAxis xAxis = mChart.getXAxis();
            xAxis.setTextColor(axisColor);
            //style fin
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Date date = new Date(xAxisValues.get(xAxisValues.size() - (int) value - 1));
                    return new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH)
                            .format(date);
                }
            });

            mChart.invalidate();
        }
    }

}
