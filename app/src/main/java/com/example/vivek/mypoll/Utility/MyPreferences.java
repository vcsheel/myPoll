package com.example.vivek.mypoll.Utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPreferences {

    public static SharedPreferences sp;

    public static SharedPreferences createSharedPref(Context cont){
        if(sp==null)
            sp=cont.getSharedPreferences("MassageOnDemand",Context.MODE_PRIVATE);

        return sp;
    }

    public static String getAddress(Context cont){
        createSharedPref(cont);
        return sp.getString("address", null);
    }

    public static void setAddress(Context cont, String myAdd){
        createSharedPref(cont);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("address", myAdd);
        spe.apply();
    }

    public static String getPollQues(Context cont){
        createSharedPref(cont);
        return sp.getString("MyCODE", null);
    }
    public static void setPollQues(Context cont, String id){
        createSharedPref(cont);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("MyCODE", id);
        spe.apply();
    }

    public static boolean getHasPolled(Context cont){
        createSharedPref(cont);
        return sp.getBoolean("isPolled", false);
    }
    public static void setHasPolled(Context cont, Boolean id){
        createSharedPref(cont);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean("isPolled", id);
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


    public static Map<String,List<String>> getAllPolls(Context cont){
        createSharedPref(cont);
        Gson gson = new Gson();
        String json = sp.getString("allpolls",null);
        Type type = new TypeToken<HashMap<String,List<String>>>() {}.getType();
        Map<String,List<String>> polllist = gson.fromJson(json,type);
        if(polllist==null){
            polllist = new HashMap<>();
        }
        return polllist;
    }
    public static void setAllPolls(Context cont, Map<String,List<String>> polllist){
        createSharedPref(cont);
        SharedPreferences.Editor spe = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(polllist);
        spe.putString("allpolls", json);
        spe.apply();
    }

    public static void clearSP() {
        sp.edit().clear().apply();
    }
}
