import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class DataEnrichmentServiceTest {

    @org.testng.annotations.Test
    public void testArquivoANS() throws IOException {
        String urlStr = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        System.out.println("Response Code: " + conn.getResponseCode());
        System.out.println("Content-Type: " + conn.getContentType());
        System.out.println("Content-Length: " + conn.getContentLength());

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.ISO_8859_1))) {

            System.out.println("\nPrimeiras 10 linhas do arquivo:");
            for (int i = 0; i < 10; i++) {
                String line = br.readLine();
                System.out.println(i + ": " + line);
                if (line == null) break;
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}