import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Utility class for field name mapping and value extraction from JSON Objects
public final class WeatherUtils {
    public static JSONArray fields;

    public static final Map<String, String> FIELD_NAME_FORMAT = Map.ofEntries(
            Map.entry("atmosphericpressure", "Atmospheric pressure"),
            Map.entry("gustspeed", "Gust speed"),
            Map.entry("precipitation", "Precipitation"),
            Map.entry("relativehumidity", "Relative humidity"),
            Map.entry("vapourpressure", "Vapour pressure"),
            Map.entry("strikes", "Strikes"),
            Map.entry("airtemp", "Air temperature"),
            Map.entry("solar", "Solar"),
            Map.entry("windspeed", "Wind speed"),
            Map.entry("strikedistance", "Strike distance"),
            Map.entry("winddirection", "Wind direction")
    );

    public static final List<DateTimeFormatter> dateFormatters = new ArrayList<>(Arrays.asList(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy")
    ));

    public static LocalDate getDateFromString(String stringDate) {
        for (var formatter : WeatherUtils.dateFormatters) {
            try {
                return LocalDate.parse(stringDate, formatter);
            } catch (Exception e) {
                //
            }
        }
        return null;
    }

    public static String mapFieldName(String fieldName) {
        return WeatherUtils.FIELD_NAME_FORMAT.get(fieldName);
    }

    /* 
        Here we get the value of a field by its name from a record
        We also check if the field exists and return empty string if not found
        
        @param record: JSONArray record from the JSON file
        @param fieldName: Name of the field to extract
    */
    public static String getWeatherValue(JSONArray record, String fieldName) {
        int recIdx = getIndex(fieldName);
        if (recIdx == -1)
            return "";

        String result = (String) record.get(recIdx);
        if (result != null && !result.isEmpty())
            return result;

        return "";
    }

    // Here we get the index of a field by its name (For example, get the index of "airtemp")
    public static int getIndex(String fieldName) {
        for (int i = 0; i < WeatherUtils.fields.size(); ++i) {
            JSONObject field = (JSONObject) WeatherUtils.fields.get(i);
            if (field.get("id").equals(fieldName))
                return i;
        }

        return -1;
    }

    // Get field name by index (For example, get the field name at index of 4: "airtemp")
    public static String getIndexFieldName(int i) {
        JSONObject field = (JSONObject) WeatherUtils.fields.get(i);
        return field.get("id").toString();

    }
}
