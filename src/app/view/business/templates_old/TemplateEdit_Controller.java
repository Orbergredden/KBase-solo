
package app.view.business.templates_old;

import app.exceptions.KBase_Ex;
import app.exceptions.KBase_ReadTextFileUTFEx;
import app.lib.DateConv;
import app.lib.FileCache;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.InfoTypeItem;
import app.model.business.InfoTypeStyleItem;
import app.model.business.templates_old.TemplateItem;
import app.model.business.templates_old.TemplateRequiredFileItem;
import app.model.business.templates_old.TemplateSimpleItem;
import app.model.business.templates_old.TemplateThemeItem;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Контроллер окна добавления/редактирования шаблона (или файла темы)
 * 
 * @author Igor Makarevich
 */
public class TemplateEdit_Controller {
	
	private Params params;
	private DBConCur_Parameters conn;
    /////////////////////private TemplatesList_Controller tmplListC;
    /**
     * Тип операции : 1 - добавить ; 2 - редактировать
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
	//
	////////////////////////private Stage dialogStage;
	
	@FXML
	private TabPane tabPane_main;
	@FXML
	private Tab tab_theme;
	@FXML
	private Tab tab_file;
	@FXML
	private Tab tab_template;
	
	@FXML
	private ComboBox<String> comboBox_TypeForAdd;
	
	@FXML
	private Label label_ThemeId;
	@FXML
	private TextField textField_ThemeName;
	@FXML
	private TextField textField_ThemeDescr;
	@FXML
	private Label label_ThemeDateCreated;
	@FXML
	private Label label_ThemeDateModified;
	@FXML
	private Label label_ThemeUserCreated;
	@FXML
	private Label label_ThemeUserModified;
	
	@FXML
	private Label label_TitleFile;
	@FXML
	private Label label_FileThemeId;
	@FXML
	private TextField textField_FileName;
	@FXML
	private TextField textField_FileDescr;
	@FXML
	private Button button_FileTextOpen;
	@FXML
	private Button button_FileTextSaveToDisk;
	@FXML
	private Button button_FileImageOpen;
	@FXML
	private Button button_FileImageSaveToDisk;
	@FXML
	private Label label_FileNameNew;
	@FXML
	private ComboBox<String> comboBox_FileType;
	@FXML
	private TabPane tabPane_fileContent;
	@FXML
	private Tab tab_fileContentText;
	@FXML
	private Tab tab_fileContentImage;
	@FXML
	private TextArea textArea_FileContent;
	@FXML
	private ImageView imageView_FileContent;
	@FXML
	private Label label_FileDateCreated;
	@FXML
	private Label label_FileDateModified;
	@FXML
	private Label label_FileUserCreated;
	@FXML
	private Label label_FileUserModified;
	
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
	private Label label_TitleTemplate;
	@FXML
	private Label label_TemplateThemeId;
	@FXML
	private Label label_TemplateInfoTypeId;
	@FXML
	private Label label_TemplateInfoTypeStyleId;
	@FXML
	private TextField textField_TemplateName;
	@FXML
	private TextField textField_TemplateFileName;
	@FXML
	private TextField textField_TemplateDescr;
	@FXML
	private Button button_TemplateFileOpen;
	@FXML
	private Button button_TemplateFileSaveToDisk;
	@FXML
	private Button button_TemplateFileSaveToDB;
	@FXML
	private TextArea textArea_TemplateContent;
	@FXML
	private Label label_TemplateDateCreated;
	@FXML
	private Label label_TemplateDateModified;
	@FXML
	private Label label_TemplateUserCreated;
	@FXML
	private Label label_TemplateUserModified;
	
	@FXML
	private Button button_Prev;
	@FXML
	private Button button_Next;
	@FXML
	private Button button_Cancel;
	
	//
	private ObservableList<String> OList_TypeForAdd;                  // for ComboBox "Type for add"
	private ObservableList<String> OList_FileType;                    // for ComboBox "Type for add"
	private TreeItem<TemplateSimpleItem> tiTheme;    // TreeItem текущей темы (for add type)
	private TreeItem<TemplateSimpleItem> tiFileDir;  // TreeItem ветки с обязательными файлами
	private TreeItem<TemplateSimpleItem> tiTmplType; // TreeItem текущего типа шаблонов (for add type)
	
	private Preferences prefs;
	private DateConv dateConv;
	
	// номера табов в окне
	private static final int TAB_ADD_SELECT = 0;
	private static final int TAB_THEME      = 1;
	private static final int TAB_FILE       = 2;
	private static final int TAB_STYLE      = 3;
	private static final int TAB_TEMPLATE   = 4;
	
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
    private void initialize() {
    	
    }
	
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     * 
     * @param 
     *        actionType : 1 - добавить ; 2 - редактировать
     */
//    public void setParrentObj(TemplatesList_Controller parrentObj, int actionType, 
//    		TreeItem<TemplateSimpleItem> editedItem_ti,
//    		Stage dialogStage) {
    public void setParams(Params params, int actionType, 
    		TreeItem<TemplateSimpleItem> editedItem_ti) {
    	this.params     = params;
    	this.conn       = params.getConCur();
    	this.actionType = actionType;
    	
    	
    	
//        this.tmplListC = parrentObj;
//        this.dialogStage = dialogStage;
        
        // init controls
        this.editedItem_ti = editedItem_ti;
        if (editedItem_ti != null) this.editedItem = editedItem_ti.getValue();
        else                       this.editedItem = null;
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
    	tabPane_main.getStyleClass().add("TemplateEdit_TabPane");
    	
    	OList_FileType = FXCollections.observableArrayList();
		OList_FileType.add("Текстовый");
		OList_FileType.add("Картинка");
		//OList_FileType.add("Бинарный");              // !!! пока не реализовываем
		comboBox_FileType.setItems(OList_FileType);
		
		initComboBoxTypeForAdd();            // инициализируем ComboBox "Тип добавления" и некоторые переменные
		
    	if (actionType == 1) {         // add
    		//---- Theme
    		label_ThemeId.setText("");
    		label_ThemeDateCreated.setText("");
    		label_ThemeDateModified.setText("");
    		label_ThemeUserCreated.setText("");
    		label_ThemeUserModified.setText("");
    		
    		//---- File
    		if (tiTheme != null)  
    			label_FileThemeId.setText(tiTheme.getValue().getName() +" ("+ Long.toString(tiTheme.getValue().getId()) +")");
    		label_FileNameNew.setText("");
    		
    		comboBox_FileType.setValue(OList_FileType.get(0));    // первый эдемент в списке
    		
    		label_FileDateCreated.setText("");
    		label_FileDateModified.setText("");
    		label_FileUserCreated.setText("");
    		label_FileUserModified.setText("");
    		
    		//---- Style
    		InfoTypeStyleItem sip = null;
    		label_StyleId.setText("");
    		
    		if ((editedItem_ti != null) && 
    			((editedItem.getType() == TemplateSimpleItem.TYPE_STYLE) || 
    			 (editedItem.getType() == TemplateSimpleItem.TYPE_TEMPLATE))) {
    			if (editedItem.getType() == TemplateSimpleItem.TYPE_STYLE) {
    				sip = conn.db.infoTypeStyleGet(editedItem.getId());
    			} else {
    				sip = conn.db.infoTypeStyleGet(editedItem.getSubItem().getId());
    			}
    			label_StyleParentId.setText(sip.getName() +" ("+ Long.toString(sip.getId()) +")");
    		} else  
    			label_StyleParentId.setText("");

    		if (tiTmplType != null)                    // показываем тип инфо блока  
    			label_StyleInfoTypeId.setText(tiTmplType.getValue().getName() +" ("+
    					                         Long.toString(tiTmplType.getValue().getId()) +")");
    		label_StyleDateCreated.setText("");
    		label_StyleDateModified.setText("");
    		label_StyleUserCreated.setText("");
    		label_StyleUserModified.setText("");
    		
    		// default
    		imageView_StyleDef.setImage(new Image("file:resources/images/icon_default_item_16.png"));
    		label_StyleDefDate.setText("");

    		if (tiTmplType != null)
    			sip = conn.db.infoTypeStyleGetDefault (tiTheme.getValue().getId(), tiTmplType.getValue().getId());

    		if (sip == null) {
    			label_StyleDefCur.setText("нету");
    		} else {
    			label_StyleDefCur.setText(sip.getName() +" ("+ sip.getId() +")");
    		}
    		
    		//---- Template
    		if (tiTheme != null)  
    			label_TemplateThemeId.setText(tiTheme.getValue().getName() +" ("+ 
    					                      Long.toString(tiTheme.getValue().getId()) +")");
    		if (tiTmplType != null)  
    			label_TemplateInfoTypeId.setText(tiTmplType.getValue().getName() +" ("+
    					                         Long.toString(tiTmplType.getValue().getId()) +")");
    		if ((editedItem_ti != null) && (editedItem.getType() == TemplateSimpleItem.TYPE_STYLE))
    			label_TemplateInfoTypeStyleId.setText(editedItem.getName() +" ("+ Long.toString(editedItem.getId()) +")");
    		
    		label_TemplateDateCreated.setText("");
    		label_TemplateDateModified.setText("");
    		label_TemplateUserCreated.setText("");
    		label_TemplateUserModified.setText("");
    		
    	}  else if (actionType == 2) {         // update
    		//---- переключаемся на нужный таб
    		switch (editedItem.getType()) {
    		case TemplateSimpleItem.TYPE_THEME :                           // 1 - тема
    			tabPane_main.getSelectionModel().select(TAB_THEME);
    			// initialize fields on the tab
    			label_ThemeId.setText(Long.toString(editedItem.getId()));
    			textField_ThemeName.setText(editedItem.getName());
    			textField_ThemeDescr.setText(editedItem.getDescr());
    			label_ThemeDateCreated.setText(dateConv.dateTimeToStr(editedItem.getDateCreated()));
    			label_ThemeDateModified.setText(dateConv.dateTimeToStr(editedItem.getDateModified()));
    			label_ThemeUserCreated.setText(editedItem.getUserCreated());
    			label_ThemeUserModified.setText(editedItem.getUserModified());
    			break;
    		case TemplateSimpleItem.TYPE_FILE :                           // 3 - обязательный файл
    			//TemplateRequiredFileItem trf = tmplListC.conn.db.templateFileGetById(editedItem.getId());
    			TemplateRequiredFileItem trf = (TemplateRequiredFileItem)editedItem;
    			
    			tabPane_main.getSelectionModel().select(TAB_FILE);
    			label_TitleFile.setText("Файл " + editedItem.getName() +" ("+ editedItem.getId() +")");
    			label_FileThemeId.setText(tiTheme.getValue().getName() +" ("+ Long.toString(tiTheme.getValue().getId()) +")");
        		textField_FileName.setText(trf.getFileName());
        		if (editedItem.getFileType() != 0) {
        			textField_FileName.setDisable(true);
        		}
        		textField_FileDescr.setText(editedItem.getDescr());
        		label_FileNameNew.setText("");
        		
        		switch (editedItem.getFileTypeExt()) {
        		case TemplateSimpleItem.FILETYPEEXT_TEXT : 
        			comboBox_FileType.setValue(OList_FileType.get(0));
        			tabPane_fileContent.getSelectionModel().select(0);
        			textArea_FileContent.appendText(trf.getBody());
        			break;
        		case TemplateSimpleItem.FILETYPEEXT_IMAGE : 
        			comboBox_FileType.setValue(OList_FileType.get(1));
        			tabPane_fileContent.getSelectionModel().select(1);
        			textArea_FileContent.appendText("Ширина : "+ trf.bodyImage.getWidth() +"\n");
            		textArea_FileContent.appendText("Высота : "+ trf.bodyImage.getHeight() +"\n");
        			imageView_FileContent.setImage(trf.bodyImage);
        			break;
        		}
        		
        		label_FileDateCreated.setText(dateConv.dateTimeToStr(editedItem.getDateCreated()));
        		label_FileDateModified.setText(dateConv.dateTimeToStr(editedItem.getDateModified()));
        		label_FileUserCreated.setText(editedItem.getUserCreated());
        		label_FileUserModified.setText(editedItem.getUserModified());
    			break;
    		case TemplateSimpleItem.TYPE_STYLE :
    			initTabStyleForEdit();
    			break;
    		case TemplateSimpleItem.TYPE_TEMPLATE : 
    			if (ShowAppMsg.showQuestionWith2Buttons("CONFIRMATION", "Изменение стиля/шаблона", 
    					"Текущая позиция содержит стиль и шаблон", "Что изменяем ?", "Стиль", "Шаблон")
    					== ShowAppMsg.SELECT_BUTTON_1) {
    				initTabStyleForEdit();
    			} else {
    				//TemplateItem tip = tmplListC.conn.db.templateGet(editedItem.getFileType());    // id of current template
    				TemplateItem tip = conn.db.templateGet(editedItem.getId());
    				//InfoTypeStyleItem sip = tmplListC.conn.db.infoTypeStyleGet(editedItem.getId());// id of current style
    				InfoTypeStyleItem sip = conn.db.infoTypeStyleGet(editedItem.getSubItem().getId());
        			
        			tabPane_main.getSelectionModel().select(TAB_TEMPLATE);
        			label_TitleTemplate.setText("ШАБЛОН : "+ tip.getName() +" ("+ Long.toString(tip.getId()) +")");
        			if (tiTheme != null)  
            			label_TemplateThemeId.setText(tiTheme.getValue().getName() +" ("+ 
            					                      Long.toString(tiTheme.getValue().getId()) +")");
            		if (tiTmplType != null)  
            			label_TemplateInfoTypeId.setText(tiTmplType.getValue().getName() +" ("+
            					                         Long.toString(tiTmplType.getValue().getId()) +")");
            		label_TemplateInfoTypeStyleId.setText(sip.getName() +" ("+ sip.getId() +")");
            		
            		textField_TemplateName.setText(tip.getName());
            		textField_TemplateFileName.setText(tip.getFileName());
            		textField_TemplateDescr.setText(tip.getDescr());
            		textArea_TemplateContent.appendText(tip.getBody());
        			
            		label_TemplateDateCreated.setText(dateConv.dateTimeToStr(tip.getDateCreated()));
            		label_TemplateDateModified.setText(dateConv.dateTimeToStr(tip.getDateModified()));
            		label_TemplateUserCreated.setText(tip.getUserCreated());
            		label_TemplateUserModified.setText(tip.getUserModified());
    			}
    			break;
    		default :
    			ShowAppMsg.showAlert("WARNING", "Редактирование шаблонов", "непонятка", "Неизвестного типа элемент дерева");
    			//handleButtonCancel();            // not close
    		}
    	}
    	
    	//======== buttons
    	button_FileTextOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
    	button_FileTextOpen.setTooltip(new Tooltip("Открыть файл..."));
    	button_FileTextSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
    	button_FileTextSaveToDisk.setTooltip(new Tooltip("Сохранить файл на диске..."));
    	
    	button_FileImageOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
    	button_FileImageOpen.setTooltip(new Tooltip("Открыть файл..."));
    	button_FileImageSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
    	button_FileImageSaveToDisk.setTooltip(new Tooltip("Сохранить файл на диске..."));

		button_TemplateFileOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
		button_TemplateFileOpen.setTooltip(new Tooltip("Открыть шаблон..."));
		button_TemplateFileSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
		button_TemplateFileSaveToDisk.setTooltip(new Tooltip("Сохранить шаблон на диске..."));
		button_TemplateFileSaveToDB.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
		button_TemplateFileSaveToDB.setTooltip(new Tooltip("Сохранить шаблон в базе данных..."));

    	if (actionType == 1) {         // add
    		button_Prev.setDisable(true);
    		button_Prev.setGraphic(new ImageView(new Image("file:resources/images/icon_previous_16.png")));
    		button_Next.setGraphic(new ImageView(new Image("file:resources/images/icon_next_16.png")));
    	} else if (actionType == 2) {         // update
    		button_Prev.setVisible(false);
    		button_Next.setText("Сохранить");
    		button_Next.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	}
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * Инициализируем ComboBox "Тип добавления" в зависимости от типа записи в TemplateSimpleItem
     */
    private void initComboBoxTypeForAdd () {
    	TreeItem<TemplateSimpleItem> tiTmp;
    	TemplateSimpleItem tftTmp;
    	boolean is_kbase_main = false;
    	boolean is_no_info    = false;
    	boolean is_load       = false;
    	
    	tiTmplType = null;               // TreeItem текущего типа шаблонов
    	
    	//======== определяем текущее местоположение в дереве и наличие файлов в теме
    	if (editedItem_ti != null) {
			//---- находим элемент-тему
			if (editedItem.getType() == TemplateSimpleItem.TYPE_THEME) tiTheme = editedItem_ti;         // 1 - тема
			else {
				tiTmp = editedItem_ti.getParent();
				while (tiTmp.getValue().getType() != TemplateSimpleItem.TYPE_THEME) {
					tiTmp = tiTmp.getParent();
				}
				tiTheme = tiTmp;
			}

			//---- ищем именованые файлы
			for (int i=0; i<tiTheme.getChildren().size(); i++) {
				tftTmp = tiTheme.getChildren().get(i).getValue();
				if (tftTmp.getType() == TemplateSimpleItem.TYPE_DIR_FOR_FILES) {            // 2 - папка для обязательных файлов
					tiFileDir = tiTheme.getChildren().get(i);
				}
				if (tftTmp.getType() == TemplateSimpleItem.TYPE_FILE) {            // 3 - обязательный файл
					switch ((int)tftTmp.getFileType()) {     // 1 - основной файл; 2 - "Информация отсутствует"; 3 - "Загрузка..."
						case TemplateSimpleItem.FILETYPE_MAIN_FILE    : is_kbase_main = true; break;
						case TemplateSimpleItem.FILETYPE_NO_INFO_FILE : is_no_info    = true; break;
						case TemplateSimpleItem.FILETYPE_LOAD_FILE    : is_load       = true; break;
					}
				}
			}
			
			//---- ищем тип шаблона
			switch (editedItem.getType()) {
			case TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES :                              // 4 - папка для шаблонов определенного типа
				tiTmplType = editedItem_ti;
				break;
			case TemplateSimpleItem.TYPE_STYLE :
			case TemplateSimpleItem.TYPE_TEMPLATE :                              // 5 - шаблон
				tiTmp = editedItem_ti.getParent();
				while (tiTmp.getValue().getType() != TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES) {
					tiTmp = tiTmp.getParent();
				}
				tiTmplType = tiTmp;
				
				break;
			default :
				tiTmplType = null;
			}
		}
    	
    	//======== формируем список
    	OList_TypeForAdd = FXCollections.observableArrayList();
		
		OList_TypeForAdd.add("Тема");
		if (editedItem_ti != null) {
			if (! is_kbase_main)       OList_TypeForAdd.add("Файл kbase_main.html");
			if (! is_no_info)          OList_TypeForAdd.add("Файл no_info.html");
			if (! is_load)             OList_TypeForAdd.add("Файл load.html");
			OList_TypeForAdd.add("Обязательный файл");
			if (tiTmplType != null)    OList_TypeForAdd.add("Стиль");
			if ((tiTmplType != null) && (editedItem.getType() == TemplateSimpleItem.TYPE_STYLE))    
				OList_TypeForAdd.add("Шаблон");
		}
		
    	comboBox_TypeForAdd.setItems(OList_TypeForAdd);
    	comboBox_TypeForAdd.setValue(OList_TypeForAdd.get(0));    // первый эдемент в списке
    }
    
    /**
     * Инициализирует таб стиля в режиме редактирования айтема
     */
    private void initTabStyleForEdit () {
    	InfoTypeStyleItem sip = null;       // id of current style
    	InfoTypeStyleItem pip = null;       // id of parent style
    	
    	switch (editedItem.getType()) {
    		case TemplateSimpleItem.TYPE_STYLE :
    			sip = conn.db.infoTypeStyleGet(editedItem.getId());
    			break;
    		case TemplateSimpleItem.TYPE_TEMPLATE :
    			sip = conn.db.infoTypeStyleGet(editedItem.getSubItem().getId());
    			break;
    	}
    	
    	tabPane_main.getSelectionModel().select(TAB_STYLE);
    	if (sip.getParentId() > 0)
    		pip = conn.db.infoTypeStyleGet(sip.getParentId()); // id of parent style
    	
    	label_StyleId.setText(Long.toString(sip.getId()));
    	if (pip != null) label_StyleParentId.setText(pip.getName() +" ("+ Long.toString(pip.getId()) +")");
    	else             label_StyleParentId.setText("");
    	if (tiTmplType != null)  
    		label_StyleInfoTypeId.setText(tiTmplType.getValue().getName() +" ("+
					                      Long.toString(tiTmplType.getValue().getId()) +")");
    	textField_StyleName.setText(sip.getName());
    	textField_StyleDescr.setText(sip.getDescr());

    	label_StyleDateCreated.setText(dateConv.dateTimeToStr(sip.getDateCreated()));
    	label_StyleDateModified.setText(dateConv.dateTimeToStr(sip.getDateModified()));
    	label_StyleUserCreated.setText(sip.getUserCreated());
    	label_StyleUserModified.setText(sip.getUserModified());
    	
    	//--------default
    	imageView_StyleDef.setImage(new Image("file:resources/images/icon_default_item_16.png"));
    	if (conn.db.infoTypeStyleIsDefault (tiTheme.getValue().getThemeId(), sip.getId())) {
    		checkBox_StyleDef.setSelected(true);
    		label_StyleDefDate.setText(dateConv.dateTimeToStr(
    				conn.db.infoTypeStyleGetDefaultDateModified(tiTheme.getValue().getThemeId(), sip.getId())));
    	} else {
    		label_StyleDefDate.setText("");
    	}
		
		InfoTypeStyleItem iti = conn.db.infoTypeStyleGetDefault (tiTheme.getValue().getId(), tiTmplType.getValue().getId());
		if (iti == null) {
			label_StyleDefCur.setText("нету");
		} else {
			label_StyleDefCur.setText(iti.getName() +" ("+ iti.getId() +")");
		}
    }
    
    /**
     * Добавляет новый стиль в дерево-контрол во все темы
     */
    private void addStyleItemToTreeRecursive (TreeItem<TemplateSimpleItem> parentTI, InfoTypeStyleItem new_sip) {
    	// получаем список дочерних элементов
    	List<TreeItem<TemplateSimpleItem>> oList = parentTI.getChildren(); 

    	// делаем цикл по дочерним элементам
    	for (TreeItem<TemplateSimpleItem> i : oList) {
    		//TemplateSimpleItem tft = new TemplateSimpleItem(i.getValue());
            TemplateSimpleItem tft = i.getValue();
    		boolean isFound = false;
    		
    		// проверяем элемент на нужный стиль
    		if ((tft.getType() == TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES) && 
    			(new_sip.getParentId() == 0) && 
    			(tft.getId() == new_sip.getInfoTypeId()))
    			isFound = true;
    		if ((tft.getType() == TemplateSimpleItem.TYPE_STYLE) && (tft.getId() == new_sip.getParentId()))
    			isFound = true;
            if ((tft.getType() == TemplateSimpleItem.TYPE_TEMPLATE) && (tft.getSubItem().getId() == new_sip.getParentId()))
                isFound = true;

    		// создаем элемент для вставки. Вставляем новый элемент как его дочерний с нужным ИД темы
    		if (isFound) {
    			TreeItem<TemplateSimpleItem> item = new TreeItem<>(new TemplateSimpleItem(
        				new_sip.getId(),        // style id
        				tft.getThemeId(),
        				new_sip.getName(),
        				new_sip.getDescr(),
        				TemplateSimpleItem.TYPE_STYLE,
        				new_sip.getInfoTypeId(),
        				0,
        				new_sip.getDateCreated(),
        				new_sip.getDateModified(),
        				new_sip.getUserCreated(),
        				new_sip.getUserModified()
                		));
    			i.getChildren().add(item);
    		}
    		
    		// вызываем эту ф-цию для проверки элементов следующей глубины вложения для текущего элемента
    		addStyleItemToTreeRecursive (i, new_sip);
    	}
    }
    
    /**
     * Изменяет стиль в дереве-контроле во всех темах
     */
    private void updateStyleItemToTreeRecursive (TreeItem<TemplateSimpleItem> parentTI, InfoTypeStyleItem new_sip) {
    	// получаем список дочерних элементов
    	List<TreeItem<TemplateSimpleItem>> oList = parentTI.getChildren(); 

    	// делаем цикл по дочерним элементам
    	for (TreeItem<TemplateSimpleItem> i : oList) {
    		//TemplateSimpleItem tft = new TemplateSimpleItem(i.getValue());
    		TemplateSimpleItem tft = i.getValue();
    		
    		// проверяем элемент на нужный стиль
    		if (((tft.getType() == TemplateSimpleItem.TYPE_STYLE) && (tft.getId() == new_sip.getId())) || 
    		    ((tft.getType() == TemplateSimpleItem.TYPE_TEMPLATE) &&
    		     //(tft.getSubItem() != null) &&
    		     (tft.getSubItem().getId() == new_sip.getId()))) {
    			
    			TemplateSimpleItem ttt = null;
    			
    			// создаем элемент для замены. Вставляем новый элемент с нужным ИД темы
    			if (tft.getType() == TemplateSimpleItem.TYPE_STYLE) {
    				ttt = new TemplateSimpleItem(
            				new_sip.getId(),        // style id
            				tft.getThemeId(),
            				new_sip.getName(),
            				new_sip.getDescr(),
            				TemplateSimpleItem.TYPE_STYLE,
            				0,
            				0,
            				new_sip.getDateCreated(),
            				new_sip.getDateModified(),
            				new_sip.getUserCreated(),
            				new_sip.getUserModified()
                    		);
    			}
    			
    			if (tft.getType() == TemplateSimpleItem.TYPE_TEMPLATE) {
    				TemplateItem tip = conn.db.templateGet(tft.getId());   // template id
    				
    				TemplateSimpleItem sss = new TemplateSimpleItem(
    						new_sip.getId(),        // style id
            				tft.getSubItem().getThemeId(),
            				new_sip.getName(),
            				new_sip.getDescr(),
            				TemplateSimpleItem.TYPE_STYLE,
            				0,
            				0,
            				new_sip.getDateCreated(),
            				new_sip.getDateModified(),
            				new_sip.getUserCreated(),
            				new_sip.getUserModified()
    						);
    				ttt = new TemplateSimpleItem(
            				tip.getId(),        // style id
            				tip.getThemeId(),
            				new_sip.getName() +" : "+ tip.getName(),
            				new_sip.getDescr() +" : "+ tip.getDescr() +" ("+ tip.getFileName() +")",
            				TemplateSimpleItem.TYPE_TEMPLATE,         // 5 - шаблон
            				0,
                    		1,
                    		tip.getDateCreated(),
                    		tip.getDateModified(),
                    		tip.getUserCreated(),
                    		tip.getUserModified()
                    		);
    				ttt.setSubItem(sss);
    			}
    			i.setValue(null);
        		i.setValue(ttt);
    		}

    		// вызываем эту ф-цию для проверки элементов следующей глубины вложения для текущего элемента
    		updateStyleItemToTreeRecursive (i, new_sip);
    	}
    }
    
    /**
     * Сохраняет значение по умолчанию для указанного стиля (в указанной теме для указанного пользователя)
     */
    private void styleSetDefault (long themeId, InfoTypeStyleItem itsi, boolean isSelected) {
    	if (isSelected) {
    		conn.db.infoTypeStyleSetDefault(themeId, itsi.getId());
    	} else {
    		conn.db.infoTypeStyleUnsetDefault(themeId, itsi.getId());
    	}
    	//TODO
    }

	/**
	 * Сохранение темы (добавление, обновление) в БД и компоненте-дереве
	 */
	private void saveTheme () throws KBase_Ex {
		//---------- check data in fields
		if ((textField_ThemeName.getText().equals("") || (textField_ThemeName.getText() == null))) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название темы", "Укажите Название темы");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Название темы", this);
		}

