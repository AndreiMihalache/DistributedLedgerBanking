package ro.ase.ism.clientapp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class Utils {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String prettyJson(final byte[] json) {
        return prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    private String prettyJson(final String json) {
        var parsedJson = JsonParser.parseString(json);
        return gson.toJson(parsedJson);
    }
}
