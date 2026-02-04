package com.validation_api.validation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class APIController {
    @GetMapping("/baixar-seguro")
    public String baixarSomenteSeForZip(@RequestParam("url") String urlZip) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlZip))
                    .method("GET", HttpRequest.BodyPublishers.noBody()) // Faz a requisição
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            String contentType = response.headers().firstValue("Content-Type").orElse("");
            boolean ehZip = urlZip.toLowerCase().endsWith(".zip") || contentType.contains("zip");

            if (response.statusCode() == 200 && ehZip) {
                Path diretorioDestino = Paths.get("extracao_repositorio");
                if (!Files.exists(diretorioDestino)) Files.createDirectories(diretorioDestino);

                try (ZipInputStream zis = new ZipInputStream(response.body())) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        Path targetPath = diretorioDestino.resolve(entry.getName()).normalize();

                        if (entry.isDirectory()) {
                            Files.createDirectories(targetPath);
                        } else {
                            Files.createDirectories(targetPath.getParent());
                            Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        zis.closeEntry();
                    }
                }
                return "Arquivo ZIP validado e extraído com sucesso!";
            } else {
                return "Erro: O link fornecido não aponta para um arquivo ZIP válido. (Status: " + response.statusCode() + ")";
            }
        } catch (Exception e) {
            return "Erro no processamento: " + e.getMessage();
        }
    }
}
