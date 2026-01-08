import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.json.simple.JSONArray;

import org.json.simple.JSONObject;

import org.json.simple.parser.*;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        long startTime = System.nanoTime();

        FileReader file = new FileReader("files/WeatherStations.json");
        Object o = new JSONParser().parse(file);
        JSONObject j = (JSONObject) o;

        // Save fields to utils class
        WeatherUtils.fields = (JSONArray) j.get("fields");

        JSONArray records = (JSONArray) j.get("records");

        System.out.println("There is " + records.size() + " weather records");

        // From index 9 to 19 there is useful weather metrics (airtemp, etc...)
        int startIdx = 9, endIdx = 19;

        // In summary we save records and metrics data by date and location
        WeatherSummary summary = new WeatherSummary();

        for (var object : records) {
            JSONArray record = (JSONArray) object;

            // Extract weather location by dev_id but can be done by location also
            String dev_id = WeatherUtils.getWeatherValue(record, "dev_id");
            WeatherLocation wLocation = summary.getOrCreateLocation(record, dev_id);

            // We create a Map<field, value> because some records may not have data

            // Extract date ("2021-11-13T11:59:05+00:00")
            String recordDate = WeatherUtils.getWeatherValue(record, "time");
            if (recordDate.isEmpty()) continue;
            String[] dateList =  recordDate.split("T")[0].split("-");
            recordDate = dateList[2] + "-" + dateList[1] + "-" + dateList[0];

            WeatherLocation weatherDay = summary.getOrCreateLocationByDate(record, recordDate, dev_id);


            Map<String, String> recordData = new HashMap<>();


            // Extract weather metrics
            for(int i = startIdx; i <= endIdx; ++i) {
                String fieldName = WeatherUtils.getIndexFieldName(i);
                if (fieldName.isBlank()) continue;

                String result = WeatherUtils.getWeatherValue(record, fieldName);
                if (result.isBlank()) continue;

                recordData.put(fieldName, result);

                // Save field into both main location and location per day
                wLocation.saveRecordData(fieldName, result);
                weatherDay.saveRecordData(fieldName, result);

            }

            wLocation.addRecordToRecordList(recordData);
            weatherDay.addRecordToRecordList(recordData);

        }


        long estimatedTime = System.nanoTime() - startTime;

        // Print statistics by date and by location
        summary.getStatisticsByDate();

        // Print statistics by locations
        summary.getStatisticsByLocation();

        // Print overall statistics
        summary.getOverallStatistics();

        System.out.println("\n\n\nNumber of locations: " + summary.getNumberOfLocations() );
        summary.printLocationsData();

        // Print file and json reading time (Used nanoTime for accuracy) 
        System.out.println("Elapsed Time: " + estimatedTime / 1_000_000_000.0 + " s");


    }

}

