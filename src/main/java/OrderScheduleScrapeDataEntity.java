import java.util.Date;

public class OrderScheduleScrapeDataEntity {
    private String plantCode;
    private long truckCount;
    private Date orderDate;

    public OrderScheduleScrapeDataEntity() {
    }

    public String getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(String plantName) {
        String plantCode;

        switch (plantName) {
            case "Alpenrose Dairy Portland OR":
                plantCode = "ALP";
                break;
            case "Andersen Dairy, Inc. Battle Ground WA":
                plantCode = "AND";
                break;
            case "Auburn Dairy Products Auburn WA":
                plantCode = "AUB";
                break;
            case "Columbia River Processing Boardman OR":
                plantCode = "CRP";
                break;
            case "Darigold - Boise Boise ID":
                plantCode = "DGB";
                break;
            case "Darigold - Bozeman Bozeman MT":
                plantCode = "DBZ";
                break;
            case "Darigold - Caldwell Caldwell ID":
                plantCode = "DCN";
                break;
            case "Darigold - Issaquah Issaquah WA":
                plantCode = "DIS";
                break;
            case "Darigold - Jerome Jerome ID":
                plantCode = "DGJ";
                break;
            case "Darigold - Lynden Lynden WA":
                plantCode = "DLY";
                break;
            case "Darigold - Portland Portland OR":
                plantCode = "DPO";
                break;

                //this is not a typo in "Rainier"
            case "Darigold - Rainier Ranier WA":
                plantCode = "DRA";
                break;
            case "Darigold - Spokane Spokane WA":
                plantCode = "DGS";
                break;
            case "Darigold - Sunnyside Sunnyside WA":
                plantCode = "DSU";
                break;
            case "Eberhard Creamery Redmond OR":
                plantCode = "EBH";
                break;
            case "Fred Meyer, Inc. Portland OR":
                plantCode = "MEY";
                break;
            case "Glanbia Foods - Twin Falls Twin Falls ID":
                plantCode = "GGD";
                break;
            case "Meadow Gold - Billings Billings MT":
                plantCode = "MGD";
                break;
            case "Ochoa's Queseria Albany OR":
                plantCode = "OCH";
                break;
            case "Punjab Milk Foods Surrey WA":
                plantCode = "PUN";
                break;
            case "Safeway Stores (Bellevue) Bellevue WA":
                plantCode = "SAF";
                break;
            case "Safeway Stores (Clackamas) Clackamas WA":
                plantCode = "SCL";
                break;
            case "Smith Brothers Kent WA":
                plantCode = "SMI";
                break;
            case "Springfield Creamery Eugene OR":
                plantCode = "SPF";
                break;
            case "Sunshine Dairy Portland OR":
                plantCode = "SUN";
                break;
            case "Tillamook County Creamery Tillamook OR":
                plantCode = "TCCA";
                break;
            case "Umpqua Dairy Central Point OR":
                plantCode = "UMP";
                break;
            default:
                plantCode = "N/A";
                break;
        }

        this.plantCode = plantCode;
    }

    public long getTruckCount() {
        return truckCount;
    }

    public void setTruckCount(long truckCount) {
        this.truckCount = truckCount;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
}
