
package app.view.business.template;

import app.lib.ConvertType;
import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.Main;
import app.model.*;
import app.model.business.InfoTypeItem;
import app.model.business.InfoTypeStyleItem;
import app.model.business.templates_old.TemplateItem;
import app.model.business.templates_old.TemplateRequiredFileItem;
import app.model.business.templates_old.TemplateSimpleItem;
import app.model.business.templates_old.TemplateThemeItem;
import app.view.business.Container_Interface;
import app.view.business.SectionList_Controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Контроллер фрейма каталога шаблонов. Показываем дерево-список тем и шаблонов, 
 * с возможность добавления, редактирования и удаления.
 * @author Igor Makarevich
 * @version 2.00.00.001   11.04.2021
 */
public class TemplateList_Controller implements AppItem_Interface {
	
	private Params params;
	private DBConCur_Parameters conn;
	private Container_Interface objContainer;
    
    /**
     * Ковертор даты/времени
     */
    DateConv dateConv;
    
    @FXML
	private Button button_Exit;
    @FXML
    private TitledPane titledPane_Title;

    @FXML
	private Button button_templateAdd;
    @FXML
	private Button button_templateUpdate;
    @FXML
	private Button button_templateDelete;
    @FXML
	private Button button_templateCopy;
    @FXML
	private Button button_templateCut;
    @FXML
	private Button button_templatePaste;
    
    @FXML
	public TreeTableView<TemplateSimpleItem> treeTableView_templates;
	@FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_id;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_name;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_descr;
    @FXML
    private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_dateCreated;
    @FXML
    private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_dateModified;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_userCreated;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_userModified;

    @FXML
	private MenuItem menuitem_templateAdd;
    @FXML
	private MenuItem menuitem_templateUpdate;
    @FXML
	private MenuItem menuitem_templateDelete;
    @FXML
	private MenuItem menuitem_templateCopy;
    @FXML
	private MenuItem menuitem_templateCut;
    @FXML
	private MenuItem menuitem_templatePaste;
    
    //
	TemplateList_Controller.TreeView_Controller treeViewCtrl;

    //
	private Preferences prefs;
    
    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateList_Controller () {
    	dateConv = new DateConv();
		prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);