		//-------- Сохраняем
		if (actionType == 1) {                                              //======== add theme
			TemplateThemeItem tip;
			long newId = conn.db.templateThemeNextId();

			tip = new TemplateThemeItem (
					newId,
					textField_ThemeName.getText(),
					textField_ThemeDescr.getText()
			);
			conn.db.templateThemeAdd(tip);             // обьект-тему добавляем в БД
			tip = conn.db.templateThemeGetById(newId); // get full info
			tip.setThemeId(tip.getId());
			tip.setType(TemplateSimpleItem.TYPE_THEME);

			//--- добавляем в контрол-дерево
			TreeItem<TemplateSimpleItem> item = new TreeItem<>(tip);
			//tmplListC.treeViewCtrl.root.getChildren().add(item);
			((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.root.getChildren().add(item);
			
			// Make sure the new item is visible
			//tmplListC.root.setExpanded(true);
			// обязательные файлы
			TemplateSimpleItem fl =
					new TemplateSimpleItem (0, 0, "Обязательные файлы", "папка такая",
							TemplateSimpleItem.TYPE_DIR_FOR_FILES, 0, 0);
			TreeItem<TemplateSimpleItem> folderItem = new TreeItem<>(fl);
			item.getChildren().add(folderItem);

			// типы инфо блоков, внутри которых шаблоны
			List<InfoTypeItem> infoTypeList = conn.db.infoTypeList();
			for (InfoTypeItem j : infoTypeList) {       // cycle for infotypes
				TreeItem<TemplateSimpleItem> infoTypeItem = new TreeItem<>(
						new TemplateSimpleItem(j.getId(), 0, j.getName(), j.getDescr(),
								TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES, 0, 0));
				item.getChildren().add(infoTypeItem);
			}

			// определяем текущий активный итем
			resultItem = item;

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Тема шаблонов '" + tip.getName() + "' добавлена.");

		} else if (actionType == 2) {                                    //======== update theme
			TemplateThemeItem tip;

			// create theme object and update it into db
			tip = new TemplateThemeItem (
					editedItem.getId(),
					textField_ThemeName.getText(),
					textField_ThemeDescr.getText()
			);
			conn.db.templateThemeUpdate(tip);
			tip = conn.db.templateThemeGetById(editedItem.getId()); // get full info

			// update in TreeTableView
			editedItem_ti = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					editedItem.getId(),
					editedItem.getType());
			if (editedItem_ti == null) {
				ShowAppMsg.showAlert("WARNING", "Нет данных", "Не найден элемент Тема в дереве", editedItem.getName() + " (" + editedItem.getId() + ")");
			} else {
				editedItem_ti.setValue(null);
				editedItem_ti.setValue(tip);
			}

			// определяем текущий активный итем
			resultItem = editedItem_ti;

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Тема '" + tip.getName() + "' изменена.");
		}
	}

