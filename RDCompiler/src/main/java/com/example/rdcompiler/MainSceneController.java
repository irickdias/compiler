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
import javax.swing.JOptionPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {


    @FXML
    private TextArea txtAreaCode, txtAreaRow;


    @FXML
    private FlowPane flowPaneTokens, flowPaneErros, flowTeste;

    private int totRow = 1;
    //private List<String> reserved;
    private String term = "";
    private boolean success_flag = true;

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
        if(success_flag)
            compileSuccess();
        else
            compileFailure();

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

            for (Token token : tokens) {
                Text txt = new Text(token.toString());
                txt.setFont(new Font(14));
                txt.setFill(Color.GREEN);
                txt.setStyle("-fx-font-weight: bold");
                flowPaneTokens.getChildren().add(txt);
                //flowTeste.getChildren().add(txt);
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


    public void evtAbrir(ActionEvent event) {
    }

    public void evtSalvar(ActionEvent event) {
    }

    public void evtFechar(ActionEvent event) {
    }

    public void evtSave(ActionEvent event) {
    }

    public void evtSobre(ActionEvent event) {
    }
}
