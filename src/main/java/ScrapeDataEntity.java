import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScrapeDataEntity {

    private String sourcePlantCode;
    private String destinationPlantCode;
    private Date routeInitialDate;

    public ScrapeDataEntity() {
    }

    public String getSourcePlantCode() {
        return sourcePlantCode;
    }

    public void setSourcePlantCode(String sourcePlantName) {
        String sourceCode;

        switch (sourcePlantName) {
            case "Darigold - Bozeman Bozeman MT":
                sourceCode = "DBZ";
                break;
            case "Darigold - Chehalis Chehalis WA":
                sourceCode = "DCH";
                break;
            case "Darigold - Jerome Jerome ID":
                sourceCode = "DGJ";
                break;
            case "Darigold - Lynden Lynden WA":
                sourceCode = "DLY";
                break;
            case "Darigold - Rainier Ranier WA":
                sourceCode = "DRA";
                break;
            case "Darigold - Spokane Spokane WA":
                sourceCode = "DGS";
                break;
            case "Darigold - Sunnyside Sunnyside WA":
                sourceCode = "DSU";
                break;
            case "Darigold - Boise Boise ID":
                sourceCode = "DGB";
                break;
            case "Darigold - Caldwell Caldwell ID":
                sourceCode = "DCN";
                break;
            default:
                sourceCode = "Not_defined";
                break;
        }
        this.sourcePlantCode = sourceCode;
    }

    public String getDestinationPlantCode() {
        return destinationPlantCode;
    }

    public void setDestinationPlantCode(String routeData) {
        int startIndex = routeData.indexOf("(");
        String firstSplit = routeData.substring(startIndex + 1);
        int indexOfRouteSplitter = firstSplit.indexOf("RD:");

        String destinationName = firstSplit.substring(0, indexOfRouteSplitter - 1);
        String destinationCode;

        switch (destinationName) {
            case "Darigold -Chehalis":
                destinationCode = "DCH";
                break;
            case "Darigold - Jerome":
                destinationCode = "DGJ";
                break;
            case "Darigold - Lynden":
                destinationCode = "DLY";
                break;
            case "Darigold - Rainier":
                destinationCode = "DRA";
                break;
            case "Darigold - Sunnyside":
                destinationCode = "DSU";
                break;
            case "Darigold - Spokane":
                destinationCode = "DGS";
                break;
            case "Darigold - Bozeman":
                destinationCode = "DBZ";
                break;
            case "Darigold - Boise":
                destinationCode = "DGB";
                break;
            case "Darigold - Caldwell":
                destinationCode = "DCN";
                break;
            case "Darigold - Portland":
                destinationCode = "DPO";
                break;
            case "Darigold - Issaquah":
                destinationCode = "DIS";
                break;
            default:
                destinationCode = "3P SALES";
                break;
        }

        this.destinationPlantCode = destinationCode;
    }

    public Date getRouteInitialDate() {
        return routeInitialDate;
    }

    public void setRouteInitialDate(Date routeInitialDate) {
        this.routeInitialDate = routeInitialDate;
    }

    public static TrimmedPlantRouteListWrapper trimListByPlantCode(String sourcePlantCode, List<ScrapeDataEntity> fullList) {
        List<ScrapeDataEntity> trimmedList = new ArrayList<>();
        Date minDate = null;

        for (ScrapeDataEntity entity : fullList) {

            if (minDate == null) {
                minDate = entity.routeInitialDate;
            } else {
                if (entity.routeInitialDate.before(minDate)) {
                    minDate = entity.routeInitialDate;
                }
            }

            if (entity.getSourcePlantCode().equals(sourcePlantCode)) {
                trimmedList.add(entity);
            }
        }

        TrimmedPlantRouteListWrapper wrapper = new TrimmedPlantRouteListWrapper();
        wrapper.setEntities(trimmedList);
        wrapper.setStartDate(minDate);

        return wrapper;
    }
}
