package mo;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntryPoint {

    private Connection _connect = null;
    private Statement _statement = null;
    private ResultSet _resultSet = null;
    private final String USER = "root";
    private final String PASSWORD = "pass";

    private final static String TOKEN = "TOKEN";


    public String checkTable() throws SQLException {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("given the table playground.");
            _resultSet = _statement.executeQuery("select * from playground.most_funded_feb_2023");
            readMetaData(_resultSet, builder);
            return builder.toString();
        } catch (Exception e) {
            throw e;
        }
    }

    public EntryPoint() throws SQLException {
        _connect = DriverManager.getConnection("jdbc:mysql://localhost/playground", USER , PASSWORD);
        _statement = _connect.createStatement();
    }

    private void close() {
        try {
            if (_resultSet != null) {
                _resultSet.close();
            }

            if (_statement != null) {
                _statement.close();
            }

            if (_connect != null) {
                _connect.close();
            }
        } catch (Exception e) {

        }
    }

    private void readMetaData(ResultSet resultSet, StringBuilder builder) throws SQLException {
        //System.out.println("The columns in the table are: ");
        builder.append( resultSet.getMetaData().getTableName(1));
        builder.append(" that has the columns: ");
        //System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
            //System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
            builder.append(" ");
            builder.append( resultSet.getMetaData().getColumnName(i));
            if (i<resultSet.getMetaData().getColumnCount()) {
                builder.append(", ");
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        //importCSV();
        NLToSQLForCSV();
        NLToSQLWithJoin();

    }

    private static void NLToSQLForCSV() throws SQLException {
        EntryPoint entry = new EntryPoint();
        OpenAiService service = new OpenAiService(TOKEN);
        StringBuilder prompt = new StringBuilder(entry.checkTable());
        String Q = "find the  10 countries with the highest total goal";
        Q = "العثور على أكثر 10 دول تسجيلاً للأهداف الإجمالية";
        Q = "find the  10 creators with the largest goal in gb";
        //Q = "find the country with most creators";


        prompt.append("Create a SQL request to ");
        prompt.append(Q);
        prompt.append(" answer only in sql");
        System.out.println("\nGenerating SQL ... \n");
        System.out.println(prompt);
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                //.model("gpt-3.5-turbo")
                .prompt(prompt.toString())
                .temperature(0.3)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .maxTokens(150)
                .build();
        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        CompletionChoice choice = choices.get(0);
        String sql = choice.getText();
        System.out.println("Question : " + Q + "\n");
        System.out.println("=======  Generated Query   ====== \n");
        System.out.println(sql.trim());

        System.out.println("\nAnswer: \n");
        entry.runQuery(sql);
    }

    private static void NLToSQLWithJoin() throws SQLException {
        EntryPoint entry = new EntryPoint();
        OpenAiService service = new OpenAiService(TOKEN);
        String prompt = "Given the tables playground.products, and playground.product_sales. Where " +
                "The playground.products table has the columns id, and name and  the playground.product_sales " +
                "table has the columns product_id, units_sold, and price_per_unit generate SQL query " +
                "to select the top 10 product names with the highest total sales answer only in sql ";
        System.out.println("\nGenerating SQL ... \n");
        System.out.println(prompt);
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                //.model("gpt-3.5-turbo")
                .prompt(prompt)
                .temperature(0.3)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .maxTokens(150)
                .build();
        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        CompletionChoice choice = choices.get(0);
        String sql = choice.getText();
        System.out.println("=======  Generated Query   ====== \n");
        System.out.println(sql.trim());

        System.out.println("\nAnswer: \n");
        entry.runQuery(sql);
    }

    private static void createImage(OpenAiService service) {
        System.out.println("\nCreating Image...");
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt("A cow breakdancing with a turtle")
                .build();

        System.out.println("\nImage is located at:");
        System.out.println(service.createImage(request).getData().get(0).getUrl());
    }

    private static void importCSV() {
        CSVToMySQL converter = new CSVToMySQL();
        try {
            converter.importCSV(new File("data/most_funded_feb_2023.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runQuery(String sql) {
        try {
            _resultSet = _statement.executeQuery(sql);
            List<String> columns = new ArrayList<>();
            List<Integer> colType = new ArrayList<>();
            for  (int i = 1; i<= _resultSet.getMetaData().getColumnCount(); i++){
                System.out.print(_resultSet.getMetaData().getColumnName(i));
                System.out.print("\t\t\t");
                columns.add(_resultSet.getMetaData().getColumnName(i));
                colType.add(_resultSet.getMetaData().getColumnType(i));
            }
            System.out.println("");
            while (_resultSet.next()) {
                StringBuilder builder = new StringBuilder();
                for (int index = 0 ; index < columns.size() ; index++) {
                    String colLabl = columns.get(index);
                    switch (colType.get(index)) {
                        case Types.DATE -> builder.append(_resultSet.getDate(colLabl));
                        case Types.INTEGER -> builder.append(_resultSet.getInt(colLabl));
                        case Types.DOUBLE -> builder.append(_resultSet.getDouble(colLabl));
                        default -> builder.append(_resultSet.getString(colLabl));
                    }
                    builder.append("\t\t\t");
                }
                System.out.println(builder);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }
}