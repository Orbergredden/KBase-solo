package app.view.business.template;

import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateStyleItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер форма для добавления/изменения файла используемого в шаблонах
 * 
 * @author Igor Makarevich
 */
public class TemplateFileEdit_Controller {
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

	// controls
	@FXML
	private Label label_Title;
	@FXML
	private Label label_ThemeId;
	@FXML
	private TextField textField_FileName;
	@FXML
	private TextField textField_Descr;
	@FXML
	private ComboBox<String> comboBox_FileType;
	@FXML
	private Label label_DateCreated;
	@FXML
	private Label label_DateModified;
	@FXML
	private Label label_UserCreated;
	@FXML
	private Label label_UserModified;
	
	@FXML
	private TextArea textArea_FileContent;
	@FXML
	private Button button_FileTextOpen;
	@FXML
	private Button button_FileTextSaveToDisk;
	
	@FXML
	private ImageView imageView_FileContent;
	@FXML
	private Button button_FileImageOpen;
	@FXML
	private Button button_FileImageSaveToDisk;
	@FXML
	private Label label_FileNameNew;
	
	@FXML
	private Button button_Ok;
	@FXML
	private Button button_Cancel;
	
	//
	private ObservableList<String> OList_FileType;                    // for ComboBox "file type"
	private Preferences prefs;
	private DateConv dateConv;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateFileEdit_Controller () {
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
    	OList_FileType = FXCollections.observableArrayList();
		OList_FileType.add("Текстовый");
		OList_FileType.add("Картинка");
		//OList_FileType.add("Бинарный");              // !!! пока не реализовываем
		comboBox_FileType.setItems(OList_FileType);
		
		if (actionType == ACTION_TYPE_ADD) {                 
    		
			
			/*
 				if (tiTheme != null)  
    			label_FileThemeId.setText(tiTheme.getValue().getName() +" ("+ Long.toString(tiTheme.getValue().getId()) +")");
    		label_FileNameNew.setText("");
    		
    		comboBox_FileType.setValue(OList_FileType.get(0));    // первый эдемент в списке
    		
    		label_FileDateCreated.setText("");
    		label_FileDateModified.setText("");
    		label_FileUserCreated.setText("");
    		label_FileUserModified.setText("");
			 */
			
			
    	} else if (actionType == ACTION_TYPE_EDIT) {

    		
    		
    		
    		
    		
    	}
    	
    	//======== buttons
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    //TODO initControlsValue
    
    /**
	 * Вызывается для открытия тестового файла
	 */
	@FXML
	private void handleButtonFileTextOpen() {
    
		
		
	
	}
	//TODO handleButtonFileTextOpen
	
	/**
	 * Вызывается для сохранения тестового файла на диске
	 */
	@FXML
	private void handleButtonFileTextSave() {
		
		
		
	}
    //TODO handleButtonFileTextSave
	
	/**
	 * Вызывается для открытия файла с картинкой
	 */
	@FXML
	private void handleButtonFileImageOpen() {
		
		
		
		
		
	}
	//TODO handleButtonFileImageOpen
	
	/**
	 * Вызывается для сохранения файла с картинкой на диске
	 */
	@FXML
	private void handleButtonFileImageSave() {
		
		
		
		
	}
	//TODO handleButtonFileImageSave
	
    /**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	TemplateFileItem fi;
    	
    	//-------- save stage position
    	prefs.putDouble("stageTemplateFileEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateFileEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateFileEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateFileEdit_PosY",  params.getStageCur().getY());
    	

    	
    	
    	
//TODO Ok    	
    	
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
    	prefs.putDouble("stageTemplateFileEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateFileEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateFileEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateFileEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
