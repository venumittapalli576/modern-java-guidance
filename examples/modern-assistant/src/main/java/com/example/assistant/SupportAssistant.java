package com.example.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
class SupportAssistant {

    private final ChatClient chatClient;

    SupportAssistant(ChatClient.Builder builder, ChatMemory chatMemory, OrderTools orderTools) {
        this.chatClient = builder
                .defaultSystem("You answer customer questions about orders. Use the tools for facts about an order.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(orderTools)
                .build();
    }

    /** The conversation id is passed on every call, so two customers never share a history. */
    String answer(String conversationId, String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(advisors -> advisors.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
