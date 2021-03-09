package app.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер окна для ввода пароля
 * @author Irog Makarevich
 */
public class InputPassword_Controller {
	/**
	 * Строка (ссылка) в которую нужно вернуть значение пароля.
	 */
	public String password;
	/**
	 * Переменная (ссылка) Ok/Cancel для возврата в родительскую форму
	 */
	public String isPassword;
	
	@FXML
	private Label label_Msg;
	@FXML
	private PasswordField passwordField_Pwd;
	@FXML
	private Button button_Ok;
	@FXML
	private Button button_Cancel;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public InputPassword_Controller () {
    	
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    	
    }
    
    /**
     * Вызывается родительским обьектом, который даёт ссылки.
     * Инициализирует контролы на слое.
     * 
     * @param 
     */
    public void setParrentObj(String password, String isPassword, String msg) {
        this.password = password;
        this.isPassword = isPassword;
        
        // init controls
        initControlsValue(msg);
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue(String msg) {
    	label_Msg.setText(msg);
    	
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_ok_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	isPassword = "Ok";
    	password = passwordField_Pwd.getText();
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
    
    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonCancel() {
    	isPassword = "Cancel";
    	
        // get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
