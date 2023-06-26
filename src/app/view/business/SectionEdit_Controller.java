
package app.view.business;

import app.Main;
import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.model.Params;
import app.model.business.IconItem;
import app.model.business.SectionItem;
import app.view.business.template.TemplateThemeSelect_Controller;

import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контроллер окна добавления/редактирования раздела
 * 
 * @author Igor Makarevich
 */
public class SectionEdit_Controller {
	
	private Params params;
    ///////////////////////public SectionList_Controller mainInfo_C;
	
    /**
     * Тип операции : 1 - добавить ; 2 - редактировать
     */
    private int actionType;
    /**
     * Текущая запись (редактируемая) в списке разделов
     */
    private TreeItem<SectionItem> selectedSectionParam_ti;
    private SectionItem selectedSectionParam;
    
    /**
     * Ковертор даты/времени
     */
    public DateConv dateConv;
    
	// Controls and related variables
	@FXML
	private Label label_id;
	@FXML
	private Label label_ParentSections;
	@FXML
	private TextField textField_Name;
	
	private long iconId;             // новая выбранная иконка
	@FXML
	private ImageView imageView_Icon;
	@FXML
	private Label label_IconName;
	@FXML
	private Button button_IconSelect;
	
	@FXML
	private TextField textField_Description;
	@FXML
	private Label label_DateCreated;
	@FXML
	private Label label_DateModified;
	@FXML
	private Label label_UserCreated;
	@FXML
	private Label label_UserModified;
	@FXML
	private Label label_DateModifiedInfo;
	
	private long iconIdRoot;             // Ветка дерева пиктограмм для выбора
	@FXML
	private ImageView imageView_IconRoot;
	@FXML
	private Label label_IconRootName;
	@FXML
	private Button button_IconRootSelect;
	
	private long iconIdDef;             // Пиктограмма по умолчанию
	@FXML
	private ImageView imageView_IconDef;
	@FXML
	private Label label_IconDefName;
	@FXML
	private Button button_IconDefSelect;
	
	private long themeId;               // Тема шаблонов
	@FXML
	private Label label_themeName;
	@FXML
	private Button button_themeSelect;
	
	@FXML
	private ComboBox<String> comboBox_cacheType;
	
    @FXML
	private Button button_Save;
	@FXML
	private Button button_Cancel;
	
