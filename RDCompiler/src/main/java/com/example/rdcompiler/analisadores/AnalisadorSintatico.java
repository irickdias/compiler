package com.example.rdcompiler.analisadores;

import com.example.rdcompiler.MainSceneController;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AnalisadorSintatico extends MainSceneController {

    private AnalisadorLexico anaLexi; // Analisado Lexico
    private AnalisadorLexico tempAnaLexi;
    private Token token; // token atual
    private Token tempToken; // token atual
    private List<String> memoria = new ArrayList<String>();
    private boolean declaracao_flag = false;

    // Analisdaor Sinatico fica chamando o Analisador Lexico toda hora
    public AnalisadorSintatico(AnalisadorLexico al)
    {
        anaLexi = al;
    }

    public void P(){
        token = anaLexi.nextToken();
        if(token.getType() == Token.TKN_TIPO) // int, float, double
        {
            declaracao_flag = true;
            DECLARACAO();
        }
        else
            if(token.getType() == Token.TKN_IF) // TOKEN "if" reconhecido
                CONDICIONAL_IF();
            else
                if(token.getType() == Token.TKN_WHILE || token.getType() == Token.TKN_FOR)
                    LACO();
                else
                    if(token.getType() == Token.TKN_ID)
                        IDENTIFICADOR();
                    else
                        throw new ErroSintatico("Inicio de novo comando errado!");
    };

    public void DECLARACAO()
    {
        // se é uma declaração, precisa inicialmente
        // de um <identificador>, para depois chamar ATRIBUICAO() se preciso
        token = anaLexi.nextToken();
        if(token.getType() == Token.TKN_ID)
        {
            //DECLARACAO2();
            IDENTIFICADOR();
            //FIMLINHA();
        }
        else
            throw new ErroSintatico("Identificador esperado!");
    }

    public void DECLARACAO2()
    {
        token = anaLexi.nextToken();
        if(token.getType() == Token.TKN_ATRI)
            ATRIBUICAO();
        else if(token.getType() == Token.TKN_PONTO_V)
            DECLARACAO();
    }

    public void IDENTIFICADOR()
    {
        token = anaLexi.nextToken();

        if(token.getType() == Token.TKN_ATRI)
            ATRIBUICAO();

        if(declaracao_flag && token.getType() == Token.TKN_PONTO_V) // quer continuar declarando variavel
        {
            // faz leitura do proximo token, que deverá ser um <identificador>
            // e chama recursivamente a função IDENTIFICADOR()
            token = anaLexi.nextToken();

            if(token.getType() == Token.TKN_ID)
                IDENTIFICADOR();
            else
                throw new ErroSintatico("Identificador esperado!");
        }


        FIMLINHA();
    }

    public void ATRIBUICAO(){
        token = anaLexi.nextToken();

        // VALOR() fará reconhecimento se o token é <identificador ou numero>
        VALOR();
        OPERADOR_A();
        if(token.getType() != Token.TKN_PONTO_PV && token.getType()  != Token.TKN_PONTO_V)
            ATRIBUICAO();


    }

    public void OPERADOR_A()
    {
        token = anaLexi.nextToken();
        if(token.getType() != Token.TKN_OPE_ARI && token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_PONTO_V)
            throw new ErroSintatico("Operador Aritmético não reconhecido!");
    }

    public void TIPO(){
        //token = anaLexi.nextToken();
        if (token.getType() != Token.TKN_TIPO)
            throw new ErroSintatico("Tipo da variavel esperada");
    }

    public void CONDICIONAL_IF(){
        token = anaLexi.nextToken(); // precisa ser um (
        if(token.getType() != Token.TKN_ABRE_PAR)
            throw new ErroSintatico("Abertura de parenteses faltando!");
        else
        {
            ER(); // expressao relacional

            token = anaLexi.nextToken(); // precisa ser um )
            if(token.getType() != Token.TKN_FECHA_PAR)
                throw new ErroSintatico("Faltando fechar parenteses!");
            else
            {
                token = anaLexi.nextToken();
                if(token.getType() != Token.TKN_ABRE_CHA) // precisa ser um {
                    throw new ErroSintatico("Faltando abrir chaves!");
                else
                {
                    // LOOP -  RESOLVER
                    // PRECISA FICAR EM LOOPING

//                    tempAnaLexi = anaLexi; // copia o abjeto
//                    tempToken = tempAnaLexi.nextToken();
//                    while(tempToken.getType() != Token.TKN_FECHA_CHA)
//                    {
//                        P(); // Chama programa novamente
//
//                        tempAnaLexi = anaLexi; // copia o abjeto
//                        tempToken = tempAnaLexi.nextToken();
//                    }

                    P();

                    token = anaLexi.nextToken();
                    if(token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
                        throw new ErroSintatico("Faltando fechar chaves!");
                    else
                    {
                        tempAnaLexi = anaLexi; // copia o abjeto
                        tempToken = tempAnaLexi.nextToken();
                        if(tempToken.getType() == Token.TKN_ELSE)
                            CONDICIONAL_ELSE();
                        else
                            FIMESTRUTURA();
                    }

                }
            }
        }
    }

    public void CONDICIONAL_ELSE()
    {
        token = anaLexi.nextToken(); // consome um TKN_ELSE
        if(token.getType() == Token.TKN_ELSE)
        {
            token = anaLexi.nextToken();
            if(token.getType() != Token.TKN_ABRE_CHA) // precisa ser um {
                throw new ErroSintatico("Faltando abrir chaves!");
            else
            {
//                tempAnaLexi = anaLexi; // copia o abjeto
//                tempToken = tempAnaLexi.nextToken();
//                while(tempToken.getType() != Token.TKN_FECHA_CHA)
//                {
//                    P(); // Chama programa novamente
//
//                    tempAnaLexi = anaLexi; // copia o abjeto
//                    tempToken = tempAnaLexi.nextToken();
//                }

                P();

                token = anaLexi.nextToken();
                if(token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
                    throw new ErroSintatico("Faltando fechar chaves!");
                else
                    FIMESTRUTURA();
            }
        }
    }

    public void LACO(){};

    public void EXPRESSAO(){
        VALOR();
        EA();
    }

    public void VALOR(){
        //token = anaLexi.nextToken();
        if(token.getType() != Token.TKN_ID && token.getType() != Token.TKN_NUM)
            throw new ErroSintatico("identificador ou numero esperado!");

    }

    public void EA(){

    }

    public void ER(){
        token = anaLexi.nextToken(); // precisa de um identifiador

        if(token.getType() != Token.TKN_ID)
            throw new ErroSintatico("Faltando identificador");
        else
        {
            token = anaLexi.nextToken(); // precisa ser um operador relacional
            if(token.getType() != Token.TKN_OPE_REL)
                throw new ErroSintatico("operador relacional não reconhecido");
            else
            {
                token = anaLexi.nextToken(); // deverá ler um indentificador ou numero
                VALOR();
            }
        }
    }

    public void FOR(){}

    public void WHILE(){}

    public void FIMLINHA() // verifica tambem se irá ler outra linha de comando
    {
        //token = anaLexi.nextToken();
        if(declaracao_flag)
            declaracao_flag = false;

        if(token.getType() != Token.TKN_PONTO_PV)
            throw new ErroSintatico("Faltando ';'");
        else
        {
            tempAnaLexi = anaLexi; // copia o abjeto
            tempToken = tempAnaLexi.nextToken();
            if(tempToken.getType() == Token.TKN_ID || tempToken.getType() == Token.TKN_IF ||
                    tempToken.getType() == Token.TKN_FOR || tempToken.getType() == Token.TKN_WHILE ||
                        tempToken.getType() == Token.TKN_TIPO)
                P();
        }
    }

    public void FIMESTRUTURA()
    {
        tempAnaLexi = anaLexi; // copia o abjeto
        tempToken = tempAnaLexi.nextToken();

        if(tempToken.getType() == Token.TKN_ID || tempToken.getType() == Token.TKN_IF ||
                tempToken.getType() == Token.TKN_FOR || tempToken.getType() == Token.TKN_WHILE ||
                tempToken.getType() == Token.TKN_TIPO)
            P();

    }

    public void showToken()
    {
        MainSceneController TESTE = new MainSceneController();
        Text txt = new Text(token.toString());
        txt.setFont(new Font(12));
        txt.setFill(Color.GREEN);
        //flowPaneTokens.getChildren().add(txt);
        //MainSceneController.class.getResource();
    }

}
