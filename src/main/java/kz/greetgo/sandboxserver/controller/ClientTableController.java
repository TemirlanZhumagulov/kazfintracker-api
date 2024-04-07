package kz.greetgo.sandboxserver.controller;

import kz.greetgo.sandboxserver.elastic.model.ClientResponse;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.web.ClientsTableRequest;
import kz.greetgo.sandboxserver.register.ClientElasticRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/table")
@CrossOrigin("*")
public class ClientTableController {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ClientElasticRegister clientElasticRegister;

    @GetMapping("/all")
    public ClientResponse loadAll(
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        return clientElasticRegister.loadAll(Paging.of(offset, limit ));
    }

    @PostMapping("/filtered")
    public ClientResponse load(@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                               @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                               @RequestBody ClientsTableRequest tableRequest) {
        return clientElasticRegister.load(tableRequest, Paging.of(offset, limit));
    }
}
