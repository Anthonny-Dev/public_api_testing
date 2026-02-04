package com.validation_api.validation.controller;

import com.validation_api.validation.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class AggregationController {

    @Autowired
    private AggregationService aggregationService;

    @GetMapping("/gerar-agregado")
    public ResponseEntity<String> gerarAgregado() {
        try {
            // Verificar se o arquivo de entrada existe
            Path arquivoEnriquecido = Paths.get("src/main/resources/output/consolidado_enriquecido.csv");

            if (!Files.exists(arquivoEnriquecido)) {
                return ResponseEntity.badRequest()
                        .body("Arquivo 'consolidado_enriquecido.csv' não encontrado em: " +
                                arquivoEnriquecido.toAbsolutePath());
            }

            System.out.println("Iniciando agregação...");
            Path resultado = aggregationService.agregarDadosESalvarCSV(arquivoEnriquecido);


            if (Files.exists(resultado)) {
                long tamanho = Files.size(resultado);
                return ResponseEntity.ok("Arquivo gerado com sucesso!\n" +
                        "Nome: " + resultado.getFileName() + "\n" +
                        "Tamanho: " + tamanho + " bytes\n" +
                        "Local: " + resultado.toAbsolutePath());
            } else {
                return ResponseEntity.internalServerError()
                        .body("Erro: Arquivo não foi criado!");
            }

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro: " + e.getMessage());
        }
    }
}