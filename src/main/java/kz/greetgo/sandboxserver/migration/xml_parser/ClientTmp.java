package kz.greetgo.sandboxserver.migration.xml_parser;

import java.util.List;

public class ClientTmp {
    public int id;
    public String client_id;
    public String surname;
    public String name;
    public String patronymic;
    public String gender;
    public String charm;
    public String birth;
    public String factStreet;
    public String factHouse;
    public String factFlat;
    public String registerStreet;
    public String registerHouse;
    public String registerFlat;
    public String error;
    public String status;
    public String homePhone;
    List<String> mobilePhones;
    List<String> workPhones;

    public ClientTmp() {
    }

    public ClientTmp(int id, String client_id, String surname, String name, String patronymic, String gender, String charm, String birth, String factStreet, String factHouse, String factFlat, String registerStreet, String registerHouse, String registerFlat, String error, String status) {
        this.id = id;
        this.client_id = client_id;
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.gender = gender;
        this.charm = charm;
        this.birth = birth;
        this.factStreet = factStreet;
        this.factHouse = factHouse;
        this.factFlat = factFlat;
        this.registerStreet = registerStreet;
        this.registerHouse = registerHouse;
        this.registerFlat = registerFlat;
        this.error = error;
        this.status = status;
    }
}
