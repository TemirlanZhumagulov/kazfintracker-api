package kz.greetgo.sandboxserver.migration;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static kz.greetgo.sandboxserver.migration.util.TimeUtils.showTime;

@Slf4j
public class CiaMigration implements Closeable {
    public static int downloadMaxBatchSize = 50_000;
    public static int uploadMaxBatchSize = 50_000;
    public String tmpClientTable;
    public String tmpPhoneTable;
    public String errorFileName;
    private Connection operConnection;
    public InputStream inputData;
    public OutputStream outputStream;

    public CiaMigration(Connection connection) {
        operConnection = connection;
    }

    @Override
    public void close() {
        closeOperConnection();
    }

    private void closeOperConnection() {
        if (operConnection != null) {
            try {
                operConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            operConnection = null;
        }
    }

    public String r(String sql) {
        sql = sql.replaceAll("TMP_CLIENT", tmpClientTable);
        sql = sql.replaceAll("TMP_PHONE", tmpPhoneTable);
        return sql;
    }

    public void exec(String sql) throws SQLException {
        String executingSql = r(sql);

        long startedAt = System.nanoTime();
        try (Statement statement = operConnection.createStatement()) {
            int updates = statement.executeUpdate(executingSql);
            log.info("Updated " + updates
                    + " records for " + showTime(System.nanoTime(), startedAt)
                    + ", EXECUTED SQL : " + executingSql);
        } catch (SQLException e) {
            log.error("ERROR EXECUTE SQL for " + showTime(System.nanoTime(), startedAt)
                    + ", message: " + e.getMessage() + ", SQL : " + executingSql);
            throw e;
        }
    }

    public void migrate() {
        prepareStorages();
        download();
        filterTmp();
        migrateFromTmp();
        uploadErrors();
    }

    protected void prepareStorages() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        Date nowDate = new Date();
        tmpClientTable = "tmp_" + sdf.format(nowDate) + "_client";
        tmpPhoneTable = "tmp_" + sdf.format(nowDate) + "_phone";
        errorFileName = "xml_file_errors_" + sdf.format(nowDate) + ".txt";

        log.info("tmp_client = " + tmpClientTable);
        log.info("tmp_phone = " + tmpPhoneTable);
        log.info("error file name = " + errorFileName);

        DatabaseSetup.dropCreateTables(tmpClientTable, tmpPhoneTable);
    }

    protected void download() {
        long startedAt = System.nanoTime();

        String insertClientSQL = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO TMP_PHONE (client_id, type, number) VALUES (?,?,?)";

        try (PreparedStatement ciaPS = operConnection.prepareStatement(r(insertClientSQL));
             PreparedStatement phonesPS = operConnection.prepareStatement(r(insertPhonesPS))) {
            operConnection.setAutoCommit(false);

            MySAXHandler handler = new MySAXHandler(operConnection, ciaPS, phonesPS);
            handler.startedAt = startedAt;
            handler.parse(inputData, outputStream);
            inputData.close();
            operConnection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException("MB53tR5JmA :: ", e);
        }
    }

    protected void filterTmp() {
        try {
            validateSurnameAbsence();
            validateNameAbsence();
            validateBirthDateAbsence();
            validateBirthDatePattern();
            validateAgeRange();
            validateDuplicateClients();
            validateDuplicatePhones();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void validateSurnameAbsence() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT SET error = 'surname is not defined', status='ERROR' " +
                "WHERE error = '' and (surname is null or surname = '')");

    }

