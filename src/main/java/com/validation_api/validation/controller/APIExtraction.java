package com.validation_api.validation.controller;

import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

// classes que vão executar um endpoint DEVEM ter um @RestController para serem encontrados
@RestController
@RequestMapping("/downloads")
public class APIExtraction {

    @GetMapping("/teste")
    public void teste(){
        String urlDoRepositorio = ("https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/2025/");
        try {
            //baixarRepositorioPublico(urlDoRepositorio);
            System.out.println("Dados baixados com sucesso!");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @GetMapping("/ler-conteudo")
    public String lerConteudoZip() {
        String urlDiretorio = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/2025/";

        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            // 1. Baixa HTML da página
            HttpRequest requestPagina = HttpRequest.newBuilder()
                    .uri(URI.create(urlDiretorio))
                    .GET()
                    .build();

            HttpResponse<String> responsePagina = client.send(requestPagina, HttpResponse.BodyHandlers.ofString());

            Pattern pattern = Pattern.compile("href=\"(.*?\\.zip)\"", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(responsePagina.body());

            while (matcher.find()) {
                String nomeZip = matcher.group(1);
                String urlZip = urlDiretorio + nomeZip;

                System.out.println("Abrindo ZIP: " + nomeZip);

                // 2. Agora sim abre o ZIP real
                HttpRequest requestZip = HttpRequest.newBuilder()
                        .uri(URI.create(urlZip))
                        .GET()
                        .build();

                HttpResponse<InputStream> responseZip = client.send(requestZip, HttpResponse.BodyHandlers.ofInputStream());

                try (ZipInputStream zis = new ZipInputStream(responseZip.body())) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        System.out.println("  Arquivo dentro do ZIP: " + entry.getName());
                        zis.closeEntry();
                    }
                }
            }

            return "Leitura concluída! Veja o console.";

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }


    @GetMapping("/baixar-todos")
    public String baixarTodosDoDiretorio() {
        String urlDiretorio = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/2025/";

        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest requestPagina = HttpRequest.newBuilder()
                    .uri(URI.create(urlDiretorio))
                    .GET()
                    .build();

            HttpResponse<String> responsePagina = client.send(requestPagina, HttpResponse.BodyHandlers.ofString());

            Pattern pattern = Pattern.compile("href=\"(.*?\\.zip)\"", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(responsePagina.body());

            List<String> zipValidos = new ArrayList<>();

            while (matcher.find()) {
                String nomeZip = matcher.group(1);
                String urlZip = urlDiretorio + nomeZip;

                if (executarDownloadEExtracao(urlZip, client)) {
                    zipValidos.add(nomeZip);
                }
            }

            if (zipValidos.isEmpty()) return "Nenhum ZIP com header 'EVENTOS' OU 'SINISTROS' encontrados.";

            for (String nomeArquivo : zipValidos) {
                executarDownloadEExtracao(urlDiretorio + nomeArquivo, client);
            }

            return "Processamento concluído: " + zipValidos.size() + " arquivos.";

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }


    // Metodo para executar download e extracao somente dos arquivos que conterem sinistros ou eventos
    private boolean executarDownloadEExtracao(String urlZip, HttpClient client) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlZip))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) return false;

        Path diretorioDestino = Paths.get("extracao_repositorio");

        boolean zipValido = false;

        try (ZipInputStream zis = new ZipInputStream(response.body())) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                if (!entry.getName().endsWith(".csv")) {
                    zis.closeEntry();
                    continue;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(zis, java.nio.charset.StandardCharsets.ISO_8859_1)
                );

                List<String> linhas = new ArrayList<>();
                String linha;

                while ((linha = reader.readLine()) != null) {
                    linhas.add(linha);
                    if (linha.toUpperCase().contains("EVENTOS") ||
                            linha.toUpperCase().contains("SINISTROS")) {
                        zipValido = true;
                    }
                }

                if (zipValido) {
                    Path destinoArquivo = diretorioDestino.resolve(entry.getName()).normalize();
                    Files.createDirectories(destinoArquivo.getParent());
                    Files.write(destinoArquivo, linhas);
                    System.out.println("⬇ Arquivo salvo: " + destinoArquivo);
                }

                zis.closeEntry();
            }
        }

        return zipValido;
    }

    @GetMapping("/consolidar")
    public String consolidarDespesas() {
        Path pasta = Paths.get("extracao_repositorio");
        Path csvFinal = Paths.get("consolidado_despesas.csv");
        Path zipFinal = Paths.get("consolidado_despesas.zip");

        class Registro {
            String cnpj;
            String razao;
            String trimestre;
            String ano;
            double valor;
        }

        Map<String, String> cnpjRazao = new HashMap<>();
        List<Registro> registros = new ArrayList<>();

        try {
            DirectoryStream<Path> arquivos = Files.newDirectoryStream(pasta, "*.csv");

            for (Path arquivo : arquivos) {
                BufferedReader reader = Files.newBufferedReader(arquivo, java.nio.charset.StandardCharsets.ISO_8859_1);
                String header = reader.readLine();

                String linha;
                while ((linha = reader.readLine()) != null) {
                    linha = linha.replace("\"", "");
                    String[] colunas = linha.split(";");

                    if (colunas.length < 6) continue;

                    String data = colunas[0];
                    String regAns = colunas[1];
                    String descricao = colunas[3];
                    String saldoFinal = colunas[5];


                    double valor;
                    try {
                        valor = Double.parseDouble(saldoFinal.replace(",", "."));
                        if (valor <= 0) continue;
                    } catch (Exception e) {
                        continue;
                    }


                    String ano;
                    String trimestre;
                    try {
                        String[] partes = data.split("-");
                        ano = partes[0];
                        int mes = Integer.parseInt(partes[1]);
                        trimestre = "T" + ((mes - 1) / 3 + 1);
                    } catch (Exception e) {
                        continue;
                    }

                    // simulando cnpj e razao social
                    String cnpj = regAns;
                    String razao = "Empresa_" + regAns;

                    // cnpj duplicado, com razao diferente
                    if (cnpjRazao.containsKey(cnpj) && !cnpjRazao.get(cnpj).equals(razao)) {
                        razao = razao + "_SUSPEITO";
                    } else {
                        cnpjRazao.put(cnpj, razao);
                    }

                    Registro r = new Registro();
                    r.cnpj = cnpj;
                    r.razao = razao;
                    r.trimestre = trimestre;
                    r.ano = ano;
                    r.valor = valor;

                    registros.add(r);
                }
            }


            BufferedWriter writer = Files.newBufferedWriter(csvFinal);
            writer.write("CNPJ;RazaoSocial;Trimestre;Ano;ValorDespesas\n");

            for (Registro r : registros) {
                writer.write(String.format("%s;%s;%s;%s;%.2f\n",
                        r.cnpj, r.razao, r.trimestre, r.ano, r.valor));
            }
            writer.close();


            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFinal))) {
                ZipEntry entry = new ZipEntry("consolidado_despesas.csv");
                zos.putNextEntry(entry);
                Files.copy(csvFinal, zos);
                zos.closeEntry();
            }

            return "Consolidação concluída - Arquivo: consolidado_despesas.zip";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro: " + e.getMessage();
        }
    }
}
