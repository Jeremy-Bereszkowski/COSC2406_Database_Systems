import java.util.Comparator;

public final class Record {
    private final String recordId;
    private final String dateString;
    private final String sensorId;
    private final String sensorName;
    private final String hourlyCounts;

    public Record(String recordId, String dateString, String sensorId, String sensorName, String hourlyCounts) {
        this.recordId = recordId;
        this.dateString = dateString;
        this.sensorId = sensorId;
        this.sensorName = sensorName;
        this.hourlyCounts = hourlyCounts;
    }

    public String getRecordId() { return recordId; }

    public String getDateString() { return dateString; }

    public String getSensorId() { return sensorId; }

    public String getSensorName() { return sensorName; }

    public String getHourlyCounts() { return hourlyCounts; }

    @Override
    public String toString() {
        return recordId +
                " " + dateString +
                " " + sensorId +
                " " + sensorName +
                " " + hourlyCounts;
    }
}
