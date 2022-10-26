package com.game.connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final String discColor1 = "#24303E";
    private static final String discColor2 = "#4CAA88";

    private static String PLAYER_ONE = "Player One";
    private static String PLAYER_TWO = "Player Two";

    private boolean isPlayerOneTurn = true;

    private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS];
    @FXML
    public GridPane rootGPane;

    @FXML
    public Pane insertedDiscPane;
    @FXML
    public Label playerNameLabel;
    @FXML
    public TextField tf1;
    @FXML
    public TextField tf2;
    @FXML
    public Button namebtn;

    private boolean isAllowedToInsert = true;
    public void createPlayGround()
    {
        Shape rectangleWithHoles = createGameStructuralGrid();
        rootGPane.add(rectangleWithHoles,0,1);

        List<Rectangle> rectangleList = createClickableCol();
        for (Rectangle rectangle:rectangleList
             ) {
            rootGPane.add(rectangle,0,1);
        }
    }
    private Shape createGameStructuralGrid(){
        Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1)*CIRCLE_DIAMETER);

        for (int i = 0; i < ROWS ; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER / 2);
                circle.setCenterX(CIRCLE_DIAMETER / 2);
                circle.setCenterY(CIRCLE_DIAMETER / 2);
                circle.setSmooth(true);

                circle.setTranslateX(j * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER/4);
                circle.setTranslateY(i * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER/4);

                rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle);
            }
        }
        rectangleWithHoles.setFill(Color.WHITE);
        return rectangleWithHoles;
    }
    private List<Rectangle> createClickableCol(){
        List<Rectangle> rectangleList = new ArrayList<>();
        for (int i = 0; i < COLUMNS; i++) {
            Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS + 1)*CIRCLE_DIAMETER);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(i * (CIRCLE_DIAMETER + 5 ) + CIRCLE_DIAMETER/4);

            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

            final int col=i;
            rectangle.setOnMouseClicked(mouseEvent -> {
                if (isAllowedToInsert){
                    isAllowedToInsert =false;
                    insertedDisc(new Disc(isPlayerOneTurn),col);
                }
            });
            rectangleList.add(rectangle);
        }
        return rectangleList;
    }

    private void insertedDisc(Disc disc, int col) {

        int row = ROWS-1;
        while (row>=0){
            if (getDiscIsPresent(row,col) == null)
                break;

            row--;
        }
        if(row<0)
            return;

        insertedDiscsArray[row][col] = disc;
        insertedDiscPane.getChildren().add(disc);

        disc.setTranslateX(col * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER/2);
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
        translateTransition.setToY(row * (CIRCLE_DIAMETER+5) + CIRCLE_DIAMETER/4);

        int currentRow =row;
        translateTransition.setOnFinished(actionEvent -> {
            isAllowedToInsert = true;
            if (gameEnded(currentRow, col)){
                gameOver();
                return;
            }

            isPlayerOneTurn = !isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
        });
        translateTransition.play();
    }
    private boolean gameEnded(int row , int col){
        List<Point2D> verticalPoints = IntStream.rangeClosed(row-3,row+3).mapToObj(r -> new Point2D(r,col)).collect(Collectors.toList());
        List<Point2D> horizontalPoints = IntStream.rangeClosed(col-3,col+3).mapToObj(c -> new Point2D(row,c)).collect(Collectors.toList());
        Point2D startPoint1 = new Point2D(row-3,col+3);
        List<Point2D> diagonalPoints = IntStream.rangeClosed(0,6).mapToObj(i -> startPoint1.add(i,-i)).collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row-3,col-3);
        List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6).mapToObj(i -> startPoint2.add(i,i)).collect(Collectors.toList());
        boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints) || checkCombinations(diagonalPoints) || checkCombinations(diagonal2Points);
        return isEnded;
    }

    private boolean checkCombinations(List<Point2D> points) {
        int ch = 0;
        for(Point2D point: points){

            int rowIndexForArray = (int) point.getX();
            int colIndexForArray = (int) point.getY();

            Disc disc = getDiscIsPresent(rowIndexForArray,colIndexForArray);

            if(disc!=null && disc.isPlayerOneMove == isPlayerOneTurn){
                ch++;
                if (ch == 4){
                    return true;
                }
            }else {
                ch=0;
            }
        }
        return false;
    }
    private Disc getDiscIsPresent(int rowIndex , int colIndex){
        if (rowIndex>=ROWS || rowIndex<0 || colIndex>= COLUMNS || colIndex<0)
            return null;
        return insertedDiscsArray[rowIndex][colIndex];
    }

    private void gameOver(){
        String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
        System.out.println("Winner is: " + winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The Winner is " + winner);
        alert.setContentText("Want to play more");

        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType noBtn = new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yesBtn,noBtn);

        Platform.runLater(() -> {
            Optional<ButtonType> btnClicked = alert.showAndWait();
            if (btnClicked.isPresent() && btnClicked.get()==yesBtn)
            {
                resetGame();
            }
            else {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void resetGame() {
        insertedDiscPane.getChildren().clear();
        for (int i = 0; i < insertedDiscsArray.length; i++) {
            for (int j = 0; j < insertedDiscsArray[i].length; j++) {
                insertedDiscsArray[i][j] = null;
            }
        }
        isPlayerOneTurn = true;
        playerNameLabel.setText(PLAYER_ONE);
        createPlayGround();
    }

    private  static class Disc extends Circle{
        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove){
            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER/2);
            setFill(isPlayerOneMove? Color.valueOf(discColor1):Color.valueOf(discColor2));
            setCenterX(CIRCLE_DIAMETER/4);
            setCenterY(CIRCLE_DIAMETER/2);
        }
    }

    private void convert() {
        String input1 = tf1.getText();
        String input2 = tf2.getText();
        PLAYER_ONE=input1;
        PLAYER_TWO=input2;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        namebtn.setOnAction(actionEvent -> convert());
    }
}