package com.example.yuyangvlcdemo.GraphData;


import com.example.yuyangvlcdemo.GraphData.Data.Item;

import java.util.ArrayList;

public class DataController {
    private ArrayList<Item> listCO2;
    private ArrayList<Item> listTemp;
    private ArrayList<Item> listHumi;

    private DataController() {
        listCO2 = new ArrayList<Item>();
        listTemp = new ArrayList<Item>();
        listHumi = new ArrayList<Item>();
    }

    public void clearItemCO2()
    {
        listCO2.clear();
    }

    public void clearItemTemp()
    {
        listTemp.clear();
    }

    public void clearItemHumi()
    {
        listHumi.clear();
    }

    public void addCO2(Item data)
    {
        if(listCO2.size() > 20) {
            listCO2.remove(0);
        }

        listCO2.add(data);
    }

    public void addTemp(Item data)
    {
        if(listTemp.size() > 20) {
            listTemp.remove(0);
        }
        listTemp.add(data);
    }

    public void addHumi(Item data)
    {
        if(listHumi.size() > 20) {
            listHumi.remove(0);
        }
        listHumi.add(data);
    }

    public ArrayList<Item> getList(DataType type)
    {
        switch (type) {
            case CO2:
                return listCO2;

            case TEMP:
                return listTemp;

            default:
                return listHumi;
        }
    }

    private static DataController controller;
    public static DataController getInstance()
    {
        if(controller == null)
            controller = new DataController();

        return controller;
    }
}