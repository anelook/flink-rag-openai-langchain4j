package org.example;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.opensearch.client.opensearch.OpenSearchClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class RAG {
    public static String getAnswer(String question) {
        // Embed segments (convert them into vectors that represent the meaning) using embedding model
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.withApiKey(Variables.OPENAI_API_KEY);

        // Create OpenSearch connection
        OpenSearchConnection openSearchConnection = new OpenSearchConnection();

        // Get the OpenSearchClient from the connection class
        OpenSearchClient client = openSearchConnection.createClient();
        EmbeddingStore<TextSegment> embeddingStore = OpenSearchEmbeddingStore.builder()
                .openSearchClient(client)
                .build();

        // Embed the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // Find relevant embeddings in embedding store by semantic similarity
        // You can play with parameters below to find a sweet spot for your specific use case
        int maxResults = 3;
        double minScore = 0.7;
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings
                = embeddingStore.findRelevant(questionEmbedding, maxResults, minScore);


        // Create a prompt for the model that includes question and relevant embeddings
        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability:\n"
                        + "\n"
                        + "Question:\n"
                        + "{{question}}\n"
                        + "\n"
                        + "Base your answer on the following information:\n"
                        + "{{information}}");

        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().text())
                .collect(joining("\n\n"));

        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);

        // Send the prompt to the OpenAI chat model
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(Variables.OPENAI_API_KEY)
                .timeout(Duration.ofSeconds(60))
                .build();
        AiMessage aiMessage = chatModel.generate(prompt.toUserMessage()).content();

        // See an answer from the model
        return aiMessage.text();
    }
}
