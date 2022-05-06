package app.view.business.template;

import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.InfoTypeItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateThemeItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
	private InfoTypeItem curInfoTypeItem; 
	private TemplateItem curTemplateItem;
	
	// controls
	@FXML
	private Label label_Theme;
	@FXML
	private Label label_StyleId;
	@FXML
	private Label label_StyleParentId;
	@FXML
	private Label label_StyleInfoTypeId;
	@FXML
	private TextField textField_StyleName;
	@FXML
	private TextField textField_StyleDescr;
	@FXML
	private TextField textField_TemplateId;
	@FXML
	private Label label_TemplateName;
	@FXML
	private Label label_StyleDateCreated;
	@FXML
	private Label label_StyleDateModified;
	@FXML
	private Label label_StyleUserCreated;
	@FXML
	private Label label_StyleUserModified;
	@FXML
	private ImageView imageView_StyleDef;
	@FXML
	private CheckBox checkBox_StyleDef;
	@FXML
	private Label label_StyleDefDate;
	@FXML
	private Label label_StyleDefCur;
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
    	long themeId;
    	TreeItem<TemplateSimpleItem> ttsi = editedItem_ti;
    	
    	//======== get current theme 
    	while (ttsi.getValue().getThemeId() == 0) {
    		ttsi = ttsi.getParent();
    		themeId = ttsi.getValue().getThemeId();
    	}
    	curThemeItem = conn.db.templateThemeGetById(editedItem.getThemeId());
    	label_Theme.setText(curThemeItem.getName() +" ("+ Long.toString(curThemeItem.getId()) +")");
    	
    	//======== get info block type
    	if (editedItem.getFlag() > 0) {
    		curInfoTypeItem = conn.db.infoTypeGet(editedItem.getFlag());
    		label_StyleInfoTypeId.setText(curInfoTypeItem.getName() +" ("+ Long.toString(curInfoTypeItem.getId()) +")");
    	} else {
    		label_StyleInfoTypeId.setText("Зарезервований стиль");
    	}
    	
    	//========
    	if (actionType == ACTION_TYPE_ADD) { 
    		label_StyleId.setText("");
    		label_StyleParentId.setText(editedItem.getName() +" ("+ Long.toString(editedItem.getId()) +")");
    		
    		
    		
    		
    		
    	} else if (actionType == ACTION_TYPE_EDIT) {
    		
    		
    		
    		
    		
    	}
    	
    	//======== buttons
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
	//TODO initControlsValue
	
    /**
     * Визивається при закінченні вводу id шаблона
     */
    @FXML
    private void handleTextFieldTemplateId() {
    	try {
    		long templateId = Long.parseLong(textField_TemplateId.getText());
        	
        	if (conn.db.templateIsPresent(templateId)) {
        		curTemplateItem = conn.db.templateGet(templateId);
        		label_TemplateName.setText(curTemplateItem.getName());
        	} else {
        		label_TemplateName.setText("ШАБЛОН ПО ВКАЗАНОМУ id НЕ ЗНАЙДЕНО !!!");
        	}
    	} catch (NumberFormatException e) {
    		label_TemplateName.setText("ВКАЗАН КОРЯВИЙ id ШАБЛОНА !!!");
    	}
    }
    
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
