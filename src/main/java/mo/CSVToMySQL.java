package mo;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CSVToMySQL {

    private final String _url = "jdbc:mysql://localhost/playground";
    private final String _user = "root";
    private final String _pass = "ThePassword";


    public void importCSV(final File file) throws IOException, ClassNotFoundException {
        try ( Connection connect = DriverManager.getConnection(_url, _user, _pass)){
            Statement statement = connect.createStatement();
            try (CSVData csvData = new CSVData(file, null)) {
                String sql = csvData.sqlRep();
                statement.executeUpdate(sql);
                System.out.println(csvData);
                int counter = 0 ;
                while (csvData.hasRow()) {
                    String row = csvData.getRowSQL();
                    if (!row.isBlank()) {
                        System.out.println(row);
                        statement.executeUpdate(row);
                        counter++;
                    }
                }
                System.out.println(counter);
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