		treeViewCtrl = this.new TreeView_Controller();
    }
	
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    	
    }
    
    /**
     * Вызывается главным приложением, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params) {
    	this.params       = params;
    	this.conn         = params.getConCur();
    	this.objContainer = params.getObjContainer();
    	
        // init controls
        initControlsValue();
    }
	
    
    
    
    
    
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	button_Exit.setTooltip(new Tooltip("Закрыть фрейм"));
    	
    	// title
    	titledPane_Title.setText(titledPane_Title.getText() + " - " + conn.param.getConnName());
    	
    	if (conn.param.getColorEnable()) {
    		titledPane_Title.setStyle(
    				"-fx-body-color: #" + 
    						ConvertType.colorToHex(new Color(conn.param.getColorBRed_A(),  conn.param.getColorBGreen_A(),
    								conn.param.getColorBBlue_A(), conn.param.getColorBOpacity_A())) + 
    						";" +
    						"-fx-text-fill: #" +
    						ConvertType.colorToHex(new Color(conn.param.getColorTRed_A(),  conn.param.getColorTGreen_A(),
              		                             conn.param.getColorTBlue_A(), conn.param.getColorTOpacity_A())) +
    				";");
    	}
    	
    	// ToolBar
    	button_templateAdd.setTooltip(new Tooltip("Добавить новый элемент"));
    	button_templateAdd.setGraphic(new ImageView(new Image("file:resources/images/icon_add_24.png")));
    	button_templateUpdate.setTooltip(new Tooltip("Изменить элемент"));
    	button_templateUpdate.setGraphic(new ImageView(new Image("file:resources/images/icon_update_24.png")));
    	button_templateDelete.setTooltip(new Tooltip("Удалить элемент"));
    	button_templateDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_24.png")));
    	button_templateCopy.setTooltip(new Tooltip("Копировать элемент (внутренний буфер)"));
    	button_templateCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_24.png")));
    	button_templateCut.setTooltip(new Tooltip("Вырезать элемент (внутренний буфер)"));
    	button_templateCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_24.png")));
    	button_templatePaste.setTooltip(new Tooltip("Вставить элемент (внутренний буфер)"));
    	button_templatePaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_24.png")));
    		
    	// TreeTableView
		treeViewCtrl.init();

    	// ContextMenu
    	menuitem_templateAdd.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	menuitem_templateUpdate.setGraphic(new ImageView(new Image("file:resources/images/icon_update_16.png")));
    	menuitem_templateDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	menuitem_templateCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	menuitem_templateCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
    	menuitem_templatePaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    }
    
    /**
     * отображает информацию выбранного шаблона, темы или файла
     */
    private void showTemplateDetails(TreeItem<TemplateSimpleItem> ti) {
    	params.setMsgToStatusBar(treeViewCtrl.getTemplatePath (ti, 1));
    }
    
    /**
     * Добавляем новый элемент
     */
    @FXML
    private void handleButtonAddItem() {
//    	if (treeTableView_templates.getSelectionModel().getSelectedItem() == null) {
//    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел", 
//    				"Выберите раздел, в который добавиться новый шаблон или файл.");
//    		return;
//    	}
    	
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/templates/TemplateEdit_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Добавление нового шаблона или файла");
			dialogStage.initModality(Modality.NONE);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/icon_CatalogTemplates_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageTemplatesEdit_Width", 700));
			dialogStage.setHeight(prefs.getDouble("stageTemplatesEdit_Height", 600));
			dialogStage.setX(prefs.getDouble("stageTemplatesEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageTemplatesEdit_PosY", 0));
/*			
			// Даём контроллеру доступ к главному прилодению.
			TemplateEdit_Controller controller = loader.getController();
	        //controller.setParrentObj(this, 1, treeTableView_templates.getSelectionModel().getSelectedItem(), dialogStage);
			
			Params params = new Params(this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
	        
	        controller.setParams(params, 1, treeTableView_templates.getSelectionModel().getSelectedItem());
*/	        
	        
	        
	        //TODO 12.02.2020
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        //dialogStage.showAndWait();
			dialogStage.show();
	        
	        //
	        ///////////treeTableView_sections.refresh();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Изменяем текущий элемент в справочнике
     */
    @FXML
    private void handleButtonUpdateItem() {
    	//======== проверки
    	if (treeTableView_templates.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран элемент", 
    				"Выберите элемент для редактирования.");
    		return;
    	}
    	
    	if ((treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getType() == 0) || // 0 - корень
    		(treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getType() == 2) || // 2 - папка для обязательных файлов
    		(treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getType() == 4)) { // 4 - папка для шаблонов определенного типа
    		ShowAppMsg.showAlert("WARNING", "Не редактируется", 
    				"\"" + treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getName() + "\"", 
    				"Данный элемент списка не редактируется.");
    		return;
    	}
    	
    	//======== load
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/templates/TemplateEdit_Layout.fxml"));
			AnchorPane page = loader.load();
    	
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Редактирование шаблона или файла");
			dialogStage.initModality(Modality.NONE);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/icon_CatalogTemplates_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageTemplatesEdit_Width", 700));
			dialogStage.setHeight(prefs.getDouble("stageTemplatesEdit_Height", 600));
			dialogStage.setX(prefs.getDouble("stageTemplatesEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageTemplatesEdit_PosY", 0));
/*			
			// Даём контроллеру доступ к главному прилодению.
			TemplateEdit_Controller controller = loader.getController();
	        //controller.setParrentObj(this, 2, treeTableView_templates.getSelectionModel().getSelectedItem(), dialogStage);
			
			Params params = new Params(this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
	        
	        controller.setParams(params, 2, treeTableView_templates.getSelectionModel().getSelectedItem());
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        //dialogStage.showAndWait();
			dialogStage.show();*/
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Удаляем текущий элемент
     */
    @FXML
    private void handleButtonDeleteItem() {
    	TreeItem<TemplateSimpleItem> selectedItem = treeTableView_templates.getSelectionModel().getSelectedItem();
    	
    	if (selectedItem == null) {
    		params.setMsgToStatusBar("Ничего не выбрано для удаления.");
    		return;
    	}
    	
    	TemplateSimpleItem tft = selectedItem.getValue();
    	TreeItem<TemplateSimpleItem> parentItem = selectedItem.getParent();
    	
    	//-------- определяем тип удаления
    	switch (tft.getType()) {
    	case TemplateSimpleItem.TYPE_ROOT :
    		ShowAppMsg.showAlert("INFORMATION", "Удаление шаблона", 
    				"Удаление шаблона '"+ tft.getName() +"'", "Удалять корневой элемент нельзя !");
    		return;
    	case TemplateSimpleItem.TYPE_THEME :
    		long countThemes = conn.db.templateThemeCount();
    		String msgTheme1;
    		
    		if (countThemes > 1) msgTheme1 = "\nСтили удаляться не будут.";
    		else                 msgTheme1 = "";
    		
    		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление темы", 
					  "Удаление темы '"+ tft.getName() +"'." + msgTheme1, 
					  "Удалить тему вместе со всеми ее файлами и шаблонами ?"))
    			return;
    		
    		// delete from DB
    		conn.db.templateFilesDelete(tft.getId());
        	conn.db.templatesDelete(tft.getId());
        	conn.db.templateThemeDelete(tft.getId());

    		if (countThemes == 1) {
    			if (ShowAppMsg.showQuestion("CONFIRMATION", "Удаление стилей", 
  					  "Удаление всех стилей", "Удалить все стили шаблонов ?")) {
    				conn.db.infoTypeStyleDelete();
    			}
    		}
    		
        	// delete from TreeTableView
        	//selectedItem.getChildren().clear();
        	if (parentItem != null) {     // текущая иконка не корневая
                parentItem.getChildren().remove(selectedItem);
        	}
    		
    		break;
    	case TemplateSimpleItem.TYPE_FILE :
    		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление файла для шаблона", 
					  "Удаление файла '"+ tft.getName() +"'", "Удалить файл ?"))
    			return;
    		
    		// delete from DB 
        	conn.db.templateFileDelete(tft.getId());
    		
        	// delete from TreeTableView
        	if (parentItem != null) {     // текущая иконка не корневая
                parentItem.getChildren().remove(selectedItem);
        	}
    		
    		break;
    	case TemplateSimpleItem.TYPE_DIR_FOR_FILES :
    		long fileCount = conn.db.templateFileCount (selectedItem.getValue().getThemeId());
    		
    		if ((fileCount > 0) && 
        			ShowAppMsg.showQuestion("CONFIRMATION", "Удаление файла(ов)", 
    					  "Удаление " +fileCount+ " файла(ов).", "Удалить файл(ы) ?")) {
    			List<TemplateRequiredFileItem> fileList = 
    					conn.db.templateFileListByType(selectedItem.getValue().getThemeId(), 0);
				
				for (TemplateRequiredFileItem t : fileList) {       // cycle for templates
					conn.db.templateFileDelete(t.getId());
    			}
				
				selectedItem.getChildren().clear();
    		}
    		break;
    	case TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES :
    		ShowAppMsg.showAlert("INFORMATION", "Удаление раздела с шаблонами и стилями", 
    				             "Удаление раздела с шаблонами и стилями", "Пока еще не реализовано...");
    		
