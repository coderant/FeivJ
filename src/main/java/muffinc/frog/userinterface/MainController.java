package muffinc.frog.userinterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import muffinc.frog.object.FrogImg;
import muffinc.frog.object.Human;
import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.acl.Group;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable{

//    @FXML
//    private MenuBar menuBar;
    
    @FXML
    public TableView<PhotoGem> photoTable;
    @FXML
    private TableColumn<PhotoGem, String> photoNameColumn;
    @FXML
    private TableColumn<PhotoGem, Integer> countColumn;

    @FXML
    public TableView<PeopleGem> humanTable;
    @FXML
    private TableColumn<PeopleGem, String> humanNameColumn;
    @FXML
    private TableColumn<PeopleGem, Integer> humanPhotoNumberColumn;

    @FXML
    private TableView<PhotoGem> humanPhotoTable;
    @FXML
    private TableColumn<PhotoGem, String> humanPhotoNameColumn;
    @FXML
    private TableColumn<PhotoGem, String> humanPhotoLocationColumn;


    @FXML
    private ImageView photoImageView;

    @FXML
    private HBox photoImageViewParent;

    @FXML
    private ImageView faceImageView;

    @FXML
    private ImageView humanPhotoImageView;

    @FXML
    private HBox humanPhotoImageViewParent;

    @FXML
    private Button addFileButton;

    @FXML
    private Button deleteSelectedPhotoButton;

    @FXML
    private Button deleteFaceButton;

    @FXML
    private Button scanButton;

    @FXML
    private Button scanAllButton;

    @FXML
    private Button detectAllButton;

    @FXML
    private Button addPeopleButton;


    @FXML
    private AnchorPane newPeopleGroup;
    @FXML
    private Button photoPageNewPeople;
    @FXML
    private Button photoPageSetAs;
    @FXML
    private ComboBox<String> setAsCombo;

    @FXML
    private TextField idTextField;

    @FXML
    private ComboBox<String> facesCombo;

    private Main main;

    private FileChooser fileChooser = new FileChooser();

    public ObservableList<PeopleGem> peopleGemObservableList = FXCollections.observableArrayList();
    private ObservableList<PhotoGem> photoGemObservableList = FXCollections.observableArrayList();

    private ObservableList<PhotoGem> peoplePhotoObservableList = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {


        initHumanTable();

        initPhotoTable();

        initHumanPhotoTable();


        initFaceComboPreview();

    }

    private void initHumanPhotoTable() {
        humanPhotoImageView.fitHeightProperty().bind(humanPhotoImageViewParent.heightProperty());

        humanPhotoTable.setItems(peoplePhotoObservableList);
        addHumanPhotoTableListener();
        humanPhotoNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
        humanPhotoLocationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
    }

    private void addHumanPhotoTableListener() {
        humanPhotoTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (humanPhotoTable.getSelectionModel().getSelectedItem() != null) {
                repaintHumanPhotoImageView(humanPhotoTable.getSelectionModel().getSelectedItem(),
                        humanTable.getSelectionModel().getSelectedItem().getHuman());
            }

        });
    }

    private void initPhotoTable() {
        photoTable.setItems(photoGemObservableList);
        addPhotoPreviewListener();
        countColumn.setCellValueFactory(cellData -> cellData.getValue().photoCountProperty().asObject());
        photoNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());

    }

    private void initHumanTable() {
        humanTable.setItems(peopleGemObservableList);
        addHumanTableListener();
        humanNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        humanPhotoNumberColumn.setCellValueFactory(cellData -> cellData.getValue().photoNumberProperty().asObject());

    }

    private void addHumanTableListener() {
        humanTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (humanTable.getSelectionModel().getSelectedItem() != null) {
                Human human = humanTable.getSelectionModel().getSelectedItem().getHuman();

                peoplePhotoObservableList.clear();
                for (FrogImg frogImg : human.frogImgs.keySet()) {
                    peoplePhotoObservableList.add(new PhotoGem(frogImg));
                }
                repaintHumanPhotoImageView(null, null);
            }
            humanPhotoTable.getSelectionModel().selectFirst();
        });
    }


    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }


    public PhotoGem addNewImg(File file) {
        return addNewImg(file, true);
    }

    public PhotoGem addNewImg(File file, boolean doScan) {
        PhotoGem photoGem = new PhotoGem(main.engine.addNewImg(file, doScan));
        photoGemObservableList.add(photoGem);
        return photoGem;
    }

    public void deleteImg(PhotoGem photoGem) {
        photoGem.getFrogImg().delete();
        photoGemObservableList.remove(photoGem);
    }

    public void handleAddFileButton() {
        String[] extensions = {"*.jpeg", "*.jpg", "*.pgm", "*.tif", "*.gif"};
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", extensions);
        fileChooser.getExtensionFilters().add(extFilter);

        List<File> files = fileChooser.showOpenMultipleDialog(addFileButton.getScene().getWindow());
        if (files != null) {
            for (File file : files) {
                System.out.println(file.getAbsolutePath() + " was chosen");
                addNewImg(file);
            }
        }

        updateHumanObservableList();
    }

    //TODO wrong update method
    public void updateHumanObservableList() {
        peopleGemObservableList.clear();

        for (Human human : main.engine.humanFactory.nameTable.values()) {
            peopleGemObservableList.add(new PeopleGem(human));
        }
    }

    //TODO does this work?
    public void handleDeleteSelectedPhotoButton() {
        if (photoTable.getSelectionModel().getSelectedItem() != null) {
            PhotoGem photoGem = photoTable.getSelectionModel().getSelectedItem();
            deleteImg(photoGem);
            photoTable.setItems(photoGemObservableList);
        }
    }

    private void initFaceComboPreview() {
        facesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {

            if (photoTable.getSelectionModel().getSelectedItem().getFrogImg().isDetected() && newValue != null) {
                int i = parseSelectedFaceIndex(newValue);

                if (i >= 0) {
//                    System.out.println("selected face: " + newValue.substring(i));
                    PhotoGem selected = photoTable.getSelectionModel().getSelectedItem();
                    opencv_core.CvRect cvRect = selected.getFrogImg().getCvRects().get(i);
                    Rectangle2D rectangle2D = new Rectangle2D(cvRect.x(), cvRect.y(), cvRect.width(), cvRect.height());
                    faceImageView.setImage(photoImageView.getImage());
                    faceImageView.setViewport(rectangle2D);
                }

//                repaintIdText(newValue);

                repaintHumanText(newValue);

            }
        });
    }


    //Parse integer from newValue
    private int parseSelectedFaceIndex(String newValue) {
        int i;

        for (i = newValue.length() - 1; i >= 0; i--) {
            if (newValue.charAt(i) < '0' || newValue.charAt(i) > '9') {
                break;
            }
        }

        if (newValue.substring(++i).length() > 0) {
            return Integer.parseInt(newValue.substring(i)) - 1;
        } else {
            return -1;
        }
    }

    private void addPhotoPreviewListener() {
        photoImageView.fitHeightProperty().bind(photoImageViewParent.heightProperty());
//        photoImageView.fitWidthProperty().bind(photoImageViewParent.widthProperty());
        photoTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            faceImageView.setImage(null);
            if (photoTable.getSelectionModel().getSelectedItem() != null) {

                PhotoGem selected = photoTable.getSelectionModel().getSelectedItem();
                repaintPhotoImageView(selected);
                repaintFacesCombo(selected);
                repaintHumanText(null);
            }

            facesCombo.getSelectionModel().selectFirst();

        });

    }


    public void handleScanButton() {
        if (photoTable.getSelectionModel().getSelectedItem() != null) {
            PhotoGem selected = photoTable.getSelectionModel().getSelectedItem();
            selected.getFrogImg().detectAndID();
            repaintPhotoImageView(selected);
            repaintFacesCombo(selected);
        }
    }

    public void handleScanAllButton() {
        for (PhotoGem photoGem : photoTable.getItems()) {
            photoGem.getFrogImg().detectAndID();
            repaintPhotoImageView(photoGem);
//            repaintFacesCombo(photoGem);
        }
    }

    public void handleDeleteFaceButton() {
        if (photoTable.getSelectionModel().getSelectedItem() != null) {

            PhotoGem selected = photoTable.getSelectionModel().getSelectedItem();

            int i = parseSelectedFaceIndex(facesCombo.getValue());

            if (i >= 0) {
                FrogImg frogImg = photoTable.getSelectionModel().getSelectedItem().getFrogImg();
                frogImg.removeCvRect(i);
                repaintPhotoImageView(selected);
                repaintFacesCombo(selected);
            }
            resetFaceImageView();
            repaintHumanText(null);
            facesCombo.getSelectionModel().selectFirst();
        }
    }

    private void resetFaceImageView() {
        faceImageView.setImage(null);
    }


    private void repaintHumanPhotoImageView(PhotoGem selected, Human human) {
        if (selected != null & human != null) {
            humanPhotoImageView.setImage(selected.getFrogImg().getThisHumanImage(human));
        } else {
            humanPhotoImageView.setImage(null);
        }
    }

    private void repaintPhotoImageView(PhotoGem selected) {
        photoImageView.setImage(selected.getFrogImg().getCurrentImage());
    }

    private void repaintFacesCombo(PhotoGem selected) {
        ObservableList<String> faces = FXCollections.observableArrayList();
        if (selected.getFrogImg().isDetected()) {
            if (selected.getFrogImg().faceNumber() >= 1) {
                for (int i = 0; i < selected.getFrogImg().faceNumber();) {
                    faces.add("Face " + ++i);
                }
                facesCombo.setValue("Please Select Face:");
            } else  {
                facesCombo.setValue("Face Not Found");
            }
        } else {
            facesCombo.setValue("Please Scan This Photo First!");
        }
        facesCombo.setItems(faces);
    }

    private void repaintHumanText(String newValue) {

        if (newValue != null) {
            int i = parseSelectedFaceIndex(newValue);

            if (i >= 0) {
                FrogImg frogImg = photoTable.getSelectionModel().getSelectedItem().getFrogImg();
                opencv_core.CvRect cvRect = frogImg.getCvRects().get(i);

                String thisIs = frogImg.whoIsThisCvRect(cvRect);
                idTextField.setText(thisIs);

                if (thisIs.equals("新人?")) {
                    newPeopleGroup.setVisible(true);
                    repaintSetAsCombo();
                } else {
                    newPeopleGroup.setVisible(false);
                }
            }
        } else {
            idTextField.clear();
        }

    }

    private void repaintSetAsCombo() {
        ObservableList<String> observableList = FXCollections.observableArrayList();

        for (Human human : main.engine.humanFactory.nameTable.values()) {
            observableList.add(human.name);
        }

        setAsCombo.setValue("请选择人物:");
        setAsCombo.setItems(observableList);
    }

    //TODO handlePhotoPageNewPeople
    public void handlePhotoPageNewPeople() {

    }

    // TODO handlePhotoPageSetAs
    public void handlePhotoPageSetAs() {

    }


    public void handleAddPeople() {
        main.showAddPeopleDialogue();
    }


    //TODO add detect all
    public void handleDetectAllButton() {

    }

}