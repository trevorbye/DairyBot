import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class OrderScheduleAndMilkSupplyWrapper {
    private List<OrderScheduleScrapeDataEntity> scheduleScrapeDataEntityList;
    private HashMap<Date, Integer> milkSupplyTruckCountMap;

    public OrderScheduleAndMilkSupplyWrapper() {
    }

    public List<OrderScheduleScrapeDataEntity> getScheduleScrapeDataEntityList() {
        return scheduleScrapeDataEntityList;
    }

    public void setScheduleScrapeDataEntityList(List<OrderScheduleScrapeDataEntity> scheduleScrapeDataEntityList) {
        this.scheduleScrapeDataEntityList = scheduleScrapeDataEntityList;
    }

    public HashMap<Date, Integer> getMilkSupplyTruckCountMap() {
        return milkSupplyTruckCountMap;
    }

    public void setMilkSupplyTruckCountMap(HashMap<Date, Integer> milkSupplyTruckCountMap) {
        this.milkSupplyTruckCountMap = milkSupplyTruckCountMap;
    }

    public Date getStartDate() {
        List<OrderScheduleScrapeDataEntity> entities = this.scheduleScrapeDataEntityList;

        Date runningEarliestDate = null;
        for (OrderScheduleScrapeDataEntity entity : entities) {

            if (runningEarliestDate == null) {
                runningEarliestDate = entity.getOrderDate();
            } else {
                if (entity.getOrderDate().before(runningEarliestDate)) {
                    runningEarliestDate = entity.getOrderDate();
                }
            }
        }
        return runningEarliestDate;
    }
}
