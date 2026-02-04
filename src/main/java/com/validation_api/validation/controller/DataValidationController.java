package com.validation_api.validation.controller;

import com.validation_api.validation.service.DataValidationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validacao")
public class DataValidationController {

    @GetMapping("/executar")
    public String executarValidacao() {
        try {
            DataValidationService.validarArquivo();
            return " Validação concluída! Arquivo gerado: validado_despesas.csv";
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
}