    protected void validateNameAbsence() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT SET error = 'name is not defined', status='ERROR' " +
                "WHERE error = '' and (name is null or name = '')");

    }

    protected void validateBirthDateAbsence() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT SET error = 'birth_date is not defined', status='ERROR' " +
                "WHERE error = '' and (birth is null or birth = '')");

    }

    protected void validateBirthDatePattern() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT SET error = 'birth_date is not correct', status='ERROR'" +
                "WHERE error = '' and NOT birth ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}$';");

    }

    protected void validateAgeRange() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT" +
                " SET error = 'AGE_OUT_OF_RANGE', status = 'ERROR'" +
                " WHERE error = '' and EXTRACT(YEAR FROM AGE(NOW(), TO_DATE(birth, 'YYYY-MM-DD'))) NOT BETWEEN 18 AND 100");
    }

    protected void validateDuplicateClients() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT AS c1 SET error = 'DUPLICATE', status = 'ERROR' " +
                "FROM (" +
                "    SELECT client_id, MAX(id) AS max_id " +
                "    FROM TMP_CLIENT" +
                "    WHERE error = ''" +
                "    GROUP BY client_id " +
                "    HAVING COUNT(*) > 1 " +
                ") AS c2 WHERE c1.client_id = c2.client_id AND c1.id < c2.max_id;"); // to do optimize takes 40 seconds
    }
    protected void validateDuplicatePhones() throws SQLException{
        //language=PostgreSQL
        exec("DELETE FROM TMP_PHONE AS t1 " +
                "USING TMP_PHONE AS t2 " +
                "WHERE t1.client_id = t2.client_id" +
                "  AND t1.type = t2.type" +
                "  AND t1.number = t2.number" +
                "  AND t1.id < t2.id");
    }
    protected void migrateFromTmp() {
        //language=PostgreSQL
        try {
            upsertDataToCharm();
//            //language=PostgreSQL
//            exec("CREATE INDEX ON charm (name)");
            updateDuplicatesInClient();
            insertDataToClient();
//            //language=PostgreSQL
//            exec("CREATE INDEX ON client (id)");
            upsertDataToClientPhone();
            upsertDataToClientAddr();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    protected void upsertDataToCharm() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO charm (name) SELECT DISTINCT charm FROM TMP_CLIENT WHERE status != 'ERROR'" +
                " ON CONFLICT (name) DO NOTHING ");
    }
    protected void updateDuplicatesInClient(){
        //language=PostgreSQL
        String query2 = "UPDATE client AS c " +
                "SET surname = t.surname, name = t.name, patronymic = t.patronymic, gender = t.gender, " +
                "birth_date = TO_DATE(t.birth, 'YYYY-MM-DD'), charm_id = (SELECT id FROM charm WHERE name = t.charm) " +
                "FROM TMP_CLIENT AS t WHERE c.id = t.client_id " +
                "RETURNING c.id, c.surname, c.name, c.birth_date, c.gender";

        try (Statement statement = operConnection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(r(query2));
            if (resultSet.next()) {
                do {
                    String id = resultSet.getString("id");
                    String surname = resultSet.getString("surname");
                    String name = resultSet.getString("name");
                    Date birthDate = resultSet.getDate("birth_date");
                    String gender = resultSet.getString("gender");

                    outputStream.write(String.format("%s, %s, %s, %s, %s, %s, %s\n",
                            id, surname, name, birthDate, gender, "DUPLICATE", "ERROR").getBytes(StandardCharsets.UTF_8));
                } while (resultSet.next());
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("3M4w7M09be :: ", e);
        }
    }
    protected void insertDataToClient() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client (id, surname, name, patronymic, gender, birth_date, charm_id) " +
                "SELECT client_id, surname, name, patronymic, gender, TO_DATE(birth, 'YYYY-MM-DD'), (SELECT id FROM charm WHERE name = charm) " +
                "FROM TMP_CLIENT WHERE status != 'ERROR' " +
                "ON CONFLICT (id) DO NOTHING");

    }

    protected void upsertDataToClientPhone() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client_phone (client, number, type) " +
                "SELECT tp.client_id, tp.number, tp.type FROM TMP_PHONE tp " +
                "INNER JOIN client c ON tp.client_id = c.id " +
                "ON CONFLICT (client, type, number) DO NOTHING");  // to do optimize takes 1 minute (CTE + JOIN OR THIS)
    }

    protected void upsertDataToClientAddr() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'FACT', fact_street, fact_house, fact_flat FROM TMP_CLIENT WHERE status != 'ERROR' " +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'REG', register_street, register_house, register_flat FROM TMP_CLIENT WHERE status != 'ERROR' " +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
    }

    protected void uploadErrors() {
//        String filePath = "build/logs/" + errorFileName;
//        File file = new File(filePath);
//        File parentDir = file.getParentFile();
//        if (!parentDir.exists()) {
//            parentDir.mkdirs();
//        }

        try (Statement statement = operConnection.createStatement()) {
//             BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {

            int offset = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                String sql = "SELECT client_id, name, surname, birth, status, error FROM " + tmpClientTable + " WHERE status='ERROR' LIMIT " + uploadMaxBatchSize + " OFFSET " + offset;
                ResultSet resultSet = statement.executeQuery(sql);

                int rowCount = 0;

                while (resultSet.next()) {
                    String data = resultSet.getString("client_id") + "," + resultSet.getString("name") + "," + resultSet.getString("surname")
                            + "," + resultSet.getString("birth") + "," + resultSet.getString("status") + "," + resultSet.getString("error") + "\n";
//                    writer.write(data);
                    outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                    rowCount++;
                }

                resultSet.close();

                hasMoreData = rowCount == uploadMaxBatchSize;

                offset += uploadMaxBatchSize;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}
