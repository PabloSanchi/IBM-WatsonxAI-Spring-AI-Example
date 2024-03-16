package com.watsonx.ai;


import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.watsonx.WatsonxChatClient;
import org.springframework.ai.watsonx.api.WatsonxAIOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class WatsonxController {

    private final WatsonxChatClient chat;

    @Autowired
    public WatsonxController(WatsonxChatClient chat) {
        this.chat = chat;
    }

    @GetMapping("/hi")
    public String sayHi() {
        var options = WatsonxAIOptions.create().withRandomSeed(1).withModel("google/flan-ul2").withDecodingMethod("sample");
        var prompt = new Prompt(new SystemMessage("say hi"), options);
        var results = this.chat.call(prompt);
        return results.getResult().getOutput().getContent();
    }

    @GetMapping(path = "/hi/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> sayHiStream() {
        var options = WatsonxAIOptions.create().withRandomSeed(1).withModel("meta-llama/llama-2-70b-chat").withDecodingMethod("greedy").withMinNewTokens(100).withMaxNewTokens(200);
        var prompt = new Prompt(new SystemMessage("say hi"), options);
        var results = this.chat.stream(prompt);
        SseEmitter emitter = new SseEmitter();

        results.subscribe(
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

        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }
}
