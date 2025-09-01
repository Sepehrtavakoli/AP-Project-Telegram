package org.example.util;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.model.country;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class CountryLoader {
        public static List<country> loadCountries() {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<country>>() {}.getType();
            Reader reader = new InputStreamReader(
                    Objects.requireNonNull(CountryLoader.class.getResourceAsStream("/countries.json"))
            );
            return gson.fromJson(reader, listType);
        }
}
