import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ApachePOIExcelWrite {
    private static final String prodWindowsFilePath = "S:\\Supply_Chain\\Movement_and_Storage\\Hauling\\Dispatch\\Cream_2018.xlsx";

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
