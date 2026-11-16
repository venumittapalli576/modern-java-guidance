---
id: langchain4j-streaming-handler
title: 'Stream chat responses with StreamingChatResponseHandler'
category: langchain4j
kind: removed
note: 'LangChain4j 1.0 streams chat through StreamingChatResponseHandler: onNext() became onPartialResponse() and onComplete(Response<AiMessage>) became onCompleteResponse(ChatResponse).'
source: https://docs.langchain4j.dev/tutorials/response-streaming
modern-since: 2025
detect:
  - 'code *.java /\bStreamingResponseHandler\s*<\s*AiMessage\s*>/'
---

The handler type for chat streaming changed along with the move from `generate` to `chat`.

## Before

```java
model.generate(prompt, new StreamingResponseHandler<AiMessage>() {

    @Override
    public void onNext(String token) {
        sink.next(token);
    }

    @Override
    public void onComplete(Response<AiMessage> response) {
        sink.complete();
    }

    @Override
    public void onError(Throwable error) {
        sink.error(error);
    }
});
```

## After

```java
model.chat(prompt, new StreamingChatResponseHandler() {

    @Override
    public void onPartialResponse(String partialResponse) {
        sink.next(partialResponse);
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        sink.complete();
    }

    @Override
    public void onError(Throwable error) {
        sink.error(error);
    }
});
```

## Notes

- The handler is `dev.langchain4j.model.chat.response.StreamingChatResponseHandler`, and `model` is a `StreamingChatModel`.
- With AI Services and `TokenStream`, the same renames apply: `onPartialResponse(...)` and `onCompleteResponse(...)`.
- `onPartialResponse` is not called when the model decides to call a tool. Handle that case in `onCompleteResponse`.
- `StreamingResponseHandler<String>` for the plain `StreamingLanguageModel` is a different, still existing type. This guide does not flag it.
