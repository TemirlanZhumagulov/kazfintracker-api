package kz.kazfintracker.sandboxserver.migration;

import kz.kazfintracker.sandboxserver.migration.xml_parser.XmlParser;
import kz.kazfintracker.sandboxserver.migration.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class CiaMigration implements Closeable {
    public static int uploadMaxBatchSize = 50_000;
    public String tmpClientTable;
    public String tmpPhoneTable;
    private Connection connection;
    public InputStream inputData;
    public OutputStream outputErrors;

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
        exec(sql, List.of());
    }

    public void exec(String sql, List<Object> clientArgs) throws SQLException {
        String executingSql = r(sql);

        long startedAt = System.nanoTime();

        try (PreparedStatement ps = connection.prepareStatement(executingSql)) {
            for (int i = 0; i < clientArgs.size(); i++) {
                ps.setObject(i + 1, clientArgs.get(i));
            }
            int updates = ps.executeUpdate();
            log.info("a38O1snPZp :: Updated " + updates
                    + " records for " + TimeUtils.showTime(System.nanoTime(), startedAt)
                    + ", EXECUTED SQL : " + executingSql);
        } catch (SQLException e) {
            log.error("dDOVScBV6A :: ERROR EXECUTE SQL for " + TimeUtils.showTime(System.nanoTime(), startedAt)
                    + ", message: " + e.getMessage() + ", SQL : " + executingSql);
            throw e;
        }
    }

    public void migrate() {
        prepareTmpTables();
        uploadToTmp();
        filterTmp();
        migrateFromTmp();
        uploadErrors();
    }

    protected void prepareTmpTables() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        Date nowDate = new Date();
        tmpClientTable = "tmp_" + sdf.format(nowDate) + "_client";
        tmpPhoneTable = "tmp_" + sdf.format(nowDate) + "_phone";

        log.info("68d59iHg3u :: tmp_client = " + tmpClientTable);
        log.info("yEGAl3c8iU :: tmp_phone = " + tmpPhoneTable);

        DatabaseSetup.createCiaMigrationTmpTables(tmpClientTable, tmpPhoneTable);
    }

    protected void uploadToTmp() {
        long startedAt = System.nanoTime();

        String insertClientSQL = "INSERT INTO TMP_CLIENT (client_id, surname, name, patronymic, gender, charm, birth, fact_street, fact_house, fact_flat, register_street, register_house, register_flat, error, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,'','JUST INSERTED')";
        String insertPhonesPS = "INSERT INTO TMP_PHONE (client_id, type, number, status) VALUES (?,?,?,'JUST INSERTED')";

        try (PreparedStatement ciaPS = connection.prepareStatement(r(insertClientSQL));
             PreparedStatement phonesPS = connection.prepareStatement(r(insertPhonesPS))) {
            connection.setAutoCommit(false);

            XmlParser xmlParser = new XmlParser(connection, ciaPS, phonesPS);
            xmlParser.startedAt = startedAt;
            xmlParser.parse(inputData, outputErrors);

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
            validateBirthDate();
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
        exec("UPDATE TMP_CLIENT SET error = substring(md5(random() :: text) from 1 for 15) || ' :: surname is not defined', status='ERROR' " +
                "WHERE error = '' and (surname is null or surname = '')");

    }

    protected void validateNameAbsence() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT SET error = substring(md5(random() :: text) from 1 for 15) || ' :: name is not defined', status='ERROR' " +
                "WHERE error = '' and (name is null or name = '')");

    }

    protected void validateBirthDateAbsence() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT SET error = substring(md5(random() :: text) from 1 for 15) || ' :: birth_date is not defined', status='ERROR' " +
                "WHERE error = '' and (birth is null or birth = '')");

    }

    protected void validateBirthDate() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT SET error = substring(md5(random() :: text) from 1 for 15) || ' :: birth_date is not correct', status='ERROR'" +
                "WHERE error = '' AND NOT birth ~ " +
                "'^(0[0-9]{2}[1-9]|[1-9][0-9]{3})-((0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01])|02-(0[1-9]|1[0-9]|2[0-8])|(0[469]|11)-(0[1-9]|[12][0-9]|30))$'");

    }

    protected void validateAgeRange() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE TMP_CLIENT" +
                " SET error = substring(md5(random() :: text) from 1 for 15) || ' :: AGE_OUT_OF_RANGE', status = 'ERROR'" +
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
                " WHEN EXISTS (SELECT 1 FROM client c WHERE c.cia_id = t.client_id) THEN 'FOR UPDATE' " +
                " ELSE 'FOR INSERT'" +
                "END" +
                " WHERE status = 'JUST INSERTED'");
    }

    protected void migrateFromTmp() {
        try {
            insertDataFromTmpToCharm();
            updateDataInClientFromTmp();
            insertDataFromTmpToClient();
            insertDataFromTmpToClientPhone();
            upsertDataFromTmpToClientAddr();
        } catch (SQLException e) {
            throw new RuntimeException("iMWzYly6lg :: ", e);
        }
    }


    protected void insertDataFromTmpToCharm() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO charm (name) SELECT DISTINCT charm FROM TMP_CLIENT WHERE status != 'ERROR' AND status != 'DUPLICATE'" +
                " ON CONFLICT (name) DO NOTHING ");
    }

    protected void updateDataInClientFromTmp() throws SQLException {
        //language=PostgreSQL
        exec("UPDATE client AS c " +
                "SET surname = t.surname, name = t.name, patronymic = t.patronymic, gender = t.gender, " +
                "birth_date = TO_DATE(t.birth, 'YYYY-MM-DD'), charm_id = (SELECT id FROM charm WHERE name = t.charm) " +
                "FROM TMP_CLIENT t WHERE t.status = 'FOR UPDATE' AND c.cia_id = t.client_id");
    }

    protected void insertDataFromTmpToClient() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client (cia_id, surname, name, patronymic, gender, birth_date, charm_id) " +
                "SELECT client_id, surname, name, patronymic, gender, TO_DATE(birth, 'YYYY-MM-DD'), (SELECT id FROM charm WHERE name = charm) " +
                "FROM TMP_CLIENT WHERE status = 'FOR INSERT'");

    }

    protected void insertDataFromTmpToClientPhone() throws SQLException {

        //language=PostgreSQL
        exec("WITH CTE AS ( SELECT cia_id FROM client) " +
                "INSERT INTO client_phone (client_cia_id, number, type) " +
                "SELECT tp.client_id, tp.number, tp.type FROM TMP_PHONE tp " +
                "INNER JOIN CTE c ON tp.client_id = c.cia_id " +
                "WHERE tp.status != 'DUPLICATE' " +
                "ON CONFLICT (client_cia_id, type, number) DO NOTHING");  // to do optimize takes 1 minute (CTE + JOIN)
    }

    protected void upsertDataFromTmpToClientAddr() throws SQLException {
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client_cia_id, type, street, house, flat) " +
                "SELECT client_id, 'FACT', fact_street, fact_house, fact_flat FROM TMP_CLIENT WHERE status != 'ERROR' AND status != 'DUPLICATE'" +
                "ON CONFLICT (client_cia_id, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
        //language=PostgreSQL
        exec("INSERT INTO client_addr (client_cia_id, type, street, house, flat) " +
                "SELECT client_id, 'REG', register_street, register_house, register_flat FROM TMP_CLIENT WHERE status != 'ERROR' AND status != 'DUPLICATE'" +
                "ON CONFLICT (client_cia_id, type) DO UPDATE  SET street=excluded.street, house=excluded.house, flat=excluded.flat");
    }

    protected void uploadErrors() {
        try (Statement statement = connection.createStatement()) {

            int offset = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                String sql = "SELECT client_id, name, surname, birth, status, error FROM " + tmpClientTable + " WHERE status='ERROR' LIMIT " + uploadMaxBatchSize + " OFFSET " + offset;
                int rowCount = 0;

                try(ResultSet resultSet = statement.executeQuery(sql)) {
                    while (resultSet.next()) {
                        String data = resultSet.getString("client_id") + "," + resultSet.getString("name") + "," + resultSet.getString("surname")
                                + "," + resultSet.getString("birth") + "," + resultSet.getString("status") + "," + resultSet.getString("error") + "\n";
                        outputErrors.write(data.getBytes(StandardCharsets.UTF_8));
                        rowCount++;
                    }
                }
                hasMoreData = rowCount == uploadMaxBatchSize;

                offset += uploadMaxBatchSize;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}
