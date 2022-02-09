package app.view.business.template;

import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateSimpleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

/**
 * Контроллер форма для добавления/изменения директории файлов, стилей или шаблонов
 * 
 * @author Igor Makarevich
 */
public class TemplateDirEdit_Controller {
	// for actionType
	public static final int ACTION_TYPE_ADD       = 0;
	public static final int ACTION_TYPE_EDIT      = 1;
	
	private Params params;
	private DBConCur_Parameters conn;
    /**
     * Тип операции : 0 - добавить ; 1 - редактировать
     */
    private int actionType;
    /**
     * Текущая запись в дереве шаблонов, передаваемая в класс
     */
    private TreeItem<TemplateSimpleItem> editedItem_ti;
	private TemplateSimpleItem editedItem;
	/**
	 * Результирующий итем в дереве после создания/изменения
	 */
	private TreeItem<TemplateSimpleItem> resultItem;
	
	@FXML
	private Label label_Id;
	@FXML
	private TextField textField_Name;
	@FXML
	private TextField textField_Descr;
	@FXML
	private Label label_DateCreated;
	@FXML
	private Label label_DateModified;
	@FXML
	private Label label_UserCreated;
	@FXML
	private Label label_UserModified;
	@FXML
	private Button button_Ok;
	@FXML
	private Button button_Cancel;
	
	private Preferences prefs;
	private DateConv dateConv;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateDirEdit_Controller () {
    	prefs = Preferences.userNodeForPackage(TemplateDirEdit_Controller.class);
    	dateConv = new DateConv();
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {         }
	    
	
	
	
    /**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	
    	
    	
    }
    //TODO
    
    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonCancel() {
    	//-------- save stage position
    	prefs.putDouble("stageTemplateDirEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateDirEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateDirEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateDirEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
