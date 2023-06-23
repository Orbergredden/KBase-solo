
package app.lib;

import app.Main;
import app.model.DBConCur_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.business.InfoHeaderItem;
import app.model.business.SectionItem;
import app.model.WinItem;
import app.view.business.DocumentView_Controller;
import app.view.business.InfoEdit_Controller;
import app.view.business.SectionList_Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Данный класс предназначен для работы по различным обьектам программы.
 * Обьекты такие как Разделы, Стили, Шаблоны, ...
 * 
 * @author Igor Makarevich
 */
public class AppDataObj {
	/**
	 * Раздел. Возвращает id темы (по умолчанию, если не указана) для указанного раздела.
	 */
	public static long sectionGetDefaultTheme (DBConCur_Parameters conn, long sectionId) {
		long retVal;
		SectionItem si;
	
		//-------- ищем в таблице разделов
		si = conn.db.sectionGetById(sectionId);
		while ((si.getThemeId() == 0) && (si.getParentId() > 0)) {
			si = conn.db.sectionGetById(si.getParentId());
		}
		if (si.getThemeId() > 0) {
			retVal = si.getThemeId();
		} else {
			retVal = Long.parseLong(conn.db.settingsGetValue("SECTION_THEME_DEFAULT"));
		}
		
		return retVal;
	}
	
	/**
	 * Открываем таб с деревом/веткой разделов
	 */
	public static void openSectionTree (Params params, long rootSectionId) {
		DBConn_Parameters conPar = params.getConCur().param;         // параметры текущего соединения
		final Tab tab;                                 // основной таб данного соединения и его основной контейнер
		AnchorPane paneMain = null;
		SectionList_Controller controller = null;      // контроллер сцены внутри таба
		String sectionName = (rootSectionId == 0) ? "усі" : params.getConCur().db.sectionGetById(rootSectionId).getName();

		//======== загружаем контроллер основного таба в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionList.fxml"));
			paneMain = loader.load();

			// Даём контроллеру доступ к главному прилодению.
			controller = loader.getController();
			controller.setParams(params, rootSectionId);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//======== создаем основной таб для этого соединения и добавляем туда фрейм
		tab = params.getObjContainer().createContainer (
				conPar.getConnName() + " : " + sectionName, 
				"file:resources/images/icon_Sections_16.png", 
				conPar.getConnName() + " : (" + rootSectionId + ") " + sectionName, 
				paneMain, 
				controller);

		params.getTabPane_Cur().getTabs().add(tab);
		params.getTabPane_Cur().getSelectionModel().select(tab);
	}
	
