package org.example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class AiJob {
    public static void main(String[] args) throws Exception {
        // Initialize Flink environment
        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        // Define a data stream of questions
        DataStream<String> questions = env.fromElements(
                "What do I do if it is dark?",
                "How can I clean my bathroom?",
                "How to make a cup of cappuccino?"
        );

        // Process each question in the stream and get an answer using the RAG module
        DataStream<String> answers = questions.map(RAG::getAnswer);

        // Print out the answers
        answers.print();

        // Execute the Flink job
        env.execute();
    }
}