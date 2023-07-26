package kz.greetgo.sandboxserver.migration;


import kz.greetgo.sandboxserver.migration.util.RND;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GenerateInputFiles {

  public static final int CIA_LIMIT = 1_000_000;
  public static final int FRS_LIMIT = 10_000_000;

  public static void main(String[] args) throws Exception {
    new GenerateInputFiles().execute();
  }

  private static final String ENG = "abcdefghijklmnopqrstuvwxyz";
  private static final String DEG = "0123456789";

  private static final char[] ALL = (ENG + ENG.toUpperCase() + DEG).toCharArray();
  private static final char[] BIG = (ENG.toUpperCase() + DEG).toCharArray();

  private static final Random random = new Random();

  private static class Info {

    int transactionCount = 0;
    int accountCount = 0;
    int clientErrorRecordCount = 0;

    final Set<String> goodClientIds = new HashSet<>();

    public void newErrorClient() {
      clientErrorRecordCount++;
    }

    public void appendGoodClientId(String clientId) {
      goodClientIds.add(clientId);
    }

    public void newAccount() {
      accountCount++;
    }

    public void newTransaction() {
      transactionCount++;
    }

    public void printTo(PrintStream pr) {
      pr.println("Unique good client count = " + goodClientIds.size());
      pr.println("ClientDetails error record count = " + clientErrorRecordCount);
      pr.println("Transaction count = " + transactionCount);
      pr.println("Account count = " + accountCount);
    }
  }

  final Info info = new Info();

  @SuppressWarnings("SameParameterValue")
  private static String rndStr(int len) {
    final int allLength = ALL.length;
    final char ret[] = new char[len];
    for (int i = 0; i < len; i++) {
      ret[i] = ALL[random.nextInt(allLength)];
    }
    return String.valueOf(ret);
  }

  private static String rndClientId() {
    char cc[] = new char[8];
    cc[0] = DEG.charAt(random.nextInt(DEG.length()));
    for (int i = 1; i < cc.length; i++) {
      cc[i] = BIG[random.nextInt(BIG.length)];
    }

    return String.valueOf(cc, 0, 1) + '-' +
      String.valueOf(cc, 1, 3) + '-' +
      String.valueOf(cc, 4, 2) + '-' +
      String.valueOf(cc, 6, 2) + '-' +
      rndStr(10);
  }

  private static String rndAccountNumber() {
    return RND.str(5) + "KZ" + RND.str(3) + "-" + RND.str(5) + "-" + RND.str(5) + "-" + RND.str(7);
  }

  public static String formatBD(BigDecimal a) {
    if (a == null) {
      return "";
    }
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);

    df.setMinimumFractionDigits(0);

    df.setGroupingUsed(true);
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setGroupingSeparator('_');
    dfs.setDecimalSeparator('.');
    df.setDecimalFormatSymbols(dfs);
    String ret = df.format(a.setScale(2, RoundingMode.HALF_UP));
    if (ret.startsWith("-")) {
      return ret;
    }
    return "+" + ret;
  }

  enum ErrorType {
    NO_SURNAME(1),
    EMPTY_SURNAME(1),
    NO_NAME(1),
    EMPTY_NAME(1),
    NO_CHARM(1),

    BIRTH_DATE_TOO_OLD(1),
    BIRTH_DATE_TOO_YOUNG(1),
    BIRTH_DATE_NO(1),
    BIRTH_DATE_LEFT(1),

    //
    ;

    public final float priority;

    ErrorType(float priority) {
      this.priority = priority;
    }

    public float getPriority() {
      return priority;
    }
  }

  enum RowType {
    NEW_FOR_PAIR(100),
    EXISTS(100),
    @SuppressWarnings("unused")
    NEW_ALONE(10),
    ERROR(10),

    //
    ;
    final float priority;

    RowType(float priority) {
      this.priority = priority;
    }

    public float getPriority() {
      return priority;
    }
  }

  final List<String> commonClientIdList = new ArrayList<>();

  private static class SelectorRnd<T> {
    private Object[] values;

    public SelectorRnd(T[] values, java.util.function.Function<T, Float> priority) {

      int len = values.length * 10;
      this.values = new Object[len];
      float total = 0;
      for (T value : values) {
        total += priority.apply(value);
      }

      outer_for:
      for (int i = 0; i < len; i++) {

        float I = (float) i / (float) len * total;

        float current = 0;
        for (T value : values) {
          float p = priority.apply(value);
          if (current + p > I) {
            this.values[i] = value;
            continue outer_for;
          }
          current += p;
        }

        throw new RuntimeException("Left error");
      }

    }

    public T next() {
      //noinspection unchecked
      return (T) values[random.nextInt(values.length)];
    }

    public void showInfo() {
      System.out.println("Selector rnd: values.length = " + values.length);
      int i = 0;
      for (Object value : values) {
        System.out.println("  " + i++ + " : " + value);
      }
    }
  }

  final SelectorRnd<RowType> rowTypeRnd = new SelectorRnd<>(RowType.values(), RowType::getPriority);
  final SelectorRnd<ErrorType> errorTypeRnd = new SelectorRnd<>(ErrorType.values(), ErrorType::getPriority);

  private static String spaces(int count) {
    char ret[] = new char[count];
    for (int i = 0; i < count; i++) ret[i] = ' ';
    return String.valueOf(ret);
  }

  static final String DIR = "build/out_files";

  final AtomicBoolean clearPrinter = new AtomicBoolean(false);
  PrintStream printer = null;
  String outFileName;
  java.io.File outFile;

  private void execute() throws Exception {

    rowTypeRnd.showInfo();
    errorTypeRnd.showInfo();

    final java.io.File workingFile = new java.io.File(DIR + "/__working__");
    final java.io.File newCiaFile = new java.io.File(DIR + "/__new_cia_file__");
    final java.io.File newFrsFile = new java.io.File(DIR + "/__new_frs_file__");

    workingFile.getParentFile().mkdirs();
    workingFile.createNewFile();

    newCiaFile.getParentFile().mkdirs();
    newCiaFile.createNewFile();

    final AtomicBoolean working = new AtomicBoolean(true);
    final AtomicBoolean showInfo = new AtomicBoolean(false);

    new Thread(() -> {

      while (workingFile.exists() && working.get()) {

        try {
          Thread.sleep(300);
        } catch (InterruptedException e) {
          break;
        }

        if (!newCiaFile.exists()) {
          clearPrinter.set(true);

          newCiaFile.getParentFile().mkdirs();
          try {
            newCiaFile.createNewFile();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

      working.set(false);

    }).start();

    new Thread(() -> {

      while (working.get()) {

        try {
          Thread.sleep(700);
        } catch (InterruptedException e) {
          break;
        }

        showInfo.set(true);
      }

    }).start();

    {
      int i = 1;
      for (RowType rowType : RowType.values()) {
        printClient(i++, rowType);
      }

      currentFileRecords = 0;

      int fileIndex = 1;
      for (; currentFileRecords < CIA_LIMIT && working.get(); i++) {

        printClient(i, rowTypeRnd.next());
        currentFileRecords++;

        if (fileIndex == 1 && currentFileRecords >= 300) {
          fileIndex++;
          clearPrinter.set(true);
        }
        if (fileIndex == 2 && currentFileRecords >= 3000) {
          fileIndex++;
          clearPrinter.set(true);
        }
        if (fileIndex == 3 && currentFileRecords >= 30_000) {
          fileIndex++;
          clearPrinter.set(true);
        }
        if (fileIndex == 4 && currentFileRecords >= 300_000) {
          fileIndex++;
          clearPrinter.set(true);
        }

        if (showInfo.get()) {
          showInfo.set(false);
          System.out.println("Сформировано записей в текущем файле CIA: "
                               + currentFileRecords + ", всего записей: " + i);
        }
      }

      System.out.println("ИТОГО: Сформировано записей в текущем файле CIA: "
                           + currentFileRecords + ", всего записей: " + i);
    }

    finishPrinter("</cia>", ".xml");

    System.out.println("Файлы CIA сформированы: приступаем к формированию файлов FRS...");

    {
      newFrsFile.getParentFile().mkdirs();
      newFrsFile.createNewFile();

      currentFileRecords = 0;

      int i = 1, fileIndex = 1;
      for (; currentFileRecords < FRS_LIMIT && working.get(); i++) {

        printAccountWithTransactions(i);
        currentFileRecords++;

        if (fileIndex == 1 && currentFileRecords >= 30_000) {
          fileIndex++;
          clearPrinter.set(true);
        }
        if (fileIndex == 2 && currentFileRecords >= 700_000) {
          fileIndex++;
          clearPrinter.set(true);
        }

        if (showInfo.get()) {
          showInfo.set(false);
          System.out.println("Сформировано записей в текущем файле FRS: "
                               + currentFileRecords + ", всего записей: " + i);
        }
      }

      System.out.println("ИТОГО: Сформировано записей в текущем файле FRS: "
                           + currentFileRecords + ", всего записей: " + i);
    }

    finishPrinter(null, ".json_row.txt");

    working.set(false);

    archive();

    workingFile.delete();
    newCiaFile.delete();
    newFrsFile.delete();

    saveInfoFile();
  }

  private void saveInfoFile() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
    Date now = new Date();

    java.io.File file = new java.io.File(DIR + "/info_" + sdf.format(now) + ".txt");
    file.getParentFile().mkdirs();

    try (PrintStream pr = new PrintStream(file, "UTF-8")) {
      info.printTo(pr);
    }
  }

  int currentFileRecords = 0;


  @SuppressWarnings("unused")
  enum PhoneType {
    homePhone, mobilePhone, workPhone,
  }

  static final char DEG_CHARS[] = DEG.toCharArray();
  static final int DEG_CHARS_LENGTH = DEG_CHARS.length;

  static class Phone {
    String number;

    static Phone next() {

      StringBuilder sb = new StringBuilder();
      sb.append("+7-");

      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append('-');
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append('-');
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append('-');
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);
      sb.append(DEG_CHARS[random.nextInt(DEG_CHARS_LENGTH)]);

      if (random.nextInt(5) == 0) {
        sb.append(" вн. ").append(RND.str(4));
      }

      {
        Phone ret = new Phone();
        ret.number = sb.toString();
        return ret;
      }
    }

    String tag(PhoneType type) {
      if (type == null || number == null) {
        return null;
      }
      return "<" + type.name() + ">" + number + "</" + type.name() + ">";
    }
  }

  static class Address {
    String street, house, flat;

    static Address next() {
      Address ret = new Address();
      ret.street = RND.str(20);
      ret.house = RND.str(2);
      ret.flat = RND.str(2);
      return ret;
    }

    String toTag(String tagName) {
      return "<" + tagName + " street=\"" + street + "\" house=\"" + house + "\" flat=\"" + flat + "\"/>";
    }
  }

  private void finishPrinter(String lastLine, String extension) {
    PrintStream pr = printer;
    if (pr != null) {
      if (lastLine != null) {
        pr.println(lastLine);
      }
      pr.close();
      printer = null;

      java.io.File newCiaFile = new java.io.File(outFileName + '-' + currentFileRecords + extension);
      outFile.renameTo(newCiaFile);
    }
  }

  private void printClient(int clientIndex, RowType rowType) throws Exception {

    List<String> tags = new ArrayList<>();

    ErrorType errorType = null;

    if (rowType == RowType.ERROR) {
      errorType = errorTypeRnd.next();
    }

    if (errorType != ErrorType.NO_SURNAME) {

      if (errorType == ErrorType.EMPTY_SURNAME) {
        tags.add("    <surname value=\"\"/>");
      } else {
        tags.add("    <surname value=\"" + nextSurname() + "\"/>");
      }

    }

    if (errorType != ErrorType.NO_NAME) {

      if (errorType == ErrorType.EMPTY_NAME) {
        tags.add("    <name value=\"\"/>");
      } else {
        tags.add("    <name value=\"" + nextName() + "\"/>");
      }

    }

    if (errorType != ErrorType.BIRTH_DATE_NO) {

      if (errorType == ErrorType.BIRTH_DATE_LEFT) {

        tags.add("    <birth value=\"" + RND.str(10) + "\"/>");

      } else {

        Date date = null;

        if (errorType == ErrorType.BIRTH_DATE_TOO_OLD) {
          date = RND.dateYears(-10_000, -200);
        } else if (errorType == ErrorType.BIRTH_DATE_TOO_YOUNG) {
          date = RND.dateYears(-10, 0);
        }

        if (date == null) {
          date = RND.dateYears(-100, -18);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        tags.add("    <birth value=\"" + sdf.format(date) + "\"/>");

      }

    }

    switch (random.nextInt(5)) {
      case 1:
        break;

      case 2:
        tags.add("    <patronymic value=\"" + spaces(random.nextInt(3)) + "\"/>");
        break;

      default:
        tags.add("    <patronymic value=\"" + nextPatronymic() + "\"/>");
        break;
    }

    tags.add("    <address>\n" +
               "      " + Address.next().toTag("fact") + "\n" +
               "      " + Address.next().toTag("register") + "\n" +
               "    </address>"
    );

    if (errorType != ErrorType.NO_CHARM) {
      tags.add("    <charm value=\"" + nextCharm() + "\"/>");
    }

    tags.add("    <gender value=\"" + (random.nextBoolean() ? "MALE" : "FEMALE") + "\"/>");

    {
      int phoneCount = 2 + random.nextInt(5);
      for (int i = 0; i < phoneCount; i++) {
        tags.add("    " + Phone.next().tag(PhoneType.values()[random.nextInt(PhoneType.values().length)]));
      }
    }

    {
      PrintStream pr = printer;

      if (pr == null || clearPrinter.get()) {
        clearPrinter.set(false);

        finishPrinter("</cia>", ".xml");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date now = new Date();

        outFileName = DIR + "/from_cia_" + sdf.format(now) + "-" + ciaFileNo++;
        outFile = new java.io.File(outFileName + ".xml");
        outFile.getParentFile().mkdirs();

        pr = new PrintStream(outFile, "UTF-8");
        printer = pr;
        pr.println("<cia>");
        currentFileRecords = 0;
      }

      Collections.shuffle(tags);

      String clientId = rndClientId();
      if (rowType == RowType.EXISTS && commonClientIdList.size() > 0) {
        clientId = commonClientIdList.get(random.nextInt(commonClientIdList.size()));
      }

      if (rowType == RowType.NEW_FOR_PAIR) {
        commonClientIdList.add(clientId);
      }

      if (rowType == RowType.ERROR) {
        info.newErrorClient();
      } else {
        info.appendGoodClientId(clientId);
      }

      pr.println("  <client id=\"" + clientId + "\"> <!-- " + clientIndex + " -->");
      tags.forEach(pr::println);
      pr.println("  </client>");
    }
  }

  int ciaFileNo = 1;
  int frsFileNo = 1;

  final AtomicReference<List<String>> charmList = new AtomicReference<>(Collections.emptyList());

  {
    moreCharms(89);
  }

  @SuppressWarnings("SameParameterValue")
  void moreCharms(int count) {

    List<String> list = new ArrayList<>(charmList.get());

    for (int i = 0; i < count; i++) {
      list.add(RND.str(10));
    }

    charmList.set(Collections.unmodifiableList(list));
  }

  private String nextCharm() {
    List<String> list = charmList.get();
    return list.get(random.nextInt(list.size()));
  }

  private String nextPatronymic() {
    return RND.str(13);
  }

  private String nextName() {
    return RND.str(10);
  }

  private String nextSurname() {
    return RND.str(10);
  }

  private static List<String> addingTransactionType = new ArrayList<>();
  private static List<String> subtractingTransactionType = new ArrayList<>();

  static {
    addingTransactionType.add("Списывание с федерального бюджета");
    addingTransactionType.add("Списывание с регионального бюджета Алматинской области");
    addingTransactionType.add("Списывание с регионального бюджета г.Алматы");
    addingTransactionType.add("Списывание с регионального бюджета Карагандинского области");
    addingTransactionType.add("Списывание с регионального бюджета г.Караганда");
    subtractingTransactionType.add("Перевод в офшоры");
    subtractingTransactionType.add("Перевод на подставной счёт");
    subtractingTransactionType.add("Отмывание на ботинках");
    subtractingTransactionType.add("Отмывание на компьютерной технике");
  }

  private static String nextTransactionType(boolean adding) {
    List<String> list = adding ? addingTransactionType : subtractingTransactionType;
    return list.get(random.nextInt(list.size()));
  }


  static GregorianCalendar registeredAtCal = null;

  static GregorianCalendar finishedAtCal = null;

  static class Account {
    String clientId, number;
    Date registeredAt;

    final List<Transaction> transactionList = new ArrayList<>();

    double ceil;

    class Transaction {
      Date finishedAt;
      String type;
      BigDecimal money;

      String toJson() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        List<String> pairs = new ArrayList<>();
        pairs.add("'type':'transaction'");
        pairs.add("'money':'" + formatBD(money) + "'");
        pairs.add("'finished_at':'" + sdf.format(finishedAt) + "'");
        pairs.add("'transaction_type':'" + type + "'");
        pairs.add("'account_number':'" + number + "'");

        Collections.shuffle(pairs);

        return pairs.stream().collect(Collectors.joining(",", "{", "}")).replace('\'', '"');
      }

      public void normMoney() {
        money = money.setScale(2, RoundingMode.HALF_UP);
      }
    }

    static Account next(String clientId) {
      if (registeredAtCal == null) {
        registeredAtCal = new GregorianCalendar();
        registeredAtCal.add(Calendar.YEAR, -17);
      }

      registeredAtCal.add(Calendar.SECOND, +2);

      {
        Account ret = new Account();
        ret.registeredAt = registeredAtCal.getTime();
        ret.clientId = clientId;
        ret.number = rndAccountNumber();

        switch (random.nextInt(12)) {
          case 0:
            ret.ceil = 1000;
            break;
          case 1:
          case 2:
          case 3:
          case 4:
            ret.ceil = 10000;
            break;
          case 5:
          case 6:
          case 7:
            ret.ceil = 100000;
            break;
          case 8:
          case 9:
          case 10:
            ret.ceil = 1000000;
            break;
          case 11:
            ret.ceil = 10000000;
            break;
        }

        return ret;
      }
    }

    String toJson() {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

      List<String> pairs = new ArrayList<>();
      pairs.add("'type':'new_account'");
      pairs.add("'client_id':'" + clientId + "'");
      pairs.add("'account_number':'" + number + "'");
      pairs.add("'registered_at':'" + sdf.format(registeredAt) + "'");

      Collections.shuffle(pairs);

      return pairs.stream().collect(Collectors.joining(",", "{", "}")).replace('\'', '"');
    }

    BigDecimal rest() {
      BigDecimal ret = BigDecimal.ZERO;
      for (Transaction transaction : transactionList) {
        ret = ret.add(transaction.money);
      }
      return ret;
    }

    public Transaction addTransaction(int rowIndex, boolean last) {
      Transaction ret = new Transaction();

      if (finishedAtCal == null) {
        finishedAtCal = new GregorianCalendar();
        finishedAtCal.add(Calendar.YEAR, -7);
      }

      finishedAtCal.add(Calendar.SECOND, +1 + (rowIndex % 2));

      ret.finishedAt = finishedAtCal.getTime();

      BigDecimal rest = rest();

      if (last) {

        ret.money = new BigDecimal(random.nextInt(10) * 1000).subtract(rest);
        ret.normMoney();

      } else {

        switch (random.nextInt(3)) {
          case 0:
            if (rest.compareTo(BigDecimal.ZERO) > 0) {
              ret.money = rest.negate();
              ret.type = nextTransactionType(false);
              break;
            } //else goto case 1
          case 1:
            ret.money = new BigDecimal(123.45 + random.nextDouble() * ceil);
            ret.type = nextTransactionType(true);
            ret.normMoney();
            break;
          case 2:
            ret.money = new BigDecimal(random.nextDouble() * rest.doubleValue()).negate();
            ret.type = nextTransactionType(false);
            ret.normMoney();
            break;
        }

      }

      transactionList.add(ret);
      return ret;
    }

  }

  private void printAccountWithTransactions(int rowIndex) throws Exception {
    List<String> records = new ArrayList<>();

    String clientId = commonClientIdList.get(random.nextInt(commonClientIdList.size()));

    Account account = Account.next(clientId);

    records.add(account.toJson());
    info.newAccount();

    int count = 2 + random.nextInt(10);
    for (int i = 0; i < count; i++) {
      records.add(account.addTransaction(rowIndex, false).toJson());
      info.newTransaction();
    }
    records.add(account.addTransaction(rowIndex, true).toJson());
    info.newTransaction();

    Collections.shuffle(records);

    {
      PrintStream pr = printer;

      if (pr == null || clearPrinter.get()) {
        clearPrinter.set(false);

        finishPrinter(null, ".json_row.txt");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date now = new Date();

        outFileName = DIR + "/from_frs_" + sdf.format(now) + "-" + frsFileNo++;
        outFile = new java.io.File(outFileName + ".json_row.txt");
        outFile.getParentFile().mkdirs();

        pr = new PrintStream(outFile, "UTF-8");
        printer = pr;
        currentFileRecords = 0;
      }

      records.forEach(pr::println);
      currentFileRecords += records.size();
    }
  }

  private void archive() throws Exception {
    java.io.File[] files = new java.io.File(DIR).listFiles(file ->
                                                             file.getName().startsWith("from")
                                                               && !file.getName().endsWith(".bz2"));

    if (files == null) {
      return;
    }

    ConcurrentLinkedQueue<java.io.File> fileQueue = new ConcurrentLinkedQueue<>();
    Collections.addAll(fileQueue, files);

    List<Thread> threadList = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      threadList.add(new Thread(() -> archiveQueue(fileQueue)));
    }

    threadList.forEach(Thread::start);
    for (Thread thread : threadList) {
      thread.join();
    }
  }

  private void archiveQueue(ConcurrentLinkedQueue<java.io.File> fileQueue) {
    try {
      archiveQueueEx(fileQueue);
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException(e);
    }
  }

  private void archiveQueueEx(ConcurrentLinkedQueue<java.io.File> fileQueue) throws Exception {
    while (true) {
      java.io.File file = fileQueue.poll();
      if (file == null) {
        return;
      }
      archiveFile(file);
    }
  }

  private void archiveFile(java.io.File file) throws Exception {
    java.io.File dest = new java.io.File(file.getPath() + ".tar.bz2");
    System.out.println("Start archiving file " + file.getName()
                         + " with size " + file.length() + " -> " + dest.getName());

    ProcessBuilder builder = new ProcessBuilder();
    builder.command("tar", "-cvjSf", dest.getPath(), file.getPath());
    builder.inheritIO();
    Process process = builder.start();
    int exitStatus = process.waitFor();
    if (exitStatus != 0) {
      throw new RuntimeException("Error archiving file " + file + " with exit status " + exitStatus);
    }

    file.delete();
  }
}