	//
	////////////////////private Stage dialogStage;
	private ObservableList<String> OList_cacheType;                  // for comboBox_cacheType
    
    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public SectionEdit_Controller () {
    	dateConv = new DateConv();
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {        }
    
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, int actionType, TreeItem<SectionItem> selectedSectionParam_ti) {
    	this.params     = params;
    	this.actionType = actionType;
        
        // init controls
        this.selectedSectionParam_ti = selectedSectionParam_ti;
        this.selectedSectionParam = selectedSectionParam_ti.getValue();
        initControlsValue(this.selectedSectionParam_ti);
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue(TreeItem<SectionItem> selectedSectionParam_ti) {
    	SectionItem selectedSectionParam = (SectionItem) selectedSectionParam_ti.getValue();
    	
    	//-------- init comboBox_cacheType
    	OList_cacheType = FXCollections.observableArrayList();
		
    	OList_cacheType.add("< по умолчанию >");
    	OList_cacheType.add("1 - на диске");
    	OList_cacheType.add("2 - в БД");
    	OList_cacheType.add("3 - на диске только обязательные файлы");
		
    	comboBox_cacheType.setItems(OList_cacheType);
    	
    	//--------
    	if (actionType == 1) {         // add 
    		label_id.setText("");
    		label_ParentSections.setText(
    				((SectionList_Controller)params.getParentObj()).treeViewCtrl.getSectionPath(selectedSectionParam_ti, 1));
    				//mainInfo_C.treeViewCtrl.getSectionPath(selectedSectionParam_ti, 1));
    		textField_Name.setText("");
    		
    		this.iconId = 0;
    		IconItem ii_def_tmp = 
    				params.getConCur().db.iconGetById(
    						params.getConCur().db.sectionGetIconIdDefault(
    								selectedSectionParam.getId(),
									true)
					);
    		imageView_Icon.setImage(ii_def_tmp.image);
    		label_IconName.setText("<по умолчанию> "+ ii_def_tmp.getName() +" ("+ ii_def_tmp.getId() +")");
    		
    		textField_Description.setText("");
    		label_DateCreated.setText("");
    		label_DateModified.setText("");
    		label_UserCreated.setText("");
    		label_UserModified.setText("");
    		label_DateModifiedInfo.setText("");
    		label_IconRootName.setText("<не выбрано>");
    		label_IconDefName.setText("<не выбрано>");
    		label_themeName.setText("<не выбрано>");
    		
    		comboBox_cacheType.setValue(OList_cacheType.get(0));    // первый эдемент в списке
    	} else if (actionType == 2) {         // update
    		label_id.setText(Long.toString(selectedSectionParam.getId()));
    		label_ParentSections.setText(
    				((SectionList_Controller)params.getParentObj()).treeViewCtrl.getSectionPath(selectedSectionParam_ti, 0));
    		textField_Name.setText(selectedSectionParam.getName());
    		
    		iconId = selectedSectionParam.getIconId();
    		if (iconId > 0) {
    			imageView_Icon.setImage(params.getConCur().db.iconGetImageById(iconId)); 
    			label_IconName.setText(params.getConCur().db.iconGetNameById(iconId) +" ("+ iconId +")");
    		} else {
        		IconItem ii_def_tmp = 
        				params.getConCur().db.iconGetById(
        						params.getConCur().db.sectionGetIconIdDefault(selectedSectionParam.getParentId(),
										true));
        		imageView_Icon.setImage(ii_def_tmp.image);
        		label_IconName.setText("<по умолчанию> "+ ii_def_tmp.getName() +" ("+ ii_def_tmp.getId() +")");
    		}
    		
    		textField_Description.setText(selectedSectionParam.getDescr());
    		label_DateCreated.setText(dateConv.dateTimeToStr(selectedSectionParam.getDateCreated()));
    		label_DateModified.setText(dateConv.dateTimeToStr(selectedSectionParam.getDateModified()));
    		label_UserCreated.setText(selectedSectionParam.getUserCreated());
    		label_UserModified.setText(selectedSectionParam.getUserModified());
    		label_DateModifiedInfo.setText(dateConv.dateTimeToStr(selectedSectionParam.getDateModifiedInfo()));
    		
    		iconIdRoot = selectedSectionParam.getIconIdRoot();
    		if (iconIdRoot != 0) {
    			imageView_IconRoot.setImage(params.getConCur().db.iconGetImageById(iconIdRoot)); 
    			label_IconRootName.setText(params.getConCur().db.iconGetNameById(iconIdRoot) +" ("+ iconIdRoot +")");
    		} else {
    			label_IconRootName.setText("<не выбрано>");
    		}
    		
    		iconIdDef = selectedSectionParam.getIconIdDef();
    		if (iconIdDef != 0) {
    			imageView_IconDef.setImage(params.getConCur().db.iconGetImageById(iconIdDef)); 
    			label_IconDefName.setText(params.getConCur().db.iconGetNameById(iconIdDef) +" ("+ iconIdDef +")");
    		} else {
    			label_IconDefName.setText("<не выбрано>");
    		}
    		
    		themeId = selectedSectionParam.getThemeId();               // Тема шаблонов
    		if (themeId != 0) 
    			label_themeName.setText(params.getConCur().db.templateThemeGetById(themeId).getName() +" ("+ themeId +")");
    		else              
    			label_themeName.setText("<не выбрано>");
    		
    		comboBox_cacheType.setValue(OList_cacheType.get(selectedSectionParam.getCacheType()));
    		
    		// При выборе редактирования корневого раздела -- дизейблить кнопку Сохранить
    		if (selectedSectionParam.getId() == 0)  button_Save.setDisable(true);
    	}
    	
    	// buttons
    	button_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * Выбираем пиктограмму для раздела из справочника и показываем ее.
     */
    @FXML
    private void handleButtonSelectImage() {
    	
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/IconSelectEx.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Выбор пиктограммы");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getStageCur());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_CatalogIcons_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageIconSelect_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageIconSelect_Height", 600));
			dialogStage.setX(prefs.getDouble("stageIconSelect_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageIconSelect_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			long siRootIdTmp;
			if (actionType == 1)
				siRootIdTmp = selectedSectionParam.getIconIdRoot();
			else
				siRootIdTmp =
						(selectedSectionParam.getParentId() > 0) ?
						params.getConCur().db.sectionGetById(selectedSectionParam.getParentId()).getIconIdRoot() :
						0;

			IconSelect_Controller controller = loader.getController();
        	
			Params params = new Params (this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(
					params,
					iconId,
					siRootIdTmp,
        			params.getConCur().db.sectionGetIconIdDefault(
							(actionType == 1) ? selectedSectionParam.getId() : selectedSectionParam.getParentId(),
							true
					));
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	switch (controller.typeSelection) {
	        	case 0 :         // default style
	        		iconId = 0;
	        		long iconId_forShow = 0;
	        		IconItem ii_forShow = null;
    	        	iconId_forShow = params.getConCur().db.sectionGetIconIdDefault(selectedSectionParam.getParentId(), true);
	        		ii_forShow = params.getConCur().db.iconGetById(iconId_forShow);
	        		imageView_Icon.setImage(ii_forShow.image);
	        		label_IconName.setText("<по умолчанию> "+ ii_forShow.getName() +" ("+ ii_forShow.getId() +")");
	        		break;
	        	case 1 :         // current style
	        	case 2 :          // style from list
	        		iconId = controller.iconIdRet;
	        		imageView_Icon.setImage(params.getConCur().db.iconGetImageById(iconId)); 
					label_IconName.setText(params.getConCur().db.iconGetNameById(iconId) +" ("+ iconId +")");
	        		break;
	        	}
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Очищаем выбор пиктограммы ветки дерева для выбора пиктограмм.
     */
    @FXML
    private void handleButtonClearImageRoot() {
    	iconIdRoot = 0;
		imageView_IconRoot.setImage(null); 
		label_IconRootName.setText("<не выбрано>");
    }
    
    /**
     * Выбираем пиктограмму ветки дерева для выбора пиктограмм.
     */
    @FXML
    private void handleButtonSelectImageRoot() {
    
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/IconSelect_Layout.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Выбор пиктограммы");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getStageCur());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_CatalogIcons_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageIconSelect_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageIconSelect_Height", 600));
			dialogStage.setX(prefs.getDouble("stageIconSelect_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageIconSelect_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			IconSelect_Controller controller = loader.getController();
			
			Params params = new Params (this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(params, iconIdRoot, 0);

	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	iconIdRoot = controller.iconIdRet;
				imageView_IconRoot.setImage(params.getConCur().db.iconGetImageById(iconIdRoot)); 
				label_IconRootName.setText(params.getConCur().db.iconGetNameById(iconIdRoot) +" ("+ iconIdRoot +")");
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Очищаем выбор пиктограммы по умолчанию.
     */
    @FXML
    private void handleButtonClearImageDef() {
    	iconIdDef = 0;
    	imageView_IconDef.setImage(null); 
		label_IconDefName.setText("<не выбрано>");
    }
    
    /**
     * Выбираем пиктограмму по умолчанию.
     */
    @FXML
    private void handleButtonSelectImageDef() {
    
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/IconSelect_Layout.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Выбор пиктограммы");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getStageCur());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_CatalogIcons_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageIconSelect_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageIconSelect_Height", 600));
			dialogStage.setX(prefs.getDouble("stageIconSelect_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageIconSelect_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			IconSelect_Controller controller = loader.getController();
			
			Params params = new Params (this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(params, iconIdDef, 0);
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	iconIdDef = controller.iconIdRet;
	        	imageView_IconDef.setImage(params.getConCur().db.iconGetImageById(iconIdDef)); 
	    		label_IconDefName.setText(params.getConCur().db.iconGetNameById(iconIdDef) +" ("+ iconIdDef +")");
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Очищаем выбор темы.
     */
    @FXML
    private void handleButtonClearTheme() {
    	themeId = 0;
    	label_themeName.setText("<не выбрано>");
    }
    
    /**
     * Выбираем тему шаблонов документов для раздела.
     */
    @FXML
    private void handleButtonSelectTheme() {
    
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/template/TemplateThemeSelect.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Выбор темы шаблонов документа");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getStageCur());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_theme_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageTemplateThemeSelect_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageTemplateThemeSelect_Height", 600));
			dialogStage.setX(prefs.getDouble("stageTemplateThemeSelect_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageTemplateThemeSelect_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			TemplateThemeSelect_Controller controller = loader.getController();
			
			Params params = new Params (this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(params, themeId);
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	themeId = controller.themeIdRet;
	        	label_themeName.setText(params.getConCur().db.templateThemeGetById(themeId).getName() +" ("+ themeId +")");
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    /**
     * Вызывается при нажатии на кнопке "Сохранить"
     */
    @FXML
    private void handleButtonSave() {
    	SectionItem sip = null;
    	
    	//-------- save stage position
    	Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
    	prefs.putDouble("stageSectionsEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageSectionsEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageSectionsEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageSectionsEdit_PosY",  params.getStageCur().getY());

        //---------- check data in fields
    	if ((textField_Name.getText().equals("") || (textField_Name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название раздела", "Укажите Название раздела");
    		return;
        }
    	
        //-------- Сохраняем параметры соединения
    	if (actionType == 1) {         // add 
    		long newId = params.getConCur().db.sectionNextId();
    		
    		sip = new SectionItem (
    				newId, 
    				selectedSectionParam.getId(),     // parent_id
    				textField_Name.getText(),
    				iconId,
    				(iconId > 0) ? imageView_Icon.getImage() : null, 
    				textField_Description.getText(),
    				null, null, null, null, null,
    				iconIdRoot,
    				iconIdDef,
    				themeId,
    				comboBox_cacheType.getSelectionModel().getSelectedIndex()
    				);
    		params.getConCur().db.sectionAdd(sip);             // обьект-раздел добавляем в БД
    		sip = params.getConCur().db.sectionGetById(newId); // get full info

			// добавляем в контрол-дерево
			TreeItem<SectionItem> item = new TreeItem<>(new SectionItem(sip));
			selectedSectionParam_ti.getChildren().add(item);
			selectedSectionParam_ti.setExpanded(true);

        	// выводим сообщение в статус бар
            params.setMsgToStatusBar("Раздел '" + sip.getName() + "' добавлен.");
    	} else if (actionType == 2) {         // update
        	// create section object and update it into db
    		sip = new SectionItem(
    				selectedSectionParam.getId(), 
    				selectedSectionParam.getParentId(),     // parent_id
    				textField_Name.getText(),
    				iconId,
    				(iconId > 0) ? imageView_Icon.getImage() : null,
    				textField_Description.getText(),
    				selectedSectionParam.getDateCreated(), 
    				null, 
    				selectedSectionParam.getUserCreated(), 
    				null, 
    				selectedSectionParam.getDateModifiedInfo(),
    				iconIdRoot,
    				iconIdDef,
    				themeId,
    				comboBox_cacheType.getSelectionModel().getSelectedIndex()
    				);
    		params.getConCur().db.sectionUpdate(sip);
    		sip = params.getConCur().db.sectionGetById(selectedSectionParam.getId()); // get full info
        	
        	// update in TreeTableView
    		selectedSectionParam_ti.setValue(null);
    		selectedSectionParam_ti.setValue(sip);
        	
        	// выводим сообщение в статус бар
        	params.setMsgToStatusBar("Раздел '" + sip.getName() + "' изменен.");
    	}

    	//-------- делаем активным добавленный/отредактированный раздел
    	((SectionList_Controller)params.getParentObj()).treeTableView_sections.sort();
    	((SectionList_Controller)params.getParentObj()).treeViewCtrl.selectTreeItemById(
    			((SectionList_Controller)params.getParentObj()).treeViewCtrl.root, sip.getId());
    	
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
    	Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
    	prefs.putDouble("stageSectionsEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageSectionsEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageSectionsEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageSectionsEdit_PosY",  params.getStageCur().getY());
    	
        // get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
