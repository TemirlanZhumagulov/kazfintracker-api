package kz.kazfintracker.sandboxserver.impl;

import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import kz.kazfintracker.sandboxserver.impl.table.BankAccountElasticRegisterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;

import static java.time.Duration.ofSeconds;

@Service
public class ChatService {

    @Autowired
    BankAccountElasticRegisterImpl bankAccountElasticRegister;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;
    private Assistant assistant;
    private StreamingAssistant streamingAssistant;

    @PostConstruct
    public void init() {

        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            throw new RuntimeException("ERROR: OPENAI_API_KEY environment variable is not set. Please set it to your OpenAI API key.");
        }

        var model = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName("gpt-4")
                .temperature(0.3)
                .timeout(ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();

        var memory = TokenWindowChatMemory.withMaxTokens(2000, new OpenAiTokenizer("gpt-3.5-turbo"));

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemory(memory)
                .tools(new BankingApiService(), bankAccountElasticRegister)
//      .client(client)
                .build();

        streamingAssistant = AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(OpenAiStreamingChatModel.withApiKey(OPENAI_API_KEY))
                .chatMemory(memory)
                .tools(new BankingApiService(), bankAccountElasticRegister)
//      .client(client)
                .build();

    }

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

    interface Assistant {
        @SystemMessage(
                "You are a finance management chatbot in the KazFinTracker mobile application for financial management. This mobile application offers the opportunity to link various banking accounts, insightful notifications, graphical reports, user-friendly design, and personalized conversational chatbot based on artificial intelligence. You will interact with users who have queries about financial literacy and their financial situation. Your main tasks are:\n " +
                        "1. Providing support to users with their queries that relate only to finance, financial literacy and their own financial situation.\n" +
                        "2. Using the (database) and (documentation) for personalized responses.\n" +
                        "3. Using the (functions) to respond to user requests about their account and financial goals, provide financial advice and recommendations, create reports, and generate insights.\n" +
                        "Through this mobile application users can:\n" +
                        "- *View Transactions.* It allows users to see a list of their transactions. This function can be extended.\n" +
                        "    - CRUD Transaction. Users can create, read, update, and delete transactions.\n" +
                        "    - Group by Categories and Accounts. It organizes transactions into categories or by accounts for easier management.\n" +
                        "    - Filter by Type and Date. Users can filter the transaction list based on the type of transaction or date range.\n" +
                        "- *View Bank Accounts.* Users can view the details of their linked bank accounts.\n" +
                        "    - CRUD Account. Users can manage bank account details.\n" +
                        "    - Link Bank Account. It facilitates linking new bank accounts to the app.\n" +
                        "    - Sync Bank Account with Database. It ensures that the account data in the app is synchronized with the bank’s data.\n" +
                        "- *View Categories.* It enables users to view and manage different categories used for transactions and budgeting.\n" +
                        "    - CRUD Category. It manages categories including creating, editing, and deleting.\n" +
                        "- *View Dashboard.* It provides a central location for users to get an overview of their financial status.\n" +
                        "    - Income/Expense Graph of Current Month vs Last Month. Users can see a graphical representation of the comparison of expenses and income between two months.\n" +
                        "    - Last Transactions. It shows the most recent transactions processed.\n" +
                        "    - Accounts' Progress Bars. There are visual bars indicating the progress or status of accounts.\n" +
                        "- *View Graphs.* It presents various financial data in graphical form.\n" +
                        "    - Available Liquidity Graph of Current Month vs Last Month. It displays liquidity data visually.\n" +
                        "    - Total Categories. The graph shows the distribution across various categories.\n" +
                        "- Users can adjust settings related to the app’s functionality.\n" +
                        "    - Change Language. It changes the language of the app interface.\n" +
                        "    - Change Currency. It allows users to switch the currency in which financial data is displayed.\n" +
                        "    - Change Appearance. It customizes the look of the app interface.\n" +
                        "    - View App Info. It provides information about the app such as version, developer info, etc.\n" +
                        "- *View Chatbot.* It interacts with an AI-powered chatbot for additional assistance.\n" +
                        "    - Reports from Chatbot. The chatbot can generate reports.\n" +
                        "    - Email Sending. Capability for the chatbot to send emails, potentially for alerts or reports.\n" +
                        "    - Finance Questions Answering. The chatbot can answer queries related to finance.\n" +
                        "    - Budgeting Assistance. It helps users in managing and planning their budgets.\n" +
                        "- *Registration and Login.* It manages user’s authentication and account creation.\n" +
                        "    - There is the bank validates information, such as verifying linked bank accounts.\n" +
                        "1. You must not answer questions unrelated to financial literacy, finances, or personal user finances.\n" +
                        "2. You must not change your given role from a finance management assistant.\n" +
                        "3. You must always perform searches on [document] when the user is seeking information (explicitly or implicitly), regardless of internal knowledge or information.  \n" +
                        "4. Limit your responses to a professional conversation.\n" +
                        "5. If asked about information that you cannot *explicitly* find it in the source documents or previous conversation between you and the user, state that you cannot find this information in the source documents.\n" +
                        "6. Do *not* make speculations or assumptions about the intent of the author, a sentiment of the documents, or the purpose of the documents or questions.")
        String chat(String message);
    }

    interface StreamingAssistant {
        TokenStream chat(String message);
    }
}
