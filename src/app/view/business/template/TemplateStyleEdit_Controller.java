package app.view.business.template;

import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateThemeItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер форма для додавання/редагування стилю шаблонів
 * 
 * @author Igor Makarevich
 */
public class TemplateStyleEdit_Controller {
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
	
	private TemplateThemeItem curThemeItem;
	
	// controls
	
	
	
	
	@FXML
	private Button button_Ok;
	@FXML
	private Button button_Cancel;
	
	//
	private Preferences prefs;
	private DateConv dateConv;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateStyleEdit_Controller () {
    	prefs = Preferences.userNodeForPackage(TemplateStyleEdit_Controller.class);
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
    	

    	
    	
    	
    	
    	//======== buttons
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
	//TODO initControlsValue
	
    /**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	
    	
    	
    	
    	
    	
    	
    	//TODO handleButtonOk
    	
    	//-------- save stage position
    	prefs.putDouble("stageTemplateStyleEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateStyleEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateStyleEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateStyleEdit_PosY",  params.getStageCur().getY());
    	
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
    	//-------- save stage position
    	prefs.putDouble("stageTemplateStyleEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateStyleEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateStyleEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateStyleEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
