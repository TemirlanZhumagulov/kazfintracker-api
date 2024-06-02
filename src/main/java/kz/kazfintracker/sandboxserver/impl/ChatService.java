package kz.kazfintracker.sandboxserver.impl;

import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;

@Service
public class ChatService {

  @Value("${openai.api.key}")
  private String OPENAI_API_KEY;
  private Assistant assistant;
  private StreamingAssistant streamingAssistant;


  interface Assistant {
    String chat(String message);
  }

  interface StreamingAssistant {
    TokenStream chat(String message);
  }

  @PostConstruct
  public void init() {

    if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
      throw new RuntimeException("ERROR: OPENAI_API_KEY environment variable is not set. Please set it to your OpenAI API key.");
    }

    var memory = TokenWindowChatMemory.withMaxTokens(2000, new OpenAiTokenizer("gpt-3.5-turbo"));

    assistant = AiServices.builder(Assistant.class)
      .chatLanguageModel(OpenAiChatModel.withApiKey(OPENAI_API_KEY))
      .chatMemory(memory)
//      .client(client)
      .build();

    streamingAssistant = AiServices.builder(StreamingAssistant.class)
      .streamingChatLanguageModel(OpenAiStreamingChatModel.withApiKey(OPENAI_API_KEY))
      .chatMemory(memory)
//      .client(client)
      .build();

  }
//
//  public static void main(String[] args) throws InterruptedException {
//
//    EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
////      .indexName("client")
//      .serverUrl("http://localhost:12216")
//      .dimension(384)
//      .build();
//
//    EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
//
//    TextSegment segment1 = TextSegment.from("I like football.");
//    Embedding embedding1 = embeddingModel.embed(segment1).content();
//    embeddingStore.add(embedding1, segment1);
//
//    TextSegment segment2 = TextSegment.from("The weather is good today.");
//    Embedding embedding2 = embeddingModel.embed(segment2).content();
//    embeddingStore.add(embedding2, segment2);
//
//    Thread.sleep(1000); // to be sure that embeddings were persisted
//
//    Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
//    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
//    EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);
//
//    System.out.println(embeddingMatch.score()); // 0.81442887
//    System.out.println(embeddingMatch.embedded().text()); // I like football.
//
//  }
//

  public String chat(String message) {
    return assistant.chat(message);
  }

  public Flux<String> chatStream(String message) {
    Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

    streamingAssistant.chat(message)
      .onNext(sink::tryEmitNext)
      .onComplete(c -> sink.tryEmitComplete())
      .onError(sink::tryEmitError)
      .start();

    return sink.asFlux();
  }
}
