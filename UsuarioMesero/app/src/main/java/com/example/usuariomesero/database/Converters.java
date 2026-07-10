package com.example.usuariomesero.database;

import androidx.room.TypeConverter;
import com.example.usuariomesero.models.ItemOrden;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Convertidores para Room.
 * Permiten guardar tipos de datos complejos (como listas de objetos)
 * transformándolos a String (JSON) y viceversa.
 */
public class Converters {
    @TypeConverter
    public static List<ItemOrden> fromString(String value) {
        Type listType = new TypeToken<List<ItemOrden>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<ItemOrden> list) {
        return new Gson().toJson(list);
    }
}
