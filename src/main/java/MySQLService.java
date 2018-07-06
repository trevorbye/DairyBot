import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MySQLService {

    public static void loadResultSetToDatabase(List<ReportScrapeEntity> reportScrapeEntityList) throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.jdbc.Driver");

        String connectionString = "jdbc:mysql://localhost:3306/utility_data";
        String username = "trbye";
        String password = "Ad7c9c3A%";

        Connection connection = null;
        Date now = new Date();

        try {
            connection = DriverManager.getConnection(connectionString, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String query = "INSERT INTO ddc_load_schedule_scraped (receiving_plant, received_date, received_time, route_number, hauler_desc, scrape_script_rundate) VALUES(?,?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        for (ReportScrapeEntity entity : reportScrapeEntityList) {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

            preparedStatement.setNString(1, entity.getReceivingPlant());
            preparedStatement.setNString(2, entity.getReceivedDate());
            preparedStatement.setNString(3, entity.getReceivedTime());
            preparedStatement.setNString(4, entity.getRouteNumber());
            preparedStatement.setNString(5, entity.getHaulerDesc());
            preparedStatement.setNString(6, df.format(now));

            preparedStatement.execute();
        }

        preparedStatement.close();
        connection.close();
        System.exit(0);
    }
}
