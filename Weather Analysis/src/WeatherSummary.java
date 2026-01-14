import org.json.simple.JSONArray;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WeatherSummary {
    // Saves List of locations, where each location has its own records
    private List<WeatherLocation> weatherLocations = new ArrayList<>();

    // Save weather locations per day for statistics by date
    // The Map is like Map<date, List<WeatherLocation>>
    private Map<String, List<WeatherLocation>> weatherPerDay = new HashMap<>();


    /*
     * Get or create a WeatherLocation by its dev_id
     * If the location does not exist we create it from the record data and add it
     * to the locations list
     *
     * @param record: JSONArray record from the JSON file
     *
     * @param devId: Device ID of the weather location
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

    public WeatherLocation getOrCreateLocationJackson(List<String> record, String devId) {
        // search for location via dev_id
        Optional<WeatherLocation> wLocation = this.weatherLocations.stream()
                .filter((wl) -> wl.getDev_id().equals(devId))
                .findAny();

        if (wLocation.isPresent())
            return wLocation.get();

        // Get data to create new location add it to list and return it
        WeatherLocation weatherLocation = this.createLocationJackson(record);
        weatherLocations.add(weatherLocation);
        return weatherLocation;
    }

    /*
     * Get or create a WeatherLocation for the weather statistics per day by its
     * dev_id and date
     * First we check if the date exists, create it if not
     * Then we check if the location exists for that date
     * If the location does not exist we create it from the record data and add it
     * to the locations list for that date
     *
     * @param record: JSONArray record from the JSON file
     *
     * @param recordDate: Date of the weather record
     *
     * @param devId: Device ID of the weather location
     */
    public WeatherLocation getOrCreateLocationByDate(JSONArray record, String recordDate, String devId) {
        // search for register via recordDate
        List<WeatherLocation> listOfLocations;
        WeatherLocation weatherLocation;
        if (weatherPerDay.containsKey(recordDate)) {
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

    public WeatherLocation getOrCreateLocationByDateJackson(List<String> record, String recordDate, String devId) {
        // search for register via recordDate
        List<WeatherLocation> listOfLocations;
        WeatherLocation weatherLocation;
        if (weatherPerDay.containsKey(recordDate)) {
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
        weatherLocation = this.createLocationJackson(record);
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

    public WeatherLocation createLocationJackson(List<String> record) {
        String devId = WeatherUtils.getWeatherValueJackson(record, "dev_id");
        String name = WeatherUtils.getWeatherValueJackson(record, "name");
        String location = WeatherUtils.getWeatherValueJackson(record, "location");

        return new WeatherLocation(devId, name, location);
    }

    public int getNumberOfLocations() {
        return weatherLocations.size();
    }

    public void printLocationsData() {
        for (var location : weatherLocations) {
            System.out.println(location.toString());
        }
    }

    /*
     * Calculate average and print statistics for a given location
     * We use this function in getStatisticsByDate and getStatisticsByLocation
     *
     * @param location: WeatherLocation to calculate statistics
     */
    private void getLocationWeatherData(WeatherLocation location) {
        Map<String, Double> averageMap = location.getAverages();
        Map<String, Double> minMap = location.getMin();
        Map<String, Double> maxMap = location.getMax();

        List<String> fields = averageMap.keySet().stream().sorted().toList();
        System.out.printf("Data from %s %n%n", location.getName() + " at location (" + location.getLocation() + ")");

        for (var fieldName : fields) {
            String avg = "--", min = "--", max = "--";
            if (averageMap.containsKey(fieldName))
                avg = String.format("%02.2f", (averageMap.get(fieldName) / location.getWeatherRecords().size()));

            if (minMap.containsKey(fieldName))
                min = String.valueOf(minMap.get(fieldName));
            if (maxMap.containsKey(fieldName))
                max = String.valueOf(maxMap.get(fieldName));

            System.out.printf("* %s: AVG=%s  MIN=%s  MAX=%s %n", WeatherUtils.mapFieldName(fieldName), avg, min, max);
        }
    }

    /*
     * Calculate and print overall statistics from all locations
     * Basically sum averages and search for the min and max location of each field
     */
    public void getOverallStatistics() {
        if (weatherLocations.isEmpty()) {
            System.out.println("There is no location's data");
        }
        long startTime = System.nanoTime();

        System.out.println("Overall Statistics\n\n");

        getStatisticsByLocations(weatherLocations);

        WeatherUtils.calculateTimeSince(startTime, "Overall statistics time");
    }

    private void getStatisticsByLocations(List<WeatherLocation> weatherLocations) {
        WeatherLocation weatherLocation = weatherLocations.get(0);
        List<String> fields = weatherLocation.getAverages().keySet().stream().sorted().toList();
        for (var fieldName : fields) {
            double average = 0.0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;


            for (var location : weatherLocations) {
                Map<String, Double> averageMap = location.getAverages();
                Map<String, Double> minMap = location.getMin();
                Map<String, Double> maxMap = location.getMax();
                if (averageMap.containsKey(fieldName))
                    average += Double.parseDouble(
                            String.valueOf(averageMap.get(fieldName) / location.getWeatherRecords().size()));

                if (minMap.containsKey(fieldName) && minMap.get(fieldName) < min)
                    min = minMap.get(fieldName);

                if (maxMap.containsKey(fieldName) && maxMap.get(fieldName) < max)
                    max = maxMap.get(fieldName);

            }

            average /= weatherLocations.size();
            System.out.printf("* %s: AVG=%02.2f  MIN=%02.2f  MAX=%02.2f %n", WeatherUtils.mapFieldName(fieldName),
                    average, min, max);
        }
    }

    /*
     * Print statistics by location
     * We use getLocationWeatherData to print and calculate each location statistics
     */
    public void getStatisticsByLocation() {
        long startTime = System.nanoTime();
        if (weatherLocations.isEmpty()) {
            System.out.println("There is no location's data");
        }

        System.out.println("Statistics by location\n\n");
        // Iterate each location
        for (var location : weatherLocations) {
            getLocationWeatherData(location);
            System.out.println("\n\n");
        }
        WeatherUtils.calculateTimeSince(startTime, "Statistics per location time");
    }

    /*
     * Print statistics by date
     * We use getLocationWeatherData to print and calculate each location statistics
     * per day
     */
    public void getStatisticsByDate() {
        long startTime = System.nanoTime();
        Set<String> days = weatherPerDay.keySet();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Create a comparator that parses each string into a LocalDate for comparison
        Comparator<String> dateComparator = Comparator.comparing(
                dateStr -> LocalDate.parse(dateStr, formatter)
        );
        List<String> daysList = days.stream().sorted(dateComparator).toList();

        System.out.println("Statistics per day\n\n");

        for (var day : daysList) {
            LocalDate date = LocalDate.parse(day, formatter);
            // this string format is in spanish, for now...
            System.out.println("\n\n" + date.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'del' yyyy")) + ": \n");

            List<WeatherLocation> weatherLocationsDay = weatherPerDay.get(day);

            // Print statistics for each location in that day
            for (var location : weatherLocationsDay) {
                getLocationWeatherData(location);
                System.out.println("\n\n");
            }

        }

        WeatherUtils.calculateTimeSince(startTime, "Statistics per day");
    }

    /*
        Get statistics for a given period
        We validate the start and end period via user input and filter each day using streams
        
     */
    public void getStatisticsByPeriod(Scanner s) {
        System.out.println("We need the date to be in the format: dd-MM-yyyy or yyyy-MM-dd");
        System.out.print("Input the start period:");
        LocalDate startPeriod;
        LocalDate endPeriod;
        while (true) {
            String input = s.nextLine().trim();
            if (input.equalsIgnoreCase("q"))
                return;

            LocalDate date = WeatherUtils.getDateFromString(input);
            if (date != null) {
                startPeriod = date;
                break;
            }
        }

        System.out.print("Input the ending period:");
        while (true) {
            String input = s.nextLine().trim();
            if (input.equalsIgnoreCase("q"))
                return;

            LocalDate date = WeatherUtils.getDateFromString(input);
            if (date != null) {
                endPeriod = date;
                break;
            }
        }

        // check that if the start is before the end period
        if (startPeriod.compareTo(endPeriod) > 0) {
            System.out.println("\u001B[31mThe start period can´t be after or equal to the end period \u001B[0m");
            getStatisticsByPeriod(s);
            return;
        }

        // for each day, lets check if its between the periods

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        Comparator<String> dateComparator = Comparator.comparing(
                dateStr -> LocalDate.parse(dateStr, formatter));

        // Order the dates, then filter by period and transform with flatMap to a list of locations
        // where each location is from a date
        List<WeatherLocation> weatherLocations = weatherPerDay.keySet()
                .stream()
                .sorted(dateComparator)
                .map(stringDate -> LocalDate.parse(stringDate, formatter))
                .filter(date -> {
                    return (date.compareTo(startPeriod) >= 0) && (date.compareTo(endPeriod) <= 0);
                })
                .map(localDate -> localDate.format(formatter))
                .flatMap(stringDate -> weatherPerDay.get(stringDate).stream())
                .toList();


        if (weatherLocations.isEmpty()) {
            System.out.println("\n\nThere is no data in the period provided :( \n\n");
            return;
        }
        long startTime = System.nanoTime();

        int numberOfRecords = 0;
        for (var location : weatherLocations)
            numberOfRecords += location.getWeatherRecords().size();

        System.out.printf("%n%nWeather information from %d records: %n%n", numberOfRecords);

        getStatisticsByLocations(weatherLocations);
        WeatherUtils.calculateTimeSince(startTime, "Statistics by date range");
    }

    public Integer getNumberOfValidRecords() {
        return weatherLocations.stream()
                .map(location -> location.getWeatherRecords().size())
                .reduce(0, Integer::sum);
    }
}
