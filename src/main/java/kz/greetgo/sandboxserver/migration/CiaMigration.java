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
    public static int uploadMaxBatchSize = 50_000;
    public String tmpClientTable;
    public String tmpPhoneTable;
    public String errorFileName;
    private Connection connection;
    public InputStream inputData;
    public OutputStream outputStream;

    public CiaMigration(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void close() {
        closeConnection();
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection = null;
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
        try (Statement statement = connection.createStatement()) {
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
        uploadToTmp();
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

    protected void uploadToTmp() {
        long startedAt = System.nanoTime();

        String insertClientSQL = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO TMP_PHONE (client_id, type, number, status) VALUES (?,?,?,'JUST INSERTED')";

        try (PreparedStatement ciaPS = connection.prepareStatement(r(insertClientSQL));
             PreparedStatement phonesPS = connection.prepareStatement(r(insertPhonesPS))) {
            connection.setAutoCommit(false);

            MySAXHandler handler = new MySAXHandler(connection, ciaPS, phonesPS);
            handler.startedAt = startedAt;
            handler.parse(inputData, outputStream);

            inputData.close();
            connection.setAutoCommit(true);
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
            markDuplicateClients();
            markDuplicatePhones();
            markUpdateAndInsertClients();
        } catch (SQLException e) {
            throw new RuntimeException("TgVy9663Cw :: ", e);
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

    protected void markDuplicateClients() throws SQLException {
        //language=PostgreSQL
        exec("WITH CTE AS ( SELECT id, ROW_NUMBER() OVER (PARTITION BY client_id ORDER BY id DESC) AS row_num" +
                " FROM TMP_CLIENT )" +
                " UPDATE TMP_CLIENT t SET status = 'DUPLICATE'" +
                " FROM CTE c WHERE c.row_num > 1 AND t.id = c.id;");
    }

    protected void markDuplicatePhones() throws SQLException {
        //language=PostgreSQL
        exec("WITH CTE AS ( SELECT id, ROW_NUMBER() OVER (PARTITION BY client_id, type, number ORDER BY id DESC) AS row_num" +
                " FROM TMP_PHONE )" +
                " UPDATE TMP_PHONE t SET status = 'DUPLICATE'" +
                " FROM CTE c WHERE c.row_num > 1 AND t.id = c.id");
    }
    protected void markUpdateAndInsertClients() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT AS t SET status = " +
                "CASE" +
                " WHEN EXISTS (SELECT 1 FROM client WHERE client.id = t.client_id) THEN 'FOR UPDATE' " +
                " ELSE 'FOR INSERT'" +
                "END" +
                " WHERE status = 'JUST INSERTED'");
    }

    protected void migrateFromTmp() {
        //language=PostgreSQL
        try {
            upsertDataToCharm();
            updateDataInClient();
            insertDataToClient();
            upsertDataToClientPhone();
            upsertDataToClientAddr();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    protected void upsertDataToCharm() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO charm (name) SELECT DISTINCT charm FROM TMP_CLIENT WHERE status != 'ERROR' AND status != 'DUPLICATE'" +
                " ON CONFLICT (name) DO NOTHING ");
    }

    protected void updateDataInClient() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE client AS c " +
                "SET surname = t.surname, name = t.name, patronymic = t.patronymic, gender = t.gender, " +
                "birth_date = TO_DATE(t.birth, 'YYYY-MM-DD'), charm_id = (SELECT id FROM charm WHERE name = t.charm) " +
                "FROM TMP_CLIENT t WHERE t.status = 'FOR UPDATE' AND c.id = t.client_id");
    }

    protected void insertDataToClient() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client (id, surname, name, patronymic, gender, birth_date, charm_id) " +
                "SELECT client_id, surname, name, patronymic, gender, TO_DATE(birth, 'YYYY-MM-DD'), (SELECT id FROM charm WHERE name = charm) " +
                "FROM TMP_CLIENT WHERE status = 'FOR INSERT'");

    }

    protected void upsertDataToClientPhone() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client_phone (client, number, type) " +
                "SELECT tp.client_id, tp.number, tp.type FROM TMP_PHONE tp " +
                "WHERE tp.status != 'DUPLICATE' " +
                "AND EXISTS (SELECT 1 FROM client c WHERE c.id = tp.client_id) " +
                "ON CONFLICT (client, type, number) DO NOTHING");  // to do optimize takes 1 minute (CTE + JOIN OR THIS)
    }

    protected void upsertDataToClientAddr() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'FACT', fact_street, fact_house, fact_flat FROM TMP_CLIENT WHERE status != 'ERROR' AND status != 'DUPLICATE'" +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client, type, street, house, flat) " +
                "SELECT client_id, 'REG', register_street, register_house, register_flat FROM TMP_CLIENT WHERE status != 'ERROR' AND status != 'DUPLICATE'" +
                "ON CONFLICT (client, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
    }

    protected void uploadErrors() {
        try (Statement statement = connection.createStatement()) {

            int offset = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                String sql = "SELECT client_id, name, surname, birth, status, error FROM " + tmpClientTable + " WHERE status='ERROR' LIMIT " + uploadMaxBatchSize + " OFFSET " + offset;
                ResultSet resultSet = statement.executeQuery(sql);

                int rowCount = 0;

                while (resultSet.next()) {
                    String data = resultSet.getString("client_id") + "," + resultSet.getString("name") + "," + resultSet.getString("surname")
                            + "," + resultSet.getString("birth") + "," + resultSet.getString("status") + "," + resultSet.getString("error") + "\n";
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
