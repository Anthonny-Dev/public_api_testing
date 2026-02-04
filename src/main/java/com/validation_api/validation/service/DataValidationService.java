package com.validation_api.validation.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataValidationService {

    public static void validarArquivo() throws IOException {
        Path entrada = Paths.get("consolidado_despesas.csv");
        Path saida = Paths.get("validado_despesas.csv");

        BufferedReader reader = Files.newBufferedReader(entrada);
        BufferedWriter writer = Files.newBufferedWriter(saida);

        writer.write("CNPJ;RazaoSocial;Trimestre;Ano;ValorDespesas\n");

        String linha = reader.readLine();

        while ((linha = reader.readLine()) != null) {
            String[] col = linha.split(";");
            if (col.length < 5) continue;

            String cnpj = col[0];
            String razao = col[1];
            String trimestre = col[2];
            String ano = col[3];

            String valorStr = (col.length > 5) ? col[4] + "." + col[5] : col[4].replace(",", ".");

            System.out.println("Processando CNPJ: " + cnpj + " | Valor: " + valorStr);

            if (razao == null || razao.trim().isEmpty()) continue;

            double valor;
            try {
                valor = Double.parseDouble(valorStr);
                if (valor <= 0) continue;
            } catch (Exception e) {
                System.out.println("Erro no valor: " + valorStr);
                continue;
            }

            writer.write(String.join(";", cnpj, razao, trimestre, ano, valorStr));
            writer.newLine();
        }
        reader.close();
        writer.close();
    }
    private static boolean cnpjValido(String cnpj) {
        cnpj = cnpj.replaceAll("\\D", "");

        if (cnpj.length() != 14) return false;
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            int soma = 0;
            int peso = 5;
            for (int i = 0; i < 12; i++) {
                soma += (cnpj.charAt(i) - '0') * peso--;
                if (peso < 2) peso = 9;
            }
            int dig1 = soma % 11 < 2 ? 0 : 11 - soma % 11;

            soma = 0;
            peso = 6;
            for (int i = 0; i < 13; i++) {
                soma += (cnpj.charAt(i) - '0') * peso--;
                if (peso < 2) peso = 9;
            }
            int dig2 = soma % 11 < 2 ? 0 : 11 - soma % 11;

            return dig1 == (cnpj.charAt(12) - '0') &&
                    dig2 == (cnpj.charAt(13) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}