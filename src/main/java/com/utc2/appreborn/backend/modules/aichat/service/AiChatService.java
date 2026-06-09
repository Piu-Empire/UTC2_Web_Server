package com.utc2.appreborn.backend.modules.aichat.service;

import com.utc2.appreborn.backend.modules.aichat.dto.ChatRequest;
import com.utc2.appreborn.backend.modules.aichat.dto.ChatResponse;
import com.utc2.appreborn.backend.modules.aichat.dto.ChatMessageDto;
import com.utc2.appreborn.backend.modules.aichat.dto.ActionButtonDto;
import com.utc2.appreborn.backend.modules.aichat.entity.ChatActionLog;
import com.utc2.appreborn.backend.modules.aichat.repository.ChatActionLogRepository;
import com.utc2.appreborn.backend.modules.aichat.repository.ChatKnowledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.utc2.appreborn.backend.modules.academic.dto.CourseGradeDto;
import com.utc2.appreborn.backend.modules.academic.dto.SemesterDto;

import com.utc2.appreborn.backend.modules.academic.service.AcademicService;
import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import com.utc2.appreborn.backend.modules.auth.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatKnowledgeRepository knowledgeRepository;
    private final GroqAiService groqAiService;
    private final ChatActionLogRepository chatActionLogRepository;
    private final AcademicService academicService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES)
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_TRAILING_COMMA);

    // ── Catalog entry (loaded once at startup) ──────────────────────────────
    private static class DocEntry {
        String id, path, topic, description;
        String[] topicTokens; // lowercase tokens split from topic for fast matching
    }

    private final List<DocEntry> catalog = new ArrayList<>();
    private String catalogJsonString = "";

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("documents/doc_catalog.json");
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                catalogJsonString = FileCopyUtils.copyToString(reader);
            }
            parseCatalog();
        } catch (Exception e) {
            System.err.println("Failed to load doc_catalog.json: " + e.getMessage());
        }
    }

    /** Parse doc_catalog.json into DocEntry list for keyword matching. */
    private void parseCatalog() {
        try {
            JsonNode root = objectMapper.readTree(catalogJsonString);
            JsonNode items = root.get("catalog");
            if (items == null || !items.isArray())
                return;
            for (JsonNode node : items) {
                DocEntry entry = new DocEntry();
                entry.id = node.path("id").asText("");
                entry.path = node.path("path").asText("");
                entry.topic = node.path("topic").asText("");
                entry.description = node.path("description").asText("");
                // Split topic by ", " or "," into lowercase tokens
                entry.topicTokens = entry.topic.toLowerCase().split(",\\s*|,");
                catalog.add(entry);
            }
            System.out.println("Loaded " + catalog.size() + " catalog entries.");
        } catch (Exception e) {
            System.err.println("Failed to parse catalog: " + e.getMessage());
        }
    }

    // ── Message type enum ────────────────────────────────────────────────────
    public enum MessageType {
        TYPE_ACTION, TYPE_MATH, TYPE_NAVIGATION, TYPE_PERSONAL_DATA, TYPE_KNOWLEDGE
    }

    // ── Entry point ──────────────────────────────────────────────────────────
    public ChatResponse processMessage(ChatRequest request) {
        MessageType type = classifyMessage(request);
        switch (type) {
            case TYPE_ACTION:
                return handleDirectAction(request);
            case TYPE_MATH:
                return handleMath(request.getMessage());
            case TYPE_NAVIGATION:
                return handleNavigation(request);
            case TYPE_PERSONAL_DATA:
                return handlePersonalDataRequest(request);
            case TYPE_KNOWLEDGE:
            default:
                return handleKnowledge(request);
        }
    }

    // ── Classifier (unchanged) ───────────────────────────────────────────────
    private MessageType classifyMessage(ChatRequest request) {
        if (request.getAction() != null && !request.getAction().trim().isEmpty()) {
            return MessageType.TYPE_ACTION;
        }
        String msg = request.getMessage() != null ? request.getMessage().trim() : "";
        if (msg.isEmpty())
            return MessageType.TYPE_KNOWLEDGE;

        Pattern mathPattern = Pattern.compile(
                "^\\s*([\\d]+(?:\\.[\\d]+)?)\\s*([+\\-*/])\\s*([\\d]+(?:\\.[\\d]+)?)\\s*$");
        if (mathPattern.matcher(msg).matches())
            return MessageType.TYPE_MATH;

        String lowerMsg = normalise(msg);

        if (lowerMsg.matches(
                ".*(tinh diem|diem cua toi|diem tich luy|cong no|gpa|qua mon|rot mon|hoc luc|lich thi|lich hoc).*")) {
            return MessageType.TYPE_PERSONAL_DATA;
        }

        if (lowerMsg.contains("mo ") || lowerMsg.contains("vao ")
                || lowerMsg.contains("chuyen den") || lowerMsg.contains("xem man hinh")
                || lowerMsg.contains("den trang") || lowerMsg.contains("trang ")) {
            return MessageType.TYPE_NAVIGATION;
        }

        return MessageType.TYPE_KNOWLEDGE;
    }

    // ── Personal Data Handling ───────────────────────────────────────────────
    private ChatResponse handlePersonalDataRequest(ChatRequest request) {
        String msg = request.getMessage().trim();
        String dataType = "grades";
        if (normalise(msg).contains("lich hoc") || normalise(msg).contains("lich thi")) {
            dataType = "schedule";
        }

        String payload = "{\"q\":\"" + msg.replace("\"", "\\\"") + "\",\"type\":\"" + dataType + "\"}";

        return ChatResponse.builder()
                .type("permission_required")
                .message(
                        "Để trả lời câu hỏi này chính xác, tôi cần truy cập vào dữ liệu học tập/cá nhân của bạn. Bạn có đồng ý cho phép tôi làm điều này không?")
                .actionButtons(List.of(
                        ActionButtonDto.builder().type("GRANT_PERMISSION").label("Cho phép").data(payload).build(),
                        ActionButtonDto.builder().type("DENY_PERMISSION").label("Từ chối").data("").build()))
                .build();
    }

    private ChatResponse handleGrantedPersonalData(ChatRequest request) {
        String payloadStr = request.getActionId() != null ? request.getActionId() : request.getMessage();
        String question = "";
        String dataType = "grades";
        try {
            JsonNode payload = objectMapper.readTree(payloadStr);
            question = payload.path("q").asText("");
            dataType = payload.path("type").asText("grades");
        } catch (Exception e) {
            question = payloadStr;
        }

        // Sanitize question against prompt injection
        question = question.replaceAll("(?i)(ignore|bỏ qua|system prompt|lệnh mới|quên đi|prompt)", "");

        String gpaRules = "";
        if ("grades".equals(dataType)) {
            try {
                org.springframework.core.io.ClassPathResource docResource = new org.springframework.core.io.ClassPathResource(
                        "documents/markdown/chien_luoc_gpa.md");
                try (java.io.InputStreamReader reader = new java.io.InputStreamReader(docResource.getInputStream(),
                        java.nio.charset.StandardCharsets.UTF_8)) {
                    gpaRules = org.springframework.util.FileCopyUtils.copyToString(reader) + "\n\n";
                }
            } catch (Exception e) {
                System.err.println("Could not load chien_luoc_gpa.md: " + e.getMessage());
            }
        }

        // Get user from SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElse(null);
        if (user == null) {
            return ChatResponse.builder().type("answer")
                    .content("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.").build();
        }

        Long userId = user.getId();
        String jsonData = "[]";
        try {
            if ("grades".equals(dataType)) {
                List<CourseGradeDto> grades = academicService.getGrades(userId, null);
                // Lọc dữ liệu nhạy cảm/không cần thiết, chỉ giữ lại môn, tín chỉ, điểm tổng
                // kết, kết quả
                List<Map<String, Object>> slimGrades = new ArrayList<>();
                for (CourseGradeDto g : grades) {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("courseName", g.getCourseName());
                    map.put("credits", g.getCredits());
                    map.put("totalScore", g.getTotalScore());
                    map.put("isPassed", g.getIsPassed());
                    slimGrades.add(map);
                }
                jsonData = objectMapper.writeValueAsString(slimGrades);
            } else {
                List<SemesterDto> semesters = academicService.getSemesters(userId);
                // Filter schedule
                List<Map<String, Object>> slimSems = new ArrayList<>();
                for (SemesterDto s : semesters) {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("semesterName", s.getSemesterName());
                    map.put("academicYear", s.getAcademicYear());
                    map.put("startDate", s.getStartDate());
                    map.put("endDate", s.getEndDate());
                    slimSems.add(map);
                }
                jsonData = objectMapper.writeValueAsString(slimSems);
            }
        } catch (Exception e) {
            System.err.println("Error fetching user data: " + e.getMessage());
            return ChatResponse.builder().type("answer").content("Lỗi khi truy xuất dữ liệu cá nhân của bạn.").build();
        }

        String prompt = "Bạn là trợ lý học tập của trường ĐH GTVT Phân hiệu tại TP.HCM (UTC2).\n" +
                "Dưới đây là dữ liệu học tập thật của sinh viên (đã được cấp quyền truy cập, dạng JSON):\n" +
                jsonData + "\n\n" +
                "Hãy dựa vào dữ liệu trên để trả lời câu hỏi sau của sinh viên:\n" +
                "CÂU HỎI: \"" + question + "\"\n\n" +
                "LƯU Ý QUAN TRỌNG: TUYỆT ĐỐI KHÔNG xuất lại chuỗi JSON thô (raw data) vào trong câu trả lời. Hãy phân tích, tính toán và diễn đạt kết quả bằng ngôn ngữ tự nhiên. Tuyệt đối không dùng ký tự ngoặc kép (\") bên trong nội dung câu trả lời để tránh gây lỗi định dạng JSON.\n\n"
                +
                "YÊU CẦU ĐẦU RA (JSON hợp lệ, luôn dùng ngoặc kép bao ngoài các trường):\n" +
                "{\n" +
                "  \"answer\": \"câu trả lời tự nhiên, giải thích chi tiết quá trình tính toán\",\n" +
                "  \"suggestions\": [\"Câu hỏi tiếp theo\"]\n" +
                "}";

        try {
            String aiReply = groqAiService.chatWithFallback(request.getConversation(), prompt, question);
            return buildDynamicResponse(aiReply, "ai_personal", null);
        } catch (Exception e) {
            return ChatResponse.builder().type("not_found").message("Hệ thống phân tích bận. Vui lòng thử lại.")
                    .build();
        }
    }

    // ── Direct action handler ────────────────────────────────────────────────
    private ChatResponse handleDirectAction(ChatRequest request) {
        if ("CONFIRM_CORRECT".equals(request.getAction()) || "CONFIRM_WRONG".equals(request.getAction())
                || "REQUEST_MORE_INFO".equals(request.getAction())) {
            chatActionLogRepository.save(ChatActionLog.builder()
                    .actionType(request.getAction())
                    .actionLabel("REQUEST_MORE_INFO".equals(request.getAction()) ? "Request More Info" : "Feedback")
                    .actionData(request.getActionId() != null ? request.getActionId() : request.getMessage())
                    .build());
        }

        if ("select_suggestion".equals(request.getAction()) && request.getActionId() != null) {
            Optional<ChatKnowledgeRepository.KnowledgeItem> item = knowledgeRepository.findById(request.getActionId());
            if (item.isPresent()) {
                return ChatResponse.builder()
                        .type("answer").source("database")
                        .content(item.get().getAnswer())
                        .actionId(item.get().getActionId())
                        .build();
            }
        }

        if ("DENY_PERMISSION".equals(request.getAction())) {
            return ChatResponse.builder()
                    .type("answer")
                    .content("Tôi đã hủy yêu cầu truy cập dữ liệu cá nhân. Bạn có câu hỏi nào khác không?")
                    .build();
        }

        if ("GRANT_PERMISSION".equals(request.getAction())) {
            return handleGrantedPersonalData(request);
        }

        if ("SUGGESTED_QUESTION".equals(request.getAction())) {
            String question = request.getActionId() != null ? request.getActionId() : request.getMessage();
            ChatRequest pseudoReq = new ChatRequest();
            pseudoReq.setMessage(question);
            pseudoReq.setConversation(request.getConversation());
            return processMessage(pseudoReq);
        }

        if ("REQUEST_MORE_INFO".equals(request.getAction())) {
            String topic = request.getActionId() != null ? request.getActionId() : request.getMessage();

            String selectedPath = routeToDocument(topic, request.getConversation());
            if (!"NONE".equalsIgnoreCase(selectedPath)) {
                try {
                    ClassPathResource docResource = new ClassPathResource("documents/" + selectedPath);
                    String docContent;
                    try (InputStreamReader reader = new InputStreamReader(docResource.getInputStream(),
                            StandardCharsets.UTF_8)) {
                        docContent = FileCopyUtils.copyToString(reader);
                    }

                    String ragPrompt = "Bạn là trợ lý của trường Đại học Giao thông Vận tải phân hiệu tại TP.HCM (UTC2).\n"
                            +
                            "Dưới đây là tài liệu chính thức (file: " + selectedPath + "):\n\n" +
                            "--- TÀI LIỆU ---\n" + docContent + "\n--- KẾT THÚC TÀI LIỆU ---\n\n" +
                            "Người dùng muốn biết THÊM thông tin chi tiết về câu hỏi: \"" + topic + "\".\n" +
                            "QUY TẮC:\n" +
                            "1. Hãy cung cấp các chi tiết mở rộng, giải thích sâu hơn từ TÀI LIỆU mà có thể chưa được nhắc đến trong câu trả lời trước.\n"
                            +
                            "2. Chỉ trả lời dựa trên nội dung TÀI LIỆU ở trên.\n" +
                            "3. Trả lời rõ ràng, chi tiết bằng tiếng Việt.\n" +
                            "4. Cuối câu trả lời, ghi chú: (Nguồn: " + selectedPath + ")";

                    String aiReply = groqAiService.chatWithFallback(request.getConversation(), ragPrompt, topic);
                    return ChatResponse.builder()
                            .type("answer")
                            .source("agentic_rag_more_info")
                            .content(aiReply)
                            .documentTitle("Tài liệu: " + selectedPath)
                            .documentSource(selectedPath)
                            .build();
                } catch (Exception e) {
                    System.err.println("Error reading document for more info: " + e.getMessage());
                }
            }

            String systemPrompt = "Bạn là trợ lý ảo của trường Đại học Giao thông Vận tải phân hiệu tại TP.HCM (UTC2). "
                    +
                    "Người dùng muốn biết thêm thông tin chi tiết về chủ đề: \"" + topic + "\". " +
                    "Hãy cung cấp thêm các chi tiết mở rộng, giải thích sâu hơn dựa trên những gì bạn biết. " +
                    "Trả lời rõ ràng, chi tiết bằng tiếng Việt.";
            try {
                String aiReply = groqAiService.chatWithFallback(request.getConversation(), systemPrompt, topic);
                return ChatResponse.builder()
                        .type("answer").source("ai_more_info")
                        .content(aiReply)
                        .build();
            } catch (Exception e) {
                return ChatResponse.builder()
                        .type("not_found")
                        .message("Hệ thống AI hiện đang bận hoặc quá tải. Vui lòng thử lại sau.")
                        .build();
            }
        }

        if ("CONFIRM_CORRECT".equals(request.getAction())) {
            return ChatResponse.builder().type("answer").source("system")
                    .content("Cảm ơn bạn đã phản hồi! Hệ thống sẽ ghi nhận để cải thiện tốt hơn.")
                    .build();
        }
        if ("CONFIRM_WRONG".equals(request.getAction())) {
            return ChatResponse.builder().type("answer").source("system")
                    .content(
                            "Xin lỗi vì câu trả lời chưa chính xác. Bạn có thể diễn đạt lại câu hỏi rõ hơn được không?")
                    .build();
        }
        return ChatResponse.builder().type("not_found").message("Hành động không hợp lệ.").build();
    }

    // ── Math handler (unchanged) ─────────────────────────────────────────────
    private ChatResponse handleMath(String msg) {
        Pattern mathPattern = Pattern.compile(
                "^\\s*([\\d]+(?:\\.[\\d]+)?)\\s*([+\\-*/])\\s*([\\d]+(?:\\.[\\d]+)?)\\s*$");
        Matcher mathMatcher = mathPattern.matcher(msg);
        if (mathMatcher.matches()) {
            double num1 = Double.parseDouble(mathMatcher.group(1));
            String operator = mathMatcher.group(2);
            double num2 = Double.parseDouble(mathMatcher.group(3));
            double result = 0;
            switch (operator) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    result = num2 != 0 ? num1 / num2 : 0;
                    break;
            }
            List<Double> numbers = new ArrayList<>();
            numbers.add(num1);
            numbers.add(num2);
            return ChatResponse.builder()
                    .type("calculation")
                    .id("MATH_" + System.currentTimeMillis())
                    .expression(msg)
                    .numbers(numbers)
                    .result(result)
                    .build();
        }
        return ChatResponse.builder().type("not_found").message("Phép tính không hợp lệ").build();
    }

    // ── Navigation handler (unchanged) ───────────────────────────────────────
    private ChatResponse handleNavigation(ChatRequest request) {
        String msg = request.getMessage().trim();
        String appRoutesInfo = "Danh sách màn hình:\n" +
                "1: Lịch học\n2: Xem điểm thi (kết quả học tập)\n3: Đăng ký tín chỉ (học phần)\n" +
                "4: Trang chủ\n5: Hồ sơ cá nhân (hồ sơ sinh viên)\n6: Học phí (công nợ)\n" +
                "7: Dịch vụ công (thủ tục hành chính)\n8: Đánh giá rèn luyện\n" +
                "9: Ký túc xá (nội trú)\n10: Hỗ trợ (báo lỗi)\n11: Danh mục khác\n" +
                "12: Thông tin cá nhân\n13: Mã QR\n14: Thông báo\n15: Tìm kiếm\n\n";

        String systemPrompt = "Bạn là trợ lý điều hướng UTC2. Hãy trả lời dưới dạng JSON hợp lệ:\n" +
                "{\"response\": \"câu trả lời hướng dẫn mở trang cho người dùng\", \"actionId\": số_màn_hình}\n" +
                appRoutesInfo +
                "Nếu không xác định được màn hình, dùng actionId: 0. " +
                "TUYỆT ĐỐI chỉ trả về JSON, không có text nào khác.";

        try {
            String aiReply = groqAiService.chatWithFallback(request.getConversation(), systemPrompt, msg);
            JsonNode node = objectMapper.readTree(aiReply);
            String responseText = node.has("response")
                    ? node.get("response").asText()
                    : "Bạn có thể xem tính năng này tại trang:";
            int actionId = node.has("actionId") ? node.get("actionId").asInt() : 0;
            return ChatResponse.builder()
                    .type("answer").source("ai_navigation")
                    .content(responseText)
                    .actionId(actionId > 0 ? String.valueOf(actionId) : null)
                    .build();
        } catch (Exception e) {
            return ChatResponse.builder()
                    .type("not_found").message("Không thể xử lý yêu cầu chuyển trang.")
                    .build();
        }
    }

    // ── Knowledge handler ─────────────────────────────────────────────────────
    private ChatResponse handleKnowledge(ChatRequest request) {
        String msg = request.getMessage() != null ? request.getMessage().trim() : "";
        if (msg.isEmpty()) {
            return ChatResponse.builder().type("not_found").message("Vui lòng nhập câu hỏi.").build();
        }

        // B3-1: FAQ exact match
        Optional<ChatKnowledgeRepository.KnowledgeItem> exactMatch = knowledgeRepository.findExactMatch(msg);
        if (exactMatch.isPresent()) {
            return ChatResponse.builder()
                    .type("answer").source("database")
                    .content(exactMatch.get().getAnswer())
                    .actionId(exactMatch.get().getActionId())
                    .build();
        }

        // B3-1b: FAQ suggestions
        if (msg.length() >= 3) {
            List<String> suggestions = knowledgeRepository.findSuggestions(msg);
            if (!suggestions.isEmpty()) {
                return ChatResponse.builder()
                        .type("suggestions").items(suggestions)
                        .build();
            }
        }

        List<ActionButtonDto> actionButtons = List.of(
                ActionButtonDto.builder().type("REQUEST_MORE_INFO").label("Tìm hiểu thêm").data(msg).build());

        // B3-2: Agentic RAG Router — pipeline: keyword match → LLM fallback
        String selectedPath = routeToDocument(msg, request.getConversation());
        System.out.println("ROUTER SELECTED PATH: " + selectedPath);

        if (!"NONE".equalsIgnoreCase(selectedPath)) {
            ChatResponse ragResponse = answerFromDocument(selectedPath, msg, request.getConversation(),
                    actionButtons);
            if (ragResponse != null)
                return ragResponse;
        }

        // B3-3: AI general fallback
        return generalAiFallback(msg, request.getConversation());
    }

    // ── Step 1: keyword match against catalog topics ──────────────────────────
    /**
     * Score each catalog entry by counting how many topic tokens appear in the
     * normalised question. Returns the path of the best match if score >= 1,
     * otherwise "NONE".
     */
    private String keywordMatch(String msg) {
        String normMsg = normalise(msg);
        String bestPath = "NONE";
        int bestScore = 0;

        for (DocEntry entry : catalog) {
            int score = 0;
            for (String token : entry.topicTokens) {
                String normToken = normalise(token.trim());
                if (!normToken.isEmpty() && normMsg.contains(normToken)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestPath = entry.path;
            }
        }

        System.out.println("KEYWORD MATCH score=" + bestScore + " path=" + bestPath);
        return bestScore >= 1 ? bestPath : "NONE";
    }

    /**
     * Remove diacritics-sensitive normalisation — keep lowercase, trim spaces,
     * remove accents.
     */
    private String normalise(String text) {
        if (text == null)
            return "";
        String normalized = java.text.Normalizer.normalize(text.toLowerCase().trim(), java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d");
    }

    // ── Step 2: LLM router (fallback when keyword match fails) ────────────────
    /**
     * Build a compact numbered list from the catalog and ask the LLM to return
     * ONLY the matching doc id (e.g. "DOC_006"). Resolves id → path locally.
     */
    private String llmRoute(String msg, List<ChatMessageDto> conversation) {
        // Build compact catalog summary: numbered list of id + topic (no full JSON)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < catalog.size(); i++) {
            DocEntry e = catalog.get(i);
            sb.append(i + 1).append(". ").append(e.id).append(": ").append(e.topic).append("\n");
        }

        String routingPrompt = "Bạn là hệ thống định tuyến tài liệu UTC2.\n" +
                "Danh sách tài liệu (id: topic):\n" +
                sb +
                "\nNhiệm vụ: Đọc câu hỏi người dùng, chọn DUY NHẤT 1 id tài liệu phù hợp nhất.\n" +
                "Trả về ĐÚNG id đó (ví dụ: DOC_006).\n" +
                "Nếu không có tài liệu nào phù hợp, trả về: NONE.\n" +
                "TUYỆT ĐỐI không trả về bất kỳ text nào khác.";

        try {
            String reply = groqAiService.chatWithFallback(conversation, routingPrompt, msg).trim();
            System.out.println("LLM ROUTER RAW: " + reply);

            // Extract DOC_XXX pattern robustly
            Pattern docIdPattern = Pattern.compile("DOC_\\d+");
            Matcher m = docIdPattern.matcher(reply.toUpperCase());
            if (m.find()) {
                String docId = m.group();
                // Resolve id to path
                for (DocEntry e : catalog) {
                    if (e.id.equalsIgnoreCase(docId)) {
                        return e.path;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("LLM router error: " + e.getMessage());
        }
        return "NONE";
    }

    private final Map<String, String> routeCache = new ConcurrentHashMap<>();

    // ── Pipeline: keyword first, LLM fallback ─────────────────────────────────
    private String routeToDocument(String msg, List<ChatMessageDto> conversation) {
        String key = normalise(msg);
        if (routeCache.containsKey(key))
            return routeCache.get(key);
        String path = keywordMatch(msg);
        if ("NONE".equals(path))
            path = llmRoute(msg, conversation);
        routeCache.put(key, path);
        return path;
    }

    private static final String APP_ROUTES_INFO = "Danh sách màn hình app (ID: Tên):\n" +
            "1: Lịch học, 2: Xem điểm thi, 3: Đăng ký tín chỉ, 4: Trang chủ, 5: Hồ sơ cá nhân, " +
            "6: Học phí, 7: Dịch vụ công, 8: Đánh giá rèn luyện, 9: Ký túc xá, 10: Hỗ trợ, " +
            "11: Danh mục khác, 12: Thông tin cá nhân, 13: Mã QR, 14: Thông báo, 15: Tìm kiếm\n";

    // ── Helper to build dynamic response ──────────────────────────────────────
    private ChatResponse buildDynamicResponse(String aiReply, String source, String docPath) {
        String answerStr = aiReply;
        List<ActionButtonDto> buttons = new ArrayList<>();

        try {
            String jsonPart = aiReply;
            int startIndex = jsonPart.indexOf('{');
            int endIndex = jsonPart.lastIndexOf('}');
            if (startIndex >= 0 && endIndex >= startIndex) {
                jsonPart = jsonPart.substring(startIndex, endIndex + 1);
                JsonNode node = objectMapper.readTree(jsonPart);
                if (node.has("answer")) {
                    answerStr = node.get("answer").asText();
                }
                if (node.has("suggested_screens") && node.get("suggested_screens").isArray()) {
                    for (JsonNode scrNode : node.get("suggested_screens")) {
                        String rawId = scrNode.asText();
                        String normalizedId = rawId.replaceAll("[^\\d]", "");
                        if (!normalizedId.isEmpty()) {
                            buttons.add(ActionButtonDto.builder()
                                    .type("NAVIGATE")
                                    .label("Mở trang")
                                    .data(normalizedId)
                                    .build());
                        }
                    }
                }
                if (node.has("suggestions") && node.get("suggestions").isArray()) {
                    for (JsonNode sugNode : node.get("suggestions")) {
                        String sug = sugNode.asText();
                        buttons.add(ActionButtonDto.builder()
                                .type("SUGGESTED_QUESTION")
                                .label(sug)
                                .data(sug)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            answerStr = aiReply;
        }

        ChatResponse.ChatResponseBuilder builder = ChatResponse.builder()
                .type("answer")
                .source(source)
                .content(answerStr)
                .actionButtons(buttons.isEmpty() ? null : buttons);

        if (docPath != null) {
            builder.documentTitle("Tài liệu: " + docPath)
                    .documentSource(docPath)
                    .confidenceScore(1.0);
        }

        return builder.build();
    }

    private String extractRelevantChunk(String docContent, String msg, int maxChars) {
        if (docContent == null || docContent.length() <= maxChars)
            return docContent;
        String normMsg = normalise(msg);
        String[] paragraphs = docContent.split("\n\n+");
        String best = paragraphs[0];
        int bestScore = -1;
        String[] queryWords = normMsg.split("\\s+");

        for (String para : paragraphs) {
            String normPara = normalise(para);
            int score = 0;
            for (String word : queryWords) {
                if (!word.isEmpty() && normPara.contains(word))
                    score++;
            }
            if (score > bestScore) {
                bestScore = score;
                best = para;
            }
        }
        return best.length() > maxChars ? best.substring(0, maxChars) : best;
    }

    // ── RAG answer from selected document ────────────────────────────────────
    /**
     * Reads the document at selectedPath and asks the LLM to answer strictly
     * from it. Returns null if the document is unreadable or the LLM signals
     * it has no information (triggers general fallback).
     */
    private ChatResponse answerFromDocument(String selectedPath, String msg,
            List<ChatMessageDto> conversation,
            List<ActionButtonDto> actionButtons) {
        String docContent;
        try {
            ClassPathResource docResource = new ClassPathResource("documents/" + selectedPath);
            try (InputStreamReader reader = new InputStreamReader(
                    docResource.getInputStream(), StandardCharsets.UTF_8)) {
                docContent = FileCopyUtils.copyToString(reader);
            }
        } catch (Exception e) {
            System.err.println("Cannot read document [" + selectedPath + "]: " + e.getMessage());
            return null;
        }

        String relevantChunk = extractRelevantChunk(docContent, msg, 2000);

        String ragPrompt = "Bạn là trợ lý của trường Đại học Giao thông Vận tải phân hiệu tại TP.HCM (UTC2).\n" +
                "Dưới đây là tài liệu chính thức (file: " + selectedPath + "):\n\n" +
                "--- TÀI LIỆU ---\n" + relevantChunk + "\n--- KẾT THÚC TÀI LIỆU ---\n\n" +
                APP_ROUTES_INFO + "\n" +
                "QUY TẮC:\n" +
                "1. Chỉ trả lời dựa trên nội dung TÀI LIỆU ở trên.\n" +
                "2. Nếu tài liệu không chứa đủ thông tin, hãy trả về đúng câu: THONG_TIN_KHONG_CO\n" +
                "3. KHÔNG suy luận hoặc thêm thông tin ngoài tài liệu.\n" +
                "4. Trả lời dưới định dạng JSON hợp lệ với cấu trúc sau (LUÔN DÙNG NGOẶC KÉP \" \"):\n" +
                "{\n" +
                "  \"answer\": \"câu trả lời của bạn\",\n" +
                "  \"suggestions\": [\"Câu hỏi gợi ý 1\", \"Câu hỏi gợi ý 2\"],\n" +
                "  \"suggested_screens\": [\"id_man_hinh\"]\n" +
                "}\n" +
                "5. 'suggested_screens' CHỈ chứa ID dạng số (chuỗi), tối đa 1 ID phù hợp nhất. TUYỆT ĐỐI KHÔNG chứa tên màn hình hay dấu hai chấm. (Ví dụ đúng: [\"1\"]. Ví dụ sai: [\"1: Lịch học\"], [\"Lịch học\"]). Nếu không có trang phù hợp, trả về mảng rỗng [].\n"
                +
                "TUYỆT ĐỐI CHỈ TRẢ VỀ JSON, KHÔNG THÊM BẤT KỲ VĂN BẢN NÀO KHÁC BÊN NGOÀI JSON.";

        try {
            String aiReply = groqAiService.chatWithFallback(conversation, ragPrompt, msg);

            // If LLM signals no info in document, fall through to general AI
            if (aiReply.contains("THONG_TIN_KHONG_CO")) {
                System.out.println("RAG: document found but no matching info, falling back.");
                return null;
            }

            ChatResponse response = buildDynamicResponse(aiReply, "agentic_rag", selectedPath);
            if (actionButtons != null && !actionButtons.isEmpty()) {
                List<ActionButtonDto> allButtons = new ArrayList<>();
                if (response.getActionButtons() != null) {
                    allButtons.addAll(response.getActionButtons());
                }
                allButtons.addAll(actionButtons);
                response.setActionButtons(allButtons);
            }
            return response;
        } catch (Exception e) {
            System.err.println("RAG answer error: " + e.getMessage());
            return null;
        }
    }

    // ── General AI fallback ───────────────────────────────────────────────────
    private ChatResponse generalAiFallback(String msg, List<ChatMessageDto> conversation) {
        String systemPrompt = "Bạn là trợ lý ảo của trường Đại học Giao thông Vận tải phân hiệu tại TP.HCM (UTC2). " +
                "Chỉ trả lời các câu hỏi liên quan đến trường đại học, học vụ, sinh viên. " +
                "Từ chối khéo léo các câu hỏi ngoài lề. " +
                "Không bịa thông tin về điểm, học phí hay quy định cụ thể nếu không chắc chắn.\n" +
                APP_ROUTES_INFO + "\n" +
                "YÊU CẦU ĐỊNH DẠNG ĐẦU RA:\n" +
                "Hãy trả lời dưới định dạng JSON hợp lệ với cấu trúc sau (LUÔN DÙNG NGOẶC KÉP \" \"):\n" +
                "{\n" +
                "  \"answer\": \"câu trả lời của bạn\",\n" +
                "  \"suggestions\": [\"Câu hỏi gợi ý 1\", \"Câu hỏi gợi ý 2\"],\n" +
                "  \"suggested_screens\": [\"id_man_hinh\"]\n" +
                "}\n" +
                "5. 'suggested_screens' CHỈ chứa ID dạng số (chuỗi), tối đa 1 ID phù hợp nhất. TUYỆT ĐỐI KHÔNG chứa tên màn hình hay dấu hai chấm. (Ví dụ đúng: [\"1\"]. Ví dụ sai: [\"1: Lịch học\"], [\"Lịch học\"]). Nếu không có trang phù hợp, trả về mảng rỗng [].\n"
                +
                "TUYỆT ĐỐI CHỈ TRẢ VỀ JSON, KHÔNG THÊM VĂN BẢN NÀO KHÁC BÊN NGOÀI JSON.";
        try {
            String aiReply = groqAiService.chatWithFallback(conversation, systemPrompt, msg);
            return buildDynamicResponse(aiReply, "ai_general", null);
        } catch (Exception e) {
            return ChatResponse.builder()
                    .type("not_found")
                    .message("Hệ thống AI hiện đang bận hoặc quá tải. Vui lòng thử lại sau.")
                    .build();
        }
    }
}