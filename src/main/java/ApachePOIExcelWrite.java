import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ApachePOIExcelWrite {
    private static final String prodWindowsFilePath = "S:\\DHQ Public\\Dispatch\\Cream_2018.xlsx";
    private static final String script2ProdWindowsFilePath = "S:\\DHQ Public\\Dispatch\\Milk_Workbook_Current.xlsx";
    private static final String script2TestPath = "/Users/trevorBye/Desktop/Milk_Workbook_Current.xlsx";

    public void manipulateExcelFileForScript2(OrderScheduleAndMilkSupplyWrapper wrapper) throws FileNotFoundException {
        //connect to workbook as fileinput stream
        FileInputStream fileInputStream = new FileInputStream(new File(script2ProdWindowsFilePath));

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get sheet for previous month
        XSSFSheet startSheet = workbook.getSheet(getOffsetMonthSheetName());
        int startSheetIndex = workbook.getSheetIndex(startSheet);

        //get start date from wrapper method
        Date startDate = wrapper.getStartDate();
        Integer startDateCellIndex = setStartDateColumn(startSheet, startDate);

        //if start date isn't found, set start sheet to next sheet
        if (startDateCellIndex == 0) {
            startSheet = workbook.getSheetAt(startSheetIndex + 1);
            startSheetIndex++;

            startDateCellIndex = setStartDateColumn(startSheet, startDate);
        }

        //loop through each plant row, process 14 days out in columns
        for (int x = 2; x <= 29; x++) {
            //reset for each new row
            int runningDateCellIndex = startDateCellIndex;

            XSSFSheet runningSheet = startSheet;
            int runningSheetIndex = workbook.getSheetIndex(runningSheet);

            //get current row, have to reset this if you change sheets
            Row currentRow = runningSheet.getRow(x);
            int cellCount = currentRow.getLastCellNum();
            String currentPlantCode = currentRow.getCell(0).getStringCellValue();

            double currentWeekAdd = 0;
            double currentWeekCut = 0;
            double monthAdd = 0;
            double monthCut = 0;

            //iterate out 14 days
            for (int i = 1; i <= 14; i++) {
                //current cell value
                Boolean isCurrentCellEmpty;
                Date correspondingDate = null;

                try {
                    correspondingDate = runningSheet.getRow(0).getCell(runningDateCellIndex).getDateCellValue();
                } catch (Exception ignored) {
                    //week has ended
                }

                //if date is empty, week has ended
                if (correspondingDate == null) {
                    //increment weekly add/cuts
                    double runningAdd = currentRow.getCell(runningDateCellIndex).getNumericCellValue();
                    double runningCut = currentRow.getCell(runningDateCellIndex + 1).getNumericCellValue();

                    runningAdd = runningAdd + currentWeekAdd;
                    runningCut = runningCut + currentWeekCut;

                    currentRow.getCell(runningDateCellIndex).setCellValue(runningAdd);
                    currentRow.getCell(runningDateCellIndex + 1).setCellValue(runningCut);

                    //reset variables
                    currentWeekAdd = 0;
                    currentWeekCut = 0;

                    //change date row reference to where next date could potentially be
                    runningDateCellIndex = runningDateCellIndex + 2;

                    //now check if month has ended. if so, change sheet and reset row reference
                    correspondingDate = runningSheet.getRow(0).getCell(runningDateCellIndex).getDateCellValue();

                    if (correspondingDate == null) {
                        //month has ended
                        runningDateCellIndex++;

                        double runningMonthAdd = currentRow.getCell(runningDateCellIndex).getNumericCellValue();
                        double runningMonthCut = currentRow.getCell(runningDateCellIndex + 1).getNumericCellValue();

                        runningMonthAdd = runningMonthAdd + monthAdd;
                        runningMonthCut = runningMonthCut + monthCut;

                        currentRow.getCell(runningDateCellIndex).setCellValue(runningMonthAdd);
                        currentRow.getCell(runningDateCellIndex + 1).setCellValue(runningMonthCut);

                        //reset out-of-scope variables
                        monthAdd = 0;
                        monthCut = 0;

                        //change sheet and row reference
                        runningSheetIndex++;
                        runningSheet = workbook.getSheetAt(runningSheetIndex);
                        currentRow = runningSheet.getRow(x);

                        //change corresponding date and column reference
                        runningDateCellIndex = 1;
                        correspondingDate = runningSheet.getRow(0).getCell(runningDateCellIndex).getDateCellValue();
                    }
                }


                try {
                    isCurrentCellEmpty = !currentRow.getCell(runningDateCellIndex).getBooleanCellValue();
                } catch (IllegalStateException e) {
                    isCurrentCellEmpty = false;
                }


                if (isCurrentCellEmpty) {
                    //do not increment add/cut
                    OrderScheduleScrapeDataEntity searchedEntity = findEntityMatch(wrapper.getScheduleScrapeDataEntityList(), correspondingDate, currentPlantCode);

                    if (searchedEntity == null) {
                        //set cell truckCount to zero
                        currentRow.getCell(runningDateCellIndex).setCellValue(0);
                    } else {
                        currentRow.getCell(runningDateCellIndex).setCellValue(searchedEntity.getTruckCount());
                    }

                } else {
                    //get current truck count
                    double currentTruckCount = currentRow.getCell(runningDateCellIndex).getNumericCellValue();
                    OrderScheduleScrapeDataEntity comparisonSearchedEntity = findEntityMatch(wrapper.getScheduleScrapeDataEntityList(), correspondingDate, currentPlantCode);

                    //increment add/cut variables
                    double comparisonTruckCount;

                    if (comparisonSearchedEntity == null) {
                        comparisonTruckCount = 0;
                    } else {
                        comparisonTruckCount = comparisonSearchedEntity.getTruckCount();
                    }

                    if (currentTruckCount < comparisonTruckCount) {
                        //add
                        currentWeekAdd = currentWeekAdd + (comparisonTruckCount - currentTruckCount);
                        monthAdd = monthAdd + (comparisonTruckCount - currentTruckCount);

                        //set cell value
                        currentRow.getCell(runningDateCellIndex).setCellValue(comparisonTruckCount);

                    } else {
                        //cut
                        currentWeekCut = currentWeekCut + (currentTruckCount - comparisonTruckCount);
                        monthCut = monthCut + (currentTruckCount - comparisonTruckCount);

                        //set cell value
                        currentRow.getCell(runningDateCellIndex).setCellValue(comparisonTruckCount);
                    }
                }
                runningDateCellIndex++;
            }

            //find next week-end, increment
            for (int y = runningDateCellIndex; y <= cellCount; y++) {
                Row dateRow = runningSheet.getRow(0);
                Date markerDate = null;

                try {
                    markerDate = dateRow.getCell(y).getDateCellValue();
                } catch (Exception ignored) {
                }

                if (markerDate == null) {

                    //increment weekly add/cuts
                    double runningAdd = currentRow.getCell(y).getNumericCellValue();
                    double runningCut = currentRow.getCell(y + 1).getNumericCellValue();

                    runningAdd = runningAdd + currentWeekAdd;
                    runningCut = runningCut + currentWeekCut;

                    currentRow.getCell(y).setCellValue(runningAdd);
                    currentRow.getCell(y + 1).setCellValue(runningCut);

                    break;
                }
            }

            //find next month-end, increment
            for (int z = runningDateCellIndex; z <= cellCount; z++) {
                //get row text "TOTAL"
                String rowText = null;
                Row testRow = runningSheet.getRow(0);

                try {
                    rowText = testRow.getCell(z).getStringCellValue();
                } catch (Exception ignored) {
                    continue;
                }

                if (rowText.equals("TOTAL")) {
                    double runningMonthAdd = currentRow.getCell(z).getNumericCellValue();
                    double runningMonthCut = currentRow.getCell(z + 1).getNumericCellValue();

                    runningMonthAdd = runningMonthAdd + monthAdd;
                    runningMonthCut = runningMonthCut + monthCut;

                    currentRow.getCell(z).setCellValue(runningMonthAdd);
                    currentRow.getCell(z + 1).setCellValue(runningMonthCut);

                    break;
                }
            }
        }

        /*
        *
        * do supply row data insert
        *
         */

        int runningDateCellIndex = startDateCellIndex;

        XSSFSheet runningSheet = startSheet;
        int runningSheetIndex = workbook.getSheetIndex(runningSheet);

        //get current row, have to reset this if you change sheets
        Row currentRow = runningSheet.getRow(31);

        //iterate out 'n' days
        for (int x = 1; x <= wrapper.getMilkSupplyTruckCountMap().size(); x++) {
            Date correspondingDate = null;

            try {
                correspondingDate = runningSheet.getRow(0).getCell(runningDateCellIndex).getDateCellValue();
            } catch (Exception ignored) {
                //week has ended
            }

            if (correspondingDate == null) {

                //change date row reference to where next date could potentially be
                runningDateCellIndex = runningDateCellIndex + 2;

                //now check if month has ended. if so, change sheet and reset row reference
                correspondingDate = runningSheet.getRow(0).getCell(runningDateCellIndex).getDateCellValue();

                if (correspondingDate == null) {
                    //change sheet and row reference
                    runningSheetIndex++;
                    runningSheet = workbook.getSheetAt(runningSheetIndex);
                    currentRow = runningSheet.getRow(31);
                }
            }

            //find match in list of supply counts
            for (Map.Entry<Date, Integer> entry : wrapper.getMilkSupplyTruckCountMap().entrySet()) {
                if (entry.getKey().compareTo(correspondingDate) == 0) {
                    currentRow.getCell(runningDateCellIndex).setCellValue(entry.getValue());
                    break;
                }
            }

            runningDateCellIndex++;
        }

        /*
        *
        *
        *  evaluate all formulas
        *
        *
         */
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

        //close input stream
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream outputStream = new FileOutputStream(new File(script2ProdWindowsFilePath));

        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(" File updated... ");
    }

    public OrderScheduleScrapeDataEntity findEntityMatch(List<OrderScheduleScrapeDataEntity> entityList, Date checkDate, String checkCode) {

        for (OrderScheduleScrapeDataEntity entity : entityList) {
            if (entity.getOrderDate().compareTo(checkDate) == 0 && entity.getPlantCode().equals(checkCode)) {
                return entity;
            }
        }
        return null;
    }

    public int setStartDateColumn(XSSFSheet startSheet, Date startDate) {
        int startDateCellIndex = 0;

        //find start sheet and column for start date
        Row dateRow = startSheet.getRow(0);
        int cellCount = dateRow.getLastCellNum();

        for (int x = 0; x <= cellCount; x++) {
            Date formattedDate = null;

            try {
                formattedDate = dateRow.getCell(x).getDateCellValue();
            } catch (Exception ignored) {
            }

            if (formattedDate != null) {
                if (formattedDate.compareTo(startDate) == 0) {
                    startDateCellIndex = x;
                    break;
                }
            }
        }
        return startDateCellIndex;
    }

    public void writeExcelFile(List<ScrapeDataEntity> entityList) throws FileNotFoundException {
        //connect to workbook as fileinput stream
        FileInputStream fileInputStream = new FileInputStream(new File(prodWindowsFilePath));

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get sheet name and index based on current month
        XSSFSheet sheet = workbook.getSheet(getMonthSheetName());
        int currentSheetIndex = workbook.getSheetIndex(sheet);

        //get startDate from wrapper object
        Date startDate = ScrapeDataEntity.trimListByPlantCode("", entityList).getStartDate();

        //find index for cell containing start date
        int currentDateCellIndex = 0;
        Row dateRow = sheet.getRow(1);
        //subtracting 1 to compensate for zero-indexed .getCell()
        int cellCount = dateRow.getPhysicalNumberOfCells() - 1;

        for (int x = 2; x <= cellCount; x++) {
            Date formattedDate = null;

            try {
                formattedDate = dateRow.getCell(x).getDateCellValue();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (formattedDate.compareTo(startDate) == 0) {
                currentDateCellIndex = x;
                break;
            }
        }

        //build each day, if week crosses over month move to next sheet
        int numberOfDaysToBuild = 7;
        Date currentDate = startDate;

        for (int x = 1; x <= numberOfDaysToBuild; x++) {

            //change to next sheet if crossing over month
            if (currentDateCellIndex > cellCount) {
                sheet = workbook.getSheetAt(currentSheetIndex + 1);
                currentDateCellIndex = 2;
            }

            String currentPlant = null;

            for (int row = 3; row <= 37; row++) {
                Row currentRow = sheet.getRow(row);
                currentPlant = currentRow.getCell(0).getStringCellValue();

                if (currentPlant.equals("")) {
                    continue;
                }

                //clear cell first
                currentRow.getCell(currentDateCellIndex).setCellValue("");

                //object to store entity to be deleted
                ScrapeDataEntity entityToRemove = null;

                //loop through entityList; if there is a match for plant and date, insert target plant and remove from list
                for (ScrapeDataEntity entity : entityList) {
                    if (entity.getSourcePlantCode().equals(currentPlant) && entity.getRouteInitialDate().compareTo(currentDate) == 0) {
                        //set cell
                        currentRow.getCell(currentDateCellIndex).setCellValue(entity.getDestinationPlantCode());

                        //flag entity to remove and break to go to next row
                        entityToRemove = entity;
                        break;
                    }
                }

                if (entityToRemove != null) {
                    entityList.remove(entityToRemove);
                }
            }

            currentDate = DateUtils.addDays(currentDate, 1);
            currentDateCellIndex++;
        }

        //close input stream
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream outputStream = new FileOutputStream(new File(prodWindowsFilePath));

        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(" File updated... ");
    }

    private String getOffsetMonthSheetName() {
        Calendar calendar = Calendar.getInstance();
        int monthNum = calendar.get(Calendar.MONTH) + 1;
        String sheetName;

        switch (monthNum) {
            case 1:
                sheetName = "Jan";
                break;
            case 2:
                sheetName = "Jan";
                break;
            case 3:
                sheetName = "Feb";
                break;
            case 4:
                sheetName = "Mar";
                break;
            case 5:
                sheetName = "Apr";
                break;
            case 6:
                sheetName = "May";
                break;
            case 7:
                sheetName = "Jun";
                break;
            case 8:
                sheetName = "Jul";
                break;
            case 9:
                sheetName = "Aug";
                break;
            case 10:
                sheetName = "Sep";
                break;
            case 11:
                sheetName = "Oct";
                break;
            case 12:
                sheetName = "Nov";
                break;
            default:
                sheetName = "nullSheet";
                break;
        }

        return sheetName;
    }

    private String getMonthSheetName() {
        Calendar calendar = Calendar.getInstance();
        int monthNum = calendar.get(Calendar.MONTH) + 1;
        String monthAsSheet = null;

        switch (monthNum) {
            case 1:
                monthAsSheet = "JAN";
                break;
            case 2:
                monthAsSheet = "FEB";
                break;
            case 3:
                monthAsSheet = "MAR";
                break;
            case 4:
                monthAsSheet = "APR";
                break;
            case 5:
                monthAsSheet = "MAY";
                break;
            case 6:
                monthAsSheet = "JUN";
                break;
            case 7:
                monthAsSheet = "JUL";
                break;
            case 8:
                monthAsSheet = "AUG";
                break;
            case 9:
                monthAsSheet = "SEP";
                break;
            case 10:
                monthAsSheet = "OCT";
                break;
            case 11:
                monthAsSheet = "NOV";
                break;
            case 12:
                monthAsSheet = "DEC";
                break;

        }
        return monthAsSheet;
    }
}
