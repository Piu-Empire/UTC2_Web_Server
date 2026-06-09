package com.utc2.appreborn.backend.modules.aichat.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class SemanticSearchService {

    private EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private boolean isInitialized = false;

    public SemanticSearchService() {
        this.embeddingStore = new InMemoryEmbeddingStore<>();
    }

    private synchronized void ensureInitialized() {
        if (isInitialized) {
            return;
        }
        
        log.info("Khởi tạo SemanticSearchService: Load model AI và dữ liệu vào In-Memory Vector Store...");
        try {
            this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
            
            ObjectMapper mapper = new ObjectMapper();
            Resource catalogResource = new org.springframework.core.io.ClassPathResource("documents/doc_catalog.json");
            DocumentParser parser = new ApacheTikaDocumentParser();

            if (catalogResource.exists()) {
                JsonNode rootNode = mapper.readTree(catalogResource.getInputStream());
                JsonNode catalog = rootNode.get("catalog");
                if (catalog != null && catalog.isArray()) {
                    for (JsonNode node : catalog) {
                        String path = node.get("path").asText();
                        String topic = node.get("topic").asText();
                        
                        Resource docResource = new org.springframework.core.io.ClassPathResource("documents/" + path);
                        if (docResource.exists() && docResource.isReadable()) {
                            log.info("Đang xử lý tài liệu từ Catalog: {}", path);
                            try (InputStream is = docResource.getInputStream()) {
                                Document document = parser.parse(is);
                                document.metadata().put("source", path);
                                document.metadata().put("title", topic);

                                List<TextSegment> segments = DocumentSplitters.recursive(500, 50).split(document);
                                for (TextSegment segment : segments) {
                                    embeddingStore.add(embeddingModel.embed(segment).content(), segment);
                                }
                            } catch (Exception e) {
                                log.error("Lỗi khi parse tài liệu {}: {}", path, e.getMessage());
                            }
                        } else {
                            log.warn("Tài liệu không tồn tại hoặc không thể đọc: {}", path);
                        }
                    }
                }
            } else {
                log.warn("Không tìm thấy doc_catalog.json trong thư mục documents.");
            }
            isInitialized = true;
            log.info("Hoàn tất load model và tài liệu vào Vector Store.");
        } catch (Exception e) {
            log.error("Lỗi khởi tạo SemanticSearchService: ", e);
            throw new RuntimeException("Lỗi lazy init SemanticSearchService", e);
        }
    }

    public List<EmbeddingMatch<TextSegment>> search(String query, double minScore) {
        ensureInitialized();
        return embeddingStore.findRelevant(embeddingModel.embed(query).content(), 3, minScore);
    }
}