	/**
	 * Открываем отдельное окно для просмотра дерева/ветки разделов
	 */
	public void openSectionTreeInWin (Params params, long rootSectionId, Double... d_win) {
		AnchorPane paneForView = null;
		int winId = params.getWinList().getNextId();
		String containerName = "sectionTree_"+ rootSectionId +","+ winId;
		SectionList_Controller controller = null;
		Stage stage = new Stage();
		WinItem winItem = null;
		String sectionName = (rootSectionId == 0) ? "усі" : params.getConCur().db.sectionGetById(rootSectionId).getName();

		//---- загружаем контроллер в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionList.fxml"));
			paneForView = loader.load();

			// Отображаем сцену, содержащую корневой макет.
			Scene scene = new Scene(paneForView);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			stage.setScene(scene);

			// Даём контроллеру доступ к фрейму с текущим документом
			// и добавляем в список открытых окон
			controller = loader.getController();
			
			winItem = new WinItem (containerName, "SectionList_Controller", controller, stage, params.getWinList());
			params.getWinList().add(winItem);
			
			controller.setParams(params, rootSectionId);
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Просмотр дерева разделов", "Ошибка при открытии окна просмотра дерева разделов",
					e.getMessage());
		}

		//---- создаем окно и добавляем туда фрейм
		String winTitle = sectionName +" ["+rootSectionId+"]";

		// Создаём окно Stage.
		stage.setTitle(winTitle);
		//dialogStage.initModality(Modality.NONE);
		stage.initOwner(null);
		stage.getIcons().add(new Image("file:resources/images/icon_Sections_16.png"));

		if (d_win.length == 4) {
			stage.setWidth(d_win[0].doubleValue());
			stage.setHeight(d_win[1].doubleValue());
			stage.setX(d_win[2].doubleValue());
			stage.setY(d_win[3].doubleValue());
		}

		// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
		//stage.showAndWait();
		stage.show();
	}
	
	/**
	 * Открываем таб для просмотра документа
	 */
	public static void openDocumentView (Params params, TreeItem<SectionItem> tsi) {
		AnchorPane paneForEdit = null;
		SectionItem si = tsi.getValue();
		DocumentView_Controller controller = null;
		SectionList_Controller parentObj = (SectionList_Controller)params.getParentObj();

		//---- загружаем контроллер таба в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/DocumentView_Layout.fxml"));
			paneForEdit = loader.load();

			// Даём контроллеру доступ к фрейму с текущим документом
			controller = loader.getController();
			
			//controller.setParrentObj(parrentObj, conn, objContainer, mainApp.params.getMainStage(), true);
			controller.setParams(params, true);
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Просмотр документа", "Ошибка при открытии таба просмотра документа",
					e.getMessage());
		}

		//---- создаем основной таб для этого соединения и добавляем туда фрейм
		String tabTitle;
		if (si.getName().length() <= 20)
			tabTitle = si.getName() +" ["+si.getId()+"]";
		else
			tabTitle = si.getName().substring(0,20) +" ["+si.getId()+"]";
		
		Tab tab = params.getObjContainer().createContainer (
				tabTitle, 
				"file:resources/images/icon_document_16.png", 
				"Раздел : "+ si.getName() +"\n"+
						"Путь : "+ parentObj.treeViewCtrl.getSectionPath (tsi, 0), 
				paneForEdit, 
				controller);

		params.getTabPane_Cur().getTabs().add(tab);
		params.getTabPane_Cur().getSelectionModel().select(tab);
		
		controller.load(si.getId(), false);
	}
	
	/**
	 * Открываем отдельное окно для просмотра документа
	 */
	public void openDocumentViewInWin (Params params, TreeItem<SectionItem> tsi, Double... d_win) {
		AnchorPane paneForView = null;
		int winId = params.getWinList().getNextId();
		SectionItem si = tsi.getValue();
		String containerName = "documentView_"+ si.getId() +","+ winId;
		DocumentView_Controller controller = null;
		Stage stage = new Stage();
		WinItem winItem = null;

		//---- загружаем контроллер в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/DocumentView_Layout.fxml"));
			paneForView = loader.load();

			// Отображаем сцену, содержащую корневой макет.
			Scene scene = new Scene(paneForView);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			stage.setScene(scene);

			// Даём контроллеру доступ к фрейму с текущим документом
			// и добавляем в список открытых окон
			controller = loader.getController();
			
			winItem = new WinItem (containerName, "DocumentView_Controller", controller, stage, params.getWinList());
			params.getWinList().add(winItem);
			//controller.setParrentObj(parrentObj, conn, mainApp.winList, mainApp.getPrimaryStage(), containerId, true);
			//controller.setParrentObj(parrentObj, conn, mainApp.params.getWinList(), mainApp.params.getMainStage(), true);
			controller.setParams(params, true);
			
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Просмотр документа", "Ошибка при открытии окна просмотра документа",
					e.getMessage());
		}

		//---- создаем окно и добавляем туда фрейм
		String winTitle = si.getName() +" ["+si.getId()+"]";

		// Создаём окно Stage.
		stage.setTitle(winTitle);
		//dialogStage.initModality(Modality.NONE);
		stage.initOwner(null);
		stage.getIcons().add(new Image("file:resources/images/icon_document_16.png"));

		if (d_win.length == 4) {
			stage.setWidth(d_win[0].doubleValue());
			stage.setHeight(d_win[1].doubleValue());
			stage.setX(d_win[2].doubleValue());
			stage.setY(d_win[3].doubleValue());
		}

		// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
		//stage.showAndWait();
		stage.show();
		
		controller.load(si.getId(), false);
	}

	/**
	 * Открываем таб для редактирования инфо блока
	 */
	public static void openEditInfo (Params params, InfoHeaderItem ihi) {
		AnchorPane paneForEdit = null;
		InfoEdit_Controller controller = null;

		//---- загружаем контроллер таба в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/InfoEdit.fxml"));
			paneForEdit = loader.load();

			// Даём контроллеру доступ к фрейму с текущим документом
			controller = loader.getController();
			controller.setParams(params, ihi.getId());
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Редактирование блока", "Ошибка при открытии таба редактирования инфо блока",
					e.getMessage());
		}
		paneForEdit.requestFocus();

		//---- создаем основной таб для этого соединения и добавляем туда фрейм
		String tabTitle;
		if (ihi.getName().length() <= 20)
			tabTitle = ihi.getName() +" ["+ihi.getSectionId() +","+ ihi.getId()+"]";
		else
			tabTitle = ihi.getName().substring(0,20) +" ["+ihi.getSectionId() +","+ ihi.getId()+"]";
		
		Tab tab = params.getObjContainer().createContainer (
				tabTitle, 
				"file:resources/images/icon_edit_16.png", 
				"Раздел : "+ params.getConCur().db.sectionGetById(ihi.getSectionId()).getName() +"\n"+
					"Блок : "+ ihi.getName(), 
				paneForEdit, 
				controller);

		params.getTabPane_Cur().getTabs().add(tab);
		params.getTabPane_Cur().getSelectionModel().select(tab);
	}

	/**
	 * Открываем отдельное окно для редактирования инфо блока
	 */
	public void openEditInfoInWin (Params params, InfoHeaderItem ihi, Double... d_win) {
		AnchorPane paneForEdit = null;
		int winId = params.getWinList().getNextId();
		String containerName = "editInfo_"+ ihi.getSectionId() +","+ ihi.getId() +","+ winId;
		final InfoEdit_Controller controller;
		Stage stage = new Stage();
		WinItem winItem = null;

		//---- загружаем контроллер в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/InfoEdit.fxml"));
			paneForEdit = loader.load();

			// Отображаем сцену, содержащую корневой макет.
			Scene scene = new Scene(paneForEdit);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			stage.setScene(scene);

			// Даём контроллеру доступ к фрейму с текущим документом
			// и добавляем в список открытых окон
			controller = loader.getController();
			winItem = new WinItem (containerName, "InfoEdit_Controller", controller, stage, params.getWinList());
			params.getWinList().add(winItem);
			//controller.setParrentObj(mainApp, conn, ihi.getId(), containerId, mainApp.winList, stage);
			//controller.setParrentObj(params.getMain(), params.getConCur(), ihi.getId(), params.getWinList(), stage);
			params.setObjContainer(params.getWinList());
			params.setStageCur(stage);
			controller.setParams(params, ihi.getId());
			
			// set hot keys
			KeyCombination kc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
			//Mnemonic mn = new Mnemonic(controller.button_Save, kc);
			//scene.addMnemonic(mn);
			Runnable rn = ()-> controller.handleButtonSave();
			scene.getAccelerators().put(kc, rn);
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Редактирование блока", "Ошибка при открытии окна редактирования инфо блока",
					e.getMessage());
		}

		//---- создаем окно и добавляем туда фрейм
		String winTitle = ihi.getName() +" ["+ihi.getSectionId() +","+ ihi.getId()+"]";

		// Создаём окно Stage.
		stage.setTitle(winTitle);
		//dialogStage.initModality(Modality.NONE);
		stage.initOwner(null);
		stage.getIcons().add(new Image("file:resources/images/icon_edit_16.png"));

		if (d_win.length == 4) {
			stage.setWidth(d_win[0].doubleValue());
			stage.setHeight(d_win[1].doubleValue());
			stage.setX(d_win[2].doubleValue());
			stage.setY(d_win[3].doubleValue());
		}

		// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
		//stage.showAndWait();
		stage.show();
	}
}
