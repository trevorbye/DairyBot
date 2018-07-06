public class ReportScrapeEntity {
    private String receivingPlant;
    private String receivedDate;
    private String receivedTime;
    private String routeNumber;
    private String haulerDesc;

    public ReportScrapeEntity() {
    }

    public String getReceivingPlant() {
        return receivingPlant;
    }

    public void setReceivingPlant(String receivingPlant) {
        this.receivingPlant = receivingPlant;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getHaulerDesc() {
        return haulerDesc;
    }

    public void setHaulerDesc(String haulerDesc) {
        this.haulerDesc = haulerDesc;
    }
}
