import org.json.simple.JSONArray;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class WeatherSummary {
    // Saves List of locations, where each location has its own records
    List<WeatherLocation> weatherLocations = new ArrayList<>();

    // Save weather locations per day for statistics by date
    // The Map is like Map<date, List<WeatherLocation>>
    Map<String, List<WeatherLocation>> weatherPerDay = new HashMap<>();


    /*
        Get or create a WeatherLocation by its dev_id
        If the location does not exist we create it from the record data and add it to the locations list

        @param record: JSONArray record from the JSON file
        @param devId: Device ID of the weather location
    */
    public WeatherLocation getOrCreateLocation(JSONArray record, String devId) {
        // search for location via dev_id
        Optional<WeatherLocation> wLocation = this.weatherLocations.stream()
                .filter((wl) -> wl.getDev_id().equals(devId))
                .findAny();

        if (wLocation.isPresent())
            return wLocation.get();

        // Get data to create new location add it to list and return it
        WeatherLocation weatherLocation = this.createLocation(record);
        weatherLocations.add(weatherLocation);
        return weatherLocation;
    }

    /*
        Get or create a WeatherLocation for the weather statistics per day by its dev_id and date
        First we check if the date exists, create it if not
        Then we check if the location exists for that date
        If the location does not exist we create it from the record data and add it to the locations list for that date

        @param record: JSONArray record from the JSON file
        @param recordDate: Date of the weather record
        @param devId: Device ID of the weather location
    */
    public WeatherLocation getOrCreateLocationByDate(JSONArray record, String recordDate, String devId) {
        // search for register via recordDate
        List<WeatherLocation> listOfLocations;
        WeatherLocation weatherLocation;
        if(weatherPerDay.containsKey(recordDate)) {
             listOfLocations = weatherPerDay.get(recordDate);

             // Search that the location(dev_id) exists
            Optional<WeatherLocation> wLocation = listOfLocations.stream()
                    .filter((wl) -> wl.getDev_id().equals(devId))
                    .findAny();

            if (wLocation.isPresent())
                return wLocation.get();

        } else
            listOfLocations = new ArrayList<>();

        // Create Location
        weatherLocation = this.createLocation(record);
        listOfLocations.add(weatherLocation);
        weatherPerDay.put(recordDate, listOfLocations);

        return weatherLocation;
    }

    // Simple location creation used in getOrCreateLocation and getOrCreateLocationByDate
    public WeatherLocation createLocation(JSONArray record) {
        String devId = WeatherUtils.getWeatherValue(record, "dev_id");
        String name = WeatherUtils.getWeatherValue(record, "name");
        String location = WeatherUtils.getWeatherValue(record, "location");

        return new WeatherLocation(devId, name, location);
    }


    public int getNumberOfLocations() {
        return weatherLocations.size();
    }

    public void printLocationsData() {
        for(var location : weatherLocations) {
            System.out.println(location.toString());
        }
    }

    /*
        Calculate average and print statistics for a given location
        We use this function in getStatisticsByDate and getStatisticsByLocation

        @param location: WeatherLocation to calculate statistics
    */
    private void getLocationWeatherData(WeatherLocation location) {
        List<String> fields = location.averages.keySet().stream().sorted().toList();
        System.out.printf("Data from %s %n%n", location.getName() + " at location (" + location.getLocation() + ")");

        for(var fieldName : fields) {
            String avg = "--", min = "--", max = "--";
            if (location.averages.containsKey(fieldName))
                avg = String.format("%02.2f",(location.averages.get(fieldName) / location.weatherRecords.size()));

            if (location.min.containsKey(fieldName))
                min = String.valueOf(location.min.get(fieldName));
            if (location.max.containsKey(fieldName))
                max = String.valueOf(location.max.get(fieldName));

            System.out.printf("* %s: AVG=%s  MIN=%s  MAX=%s %n", WeatherUtils.mapFieldName(fieldName), avg, min, max);
        }
    }

    
    
    /*
        Calculate and print overall statistics from all locations
        Basically sum averages and search for the min and max location of each field
    */
    public void getOverallStatistics() {
        if(weatherLocations.isEmpty()) {
            System.out.println("There is no locations data");
        }

        System.out.println("Overall Statistics\n\n");

        WeatherLocation weatherLocation = weatherLocations.get(0);
        List<String> fields = weatherLocation.averages.keySet().stream().sorted().toList();
        for(var fieldName : fields) {
            double average = 0.0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for(var location: weatherLocations) {
                if (location.averages.containsKey(fieldName))
                    average += Double.parseDouble(String.valueOf(location.averages.get(fieldName) / location.weatherRecords.size() ));

                if (location.min.containsKey(fieldName) && location.min.get(fieldName) < min )
                    min = location.min.get(fieldName);

                if (location.max.containsKey(fieldName) && location.max.get(fieldName) < max )
                    max = location.max.get(fieldName);

            }

            average /= weatherLocations.size();
            System.out.printf("* %s: AVG=%02.2f  MIN=%02.2f  MAX=%02.2f %n", WeatherUtils.mapFieldName(fieldName), average, min, max);
        }
    }

    /*
        Print statistics by location
        We use getLocationWeatherData to print and calculate each location statistics
    */
    public void getStatisticsByLocation() {
        if(weatherLocations.isEmpty()) {
            System.out.println("There is no locations data");
        }

        System.out.println("Statistics by location\n\n");
        // Iterate each location
        for(var location: weatherLocations) {
            getLocationWeatherData(location);
            System.out.println("\n\n");
        }
    }


    /*
        Print statistics by date
        We use getLocationWeatherData to print and calculate each location statistics per day
    */
    public void getStatisticsByDate() {
        Set<String> days = weatherPerDay.keySet();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Create a comparator that parses each string into a LocalDate for comparison
        Comparator<String> dateComparator = Comparator.comparing(
                dateStr -> LocalDate.parse(dateStr, formatter)
        );
        List<String> daysList = days.stream().sorted(dateComparator).toList();


        System.out.println("Statistics per day\n\n");

        for(var day: daysList) {
            LocalDate date = LocalDate.parse(day, formatter);
            // this string format is in spanish, for now...
            System.out.println("\n\n" + date.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'del' yyyy")) + ": \n");

            List<WeatherLocation> weatherLocationsDay = weatherPerDay.get(day);

            // Print statistics for each location in that day
            for(var location : weatherLocationsDay) {
                getLocationWeatherData(location);
                System.out.println("\n\n");
            }

        }

    }

}
