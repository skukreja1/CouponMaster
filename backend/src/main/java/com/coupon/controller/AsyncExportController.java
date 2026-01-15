package com.coupon.controller;

import com.coupon.dto.ExportJobDTO;
import com.coupon.service.AsyncExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class AsyncExportController {

    private final AsyncExportService asyncExportService;

    @PostMapping("/batch/{batchId}")
    public ResponseEntity<ExportJobDTO> submitBatchExport(@PathVariable Long batchId) {
        ExportJobDTO job = asyncExportService.submitBatchExport(batchId);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/all")
    public ResponseEntity<ExportJobDTO> submitAllExport() {
        ExportJobDTO job = asyncExportService.submitAllExport();
        return ResponseEntity.ok(job);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ExportJobDTO> getJobStatus(@PathVariable Long jobId) {
        ExportJobDTO job = asyncExportService.getJobStatus(jobId);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<ExportJobDTO>> getAllJobs() {
        List<ExportJobDTO> jobs = asyncExportService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/batch/{batchId}")
    public ResponseEntity<List<ExportJobDTO>> getJobsForBatch(@PathVariable Long batchId) {
        List<ExportJobDTO> jobs = asyncExportService.getJobsForBatch(batchId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/download/{jobId}")
    public ResponseEntity<Resource> downloadExport(@PathVariable Long jobId) {
        File file = asyncExportService.getExportFile(jobId);

        FileSystemResource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(file.length())
                .body(resource);
    }
}
