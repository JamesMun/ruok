package com.example.mun.ruok.ChartAxisSetting;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;

public class MyXAxisValueFormatter implements IAxisValueFormatter {

    private ArrayList<String> mValues;

    public MyXAxisValueFormatter(ArrayList<String> values) {
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        return mValues.get((int) value);
    }

    /** this is only needed if numbers are returned, else return 0 */
    /*@Override
    public int getDecimalDigits() { return 0; }*/
}