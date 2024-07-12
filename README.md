# Watsonx AI Integration
## Overview

This project demonstrates how to integrate IBM's Watsonx AI models into a Spring Boot application. The application provides endpoints for generating chat responses and text embeddings using the Watsonx AI chat and embedding models. As a contributor to the Spring AI module, I have implemented these integrations to enable seamless interactions with Watsonx AI.

## Features
- **Chat API**: Get responses from the Watsonx AI chat model based on input text.
- **Streaming Chat API**: Stream responses from the Watsonx AI chat model in real-time.
- **Embedding API**: Get text embeddings using the Watsonx AI embedding model.


## Getting Started
1. **Clone the Repository**
```sh
git clone https://github.com/PabloSanchi/IBM-WatsonxAI-Spring-AI-Example
```
2. **Change Directory**
```sh
cd IBM-WatsonxAI-Spring-AI-Example
```
3. **Configure Watsonx AI**: Ensure you have your Watsonx AI credentials set up in your application properties or environment variables.
<br/><br/>Example shcema:
```properties
spring:
  ai:
    watsonx:
      ai:
        base-url: ${WATSONX_BASE_URL}
        stream-endpoint: ${WATSONX_STREAM_ENDPOINT}
        text-endpoint: ${WATSONX_TEXT_ENDPOINT}
        embedding-endpoint: ${WATSONX_EMBEDDING_ENDPOINT}
        project-id: ${WATSONX_PROJECT_ID}
        iam-token: ${WATSONX_IAM_TOKEN}

        chat:
          options:
            model: ${WATSONX_CHAT_MODEL}

        embedding:
          options:
            model: ${WATSONX_EMBEDDING_MODEL}

springdoc:
  swagger-ui:
    path: /swagger.html
```
5. **Run the APP**
6. **Go to the Swagger UI**: test the endpoints (http://localhost:8080/swagger.html/)