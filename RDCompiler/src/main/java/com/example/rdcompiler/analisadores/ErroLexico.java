package com.example.rdcompiler.analisadores;

public class ErroLexico extends RuntimeException{
    private int linha;
    private String msg;

    public ErroLexico(int linha, String msg)
    {
        this.linha = linha;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }



    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }
}
