package app.view.business.template;

import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateSimpleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

/**
 * Контроллер форма для додавання/редагування шаблона
 * 
 * @author Igor Makarevich
 */
public class TemplateEdit_Controller {
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
	
	@FXML
	private Label label_TemplateId;
/*	@FXML
	private Label label_TemplateParentId;
	@FXML
	private TextField textField_TemplateName;
	@FXML
	private TextField textField_TemplateFileName;
	@FXML
	private TextField textField_TemplateDescr;
	@FXML
	private Label label_TemplateDateCreated;
	@FXML
	private Label label_TemplateDateModified;
	@FXML
	private Label label_TemplateUserCreated;
	@FXML
	private Label label_TemplateUserModified;
	*/
	@FXML
	private Button button_TemplateFileOpen;
	@FXML
	private Button button_TemplateFileSaveToDisk;
	@FXML
	private Button button_TemplateFileSaveToDB;
	@FXML
	private TextArea textArea_TemplateContent;
	
	@FXML
	private Button button_Next;
	@FXML
	private Button button_Cancel;
	
	//
	private Preferences prefs;
	private DateConv dateConv;

	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateEdit_Controller () {
    	prefs = Preferences.userNodeForPackage(TemplateEdit_Controller.class);
    	dateConv = new DateConv();
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {         }
    
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     * 
     * @param 
     *        actionType : 0 - добавить ; 1 - редактировать
     */
    public void setParams(Params params, int actionType, 
    		TreeItem<TemplateSimpleItem> editedItem_ti) {
    	this.params     = params;
    	this.conn       = params.getConCur();
    	this.actionType = actionType;
        this.editedItem_ti = editedItem_ti;
        if (editedItem_ti != null) this.editedItem = editedItem_ti.getValue();
        else                       this.editedItem = null;
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
    	
    	
    	
    	
    	
    }
	//TODO
	
	/**
     * Вызывается для открытия тестового файла с тектом для шаблона
     */
    @FXML
    private void handleButtonTemplateFileOpen() {
    	
    	
    	
    	
    }
	//TODO
    
    /**
     * Вызывается для сохранения шаблона в файл на диске
     */
    @FXML
    private void handleButtonTemplateFileSaveToDisk() {
    	
    	
    	
    }
	//TODO
    
    /**
	 * Вызывается для сохранения шаблона в БД
	 */
	@FXML
	private void handleButtonTemplateFileSaveToDB() {
		
		
		
	}
	//TODO
	
	/**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	
    	
    	
    	
    	
    	//-------- save stage position
    	prefs.putDouble("stageTemplateEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
    //TODO

    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonCancel() {
    	//-------- save stage position
    	prefs.putDouble("stageTemplateEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
