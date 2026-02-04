package com.validation_api.validation.service;
import com.validation_api.validation.model.OperadoraANS;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataEnrichmentService {
    private static final String URL_OPERADORAS =
            "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv";


    public Path enriquecerCsv(Path csvConsolidado) throws IOException {
        Map<String, OperadoraANS> cadastro = carregarCadastroANS();
        Path output = Paths.get("src/main/resources/output/consolidado_enriquecido.csv");

        try (
                BufferedReader reader = Files.newBufferedReader(csvConsolidado);
                BufferedWriter writer = Files.newBufferedWriter(output)
        ) {

            reader.readLine();


            writer.write("RegistroANS;RazaoSocial;Trimestre;Ano;ValorDespesas;RegistroANS_Original;Modalidade;UF\n");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(";");

                if (cols.length >= 5) {
                    String registroANS = cols[0].trim();
                    OperadoraANS op = cadastro.get(registroANS);

                    System.out.println(cols[0]);


                    writer.write(cols[0] + ";" + cols[1] + ";" + cols[2] + ";" + cols[3] + ";" + cols[4]);


                    if (op != null) {
                        writer.write(";" + op.getRegistroANS() + ";" + op.getModalidade() + ";" + op.getUf());
                    } else {
                        writer.write(";;;");
                    }
                    writer.write("\n");

                    if (op == null) {
                        System.out.println("RegistroANS não encontrado: " + registroANS);
                    }

                }
            }
        }
        return output;
    }

    private Map<String, OperadoraANS> carregarCadastroANS() throws IOException {
        Map<String, OperadoraANS> map = new HashMap<>();

        URL url = new URL(URL_OPERADORAS);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.ISO_8859_1))) {

            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] c = line.split(";");

                if (c.length >= 11) {

                    String registroANS = c[0].trim().replace("\"", "");


                    if (!registroANS.isEmpty() && registroANS.matches("\\d+")) {

                        OperadoraANS operadora = new OperadoraANS();


                        operadora.setRegistroANS(registroANS);


                        if (c.length > 1) {
                            operadora.setCnpj(c[1].trim().replace("\"", ""));
                        }


                        if (c.length > 4) {
                            operadora.setModalidade(c[4].trim().replace("\"", ""));
                        }


                        if (c.length > 10) {
                            operadora.setUf(c[10].trim().replace("\"", ""));
                        }


                        map.put(registroANS, operadora);
                    }
                }
            }
            System.out.println("Total de operadoras indexadas por RegistroANS: " + map.size());


            System.out.println("Exemplos de RegistroANS no mapa:");
            map.keySet().stream().limit(5).forEach(key -> {
                System.out.println("  " + key + " -> " + map.get(key));
            });
        }
        return map;
    }

    private OperadoraANS criarOperadora(String[] c) {
        OperadoraANS o = new OperadoraANS();

        o.setRegistroANS(c[0]);
        o.setCnpj(c[1]);
        o.setModalidade(c[5]);
        o.setUf(c[11]);
        return o;
    }
}