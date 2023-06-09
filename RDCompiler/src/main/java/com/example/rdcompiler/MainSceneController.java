package com.example.rdcompiler;

import com.example.rdcompiler.analisadores.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import javax.swing.JOptionPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {


    @FXML
    private TextArea txtAreaCode, txtAreaRow;


    @FXML
    private FlowPane flowPaneTokens, flowPaneErros, flowTeste;

    public static int totRow = 1;
    //private List<String> reserved;
    private String term = "";
    private boolean success_flag = true;
    private boolean warning_flag = false;

    public MainSceneController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // inicializa lista que guarda tokens reservados
//        reserved = new ArrayList<String>();
//        reserved.add("int");
//        reserved.add("float");
//        reserved.add("double");
//        reserved.add("char");
//        reserved.add("string");
//        reserved.add("if");
//        reserved.add("while");

        txtAreaCode.setText("");
    }

    @FXML
    public void evtKeyReleased(KeyEvent event)
    {
        ObservableList<CharSequence> list = txtAreaCode.getParagraphs();
        int rowCount = list.size();
        //txtAreaRow.setText(""+rowCount);

        term += event.getText();
        if(rowCount > totRow )
        {
            //String s = lbRow.getText();

            txtAreaRow.setText(txtAreaRow.getText() + "\n" + rowCount);
            //txtAreaCode.setPromptText(txtAreaCode.getPromptText() + "\n" + rowCount);
            totRow++;
        }

//        if(reserved.contains(term))
//        {
//            String word = "";
//            String code = txtAreaCode.getText();
//
//            for (int i = code.length()-1; i > 0 && code.charAt(i) != ' ' ; i--) {
//                word = code.charAt(i) + word;
//                txtAreaCode.deletePreviousChar();
//            }
//
//            txtAreaCode.setStyle("-fx-text-fill: blue ;");
//            txtAreaCode.setText(txtAreaCode.getText() + word);
//            //txtAreaCode.setS
//            term = "";
//        }

    }

    @FXML
    public void evtSave()
    {

    }

    @FXML
    public void evtCompile(ActionEvent event)
    {
        String code = txtAreaCode.getText();

        // Limpa os FlowPane mostrando os tokens e erros
        flowPaneTokens.getChildren().clear();
        flowPaneErros.getChildren().clear();


        //String token = "";
        analisadorSintatico(code);
//        if(warning_flag)
//            compileWarning();

        if(!success_flag)
            compileFailure();
        else
        if(success_flag && warning_flag)
            compileWarning();
        else
            compileSuccess();



    }

    public void analisadorSintatico(String code)
    {
        try
        {
            AnalisadorLexico al =  new AnalisadorLexico(code);
            AnalisadorSintatico as = new AnalisadorSintatico(al);
            //Token token = null;

            do
            {
                try{
                    as.P();
                    //token = al.nextToken();

//                    if(token != null)
//                    {
//                        System.out.println(token);
//                        Text txt = new Text(token.toString());
//                        txt.setFont(new Font(12));
//                        txt.setFill(Color.GREEN);
//                        flowPaneTokens.getChildren().add(txt);
//                    }
                }
                catch(ErroLexico error)
                {
                    //System.out.println("ERRO LEXICO: " + error.getMsg());
                    success_flag = false;
                    Text txt = new Text("ERRO LEXICO na linha " +error.getLinha() + ": " + error.getMsg());
                    txt.setFont(new Font(14));
                    txt.setFill(Color.RED);
                    txt.setStyle("-fx-font-weight: bold");
                    flowPaneErros.getChildren().add(txt);
                    //as.syncTokens();
                }
                catch (ErroSintatico errorS)
                {
                    success_flag = false;
                    Text txt = new Text("ERRO SINTATICO na linha " + errorS.getLinha() + ": " + errorS.getMsg());
                    txt.setFont(new Font(14));
                    txt.setFill(Color.RED);
                    txt.setStyle("-fx-font-weight: bold");
                    flowPaneErros.getChildren().add(txt);
                }

            } while (!as.isFim_programa());

            // mostra tokens na tela
            List<Token> tokens = as.getTokens();

            System.out.println(tokens.size());

            for (Token token : tokens) {
                if(token != null)
                {
                    Text txt = new Text(token.toString());
                    txt.setFont(new Font(14));
                    txt.setFill(Color.GREEN);
                    txt.setStyle("-fx-font-weight: bold");
                    flowPaneTokens.getChildren().add(txt);
                    //flowTeste.getChildren().add(txt);
                }

            }

//            for (Token token : tokens) {
//                Text txt = new Text(token.toString());
//                txt.setFont(new Font(14));
//                txt.setFill(Color.GREEN);
//                txt.setStyle("-fx-font-weight: bold");
//                flowPaneTokens.getChildren().add(txt);
//                //flowTeste.getChildren().add(txt);
//            }

            List<ErroSemantico> erroS = as.getErrosSemanticos();
            if(erroS.size() > 0)
            {
                for(ErroSemantico es: erroS)
                {
                    if(es != null)
                    {
                        success_flag = false;
                        Text txt = new Text("ERRO SEMANTICO na linha " + es.getLinha() + ": " + es.getMsg());
                        txt.setFont(new Font(14));
                        txt.setFill(Color.RED);
                        txt.setStyle("-fx-font-weight: bold");
                        flowPaneErros.getChildren().add(txt);
                    }

                }
            }

            List<Variable> vars = as.getVariables();
            if(vars.size() > 0)
            {
                for(Variable v : vars)
                {
                    if(v != null)
                    {
                        if(v.getValue().equals("")) // não foi iniciado
                        {
                            //success_flag = false;
                            warning_flag = true;
                            Text txt = new Text("ERRO SEMANTICO na linha " + v.getRow() + ": variavel '" + v.getName() + "' não foi inicializada!");
                            txt.setFont(new Font(14));
                            txt.setFill(Color.ORANGE);
                            txt.setStyle("-fx-font-weight: bold");
                            flowPaneErros.getChildren().add(txt);
                        }

                        if(!v.isUsed())
                        {
                            //success_flag = false;
                            warning_flag = true;
                            Text txt = new Text("ERRO SEMANTICO: variavel '" + v.getName() + "' declarada na linha " + v.getRow() + " nunca foi utilizada!");
                            txt.setFont(new Font(14));
                            txt.setFill(Color.ORANGE);
                            txt.setStyle("-fx-font-weight: bold");
                            flowPaneErros.getChildren().add(txt);
                        }
                    }
                }
            }



        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void compileSuccess()
    {
        JOptionPane.showMessageDialog(null, "Código compilado com Sucesso!", "SUCCESS", JOptionPane.INFORMATION_MESSAGE);

    }

    public void compileFailure()
    {
        JOptionPane.showMessageDialog(null, "Código compilado com Erros!", "ERROR", JOptionPane.ERROR_MESSAGE);
        success_flag = true;
    }

    public void compileWarning()
    {
        JOptionPane.showMessageDialog(null, "Código compilado com Avisos!", "WARNING", JOptionPane.WARNING_MESSAGE);
        success_flag = true;
        warning_flag = false;
    }

    @FXML
    private void evtClear()
    {
//        JOptionPane.showMessageDialog(null,
//                "Limpar código");
        if(JOptionPane.showConfirmDialog(null, "Deseja realmente limpar todo o código?")==JOptionPane.YES_OPTION)
        {
            txtAreaCode.setText("");
            totRow = 1;
            txtAreaRow.setText("1");
            //Platform.exit();
        }
    }


    public void evtAbrir(ActionEvent event) throws IOException {
        txtAreaRow.setText("1");
        FileChooser fileChooser=new FileChooser();
        fileChooser.setInitialDirectory(new File("..\\"));
        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("All Files", "*.*"),
//                new FileChooser.ExtensionFilter("JPEG Files", "*.jpeg"),
//                new FileChooser.ExtensionFilter("JPG Files", "*.jpg"),
//                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
//                new FileChooser.ExtensionFilter("GIF Files", "*.gif"));
                new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
        File file=fileChooser.showOpenDialog(null);

        if (file!=null)
        {
            byte[] content = Files.readAllBytes(Paths.get(file.toURI()));
            txtAreaCode.setText(new String(content));

        }

        for (int i = 0; i < txtAreaCode.getParagraphs().size()-1 ; i++) {
            txtAreaRow.setText(txtAreaRow.getText() + "\n" + (i+2));
        }
        //txtAreaRow.setText(txtAreaRow.getText() + "\n" + rowCount);
    }

    public void evtSalvar(ActionEvent event) {
    }

    public void evtFechar(ActionEvent event) {
        if(JOptionPane.showConfirmDialog(null, "Deseja realmente fechar o arquivo?")==JOptionPane.YES_OPTION)
        {
            txtAreaCode.setText("");
            totRow = 1;
            txtAreaRow.setText("1");
            //Platform.exit();
        }
    }

    public void evtSave(ActionEvent event) {
    }

    public void evtSobre(ActionEvent event) {
    }
}
