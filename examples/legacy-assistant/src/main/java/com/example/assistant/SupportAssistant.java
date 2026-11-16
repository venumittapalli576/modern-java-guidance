package com.example.assistant;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class SupportAssistant {

    private final ChatClient chatClient;

    public SupportAssistant(ChatClient.Builder builder, ChatMemory chatMemory, OrderLookup orderLookup) {
        this.chatClient = builder
                .defaultOptions(OpenAiChatOptions.builder().model("gpt-4").build())
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
                .defaultFunctions(FunctionCallback.builder()
                        .function("orderStatus", orderLookup)
                        .description("Look up the status of an order")
                        .inputType(OrderLookup.Request.class)
                        .build())
                .build();
    }

    public String answer(String sessionId, String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
    }
}
