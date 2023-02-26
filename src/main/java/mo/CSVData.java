package mo;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CSVData implements Closeable {

    private final BufferedReader  _csvFile;
    private final String _path;
    private final List<Column> _columns = new ArrayList<>();

    private String _line;
    private final String _name;

    private final StringBuilder _rowSQL;

    public enum ColumnType {
        NUMBER,
        STRING,
        DATE,
        INT,
    }

    static public class Column {
        private final String _name;
        private ColumnType _type;

        public Column (String name, String sample) {
            _name = name;
            if (sample==null || sample.isBlank()) {
                _type = ColumnType.STRING;
                return;
            }
            try {
                Integer.parseInt(sample);
                _type = ColumnType.INT;
            } catch ( NumberFormatException e) {
                try {
                    Double.parseDouble(sample);
                    _type = ColumnType.NUMBER;
                } catch (NumberFormatException e1) {
                    try {
                        new SimpleDateFormat("dd/MM/yyyy").parse(sample);
                        _type = ColumnType.DATE;
                    } catch (ParseException e2) {
                        _type = ColumnType.STRING;
                    }
                }
            }
        }

        public String getName() {
            return _name;
        }

        public String sqlRep() {
            StringBuilder builder = new StringBuilder();
            builder.append(_name.replaceAll(" ", "_"));
            builder.append(" ");
            switch (_type) {
                case STRING -> builder.append("VARCHAR(255)");
                case NUMBER -> builder.append("DOUBLE");
                case DATE -> builder.append("DATE");
                case INT -> builder.append("INTEGER");
            }
            return builder.toString();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(_name);
            switch (_type) {
                case DATE -> builder.append(" Date ");
                case NUMBER -> builder.append(" Double ");
                case STRING -> builder.append(" String ");
                case INT -> builder.append(" Integer ");
            }
            builder.append(" SQL rep => ");
            builder.append(sqlRep());
            return builder.toString();
        }

        public ColumnType getType() {
            return _type;
        }
    }

    public CSVData(File file, String name) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        if (name ==null || name.isBlank()) {
            _name = file.getName().trim().replaceAll(" ", "_").replaceFirst("[.][^.]+$", "").toUpperCase();
        }else {
            _name = name.trim().replaceAll(" ", "_");
        }
        _path = file.getPath();
        _csvFile = new BufferedReader(new FileReader(file));
        String header = _csvFile.readLine();
        _line = _csvFile.readLine();
        collectColumns(header);
        _rowSQL = new StringBuilder("INSERT INTO ");
        _rowSQL.append(_name);
        _rowSQL.append("(");
        for (int index = 0 ; index < _columns.size() ; index++) {
            _rowSQL.append(" ");
            _rowSQL.append(_columns.get(index).getName());
            if (index != _columns.size()-1) {
                _rowSQL.append(",");
            }
        }
        _rowSQL.append(")");
    }

    private void collectColumns(final String header) {
        StringTokenizer tokenizerH = new StringTokenizer(header, ",");
        StringTokenizer tokenizerD = new StringTokenizer(_line, ",");
        while (tokenizerH.hasMoreElements()) {
            String hValue = tokenizerH.nextToken().trim();
            String dValue = tokenizerD.hasMoreElements() ? tokenizerD.nextToken().trim() : null;
            _columns.add(new Column(hValue, dValue));
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(_path);
        builder.append("\n");
        _columns.forEach(a -> {
            builder.append("\t");
            builder.append(a);
            builder.append("\n");
        });
        builder.append("SQL Rep: ");
        builder.append("\n");
        builder.append(sqlRep());
        builder.append("\n");
        builder.append(_rowSQL);
        return builder.toString();
    }

    public String sqlRep() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");
        builder.append(_name);
        builder.append(" (");
        for (int index = 0 ; index < _columns.size() ; index++) {
            builder.append(" ");
            builder.append(_columns.get(index).sqlRep());
            if (index!= _columns.size()-1) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public boolean hasRow() {
        return _line!=null;
    }

    public List<String> getRow() {
        if (_line!=null) {
            List<String> val = new ArrayList<>();
            StringTokenizer tokenizerD = new StringTokenizer(_line, ",");
            while (tokenizerD.hasMoreElements()) {
                val.add(tokenizerD.nextToken().trim());
            }
            try {
                _line = _csvFile.readLine();
                while (_line!=null && _line.contains("\"")){
                    _line = _csvFile.readLine();
                }
            } catch (IOException e) {
                System.out.println(" =============== Error =================== ");
            }
            return val;
        }
        return null;
    }

    public String getRowSQL() {
        List<String> data = getRow();
        if (data==null){
            return "";
        }
        StringBuilder row = new StringBuilder(_rowSQL);
        row.append(" VALUES (");
        for (int index = 0 ; index < data.size() ; index++) {
            row.append(" ");
            Column col = _columns.get(index);
            if (col.getType() == ColumnType.STRING) {
                row.append("\"");
            }
            row.append(data.get(index).replaceAll("\""," "));
            if (col.getType() == ColumnType.STRING) {
                row.append("\"");
            }
            if (index != data.size()-1) {
                row.append(",");
            }
        }
        row.append(")");
        return row.toString();
    }

    @Override
    public void close() throws IOException {
        _csvFile.close();
    }
}
