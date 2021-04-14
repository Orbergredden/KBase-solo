package app;
	
import app.exceptions.KBase_DbConnEx;
import app.lib.AppDataObj;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.DBConnList_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.WinItem;
//import app.model.*;
import app.view.InputPassword_Controller;
import app.view.Root_Controller;
import app.view.business.SectionList_Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// test
import java.time.LocalDate;     // change !!!!!! on DateTime
import java.util.Comparator;
import java.util.prefs.Preferences;

/**
 * Основной класс приложения KBase_solo
 * @author Igor Makarevich
 */
public class Main extends Application {
	// собираем данные до кучи и передаем в виде параметра
	private Params params;

	/**
     * for Drag&Drop
     */
    public final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	
	/**
     * start
     * @param primaryStage главная сцена
     */	
	@Override
	public void start(Stage mainStage) {
		Preferences prefs = Preferences.userNodeForPackage(Main.class);
		
		//--------
		params.setMainStage(mainStage);
        params.getMainStage().setTitle("KBase solo");
        params.getMainStage().getIcons().add(new Image("file:resources/images/MainIco.png")); // Устанавливаем иконку приложения
        params.getMainStage().setWidth(prefs.getDouble("primaryStageWidth", 800));
        params.getMainStage().setHeight(prefs.getDouble("primaryStageHeight", 600));
        params.getMainStage().setX(prefs.getDouble("primaryStagePosX", 0));
        params.getMainStage().setY(prefs.getDouble("primaryStagePosY", 0));
        
        initRootLayout();

		if (params.getConfig().getItemValue("AppState","SaveAppStateOnExit").equals("1")) {
			restoreControlsStateMain(null);
		} else {
			autoDbConnect();
		}
	}

	/**
	 * действия при закрытии приложения
	 */
	@Override
	public void stop(){
		//beforeExit();
	}

	/**
	 * main method
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	/**
     * Конструктор
     */
    public Main() {
    	params = new Params();
    	
    	params.setMain(this);
    }
    
    /**
     * Геттер - возвращает главную сцену.
     * @return primaryStage - главная сцена
     */
//    public Stage getPrimaryStage() {
//        return primaryStage;
//    }
    
