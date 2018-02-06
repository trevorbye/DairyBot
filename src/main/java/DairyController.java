import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Map;

public class DairyController {

    public static void main(String[] args) throws FileNotFoundException {
        SeleniumCrawler crawler = new SeleniumCrawler();

        //milk demand script
        /*
        ApachePOIExcelWrite excelWrite = new ApachePOIExcelWrite();
        excelWrite.writeExcelFile(crawler.crawl());
        */

        //orders script
        OrderScheduleAndMilkSupplyWrapper wrapper = crawler.crawlForScript2();

        for (OrderScheduleScrapeDataEntity entity : wrapper.getScheduleScrapeDataEntityList()) {
            System.out.println(entity.getPlantCode() + " : " + entity.getOrderDate() + " : " + entity.getTruckCount());
        }

        for (Map.Entry<Date, Integer> entry : wrapper.getMilkSupplyTruckCountMap().entrySet()) {
            System.out.println(entry.getKey().toString() + " : " + entry.getValue());
        }

        ApachePOIExcelWrite excelWrite = new ApachePOIExcelWrite();
        excelWrite.manipulateExcelFileForScript2(wrapper);
    }
}