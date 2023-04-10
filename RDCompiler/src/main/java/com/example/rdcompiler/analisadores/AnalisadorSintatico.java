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
    private List<Token> tokens = new ArrayList<Token>();

    private boolean declaracao_flag = false;
    private boolean for_flag = false;
    private boolean fim_programa = false;

    public boolean isFim_programa() {
        return fim_programa;
    }

    private int linha;
    private int lastLinha;



    // Analisdaor Sinatico fica chamando o Analisador Lexico toda hora
    public AnalisadorSintatico(AnalisadorLexico al)
    {
        anaLexi = al;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public void P(){
        token = anaLexi.nextToken(); tokens.add(token);

        if(token == null)
            fim_programa =true;
        else
        {
            lastLinha = token.getLinha();
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
            {
                // sincroniza tokens
                int saveLinha = token.getLinha();
                syncTokens();
                throw new ErroSintatico(saveLinha, "Inicio/caracter de novo comando errado!");
            }
        }



    };

    public void DECLARACAO()
    {
        // se é uma declaração, precisa inicialmente
        // de um <identificador>, para depois chamar ATRIBUICAO() se preciso
        token = anaLexi.nextToken(); tokens.add(token);

        if (token == null)
        {
            fim_programa = true;
            throw new ErroSintatico(lastLinha, "Identificador esperado!");
        }
        else
        {
            lastLinha = token.getLinha();
            if(token.getType() == Token.TKN_ID)
            {
                //DECLARACAO2();
                IDENTIFICADOR();
                //FIMLINHA();
            }
            else
            {
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                throw new ErroSintatico(saveLinha, "Identificador esperado!");
            }
        }


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
        token = anaLexi.nextToken(); tokens.add(token);

        if(for_flag && token.getType() == Token.TKN_ATRI)
            ATRIBUICAO_FOR();
        else if(token != null && token.getType() == Token.TKN_ATRI)
            ATRIBUICAO();

        if(declaracao_flag && token != null && token.getType() == Token.TKN_PONTO_V) // quer continuar declarando variavel
        {
            // faz leitura do proximo token, que deverá ser um <identificador>
            // e chama recursivamente a função IDENTIFICADOR()
            token = anaLexi.nextToken(); tokens.add(token);

            if(token.getType() == Token.TKN_ID)
                IDENTIFICADOR();
            else
            {
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                throw new ErroSintatico(saveLinha,"Identificador esperado!");
            }

        }


        FIMLINHA();
    }

    public void ATRIBUICAO(){
        token = anaLexi.nextToken(); tokens.add(token);

        // VALOR() fará reconhecimento se o token é <identificador ou numero>
        VALOR();
        OPERADOR_A();
        if(token.getType() != Token.TKN_PONTO_PV && token.getType()  != Token.TKN_PONTO_V)
            ATRIBUICAO();


    }

    public void ATRIBUICAO_FOR()
    {

    }

    public void OPERADOR_A()
    {
        token = anaLexi.nextToken(); tokens.add(token);
        if(token.getType() != Token.TKN_OPE_ARI && token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_PONTO_V)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "Operador Aritmético não reconhecido!");
        }

    }

    public void TIPO(){
        //token = anaLexi.nextToken();
//        if (token.getType() != Token.TKN_TIPO)
//            throw new ErroSintatico("Tipo da variavel esperada");
    }

    public void CONDICIONAL_IF(){
        token = anaLexi.nextToken(); // precisa ser um (
        tokens.add(token);

        if(token.getType() != Token.TKN_ABRE_PAR)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "Abertura de parenteses faltando!");
        }
        else
        {
            ER(); // expressao relacional

            token = anaLexi.nextToken(); // precisa ser um )
            tokens.add(token);

            if(token.getType() != Token.TKN_FECHA_PAR)
            {
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                throw new ErroSintatico(saveLinha, "Faltando fechar parenteses!");
            }

            else
            {
                token = anaLexi.nextToken(); tokens.add(token);
                if(token.getType() != Token.TKN_ABRE_CHA) // precisa ser um {
                {
                    int saveLinha = token.getLinha();
                    syncTokens();
                    //int saveLinha = token.getLinha();
                    throw new ErroSintatico(saveLinha, "Faltando abrir chaves!");
                }
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

                    token = anaLexi.nextToken(); tokens.add(token);
                    if(token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
                    {
                        int saveLinha = token.getLinha();
                        syncTokens();
                        //int saveLinha = token.getLinha();
                        throw new ErroSintatico(saveLinha, "Faltando fechar chaves!");
                    }
                    else
                    {
//                        tempAnaLexi = anaLexi; // copia o abjeto
//                        tempToken = tempAnaLexi.nextToken();

                        int savePos = anaLexi.getPos(); // salva posição que está  referenete ao vetor de caracteres
                        Token tempToken = anaLexi.nextToken(); // visualiza o proximo token
                        anaLexi.setPos(savePos); // volta para a posição que estava antes

                        if(tempToken != null && tempToken.getType() == Token.TKN_ELSE)
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
        tokens.add(token);

        if(token.getType() == Token.TKN_ELSE)
        {
            token = anaLexi.nextToken(); tokens.add(token);
            if(token.getType() != Token.TKN_ABRE_CHA) // precisa ser um {
            {
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                throw new ErroSintatico(saveLinha, "Faltando abrir chaves!");
            }
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

                // inclusive, pode ser um if de novo
                P();

                token = anaLexi.nextToken(); tokens.add(token);
                if(token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
                {
                    int saveLinha = token.getLinha();
                    syncTokens();
                    //int saveLinha = token.getLinha();
                    throw new ErroSintatico(saveLinha, "Faltando fechar chaves!");
                }
                else
                    FIMESTRUTURA();
            }
        }
    }

    public void LACO(){
        if(token.getType() == Token.TKN_FOR)
            FOR();
        else if(token.getType() == Token.TKN_WHILE)
            WHILE();
    };

    public void EXPRESSAO(){
        VALOR();
        EA();
    }

    public void VALOR(){
        //token = anaLexi.nextToken();
        if(token.getType() != Token.TKN_ID && token.getType() != Token.TKN_NUM)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "identificador ou numero esperado!");
        }


    }

    public void EA(){

    }

    public void ER(){
        token = anaLexi.nextToken(); // precisa de um identificador
        tokens.add(token);

        if(token.getType() != Token.TKN_ID)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "Faltando identificador");
        }
        else
        {
            token = anaLexi.nextToken(); // precisa ser um operador relacional
            tokens.add(token);

            if(token.getType() != Token.TKN_OPE_REL)
            {
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                throw new ErroSintatico(saveLinha, "operador relacional não reconhecido");
            }
            else
            {
                token = anaLexi.nextToken(); // deverá ler um indentificador ou numero
                tokens.add(token);

                VALOR();
            }
        }
    }

    public void FOR(){
        token = anaLexi.nextToken(); tokens.add(token);
        if(token.getType() != Token.TKN_ABRE_PAR) // precisa ser um (
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "Abertura de parenteses no comando 'for' faltando!");
        }
        else
        {
            token = anaLexi.nextToken(); tokens.add(token);
            if(token.getType() == Token.TKN_TIPO)
                DECLARACAO();
            else if(token.getType() == Token.TKN_ID)
                IDENTIFICADOR();
            else
            {
                // laço para sincronizar tokens
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                throw new ErroSintatico(saveLinha, "Estrutura do laço 'for' mal feita! Variavel de indice esperada!");
            }

            ER();
            token = anaLexi.nextToken(); tokens.add(token); // precisa ler um ';' apos a condicao de parada do for
            if(token.getType() != Token.TKN_PONTO_PV)
            {
                // erro de ponto e virgula faltando na condicao de parada
                // sincroniza tokens
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                throw new ErroSintatico(saveLinha, "';' faltando na condicao de parada do comando 'for'");
            }
            else
            {
                // funcao Incremento, funcao com o mesmo comportamento de ATRIBUICAO
                // porem com objetivo mais especifico de verificar se está incrementando
                token = anaLexi.nextToken(); tokens.add(token); // espera um identificador

                if(token.getType() != Token.TKN_ID)
                {
                    //sincroniza tokens
                    int saveLinha = token.getLinha();
                    syncTokens();
                    //int saveLinha = token.getLinha();
                    throw new ErroSintatico(saveLinha, "Esperando identificador no incremento do for");
                }
                else
                {
                    // ultima condicao de incremento não precisa de ;

                    // agora se espera um )

                    token = anaLexi.nextToken(); tokens.add(token);
                    if(token.getType() != Token.TKN_FECHA_PAR)
                    {
                        // sincroniza tokens
                        int saveLinha = token.getLinha();
                        syncTokens();
                        //int saveLinha = token.getLinha();
                        throw new ErroSintatico(saveLinha, "Faltando fechar parenteses no comando 'for'");
                    }
                    else
                    {
                        token = anaLexi.nextToken(); tokens.add(token);
                        if(token.getType() != Token.TKN_ABRE_CHA) // precisa ser um {
                        {
                            // sincroniza tokens
                            int saveLinha = token.getLinha();
                            syncTokens();
                            //int saveLinha = token.getLinha();
                            throw new ErroSintatico(saveLinha, "Faltando abrir chaves no comando 'for'!");
                        }
                        else
                        {

                            P();

                            token = anaLexi.nextToken(); tokens.add(token);
                            if(token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
                            {
                                //sincroniza tokens
                                int saveLinha = token.getLinha();
                                syncTokens();
                                //int saveLinha = token.getLinha();
                                throw new ErroSintatico(saveLinha,"Faltando fechar chaves no comando 'for'!");
                            }
                            else
                                FIMESTRUTURA();

                        }
                    }
                }
            }
        }
    }

    public void WHILE(){
        token = anaLexi.nextToken(); tokens.add(token); // precisa ser um (
        if(token.getType() != Token.TKN_ABRE_PAR)
        {
            // laço para sincronizar os tokens
            int saveLinha = token.getLinha();
            syncTokens();
            throw new ErroSintatico(saveLinha, "Abertura de parenteses no comando 'while' faltando!");
        }
        else
        {
            ER(); // expressao relacional

            token = anaLexi.nextToken(); tokens.add(token); // precisa ser um )
            if(token.getType() != Token.TKN_FECHA_PAR)
            {
                int saveLinha = token.getLinha();
                syncTokens();
                throw new ErroSintatico(saveLinha, "Faltando fechar parenteses no comando 'while'!");
            }
            else
            {
                token = anaLexi.nextToken(); tokens.add(token);
                if(token.getType() != Token.TKN_ABRE_CHA) // precisa ser um {
                {
                    int saveLinha = token.getLinha();
                    syncTokens();
                    throw new ErroSintatico(saveLinha, "Faltando abrir chaves no comando 'while'!");
                }
                else
                {

                    P();

                    token = anaLexi.nextToken(); tokens.add(token);
                    if(token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
                    {
                        int saveLinha = token.getLinha();
                        syncTokens();
                        throw new ErroSintatico(saveLinha, "Faltando fechar chaves no comando 'while'!");
                    }
                    else
                        FIMESTRUTURA();

                }
            }
        }
    }

    public void FIMLINHA() // verifica tambem se irá ler outra linha de comando
    {
        //token = anaLexi.nextToken();
        if(declaracao_flag)
            declaracao_flag = false;

        if(token == null)
        {
            fim_programa =  true;
            throw new ErroSintatico(lastLinha, "Faltando ';'");
        }
        else
        {
            if(token.getType() != Token.TKN_PONTO_PV)
            {
                syncTokens();
                throw new ErroSintatico(token.getLinha(), "Faltando ';'");
            }
            else
            {
                // testar os tokens da frente para saber se tem mais linhas de comando
                //tempAnaLexi = anaLexi; // copia o abjeto
                //tempToken = tempAnaLexi.nextToken();
                // copiar objeto nao funcionou

                // Possivel solução 1: tentar criar uma nova instancia para p tempAnaLexi, e então copiar o objeto

                //Possivel solucao 2: salvar o 'pos' atual do anaLexi, andar com o anaLexi normalmente
                // e quando voltar, setar novamente o pos, com o pos salvo previamente
                // anaLexi.setPos(savePos);
                // e continua o programa

                int savePos = anaLexi.getPos(); // salva posição que está  referenete ao vetor de caracteres
                Token tempToken = anaLexi.nextToken(); // visualiza o proximo token
                anaLexi.setPos(savePos); // volta para a posição que estava antes

                if(tempToken != null && (tempToken.getType() == Token.TKN_ID || tempToken.getType() == Token.TKN_IF ||
                        tempToken.getType() == Token.TKN_FOR || tempToken.getType() == Token.TKN_WHILE ||
                        tempToken.getType() == Token.TKN_TIPO))
                    P();
            }
        }


    }

    public void FIMESTRUTURA()
    {
//        tempAnaLexi = anaLexi; // copia o abjeto
//        tempToken = tempAnaLexi.nextToken();

        int savePos = anaLexi.getPos(); // salva posição que está  referenete ao vetor de caracteres
        Token tempToken = anaLexi.nextToken(); // visualiza o proximo token
        anaLexi.setPos(savePos); // volta para a posição que estava antes

        if(tempToken != null && (tempToken.getType() == Token.TKN_ID || tempToken.getType() == Token.TKN_IF ||
                tempToken.getType() == Token.TKN_FOR || tempToken.getType() == Token.TKN_WHILE ||
                tempToken.getType() == Token.TKN_TIPO))
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

    public void syncTokens()
    {
        while(token != null && token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_ABRE_CHA && token.getType() != Token.TKN_FECHA_CHA)
            token = anaLexi.nextToken();
//        do
//        {
//            token = anaLexi.nextToken();
//        } while(token != null && token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_ABRE_CHA && token.getType() != Token.TKN_FECHA_CHA);
    }

    public List<Token> getTokens() {
        return tokens;
    }

}
