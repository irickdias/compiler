package com.example.rdcompiler.analisadores;

import java.util.*;

public class Token {
    public static final int TKN_ID = 0; // Token Identificador
    public static final int TKN_NUM = 1; // Token Numero
    public static final int TKN_OPE_REL = 2; // Token Operador Relaciaonal
    public static final int TKN_PONTO_V = 3; // Token para laço for
    //public static final int TKN_PON = 3; // Token Pontuação

    //public static final int TKN_ASN = 4; // Token Assign
    public static final int TKN_ATRI = 4; // Token Assign
    public static final int TKN_RES = 5; // Token de palavras reservadas
    public static final int TKN_TIPO = 6; // Token de tipos (int, float, double)
    public static final int TKN_IF = 7; // Token para condicional if
    public static final int TKN_WHILE = 8; // Token para laço while
    public static final int TKN_FOR = 9; // Token para laço for
    public static final int TKN_PONTO_PV = 10; // Token ponto e vergula
    public static final int TKN_OPE_ARI = 11; // Token Operador Aritmetico
    public static final int TKN_ABRE_PAR = 12; // Token Abre parenteses
    public static final int TKN_FECHA_PAR = 13; // Token Fecha parenteses
    public static final int TKN_ABRE_CHA = 14; // Token Abre chaves
    public static final int TKN_FECHA_CHA = 15; // Token Fecha chaves
    public static final int TKN_ELSE = 16; // Token else

    private int linhaErro;
    private int linha;



    private final List<String> listaTokens = new ArrayList<String>(
            Arrays.asList("TKN_ID", "TKN_NUM", "TKN_OPE", "TKN_PON", "TKN_ASN", "TKN_RES")
    );
    private int type;
    private String text;

    public Token(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public Token()
    {
        super();
    }

    public int getType() {
        return type;
    }

    public String getTypeString()
    {
        return listaTokens.get(type);
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLinhaErro() {
        return linhaErro;
    }

    public void setLinhaErro(int linhaErro) {
        this.linhaErro = linhaErro;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", text='" + text + '\'' +
                '}';
    }
}
