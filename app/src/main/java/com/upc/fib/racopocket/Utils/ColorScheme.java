package com.upc.fib.racopocket.Utils;

import android.content.Context;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ColorScheme
{
    Context context;
    int[] colors = {
            Color.parseColor("#DFE9C6"),
            Color.parseColor("#FFF3BA"),
            Color.parseColor("#FFD2A7"),
            Color.parseColor("#BDDCE9"),
            Color.parseColor("#DDBFE4"),
            Color.parseColor("#F4828C"),
            Color.parseColor("#BD8B5A"),
            Color.parseColor("#EEABCA"),
            Color.parseColor("#C2BB63"),
            Color.parseColor("#297DB5"),
    };

    public ColorScheme(Context context)
    {
        this.context = context;
    }

    public HashMap<String, Integer> setColorsToSubjects()
    {
        HashMap<String, Integer> colorScheme = new HashMap<>();
        String mySubjects = FileUtils.readFileToString(this.context, "assignatures.json");
        try {
            JSONArray mySubjectsJSONArray = new JSONArray(mySubjects);
            for (int i = 0; i < mySubjectsJSONArray.length(); i++) {
                JSONObject mySubjectJSONObject = mySubjectsJSONArray.getJSONObject(i);
                colorScheme.put(mySubjectJSONObject.getString("idAssig"), colors[i % colors.length]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return colorScheme;
    }
}
