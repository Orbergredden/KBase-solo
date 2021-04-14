
package app.view;

import app.Main;
import app.lib.AppDataObj;
import app.lib.ConvertType;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.WinItem;
import app.view.business.IconsList_Controller;
import app.view.business.template.TemplateList_Controller;
import app.view.business.Container_Interface;
import app.view.structure.TabNavigationHistory;

import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контроллер для корневого макета. Корневой макет предоставляет базовый
 * макет приложения, содержащий строку меню и место, где будут размещены
 * остальные элементы JavaFX.
 * 
 * @author Игорь Макаревич
 */
public class Root_Controller implements Container_Interface {
    /**
	 * Пункт меню : сохранение информации в БД из активного таба
	 */
	@FXML
	private MenuItem menuitem_Save;
	/**
	 * Пункт меню : сохранение всей измененной информации в БД
	 */
	@FXML
	private MenuItem menuitem_SaveAll;
    /**
	 * Пункт меню со Списком НеПользовательских настроек программы
	 */
	@FXML
	private MenuItem menuitem_ConfigSysMainList;
	/**
	 * Пункт меню со Списком настроек программы
	 */
	@FXML
	private MenuItem menuitem_ConfigMainList;
	/**
     * Пункт меню со Списком соединений к источникам данных
     */
    @FXML
    private MenuItem menuitem_ConnectToDB;
	/**
	 * Пункт меню с Разделы документов
	 */
	@FXML
	private MenuItem menuitem_SectionsOfDocuments;
    /**
     * Пункт меню с Каталогом пиктограмм
     */
    @FXML
    private MenuItem menuitem_CatalogIcons;
    /**
     * Пункт меню с Каталогом шаблонов
     */
    @FXML
    private MenuItem menuitem_CatalogTemplates;
    /**
     * Пункт меню О программе
     */
    @FXML
    private MenuItem menuitem_About;
    
    /**
     * Главный контейнер вкладок (TabPane)
     */
    @FXML
    private TabPane tabPane_Main; 
    
    /**
     * Для вывода на экран простого сообщения
     */
    @FXML
    private Label label_StatusBar_msg;

	/**
	 * Контейнер для лампочек активных коннектов
	 */
	@FXML
	private HBox containerConnectionIndicator;
	
	private Params params;
	
	private TabNavigationHistory tabNavigationHistory;
	
	/**
	 * Constructor 
	 */
	public Root_Controller() {
		tabNavigationHistory = new TabNavigationHistory();
	}

