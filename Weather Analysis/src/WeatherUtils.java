import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

public final class WeatherUtils {
    public static JSONArray fields;
    public static final Map<String, String> FIELD_NAME = Map.ofEntries(
            Map.entry("atmosphericpressure", "Atmospheric pressure"),
            Map.entry("gustspeed", "Gust speed"),
            Map.entry("precipitation", "Precipitation"),
            Map.entry("relativehumidity", "Relative humidity"),
            Map.entry("vapourpressure", "Vapour pressure"),
            Map.entry("strikes", "Strikes"),
            Map.entry("airtemp", "Air temperature"),
            Map.entry("solar", "Solar"),
            Map.entry("windspeed", "Windspeed"),
            Map.entry("strikedistance", "Strike distance"),
            Map.entry("winddirection", "Wind direction")
    );

    public static String getWeatherValue(JSONArray record, String fieldName) {
        int recIdx = getIndex(fieldName);
        if (recIdx == -1)
            return "";

        String result = (String) record.get(recIdx);
        if (result != null && !result.isEmpty())
            return result;

        return "";
    }

    public static int getIndex(String fieldName) {
        for(int i = 0; i < WeatherUtils.fields.size(); ++i){
            JSONObject field = (JSONObject) WeatherUtils.fields.get(i);
            if (field.get("id").equals(fieldName))
                return i;
        }

        return -1;
    }

    public static String getIndexFieldName(int i) {
        JSONObject field = (JSONObject) WeatherUtils.fields.get(i);
        return field.get("id").toString();

    }
}
