package org.example;


import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.opensearch.client.opensearch.OpenSearchClient;

import java.util.ArrayList;
import java.util.List;

public class StoreKnowledge {

    public static void main(String[] args) {
        // Prep embedding model
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.withApiKey(Variables.OPENAI_API_KEY);

        // Prep vector store / embedding store
        OpenSearchConnection openSearchConnection = new OpenSearchConnection();
        OpenSearchClient client = openSearchConnection.createClient();
        EmbeddingStore<TextSegment> embeddingStore = OpenSearchEmbeddingStore.builder()
                .openSearchClient(client)
                .build();
        // Store embeddings into vector store for further search / retrieval
        BlipBlopFeatures blipBlopFeatures = new BlipBlopFeatures("src/main/resources/features.json");
        List<TextSegment> segments = new ArrayList<>();
        for (String feature : blipBlopFeatures.getFeatures()) {
            segments.add(TextSegment.from(feature));
        }
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);

        System.out.println("Data successfully stored");
    }
}