	/**
	 * Сохранение обязательного файла (добавление, обновление) в БД и компоненте-дереве
	 */
	private void saveRequiredFile () throws KBase_Ex {
		int fileTypeExt;

		switch (comboBox_FileType.getSelectionModel().getSelectedItem()) {
			case "Текстовый" :
				fileTypeExt = TemplateSimpleItem.FILETYPEEXT_TEXT;
				break;
			case "Картинка" :
				fileTypeExt = TemplateSimpleItem.FILETYPEEXT_IMAGE;
				break;
			default :
				fileTypeExt = 0;
		}

		//---------- check data in fields
		if ((textField_FileName.getText().equals("") || (textField_FileName.getText() == null))) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Имя файла", "Укажите Имя файла");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Имя файла", this);
		}
		if (	(fileTypeExt == TemplateSimpleItem.FILETYPEEXT_TEXT) &&
				((textArea_FileContent.getText().equals("")) || (textArea_FileContent.getText() == null))) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Нет текста файла",
			//		"Укажите текст файла или измените тип.");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Нет текста файла", this);
		}
		if ((fileTypeExt == TemplateSimpleItem.FILETYPEEXT_IMAGE) && (imageView_FileContent.getImage() == null)) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Нет картинки",
			//		"Выберите картинку для файла или измените тип.");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Нет картинки", this);
		}

		//-------- Сохраняем
		if (actionType == 1) {                                  //======== add file
			TemplateRequiredFileItem fip;
			long newId = conn.db.templateFileNextId();
			int fileType;

			// проверка на уникальность имени файла
			if (conn.db.templateFileIsExistNameInTheme(
					tiTheme.getValue().getId(), textField_FileName.getText())) {
				//ShowAppMsg.showAlert("WARNING", "Добавление файла.",
				//		"Файл с таким именем уже существует в редактируемой теме.",
				//		"Добавление прервано.");
				//return;
				throw new KBase_Ex (1, "Ошибка при сохранении", "Файл с таким именем уже существует в редактируемой теме.", this);
			}

			//--------
			switch(textField_FileName.getText()) {
				case "kbase_main.html" :
					fileType = TemplateSimpleItem.FILETYPE_MAIN_FILE;
					break;
				case "no_info.html" :
					fileType = TemplateSimpleItem.FILETYPE_NO_INFO_FILE;
					break;
				case "load.html" :
					fileType = TemplateSimpleItem.FILETYPE_LOAD_FILE;
					break;
				default :
					fileType = 0;
			}

			fip = new TemplateRequiredFileItem (
					newId,
					tiTheme.getValue().getId(),
					textField_FileName.getText(),
					textField_FileDescr.getText(),
					textArea_FileContent.getText(),
					imageView_FileContent.getImage(),
					fileType,
					fileTypeExt
			);
			conn.db.templateFileAdd(fip, label_FileNameNew.getText());  // обьект-файл добавляем в БД
			fip = conn.db.templateFileGetById(newId);                   // get full info

			//--- добавляем в контрол-дерево
			// Prepare a new TreeItem with a new templateTheme object
			TreeItem<TemplateSimpleItem> item = new TreeItem<>(fip);
			// Add the new item as children to the parent item
			// Make sure the new item is visible
			if (fip.getFileType() == TemplateSimpleItem.FILETYPE_REQUIRED_FILE) {
				tiFileDir.getChildren().add(item);
				tiFileDir.setExpanded(true);
			} else {
				tiTheme.getChildren().add(item);
				tiTheme.setExpanded(true);
			}

			// определяем текущий активный итем
			resultItem = item;

			//--- при необходимости кешируем файл на диске
			FileCache fileCache = new FileCache (conn, fip.getThemeId());
			fileCache.updateRequredFile(fip);

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Файл для шаблона '" + fip.getFileName() + "' добавлен.");

		} else if (actionType == 2) {                          //======== update file
			TemplateRequiredFileItem rfi = conn.db.templateFileGetById(editedItem.getId()); // get full info

			//-------- дополнительные проверки
			// только для текстового файла
			if (	(fileTypeExt == TemplateSimpleItem.FILETYPEEXT_TEXT) &&
					textArea_FileContent.getText().equals(rfi.getBody()) &&
					textField_FileName.getText().equals(editedItem.getName()) &&
					textField_FileDescr.getText().equals(editedItem.getDescr())) {
				//ShowAppMsg.showAlert("WARNING", "Изменение файла", "Нет никаких изменений", "Укажите новые значения.");
				//return;
				throw new KBase_Ex (1, "Ошибка при сохранении", "Нет никаких изменений", this);
			}
			// проверка на дублирование имени файла
			if (conn.db.templateFileIsExistNameInTheme(rfi.getThemeId(), textField_FileName.getText()) &&
					(conn.db.templateFileGet(rfi.getThemeId(), textField_FileName.getText()).getId() != rfi.getId())) {
				//ShowAppMsg.showAlert("WARNING", "Изменение файла.",
				//		"Файл с таким именем уже существует в редактируемой теме.",
				//		"Изменение прервано.");
				//return;
				throw new KBase_Ex (1, "Ошибка при сохранении", "Файл с таким именем уже существует в редактируемой теме.", this);
			}

			// prepare
			String body = textArea_FileContent.getText();
			Image bodyImg = null;

			if (	(fileTypeExt == TemplateSimpleItem.FILETYPEEXT_IMAGE) &&
					(! label_FileNameNew.getText().equals("")) && (label_FileNameNew.getText() != null)) {
				bodyImg = imageView_FileContent.getImage();
			}

			// create file object and update it into db
			TemplateRequiredFileItem fip = new TemplateRequiredFileItem (
					editedItem.getId(),
					editedItem.getThemeId(),
					textField_FileName.getText(),
					textField_FileDescr.getText(),
					body,
					bodyImg,
					(int)editedItem.getFileType(),
					editedItem.getFileTypeExt(),
					editedItem.getDateCreated(),
					null,
					editedItem.getUserCreated(),
					null
			);
			conn.db.templateFileUpdate (fip, label_FileNameNew.getText());
			fip = conn.db.templateFileGetById(editedItem.getId()); // get full info

			// update in TreeTableView
			editedItem_ti = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					editedItem.getId(),
					editedItem.getType());
			if (editedItem_ti == null) {
				ShowAppMsg.showAlert("WARNING", "Нет данных", "Не найден элемент Файл", editedItem.getName() + " (" + editedItem.getId() + ")");
			} else {
				editedItem_ti.setValue(null);
				editedItem_ti.setValue(fip);
			}

			// определяем текущий активный итем
			resultItem = editedItem_ti;

			//--- при необходимости кешируем файл на диске
			FileCache fileCache = new FileCache (conn, fip.getThemeId());
			fileCache.updateRequredFile(fip);

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Файл '" + fip.getName() + "' изменен.");
		}
	}

	/**
	 * Сохранение стиля (добавление, обновление) в БД и компоненте-дереве
	 */
	private void saveStyle () throws KBase_Ex {
		//---------- check data in fields
		if ((textField_StyleName.getText().equals("") || (textField_StyleName.getText() == null))) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название стиля", "Укажите Название стиля");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Название стиля", this);
		}

		//-------- Сохраняем
		if (actionType == 1) {                                  //======== add style
			//-------- declare and init variables
			InfoTypeStyleItem sip;
			long newId = conn.db.infoTypeStyleNextId();
			long parentId = 0;

			if (editedItem.getType() == TemplateSimpleItem.TYPE_STYLE)
				parentId = editedItem.getId();
			if (editedItem.getType() == TemplateSimpleItem.TYPE_TEMPLATE)
				parentId = editedItem.getSubItem().getId();

			//-------- add to DB
			sip = new InfoTypeStyleItem (
					newId,
					parentId,
					tiTmplType.getValue().getId(),
					textField_StyleName.getText(),
					textField_StyleDescr.getText()
			);
			conn.db.infoTypeStyleAdd(sip);  // стиль добавляем в БД
			sip = conn.db.infoTypeStyleGet(newId);                   // get full info by Id

			// set/unset default
			styleSetDefault (tiTheme.getValue().getId(), sip, checkBox_StyleDef.isSelected());

			// добавляем в контрол-дерево. Добавляем во все темы.
			addStyleItemToTreeRecursive (((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(), sip);

			// определяем текущий активный итем
			resultItem = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					sip.getId(),
					TemplateSimpleItem.TYPE_STYLE,
					tiTheme.getValue().getId()
					);

			// раскрываем текущий элемент
			editedItem_ti = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					editedItem.getId(),
					editedItem.getType(),
					tiTheme.getValue().getId());
			if (editedItem_ti == null) {
				ShowAppMsg.showAlert(
						"WARNING",
						"Нет данных",
						"Не найден элемент типа " + editedItem.getType(),
						editedItem.getName() + " (" + editedItem.getId() + ")");
			} else {
				editedItem_ti.setExpanded(true);
			}

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Стиль '" + sip.getName() + "' добавлен.");
		} else if (actionType == 2) {                          //======== update style
			InfoTypeStyleItem sip = null;

			if (editedItem.getType() == TemplateSimpleItem.TYPE_STYLE)
				sip = conn.db.infoTypeStyleGet(editedItem.getId());             // id of current style
			if (editedItem.getType() == TemplateSimpleItem.TYPE_TEMPLATE)
				sip = conn.db.infoTypeStyleGet(editedItem.getSubItem().getId());// id of current style

			//-------- дополнительные проверки
			//if (	textField_StyleName.getText().equals(sip.getName()) &&
			//		textField_StyleDescr.getText().equals(sip.getDescr())
			//	) {
			//	ShowAppMsg.showAlert("WARNING", "Изменение стиля", "Нет никаких изменений", "Укажите новые значения.");
			//	return;
			//}

			//-------- create style object and update it into db
			InfoTypeStyleItem usip = new InfoTypeStyleItem (
					sip.getId(),
					sip.getParentId(),
					sip.getInfoTypeId(),
					textField_StyleName.getText(),
					textField_StyleDescr.getText(),
					sip.getDateCreated(),
					null,
					sip.getUserCreated(),
					null
			);
			conn.db.infoTypeStyleUpdate (usip);
			usip = conn.db.infoTypeStyleGet(sip.getId());                   // get full info by Id

			// set/unset default
			styleSetDefault (tiTheme.getValue().getId(), usip, checkBox_StyleDef.isSelected());

			// update in TreeTableView. Изменяем во всех темах
			updateStyleItemToTreeRecursive (((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(), usip);

			// определяем текущий активный итем
			resultItem = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					usip.getId(),
					TemplateSimpleItem.TYPE_STYLE,
					tiTheme.getValue().getId()
			);

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Стиль '" + usip.getName() + "' изменен.");
		}
	}

	/**
	 * Сохранение шаблона в БД и компоненте-дереве
	 */
	private void saveTemplate () throws KBase_Ex {
		//---------- check data in fields
		if ((textField_TemplateName.getText().equals("") || (textField_TemplateName.getText() == null))) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название шаблона", "Укажите Название шаблона");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Название шаблона", this);
		}
		if ((textField_TemplateFileName.getText().equals("") || (textField_TemplateFileName.getText() == null))) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Имя файла шаблона", "Укажите Имя файла шаблона");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Имя файла шаблона", this);
		}
		if ((textArea_TemplateContent.getText().equals("") || (textArea_TemplateContent.getText() == null))) {
			//ShowAppMsg.showAlert("WARNING", "Нет данных", "Нет текста шаблона", "Укажите текст шаблона");
			//return;
			throw new KBase_Ex (1, "Ошибка при сохранении", "Нет текста шаблона", this);
		}

		//-------- Сохраняем
		if (actionType == 1) {                                  //======== add template
			//ShowAppMsg.showAlert("INFORMATION", "Внимание !", "При добавлении нового шаблона не рефрешится дерево !!!", ":(");

			//-------- declare and init variables
			TemplateItem tip;
			long newId = conn.db.templateNextId();
			// style data from DB
			InfoTypeStyleItem sip = conn.db.infoTypeStyleGet(editedItem.getId());// id of current style

			//-------- add to DB
			tip = new TemplateItem (
					newId,
					tiTheme.getValue().getId(),
					tiTmplType.getValue().getId(),
					sip.getId(),
					textField_TemplateName.getText(),
					textField_TemplateFileName.getText(),
					textField_TemplateDescr.getText(),
					textArea_TemplateContent.getText()
			);
			conn.db.templateAdd(tip);  // шаблон добавляем в БД
			tip = conn.db.templateGet(newId);                   // get full info by Id

			//--- добавляем в контрол-дерево
			// Prepare a new TreeItem with a new templateTheme object
			TemplateSimpleItem item = new TemplateSimpleItem(
					tip.getId(),        // template id
					tip.getThemeId(),
					sip.getName() +" : "+ tip.getName(),
					sip.getDescr() +" : "+ tip.getDescr() +" ("+ tip.getFileName() +")",
					TemplateSimpleItem.TYPE_TEMPLATE,         // 5 - шаблон
					0,
					1,
					tip.getDateCreated(),
					tip.getDateModified(),
					tip.getUserCreated(),
					tip.getUserModified()
			);
			item.setSubItem(new TemplateSimpleItem(editedItem));

			// Add the new item
			editedItem_ti = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					editedItem.getId(),
					editedItem.getType());
			if (editedItem_ti == null) {
				ShowAppMsg.showAlert("WARNING", "Нет данных", "Не найден элемент Стиль", editedItem.getName() + " (" + editedItem.getId() + ")");
			} else {
				editedItem_ti.setValue(null);
				editedItem_ti.setValue(item);
			}

			// определяем текущий активный итем
			resultItem = editedItem_ti;

			//--- переводим в режим редактирования
			editedItem = editedItem_ti.getValue();
			actionType = 2;           // edit mode

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Шаблон '" + tip.getName() + "' добавлен.  " + (new Date()));
		} else if (actionType == 2) {                          //======== update template
			TemplateItem utip = conn.db.templateGet(editedItem.getId());   // template id

			//-------- дополнительные проверки
			if (	textField_TemplateName.getText().equals(utip.getName()) &&
					textField_TemplateFileName.getText().equals(utip.getFileName()) &&
					textField_TemplateDescr.getText().equals(utip.getDescr()) &&
					textArea_TemplateContent.getText().equals(utip.getBody())
					) {
				//ShowAppMsg.showAlert("WARNING", "Изменение шаблона", "Нет никаких изменений", "Укажите новые значения.");
				//return;
				throw new KBase_Ex (1, "Ошибка при сохранении", "Нет никаких изменений", this);
			}

			//-------- create template object and update it into db
			TemplateItem tip = new TemplateItem (
					utip.getId(),
					utip.getThemeId(),
					utip.getInfoTypeId(),
					utip.getInfoTypeStyleId(),
					textField_TemplateName.getText(),
					textField_TemplateFileName.getText(),
					textField_TemplateDescr.getText(),
					textArea_TemplateContent.getText(),
					utip.getDateCreated(),
					null,
					utip.getUserCreated(),
					null
			);
			conn.db.templateUpdate (tip);
			tip = conn.db.templateGet(utip.getId());                   // get full info by Id

			//-------- update in TreeTableView
			TemplateSimpleItem ttt = new TemplateSimpleItem(
					tip.getId(),        // style id
					tip.getThemeId(),
					editedItem.getSubItem().getName() +" : "+ tip.getName(),
					editedItem.getSubItem().getDescr() +" : "+ tip.getDescr() +" ("+ tip.getFileName() +")",
					TemplateSimpleItem.TYPE_TEMPLATE,         // 5 - шаблон
					0,
					1,
					tip.getDateCreated(),
					tip.getDateModified(),
					tip.getUserCreated(),
					tip.getUserModified()
			);
			ttt.setSubItem(editedItem.getSubItem());

			editedItem_ti = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					editedItem.getId(),
					editedItem.getType());
			//TemplateSimpleItem.TYPE_TEMPLATE);
			if (editedItem_ti == null) {
				ShowAppMsg.showAlert("WARNING", "Нет данных", "Не найден элемент Шаблон", editedItem.getName() + " (" + editedItem.getId() + ")");
			} else {
				editedItem_ti.setValue(null);
				editedItem_ti.setValue(ttt);
			}

			// определяем текущий активный итем
			resultItem = editedItem_ti;

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Шаблон '" + ttt.getName() + "' изменен.  " + (new Date()));
		}
	}

    /**
	 * Вызывается для открытия тестового обязательного файла
	 */
	@FXML
	private void handleButtonFileTextOpen() {
		FileChooser fileChooser = new FileChooser();
		String curDir;
		
		// Задаём фильтр расширений
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML файлы (*.html)", "*.html"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSS файлы (*.css)", "*.css"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
		
	    // set directory
	    curDir = prefs.get("stageTemplatesEdit_SelectFileDir_Text", "");
	    if (! curDir.equals("")) 
	    	fileChooser.setInitialDirectory(new File(curDir));
	    
	    // Показываем диалог загрузки файла
	    File file = fileChooser.showOpenDialog(params.getStageCur());
		
	    if (file != null) {
	    	int fileType = FileUtil.getFileTypeByExt (file.toString());// return 0 - тип не определен ; 1 - текстовый ; 2 - картинка
	    	
	    	if (fileType == 0) {                   // 0 - тип не определен
	    		//textArea_TemplateContent.appendText("Тип загружаемого файла не определен !" +"\n");
	    		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка тестового файла", "Тип загружаемого файла не определен !");
	    		return;
	    	}
	
	    	if (fileType == 1) {                   // 1 - текстовый
	    		try {
	    			textArea_FileContent.clear();
	    			textArea_FileContent.appendText(FileUtil.readTextFileToString(file.toString()));
				} catch (KBase_ReadTextFileUTFEx e) {
					//e.printStackTrace();
					ShowAppMsg.showAlert("WARNING", "Чтение тестового файла", e.msg, "Чтение файла прервано.");
	    			return;
				}
	    	}
	    	
	    	if (fileType == 2) {                   // 2 - image
	    		//textArea_TemplateContent.appendText("Картинки использовать нельзя!" +"\n");
	    		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка тестового файла", "Картинки здесь использовать нельзя!");
	    		return;
	    	}
	    	
	    	// save dir name
	    	curDir = file.getAbsolutePath();
	    	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
	    	prefs.put("stageTemplatesEdit_SelectFileDir_Text", curDir);
	    }
	}

	/**
	 * Вызывается для сохранения тестового обязательного файла на диске
	 */
	@FXML
	private void handleButtonFileTextSave() {
		Preferences prefs = Preferences.userNodeForPackage(TemplateEdit_Controller.class);
		
		//======== get file name
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранение текста в файл");
		  
	    //Set extension filter
	    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML файл (*.html)", "*.html");
	    fileChooser.getExtensionFilters().add(extFilter);
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSS файлы (*.css)", "*.css"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
	    fileChooser.setInitialFileName(textField_TemplateFileName.getText());
	    
	    // set current dir
	    String curDir = prefs.get("stageTemplatesEdit_SelectFileDir_Text", "");
	    if (! curDir.equals("")) 
	    	fileChooser.setInitialDirectory(new File(curDir));
	    
	    //Show save file dialog
	    File file = fileChooser.showSaveDialog(params.getStageCur());
		
	    if (file != null) {
	    	FileUtil.writeTextFile(file.toString(), textArea_FileContent.getText());
	    	
	        // save dir name
	    	curDir = file.getAbsolutePath();
	    	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
	    	prefs.put("stageTemplatesEdit_SelectFileDir_Text", curDir);
	    }
	}

	/**
	 * Вызывается для открытия обязательного файла с картинкой
	 */
	@FXML
	private void handleButtonFileImageOpen() {
		FileChooser fileChooser = new FileChooser();
		String curDir;
		
		// Задаём фильтр расширений
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG файлы (*.png)", "*.png"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения (*.png,*.gif,*.jpg,*.jpeg)", "*.png","*.gif","*.jpg","*.jpeg"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
		
		// set directory
	    curDir = prefs.get("stageTemplatesEdit_SelectFileDir_Image", "");
	    if (! curDir.equals("")) 
	    	fileChooser.setInitialDirectory(new File(curDir));
	    
	    // Показываем диалог загрузки файла
	    File file = fileChooser.showOpenDialog(params.getStageCur());
	    
	    if (file != null) {
	    	// обновляем контролы
	    	label_FileNameNew.setText(file.toString());
		
	    	int fileType = FileUtil.getFileTypeByExt (file.toString());// return 0 - тип не определен ; 1 - текстовый ; 2 - картинка
	    	
	    	if (fileType == 0) {                   // 0 - тип не определен
	    		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка картинки из файла", "Тип загружаемого файла не определен !");
	    		return;
	    	}
	    	if (fileType == 1) {                   // 1 - текстовый
	    		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка картинки из файла", 
	    				"Текстовый файл здесь использовать нельзя!");
	    		return;
	    	}
	    	
	    	if (fileType == 2) {                   // 2 - image
	    		Image img = new Image(file.toURI().toString());           // not resize
	    		imageView_FileContent.setImage(img);
	    		textArea_FileContent.appendText("Ширина : "+ img.getWidth() +"\n");
	    		textArea_FileContent.appendText("Высота : "+ img.getHeight() +"\n");
	    		//comboBox_FileType.setValue(OList_FileType.get(1));
	    	}
	    	
	    	// save dir name
	    	curDir = file.getAbsolutePath();
	    	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
	    	prefs.put("stageTemplatesEdit_SelectFileDir_Image", curDir);
	    }
	}

	/**
	 * Вызывается для сохранения обязательного файла с картинкой на диске
	 */
	@FXML
	private void handleButtonFileImageSave() {
		Preferences prefs = Preferences.userNodeForPackage(TemplateEdit_Controller.class);
		
		//======== get file name
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранение картинки в файл");
		  
	    //Set extension filter
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG файлы (*.png)", "*.png"));
	    //fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения (*.png,*.gif,*.jpg,*.jpeg)", "*.png","*.gif","*.jpg","*.jpeg"));
		//fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
	    fileChooser.setInitialFileName(textField_FileName.getText());
	    
	    // set current dir
	    String curDir = prefs.get("stageTemplatesEdit_SelectFileDir_Image", "");
	    if (! curDir.equals("")) 
	    	fileChooser.setInitialDirectory(new File(curDir));
	    
	    //Show save file dialog
	    File file = fileChooser.showSaveDialog(params.getStageCur());
		
	    if (file != null) {
	    	FileUtil.writeImageFile(file.toString(), imageView_FileContent.getImage());
			
	        // save dir name
	    	curDir = file.getAbsolutePath();
	    	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
	    	prefs.put("stageTemplatesEdit_SelectFileDir_Image", curDir);
	    }
	}

	/**
     * Вызывается для открытия тестового файла с тектом для шаблона
     */
    @FXML
    private void handleButtonTemplateFileOpen() {
    	FileChooser fileChooser = new FileChooser();
    	String curDir;
    	
    	// Задаём фильтр расширений
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML файлы (*.html)", "*.html"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
    	
        // set directory
        curDir = prefs.get("stageTemplatesEdit_SelectTemplateDir", "");
        if (! curDir.equals("")) 
        	fileChooser.setInitialDirectory(new File(curDir));
        
        // Показываем диалог загрузки файла
        File file = fileChooser.showOpenDialog(params.getStageCur());
    	
        if (file != null) {
        	int fileType = FileUtil.getFileTypeByExt (file.toString());// return 0 - тип не определен ; 1 - текстовый ; 2 - картинка
        	
        	if (fileType == 0) {                   // 0 - тип не определен
        		//textArea_TemplateContent.appendText("Тип загружаемого файла не определен !" +"\n");
        		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка файла шаблона", "Тип загружаемого файла не определен !");
        		return;
        	}

        	if (fileType == 1) {                   // 1 - текстовый
        		try {
        			textArea_TemplateContent.clear();
        			
    				//textArea_TemplateContent.appendText(FileUtil.readTextFileToString(label_TemplateNameNew.getText()));
        			textArea_TemplateContent.appendText(FileUtil.readTextFileToString(file.toString()));
    			} catch (KBase_ReadTextFileUTFEx e) {
    				//e.printStackTrace();
    				ShowAppMsg.showAlert("WARNING", "Чтение файла", e.msg, "Чтение файла шаблона прервано.");
        			return;
    			}
        	}
        	
        	if (fileType == 2) {                   // 2 - image
        		//textArea_TemplateContent.appendText("Картинки использовать нельзя!" +"\n");
        		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка файла шаблона", "Картинки здесь использовать нельзя!");
        		return;
        	}
        	
        	// save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("stageTemplatesEdit_SelectTemplateDir", curDir);
        }
    }
    
    /**
     * Вызывается для сохранения шаблона в файл на диске
     */
    @FXML
    private void handleButtonTemplateFileSaveToDisk() {
    	Preferences prefs = Preferences.userNodeForPackage(TemplateEdit_Controller.class);
    	
    	//======== get file name
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Сохранение шаблона в файл");
    	  
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML файл (*.html)", "*.html");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(textField_TemplateFileName.getText());
        
        // set current dir
        String curDir = prefs.get("stageTemplatesEdit_CurDirNameForSave", "");
        if (! curDir.equals("")) 
        	fileChooser.setInitialDirectory(new File(curDir));
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(params.getStageCur());
    	
        if (file != null) {
        	FileUtil.writeTextFile(file.toString(), textArea_TemplateContent.getText());
            
            // save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("stageTemplatesEdit_CurDirNameForSave", curDir);
        }
    }

	/**
	 * Вызывается для сохранения шаблона в БД
	 */
	@FXML
	private void handleButtonTemplateFileSaveToDB() {
		try {
			saveTemplate();
		} catch (KBase_Ex e) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", e.errSign, e.msg, "");
			return;
		}
		((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.sort();
	}

    /**
     * Вызывается при нажатии на кнопке "Пред."
     */
    @FXML
    private void handleButtonPrev() {
    	
    	tabPane_main.getSelectionModel().select(TAB_ADD_SELECT);
    	button_Prev.setDisable(true);
		button_Next.setText("След.");
		button_Next.setGraphic(new ImageView(new Image("file:resources/images/icon_next_16.png")));
    }    
    
    /**
     * Вызывается при нажатии на кнопке "След./Сохранить"
     */
    @FXML
    private void handleButtonNext() {
    	if (tabPane_main.getSelectionModel().getSelectedIndex() == TAB_ADD_SELECT) {        // переход на второй шаг
    		// change active tab and FileName
    		switch (comboBox_TypeForAdd.getSelectionModel().getSelectedItem()) {
    		case "Тема" :
    			tabPane_main.getSelectionModel().select(TAB_THEME);
    			break;
    		case "Файл kbase_main.html" :
    			tabPane_main.getSelectionModel().select(TAB_FILE);
    			label_TitleFile.setText("Файл kbase_main.html");
    			textField_FileName.setText("kbase_main.html");
    			textField_FileName.setDisable(true);
    			break;
    		case "Файл no_info.html" :
    			tabPane_main.getSelectionModel().select(TAB_FILE);
    			label_TitleFile.setText("Файл no_info.html");
    			textField_FileName.setText("no_info.html");
    			textField_FileName.setDisable(true);
    			break;
    		case "Файл load.html" : 
    			tabPane_main.getSelectionModel().select(TAB_FILE);
    			label_TitleFile.setText("Файл load.html");
    			textField_FileName.setText("load.html");
    			textField_FileName.setDisable(true);
    			break;
    		case "Обязательный файл" :
    			tabPane_main.getSelectionModel().select(TAB_FILE);
    			label_TitleFile.setText(comboBox_TypeForAdd.getSelectionModel().getSelectedItem());
    			textField_FileName.setText("");
    			textField_FileName.setDisable(false);
    			break;
    		case "Стиль" :
    			tabPane_main.getSelectionModel().select(TAB_STYLE);
    			break;
    		case "Шаблон" :
    			tabPane_main.getSelectionModel().select(TAB_TEMPLATE);
    			break;
    		}
    		
    		// change buttons settings
    		button_Prev.setDisable(false);
    		button_Next.setText("Сохранить");
    		button_Next.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	} else {      // сохраняем элемент
    		//-------- save stage position
        	prefs.putDouble("stageTemplatesEdit_Width", params.getStageCur().getWidth());
        	prefs.putDouble("stageTemplatesEdit_Height",params.getStageCur().getHeight());
        	prefs.putDouble("stageTemplatesEdit_PosX",  params.getStageCur().getX());
        	prefs.putDouble("stageTemplatesEdit_PosY",  params.getStageCur().getY());

			try {
				// save a Theme
				if (tabPane_main.getSelectionModel().getSelectedIndex() == TAB_THEME) {        // Сохраняем тему
					saveTheme();
				}
				// save a File
				if (tabPane_main.getSelectionModel().getSelectedIndex() == TAB_FILE) {        // Сохраняем file
					saveRequiredFile();
				}
				// save a Style
				if (tabPane_main.getSelectionModel().getSelectedIndex() == TAB_STYLE) {        // Сохраняем стиль
					saveStyle();
				}
				// save a Template
				if (tabPane_main.getSelectionModel().getSelectedIndex() == TAB_TEMPLATE) {        // Сохраняем template
					saveTemplate();
				}
			} catch (KBase_Ex e) {
				//e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", e.errSign, e.msg, "");
				return;
			}

			((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.sort();

			// устанавливаем текущий активный итем
			TreeItem<TemplateSimpleItem> resultItem_tmp = ((TemplatesList_Controller)params.getParentObj()).treeViewCtrl.getTreeItemByTemplateId(
					((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getRoot(),
					resultItem.getValue().getId(),
					resultItem.getValue().getType(),
					//tiTheme.getValue().getId()
					resultItem.getValue().getThemeId()
			);
			((TemplatesList_Controller)params.getParentObj()).treeTableView_templates.getSelectionModel().select(resultItem_tmp);

        	//-------- close window
        	// get a handle to the stage
            Stage stage = (Stage) button_Cancel.getScene().getWindow();
            // do what you have to do
            stage.close();
    	}
    }
    
    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonCancel() {
    	//-------- save stage position
    	prefs.putDouble("stageTemplatesEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplatesEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplatesEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplatesEdit_PosY",  params.getStageCur().getY());
    	
        // get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
