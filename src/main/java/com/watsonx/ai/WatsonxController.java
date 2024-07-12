package com.watsonx.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.watsonx.WatsonxAiChatModel;
import org.springframework.ai.watsonx.WatsonxAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class WatsonxController {

    private final WatsonxAiChatModel chat;
    private final WatsonxAiEmbeddingModel embedding;

    private final String stringTemplate = """
        <|system|>
        You are Granite Chat, an AI language model developed by IBM.
        You are a cautious assistant. You carefully follow instructions.
        You are helpful and harmless and you follow ethical guidelines and promote positive behavior.
        <|user|>
        {input}
        <|assistant|>
        
        """.stripIndent();

    private final PromptTemplate template = new PromptTemplate(stringTemplate);

    @Autowired
    public WatsonxController(WatsonxAiChatModel chat, WatsonxAiEmbeddingModel embedding) {
        this.chat = chat;
        this.embedding = embedding;
    }

    @GetMapping("/text")
    @Operation(summary = "Get chat response", description = "Get a response from the Watsonx AI chat model based on input text")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response", content = @Content(mediaType = "text/plain; charset=utf-8")),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<String> chat(@RequestParam String input) {
        Prompt prompt = this.template.create(Map.of("input", input));
        ChatResponse genAiResponse = this.chat.call(prompt);
        return ResponseEntity.ok(genAiResponse.getResult().getOutput().getContent());
    }

    @GetMapping(path = "/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream chat response", description = "Stream a response from the Watsonx AI chat model based on input text")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response", content = @Content(mediaType = "text/event-stream")),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<SseEmitter> chatStream(@RequestParam String input) {
        Prompt prompt = this.template.create(Map.of("input", input));
        Flux<ChatResponse> genAiStreaming = this.chat.stream(prompt);

        SseEmitter emitter = new SseEmitter();

        genAiStreaming.subscribe(
                data -> {
                    try {
                        emitter.send(data.getResult().getOutput().getContent(), MediaType.TEXT_PLAIN);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );

        return ResponseEntity.ok(emitter);
    }

    @GetMapping("/embedding")
    @Operation(summary = "Get text embedding", description = "Get the embedding of the input text using the Watsonx AI embedding model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Embedding.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Embedding> embedding(@RequestParam String text) {
        EmbeddingResponse response = this.embedding.embedForResponse(List.of(text));
        return ResponseEntity.ok(response.getResult());
    }
}
