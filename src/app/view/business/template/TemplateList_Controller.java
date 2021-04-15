
package app.view.business.template;

import app.lib.ConvertType;
import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.Main;
import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.InfoTypeItem;
//import app.model.business.InfoTypeStyleItem;
//import app.model.business.template.TemplateItem;
//import app.model.business.templates_old.TemplateRequiredFileItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateThemeItem;
import app.view.business.Container_Interface;

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
    	button_templateAdd.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	button_templateUpdate.setTooltip(new Tooltip("Изменить элемент"));
    	button_templateUpdate.setGraphic(new ImageView(new Image("file:resources/images/icon_update_16.png")));
    	button_templateDelete.setTooltip(new Tooltip("Удалить элемент"));
    	button_templateDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	button_templateCopy.setTooltip(new Tooltip("Копировать элемент (внутренний буфер)"));
    	button_templateCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	button_templateCut.setTooltip(new Tooltip("Вырезать элемент (внутренний буфер)"));
    	button_templateCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
    	button_templatePaste.setTooltip(new Tooltip("Вставить элемент (внутренний буфер)"));
    	button_templatePaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    		
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
/*    	
    	if ((treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getType() == 0) || // 0 - корень
    		(treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getType() == 2) || // 2 - папка для обязательных файлов
    		(treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getType() == 4)) { // 4 - папка для шаблонов определенного типа
    		ShowAppMsg.showAlert("WARNING", "Не редактируется", 
    				"\"" + treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getName() + "\"", 
    				"Данный элемент списка не редактируется.");
    		return;
    	}
*/    	
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

    	//
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
		return AppItem_Interface.ELEMENT_TEMPLATE_LIST;
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
					0, "Темы и Шаблоны", "это корень, он не редактируется", 0, 
					TemplateSimpleItem.TYPE_ITEM_ROOT, 0));
			root.setExpanded(true);
			treeTableView_templates.setShowRoot(false);
			treeTableView_templates.setRoot(root);

			initTreeItems(root);
			initCellFactory();
///			initRowFactory();

			// Слушаем изменения выбора, и при изменении отображаем информацию.
			treeTableView_templates.getSelectionModel().selectedItemProperty().addListener(
					(observable, oldValue, newValue) -> showTemplateDetails(newValue));

///			initSortColumn();
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

			if (f == null)  return;
			
			TreeItem<TemplateSimpleItem> sectionThemeItem = new TreeItem<>(new TemplateSimpleItem(
					0, "Темы", "", 0, TemplateSimpleItem.TYPE_ITEM_SECTION_THEME, 0));
			ti.getChildren().add(sectionThemeItem);
			
			List<TemplateThemeItem> themesList = conn.db.templateThemesList();
			
			for (TemplateThemeItem i : themesList) {       // cycle for themes
				TreeItem<TemplateSimpleItem> themeItem = new TreeItem<>(i);
				themeItem.getValue().setThemeId(i.getId());
				themeItem.getValue().setTypeItem(TemplateSimpleItem.TYPE_ITEM_THEME);
				sectionThemeItem.getChildren().add(themeItem);
				
				
				
				
				
				
				
			}
			
			//======== Templates
			TreeItem<TemplateSimpleItem> sectionTemplateItem = new TreeItem<>(new TemplateSimpleItem(
					0, "Шаблоны", "", 0, TemplateSimpleItem.TYPE_ITEM_SECTION_TEMPLATE, 0));
			ti.getChildren().add(sectionTemplateItem);
			
		
		
		}
		//TODO
		
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
						switch (row.getTypeItem()) {
							case TemplateSimpleItem.TYPE_ITEM_ROOT :                      // 0 - корень
								graphic = null;
								break;
							case TemplateSimpleItem.TYPE_ITEM_SECTION_THEME :
							case TemplateSimpleItem.TYPE_ITEM_THEME : 
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_theme_16.png"));
								break;
							case TemplateSimpleItem.TYPE_ITEM_SECTION_FILE : 
							case TemplateSimpleItem.TYPE_ITEM_SECTION_FILE_OPTIONAL :
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_section_file_16.png"));
								break;
								
								
								
							case TemplateSimpleItem.TYPE_ITEM_SECTION_TEMPLATE :
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_section_template_16.png"));
								break;
								
/*								
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
*/
/*								
							case TemplateSimpleItem.TYPE_DIR_FOR_TEMPLATES :                      // 4 - папка для шаблонов определенного типа
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_InfoBlock_16.png"));
								break;
*/
/*
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
*/



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
		//TODO

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
				if (f.getTypeItem() != 0) {
					TreeItem<TemplateSimpleItem> curTI = ti.getParent();
					TemplateSimpleItem curTemplate = curTI.getValue();

					while (curTemplate.getTypeItem() != 0) {
						if ((level == 1) || (! isFirst)) {
							msg = curTemplate.getName() + " / " + msg;
						} else {
							msg = curTemplate.getName();
							isFirst = false;
						}

						if (curTemplate.getTypeItem() == 0)   break;
						else {
							curTI = curTI.getParent();
							curTemplate = curTI.getValue();
						}
					}
				}
			}
			return msg;
		}
		
		
	}
}
