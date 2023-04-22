package com.example.rdcompiler.analisadores;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnalisadorLexico {
    private char[] content;
    //private String content;
    private int estado;
    private int pos = 0;
    private List<String> reserved;
    private List<String> tipos;
    private int linha = 1;
    private boolean temporario = false;


    public AnalisadorLexico(String code)
    {
        try {
            //String txtConteudo;
            //txtConteudo = Files.readString(Paths.get(filename));
            //content = txtConteudo.toCharArray();
            content = code.toCharArray();
            reserved = new ArrayList<String>();
            reserved.add("int");
            reserved.add("float");
            reserved.add("double");
            reserved.add("char");
            reserved.add("string");
            reserved.add("if");
            reserved.add("while");
            reserved.add("for");

            tipos = new ArrayList<String>();
            tipos.add("int");
            tipos.add("double");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }


    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private boolean isChar(char c)
    {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isOperatorR(char c)
    {
        return c == '>' || c == '<' || c == '!';
    }
//    private boolean isOperatorR(char c)
//    {
//        return c == '>' || c == '<' || c == '=' || c == '!';
//    }

    private boolean isOperatorA(char c)
    {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean isSpace(char c)
    {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private boolean isDotComma(char c)
    {
        return c == ';';
    }

    private boolean isComma(char c)
    {
        return c == ',';
    }

    private boolean isKeyOpener(char c)
    {
        return c == '{';
    }

    private boolean isKeyCloser(char c)
    {
        return c == '}';
    }

    private boolean isParOpener(char c)
    {
        return c == '(';
    }

    private boolean isParCloser(char c)
    {
        return c == ')';
    }

    private boolean isEqualSign(char c) {return c == '=';}



    private char nextChar()
    {
        if(eof())
            return '\0';

        return content[pos++];
    }

    public int getPos()
    {
        return pos;
    }

    public void setPos(int p)
    {
        this.pos = p;
    }

    public boolean isTemporario() {
        return temporario;
    }

    public void setTemporario(boolean temporario) {
        this.temporario = temporario;
    }

    public Token nextToken() // automato do analizador léxico
    {
        char current;
        Token token;
        String term = "";
        char pontuacao = 0;
        //int linha = 1;
        int ope_rel_count = 0;

        if(eof())
            return null;


        estado = 0; // Estado inicial do automato é 0
        while(!eof() || estado != 0)
        {

            current = nextChar();
            //'\u0000' = fim de linha
//            if(current == '\n')
//            {
//
//                linha++;
//            }


            switch(estado)
            {
                case 0: // estando em 0  e ler...
                    if (current == '\u0000')
                        return null;
                    else
                    {
                        if (isChar(current)) // caracter, vai para o estado 1
                        {
                            estado = 1;
                            term += current;
                        }
                        else if (isDigit(current)) // dígito, vai para o estado 3
                        {
                            estado = 3;
                            term += current;
                        }
                        else if (isSpace(current)) // algum tipo de espaço (\n \t \r ou espaço normal), permanece em 0
                        {
                            estado = 0;
                            if(!isTemporario() && current=='\n')
                                linha++;
                        }
                        else if (isOperatorR(current))
                        {
                            estado = 5;
                            term += current;
                            ope_rel_count++;
                        }
                        else
                        if(isDotComma(current) || isComma(current) || isEqualSign(current))
                        {
                            //backChar();
                            pontuacao = current;
                            estado = 6;
                        }
                        else
                        if(isOperatorA(current))
                        {
                            term += current;
                            estado = 7;
                        }
                        else
                        if(isParOpener(current) || isParCloser(current) || isKeyOpener(current) || isKeyCloser(current))
                        {
                            term += current;
                            estado = 8;
                        }
                        else
                        {
                            syncChar();
                            throw new ErroLexico(linha,"SÍMBOLO DESCONHECIDO!");
                        }
                    }


                    break;

                case 1: // estando em 1 e ler...  Estado 1 é reconhecedor de identificadores
                    if (isChar(current) || isDigit(current)) // caracter ou dígito, permanece em 1
                    {
                        estado = 1;
                        term += current;
                    }
                    else if(isSpace(current) || isOperatorR(current) || current == '\0' || isEqualSign(current) ||
                            isDotComma(current) || isComma(current) || isParOpener(current) || isParCloser(current) )
                    {
                        //estado = 2;
                        if(current != '\u0000' && current!='\n')
                            backChar();

                        // depois de atribuir a linha atual para o token

                        token = new Token();
                        token.setText(term);
                        token.setLinha(linha);

                        if(!isTemporario() && current == '\n')
                            linha++;

                        if(tipos.contains(term))
                        {

                            token.setType(Token.TKN_TIPO);
                        }
                        else
                        if(term.equals("if"))
                            token.setType(Token.TKN_IF);
                        else
                        if(term.equals("else"))
                            token.setType(Token.TKN_ELSE);
                        else
                        if (term.equals("while"))
                            token.setType(Token.TKN_WHILE);
                        else
                        if (term.equals("for"))
                            token.setType(Token.TKN_FOR);
                        else
                            token.setType(Token.TKN_ID);


                        return token;
                    }
                    else
                    {
                        syncChar();
                        throw new ErroLexico(linha,"IDENTIFICADOR MAL FORMADO");
                    }


                    break;

                case 2: // estado 2 é estado final
//                    if(!eof())
//                        backChar(); // volta uma posição, depois de ler \n
//                    else
//                        if(eof() && (isDotComma(current)))
                    if(current != '\u0000' && current!='\n')
                        backChar();

                    token = new Token();
                    token.setLinha(linha);
                    token.setText(term);
                    if(tipos.contains(term))
                    {

                        token.setType(Token.TKN_TIPO);
                    }
                    else
                        token.setType(Token.TKN_ID);


                    return token;

                case 3: // estando em 3 e ler...  // reconhecedor de numeros
                    if(isDigit(current))
                    {
                        estado = 3;
                        term += current;
                    }
                    else if( isChar(current))
                    {
                        //estado = 4;
                        syncChar();
                        throw new ErroLexico(linha,"NÚMERO DESCONHECIDO!");
                    }
                    else
                    //if(isSpace(current) || isComma(current) || isDotComma(current))
                    {
                        if(current!='\u0000' && current!='\n')
                            backChar();

                        token = new Token();
                        token.setLinha(linha);

                        if(!isTemporario() && current == '\n')
                            linha++;

                        token.setType(Token.TKN_NUM);
                        token.setText(term);
                        return token;
                    }

                    break;

                case 4: // estado 4 é estado final
                    if(!eof())
                        backChar();
                    token = new Token();
                    token.setType(Token.TKN_NUM);
                    token.setText(term);
                    return token;

                case 5:
                    //term += current;

                    if(current != '\u0000' && current!='\n')
                        backChar();

                    if(isOperatorR(current) && ope_rel_count < 3)
                    {
                        term += current;
                        ope_rel_count++;
                    }
                    else if(isOperatorR(current) && ope_rel_count == 3)
                    {
                        syncChar();
                        throw new ErroLexico(linha, "Operador relacional desconhecido!");
                    }
                    else
                    {
                        token = new Token();
                        token.setLinha(linha);

                        if(!isTemporario() && current == '\n')
                            linha++;

                        token.setType(Token.TKN_OPE_REL);
                        token.setText(term);
                        return token;
                    }
                    break;

                case 6:
                    token = new Token();
                    token.setLinha(linha);

                    if(!isTemporario() && current == '\n')
                        linha++;

                    if(isDotComma(pontuacao))
                    {
                        token.setType(Token.TKN_PONTO_PV);
                        token.setText("" + pontuacao);

                    }
                    else
                    if (isComma(pontuacao))
                    {
                        backChar();
                        token.setType(Token.TKN_PONTO_V);
                        token.setText("" + pontuacao);
                    }
                    else
                    if(isEqualSign(pontuacao))
                    {
                        backChar();
                        token.setType(Token.TKN_ATRI);
                        token.setText("" + pontuacao);
                    }
                    return token;

                case 7:
                    // backchar();
                    token = new Token();
                    token.setLinha(linha);
                    token.setType(Token.TKN_OPE_ARI);
                    token.setText(term);

                    if(!isTemporario() && current == '\n')
                        linha++;

                    return token;

                case 8:
                    if(current != '\u0000' && current!='\n')
                        backChar();

                    token = new Token();
                    token.setLinha(linha);

                    if(!isTemporario() && current == '\n')
                        linha++;

                    //token.setType(Token.TKN_OPE_ARI);
                    token.setText(term);
                    if(isParOpener(term.charAt(0)))
                        token.setType(Token.TKN_ABRE_PAR);
                    else
                    if(isParCloser(term.charAt(0)))
                        token.setType(Token.TKN_FECHA_PAR);
                    else
                    if(isKeyOpener(term.charAt(0)))
                        token.setType(Token.TKN_ABRE_CHA);
                    else
                        token.setType(Token.TKN_FECHA_CHA);

                    return token;

            }
        }

        return null;
    }

    private void backChar() // volta uma posição
    {
        pos--;
    }

    private boolean eof()
    {
        return pos == content.length;
    }

    private void syncChar()
    {
        char sync;
        //String term = "";
        do
        {
            sync = nextChar();
            //term += sync;

//            if(sync == ' ')
//                term = "";

            if (sync == '\n')
                linha++;

        } while(!isDotComma(sync) && sync != '\n');
    }
}
