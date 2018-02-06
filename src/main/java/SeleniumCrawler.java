import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.time.DateUtils.*;

public class SeleniumCrawler {


    public List<ScrapeDataEntity> crawl() {

        WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME, true) {

            @Override
            protected WebClient newWebClient(BrowserVersion version) {
                WebClient client = super.newWebClient(version);
                client.getOptions().setThrowExceptionOnScriptError(false);
                return client;
            }
        };

        driver.get("removed_for_security");

        login(driver);

        List<ScrapeDataEntity> routeAndPlantList = navigate(driver);

        for (ScrapeDataEntity entity : routeAndPlantList) {
            System.out.println(entity.getSourcePlantCode() + " : " + entity.getDestinationPlantCode() + " : " + entity.getRouteInitialDate());
        }

        return routeAndPlantList;
    }

    public OrderScheduleAndMilkSupplyWrapper crawlForScript2() {
        OrderScheduleAndMilkSupplyWrapper wrapper = new OrderScheduleAndMilkSupplyWrapper();

        WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME, true) {

            @Override
            protected WebClient newWebClient(BrowserVersion version) {
                WebClient client = super.newWebClient(version);
                client.getOptions().setThrowExceptionOnScriptError(false);
                return client;
            }
        };

        driver.get("https://app.dairy.com/fusion/Moo?skin=18&usageType=C&build=Login&forceLogout=true");

        login(driver);
        navigateToOrdersPage(driver);

        //build first week into list
        List<OrderScheduleScrapeDataEntity> scrapeDataEntities = scrapeOrders(driver);

        //append second week to first list
        scrapeDataEntities.addAll(switchDateAndScrapeOrders(driver));
        wrapper.setScheduleScrapeDataEntityList(scrapeDataEntities);

        //change page and scrape truck counts
        navigateToMilkSupplyPageFromWithinMilk(driver);
        HashMap<Date, Integer> truckCountMap = scrapeMilkSupplyTruckCounts(driver);
        wrapper.setMilkSupplyTruckCountMap(truckCountMap);

        return wrapper;
    }

    private void login(WebDriver driver) {
        String username = "removed";
        String password = "removed";

        //wait for form to load
        waitUntilVisible(driver, By.className("form-horizontal"), 10);

        //find username element and send keys
        WebElement usernameInputTag = driver.findElement(By.xpath("//*[@id='login-overlay']/div/div[2]/div[2]/div[1]/div/form/div[1]/input"));
        usernameInputTag.sendKeys(username);

        //find password element and send keys
        WebElement passwordInputTag = driver.findElement(By.xpath("//*[@id='login-overlay']/div/div[2]/div[2]/div[1]/div/form/div[2]/input"));
        passwordInputTag.sendKeys(password);

        //find submit button and click
        WebElement submitButtonElement = driver.findElement(By.xpath("//*[@id='login-overlay']/div/div[2]/div[2]/div[1]/div/form/button"));
        submitButtonElement.click();
    }

    private void navigateToOrdersPage(WebDriver driver) {
        waitUntilVisible(driver, By.id("toptabs"), 10);

        //click on milk
        WebElement linkAnchor = driver.findElement(By.xpath("//*[@id='toptabs']/table/tbody/tr/td[1]/a"));
        linkAnchor.click();

        //click on order schedule
        waitUntilVisible(driver, By.id("dispatchRegionDiv"), 10);
        WebElement orderScheduleAnchor = driver.findElement(By.xpath("//*[@id='dispatchRegionDiv']/table/tbody/tr[4]/td/a"));
        orderScheduleAnchor.click();
    }

    private void navigateToMilkSupplyPageFromWithinMilk(WebDriver driver) {

        //click on milk anchor
        WebElement milkAnchor = driver.findElement(By.xpath("//*[@id='toptabs']/table/tbody/tr/th/a"));
        milkAnchor.click();

        //click on supply schedule
        waitUntilVisible(driver, By.id("dispatchRegionDiv"), 10);
        WebElement supplyScheduleAnchor = driver.findElement(By.xpath("//*[@id='dispatchRegionDiv']/table/tbody/tr[2]/td/a"));
        supplyScheduleAnchor.click();

    }

    private List<OrderScheduleScrapeDataEntity> scrapeOrders(WebDriver driver) {

        //wait for table visibility
        waitUntilVisible(driver, By.id("scheduleTable"), 10);

        //find startDate and parse
        WebElement startDateTableCell = driver.findElement(By.xpath("//*[@id='interval-0']/table/tbody/tr[2]/th[1]"));
        String startDateAsString = startDateTableCell.getText();
        //add current year to date string
        startDateAsString += ("/" + Calendar.getInstance().get(Calendar.YEAR));

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Date startDate = null;

        try {
            startDate = format.parse(startDateAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //get table and extract all <tr> tags
        WebElement mainTable = driver.findElement(By.xpath("//*[@id='scheduleTable']/tbody"));
        List<WebElement> tableRows = mainTable.findElements(By.tagName("tr"));

        //build list to store data, keep track of running plant name
        List<OrderScheduleScrapeDataEntity> orderScheduleScrapeDataEntityList = new ArrayList<>();
        String currentPlant = "none";

        for (WebElement row : tableRows) {
            if (row.isDisplayed()) {
                String currentRowId = row.getAttribute("id");
                Date runningdate = startDate;

                //avoid null pointer if tr does not have an id attribute
                if (currentRowId == null) {
                    currentRowId = "";
                }

                //get current plant; use as running variable
                if (currentRowId.startsWith("topic")) {
                    WebElement plantAnchor = row.findElement(By.xpath(".//td[1]/a"));
                    String currentAchorId = plantAnchor.getAttribute("id");

                    currentPlant = plantAnchor.getText();
                    continue;
                }

                //get header cell text
                WebElement headerTag = row.findElement(By.xpath(".//th[1]"));
                String headerText = headerTag.getText();

                if (headerText.equals("Orders")) {
                    //get all <td> tags
                    List<WebElement> cells = row.findElements(By.tagName("td"));

                    for (WebElement cell : cells) {
                        String cellClass = cell.getAttribute("class");

                        if (cellClass.equals(" mcol") || cellClass.equals(" lcol")) {
                            WebElement span = null;

                            try {
                                span = cell.findElement(By.tagName("span"));
                            } catch (Exception ignored) {
                            }

                            if (span != null) {
                                String spanText = span.getText();
                                long orderQuantityPounds = Long.valueOf(spanText.replaceAll(",", ""));
                                long truckCount = 0;

                                if (orderQuantityPounds != 0) {
                                    if (orderQuantityPounds < 70000) {
                                        truckCount = 1;
                                    } else {
                                        truckCount = orderQuantityPounds / 70000;
                                    }

                                    OrderScheduleScrapeDataEntity entity = new OrderScheduleScrapeDataEntity();
                                    entity.setTruckCount(truckCount);
                                    entity.setPlantCode(currentPlant);
                                    entity.setOrderDate(runningdate);

                                    orderScheduleScrapeDataEntityList.add(entity);
                                }
                            }

                            //increment runningDate
                            runningdate = addDays(runningdate, 1);
                        }
                    }
                }
            }
        }
        return orderScheduleScrapeDataEntityList;
    }

    private List<OrderScheduleScrapeDataEntity> switchDateAndScrapeOrders(WebDriver driver) {
        //create current date, add 7 days convert to string
        Date currentDate = new Date();
        currentDate = addDays(currentDate, 7);
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String currentDateAsString = dateFormat.format(currentDate);

        //find date input box and clear
        WebElement dateInput = driver.findElement(By.xpath("/html/body/div[3]/form[1]/div[1]/table/tbody/tr/td[1]/input[3]"));
        dateInput.clear();
        dateInput.sendKeys(currentDateAsString);

        //find go button and click
        WebElement goButton = driver.findElement(By.xpath("/html/body/div[3]/form[1]/div[1]/table/tbody/tr/td[1]/input[4]"));
        goButton.click();

        return scrapeOrders(driver);
    }

    private HashMap<Date, Integer> scrapeMilkSupplyTruckCounts(WebDriver driver) {
        HashMap<Date, Integer> truckCountMap = new HashMap<>();

        //wait for table visibility
        waitUntilVisible(driver, By.id("scheduleTable"), 10);

        //find startDate and parse
        WebElement startDateTableCell = driver.findElement(By.xpath("//*[@id='interval-0']/table/tbody/tr[2]/th[1]"));
        String startDateAsString = startDateTableCell.getText();
        //add current year to date string
        startDateAsString += ("/" + Calendar.getInstance().get(Calendar.YEAR));

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Date startDate = null;

        try {
            startDate = format.parse(startDateAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //get table and extract all <tr> tags
        WebElement mainTable = driver.findElement(By.xpath("//*[@id='scheduleTable']/tbody"));
        List<WebElement> tableRows = mainTable.findElements(By.tagName("tr"));

        for (WebElement row : tableRows) {
            Date runningDate = startDate;

            if (row.isDisplayed()) {
                WebElement tableHeaderTag = null;

                try {
                    tableHeaderTag = row.findElement(By.xpath(".//th[1]"));
                } catch (Exception ignored){
                }

                if (tableHeaderTag != null) {
                    String tagText = tableHeaderTag.getText();

                    if (tagText.equals("Total Trucks")) {
                        List<WebElement> tdtags = row.findElements(By.tagName("td"));

                        for (WebElement tag : tdtags) {
                            if (tag.getAttribute("class").equals(" mcol bold") || tag.getAttribute("class").equals(" lcol bold")) {
                                String cellText = tag.getText();

                                if (!cellText.equals(" ")) {
                                    Integer truckCount = Integer.valueOf(cellText);

                                    if (truckCountMap.containsKey(runningDate)) {
                                        Integer currentTruckCount = truckCountMap.get(runningDate);

                                        currentTruckCount = currentTruckCount + truckCount;
                                        truckCountMap.put(runningDate, currentTruckCount);
                                    } else {
                                        truckCountMap.put(runningDate, truckCount);
                                    }
                                }

                                runningDate = addDays(runningDate, 1);
                            }
                        }
                    }
                }
            }
        }

        return truckCountMap;
    }

    //used for first script
    private List<ScrapeDataEntity> navigate(WebDriver driver) {
        waitUntilVisible(driver, By.id("toptabs"), 10);

        //click on other
        WebElement otherAnchor = driver.findElement(By.xpath("//*[@id='toptabs']/table/tbody/tr/td[2]/a"));
        otherAnchor.click();

        //click on supply schedule
        WebElement supplyAnchor = driver.findElement(By.xpath("//*[@id='dispatchRegionDiv']/table/tbody/tr[2]/td/a"));
        supplyAnchor.click();

        // wait for visible and click detail dropdown
        waitUntilVisible(driver, By.id("sch-view-det"), 10);
        WebElement detailDropdown = driver.findElement(By.id("sch-view-det"));
        detailDropdown.click();

        //find main table and fetch all tr tags
        WebElement tableBody = driver.findElement(By.xpath("//*[@id='scheduleTable']/tbody"));
        List<WebElement> tableRows = tableBody.findElements(By.tagName("tr"));

        //build list to store scraped data, instantiate running variable for plant name
        List<ScrapeDataEntity> dataEntityList = new ArrayList<>();
        String currentPlant = "none";
        Boolean withinDetailRange = false;

        //find start date and use to be incremented
        WebElement startDateTableCell = driver.findElement(By.xpath("//*[@id='interval-0']/table/tbody/tr[2]/th[1]"));
        String startDateAsString = startDateTableCell.getText();
        //add current year to date string
        startDateAsString += ("/" + Calendar.getInstance().get(Calendar.YEAR));

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Date startDate = null;

        try {
            startDate = format.parse(startDateAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //iterate through each table row
        for (WebElement row : tableRows) {
            String currentRowId = row.getAttribute("id");
            //reset running Date to original start date for each table row
            Date runningDate = startDate;

            if (currentRowId == null) {
                currentRowId = "";
            }

            //get current plant; use as running variable
            if (currentRowId.startsWith("topic")) {
                WebElement plantAnchor = row.findElement(By.xpath(".//td[1]/a"));
                String currentAchorId = plantAnchor.getAttribute("id");

                currentPlant = plantAnchor.getText();
                withinDetailRange = false;
                continue;
            }

            if ((currentRowId.startsWith("detail") && !(currentRowId.contains("end")) && row.isDisplayed()) || (withinDetailRange && row.isDisplayed())) {
                withinDetailRange = true;

                //get all td tags
                List<WebElement> tdTags = row.findElements(By.tagName("td"));
                int tdCount = 0;

                for (WebElement td : tdTags) {
                    //increment td count to track date switch
                    tdCount += 1;

                    //skip first increment but then increment date every other <td> iteration
                    if (tdCount != 1 && (tdCount % 2 != 0)) {
                        //increment date
                        assert runningDate != null;
                        runningDate = addDays(runningDate, 1);
                    }

                    //debug check
                    String tdClass = td.getAttribute("class");

                    if (!(td.getAttribute("class").contains("sep")) && !(td.getAttribute("class").contains("empty"))) {


                        ScrapeDataEntity dataEntity = new ScrapeDataEntity();
                        WebElement routeAnchor = td.findElement(By.xpath(".//div/a"));

                        //set params
                        dataEntity.setSourcePlantCode(currentPlant);
                        dataEntity.setDestinationPlantCode(routeAnchor.getText());
                        dataEntity.setRouteInitialDate(runningDate);

                        //add to list: to be returned from method call
                        dataEntityList.add(dataEntity);
                    }
                }

                if (currentRowId.startsWith("detail-end")) {
                    withinDetailRange = false;
                }
            }
        }
        return dataEntityList;
    }

    private void waitUntilVisible(WebDriver driver, By location, int waitTimeSec) {
        WebDriverWait wait = new WebDriverWait(driver, waitTimeSec);
        wait.until(ExpectedConditions.visibilityOfElementLocated(location));
    }
}
