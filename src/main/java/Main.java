import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static Set<Station> stations = new TreeSet<>(Comparator.comparing(Station::getName));

    public static void main(String[] args) {
        try {
            readFolders("folder");
            for (Station station : stations) {
                System.out.println(station);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void readFolders(String path) throws Exception {
        File file = new File(path);
        if (file.isFile() && file.getAbsolutePath().endsWith(".csv")) {
            parseCSV(readFile(path));
        }
        if (file.isFile() && file.getAbsolutePath().endsWith(".json")) {
            if (file.getName().equals("depths-1.json")) {
                parseJSON_1(readFile(path));
            }
            if (file.getName().equals("dates-2.json")) {
                parseJSON_2(readFile(path));
            }
            if (file.getName().equals("depths-3.json")) {
                parseJSON_3(readFile(path));
            }
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                readFolders(file1.getAbsolutePath());
            }
        }
    }

    public static void parseJSON_1(String file) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(file);
        for (Object line : jsonArray) {
            JSONObject object = (JSONObject) line;
            Station station = new Station(object.get("name").toString());
            if (stations.contains(station)) {
                stations.forEach(currentStation -> {
                    if (station.equals(currentStation)) {
                        currentStation.setDepth(object.get("depth").toString());
                    }
                });
                continue;
            }
            station.setDepth(object.get("depth").toString());
            stations.add(station);
        }
    }

    public static void parseJSON_2(String file) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(file);
        for (Object line : jsonArray) {
            JSONObject object = (JSONObject) line;
            Station station = new Station(object.get("name").toString());
            if (stations.contains(station)) {
                stations.forEach(currentStation -> {
                    if (station.equals(currentStation)) {
                        currentStation.setDepth(object.get("date").toString());
                    }
                });
                continue;
            }
            station.setDepth(object.get("date").toString());
            stations.add(station);
        }
    }

    public static void parseJSON_3(String file) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(file);
        for (Object line : jsonArray) {
            JSONObject object = (JSONObject) line;
            Station station = new Station(object.get("station_name").toString());
            if (stations.contains(station)) {
                stations.forEach(currentStation -> {
                    if (station.equals(currentStation)) {
                        currentStation.setDepth(object.get("depth_meters").toString());
                    }
                });
                continue;
            }
            station.setDepth(object.get("depth_meters").toString());
            stations.add(station);
        }
    }

    public static void parseCSV(String file) {
        String[] lines = file.split("\n");

        if (lines[0].equals("Название,Глубина")) {
            for (int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(",", 2);
                Station station = new Station(data[0]);
                if (stations.contains(station)) {
                    stations.forEach(currentStation -> {
                        if (station.equals(currentStation)) {
                            currentStation.setDepth(data[1].toString());
                        }
                    });
                    continue;
                }
                station.setDepth(data[1].toString());
                stations.add(station);
            }
        }

        if (lines[0].equals("Название станции,Дата открытия")) {
            for (int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(",", 2);
                Station station = new Station(data[0]);
                if (stations.contains(station)) {
                    stations.forEach(currentStation -> {
                        if (station.equals(currentStation)) {
                            currentStation.setDepth(data[1].toString());
                        }
                    });
                    continue;
                }
                station.setDate(data[1]);
                stations.add(station);
            }
        }
    }

    public static String readFile(String path) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        lines.forEach(line -> stringBuilder.append(line + "\n"));
        return stringBuilder.toString();
    }
}

