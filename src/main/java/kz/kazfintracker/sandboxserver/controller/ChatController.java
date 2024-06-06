package kz.kazfintracker.sandboxserver.controller;

import kz.kazfintracker.sandboxserver.impl.ChatService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.security.Principal;

@RestController
@RequestMapping("/v1/api")
@CrossOrigin("*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message, Principal connectedUser) {
        if(message.contains("email") || message.contains("mail")) {
            message += " (Current user's email: " + connectedUser.getName();
        }
        return chatService.chat(message);
    }

    @PostMapping("/chat/stream")
    public Flux<String> chatStream(@RequestBody String message) {
        return chatService.chatStream(message);
    }

}
