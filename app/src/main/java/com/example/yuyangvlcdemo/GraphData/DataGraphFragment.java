package com.example.yuyangvlcdemo.GraphData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.yuyangvlcdemo.GraphData.Data.Item;
import com.example.yuyangvlcdemo.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class DataGraphFragment extends Fragment {

    private DataType type;
    private GraphView graph;
    private LineGraphSeries<DataPoint> mSeries;
    private DataController dataController = DataController.getInstance();
    private ArrayList<Item> list;

    public DataGraphFragment(DataType _type)
    {
        type = _type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        graph = (GraphView) rootView.findViewById(R.id.graph);

        list = loadData();
        mSeries = new LineGraphSeries<DataPoint>(generateData());
        graph.addSeries(mSeries);

        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(20);
        graph.onDataChanged(false, false);

        // enable scrolling
        graph.getViewport().setScrollable(true);

        return rootView;
    }

    private ArrayList<Item> loadData()
    {
        return dataController.getList(type);
    }

    private double getValue(int idx)
    {
        if(type == DataType.CO2)
            return (double)(list.get(idx).getIntValue());
        else
            return list.get(idx).getDoubleValue();
    }

    private DataPoint[] generateData()
    {
        int count = list.size();
        DataPoint[] values = new DataPoint[count];
        for(int i=0; i<count; i++)
        {
            double x = i;
            double y = getValue(i);
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }
}
