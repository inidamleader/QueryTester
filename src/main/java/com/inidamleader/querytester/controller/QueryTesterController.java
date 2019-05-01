package com.inidamleader.querytester.controller;

import com.inidamleader.querytester.QueryTesterApplication;
import com.inidamleader.querytester.model.ConnectionManager;
import com.inidamleader.querytester.model.exceptions.ConnectionNullPointerException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class QueryTesterController {
	@FXML
	private Button mConnectedDisconnectedButton;
	@FXML
	private Button mStartLoadingButton;
	@FXML
	private TextArea mTextArea;
	@FXML
	private TableView mTableView;
	@FXML
	private Label mExecutionTimeAndResultLabel;
	private Connection mConnection;
	private ImageView mConnectedImageView;
	private ImageView mDisconnectedImageView;
	private ImageView mStartImageView;
	private ImageView mLoadingImageView;
	private String mLastSelectQueryString;
	private boolean mIsButtonClicked = false;

	@FXML
	private void initialize() {
		mLoadingImageView = new ImageView(QueryTesterApplication.class
				.getResource("view/assets/images/loading.gif").toString());
		mLoadingImageView.setPreserveRatio(true);
		mLoadingImageView.setFitWidth(30);

		mStartImageView = new ImageView(QueryTesterApplication.class
				.getResource("view/assets/images/start.png").toString());
		mStartImageView.setPreserveRatio(true);
		mStartImageView.setFitWidth(30);

		mDisconnectedImageView = new ImageView(QueryTesterApplication.class
				.getResource("view/assets/images/disconnected.png").toString());
		mDisconnectedImageView.setPreserveRatio(true);
		mDisconnectedImageView.setFitWidth(30);

		mConnectedImageView = new ImageView(QueryTesterApplication.class
				.getResource("view/assets/images/connected.png").toString());
		mConnectedImageView.setPreserveRatio(true);
		mConnectedImageView.setFitWidth(30);

		mStartLoadingButton.setGraphic(mStartImageView);
		mStartLoadingButton.setDisable(true);
		mConnectedDisconnectedButton.setGraphic(mDisconnectedImageView);

		mTextArea.setText("Select * From VEHICULE");
		mTextArea.textProperty().addListener((pObservableValue, pS, pT1) -> {
			if (pT1.trim().length() != 0 && mConnection != null)
				mStartLoadingButton.setDisable(false);
			else mStartLoadingButton.setDisable(true);
		});
	}

	@FXML
	private void onButtonClicked() {
		if (!mIsButtonClicked) {
			mIsButtonClicked = true;
			executeQuery();
		} else {
			showExceptionAlert(new Exception("You have to wait for the current query to be executed!"));
		}
	}

	private void executeQuery() {
		mStartLoadingButton.setGraphic(mLoadingImageView);
		new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException pE) {
				pE.printStackTrace();
			}
			try {
				// todo GUI updates
				LocalTime lStartLocalTime = LocalTime.now();

				if (mConnection == null)
					throw new ConnectionNullPointerException("You’re not connected, press connect button !");

				String lQueryString = mTextArea.getText().trim();
				String lResultMessage;
				if (lQueryString.toUpperCase().startsWith("SELECT"))
					lResultMessage = "- Number of found records: " + executeSelectQuery(lQueryString);
				else if (lQueryString.toUpperCase().startsWith("INSERT") || lQueryString.toUpperCase().startsWith("UPDATE") || lQueryString.toUpperCase().startsWith("DELETE"))
					lResultMessage = "- Number of updated records: " + executeUpdateQuery(lQueryString);
				else lResultMessage = "- Number of records: " + executeQuery(lQueryString);


				// Set the bottom GUI message
				long l = lStartLocalTime.until(LocalTime.now(), ChronoUnit.MILLIS);
				String lExecutionTimeMessage = "Execution time: " + l / 1000.0d + "s";

				mTextArea.setText(lQueryString);
				String finalLResultMessage = lResultMessage;
				Platform.runLater(() -> mExecutionTimeAndResultLabel.setText(lExecutionTimeMessage + finalLResultMessage));
			} catch (ConnectionNullPointerException | SQLException pE) {
				Platform.runLater(() -> showExceptionAlert(pE));
			} finally {
				mIsButtonClicked = false;
				Platform.runLater(() -> mStartLoadingButton.setGraphic(mStartImageView));
			}
		}).start();
	}

	private int executeUpdateQuery(String pQueryString) throws SQLException {
		try (PreparedStatement lPreparedStatement = mConnection.prepareStatement(pQueryString)) {
			int lResultInt;
			lResultInt = lPreparedStatement.executeUpdate();
			if (mLastSelectQueryString != null)
				executeSelectQuery(mLastSelectQueryString);
			return lResultInt;
		}
	}

	private boolean executeQuery(String pQueryString) throws SQLException {
		try (PreparedStatement lPreparedStatement = mConnection.prepareStatement(pQueryString)) {
			boolean lB = lPreparedStatement.execute();
			if (mLastSelectQueryString != null)
				executeSelectQuery(mLastSelectQueryString);
			return lB;
		}
	}

	private int executeSelectQuery(String pQueryString) throws SQLException {
		try (PreparedStatement lPreparedStatement = mConnection.prepareStatement(pQueryString); ResultSet lResultSet = lPreparedStatement.executeQuery()) {
			int lResultInt = 0;
			ResultSetMetaData lResultSetMetaData = lResultSet.getMetaData();

			int lColumnCount = lResultSetMetaData.getColumnCount();
			ObservableList lColumns = mTableView.getColumns();

			Platform.runLater(() -> {
				// We remove previous columns on mTableView for the next request
				mTableView.getColumns().removeAll(mTableView.getColumns());
			});
			// Fill table head
			for (int i = 1; i < lColumnCount + 1; i++) {
				TableColumn lTableColumn = new TableColumn(lResultSetMetaData.getColumnName(i));
				Platform.runLater(() -> lColumns.add(lTableColumn));
				int j = i - 1;
				lTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
					@Override
					public ObservableValue call(TableColumn.CellDataFeatures pCellDataFeatures) {
						return new SimpleStringProperty(((ObservableList) pCellDataFeatures.getValue()).get(j).toString());
					}
				});
			}

			// fill data
			ObservableList<ObservableList> data = FXCollections.observableArrayList();
			while (lResultSet.next()) {
				lResultInt++;
				ObservableList row = FXCollections.observableArrayList();
				for (int i = 1; i < lColumnCount + 1; i++) {
					//Iterate Column
					row.add(lResultSet.getObject(i));
				}
				data.add(row);
			}
			mTableView.setItems(data);
			mLastSelectQueryString = pQueryString;
			return lResultInt;
		}
	}

	private void showExceptionAlert(Exception pE) {
		Dialog lAlert = new Alert(Alert.AlertType.ERROR);
		lAlert.setTitle("Error");
		lAlert.setHeaderText(pE.getClass().toString());
		lAlert.setContentText(pE.getMessage());
		lAlert.setResizable(true);
		lAlert.showAndWait();
	}

	@FXML
	private void showEditConnectionParametersDialog() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(QueryTesterApplication.class.getResource("view/ConnectionView.fxml"));
			Parent root = loader.load();
			ConnectionController lConnectionController = loader.getController();
			Dialog lDialog = new Dialog();
			lDialog.setTitle("Connection");
			lDialog.setHeaderText("Edit Connection :");
			lDialog.getDialogPane().setContent(root);
			ButtonType lButtonType = ButtonType.APPLY;
			lDialog.getDialogPane().getButtonTypes().setAll(lButtonType, ButtonType.CANCEL);
			lDialog.setResultConverter((Callback) pO -> {
				if (pO == lDialog.getDialogPane().getButtonTypes().get(0))
					lConnectionController.editConnectionParameters();
				return null;
			});
			lDialog.showAndWait();
		} catch (IOException pE) {
			pE.printStackTrace();
		}
	}

	@FXML
	private void connect() {
		try {
			if (mConnection == null) {
				mConnection = ConnectionManager.getConnection();
				mConnectedDisconnectedButton.setGraphic(mConnectedImageView);
				if (mTextArea.getText().trim().length() != 0)
					mStartLoadingButton.setDisable(false);
			} else {
				mConnection = null;
				mConnectedDisconnectedButton.setGraphic(mDisconnectedImageView);
				mStartLoadingButton.setDisable(true);
			}
		} catch (SQLException pE) {
			showExceptionAlert(pE);
		}
	}

	@FXML
	private void close() {
		QueryTesterApplication.getPrimaryStage().close();
	}

	@FXML
	private void about() {
		Dialog lAlert = new Alert(Alert.AlertType.INFORMATION);
		lAlert.setTitle("About");
		lAlert.setHeaderText("About Query Tester Application");
		lAlert.setContentText("Author : Réda El Madini");
		lAlert.showAndWait();
	}
}
