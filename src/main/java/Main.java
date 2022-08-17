import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static Set<Station> stations = new TreeSet<>(Comparator.comparing(Station::getName));
    private static Map<String, ArrayList<String>> linesAndStations = new TreeMap<>(Comparator.naturalOrder());
    private static List<String> stationsWithConnections = new ArrayList<>();

    private static List<Line> lines = new ArrayList<>();

    private static Document docHTML;

    static {
        try {
            docHTML = Jsoup.connect("https://skillbox-java.github.io/").get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document doc = Jsoup.parse(String.valueOf(docHTML));
    private static Elements elementsDiv = doc.getElementsByClass("js-metro-stations t-metrostation-list-table");


    public static void main(String[] args) {
        try {
            readFolders("/user/data/");
            addLineAndConnection();
            readFileJSONToConsole(readFile("/data/map.json"));
            writeFile1JSON("/user/file1.json");
            writeFile2JSON("/user/file2.json");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeFile1JSON(String out) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonLineAndStations = new JSONObject();
        //add stations
        for (String key : linesAndStations.keySet()) {
            JSONArray nameStations = new JSONArray();
            List<String> listStations = linesAndStations.get(key);
            listStations.forEach(name -> nameStations.add(name));
            jsonLineAndStations.put(key, listStations);
        }
        //add connections
        JSONArray arrayConnections = new JSONArray();
        for (String key : linesAndStations.keySet()) {
            JSONObject currentObject = new JSONObject();
            List<String> listStations = linesAndStations.get(key);
            for (String nameStation : listStations) {
                if (stationsWithConnections.contains(nameStation)) {
                    currentObject = new JSONObject();
                    currentObject.put("line", key);
                    currentObject.put("station", nameStation);
                    arrayConnections.add(currentObject);
                }
            }
        }
        //add lines
        JSONArray arrayLines = new JSONArray();
        addLine();
        lines.forEach(line -> {
            JSONObject objectLine = new JSONObject();
            objectLine.put("number", line.getNumber());
            objectLine.put("name", line.getName());
            arrayLines.add(objectLine);
        });

        jsonObject.put("stations", jsonLineAndStations);
        jsonObject.put("connections", arrayConnections);
        jsonObject.put("lines", arrayLines);

        try (FileWriter file = new FileWriter(out)) {
            file.write(jsonObject.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addLine() {
        Elements divLine1 = doc.getElementsByClass("js-metro-line t-metrostation-list-header t-icon-metroln ln-1");
        divLine1.forEach(element -> lines.add(new Line(element.select("span").text(), element.attr("data-line"))));

        Elements divLine = doc.getElementsByClass("js-toggle-depend s-depend-control-single  ").select("span");
        divLine.forEach(element -> lines.add(new Line(element.select("span").text(), element.attr("data-line"))));
    }

    public static void writeFile2JSON(String out) throws Exception {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        stations.forEach(station -> {
            JSONObject currentStation = new JSONObject();
            currentStation.put("name", station.getName());
            currentStation.put("line", station.getLine());
            currentStation.put("date", station.getDate());
            currentStation.put("depth", station.getDepth());
            if (station.isHasConnection()) {
                currentStation.put("hasConnection", station.isHasConnection());
            }
            array.add(currentStation);
        });
        object.put("stations", array);

        try (FileWriter file = new FileWriter(out)) {
            file.write(object.toJSONString());
        }

    }

    public static void readHTML() throws Exception {
//      add Map<String, ArrayList<String>> linesAndStations
        for (Element element : elementsDiv) {
            linesAndStations.put(element.attr("data-line"), new ArrayList<>());
            ArrayList<String> currentList = linesAndStations.get(element.attr("data-line"));
            Elements names = element.select(".name");
            names.forEach(name -> currentList.add(name.text()));
        }

//      add List<String> stationsWithConnections
        Elements connections = doc.select("p:has(.t-icon-metroln)").select(".name");
        connections.forEach(element -> stationsWithConnections.add(element.text()));
    }

    public static void addLineAndConnection() {
//      set Line in Station
        for (Station currentStation : stations) {
            for (Map.Entry<String, ArrayList<String>> element : linesAndStations.entrySet()) {
                for (String s : element.getValue()) {
                    if (s.equals(currentStation.getName())) {
                        currentStation.setLine(element.getKey());
                    }
                }
            }
        }
//      set isConnection
        for (Station currentStation : stations) {
            stationsWithConnections.forEach(s -> {
                if (s.equals(currentStation.getName())) {
                    currentStation.setHasConnection(true);
                }
            });
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

    public static void readFileJSONToConsole(String file) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(file);
        JSONObject jsonStation = (JSONObject) jsonObject.get("stations");
        jsonStation.keySet().forEach(lineNumberObject -> {
            int lineNumber = Integer.parseInt((String) lineNumberObject);
            JSONArray arrayStations = (JSONArray) jsonStation.get((String) lineNumberObject);
            System.out.println("Line: " + lineNumber + " stations: " + arrayStations.toArray().length);
        });


    }

    public static void parseJSON_1(String file) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(file);
        for (Object line : jsonArray) {
            JSONObject object = (JSONObject) line;
            Station station = new Station(object.get("name").toString());
            if (!stations.isEmpty()) {
                for (Station currentStation : stations) {
                    if (currentStation.equalsName(station)) {
                        currentStation.setDepth(object.get("depth").toString());
                    }
                }
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
            if (!stations.isEmpty()) {
                for (Station currentStation : stations) {
                    if (currentStation.equalsName(station)) {
                        currentStation.setDate(object.get("date").toString());
                    }
                }
            }
            station.setDate(object.get("date").toString());
            stations.add(station);
        }
    }

    public static void parseJSON_3(String file) throws Exception {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(file);
        for (Object line : jsonArray) {
            JSONObject object = (JSONObject) line;
            Station station = new Station(object.get("station_name").toString());
            if (!stations.isEmpty()) {
                for (Station currentStation : stations) {
                    if (currentStation.equalsName(station)) {
                        currentStation.setDepth(object.get("depth_meters").toString());
                    }
                }
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
                if (!stations.isEmpty()) {
                    for (Station currentStation : stations) {
                        if (currentStation.equalsName(station)) {
                            currentStation.setDepth(data[1].toString());
                        }
                    }
//                    stations.forEach(currentStation -> {
//                        if (station.equalsName(currentStation)) {
//                            currentStation.setDepth(data[1].toString());
//                        }
//                    });
                }
                station.setDepth(data[1].toString());
                stations.add(station);
            }
        }

        if (lines[0].equals("Название станции,Дата открытия")) {
            for (int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(",", 2);
                Station station = new Station(data[0]);
                if (!stations.isEmpty()) {
                    for (Station currentStation : stations) {
                        if (currentStation.equalsName(station)) {
                            currentStation.setDate(data[1].toString());
                        }
                    }
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

