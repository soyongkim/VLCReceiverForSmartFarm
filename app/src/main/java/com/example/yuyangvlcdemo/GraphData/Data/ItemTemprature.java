package com.example.yuyangvlcdemo.GraphData.Data;

import com.example.yuyangvlcdemo.GraphData.Data.Item;

public class ItemTemprature implements Item {
    private double value;

    public ItemTemprature(double _value)
    {
        value = _value;
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public double getDoubleValue() {
        return value;
    }

    @Override
    public void setValue(int v) {

    }

    @Override
    public void setValue(double v) {
        value = v;
    }
}