    /**
     * Инициализирует корневой макет.
     */
    private void initRootLayout() {
		BorderPane rootLayout;
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/Root_Layout.fxml"));
            rootLayout = loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
            params.getMainStage().setScene(scene);
            
            // Даём контроллеру доступ к главному прилодению.
            Root_Controller controller = loader.getController();
            controller.setParams(params);
            
            params.getMainStage().show();

            // событие выхода из программы
            params.getMainStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					if (! ShowAppMsg.showQuestion("CONFIRMATION", "Выход из приложения",
							"Выйти из приложения ?", "")) {
						//we.consume();
						beforeExit();
						
						System.out.println("stop()");
						
						System.exit(0);
					}
				}
			});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Открываем все соединения из списка с признаком Автоподключения
     */
    private void autoDbConnect () {
    	DBConnList_Parameters dbConnList = new DBConnList_Parameters();
    	boolean autoConnPresent = false;
    
    	for (int i=0; i<dbConnList.dbConnListParam.size(); i++) {
    		DBConn_Parameters curConn = dbConnList.dbConnListParam.get(i);
    		
    		if (curConn.getAutoConn()) {
    	    	if (dbConnect(new DBConn_Parameters(curConn)) != null) {          // передаем не ссылку а значения
    	    		autoConnPresent = true;
    	    		
    	    		//-------- меняем дату последнего подключения и увеличиваем счетчик
    	    		curConn.setLastConn(LocalDate.now());
    	    		curConn.setCounter(curConn.getCounter() + 1);
    	    	}
    		}
    	}
    	
    	// save to disk
    	if (autoConnPresent) {
    		dbConnList.saveToFile();
    	}
    }
    
    /**
     * Открываем новое соединение или делаем текущим открытое
     */
    public DBConCur_Parameters dbConnect (DBConn_Parameters conPar) {
    	// для возврата значений при вводе пароля
    	String password = conPar.getPassword();
    	String isPassword = "";
    	
    	//======== перед открытием коннекта проверяем может он уже открыт
		for (DBConCur_Parameters con : params.getConnDB().conList) {
			if (con.param.getConnId() == conPar.getConnId()) {
				params.getConnDB().curId = con.Id;
				params.getRootController().setActiveConnection(con.Id);
				//return true;
				return con;
			}
		}

    	//======== check and input password
    	if (password.equals("")) {
    		//-------- open window for password input
    		try {
    	    	// Загружаем fxml-файл и создаём новую сцену
    			// для всплывающего диалогового окна.
    			FXMLLoader loader = new FXMLLoader();
    			loader.setLocation(Main.class.getResource("view/InputPassword_Layout.fxml"));
    			AnchorPane page = loader.load();
    		
    			// Создаём диалоговое окно Stage.
    			Stage dialogStage = new Stage();
    			dialogStage.setTitle("Пароль");
    			dialogStage.initModality(Modality.WINDOW_MODAL);
    			dialogStage.initOwner(params.getMainStage());
    			Scene scene = new Scene(page);
    			dialogStage.setScene(scene);
    		
    			// Даём контроллеру доступ к главному прилодению.
    			InputPassword_Controller controllerIP = loader.getController();
    	        controllerIP.setParrentObj(password, isPassword, "Введите пароль для соединения \"" + conPar.getConnName() +"\"");
    	        
    	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
    	        dialogStage.showAndWait();
    	        
    	        // get result
    	        isPassword = controllerIP.isPassword;
    	        password = controllerIP.password;
        	} catch (IOException e) {
                e.printStackTrace();
            }
    		
    		//-------- check password is present
    		if (! isPassword.equals("Ok")) {
    			ShowAppMsg.showAlert("WARNING", "Пароль", "Не указан пароль", "Соединение не установлено.");
    			//return false;
				return null;
    		} else {
    			conPar.setPassword(password);
    		}
    	}
    	
    	//======== конектимся к БД -- создаем соединение
    	try {
    		params.getConnDB().curId = params.getConnDB().add(conPar);
			params.getRootController().createConnectionIndicators();
		} catch (KBase_DbConnEx e) {
			ShowAppMsg.showAlert("WARNING", "Соединение с БД", "Соединение не установлено", e.msg);
			//e.printStackTrace();
			//return false;
			return null;
		}

    	//return true;
		return params.getRootController().getActiveConnection();
    }
    
    /**
     * Возвращает соединение с БД текущего таба
     */
    public int getCurConnId () {
    	//return Integer.parseInt(params.getRootController().tabPane_Main.getSelectionModel().getSelectedItem().getId());
    	return Integer.parseInt(params.getTabPane_Main().getSelectionModel().getSelectedItem().getId());
    }
    
	/**
	 * действия перед закрытием приложения
	 */
	public void beforeExit () {
		Preferences prefs = Preferences.userNodeForPackage(Main.class);
		
		//-------- Сделать перед выходом из программы проверку на несохраненные данные, 
		//         спрашивать за сохранение и сохранять
		AppItem_Interface appItem_forCheckUnsavedData;
		boolean isUnsavedDataPresent = false;
		
		// по всем табам
		for (int i=0; 
			 (i<params.getTabPane_Main().getTabs().size()) && (!isUnsavedDataPresent); 
			 i++) {
			appItem_forCheckUnsavedData = (AppItem_Interface)params.getTabPane_Main().getTabs().get(i).getUserData();
			isUnsavedDataPresent = appItem_forCheckUnsavedData.checkUnsavedData();
		}
		
		// по всем окнам
		if (! isUnsavedDataPresent) {
			for (WinItem wi : params.getWinList().items) {
				isUnsavedDataPresent = wi.getController().checkUnsavedData();
				if (isUnsavedDataPresent) {
					break;
				}
			}
		}
		
		if (isUnsavedDataPresent) {
			if (ShowAppMsg.showQuestion (
					"CONFIRMATION", 
					"Вопрос", 
					"В программе есть несохраненные данные.", 
					"Сохнять их перед выходом ?")) {
				params.getRootController().handleSaveAll();
			}
		}

		//-------- save stage size and position
		prefs.putDouble("primaryStageWidth", params.getMainStage().getWidth());
		prefs.putDouble("primaryStageHeight",params.getMainStage().getHeight());
		prefs.putDouble("primaryStagePosX",  params.getMainStage().getX());
		prefs.putDouble("primaryStagePosY",  params.getMainStage().getY());

		//-------- save application (controls) state
		if (params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка утановки в конфигурации
			saveControlsStateMain (null);
		}
		
		//-------- close DB connections
		params.getConnDB().clear();
		//System.out.println("Total connections = " + connDB.conList.size());
		
		//-------- delete cache dir
		if (params.getConfig().getItemValue("AppState", "DeleteCacheOnExit").equals("1")) { // проверка утановки в конфигурации
			try {
				Files.walk(Paths.get(params.getConfig().getItemValue("directories", "PathDirCache")))
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
			} catch (NoSuchFileException e) {
				System.out.println ("Can not delete directory 'cache'. The directory does not exist.");
			} catch (IOException e) {
				ShowAppMsg.showAlert("WARNING", "Удаление файлового кеша", "Ошибка удаления файлового кеша", 
						params.getConfig().getItemValue("directories", "PathDirCache"));
				e.printStackTrace();
			}
		}
		//TODO 2020-03-13
	}
	
	/**
	 * Сохраняем состояние приложения (контролов)
	 * если fileName == null, то сохраняем в файл по умолчанию
	 */
	private void saveControlsStateMain (String fileName) {
		StateList stateList = new StateList();
		StateItem stateItem;
		AppItem_Interface appItem;
		DBConCur_Parameters conCur;           // обьект текущего соединения
		DBConn_Parameters conPar;             // параметры текущего соединения

		//---- save tabs
		for (int i=0; i<params.getTabPane_Main().getTabs().size(); i++) {
			appItem = (AppItem_Interface)params.getTabPane_Main().getTabs().get(i).getUserData();
			conCur = params.getConnDB().conList.get(params.getConnDB().getIndexById(appItem.getDbConnId()));
			conPar = conCur.param;
			
			Tab curTab = params.getTabPane_Main().getTabs().get(i);
			HBox hbox = (HBox) curTab.getGraphic();
			Node nodeTitle;
			if (hbox.getChildren().size() == 3) {
				nodeTitle = hbox.getChildren().get(2);
			} else {
				nodeTitle = hbox.getChildren().get(1);
			}
			String tabTitle = ((Label)nodeTitle).getText();

			stateList.add(
					"tabName",
					appItem.getName(),
					null);
			stateList.add(
					"tabDbConnId",
					Integer.toString(conPar.getConnId()),
					null);
			stateList.add(
					"tabAppItemId",
					Long.toString(appItem.getAppItemId()),
					null);
			stateList.add(
					"tabRootId",
					Long.toString(appItem.getRootId()),
					null);
			stateList.add(
					"tabCreateAction",
					"",
					null);
			stateList.add(
					"tabRenameTitleAction",
					tabTitle,
					null);
			stateItem = stateList.add(
					"tabSubItems",
					"",
					new StateList());
			appItem.saveControlsState(stateItem.subItems);
		}

		// active tab
		stateList.add(
				"tabActiveIndex",
				Integer.toString(params.getTabPane_Main().getSelectionModel().getSelectedIndex()),
				null);

		//---- save windows
		for (WinItem wi : params.getWinList().items) {
			stateList.add(
					"winClassName",
					wi.getClassName(),
					null);
			stateList.add(
					"winDbConnId",
					//Integer.toString(wi.getController().conn.param.getConnId()),
					Integer.toString(params.getConnDB().conList.get(params.getConnDB().getIndexById(wi.getController().getDbConnId())).param.getConnId()),
					null);
			stateList.add(
					"winItemId",
					Long.toString(wi.getController().getAppItemId()),
					null);
			stateList.add(
					"winRootId",
					Long.toString(wi.getController().getRootId()),
					null);
			stateList.add(
					"winWidth",
					Double.toString(wi.getStage().getWidth()),
					null);
			stateList.add(
					"winHeight",
					Double.toString(wi.getStage().getHeight()),
					null);
			stateList.add(
					"winX",
					Double.toString(wi.getStage().getX()),
					null);
			stateList.add(
					"winY",
					Double.toString(wi.getStage().getY()),
					null);
			stateList.add(
					"winCreateAction",
					"",
					null);
			stateItem = stateList.add(
					"winSubItems",
					"",
					new StateList());
			wi.getController().saveControlsState(stateItem.subItems);
		}

		if (fileName == null) {
			stateList.saveToFile();
		} else {
			ShowAppMsg.showAlert("WARNING", "Сохранение состояния программы", "Сохранение в файл с произвольным именем", 
					"Не реализовано.");
		}
	}
	
	/**
	 * Сохраняем состояние приложения (контролов)
	 * если fileName == null, то восстанавливаем из файла по умолчанию
	 */
	private void restoreControlsStateMain (String fileName) {
		StateList stateList = new StateList();

		String tabName = "noname";
		int tabDbConnId = 0;
		long tabAppItemId = 0;
		long tabRootId = 0;

		String winClassName = "noname";
		int winDbConnId = 0;
		long winItemId = 0;
		long winRootId = 0;
		double winWidth = 0;
		double winHeight = 0;
		double winX = 0;
		double winY = 0;

		stateList.loadFromFile();
		for (StateItem si : stateList.list) {
			DBConCur_Parameters conn = null;
			
			switch (si.getName()) {
				case "tabName" :
					tabName = si.getParams();
					break;
				case "tabDbConnId" :
					tabDbConnId = Integer.parseInt(si.getParams());
					break;
				case "tabAppItemId" :
					tabAppItemId = Long.parseLong(si.getParams());
					break;
				case "tabRootId" :
					tabRootId = Long.parseLong(si.getParams());
					break;
				case "tabCreateAction" :
					switch (tabName) {
						case AppItem_Interface.ELEMENT_SECTION_LIST :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == tabDbConnId) {
									DBConCur_Parameters conCur = dbConnect(cp);
									
									if (conCur != null) {
										Params params = new Params(this.params);
										params.setConCur(conCur);
										params.setObjContainer(params.getRootController());
										params.setTabPane_Cur(params.getTabPane_Main());
										
										AppDataObj.openSectionTree (params, tabRootId);
									}
								}
							}
							break;
						case AppItem_Interface.ELEMENT_DOCUMENT_VIEW :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == tabDbConnId) {
									conn = dbConnect(cp);
									if (conn != null) {
										SectionList_Controller sl = getSectionListInTabPane (tabDbConnId, 1);
										
										if (sl == null) {
											ShowAppMsg.showAlert("WARNING", "Восстановление состояния программы", 
													"Восстановление просмотра документа в главном табе.", 
													"Не открываем!\nОтсутствует соответствующий таб списка разделов.");
										} else {
											Params params = new Params(this.params);
											params.setConCur(conn);
											params.setObjContainer(params.getRootController());
											params.setTabPane_Cur(params.getTabPane_Main());
											params.setParentObj(sl);
											
											AppDataObj.openDocumentView (
													params,
													sl.treeViewCtrl.getTreeItemById(sl.treeViewCtrl.root, tabAppItemId));
										}
									}
								}
							}
							break;	
						case AppItem_Interface.ELEMENT_INFO_EDIT :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == tabDbConnId) {
									conn = dbConnect(cp);
									if (conn != null) {
										Params params = new Params(this.params);
										params.setConCur(conn);
										params.setObjContainer(params.getRootController());
										params.setTabPane_Cur(params.getTabPane_Main());
										
										AppDataObj.openEditInfo (params, conn.db.infoGet(tabAppItemId));
									}
								}
							}
							break;	
						case AppItem_Interface.ELEMENT_ICON_LIST :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == tabDbConnId) {
									if (dbConnect(cp) != null) {
										params.getRootController().handleCatalogIcons();
									}
								}
							}
							break;
						case AppItem_Interface.ELEMENT_TEMPLATE_LIST :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == tabDbConnId) {
									if (dbConnect(cp) != null) {
										params.getRootController().handleCatalogTemplates();
									}
								}
							}
							break;
					}
					break;
				case "tabRenameTitleAction" :
					// берем последний созданный таб и изменяем в нем заголовок 
					if (params.getTabPane_Main().getTabs().size() > 0) {
						String tabTitle = si.getParams();
						int tabIndex = params.getTabPane_Main().getTabs().size()-1;
						Tab curTab = params.getTabPane_Main().getTabs().get(tabIndex);
						HBox hbox = (HBox) curTab.getGraphic();
						Node nodeTitle = hbox.getChildren().get(1);
						
						((Label)nodeTitle).setText(tabTitle);
					}
					break;
				case "tabSubItems" :
					// берем последний созданный таб и вызываем в нем метод восстановления состояния
					if (params.getTabPane_Main().getTabs().size() > 0) {
						int tabIndex = params.getTabPane_Main().getTabs().size()-1;
						AppItem_Interface ai =
								(AppItem_Interface)params.getTabPane_Main().getTabs().get(tabIndex).getUserData();
						ai.restoreControlsState(si.subItems);
					}
					break;
				case "tabActiveIndex" :
					params.getTabPane_Main().getSelectionModel().select(Integer.parseInt(si.getParams()));
					break;

				//---- open window
				case "winClassName" :
					winClassName = si.getParams();
					break;
				case "winDbConnId" :
					winDbConnId = Integer.parseInt(si.getParams());
					break;
				case "winItemId" :
					winItemId = Long.parseLong(si.getParams());
					break;
				case "winRootId" :
					winRootId = Long.parseLong(si.getParams());
					break;
				case "winWidth" :
					winWidth = Double.parseDouble(si.getParams());
					break;
				case "winHeight" :
					winHeight = Double.parseDouble(si.getParams());
					break;
				case "winX" :
					winX = Double.parseDouble(si.getParams());
					break;
				case "winY" :
					winY = Double.parseDouble(si.getParams());
					break;
				case "winCreateAction" :
					switch (winClassName) {
						case AppItem_Interface.ELEMENT_SECTION_LIST :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == winDbConnId) {
									conn = dbConnect(cp);
									if (conn != null) {
										Params params = new Params(this.params);
										params.setConCur(conn);
										params.setObjContainer(params.getWinList());
										params.setTabPane_Cur(null);
										
										(new AppDataObj()).openSectionTreeInWin(
												params, 
												winRootId,
												winWidth, winHeight, winX, winY);
									}
								}
							}
							break;
						case AppItem_Interface.ELEMENT_DOCUMENT_VIEW :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == winDbConnId) {
									conn = dbConnect(cp);
									if (conn != null) {
										SectionList_Controller sl = getSectionListInTabPane (winDbConnId, 1);
										
										if (sl == null) {
											ShowAppMsg.showAlert("WARNING", "Восстановление состояния программы", 
													"Восстановление просмотра документа в окне.", 
													"Не открываем!\nОтсутствует соответствующий таб списка разделов.");
										} else {
											Params params = new Params(this.params);
											params.setConCur(conn);
											params.setObjContainer(params.getWinList());
											params.setTabPane_Cur(null);
											params.setParentObj(sl);
											
											(new AppDataObj()).openDocumentViewInWin(
													params,
													sl.treeViewCtrl.getTreeItemById(sl.treeViewCtrl.root, winItemId),
													winWidth, winHeight, winX, winY);
										}
									}
								}
							}
							break;
						case AppItem_Interface.ELEMENT_INFO_EDIT :
							for (DBConn_Parameters cp : new DBConnList_Parameters().dbConnListParam) {
								if (cp.getConnId() == winDbConnId) {
									conn = dbConnect(cp);
									if (conn != null) {
										Params params = new Params(this.params);
										params.setConCur(conn);
										params.setObjContainer(params.getWinList());
										params.setTabPane_Cur(null);
										params.setParentObj(null);
										
										(new AppDataObj()).openEditInfoInWin(
												params,
												conn.db.infoGet(winItemId),
												winWidth, winHeight, winX, winY);
									}
								}
							}
							break;
					}
					break;
				case "winSubItems" :
					// берем последнее созданное окно и вызываем в нем метод восстановления состояния
					if (params.getWinList().items.size() > 0) {
						int winIndex = params.getWinList().items.size() - 1;
						AppItem_Interface ai =
								(AppItem_Interface)params.getWinList().items.get(winIndex).getController();
						ai.restoreControlsState(si.subItems);
					}
					break;
			}
		}
	}
	
	/**
	 * поиск таба списка разделов нужного конекта. Параметром задать - искать с начала или конца списка табов
	 * @param dbConnId
	 * @param direction
	 * @return
	 */
	private SectionList_Controller getSectionListInTabPane (int dbConnId, int direction) {
		AppItem_Interface appItem;
		
		DBConCur_Parameters conCur;           // обьект текущего соединения
		DBConn_Parameters conPar;             // параметры текущего соединения
		
		SectionList_Controller sl = null;
		
		if (direction == 0) {
			for (int i=0; i<params.getTabPane_Main().getTabs().size(); i++) {
				appItem = (AppItem_Interface)params.getTabPane_Main().getTabs().get(i).getUserData();
				conCur = params.getConnDB().conList.get(params.getConnDB().getIndexById(appItem.getDbConnId()));
				conPar = conCur.param;
				
				if (appItem.getName().equals("SectionList_Controller") &&
				    (conPar.getConnId() == dbConnId)) {
					sl = (SectionList_Controller) appItem.getController();
					//System.out.println("sl.getName() = " + sl.getName());
				}
			}
		} else {
			for (int i=params.getTabPane_Main().getTabs().size()-1; i>=0; i--) {
				appItem = (AppItem_Interface)params.getTabPane_Main().getTabs().get(i).getUserData();
				conCur = params.getConnDB().conList.get(params.getConnDB().getIndexById(appItem.getDbConnId()));
				conPar = conCur.param;
				
				if (appItem.getName().equals("SectionList_Controller") &&
				    (conPar.getConnId() == dbConnId)) {
					sl = (SectionList_Controller) appItem.getController();
				}
			}
		}
		return sl;
	}
}

