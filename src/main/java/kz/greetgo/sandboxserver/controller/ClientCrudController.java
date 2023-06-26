package kz.greetgo.sandboxserver.controller;

import kz.greetgo.sandboxserver.model.web.read.ClientToRead;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.register.ClientRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud")
@CrossOrigin("*")
public class ClientCrudController {
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
    public void update(@RequestBody ClientToUpsert client) {
        clientRegister.update(client);
    }

    @PostMapping("/delete")
    public void delete(@RequestParam("id") String id) {
        clientRegister.delete(id);
    }
}
