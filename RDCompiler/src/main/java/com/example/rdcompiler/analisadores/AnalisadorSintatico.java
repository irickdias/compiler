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
    private List<Variable> variables = new ArrayList<Variable>();

    private boolean declaracao_flag = false;
    private boolean for_flag = false;
    private boolean fim_programa = false;
    private boolean chaves_aberta = false;
    private boolean if_flag = false;
    private boolean else_flag = false;

    private boolean erro_flag = false;

    private int linha;
    private int lastLinha;
    private int id_call_controll = 0;
    private int savePos;
    private int saveLinha;

    private Variable var;
    private String saveType;
    private List<ErroSemantico> errosSemanticos = new ArrayList<ErroSemantico>();

    private String saveValue = "";


    // Analisdaor Sinatico fica chamando o Analisador Lexico toda hora
    public AnalisadorSintatico(AnalisadorLexico al)
    {
        anaLexi = al;
    }

    public boolean isFim_programa() {
        return fim_programa;
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
                var = new Variable(); // instancia nova variavel
                variables.add(var); // adiciona na lista de variaveis
                saveType = token.getText();
                variables.get(variables.size()-1).setType(saveType); // seta o tipo que foi reconhecido pelo token
                variables.get(variables.size()-1).setRow(lastLinha);
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
                erro_flag = true;
                int saveLinha = token.getLinha();
                syncTokens();
                throw new ErroSintatico(saveLinha, "Inicio/caracter de novo comando errado!");
            }
        }



    }

    public boolean checkVariableExistence(String varName)
    {

        for(Variable v : variables)
        {
            if(v.getName().equals(varName))
                return true;
        }

        return false;
    }

    public void DECLARACAO() //int x = 0 ;
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
                if( !checkVariableExistence(token.getText()) )
                {
                    // nao existe essa variavel na lista, entao seta o nome
                    variables.get(variables.size()-1).setName(token.getText());
                    //IDENTIFICADOR();
                }
                else {
                    // exclui o ultimo objeto que adicionou
                    variables.remove(variables.size()-1);
                    errosSemanticos.add(new ErroSemantico(lastLinha, "Identificador '" + token.getText() + "' já está declarado!"));
                }
                IDENTIFICADOR();

                //FIMLINHA();
            }
            else
            {
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                erro_flag = true;
                throw new ErroSintatico(saveLinha, "Identificador esperado!");
            }
        }


    }

    public void searchVariableNSetUsed(String varName)
    {
        for(Variable v : variables)
        {
            if(v.getName().equals(varName))
                v.setUsed(true);

        }
    }

    public void IDENTIFICADOR()
    {
        int firstCall = id_call_controll;
        savePos = anaLexi.getPos();
        String identifier = token.getText();

        if(!declaracao_flag && token != null)
        {
            // verifica se está declarado
            if(!checkVariableExistence(token.getText()))
                errosSemanticos.add(new ErroSemantico(saveLinha, "Identificador '" + token.getText() + "' não foi declarado!"));
            else {
                searchVariableNSetUsed(token.getText());
            }
        }

        token = anaLexi.nextToken(); tokens.add(token);
        //saveLinha = token.getLinha();
        saveLinha = anaLexi.getLinha();

        /*if(for_flag && token.getType() == Token.TKN_ATRI)
            ATRIBUICAO_FOR();
        else */
        if(token != null && token.getType() == Token.TKN_ATRI)
            ATRIBUICAO();
        // tratar erro aqui

        if(declaracao_flag && token != null && token.getType() == Token.TKN_PONTO_V) // quer continuar declarando variavel
        {
            id_call_controll++;

            variables.get(variables.size()-1).setValue(saveValue);
            saveValue = ""; // reseta o saveValue

            // faz leitura do proximo token, que deverá ser um <identificador>
            // e chama recursivamente a função IDENTIFICADOR()
            token = anaLexi.nextToken(); tokens.add(token);

            if(token.getType() == Token.TKN_ID)
            {
                var = new Variable();
                variables.add(var);
                variables.get(variables.size()-1).setType(saveType);
                variables.get(variables.size()-1).setRow(saveLinha);
                if( !checkVariableExistence(token.getText()) )
                {
                    // nao existe essa variavel na lista, entao seta o nome
                    variables.get(variables.size()-1).setName(token.getText());
                }
                else
                {
                    // exclui o ultimo objeto que adicionou
                    variables.remove(variables.size()-1);
                    errosSemanticos.add(new ErroSemantico(saveLinha, "Identificador '" + token.getText() + "' já está declarado!"));
                }
                IDENTIFICADOR();
            }
            else
            {
                int saveLinha = token.getLinha();
                syncTokens();
                //int saveLinha = token.getLinha();
                erro_flag = true;
                throw new ErroSintatico(saveLinha,"Identificador esperado!");
            }

        }

        searchVariableNSetValue(identifier, saveValue);
        //variables.get(variables.size()-1).setValue(saveValue);
        saveValue = ""; // reseta o saveValue

        if(firstCall == 0 /*&& !for_flag*/)
        {
            id_call_controll = 0;
            FIMLINHA();
        }

    }

    public void searchVariableNSetValue(String varName, String value)
    {
        for(Variable v : variables)
        {
            if(v.getName().equals(varName))
                v.setValue(value);
        }
    }

    public void ATRIBUICAO(){
        token = anaLexi.nextToken(); tokens.add(token);

        // VALOR() fará reconhecimento se o token é <identificador ou numero>
        VALOR();
        OPERADOR_A();
//        if(token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_PONTO_PV && token.getType()  != Token.TKN_PONTO_V
//                /*&& token.getType()  != Token.TKN_FECHA_PAR*/)
//            ATRIBUICAO();

        if(token != null && token.getType() == Token.TKN_OPE_ARI)
        {
            saveValue += token.getText();
            ATRIBUICAO();
        }




    }

    public void ATRIBUICAO_FOR()
    {

        token = anaLexi.nextToken(); tokens.add(token);

        // VALOR() fará reconhecimento se o token é <identificador ou numero>
        VALOR();
        OPERADOR_A();
    }

    public void OPERADOR_A()
    {
        savePos = anaLexi.getPos();
        token = anaLexi.nextToken(); tokens.add(token);
        saveLinha = anaLexi.getLinha();
        /*if(for_flag && token.getType() != Token.TKN_OPE_ARI && token.getType() != Token.TKN_FECHA_PAR)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "Operador Aritmético não reconhecido!");
        }
        else*/
//        if(token.getType() != Token.TKN_OPE_ARI && token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_PONTO_V ||
//        )
//        {
//            int saveLinha = token.getLinha();
//            syncTokens();
//            //int saveLinha = token.getLinha();
//            throw new ErroSintatico(saveLinha, "Operador Aritmético não reconhecido ou ';' faltando!");
//        }

    }

    public void TIPO(){
        //token = anaLexi.nextToken();
//        if (token.getType() != Token.TKN_TIPO)
//            throw new ErroSintatico("Tipo da variavel esperada");
    }

    public void CONDICIONAL_IF(){
        if_flag = true;
        token = anaLexi.nextToken(); // precisa ser um (
        tokens.add(token);

        if(token.getType() != Token.TKN_ABRE_PAR)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            //erro_flag = true;
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
                    chaves_aberta = true;
                    P();

                    token = anaLexi.nextToken(); tokens.add(token);
                    // NAO PRECISA USAR TOKEN != NULL
                     //  USAR FLAGS DE ERROS EM IF, ELSE, WHILE E FOR
                    // FAZER A VERIFICACAO DENTRO DO 'FIMLINHA'
                    if(token != null && token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
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

                        if(erro_flag)
                            erro_flag = false;

                        if_flag = false;
                        chaves_aberta = false;
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
        else_flag = true;
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
                chaves_aberta = true;
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
                    if(erro_flag)
                        erro_flag = false;

                    else_flag = false;
                    chaves_aberta = false;
                    FIMESTRUTURA();
                }

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

//    public int getVariableValue(String target)
//    {
//        for(Variable v : variables)
//        {

    public List<ErroSemantico> getErrosSemanticos() {
        return errosSemanticos;
    }
//            if(v.getName().equals(target))
//                return  v.getValue();
//        }
//
//        return -999;
//    }

    public void VALOR(){
        //token = anaLexi.nextToken();
        if(token.getType() != Token.TKN_ID && token.getType() != Token.TKN_NUM)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "identificador ou numero esperado!");
        }
        else { // é identificador ou numero

            if(token.getType() == Token.TKN_ID)
            {
                if(!checkVariableExistence(token.getText()))
                    errosSemanticos.add(new ErroSemantico(saveLinha, "Identificador '" + token.getText() + "' não foi declarado!"));
                else {
                    searchVariableNSetUsed(token.getText());
                }
            }

            saveValue += token.getText();

        }