/*    		long tmplCount = conn.db.templateCount (selectedItem.getValue().getThemeId(), selectedItem.getValue().getId());
    		
    		if ((tmplCount > 0) && 
    			ShowAppMsg.showQuestion("CONFIRMATION", "Удаление шаблона(ов)", 
					  "Удаление " +tmplCount+ " шаблона(ов).", "Удалить шаблон(ы) ?")) {
    			List<TemplateForTreeTableViewItem> templateList = 
    					conn.db.templateList(selectedItem.getValue().getThemeId(), selectedItem.getValue().getId());
				
				for (TemplateForTreeTableViewItem t : templateList) {       // cycle for templates
					conn.db.templateDelete(t.getId());
    			}
				
				selectedItem.getChildren().clear();
    		} */
    		break;
    	case TemplateSimpleItem.TYPE_STYLE :          // пока удаление конечного стиля без шаблонов
    		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление стиля", 
					  "Удаление стиля '"+ tft.getName() +"'", "Удалить стиль ?"))
    			return;

    		// делаем проверку на наличие шаблонов у данного стиля
    		if (conn.db.templateCountByInfoTypeStyle(tft.getId()) > 0) {
    			ShowAppMsg.showAlert("INFORMATION", "Удаление стиля", 
			             "Данный стиль используется в шаблонах", "Удаление невозможно");
    			return;
    		}
    		
    		//  делаем проверку на наличие дочерних стилей у данного стиля
    		if (conn.db.infoTypeStyleCountByParentId(tft.getId()) > 0) {
    			ShowAppMsg.showAlert("INFORMATION", "Удаление стиля", 
			             "Данный стиль имеет подчиненные стили", "Удаление невозможно");
    			return;
    		}
    		
    		InfoTypeStyleItem ssip = conn.db.infoTypeStyleGet(tft.getId());                   // get full info by Id
    		
    		// удаляем стиль из БД
    		conn.db.infoTypeStyleDelete(tft.getId());
    		
    		// удаляем в дереве-контроле все итемы с данным стилем (рекурсия по всему дереву)
    		treeViewCtrl.deleteStyleItemFromTreeRecursive (treeViewCtrl.root, ssip);
    	
    		break;
    	case TemplateSimpleItem.TYPE_TEMPLATE :         // удаление единичного шаблоеа без удаления стиля
    		TemplateItem tip = conn.db.templateGet(tft.getId());   // template id
    		
        	if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление шаблона", 
        								  "Удаление шаблона '"+ tip.getName() +"'", "Удалить шаблон ?"))
        		return;
    		
        	// delete from DB 
        	conn.db.templateDelete(tft.getId());
    		
        	// change item on style in TreeTableView
        	if (parentItem != null) {     // текущая иконка не корневая
        		selectedItem.setValue(null);
        		selectedItem.setValue(tft.getSubItem());
        	}
    		
    		break;
    	default : 
    		ShowAppMsg.showQuestion("CONFIRMATION", "Удаление шаблона/файла/темы", 
    				"Тип элемента '"+ tft.getName() +"' не определен.", "Не удаляем.");
    	}
    	
    	// выводим сообщение в статус бар
    	params.setMsgToStatusBar("Элемент '" + tft.getName() + "' удален.");
    }
    
    /**
     * Копирует текущий элемент в локальный буфер обмена
     */
    @FXML
    private void handleButtonItemCopy() {
    	ShowAppMsg.showAlert("INFORMATION", "Копирование", "Пока не реализовано", "");
    	
    	
    	
    }
    
    /**
     * Вырезает текущий элемент с занесением в локальный буфер обмена
     */
    @FXML
    private void handleButtonItemCut() {
    	ShowAppMsg.showAlert("INFORMATION", "Вырезание", "Пока не реализовано", "");
    	
    	
    	
    }
    
    /**
     * Вставляет элемент указанный в буфере обмена
     */
    @FXML
    private void handleButtonItemPaste() {
    	ShowAppMsg.showAlert("INFORMATION", "Вставка", "Пока не реализовано", "");
    	
    	
    	
    }
    
    /**
     * Вызывается при нажатии на кнопке "Закрыть" (X)
     */
    @FXML
    private void handleButtonExit() {

		//-------- sort
		if (treeTableView_templates.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = treeTableView_templates.getSortOrder().get(0);
			prefs.put("stageTemplatesList_sortColumnId",currentSortColumn.getId());
			prefs.put("stageTemplatesList_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("stageTemplatesList_sortColumnId");
			prefs.remove("stageTemplatesList_sortType");
		}







    	//TODO

    	//
    	//mainApp.closeCurTab();
		objContainer.closeContainer(getOID());
    }
    
    /**
	 * уникальный ИД обьекта
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getOID() {
		return hashCode();
	}

	/**
	 * Название элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public String getName() {
		return "TemplatesList_Controller";
	}

	/**
	 * id соединения с базой данных
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getDbConnId() {
		return conn.Id;
	}

	/**
	 * контроллер элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Object getController() {
		return this;
	}

	/**
	 * Класс обработки дерева шаблонов
	 */
	class TreeView_Controller {
		//
		public TreeItem<TemplateSimpleItem> root;

		/**
		 * constructor
		 */
		TreeView_Controller () {
			root = null;
		}

		/**
		 * инициализируем дерево, основной метод инициализации
		 */
		void init () {
			initCellValueFactory ();
			initColumnWidth ();

			root = new TreeItem<>(new TemplateSimpleItem(
					0, 0, "Темы и Шаблоны", "это корень, он не редактируется",
					TemplateSimpleItem.TYPE_ROOT, 0, 0));
			root.setExpanded(true);
			treeTableView_templates.setShowRoot(false);
			treeTableView_templates.setRoot(root);

			initTreeItems(root);
			initCellFactory();
			initRowFactory();

			// Слушаем изменения выбора, и при изменении отображаем информацию.
			treeTableView_templates.getSelectionModel().selectedItemProperty().addListener(
					(observable, oldValue, newValue) -> showTemplateDetails(newValue));

			initSortColumn();
		}

		/**
		 * initCellValueFactory ()
		 */
		private void initCellValueFactory () {
			treeTableColumn_id.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
			);
			treeTableColumn_name.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getName())
			);
			treeTableColumn_descr.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
			);
			treeTableColumn_dateCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateCreated()))
			);
			treeTableColumn_dateModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModified()))
			);
			treeTableColumn_userCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
			);
			treeTableColumn_userModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
			);
		}

		/**
		 * initColumnWidth ()
		 */
		private void initColumnWidth () {
			// set/get Pref Width
			treeTableColumn_id.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_id__PrefWidth", 50));

			treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_id__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_name.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_name__PrefWidth", 250));

			treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_name__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_descr.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_descr__PrefWidth", 250));

			treeTableColumn_descr.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_descr__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateCreated.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_dateCreated__PrefWidth", 150));

			treeTableColumn_dateCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_dateCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateModified.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_dateModified__PrefWidth", 150));

			treeTableColumn_dateModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_dateModified__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userCreated.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_userCreated__PrefWidth", 150));

			treeTableColumn_userCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_userCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userModified.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_userModified__PrefWidth", 150));

			treeTableColumn_userModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_userModified__PrefWidth", t1.doubleValue());
				}
			});
		}

		/**
		 * Инициализация TreeTableView.
		 */
		private void initTreeItems (TreeItem<TemplateSimpleItem> ti) {
			TemplateSimpleItem f = ti.getValue();

			if (f != null) {
				List<TemplateThemeItem> themesList = conn.db.templateThemesList();

				for (TemplateThemeItem i : themesList) {       // cycle for themes
					TreeItem<TemplateSimpleItem> themeItem = new TreeItem<>(i);
					themeItem.getValue().setThemeId(i.getId());
					themeItem.getValue().setType(TemplateSimpleItem.TYPE_THEME);
					ti.getChildren().add(themeItem);

					TreeItem<TemplateSimpleItem> fileItem;
					TemplateRequiredFileItem fp;

					TreeItem<TemplateSimpleItem> folderItem;
					TemplateSimpleItem fl;

					//-------- типовые файлы
					fp = conn.db.templateFileGetByType(i.getId(), 1);						// 1 - основной файл
					if (fp != null) {
						fileItem = new TreeItem<>(fp);
						themeItem.getChildren().add(fileItem);
					}

					fp = conn.db.templateFileGetByType(i.getId(), 2);					// 2 - "Информация отсутствует"
					if (fp != null) {
						fileItem = new TreeItem<>(fp);
						themeItem.getChildren().add(fileItem);
					}

					fp = conn.db.templateFileGetByType(i.getId(), 3);					// 3 - "Загрузка..."
					if (fp != null) {
						fileItem = new TreeItem<>(fp);
						themeItem.getChildren().add(fileItem);
					}

					//-------- обязательные файлы
					fl = new TemplateSimpleItem (0, themeItem.getValue().getId(),
							"Обязательные файлы", "папка такая",
							TemplateSimpleItem.TYPE_DIR_FOR_FILES, 0, 0);
					folderItem = new TreeItem<>(fl);
					themeItem.getChildren().add(folderItem);

					List<TemplateRequiredFileItem> filesList = conn.db.templateFileListByType(i.getId(), 0);

					for (TemplateRequiredFileItem j : filesList) {       // cycle for files
						fileItem = new TreeItem<>(j);
						folderItem.getChildren().add(fileItem);
					}

					//-------- типы инфо блоков, внутри которых шаблоны
					List<InfoTypeItem> infoTypeList = conn.db.infoTypeList();

					for (InfoTypeItem j : infoTypeList) {       // cycle for infotypes
						TreeItem<TemplateSimpleItem> infoTypeItem = new TreeItem<>(
								new TemplateSimpleItem(j.getId(),
										themeItem.getValue().getId(), j.getName(), j.getDescr(),
										TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES,
										j.getId(),
										0,
										j.getDateCreated(), j.getDateModified(), j.getUserCreated(), j.getUserModified()));
						themeItem.getChildren().add(infoTypeItem);

						initTreeItemsStyleAndTemplatesRecursive (infoTypeItem, j.getId(), 0);
					}
				}
			}
		}

		/**
		 * Инициализирует подветки TreeTableView, содержащие стили/шаблоны
		 */
		private void initTreeItemsStyleAndTemplatesRecursive (TreeItem<TemplateSimpleItem> ti,
															  long infoTypeId, long infoTypeStyleId) {
			TemplateSimpleItem f = ti.getValue();
			List<TemplateSimpleItem> tList;

			if (f != null) {
				tList = conn.db.infoTypeStyleList (infoTypeId, infoTypeStyleId);

				for (TemplateSimpleItem i : tList) {
					i.setThemeId(f.getThemeId());
					i.setFileType(infoTypeId);               // тип инфо блока

					//---- если есть шаблон для этого стиля, то добавляем его в дерево на место стиля,
					//     а стиль подвязываем к шаблону
					TemplateItem tti = conn.db.templateGet(i.getThemeId(), i.getId());
					TreeItem<TemplateSimpleItem> subItem;
					if (tti != null) {
						tti.setName(i.getName() +" : "+ tti.getName());
						tti.setDescr(i.getDescr() +" : "+ tti.getDescr() +" ("+ tti.getFileName() +")");
						tti.setThemeId(i.getThemeId());
						tti.setFileType(infoTypeId);               // тип инфо блока
						tti.setSubItem(i);

						subItem = new TreeItem<>(tti);
					} else {
						subItem = new TreeItem<>(i);
					}

					//---- добавляем в дерево и ищем дочерние стили с шаблонами
					ti.getChildren().add(subItem);

					initTreeItemsStyleAndTemplatesRecursive(subItem, infoTypeId, i.getId());
				}
			}
		}

		/**
		 * CellFactory - показ иконок
		 */
		public void initCellFactory () {
			treeTableColumn_name.setCellFactory(ttc -> new TreeTableCell<TemplateSimpleItem, String>() {
				private TemplateSimpleItem row;
				private ImageView graphic;
				private ImageView graphic_default;
				private HBox hBox;
				private boolean isDefault;

				@Override
				protected void updateItem(String item, boolean empty) {    // display graphic
					isDefault = false;

					try {
						row = getTreeTableRow().getItem();
						switch (row.getType()) {
							case TemplateSimpleItem.TYPE_ROOT :                      // 0 - корень
								graphic = null;
								break;
							case TemplateSimpleItem.TYPE_THEME :                      // 1 - тема
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_theme_16.png"));
								break;
							case TemplateSimpleItem.TYPE_DIR_FOR_FILES :                      // 2 - папка для обязательных файлов
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_folder_16.png"));
								break;
							case TemplateSimpleItem.TYPE_FILE :                      // 3 - обязательный файл
								switch (row.getFileTypeExt()) {
									case TemplateSimpleItem.FILETYPEEXT_TEXT :                  // 1 - текстовый
										graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_file_text_16.png"));
										break;
									case TemplateSimpleItem.FILETYPEEXT_IMAGE :                  // 2 - картинка
										graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_file_picture_16.png"));
										break;
									case TemplateSimpleItem.FILETYPEEXT_BINARY :                  // 3 - бинарный
										graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_file_binary_16.png"));
										break;
								}
								break;
							case TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES :                      // 4 - папка для шаблонов определенного типа
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_InfoBlock_16.png"));
								break;
							case TemplateSimpleItem.TYPE_STYLE :
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_style_empty_16.png"));
								if (conn.db.infoTypeStyleIsDefault (row.getThemeId(), row.getId())) {
									graphic_default = new ImageView(new Image("file:resources/images/icon_default_item_16.png"));
									hBox = new HBox();
									hBox.getChildren().addAll(graphic, graphic_default);
									isDefault = true;
								}
								break;
							case TemplateSimpleItem.TYPE_TEMPLATE :                      // 5 - шаблон
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_template_16.png"));
								if (conn.db.infoTypeStyleIsDefault (row.getThemeId(), row.getSubItem().getId())) {
									graphic_default = new ImageView(new Image("file:resources/images/icon_default_item_16.png"));
									hBox = new HBox();
									hBox.getChildren().addAll(graphic, graphic_default);
									isDefault = true;
								}
								break;
						}
					} catch (NullPointerException e) {
						//e.printStackTrace();
						graphic = null;
					}

					super.updateItem(item, empty);
					setText(empty ? null : item);
					if (isDefault) setGraphic(empty ? null : hBox);
					else           setGraphic(empty ? null : graphic);
				}
			});
		}

		/**
		 * RowFactory - for Drag&Drop
		 */
		private void initRowFactory () {
			treeTableView_templates.setRowFactory(new Callback<TreeTableView<TemplateSimpleItem>,
					TreeTableRow<TemplateSimpleItem>>() {
				@Override
				public TreeTableRow<TemplateSimpleItem> call(final TreeTableView<TemplateSimpleItem> param) {
					final TreeTableRow<TemplateSimpleItem> row = new TreeTableRow<TemplateSimpleItem>();

					row.setOnDragDetected(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							// drag was detected, start drag-and-drop gesture
							TreeItem<TemplateSimpleItem> selected = treeTableView_templates.getSelectionModel().getSelectedItem();

							if (selected != null) {
								//Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
								Dragboard db = row.startDragAndDrop(TransferMode.ANY);

								// create a miniature of the row you're dragging
								db.setDragView(row.snapshot(null, null));

								// Keep whats being dragged on the clipboard
								ClipboardContent content = new ClipboardContent();
								//content.putString(selected.getValue().getName());
								content.put(params.getMain().SERIALIZED_MIME_TYPE, row.getIndex());
								db.setContent(content);

								event.consume();
							}
						}
					});

					row.setOnDragOver(new EventHandler<DragEvent>() {
						@Override
						public void handle(DragEvent event) {
							// data is dragged over the target
							Dragboard db = event.getDragboard();

							if (dragAndDropAcceptable(db, row)) {
								event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
								event.consume();
							}
						}});

					row.setOnDragDropped(new EventHandler<DragEvent>() {
						@Override
						public void handle(DragEvent event) {
							Dragboard db = event.getDragboard();

							if (dragAndDropAcceptable(db, row)) {
								int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
								TreeItem<TemplateSimpleItem> item = treeTableView_templates.getTreeItem(index);

								if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
									int businessOpType =
											dragAndDropGetBusinessOpType (
													TransferMode.MOVE, item, dragAndDropGetTarget(row));

									if (businessOpType == 1) {     // 1 - Перемещение стиля/шаблона в подчинение приемнику
										//long targetThemeId = dragAndDropGetTarget(row).getValue().getThemeId();

										moveStyleItemInTree (item, dragAndDropGetTarget(row));
										event.setDropCompleted(true);

										//выбираем перемещенный итем по Id
										expandTreeItemsByItem(root, item);
										treeTableView_templates.sort();

										//treeTableView_templates.getSelectionModel().select(item);
										treeTableView_templates.getSelectionModel().select(
												getTreeItemByTemplateId (root,
														item.getValue().getId(), item.getValue().getType(), item.getValue().getThemeId())
										);

										// update in DB
										long sourceStyleId =
												(item.getValue().getType() == TemplateSimpleItem.TYPE_TEMPLATE) ?
														item.getValue().getSubItem().getId() :
														item.getValue().getId();
										InfoTypeStyleItem si = conn.db.infoTypeStyleGet(sourceStyleId);
										
										if (dragAndDropGetTarget(row).getValue().getType() == 
												TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES) {
											si.setParentId(0);
										} else {
											si.setParentId(dragAndDropGetTarget(row).getValue().getId());
										}
										
										conn.db.infoTypeStyleUpdate(si);

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Элемент '" + item.getValue().getName() + "' перемещен.");
									}
								} else if (event.getAcceptedTransferMode() == TransferMode.COPY) {
									int businessOpType =
											dragAndDropGetBusinessOpType (
													TransferMode.COPY, item, dragAndDropGetTarget(row));

									if (businessOpType == 2) {        // Копирование ветки целиком в подчинение приемнику
										ShowAppMsg.showAlert("INFORMATION", "Копирование",
												"Копирование ветки целиком в подчинение приемнику", "Пока Не Реализовано...");
									} else if (businessOpType == 3) { // Копировать стиль в подчинение (когда источник стиль)



										ShowAppMsg.showAlert("INFORMATION", "Копирование",
												"3", "Пока Не Реализовано...");


									} else if (businessOpType == 4) { // Копировать стиль/шаблон в подчинение приемнику



										ShowAppMsg.showAlert("INFORMATION", "Копирование",
												"4", "Пока Не Реализовано...");



									} else if (businessOpType == 5) { // Копировать стилю-приемнику только шаблон







										ShowAppMsg.showAlert("INFORMATION", "Копирование",
												"5", "Пока Не Реализовано...");
										//todo

									} else {
										ShowAppMsg.showAlert("ERROR", "Копирование",
												"Неизвестный тип копирования", "такого быть не должно...");
									}
									//TODO COPY
                                /*
                                boolean copyWithSubSections = prefs.get("copyWithSubSections", "No").equals("Yes") ? true : false;
                                int retVal = ShowAppMsg.showQuestionWithOption(
                                        "CONFIRMATION", "Копирование раздела",
                                        "Копировать раздел '"+ item.getValue().getName() +"' ?", null,
                                        "Копировать ветку целиком", copyWithSubSections);

                                if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только текущий итем
                                    event.setDropCompleted(true);

                                    copySection (item.getValue(), getTarget_forDragAndDrop(row), false);

                                    // save Option value
                                    prefs.put("copyWithSubSections", "No");

                                    // выводим сообщение в статус бар
                                    mainApp.statusBar_ShowMsg("Раздел '" + item.getValue().getName() + "' скопирован.");
                                }
                                if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
                                    event.setDropCompleted(true);

                                    // item                          - source TreeItem
                                    // getTarget_forDragAndDrop(row) - target parent TreeItem
                                    copySection (item.getValue(), getTarget_forDragAndDrop(row), true);

                                    // save Option value
                                    prefs.put("copyWithSubSections", "Yes");

                                    // выводим сообщение в статус бар
                                    mainApp.statusBar_ShowMsg("Раздел (ветка) '" + item.getValue().getName() + "' скопирован.");
                                }
                                */



									treeTableView_templates.sort();
								} else {
									ShowAppMsg.showAlert("WARNING", "Перетаскивание", "Не известный режим перетаскивания", "Не обрабатывается.");
								}
								event.consume();
							}
						}});

					return row;
				}
			});
		}

		/**
		 * восстанавливаем сортировку таблицы по столбцу
		 */
		public void initSortColumn () {
			String sortColumnId = prefs.get("stageTemplatesList_sortColumnId","");

			if (! sortColumnId.equals("")) {
				for (TreeTableColumn column : treeTableView_templates.getColumns()) {
					if (column.getId().equals(sortColumnId)) {
						String sortType = prefs.get("stageTemplatesList_sortType","ASCENDING");

						treeTableView_templates.setSortMode(TreeSortMode.ALL_DESCENDANTS);
						column.setSortable(true); // This performs a sort
						treeTableView_templates.getSortOrder().add(column);
						if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
						else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
						treeTableView_templates.sort();
					}
				}
			}
		}

		/**
		 * Возвращает Истину, если перетаскивание возможно, иначе Ложь.
		 */
		private boolean dragAndDropAcceptable(Dragboard db, TreeTableRow<TemplateSimpleItem> row) {
			boolean result = false;
			if (db.hasContent(params.getMain().SERIALIZED_MIME_TYPE)) {
				int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
				if (row.getIndex() != index) {
					TreeItem<TemplateSimpleItem> target = dragAndDropGetTarget(row);
					TreeItem<TemplateSimpleItem> item = treeTableView_templates.getTreeItem(index);
					result = !dragAndDropIsParent(item, target);
				}
			}
			return result;
		}

		/**
		 * Проверяем , является ли стиль parent каким то предком у стиля child
		 */
		private boolean dragAndDropIsParent(TreeItem<TemplateSimpleItem> parent, TreeItem<TemplateSimpleItem> child) {
			TemplateSimpleItem parentStyle = parent.getValue();
			boolean result = false;

			if (parentStyle.getType() == TemplateSimpleItem.TYPE_TEMPLATE) {
				parentStyle = parentStyle.getSubItem();
			}

			child = child.getParent();
			while (!result && (child.getValue().getType() != TemplateSimpleItem.TYPE_THEME)) {
				TemplateSimpleItem childStyle = child.getValue();

				if (childStyle.getType() == TemplateSimpleItem.TYPE_TEMPLATE) {
					childStyle = childStyle.getSubItem();
				}
				result = childStyle.getId() == parentStyle.getId();
				child = child.getParent();
			}

			return result;
		}

		/**
		 * Получаем строчку-приемник при перетаскивании
		 */
		private TreeItem<TemplateSimpleItem> dragAndDropGetTarget(TreeTableRow<TemplateSimpleItem> row) {
			TreeItem<TemplateSimpleItem> target = treeTableView_templates.getRoot();
			if (!row.isEmpty()) {
				target = row.getTreeItem();
			}
			return target;
		}

		/**
		 * ф-ция проверки исходного и конечного элемента дерева на возможность Перемещения/Копирования
		 * и определяем тип действий
		 * return : 0 - нет операции (ничего не делаем)
		 *          1 - Перемещение стиля/шаблона в подчинение приемнику
		 *          2 - Копирование ветки целиком в подчинение приемнику
		 *          3 - Копировать стиль в подчинение (когда источник стиль)
		 *          4 - Копировать стиль/шаблон в подчинение приемнику
		 *          5 - Копировать стилю-приемнику только шаблон
		 */
		private int dragAndDropGetBusinessOpType (
				TransferMode transferMode,
				TreeItem<TemplateSimpleItem> sourceItem,
				TreeItem<TemplateSimpleItem> targetItem)
		{
			TemplateSimpleItem source = sourceItem.getValue();
			TemplateSimpleItem target = targetItem.getValue();

			//--- проверки
			if ((source.getType() != TemplateSimpleItem.TYPE_STYLE) &&
					(source.getType() != TemplateSimpleItem.TYPE_TEMPLATE)) {
				ShowAppMsg.showAlert("WARNING", "Перетаскивание",
						"Перетаскивать можно только стили и шаблоны", "Операция отменена.");
				return 0;
			}
			if ((target.getType() != TemplateSimpleItem.TYPE_STYLE) &&
					(target.getType() != TemplateSimpleItem.TYPE_TEMPLATE) &&
					(target.getType() != TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES)) {
				ShowAppMsg.showAlert("WARNING", "Перетаскивание",
						"Перетаскивать можно только в ветку стилей и шаблонов", "Операция отменена.");
				return 0;
			}
			if (source.getFileType() != target.getFileType()) {
				ShowAppMsg.showAlert("WARNING", "Перетаскивание",
						"У источника и приемника не совпадает тип инфо блока.", "Операция отменена.");
				return 0;
			}

			//---- спрашиваем подтверждение и вычисляем тип операции перетаскивания
			if (transferMode == TransferMode.MOVE) {
				if (ShowAppMsg.showQuestion("CONFIRMATION", "Перемещение стиля/шаблона",
						"Перемещение стиля/шаблона '" + source.getName() + "'", "Переместить элемент ?")) {
					return 1;
				} else {
					return 0;
				}
			}
			if (transferMode == TransferMode.COPY) {
				Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
				boolean copyWithSubSections = prefs.get("copyWithSubSections", "No").equals("Yes");
				int retVal = ShowAppMsg.showQuestionWithOption(
						"CONFIRMATION", "Копирование стиля/шаблона",
						"Копировать стиль/шаблон '"+ source.getName() +"' ?", null,
						"Копировать ветку целиком", copyWithSubSections);

				if (retVal == ShowAppMsg.QUESTION_CANCEL) {
					return 0;
				} else if (retVal == ShowAppMsg.QUESTION_OK) {   // ... варианты единичного копирования
					// если источник стиль, то можно только добавить его в подчинение
					if (source.getType() == TemplateSimpleItem.TYPE_STYLE) {
						return 3;
					}
					// источник стиль/шаблон
					if (source.getType() == TemplateSimpleItem.TYPE_TEMPLATE) {
						// приемник только стиль
						if (target.getType()  == TemplateSimpleItem.TYPE_STYLE) {
							// делаем выбор
							if (ShowAppMsg.showQuestionWith2Buttons("CONFIRMATION",
									"Перемещение/Добавление стиля/шаблона",
									"Добавить стиль/шаблон в подчинение приемнику или перенести этому стилю только шаблон",
									"Что переносим ?", "Стиль/шаблон", "Шаблон")
									== ShowAppMsg.SELECT_BUTTON_1) {
								return 4;	// Добавить стиль/шаблон в подчинение приемнику
							} else {
								return 5;   // Добавить стилю-приемнику только шаблон
							}
						} else {
							return 4;	// Добавить стиль/шаблон в подчинение приемнику
						}
					}
				} else if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {   // Копировать ветку целиком
					return 2;
				} else {
					ShowAppMsg.showAlert("ERROR", "Перетаскивание",
							"Лажа. Неизвестный код ответа.", "Операция отменена.");
					return 0;
				}
			}

			return 0;
		}

		/*
		 * Возвращает строку иерархического пути шаблона
		 *
		 * ti - текущий шаблон
		 * level - 0 - показывает без текущего шаблона ; 1 - с текущим шаблоном
		 */
		public String getTemplatePath (TreeItem<TemplateSimpleItem> ti, int level) {
			String msg = null;
			boolean isFirst = true;

			if (ti != null) {
				TemplateSimpleItem f = ti.getValue();

				// show template path in StatusBar
				if (level == 1) msg = f.getName();
				//if (f.getId() > 0) {
				if (f.getType() != 0) {
					TreeItem<TemplateSimpleItem> curTI = ti.getParent();
					TemplateSimpleItem curTemplate = curTI.getValue();

					while (curTemplate.getType() != 0) {
						if ((level == 1) || (! isFirst)) {
							msg = curTemplate.getName() + " / " + msg;
						} else {
							msg = curTemplate.getName();
							isFirst = false;
						}

						if (curTemplate.getType() == 0)   break;
						else {
							curTI = curTI.getParent();
							curTemplate = curTI.getValue();
						}
					}
				}
			}
			return msg;
		}

		/**
		 * Раскрываем в дереве элемент по его TreeItem
		 */
		private int expandTreeItemsByItem(TreeItem<TemplateSimpleItem> ti, TreeItem<TemplateSimpleItem> curItem) {
			//---------- проверяем и выбираем текущий итем
			if (ti == curItem) {
				return 1;
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<TemplateSimpleItem> i : ti.getChildren()) {
				if (expandTreeItemsByItem(i, curItem) == 1) {
					ti.setExpanded(true);
					return 1;
				}
			}

			return 0;
		}

		/**
		 * Раскрываем в дереве элемент по его Id и типу
		 */
		private int expandTreeItemsById(TreeItem<TemplateSimpleItem> ti, long selId, int type) {
			TemplateSimpleItem si = ti.getValue();

			//---------- проверяем и выбираем текущий итем
			if ((si.getId() == selId) && (type == si.getType())) {
				return 1;
			}

			// учитывать специфику стилей : если текущий итем Шаблон, то стиль находится в подитеме
			if ((type == TemplateSimpleItem.TYPE_STYLE) && (si.getType() == TemplateSimpleItem.TYPE_TEMPLATE)) {
				TemplateSimpleItem simpleItem = si.getSubItem();

				if (selId == simpleItem.getId()) {
					return 1;
				}
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<TemplateSimpleItem> i : ti.getChildren()) {
				if (expandTreeItemsById(i, selId, type) == 1) {
					ti.setExpanded(true);
					return 1;
				}
			}

			return 0;
		}

		/**
		 * Делаем рекурсивный проход по дереву и ищем первый TreeItem по id и типу обьекта
		 */
		TreeItem<TemplateSimpleItem> getTreeItemByTemplateId (TreeItem<TemplateSimpleItem> rootTI, long id, int type) {
			TreeItem<TemplateSimpleItem> tmpVal;

			//---------- проверяем и выбираем текущий итем
			if ((id == rootTI.getValue().getId()) && (type == rootTI.getValue().getType())) {
				return rootTI;
			}

			// учитывать специфику стилей : если текущий итем Шаблон, то стиль находится в подитеме
			if ((type == TemplateSimpleItem.TYPE_STYLE) &&
					(rootTI.getValue().getType() == TemplateSimpleItem.TYPE_TEMPLATE)) {

				TemplateSimpleItem simpleItem = rootTI.getValue().getSubItem();

				if (id == simpleItem.getId()) {
					return rootTI;
				}
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<TemplateSimpleItem> i : rootTI.getChildren()) {
				tmpVal = getTreeItemByTemplateId (i, id, type);
				if (tmpVal != null) {
					return tmpVal;
				}
			}

			return null;
		}

		/**
		 * Делаем рекурсивный проход по дереву и ищем первый TreeItem по id , типу обьекта и id темы.
		 * Функция создана для поиска активного стиля/шаблона после перетаскивания
		 */
		TreeItem<TemplateSimpleItem> getTreeItemByTemplateId (TreeItem<TemplateSimpleItem> rootTI, long id, int type, long themeId) {
			TemplateSimpleItem root = rootTI.getValue();
			TreeItem<TemplateSimpleItem> tmpVal;

			//---------- проверяем и выбираем текущий итем
			if ((id == root.getId()) && (type == root.getType()) && (themeId == root.getThemeId())) {
				return rootTI;
			}

			// учитывать специфику стилей : если текущий итем Шаблон, то стиль находится в подитеме
			if ((type == TemplateSimpleItem.TYPE_STYLE) &&
					(root.getType() == TemplateSimpleItem.TYPE_TEMPLATE)) {

				TemplateSimpleItem simpleItem = root.getSubItem();

				if ((id == simpleItem.getId()) && (themeId == simpleItem.getThemeId())) {
					return rootTI;
				}
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<TemplateSimpleItem> i : rootTI.getChildren()) {
				tmpVal = getTreeItemByTemplateId (i, id, type, themeId);
				if (tmpVal != null) {
					return tmpVal;
				}
			}

			return null;
		}

		/**
		 * Перемещает ветку стилей во всех темах
		 */
		private void moveStyleItemInTree(TreeItem<TemplateSimpleItem> itemSource,
										 TreeItem<TemplateSimpleItem> itemTarget) {
			TemplateSimpleItem sourceStyle;
			TemplateSimpleItem targetStyle;

			if (itemSource.getValue().getType() == TemplateSimpleItem.TYPE_TEMPLATE) {
				sourceStyle = itemSource.getValue().getSubItem();
			} else {
				sourceStyle = itemSource.getValue();
			}
			if (itemTarget.getValue().getType() == TemplateSimpleItem.TYPE_TEMPLATE) {
				targetStyle = itemTarget.getValue().getSubItem();
			} else {
				targetStyle = itemTarget.getValue();
			}

			for (TreeItem<TemplateSimpleItem> itemTheme : root.getChildren()) {       // cycle for themes
				TreeItem<TemplateSimpleItem> itemSourceLocal = getTreeItemByTemplateId (itemTheme, sourceStyle.getId(), sourceStyle.getType());
				TreeItem<TemplateSimpleItem> itemTargetLocal = getTreeItemByTemplateId (itemTheme, targetStyle.getId(), targetStyle.getType());

				itemSourceLocal.getParent().getChildren().remove(itemSourceLocal);
				itemTargetLocal.getChildren().add(itemSourceLocal);
			}
		}

		/**
		 * Удаляет стиль в дереве-контроле во всех темах
		 */
		private void deleteStyleItemFromTreeRecursive (TreeItem<TemplateSimpleItem> parentTI,
													   InfoTypeStyleItem sip) {
			// получаем список дочерних элементов
			List<TreeItem<TemplateSimpleItem>> oList = parentTI.getChildren();
			Iterator<TreeItem<TemplateSimpleItem>> iter = oList.iterator();

			while (iter.hasNext()) {
				TreeItem<TemplateSimpleItem> ti = iter.next();
				TemplateSimpleItem tft = new TemplateSimpleItem(ti.getValue());

				// проверяем элемент на нужный стиль
				if ((tft.getType() == TemplateSimpleItem.TYPE_STYLE) && (tft.getId() == sip.getId())) {
					//parentTI.getChildren().remove(i);
					iter.remove();
				} else {
					// вызываем эту ф-цию для проверки элементов следующей глубины вложения для текущего элемента
					deleteStyleItemFromTreeRecursive (ti, sip);
				}
			}
		}
	}
}
