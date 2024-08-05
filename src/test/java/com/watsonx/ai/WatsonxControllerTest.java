package com.watsonx.ai;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watsonx.ai.dto.ChatAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.watsonx.WatsonxAiChatModel;
import org.springframework.ai.watsonx.WatsonxAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(WatsonxController.class)
@ActiveProfiles("dev")
public class WatsonxControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WatsonxAiChatModel chat;

	@MockBean
	private WatsonxAiEmbeddingModel embedding;

	@InjectMocks
	private WatsonxController watsonxController;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testChat() throws Exception {
		String input = "Hello, how are you?";
		String expectedResponse = "I'm fine, thank you!";
		ChatResponse mockResponse = new ChatResponse(List.of(new Generation(expectedResponse)));

		when(chat.call(any(Prompt.class))).thenReturn(mockResponse);

		mockMvc.perform(get("/api/v1/text").param("question", input).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(new ChatAnswer(expectedResponse))));
	}

	@Test
	public void testChatStream() throws Exception {
		String input = "Stream test";
		String outputText = "Streaming response!";
		ChatAnswer expectedResponse = ChatAnswer.of(outputText);
		ChatResponse mockResponse = new ChatResponse(List.of(new Generation(outputText)));

		when(chat.stream(any(Prompt.class))).thenReturn(Flux.just(mockResponse));

		MvcResult result = mockMvc
			.perform(get("/api/v1/text/stream").param("question", input).accept(MediaType.APPLICATION_NDJSON))
			.andExpect(status().isOk())
			.andReturn();

		ChatAnswer response = objectMapper.readValue(result.getResponse().getContentAsString(), ChatAnswer.class);
		assertThat(response.answer()).isEqualTo(expectedResponse.answer());
	}

	@Test
	public void testEmbedding() throws Exception {
		String text = "This is a test text";
		Embedding mockEmbedding = new Embedding(List.of(0.1, 0.2, 0.3), 0);
		EmbeddingResponse mockResponse = new EmbeddingResponse(List.of(mockEmbedding));

		when(embedding.embedForResponse(any(List.class))).thenReturn(mockResponse);

		MvcResult result = mockMvc
			.perform(get("/api/v1/embedding").param("text", text).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		String content = result.getResponse().getContentAsString();
		Map<String, Object> responseEmbedding = objectMapper.readValue(content, Map.class);
		assertThat(responseEmbedding.get("output")).isEqualTo(mockEmbedding.getOutput());
	}

}
