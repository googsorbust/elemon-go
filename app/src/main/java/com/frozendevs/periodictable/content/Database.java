package com.frozendevs.periodictable.content;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.tensorflow.lite.examples.detection.R;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static InputStreamReader getInputStreamReader(Context context) {
        return new InputStreamReader(context.getResources().openRawResource(R.raw.database));
    }

    public static <T> List<T> getAllElements(Context context, Class<T> classOfT) {
        final List<T> elements = new ArrayList<>();

        final JsonReader reader = new JsonReader(getInputStreamReader(context));

        try {
            reader.beginArray();

            final Gson gson = new Gson();

            while (reader.hasNext()) {
                elements.add(classOfT.cast(gson.fromJson(reader, classOfT)));
            }

            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return elements;
    }

    public static <T> T getElement(Context context, Class<T> classOfT, int number) {
        T element = null;

        final JsonReader reader = new JsonReader(getInputStreamReader(context));

        try {
            reader.beginArray();

            final Gson gson = new Gson();

            for (int i = 1; reader.hasNext(); i++) {
                if (i == number) {
                    element = classOfT.cast(gson.fromJson(reader, classOfT));

                    break;
                } else {
                    reader.skipValue();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return element;
    }
}
