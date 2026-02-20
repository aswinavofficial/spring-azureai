# Azure OpenAI + Spring AI Application

Spring Boot application integrating with **Azure OpenAI** for AI-powered text/image summarization, embedding, and semantic search using **Qdrant** vector store. Optionally supports **ColBERT** late interaction retrieval model.

## Tech Stack

| Component | Version |
|---|---|
| Spring Boot | 4.0.1 |
| Spring AI | 2.0.0-M2 |
| Java | 21+ |
| Vector Store | Qdrant |
| AI Provider | Azure OpenAI |

## Key Features

- **Dual Azure OpenAI deployments** — separate endpoints/keys for Chat (GPT-4o) and Embedding models
- **Text summarization** — summarize long texts using GPT-4o
- **Image summarization** — analyze images using GPT-4o vision (base64 upload, URL, or file upload)
- **Image → Embed pipeline** — summarize image to text, then embed and store in Qdrant
- **Similarity search** — find similar documents in Qdrant vector store
- **ColBERT late interaction** — toggleable per-token embedding with MaxSim scoring via Qdrant's native multi-vector support

## Quick Start

### 1. Prerequisites

- Java 21+
- Docker (for Qdrant)
- Azure OpenAI resource with Chat and Embedding deployments

```bash
# Start Qdrant
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

### 2. Configuration

Add your Azure OpenAI credentials to `set_cred.sh`:

```bash
# Chat deployment (GPT-4o)
export AZURE_CHAT_ENDPOINT=https://your-resource.openai.azure.com
export AZURE_CHAT_API_KEY=your-key
export AZURE_CHAT_DEPLOYMENT=gpt-4o

# Embedding deployment
export AZURE_EMBEDDING_ENDPOINT=https://your-resource.openai.azure.com
export AZURE_EMBEDDING_API_KEY=your-key
export AZURE_EMBEDDING_DEPLOYMENT=text-embedding-ada-002

# Optional: Enable ColBERT
export COLBERT_ENABLED=true
```

### 3. Run

```bash
source set_cred.sh && ./mvnw spring-boot:run
```

## Bruno API Collection

A complete [Bruno](https://www.usebruno.com/) collection is included in `bruno-collection/`. Open it in Bruno via **File → Open Collection** and select the `bruno-collection/` folder.

| Folder | Requests |
|---|---|
| Summarization | Summarize Text, Summarize Text Custom, Summarize Image URL, Summarize Image Base64 |
| Embedding | Embed Text, Embed and Store Text, Embed Image URL, Similarity Search |
| ColBERT | Encode Tokens, MaxSim Score, Store Document |

## API Endpoints

### Summarization

```bash
# Text summarization
curl -X POST http://localhost:8080/api/summarize/text \
  -H "Content-Type: application/json" \
  -d '{"text": "Your long text here..."}'

# Image summarization (URL)
curl -X POST http://localhost:8080/api/summarize/image \
  -H "Content-Type: application/json" \
  -d '{"imageUrl": "https://example.com/image.jpg"}'

# Image summarization (file upload)
curl -X POST http://localhost:8080/api/summarize/image/upload \
  -F "file=@/path/to/image.jpg"
```

### Embedding & Search

```bash
# Embed text and store in Qdrant
curl -X POST http://localhost:8080/api/embed/text \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello world", "store": true}'

# Embed image (summarize → embed → store)
curl -X POST http://localhost:8080/api/embed/image \
  -H "Content-Type: application/json" \
  -d '{"imageUrl": "https://example.com/image.jpg"}'

# Similarity search
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "What is AI?", "topK": 5}'
```

### ColBERT (when `COLBERT_ENABLED=true`)

```bash
# Encode tokens (multi-vector)
curl -X POST http://localhost:8080/api/colbert/encode \
  -H "Content-Type: application/json" \
  -d '{"text": "What is artificial intelligence?"}'

# MaxSim score
curl -X POST http://localhost:8080/api/colbert/score \
  -H "Content-Type: application/json" \
  -d '{"query": "What is AI?", "document": "Artificial intelligence is a branch of computer science..."}'

# Store document with multi-vector
curl -X POST http://localhost:8080/api/colbert/store \
  -H "Content-Type: application/json" \
  -d '{"text": "Artificial intelligence is a branch of computer science..."}'
```

## Project Structure

```
src/main/java/com/example/azopenai/
├── AzOpenAiApplication.java          # Entry point
├── config/
│   ├── AzureOpenAiConfig.java        # Dual Chat + Embedding bean config
│   └── AzureOpenAiProperties.java    # Type-safe properties
├── controller/
│   ├── SummarizationController.java  # /api/summarize/*
│   ├── EmbeddingController.java      # /api/embed/*, /api/search
│   └── ColbertController.java        # /api/colbert/* (conditional)
├── model/
│   ├── SummarizationRequest/Response
│   ├── EmbeddingRequest/Response
│   ├── SearchRequest/Response
│   └── ColbertRequest/Response
└── service/
    ├── TextSummarizationService.java
    ├── ImageSummarizationService.java
    ├── EmbeddingService.java
    └── ColbertService.java           # Conditional on app.colbert.enabled
```
