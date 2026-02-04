package com.validation_api.validation.model;

public class OperadoraANS {

    private String registroANS;
    private String cnpj;
    private String modalidade;
    private String uf;

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRegistroANS() {
        return registroANS;
    }

    public void setRegistroANS(String registroANS) {
        this.registroANS = registroANS;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    @Override
    public String toString() {
        return "OperadoraANS{" +
                "registroANS='" + registroANS + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", modalidade='" + modalidade + '\'' +
                ", uf='" + uf + '\'' +
                '}';
    }
}
