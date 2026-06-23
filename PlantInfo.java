/**
 * PlantInfo.java
 * Lightweight value object used by the bar-graph panel to hold
 * aggregated plant data returned from the database.
 */
public class PlantInfo {
    private final Integer quantity;
    private final String  recordIDs;

    public PlantInfo(Integer quantity, String locations, String recordIDs) {
        this.quantity  = quantity;
        this.recordIDs = recordIDs;
    }

    public Integer getQuantity()  { return quantity;  }
    public String  getRecordIDs() { return recordIDs; }
}
