package com.utc2.appreborn.backend.modules.imports.service;

import com.utc2.appreborn.backend.modules.imports.dto.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {
    ImportResultResponse importProfiles(MultipartFile file, boolean overwrite);
    ImportResultResponse importTuition(MultipartFile file, boolean overwrite);
    ImportResultResponse importCurriculum(MultipartFile file, boolean overwrite);
    ImportResultResponse importCourses(MultipartFile file, boolean overwrite);
    ImportResultResponse importStudents(MultipartFile file, boolean overwrite);
    ImportResultResponse importDormitoryRooms(MultipartFile file, boolean overwrite);
    ImportResultResponse importEnrollments(MultipartFile file, boolean overwrite);
}