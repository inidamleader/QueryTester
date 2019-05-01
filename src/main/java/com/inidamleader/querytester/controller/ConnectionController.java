package com.inidamleader.querytester.controller;

import com.inidamleader.querytester.model.ConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ConnectionController {
    @FXML
    private TextField mTextFieldURL;
    @FXML
    private TextField mTextFieldHost;
    @FXML
    private TextField mTextFieldPort;
    @FXML
    private TextField mTextFieldDataBase;
    @FXML
    private TextField mTextFieldUser;
    @FXML
    private PasswordField mPasswordField;
    @FXML
    private CheckBox mCheckBoxRememberMe;

    @FXML
    private void initialize() {
        mTextFieldURL.setText(ConnectionManager.getUrl());
        mTextFieldHost.setText(ConnectionManager.getHost());
        mTextFieldPort.setText(ConnectionManager.getPort());
        mTextFieldDataBase.setText(ConnectionManager.getDatabase());
        mTextFieldUser.setText(ConnectionManager.getUser());
        mPasswordField.setText(ConnectionManager.getPassword());
        if (ConnectionManager.haveParameters()) {
            mCheckBoxRememberMe.setSelected(true);
        }
    }

    public void editConnectionParameters() {
        ConnectionManager.setUrl(mTextFieldURL.getText());
        ConnectionManager.setHost(mTextFieldHost.getText());
        ConnectionManager.setPort(mTextFieldPort.getText());
        ConnectionManager.setDatabase(mTextFieldDataBase.getText());
        ConnectionManager.setUser(mTextFieldUser.getText());
        ConnectionManager.setPassword(mPasswordField.getText());
        if (mCheckBoxRememberMe.isSelected())
            ConnectionManager.storeParameters();
        else
            ConnectionManager.deleteParameters();
    }
}
