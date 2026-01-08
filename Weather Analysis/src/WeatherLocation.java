import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherLocation  {
    String name;
    String dev_id;
    String location;

    List<Map<String, String>> weatherRecords = new ArrayList<>();

    // For each value have a map for its average, min and max
    Map<String, Double> averages = new HashMap<>();
    Map<String, Double> min = new HashMap<>();
    Map<String, Double> max = new HashMap<>();

    WeatherLocation(String dev_id, String name, String location) {
        this.dev_id = dev_id;
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getDev_id() {
        return dev_id;
    }

    public String getLocation() {
        return location;
    }

    public void saveRecordData(String fieldName, String result) {
        try {
            Double Result = Double.parseDouble(result);

            if(averages.containsKey(fieldName))
                averages.put(fieldName, averages.get(fieldName) + Result);
            else
                averages.put(fieldName, Result);

            // Save min
            if(min.containsKey(fieldName)) {
                if (min.get(fieldName) > Result)
                    min.put(fieldName, Result);
            } else
                min.put(fieldName, Result);

            // Save max
            if(max.containsKey(fieldName)) {
                if (max.get(fieldName) < Result)
                    max.put(fieldName, Result);
            } else
                max.put(fieldName, Result);
        } catch (NumberFormatException e) {
            System.out.println("Error with field=" + fieldName + "  result=" + result);
            throw new RuntimeException(e);
        }

    }

    public void addRecordToRecordList(Map<String, String> recordData) {
        weatherRecords.add(recordData);
    }

    @Override
    public String toString() {
        return "Name='" + name + "'\n" +
                "Location='" + location + "'\n" +
                "Dev_id='" + dev_id + "'\n";
    }

}
