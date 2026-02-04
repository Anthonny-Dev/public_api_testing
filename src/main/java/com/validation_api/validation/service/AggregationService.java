package com.validation_api.validation.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AggregationService {

    public Path agregarDadosESalvarCSV(Path csvEnriquecido) throws IOException {
        Map<String, List<Double>> despesasPorOperadora = new HashMap<>();


        try (BufferedReader reader = Files.newBufferedReader(csvEnriquecido)) {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(";", -1);

                if (cols.length >= 8) {
                    String razaoSocial = cols[1].trim();
                    String uf = cols[7].trim();
                    try {
                        double valor = Double.parseDouble(cols[4].replace(",", ".").trim());
                        String chave = razaoSocial + " (" + uf + ")";
                        despesasPorOperadora
                                .computeIfAbsent(chave, k -> new ArrayList<>())
                                .add(valor);
                    } catch (Exception e) {
                        // Ignorar linhas com erro
                        System.err.println("Erro ao processar linha: " + line);
                    }
                }
            }
        }
        List<Map.Entry<String, Double>> totaisOrdenados = despesasPorOperadora.entrySet().stream()
                .map(entry -> {
                    double total = entry.getValue().stream().mapToDouble(Double::doubleValue).sum();
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), total);
                })
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // Ordenar decrescente
                .collect(Collectors.toList());


        Path outputDir = Paths.get("src/main/resources/output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            System.out.println("Pasta 'output' criada: " + outputDir.toAbsolutePath());
        }


        Path outputPath = outputDir.resolve("despesas_agregadas.csv");

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            writer.write("Posicao;Operadora;TotalDespesas;Media;QuantidadeRegistros\n");


            for (int i = 0; i < totaisOrdenados.size(); i++) {
                Map.Entry<String, Double> entry = totaisOrdenados.get(i);
                String operadora = entry.getKey();
                double total = entry.getValue();

                List<Double> despesas = despesasPorOperadora.get(operadora);
                double media = despesas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                int quantidade = despesas.size();


                String totalStr = String.format("%.2f", total).replace(",", ".");
                String mediaStr = String.format("%.2f", media).replace(",", ".");

                writer.write(String.format("%d;%s;%s;%s;%d\n",
                        i + 1, operadora, totalStr, mediaStr, quantidade));
            }
        }

        System.out.println("Arquivo 'despesas_agregadas.csv' gerado com sucesso!");
        System.out.println("Local: " + outputPath.toAbsolutePath());
        System.out.println("Total de operadoras: " + totaisOrdenados.size());

        return outputPath;
    }

    public Path agregarDados(Path csvEnriquecido) {
        return csvEnriquecido;
    }
}