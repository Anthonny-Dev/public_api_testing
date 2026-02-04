package com.validation_api.validation.controller;

import com.validation_api.validation.service.DataEnrichmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/enrichment")
public class DataEnrichmentController {

    private final DataEnrichmentService service;

    public DataEnrichmentController(DataEnrichmentService service) {
        this.service = service;
    }

    @GetMapping("/executar")
    public ResponseEntity<String> enriquecer() {
        try {
            Path consolidado = Paths.get("consolidado_despesas.csv");
            Path result = service.enriquecerCsv(consolidado);

            return ResponseEntity.ok(
                    "Arquivo enriquecido gerado em: " + result.toAbsolutePath()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }
}
