import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DairyController {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        SeleniumCrawler crawler = new SeleniumCrawler();

        List<ReportScrapeEntity> reportScrapeEntityList = new ArrayList<>();
        try {
            reportScrapeEntityList = crawler.crawlForScheduleReport();
        } catch (Exception e) {
            //send error notification email
        }

        try {
            MySQLService.loadResultSetToDatabase(reportScrapeEntityList);
        } catch (Exception e) {
            //send error notification email
        }

    }
}