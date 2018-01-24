import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DairyController {

    public static void main(String[] args) throws FileNotFoundException {

        SeleniumCrawler crawler = new SeleniumCrawler();
        ApachePOIExcelWrite excelWrite = new ApachePOIExcelWrite();

        excelWrite.writeExcelFile(crawler.crawl());
    }
}
