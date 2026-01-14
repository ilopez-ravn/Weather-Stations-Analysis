import java.util.List;

public class WeatherJSONStructure {
    public List<JSONFields> fields;
    public List<List<String>> records;

    public List<JSONFields> getFields() {
        return fields;
    }

    public List<List<String>> getRecords() {
        return records;
    }
}
