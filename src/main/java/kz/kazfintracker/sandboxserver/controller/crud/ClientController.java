package kz.kazfintracker.sandboxserver.controller.crud;

import kz.kazfintracker.sandboxserver.model.web.read.ClientToRead;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.kazfintracker.sandboxserver.register.ClientRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud")
@CrossOrigin("*")
public class ClientController {
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ClientRegister clientRegister;

    @GetMapping("/load")
    public ClientToRead load(@RequestParam("id") String id) {
        return clientRegister.load(id);
    }

    @PostMapping("/create")
    public String create(@RequestBody ClientToUpsert client) {
        return clientRegister.create(client);
    }

    @PostMapping("/update")
    public String update(@RequestBody ClientToUpsert client) {
        return clientRegister.update(client);
    }

    @PostMapping("/delete")
    public void delete(@RequestParam("id") String id) {
        clientRegister.delete(id);
    }
}