//        if(token.getType() != Token.TKN_NUM)
//            variables.get(variables.size()-1).setValue(Integer.parseInt(token.getText()));
//        else {
//            int value = getVariableValue(token.getText());
//            if(value != -999)
//                variables.get(variables.size()-1).setValue(value);
//            else { // nao existe declarado essa variavel
//                //syncTokens();
//                errosSemanticos.add(new ErroSemantico(saveLinha, "Variavel '" + token.getText() + "' não foi declarada!"));
//            }
//        }


    }

    public void EA(){

    }

    public void checkVariableInicialization(String varName)
    {
        for(Variable v : variables)
        {
            if(v.getName().equals(varName))
            {
                if(v.getValue().equals(""))
                    errosSemanticos.add(new ErroSemantico(saveLinha, "Variavel '" + varName + "' não foi inicializada antes desta estrutura!"));
            }
        }
    }

    public void ER(){
        token = anaLexi.nextToken(); // precisa de um identificador
        tokens.add(token);
        saveLinha = token.getLinha();

        if(token.getType() != Token.TKN_ID)
        {
            int saveLinha = token.getLinha();
            syncTokens();
            //int saveLinha = token.getLinha();
            throw new ErroSintatico(saveLinha, "Faltando identificador");
        }
        else
        {
            searchVariableNSetUsed(token.getText());
            checkVariableInicialization(token.getText());
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
            for_flag = true;
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
            for_flag = false;

//            token = anaLexi.nextToken(); tokens.add(token);
//            if(token != null && token.getType() != Token.TKN_PONTO_PV)
//            {
//                int saveLinha = token.getLinha();
//                syncTokens();
//                //int saveLinha = token.getLinha();
//                throw new ErroSintatico(saveLinha, "';' faltando na variavel de índice do comando 'for'");
//            }

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

//                    for_flag = true;
//                    IDENTIFICADOR();
//                    for_flag = false;


                    // agora se espera um )
                    searchVariableNSetUsed(token.getText());

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
                            chaves_aberta = true;
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
                            {
                                chaves_aberta = false;
                                FIMESTRUTURA();
                            }


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
                    chaves_aberta = true;
                    P();

                    token = anaLexi.nextToken(); tokens.add(token);
                    if(token != null && token.getType() != Token.TKN_FECHA_CHA) // precisa ser um }
                    {
                        int saveLinha = token.getLinha();
                        //anaLexi.setTemporario(true);
                        syncTokens();
                        //anaLexi.setTemporario(false);
                        throw new ErroSintatico(saveLinha, "Faltando fechar chaves no comando 'while'!");
                    }
                    else
                    {
                        chaves_aberta = false;
                        FIMESTRUTURA();
                    }


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

            if(token.getType() != Token.TKN_PONTO_PV && token.getType() == Token.TKN_NUM)
            {
                syncTokens();
                // linha em que estava o penultimo token reconhecido
                erro_flag = true;
                throw new ErroSintatico(token.getLinha(), "Faltando '=' depois do identificador");
            }
            else if(token.getType() != Token.TKN_PONTO_PV && (token.getType() == Token.TKN_ID ||
                    token.getType() == Token.TKN_IF || token.getType() == Token.TKN_WHILE ||
                    token.getType() == Token.TKN_FOR))
            {
                anaLexi.setPos(savePos);
                tokens.remove(tokens.size()-1);
                int last = tokens.get(tokens.size()-1).getLinha();
                anaLexi.setLinha(last+1);

                erro_flag = true;

                throw new ErroSintatico(last, "Faltando ';'");
            }
            else if(token.getType() != Token.TKN_PONTO_PV)
            {
                syncTokens();
                //int li = tokens.get(tokens.size()-1).getLinha();

                // size == 3, então penultimo é -2, já que os index estão em 0 1 2 ...
                int li = tokens.get(tokens.size()-2).getLinha();
                erro_flag = true;

                throw new ErroSintatico(li, "Faltando ';'");
            }
            else
            {

                int savePos = anaLexi.getPos(); // salva posição que está  referenete ao vetor de caracteres
                anaLexi.setTemporario(true);
                Token tempToken = anaLexi.nextToken(); // visualiza o proximo token
                anaLexi.setPos(savePos); // volta para a posição que estava antes
                anaLexi.setTemporario(false);

                if(tempToken != null && !for_flag && (tempToken.getType() == Token.TKN_ID || tempToken.getType() == Token.TKN_IF ||
                        tempToken.getType() == Token.TKN_FOR || tempToken.getType() == Token.TKN_WHILE ||
                        tempToken.getType() == Token.TKN_TIPO))
                    P();
                else if (tempToken != null && !if_flag && !else_flag && tempToken.getType() == Token.TKN_FECHA_CHA)
                    token = anaLexi.nextToken();
                else if(tempToken != null && tempToken.getType() == Token.TKN_FECHA_CHA)
                {
                    // funciona para o example 1
                    //syncTokens();

                    // funciona para o example 2
                    //anaLexi. setPos(savePos);

                    //POSSIVEL SOLUÇÃO - FAZER UM FLAG DE ERRO NO IF, SE TIVER DADO ERRO NAO IRÁ SEGUIR PELO FLUXO CERTO
                    // ENTÃO NÃO CHAMARÁ syncTokens
                    // SE TIVER DADO ERRO EM ALGUMA ESTRUTURA, APENAS VOLTARÁ A POSIÇÃO ORIGINAL ANTES DE LER O TEMPTOKEN

                    if(erro_flag)
                        syncTokens();
                    else
                    {
                        anaLexi.setPos(savePos);
                    }

                }
                else if (tempToken == null && chaves_aberta)
                    throw new ErroSintatico(MainSceneController.totRow, "Faltando fechar chaves!");
            }
        }


    }

    public void FIMESTRUTURA()
    {
//        tempAnaLexi = anaLexi; // copia o abjeto
//        tempToken = tempAnaLexi.nextToken();

        int savePos = anaLexi.getPos(); // salva posição que está  referenete ao vetor de caracteres
        anaLexi.setTemporario(true);
        Token tempToken = anaLexi.nextToken(); // visualiza o proximo token
        anaLexi.setPos(savePos); // volta para a posição que estava antes
        anaLexi.setTemporario(false);

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
        // melhorar lógica de sincronização
        while(token != null && token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_ABRE_CHA && token.getType() != Token.TKN_FECHA_CHA)
            token = anaLexi.nextToken();
//        do
//        {
//            token = anaLexi.nextToken();
//        } while(token != null && token.getType() != Token.TKN_PONTO_PV && token.getType() != Token.TKN_ABRE_CHA && token.getType() != Token.TKN_FECHA_CHA);

        if (token != null && token.getType() == Token.TKN_PONTO_PV)
        {
            int savePos = anaLexi.getPos(); // salva posição que está  referenete ao vetor de caracteres
            anaLexi.setTemporario(true);
            Token tempToken = anaLexi.nextToken(); // visualiza o proximo token
            //anaLexi.setPos(savePos); // volta para a posição que estava antes
            //anaLexi.setTemporario(false);

            if (tempToken != null && tempToken.getType() == Token.TKN_FECHA_CHA)
            {
                if(erro_flag)
                    erro_flag = false;

                if(!chaves_aberta)
                {
                    throw new ErroSintatico(tempToken.getLinha(), "Fecha-chaves avulso no código, verifique a estutura do comando!");
                }
                else
                {
                    chaves_aberta = false;
                }

            }
            else
            {
                anaLexi.setPos(savePos); // volta para a posição que estava antes
                anaLexi.setTemporario(false);
            }
        }
        // verificar se em em pontosVirgulas, se o proximo for '}', faz um flag de chaves abertas
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Variable> getVariables() {
        return variables;
    }
}
