import java.io.FileNotFoundException;

public class DairyController {

    public static void main(String[] args) throws FileNotFoundException {

        SeleniumCrawler crawler = new SeleniumCrawler();
        ApachePOIExcelWrite excelWrite = new ApachePOIExcelWrite();

        excelWrite.writeExcelFile(crawler.crawl());
    }
}
