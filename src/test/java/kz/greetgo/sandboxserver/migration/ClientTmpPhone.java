package kz.greetgo.sandboxserver.migration;

public class ClientTmpPhone {
    public int id;
    public String client_id;
    public String type;
    public String number;
    public String status;

    public ClientTmpPhone(int id, String client_id, String type, String number, String status) {
        this.id = id;
        this.client_id = client_id;
        this.type = type;
        this.number = number;
        this.status = status;
    }
}