package br.com.clairtonluz.bytecom.model.jpa.entity.comercial;

import br.com.clairtonluz.bytecom.model.jpa.entity.extra.EntityGeneric;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author clairton
 */
@Entity
@Table(name = "plano")
public class Plano extends EntityGeneric {

    private static final long serialVersionUID = 5998642329673118596L;
    private String nome;
    private int upload;
    private int download;
    @Column(name = "valor_instalacao")
    private double valorInstalacao;
    @Column(name = "valor_mensalidade")
    private double valorTitulo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getUpload() {
        return upload;
    }

    public void setUpload(int upload) {
        this.upload = upload;
    }

    public int getDownload() {
        return download;
    }

    public void setDownload(int download) {
        this.download = download;
    }

    public double getValorInstalacao() {
        return valorInstalacao;
    }

    public void setValorInstalacao(double valorInstalacao) {
        this.valorInstalacao = valorInstalacao;
    }

    public double getValorTitulo() {
        return valorTitulo;
    }

    public void setValorTitulo(double valorTitulo) {
        this.valorTitulo = valorTitulo;
    }

}
