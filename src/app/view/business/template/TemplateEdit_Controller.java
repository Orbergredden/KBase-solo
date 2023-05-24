package app.view.business.template;

import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.lib.FileCache;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	@FXML
	private Label label_TemplateParentId;
	@FXML
	private TextField textField_TemplateName;
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

	@FXML
	private Button button_TemplateFileOpen;
	@FXML
	private Button button_TemplateFileSaveToDisk;
	@FXML
	private Button button_TemplateFileSaveToDB;
	@FXML
	private TextArea textArea_TemplateContent;
	
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
    	
    	//========
    	if (actionType == ACTION_TYPE_ADD) {
    		label_TemplateId.setText("");
    		label_TemplateParentId.setText(editedItem.getName() +" ("+ Long.toString(editedItem.getId()) +")");
    		label_TemplateDateCreated.setText("");
    		label_TemplateDateModified.setText("");
    		label_TemplateUserCreated.setText("");
    		label_TemplateUserModified.setText("");
    	} else if (actionType == ACTION_TYPE_EDIT) {
    		label_TemplateId.setText(Long.toString(editedItem.getId()));
    		label_TemplateParentId.setText(
    				editedItem_ti.getParent().getValue().getName() +
    				" ("+ Long.toString(editedItem_ti.getParent().getValue().getId()) +")");
    		textField_TemplateName.setText(editedItem.getName());
        	textField_TemplateDescr.setText(editedItem.getDescr());
        	label_TemplateDateCreated.setText(dateConv.dateTimeToStr(editedItem.getDateCreated()));
        	label_TemplateDateModified.setText(dateConv.dateTimeToStr(editedItem.getDateModified()));
        	label_TemplateUserCreated.setText(editedItem.getUserCreated());
        	label_TemplateUserModified.setText(editedItem.getUserModified());
    		
    		
    		
        	
        	
    		//TODO
    	}
    	
    	//======== buttons
    	button_TemplateFileOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
		button_TemplateFileOpen.setTooltip(new Tooltip("Открыть шаблон..."));
		button_TemplateFileSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
		button_TemplateFileSaveToDisk.setTooltip(new Tooltip("Сохранить шаблон на диске..."));
		button_TemplateFileSaveToDB.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
		button_TemplateFileSaveToDB.setTooltip(new Tooltip("Сохранить шаблон в базе данных..."));button_TemplateFileOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
		button_TemplateFileOpen.setTooltip(new Tooltip("Открыть шаблон..."));
		button_TemplateFileSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
		button_TemplateFileSaveToDisk.setTooltip(new Tooltip("Сохранить шаблон на диске..."));
		button_TemplateFileSaveToDB.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
		button_TemplateFileSaveToDB.setTooltip(new Tooltip("Сохранить шаблон в базе данных..."));
    	
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
	
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
    	TemplateItem ti;
    	
    	//---------- check data in fields
		if ((textField_TemplateName.getText().equals("") || (textField_TemplateName.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название шаблона", "Укажите Название шаблона");
    		return;
    		//throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Название шаблона", this);
    	}
    	if ((textArea_TemplateContent.getText().equals("") || (textArea_TemplateContent.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Нет текста шаблона", "Укажите текст шаблона");
    		return;
    		//throw new KBase_Ex (1, "Ошибка при сохранении", "Нет текста шаблона", this);
    	}
    	
    	//-------- Сохраняем
    	switch (actionType) {
    	case ACTION_TYPE_ADD :
    		long newId = conn.db.templateNextId();
    		
    		//-------- add to DB
    		ti = new TemplateItem(
    				newId,
    				editedItem.getId(),
    				(editedItem.getSubtypeItem() == 1) ? 0 : 10,
    				textField_TemplateName.getText(),
    				textField_TemplateDescr.getText(),
    				textArea_TemplateContent.getText()
    				);
    		conn.db.templateAdd(ti);  // шаблон добавляем в БД
    		ti = conn.db.templateGet(newId);                   // get full info by Id
    		
    		//--- добавляем в контрол-дерево
			// Prepare a new TreeItem with a new template object
			TreeItem<TemplateSimpleItem> item = new TreeItem<>(ti);
			// Add the new item as children to the parent item
			// Make sure the new item is visible
			editedItem_ti.getChildren().add(item);
			editedItem_ti.setExpanded(true);
			
			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Новий шаблон '" + ti.getName() + "' доданий.");
    		
    		break;
    	case ACTION_TYPE_EDIT :
    	
    	
    	

    		
    		//TODO
    		
    		break;
    	}
    	
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
