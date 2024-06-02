package kz.kazfintracker.sandboxserver.controller;

import kz.kazfintracker.sandboxserver.impl.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/api")
@CrossOrigin("*")
public class ChatController {
  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping("/chat")
  public ResponseEntity<String> chat(@RequestBody String message) {
//    if(message.contains("report")){
//      generateReportFileAndSendItToEmail();
//      return "I have sent to you report email!";
//    }

    String response = chatService.chat(message);
    return ResponseEntity.ok().body("{\"response\": \"" + response + "\"}");
  }

  @PostMapping("/chat/stream")
  public Flux<String> chatStream(@RequestBody String message) {
    return chatService.chatStream(message);
  }

}
