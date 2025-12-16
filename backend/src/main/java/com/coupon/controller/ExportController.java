package com.coupon.controller;

import com.coupon.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/batch/{batchId}")
    public void exportBatchToCSV(@PathVariable Long batchId, HttpServletResponse response) throws IOException {
        exportService.exportBatchToCSV(batchId, response);
    }

    @GetMapping("/all")
    public void exportAllToCSV(HttpServletResponse response) throws IOException {
        exportService.exportAllToCSV(response);
    }
}