	/**
     * Вызывается главным приложением, чтобы передать параметры и вернуть ссылку на контроллер
     * по сути это сетер
     */
    public void setParams(Params params) {
    	this.params = params;
    	this.params.setRootController(this);
    	this.params.setTabPane_Main(tabPane_Main);
    	
    	// init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	//======== main menu
    	menuitem_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	menuitem_SaveAll.setGraphic(new ImageView(new Image("file:resources/images/icon_save_all_24.png",16,16,false,false)));
    	menuitem_ConfigSysMainList.setGraphic(new ImageView(new Image("file:resources/images/icon_setting_16.png")));
		menuitem_ConfigMainList.setGraphic(new ImageView(new Image("file:resources/images/icon_setting_16.png")));
		menuitem_ConnectToDB.setGraphic(new ImageView(new Image("file:resources/images/icon_Connect_16.png")));
		menuitem_SectionsOfDocuments.setGraphic(new ImageView(new Image("file:resources/images/icon_Sections_16.png",16,16,false,false)));
		menuitem_CatalogIcons.setGraphic(new ImageView(new Image("file:resources/images/icon_CatalogIcons_16.png")));
    	menuitem_CatalogTemplates.setGraphic(new ImageView(new Image("file:resources/images/icon_templates/icon_CatalogTemplates_16.png")));
    	menuitem_About.setGraphic(new ImageView(new Image("file:resources/images/icon_About_16.png")));
    	
    	//======== Tabs
    	//изменение активного таба
    	tabPane_Main.getSelectionModel().selectedItemProperty().addListener(
    	    new ChangeListener<Tab>() {
    	        @Override
    	        public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
    	        	//-------- устанавливаем стили (не активные)
    	        	if (oldTab != null) {
						AppItem_Interface oldTabCtrl = (AppItem_Interface)oldTab.getUserData();

    	        		// обьект текущего соединения
						DBConCur_Parameters conOld =
								 params.getConnDB().conList.get(params.getConnDB().getIndexById(oldTabCtrl.getDbConnId()));
    	            	//DBConCur_Parameters conOld =
    	            	//		mainApp.connDB.conList.get(mainApp.connDB.getIndexById(Integer.parseInt(oldTab.getId())));
    	            	// параметры текущего соединения
    	            	DBConn_Parameters parOld = conOld.param;
    	        		
    	            	if (parOld.getColorEnable()) {
    	            		oldTab.setStyle("-fx-background-color: #" + 
    	            				ConvertType.colorToHex(new Color(parOld.getColorBRed_N(),  parOld.getColorBGreen_N(),
    	            						parOld.getColorBBlue_N(), parOld.getColorBOpacity_N())) + 
    	            				";");
    	            		for(Node nodeIn : ((HBox)oldTab.getGraphic()).getChildren()) {
    	            			if (nodeIn instanceof Label) {
    	            				((Label)nodeIn).setStyle("-fx-text-fill: #" +
    	            						ConvertType.colorToHex(new Color(parOld.getColorTRed_N(),  parOld.getColorTGreen_N(),
    	    	            						parOld.getColorTBlue_N(), parOld.getColorTOpacity_N())) +
    	            						";-fx-padding: 0 0 0 5px;");
    	            			}
    	            		}
    	            	}
    	        	}
    	        	
    	        	//-------- устанавливаем стили (активные)
    	        	if (newTab != null) {
						AppItem_Interface newTabCtrl = (AppItem_Interface)newTab.getUserData();

    	        		// обьект текущего соединения
    	            	DBConCur_Parameters conNew = 
    	            			params.getConnDB().conList.get(params.getConnDB().getIndexById(newTabCtrl.getDbConnId()));
    	            	// параметры текущего соединения
    	            	DBConn_Parameters parNew = conNew.param;
    	        		
    	            	if (parNew.getColorEnable()) {
    	            		newTab.setStyle("-fx-background-color: #" + 
    	            				        ConvertType.colorToHex(new Color(parNew.getColorBRed_A(),  parNew.getColorBGreen_A(),
    	            						                                 parNew.getColorBBlue_A(), parNew.getColorBOpacity_A())) + 
    	            				        ";");
    	            		for(Node nodeIn : ((HBox)newTab.getGraphic()).getChildren()) {
    	            			if (nodeIn instanceof Label) {
    	            				((Label)nodeIn).setStyle("-fx-text-fill: #" +
    	            						ConvertType.colorToHex(new Color(parNew.getColorTRed_A(),  parNew.getColorTGreen_A(),
    	    	            						parNew.getColorTBlue_A(), parNew.getColorTOpacity_A())) +
    	            						";-fx-padding: 0 0 0 5px;");
    	            			}
    	            		}
    	            	}
    	        	}
    	        	
    	        	//-------- adding a new tab to the history of active tabs (stack)
    	        	if (newTab != null) {
						AppItem_Interface newTabCtrl = (AppItem_Interface)newTab.getUserData();
    	        	
						if (! tabNavigationHistory.getIsDeleted()) {
							tabNavigationHistory.add(newTabCtrl.getOID());
						}
    	        	}
    	        }
    	    }
    	);
    }

	/**
	 * Создание индикаторов открытых коннектов
	 */
	public void createConnectionIndicators () {
		ToggleGroup groupRadio = new ToggleGroup();

		// очищаем контейнер от индикаторов
		containerConnectionIndicator.getChildren().clear();

		// создаем новый список индикаторов
		for (DBConCur_Parameters c : params.getConnDB().conList) {
			DBConn_Parameters p = c.param;
			RadioButton radioButtonCur = new RadioButton (p.getConnName());
			Label labelEmpty = new Label("   ");

			if (p.getColorEnable())
				radioButtonCur.setStyle
					("-fx-text-fill: #"+
							ConvertType.colorToHex(new Color(p.getColorTRed_A(),  p.getColorTGreen_A(),
									p.getColorTBlue_A(), p.getColorTOpacity_A())) +"; " +
							"-fx-background-color: #"+
							ConvertType.colorToHex(new Color(p.getColorBRed_A(),  p.getColorBGreen_A(),
									p.getColorBBlue_A(), p.getColorBOpacity_A())) +";"
					);
			radioButtonCur.setTooltip(new Tooltip(p.getConnName()));
			radioButtonCur.setToggleGroup(groupRadio);
			radioButtonCur.setSelected(true);
			radioButtonCur.setUserData(c);

			containerConnectionIndicator.getChildren().add(radioButtonCur);
			containerConnectionIndicator.getChildren().add(labelEmpty);
		}
	}

	/**
	 * Возвращает активный коннект
	 */
	public DBConCur_Parameters getActiveConnection () {
		DBConCur_Parameters retVal = null;

		for (Object o : containerConnectionIndicator.getChildren()) {
			if ((o instanceof RadioButton) && (((RadioButton) o).isSelected())) {
				retVal = (DBConCur_Parameters)((RadioButton) o).getUserData();
			}
		}

		return retVal;
	}

	/**
	 * Устанавливает активный коннект
	 */
	public void setActiveConnection (int id) {
		int curNum = 1;

		for (Object o : containerConnectionIndicator.getChildren()) {
			if (o instanceof RadioButton) {
				if (curNum == id) {
					((RadioButton) o).setSelected(true);
				}
				curNum++;
			}
		}
	}
	
	/**
     * Выводит сообщение в Статус Бар
     */
    public void statusBar_ShowMsg (String msg) {
    	label_StatusBar_msg.setText(" " + msg);
    }
	
	/**
	 * Сохраняем изменения в активном табе
	 */
	@FXML
	private void handleSave () {
		AppItem_Interface appItem = (AppItem_Interface)
				tabPane_Main.getTabs().get(tabPane_Main.getSelectionModel().getSelectedIndex()).getUserData();
		
		appItem.save();
	}
	
	/**
	 * Сохраняем всю измененную информацию
	 */
	@FXML
	public void handleSaveAll () {
		AppItem_Interface appItem;
		
		// по всем табам
		for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData();
			appItem.saveAll();
		}
		
		// по всем окнам
		for (WinItem wi : params.getWinList().items) {
			wi.getController().saveAll();
		}
	}
	
	/**
	 * Окно со списком НЕ пользовательских настроек программы.
	 */
	@FXML
	private void handleConfigSysMainList() {
		try {
			// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/ConfigMainList.fxml"));
			AnchorPane page = loader.load();

			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Список не пользовательских настроек программы");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_setting_16.png"));

			Preferences prefs = Preferences.userNodeForPackage(ConfigMainList_Controller.class);
			dialogStage.setWidth(prefs.getDouble("ConfigMainList_Width", 600));
			dialogStage.setHeight(prefs.getDouble("ConfigMainList_Height", 400));
			dialogStage.setX(prefs.getDouble("ConfigMainList_PosX", 0));
			dialogStage.setY(prefs.getDouble("ConfigMainList_PosY", 0));

			// Даём контроллеру доступ к главному прилодению.
			ConfigMainList_Controller controller = loader.getController();
			controller.setMainApp(params.getConfigSys(), dialogStage);

			// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Окно со списком пользовательских настроек программы.
	 */
	@FXML
	private void handleConfigMainList() {
		try {
			// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/ConfigMainList.fxml"));
			AnchorPane page = loader.load();

			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Список пользовательских настроек программы");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_setting_16.png"));

			Preferences prefs = Preferences.userNodeForPackage(ConfigMainList_Controller.class);
			dialogStage.setWidth(prefs.getDouble("ConfigMainList_Width", 600));
			dialogStage.setHeight(prefs.getDouble("ConfigMainList_Height", 400));
			dialogStage.setX(prefs.getDouble("ConfigMainList_PosX", 0));
			dialogStage.setY(prefs.getDouble("ConfigMainList_PosY", 0));

			// Даём контроллеру доступ к главному прилодению.
			ConfigMainList_Controller controller = loader.getController();
			controller.setMainApp(params.getConfig(), dialogStage);

			// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    /**
     * Закрывает приложение.
     */
    @FXML
    private void handleExit() {
    	params.getMain().beforeExit();
    	System.exit(0);
    }

	/**
	 * Подключаемся к БД. Есть возможность администрирования коннектов.
	 */
	@FXML
	private void handleConnectToDB() {
		//ShowAppMsg.showAlert("INFORMATION", "handleConnectToDB", "Test", "test");
		
		try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/DBConnList_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Список параметров подключений");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Connect_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(DBConnList_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageDBConnList_Width", 600));
			dialogStage.setHeight(prefs.getDouble("stageDBConnList_Height", 400));
			dialogStage.setX(prefs.getDouble("stageDBConnList_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageDBConnList_PosY", 0));
		
			// Даём контроллеру доступ к главному прилодению.
			DBConnList_Controller controller = loader.getController();
	        controller.setMainApp(this.params.getMain(), dialogStage);
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Открывает таб с деревом разделов документов
	 */
	@FXML
	public void handleSectionsOfDocuments() {
		DBConCur_Parameters conCur = getActiveConnection();        // обьект текущего соединения

		//
		if (conCur == null) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("INFORMATION", "Сообщение", "Нет активного соединения с источником данных", "Необходимо подключиться.");
			//handleConnectToDB();
			return;
		}

		//-------- открываем таб
		Params params = new Params(this.params);
		params.setConCur(conCur);
		params.setObjContainer(this);
		params.setTabPane_Cur(tabPane_Main);
		params.setStageCur(params.getMainStage());
		
		AppDataObj.openSectionTree (params, 0);
	}

    /**
     * Открывает таб со справочником пиктограмм (древовидный список иконок)
     */
    @FXML
    public void handleCatalogIcons() {
		IconsList_Controller controller = null;
		DBConCur_Parameters conCur = getActiveConnection();        // обьект текущего соединения

    	//
		if (conCur == null) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("INFORMATION", "Сообщение", "Нет активного соединения с источником данных", "Необходимо подключиться.");
			//handleConnectToDB();
			return;
		}

    	// параметры текущего соединения
    	DBConn_Parameters conPar = conCur.param;
    	// новый добавляемый Таб с иконками
    	final Tab tab;
    	// основной контейнер для таба
    	AnchorPane page = null;
    	
    	//======== загружаем контроллер таба в AnchorPane
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/IconsList_Layout.fxml"));
			page = loader.load();
			
			// Даём контроллеру доступ к главному прилодению.
			controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setConCur(conCur);
			params.setObjContainer(this);
			params.setTabPane_Cur(tabPane_Main);
			
	        controller.setParams(params);
    	} catch (IOException e) {
            e.printStackTrace();
        }
    	
    	//======== создаем основной таб для этого соединения и добавляем туда фрейм
    	tab = createContainer (
    			conPar.getConnName() + " - пиктограммы", 
    			"file:resources/images/icon_CatalogIcons_16.png", 
    			conPar.getConnName() + " - пиктограммы",
				page, 
				controller);
    	
    	tabPane_Main.getTabs().add(tab);
    	tabPane_Main.getSelectionModel().select(tab);
    }
    
    /**
     * Открывает таб со справочником шаблонов (древовидный список тем и шаблонов)
     */
    @FXML
    public void handleCatalogTemplates() {
		TemplateList_Controller controller = null;
		DBConCur_Parameters conCur = getActiveConnection();        // обьект текущего соединения

		//
		if (conCur == null) {
			ShowAppMsg.showAlert("INFORMATION", "Сообщение", "Нет активного соединения с источником данных", "Необходимо подключиться.");
			//handleConnectToDB();
			return;
		}
    	
    	// параметры текущего соединения
    	DBConn_Parameters conPar = conCur.param;
    	// новый добавляемый Таб с шаблонами
    	final Tab tab;
    	// основной контейнер для таба
    	AnchorPane page = null;
    
    	//======== загружаем контроллер таба в AnchorPane
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/template/TemplateList.fxml"));
			page = loader.load();
			
			// Даём контроллеру доступ к главному прилодению.
			controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setConCur(conCur);
			params.setObjContainer(this);
			params.setTabPane_Cur(tabPane_Main);
			
	        controller.setParams(params);
    	} catch (IOException e) {
            e.printStackTrace();
        }
    	
    	//======== создаем основной таб для этого соединения и добавляем туда фрейм
    	tab = createContainer (
    			conPar.getConnName() + " - шаблоны", 
    			"file:resources/images/icon_templates/icon_CatalogTemplates_16.png", 
    			conPar.getConnName() + " - шаблоны",
				page, 
				controller);
    	
    	tabPane_Main.getTabs().add(tab);
    	tabPane_Main.getSelectionModel().select(tab);
    }
    
    /**
     * Открывает диалоговое окно about.
     */
    @FXML
    private void handleAbout() {
    	ShowAppMsg.showAlert("INFORMATION", "О программе", params.getConfigSys().getItemValue("AboutProgram", "name"), 
				 "Версия: "+params.getConfigSys().getItemValue("AboutProgram", "version")+
				 " ("+params.getConfigSys().getItemValue("AboutProgram", "BeginDate")+
				 " - "+params.getConfigSys().getItemValue("AboutProgram", "EndDate")+")\n" +
				 "Поддерживаемая версия БД: "+ params.getConfigSys().getItemValue("DB", "Required version")+"\n" +
                "Кодовое название : "+params.getConfigSys().getItemValue("AboutProgram", "CodeName")+" \n" +
                "Автор: "+params.getConfigSys().getItemValue("AboutProgram", "Author")+"\n" +
                   "Сайт: "+params.getConfigSys().getItemValue("AboutProgram", "site"));
    }

	/**
	 * Реализуем метод интерфейса Container_Interface.
	 * Показывает состояние инфо блока во внешнем контейнере - были несохраненные изменения или нет.
	 */
	public void showStateChanged(int oid, boolean isChanged) {
		Tab ourTab;
		HBox hbox;
		String changingIconId = "changingIconId";

		//---- ищем таб по oid
		for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
			//if ((tabPane_Main.getTabs().get(i).getId() != null) && tabPane_Main.getTabs().get(i).getId().equals(containerId)) {
			if ((tabPane_Main.getTabs().get(i).getUserData() != null) && 
				(((AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData()).getOID() == oid)) {
				ourTab = tabPane_Main.getTabs().get(i);
				hbox = (HBox) ourTab.getGraphic();

				if (isChanged) {               // was change
					if ((hbox.getChildren().get(1).getId() == null) ||
							(! hbox.getChildren().get(1).getId().equals(changingIconId))) {
						ImageView imageView_changed;

						imageView_changed = new ImageView(new Image("file:resources/images/icon_edited_16.png"));
						imageView_changed.setId(changingIconId);

						hbox.getChildren().add(1, imageView_changed);
					}
				} else {                       // no change
					if ((hbox.getChildren().get(1).getId() != null) &&
							(hbox.getChildren().get(1).getId().equals(changingIconId))) {
						hbox.getChildren().remove(1);
					}
				}
			}
		}
	}

	/**
	 * Реализуем метод интерфейса Container_Interface.
	 * Закрываем фрейм с редактированием инфо блока
	 */
	public void closeContainer (int oid) {
		
		for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
			if ((tabPane_Main.getTabs().get(i).getUserData() != null) && 
				(((AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData()).getOID() == oid)) {
				
				tabNavigationHistory.delete(oid);
				
				tabPane_Main.getTabs().remove(i);
			}
		}
		
		tabNavigationHistory.setIsDeleted(false);
		int lastOId = tabNavigationHistory.getLast();
		
		if (lastOId != 0) {
			for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
				if ((tabPane_Main.getTabs().get(i).getUserData() != null) && 
					(((AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData()).getOID() == lastOId)) {
					
					// activate tab
					tabPane_Main.getSelectionModel().select(i);
				}
			}
		}
	}
}
