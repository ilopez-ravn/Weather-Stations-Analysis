import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

// Utility class for field name mapping and value extraction from JSON Objects
public final class WeatherUtils {
    public static JSONArray fields;
    public static Map<String, Integer> mapFields = new HashMap<>();

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

    public static void calculateTimeSince(long startTime, String message) {
        double estimatedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) / 1000.0;

        // Print file and JSON reading time (Used nanoTime for accuracy)
        System.out.printf("%s: %02.3f seconds%n", message, estimatedTime);
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

    public static String getWeatherValueJackson(List<String> record, String fieldName) {
        String result = record.get(mapFields.get(fieldName));
        if (result != null && !result.isEmpty())
            return result;

        return "";
    }

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

    public static void saveFields(List<JSONFields> fields) {
        for (int i = 0; i < fields.size(); ++i)
            WeatherUtils.mapFields.put(fields.get(i).getId(), i);
    }
}
