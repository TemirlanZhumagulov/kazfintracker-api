package kz.greetgo.sandboxserver.migration;

public class LaunchMigration {
    public static void main(String[] args) {
        try (CiaMigration migration = new CiaMigration()) {
            CiaMigration.uploadMaxBatchSize = 50_000;
            CiaMigration.downloadMaxBatchSize = 50_000;
            migration.migrate();
        }
        System.out.println("Finish migration");
    }


}

