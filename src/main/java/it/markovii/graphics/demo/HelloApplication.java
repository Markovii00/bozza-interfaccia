package it.markovii.graphics.demo;

import it.markovii.framework.HttpRequest;
import it.markovii.framework.Provider;
import it.markovii.framework.Service;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1250, 750);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.setResizable(false);

        final CheckBox[] lastCheckBox = new CheckBox[1];

        VBox checkBoxesLeft = new VBox();
        VBox checkBoxesRight = new VBox();

        HttpRequest fetchContriesList = new HttpRequest("https://esignature.ec.europa.eu/efda/tl-browser/api/v1/search/countries_list");
        JSONArray jsonCountriesList = new JSONArray(fetchContriesList.getResponse());

        Vector<String> countries = new Vector<>();
        Map<String, String> countryNameToCode = new HashMap<>();

        for (int i = 0; i<jsonCountriesList.length(); i++) {
            countries.add(jsonCountriesList.getJSONObject(i).getString("countryName"));
            countryNameToCode.put(jsonCountriesList.getJSONObject(i).getString("countryName"), jsonCountriesList.getJSONObject(i).getString("countryCode"));
        }

        CountriesCheckBoxController countriesCheckBoxController = new CountriesCheckBoxController(countries, countryNameToCode);

        int separator = 0;
        for (int i = 0; i<jsonCountriesList.length(); i++) {
            CheckBox countryCheckbox = new CheckBox(jsonCountriesList.getJSONObject(i).getString("countryName"));
            countryCheckbox.getStyleClass().add("countries-check-box");
            countryCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                    lastCheckBox[0] = countryCheckbox;
                    if (countryCheckbox.isSelected())
                        countriesCheckBoxController.onCheckBoxMark(countryCheckbox.getText());
                    else
                        countriesCheckBoxController.onCheckBoxUnmark(countryCheckbox.getText());
                }
            });

            if (separator < (jsonCountriesList.length()/2))
                checkBoxesLeft.getChildren().add(countryCheckbox);
            else
                checkBoxesRight.getChildren().add(countryCheckbox);
            separator++;
        }

        AnchorPane countriesPane = (AnchorPane) fxmlLoader.getNamespace().get("countriesAnchorPane");

        HBox checkBoxes = new HBox(checkBoxesLeft, checkBoxesRight);
        checkBoxes.setSpacing(15);

        AnchorPane.setTopAnchor(checkBoxes, 75.0);
        AnchorPane.setLeftAnchor(checkBoxes, 22.0);

        checkBoxes.setSpacing(55);
        checkBoxes.setAlignment(Pos.CENTER_LEFT);



        HttpRequest fetchAllProviders = new HttpRequest("https://esignature.ec.europa.eu/efda/tl-browser/api/v1/search/tsp_list");
        JSONArray jsonProvidersList = new JSONArray(fetchAllProviders.getResponse());
        Provider[] all_tsp = new Provider[jsonProvidersList.length()];

        Multimap<String, Provider> countryMap = ArrayListMultimap.create();
        Multimap<String, Provider> typeMap = ArrayListMultimap.create();
        Multimap<String, Provider> statusMap = ArrayListMultimap.create();
        Vector<String> typesOfService = new Vector<>();
        Vector<Provider> providers = new Vector<>();
        Vector<String> statuses = new Vector<>();

        // Riempimento mappe e vettori iniziali
        for (int i = 0; i<jsonProvidersList.length(); i++) {

            //Decodifica json
            all_tsp[i] = new Provider(jsonProvidersList.getJSONObject(i).toString());

            //inserimento mappa key Country value provider
            countryMap.put(all_tsp[i].getCountryCode(), all_tsp[i]);

            //inserimento vector provider e vettore copia
            providers.add(all_tsp[i]);

            //inserimento vector statuses
            for (int j = 0; j<all_tsp[i].getServices().length; j++) {
                Service[] s = all_tsp[i].getServices();
                typeMap.put(s[j].getCurrentStatus(), all_tsp[i]);
                if (!statuses.contains(s[j].getCurrentStatus()))
                    statuses.add(s[j].getCurrentStatus());
            }

            //Inserimento mappa Service Types
            for (int j = 0; j<all_tsp[i].getServiceTypes().length; j++) {
                String[] s = all_tsp[i].getServiceTypes();
                typeMap.put(s[j], all_tsp[i]);
                if (!typesOfService.contains(s[j]))
                    typesOfService.add(s[j]);
            }
        }

        TypeCheckBoxController typeCheckBoxController = new TypeCheckBoxController(typesOfService);

        AnchorPane tosAnchorPane = (AnchorPane) fxmlLoader.getNamespace().get("tosAnchorPane");

        VBox tosCheckBoxesLeft = new VBox();
        VBox tosCheckBoxesRight = new VBox();

        separator = 0;
        for (int i = 0; i<typesOfService.size(); i++) {
            CheckBox tosCheckBox = new CheckBox(typesOfService.get(i));
            tosCheckBox.getStyleClass().add("countries-check-box");
            tosCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                lastCheckBox[0] = tosCheckBox;
                if (tosCheckBox.isSelected())
                    typeCheckBoxController.onCheckBoxMark(tosCheckBox.getText());
                else
                    typeCheckBoxController.onCheckBoxUnmark(tosCheckBox.getText());
            });

            if (separator < (typesOfService.size()/2))
                tosCheckBoxesLeft.getChildren().add(tosCheckBox);
            else
                tosCheckBoxesRight.getChildren().add(tosCheckBox);
            separator++;
        }

        HBox tosCheckBoxes = new HBox(tosCheckBoxesLeft, tosCheckBoxesRight);
        tosCheckBoxes.setSpacing(15);

        AnchorPane.setTopAnchor(tosCheckBoxes, 75.0);
        AnchorPane.setLeftAnchor(tosCheckBoxes, 22.0);


        tosCheckBoxes.setSpacing(55);
        tosCheckBoxes.setAlignment(Pos.CENTER_LEFT);

        AnchorPane ssAnchorPane = (AnchorPane) fxmlLoader.getNamespace().get("ssAnchorPane");
        StatusCheckBoxController statusCheckBoxController = new StatusCheckBoxController(statuses);

        VBox ssCheckBoxesLeft = new VBox();
        VBox ssCheckBoxesRight = new VBox();

        separator = 0;
        for (int i = 0; i<statuses.size(); i++) {
            CheckBox ssCheckBox = new CheckBox(statuses.get(i));
            ssCheckBox.getStyleClass().add("countries-check-box");
            ssCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                lastCheckBox[0] = ssCheckBox;
                if (ssCheckBox.isSelected())
                    statusCheckBoxController.onCheckBoxMark(ssCheckBox.getText());
                else
                    statusCheckBoxController.onCheckBoxUnmark(ssCheckBox.getText());
            });

            if (separator < (typesOfService.size()/2))
                ssCheckBoxesLeft.getChildren().add(ssCheckBox);
            else
                ssCheckBoxesRight.getChildren().add(ssCheckBox);
            separator++;
        }

        HBox ssCheckBoxes = new HBox(ssCheckBoxesLeft, ssCheckBoxesRight);
        ssCheckBoxes.setSpacing(15);

        AnchorPane.setTopAnchor(ssCheckBoxes, 75.0);
        AnchorPane.setLeftAnchor(ssCheckBoxes, 22.0);


        ssCheckBoxes.setSpacing(55);
        ssCheckBoxes.setAlignment(Pos.CENTER_LEFT);

        AnchorPane scrollPane = (AnchorPane) fxmlLoader.getNamespace().get("providersScrollPAne");
        ProviderFilterController fss = new ProviderFilterController(providers);

        VBox providersCheckBoxes = new VBox();

        Timeline pane = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {

            List<CheckBox> filtered_providers = fss.filterProviders(countriesCheckBoxController.getSelectedCountriesCode(), typeCheckBoxController.getSelectdTypes(), statusCheckBoxController.getSelectedStatuses());
            providersCheckBoxes.getChildren().clear();
            providersCheckBoxes.getChildren().addAll(filtered_providers);
        }));

        pane.setCycleCount(Animation.INDEFINITE);
        pane.play();

        AnchorPane.setTopAnchor(providersCheckBoxes, 10.0);
        AnchorPane.setLeftAnchor(providersCheckBoxes, 22.0);

        providersCheckBoxes.setSpacing(10);
        providersCheckBoxes.setAlignment(Pos.CENTER_LEFT);


        countriesPane.getChildren().add(checkBoxes);
        tosAnchorPane.getChildren().add(tosCheckBoxes);
        ssAnchorPane.getChildren().add(ssCheckBoxes);
        scrollPane.getChildren().add(providersCheckBoxes);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/stylesheet.css")).toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}