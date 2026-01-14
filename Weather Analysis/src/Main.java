import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.json.simple.JSONArray;

import org.json.simple.JSONObject;

import org.json.simple.parser.*;
import tools.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        long startTime = System.nanoTime();

        FileReader file = new FileReader("files/WeatherStations.json");
        Object o = new JSONParser().parse(file);
        JSONObject j = (JSONObject) o;

        // Save fields to utils class
        WeatherUtils.fields = (JSONArray) j.get("fields");

        JSONArray records = (JSONArray) j.get("records");

        // From index 9 to 19 there is useful weather metrics (airtemp, etc...)
        int startIdx = 9, endIdx = 19;

        // In summary we save records and metrics data by date and location
        WeatherSummary summary = new WeatherSummary();


        for (var object : records) {
            JSONArray record = (JSONArray) object;

            // Validate record data
            if (
                    WeatherUtils.getWeatherValue(record, "time").isBlank()
                            || WeatherUtils.getWeatherValue(record, "dev_id").isBlank()
                            || WeatherUtils.getWeatherValue(record, "keep_record").isBlank()
                            || !WeatherUtils.getWeatherValue(record, "keep_record").equalsIgnoreCase("y")
            ) continue;

            // Extract weather location by dev_id but can be done by location also
            String dev_id = WeatherUtils.getWeatherValue(record, "dev_id");
            WeatherLocation wLocation = summary.getOrCreateLocation(record, dev_id);

            // We create a Map<field, value> because some records may not have data

            // Extract date ("2021-11-13T11:59:05+00:00") and transform to dd-MM-yyyy
            String recordDate = WeatherUtils.getWeatherValue(record, "time");
            if (recordDate.isEmpty()) continue;
            String[] dateList = recordDate.split("T")[0].split("-");
            recordDate = dateList[2] + "-" + dateList[1] + "-" + dateList[0];

            WeatherLocation weatherDay = summary.getOrCreateLocationByDate(record, recordDate, dev_id);


            Map<String, String> recordData = new HashMap<>();

            // Extract weather metrics
            for (int i = startIdx; i <= endIdx; ++i) {
                // String fieldName = WeatherUtils.getIndexFieldName(i);
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

        // Transform from Nano to milli, because transforming it to seconds, removes the decimal part, which is important
        WeatherUtils.calculateTimeSince(startTime, "(JSON-SIMPLE) Time for loading data");


        // Loading JSON with jackson
        startTime = System.nanoTime();
        ObjectMapper mapper = new ObjectMapper();
        WeatherJSONStructure weatherStruct = mapper.readValue(new FileReader("files/WeatherStations.json"), WeatherJSONStructure.class);

        WeatherUtils.saveFields(weatherStruct.getFields());

        List<String> fieldIds = new ArrayList<>(endIdx - startIdx + 1);
        for (int i = startIdx; i <= endIdx; ++i) {
            String fieldId = weatherStruct.getFields().get(i).getId();
            if (!fieldId.isBlank()) {
                fieldIds.add(fieldId);
            }
        }

        summary = new WeatherSummary();

        for (var record : weatherStruct.getRecords()) {

            String recordDate = WeatherUtils.getWeatherValueJackson(record, "time");
            String dev_id = WeatherUtils.getWeatherValueJackson(record, "dev_id");
            // Validate record data
            if (
                    recordDate.isBlank()
                            || dev_id.isBlank()
            ) continue;

            // Extract weather location by dev_id but can be done by location also
            WeatherLocation wLocation = summary.getOrCreateLocationJackson(record, dev_id);

            // Extract date ("2021-11-13T11:59:05+00:00") and transform to dd-MM-yyyy
            String[] dateList = recordDate.split("T")[0].split("-");
            recordDate = dateList[2] + "-" + dateList[1] + "-" + dateList[0];

            WeatherLocation weatherDay = summary.getOrCreateLocationByDateJackson(record, recordDate, dev_id);


            Map<String, String> recordData = new HashMap<>();

            // Extract weather metrics
            for (String fieldName : fieldIds) {
                String result = WeatherUtils.getWeatherValueJackson(record, fieldName);
                if (result.isBlank()) continue;

                recordData.put(fieldName, result);

                // Save field into both main location and location per day
                wLocation.saveRecordData(fieldName, result);
                weatherDay.saveRecordData(fieldName, result);
            }

            wLocation.addRecordToRecordList(recordData);
            weatherDay.addRecordToRecordList(recordData);
        }
        WeatherUtils.calculateTimeSince(startTime, "(JACKSON) Time for loading data");

        System.out.println("\n\n\nNumber of valid weather records:" + summary.getNumberOfValidRecords());
        System.out.printf("We found %d location(s): ", summary.getNumberOfLocations());
        summary.printLocationsData();

        Scanner s = new Scanner(System.in);

        while (true) {
            drawMenu();
            String op = s.nextLine();

            if (op.equalsIgnoreCase("q")) {
                System.out.println("Goodbye!");
                s.close();
                return;
            }

            switch (op) {
                case "1" -> summary.getOverallStatistics();
                case "2" -> summary.getStatisticsByLocation();
                case "3" -> summary.getStatisticsByDate();
                case "4" -> summary.getStatisticsByPeriod(s);
                default -> System.out.println("Invalid option, please try again.\n\n");
            }

        }

    }

    public static void drawMenu() {
        System.out.print("""
                \u001B[32m
                
                Weather Statistics Menu
                
                1. Get Overall Statistics
                2. Get Location Statistics
                3. Get Per day Statistics
                4. Get Statistics by date period
                Enter a number between 1 to 4 or press 'q' to exit: \u001B[37m""");


    }

}

