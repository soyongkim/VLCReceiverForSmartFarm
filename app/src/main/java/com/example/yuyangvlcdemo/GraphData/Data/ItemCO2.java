package com.example.yuyangvlcdemo.GraphData.Data;

import com.example.yuyangvlcdemo.GraphData.Data.Item;

public class ItemCO2 implements Item {
    private int value;
    public ItemCO2(int _value)
    {
        value = _value;
    }

    @Override
    public int getIntValue() {
        return value;
    }

    @Override
    public double getDoubleValue() {
        return 0;
    }

    @Override
    public void setValue(int v) {
        value = v;
    }

    @Override
    public void setValue(double v) {

    }
}
