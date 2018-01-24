import java.util.Date;
import java.util.List;

public class TrimmedPlantRouteListWrapper {
    private List<ScrapeDataEntity> entities;
    private Date startDate;

    public List<ScrapeDataEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<ScrapeDataEntity> entities) {
        this.entities = entities;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
