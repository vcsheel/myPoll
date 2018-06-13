package com.example.vivek.mypoll.Utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyPreferences {

    public static SharedPreferences sp;

    public static SharedPreferences createSharedPref(Context cont){
        if(sp==null)
            sp=cont.getSharedPreferences("MassageOnDemand",Context.MODE_PRIVATE);

        return sp;
    }
    public static String getMyCode(Context cont){
        createSharedPref(cont);
        return sp.getString("MyCODE", null);
    }
    public static void setMyCode(Context cont, String id){
        createSharedPref(cont);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("MyCODE", id);
        spe.apply();
    }

    public static List<String> getOptionsList(Context cont){
        createSharedPref(cont);
        Gson gson = new Gson();
        String json = sp.getString("AddressList",null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> addresslist = gson.fromJson(json,type);
        if(addresslist==null){
            addresslist = new ArrayList<>();
        }
        return addresslist;
    }
    public static void setOptionssList(Context cont, List<String> addresslist){
        createSharedPref(cont);
        SharedPreferences.Editor spe = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(addresslist);
        spe.putString("AddressList", json);
        spe.apply();
    }
}
