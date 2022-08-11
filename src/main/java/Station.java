import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Station {
    private String name;
    private String line;
    private LocalDate date;
    private String depth;
    private  boolean hasConnection;



    public Station(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        this.date = LocalDate.parse(date, formatter);
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public boolean isHasConnection() {
        return hasConnection;
    }

    public void setHasConnection(boolean hasConnection) {
        this.hasConnection = hasConnection;
    }


    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", line=" + line +
                ", date='" + date + '\'' +
                ", depth=" + depth +
                ", hasConnection=" + hasConnection +
                '}';
    }

    public boolean equalsName(Station obj) {
        return this.getName().equals(obj.getName());
    }
}

