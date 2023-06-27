
package app.view.business;

import app.Main;
import app.exceptions.KBase_Ex;
import app.exceptions.KBase_HtmlCompileEx;
import app.lib.AppDataObj;
import app.lib.DateConv;
import app.lib.FileCache;
import app.lib.FromJS_InfoFile;
import app.lib.HtmlCompile;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.DocumentItem;
import app.model.business.InfoHeaderItem;
import app.model.business.SectionItem;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateThemeItem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * Контроллер фрейма показа документа. Показывает список инфо блоков и готовый документ.
 * @author Igor Makarevich
 */
public class DocumentView_Controller implements AppItem_Interface {
	
	private Params params;
	
    /**
     * Раздел отображаемого документа
     */
    private long sectionId;
    /**
     * Ковертор даты/времени
     */
    private DateConv dateConv;
    
    @FXML
    private AnchorPane anchorPane_DocView;
    @FXML
    private WebView webView_current;
    
    @FXML
    private Button button_ChangeInfoListOrientation;
    @FXML
    private Button button_refresh;
    @FXML
    private Button button_AddInfoBefore;
    @FXML
    private Button button_AddInfoLast;
    @FXML
    private Button button_EditInfo;
	@FXML
	private Button button_EditInfo2;
	@FXML
	private Button button_EditInfo3;
    @FXML
    private Button button_DeleteInfo;
    @FXML
    private Button button_Close;
    
    @FXML
    private SplitPane splitPane_info;
    
    @FXML
	public TreeTableView<InfoHeaderItem> treeTableView_InfoHeader;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_id;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_name;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_descr;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_position;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_type;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_style;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_infoId;
    @FXML
    private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_dateCreated;
    @FXML
    private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_dateModified;
    @FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_userCreated;
    @FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_userModified;
    
    @FXML
	private MenuItem menuitem_AddInfoBefore;
    @FXML
	private MenuItem menuitem_AddInfoLast;
    @FXML
	private MenuItem menuitem_EditInfo;
    @FXML
	private MenuItem menuitem_DeleteInfo;
    
    // можно ли закрывать данную сцену
    private boolean canClose;

    /**
     * Корень в дереве-контроле списка инфо блоков
     */
    TreeItem<InfoHeaderItem> rootInfoList;
    /**
     * 
     */
    WebEngine webEngine;
    /**
     * for debug
     */
    private boolean isDebug = false;
    String strDebugForShow;
    
    //
    private Preferences prefs = Preferences.userNodeForPackage(DocumentView_Controller.class);
    
    // флаг/id для листенера - нужно ли переходить в документе на текущий инфо блок
    private long isReloadedId = -1;
    
    
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public DocumentView_Controller () {
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
     * Вызывается родительским обьектом, который передает параметры.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, boolean canClose) {
    	this.params = params;
    	this.canClose = canClose;
    	
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	
    	//======== splitPane_info
    	if (! params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка утановки в конфигурации
    		String orientation = prefs.get("DocumentView_splitPane_info_orientation", "HORIZONTAL");
    		
    		if (orientation.equals("HORIZONTAL")) {
    			splitPane_info.setOrientation(Orientation.HORIZONTAL);
    		} else {
    			splitPane_info.setOrientation(Orientation.VERTICAL);
    		}
		}
    	
    	splitPane_info.setDividerPositions(prefs.getDouble("DocumentView_splitPane_info_position", 0.8));
    	
    	splitPane_info.getDividers().get(0).positionProperty().addListener(
                o -> {
                	prefs.putDouble("DocumentView_splitPane_info_position", splitPane_info.getDividerPositions()[0]);
                }
        );
    	
    	//======== ToolBar
    	button_ChangeInfoListOrientation.setTooltip(new Tooltip("Изменить ориентацию списка инфоблоков"));
    	button_ChangeInfoListOrientation.setGraphic(new ImageView(new Image("file:resources/images/icon_orientation_16.png")));
    	button_refresh.setTooltip(new Tooltip("Обновить документ"));
    	button_refresh.setGraphic(new ImageView(new Image("file:resources/images/icon_refresh_16.png")));
    	button_AddInfoBefore.setTooltip(new Tooltip("Добавить новый блок перед текущим..."));
    	button_AddInfoBefore.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_up_16.png")));
    	button_AddInfoLast.setTooltip(new Tooltip("Добавить новый блок в конец списка..."));
    	button_AddInfoLast.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_down_16.png")));
    	button_EditInfo.setTooltip(new Tooltip("Редактирование инфо блока...\n во внутреннем табе"));
    	button_EditInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_1_16.png")));
		button_EditInfo2.setTooltip(new Tooltip("Редактирование инфо блока...\n в главном табе"));
		button_EditInfo2.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_2_16.png")));
		button_EditInfo3.setTooltip(new Tooltip("Редактирование инфо блока...\n в отдельном окне"));
		button_EditInfo3.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_3_16.png")));
    	button_DeleteInfo.setTooltip(new Tooltip("Удаление инфо блока"));
    	button_DeleteInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	
    	button_Close.setTooltip(new Tooltip("Закрыть"));
    	button_Close.setGraphic(new ImageView(new Image("file:resources/images/icon_close_16.png")));
    	button_Close.setDisable(! canClose);
    	
    	//======== TreeTableView InfoHeader ============================================================================
    	//-------- columns 
    	// setCellValueFactory
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
    			);
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_descr.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_position.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getPosition()))
    			);
    	treeTableColumn_type.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(
    					Long.toString(param.getValue().getValue().getInfoTypeId()) +" - "+
    					params.getConCur().db.infoTypeGet(param.getValue().getValue().getInfoTypeId()).getName())
    			);
    	treeTableColumn_style.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(
    					(param.getValue().getValue().getTemplateStyleId() != 0) ?
    					Long.toString(param.getValue().getValue().getTemplateStyleId()) +" - "+
    					params.getConCur().db.templateStyleGet(param.getValue().getValue().getTemplateStyleId()).getName() :
    					""
    			));
    	treeTableColumn_infoId.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getInfoId()))
    			);
    	treeTableColumn_dateCreated.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateCreated()))
    			);
    	treeTableColumn_dateModified.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModified()))
    			);
    	treeTableColumn_userCreated.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
    			);
    	treeTableColumn_userModified.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
    			);
    	
    	// set/get Pref Width
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_id__PrefWidth", 50));
    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_name__PrefWidth", 250));
    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_descr.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_descr__PrefWidth", 250));
    	treeTableColumn_descr.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_descr__PrefWidth", t1.doubleValue());
            }
        });

    	treeTableColumn_position.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_position__PrefWidth", 75));
    	treeTableColumn_position.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_position__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_type.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_type__PrefWidth", 75));
    	treeTableColumn_type.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_type__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_style.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_style__PrefWidth", 75));
    	treeTableColumn_style.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_style__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_infoId.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_infoId__PrefWidth", 75));
    	treeTableColumn_infoId.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_infoId__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_dateCreated.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_dateCreated__PrefWidth", 150));
    	treeTableColumn_dateCreated.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_dateCreated__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_dateModified.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_dateModified__PrefWidth", 150));
    	treeTableColumn_dateModified.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_dateModified__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_userCreated.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_userCreated__PrefWidth", 150));
    	treeTableColumn_userCreated.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_userCreated__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_userModified.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_userModified__PrefWidth", 150));
    	treeTableColumn_userModified.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_userModified__PrefWidth", t1.doubleValue());
            }
        });
    	
    	//-------- init 
    	rootInfoList = new TreeItem<>(new InfoHeaderItem(0,0,0,0,0,0, "root item","invisible"));
    	rootInfoList.setExpanded(true);
    	treeTableView_InfoHeader.setShowRoot(false);
    	treeTableView_InfoHeader.setRoot(rootInfoList);
    	
    	// Слушаем изменения выбора, и при изменении отображаем информацию .
    	treeTableView_InfoHeader.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> onChangeSelectedInfo(newValue));
    	
    	// ContextMenu
    	menuitem_AddInfoBefore.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_up_16.png")));
    	menuitem_AddInfoLast.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_down_16.png")));
    	menuitem_EditInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_16.png")));
    	menuitem_DeleteInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	
    	//======== init webView_current
    	anchorPane_DocView.getStyleClass().add("WevView_Doc_Pain");
    	webEngine = webView_current.getEngine();
    	
    	webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
            	//-------- Show debug information
            	if (isDebug) {
            		webEngine.executeScript("document.body.innerHTML += '"+ strDebugForShow +"' ");
            	}
            	
            	//-------- go to current pos
            	if (isReloadedId != -1) {
            		webEngine.executeScript("scrollToElement(\""+ isReloadedId +"\")");
            		isReloadedId = -1;
            	}
            	
            	//-------- Executing Java From JavaScript
            	JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaInfoFile", new FromJS_InfoFile(params));
            }
        });
    	
    	loadEmptyPage();
    }
    
    /**
     * Загружаем пустой документ
     */
    public void loadEmptyPage () {
    	webView_current.getEngine().loadContent(
    			"<html>" +
                "<head>" +
                "</head>" +
                "<body>" +
                "Ничего не выбрано.<br><br>" +
                "</body>" +
                "</html>");
    }
    
    /**
     * Загружаем документ
     */
    public void load (long sectionId, boolean isReload) {
    	long countInfoHeaders = params.getConCur().db.infoCount(sectionId);
    	long selectedItemId = 0;

    	this.sectionId = sectionId;
    	
    	//======== если это релоад , то запоминаем текущий инфо блок , что бы потом на него вернуться
    	if (isReload && (countInfoHeaders > 0)) {
    		if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() != null) {
    			selectedItemId = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue().getId();
    		}
    	}
    	
    	//======== info block list init
    	rootInfoList.getChildren().clear();
    	rootInfoList = new TreeItem<>(new InfoHeaderItem(0,0,0,0,0,0, "root item","invisible"));
    	rootInfoList.setExpanded(true);
    	treeTableView_InfoHeader.setShowRoot(false);
    	treeTableView_InfoHeader.setRoot(rootInfoList);
    	
    	if (countInfoHeaders > 0) {
    		//======== 
        	showDocument (sectionId, 0, isReload);
    		
    		//======== info block list init -- load data
        	List<InfoHeaderItem> infoList = params.getConCur().db.infoListBySectionId (sectionId);
        	
        	for (InfoHeaderItem i : infoList) {
    			TreeItem<InfoHeaderItem> subItem = new TreeItem<>(i);
    			rootInfoList.getChildren().add(subItem);
    		}
        	
        	//======== restore active list item
        	if (isReload) {
        		for (TreeItem<InfoHeaderItem> t : rootInfoList.getChildren()) {
        			if (t.getValue().getId() == selectedItemId) {
        				treeTableView_InfoHeader.getSelectionModel().select(t);
        				isReloadedId = t.getValue().getId();
        			}
        		}
        	}
    	} else {
    		showTemplateFile (params.getConCur().db.sectionGetThemeId(sectionId, true), "no_info.html");
    	}
    }
    
    /**
     * Вызывается при выборе заголовка инфоблока из списка.
     * Перемещается к нужной части документа.
     * 
     * @param ti — информация по одном разделе
     */
    private void onChangeSelectedInfo(TreeItem<InfoHeaderItem> ti) {
    	if (ti != null) {
    		webEngine.executeScript("scrollToElement(\""+ ti.getValue().getId() +"\")");
    	}
    }
    
    /**
     * Показывает документ в WebView.
     * Перед показом возможна компиляция и кеширование.
     */
    private void showDocument (long sectionId, long infoHeaderId, boolean isReload) {
    	// Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы
    	SectionItem si = params.getConCur().db.sectionGetById(sectionId);
    	int cacheType = 
    			(si.getCacheType() == 0) ?
    			Integer.parseInt(params.getConCur().db.settingsGetValue("MAIN__CACHE_DOC__ENABLE")) :
    			si.getCacheType();
    	TemplateThemeItem tti = params.getConCur().db.templateThemeGetById(params.getConCur().db.sectionGetThemeId(sectionId, true));
    	boolean isDocumentCached = params.getConCur().db.documentFindBySectionId(sectionId);
    	DocumentItem di = (isDocumentCached) ? params.getConCur().db.documentGetBySectionId(sectionId) : null;
    	
    	FileCache fileCache = new FileCache(params.getConCur(), tti.getId());
    	
    	//======== Компилируем документ если это необходимо
    	if (isReload || (di == null) || (di.getDateModified().compareTo(si.getDateModifiedInfo()) < 0)) {
    		HtmlCompile hc = new HtmlCompile (params.getConfig(), params.getConCur(), sectionId);
    		
    		try {
    			hc.compile();
    		} catch (KBase_HtmlCompileEx ex) {
    			ShowAppMsg.showAlert("WARNING", "Compile document", ex.msg, "");
    		}
    		
    		di = params.getConCur().db.documentGetBySectionId(sectionId);
    	}
    	
    	//======== Проверка на диске наличия директорий, обязательных файлов и документа (если нужно). Создаем их если нужно.
    	if (cacheType != 2) {        // есть дисковое кеширование обязательных файлов
    		//-------- создаем недостающие директории и файлы
    		try {
				fileCache.createDirAndFiles();
			} catch (KBase_Ex e) {
				ShowAppMsg.showAlert("ERROR", "Дисковое кеширование файлов", e.msg, "Документ не показывается.");
				e.printStackTrace();
				return;
			}        
    		
    		//-------- Проверка на диске наличия документа
        	if (cacheType == 1) {        // есть дисковое кеширование документа
        		fileCache.createFileDoc(di);
        		
        		try {
        			fileCache.createFilesOfInfoBlocksForDoc(sectionId);
    			} catch (KBase_Ex e) {
    				ShowAppMsg.showAlert("ERROR", "Дисковое кеширование Изображений", e.msg, "Документ не показывается.");
    				e.printStackTrace();
    				return;
    			}
        	}
    	}

    	//========== show document
    	if (isDebug) {
    		strDebugForShow = 
    			"<hr>" +
    			"<h1>"+si.getName()+"</h1>" +
    			"cacheType = "+ cacheType +"<br>"+
    			"sectionId = "+ si.getId() +"<br>"+
    			"themeId   = "+ tti.getId() +"<br>"+
    			"Document cached = "+ isDocumentCached +"<br>"+
    			"Document directory = " + fileCache.getPath()  +"<br>"+
    			"Document name = " + fileCache.getDocFileName() +"<br>"
    			;
    	}
    	
    	if (cacheType == 1) {                             // files cache
    		if (infoHeaderId == 0) {
    			File fDoc = new File(fileCache.getPath() + fileCache.getDocFileName());
    			webEngine.load(fDoc.toURI().toString());
    			if (isReload) {
    				webEngine.reload();
    			}
    		} else {
    			File fDoc = new File(fileCache.getPath() + fileCache.getDocFileName());
    			webEngine.load (fDoc.toURI().toString() +"#"+ infoHeaderId);
    			if (isReload) {
    				webEngine.reload();
    			}
    		}
    	} else {                                          // DB cache only
    		//webEngine.loadContent("");
    		//webEngine.load("about:blank");
    		webEngine.loadContent(di.getText());
    		if (infoHeaderId != 0) {                            // not work - реализовано в onChangeSelectedInfo
    			///webEngine.load ("#"+ infoHeaderId);
    			///webEngine.executeScript("scrollToElement(\""+ infoHeaderId +"\")");
    			///webEngine.executeScript("document.querySelector(\"[href='#"+ infoHeaderId +"']\").click()");
    		}
    	}
    }
    
    /**
     * Показывает обязательный файл в WebView.
     */
    private void showTemplateFile (long themeId, String fileName) {
    	if (isDebug)   strDebugForShow = "";
    	
    	TemplateFileItem trf = params.getConCur().db.templateFileGet(themeId, fileName);
    	webEngine.loadContent(trf.getBody());
    }
    
    /**
     * Изменить ориентацию списка инфоблоков
     */
    @FXML
    void handleButtonChangeInfoListOrientation() {
    	
    	if (splitPane_info.getOrientation() == Orientation.HORIZONTAL) {
    		splitPane_info.setOrientation(Orientation.VERTICAL);
    		if (! params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка утановки в конфигурации
    			prefs.put("DocumentView_splitPane_info_orientation", "VERTICAL");
    		}
    	} else {
    		splitPane_info.setOrientation(Orientation.HORIZONTAL);
    		if (! params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка утановки в конфигурации
    			prefs.put("DocumentView_splitPane_info_orientation", "HORIZONTAL");
    		}
    	}
    }
    
    /**
     * Обновляем документ.
     * При это происходит принудительная компиляция.
     */
    @FXML
    void handleButtonRefresh() {
    	if (sectionId > 0) {
    		load (sectionId, true);
    	}
    }
    
    /**
     * Добавление нового блока перед текущим
     */
    @FXML
    private void handleButtonAddInfoBefore() {
    	if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке", 
    				"Выберите инфо блок, перед которым необходимо добавить новый.");
    		return;
    	}
    	
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/InfoAdd.fxml"));
    		AnchorPane page = (AnchorPane) loader.load();
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
    		dialogStage.setTitle("Добавление нового блока перед текущим");
    		dialogStage.initModality(Modality.WINDOW_MODAL);
    		dialogStage.initOwner(params.getMainStage());
    		Scene scene = new Scene(page);
    		scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
    		dialogStage.setScene(scene);
    		dialogStage.getIcons().add(new Image("file:resources/images/icon_insert_up_16.png"));
    		
    		Preferences prefs = Preferences.userNodeForPackage(DocumentView_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageInfoAdd_Width", 450));
			dialogStage.setHeight(prefs.getDouble("stageInfoAdd_Height", 300));
			dialogStage.setX(prefs.getDouble("stageInfoAdd_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageInfoAdd_PosY", 0));
    		
			// Даём контроллеру доступ к главному прилодению.
			InfoAdd_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setTabPane_Cur(((SectionList_Controller)params.getParentObj()).tabPane_info);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(
					params,
					sectionId,
					treeTableView_InfoHeader.getSelectionModel().getSelectedItem());
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Добавление нового блока в конец списка
     */
    @FXML
    private void handleButtonAddInfoLast() {
    	if (sectionId == 0)   return;
    	
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/InfoAdd.fxml"));
    		AnchorPane page = (AnchorPane) loader.load();
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
    		dialogStage.setTitle("Добавление нового блока в конец списка");
    		dialogStage.initModality(Modality.WINDOW_MODAL);
    		dialogStage.initOwner(params.getMainStage());
    		Scene scene = new Scene(page);
    		scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
    		dialogStage.setScene(scene);
    		dialogStage.getIcons().add(new Image("file:resources/images/icon_insert_down_16.png"));
    		
    		Preferences prefs = Preferences.userNodeForPackage(DocumentView_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageInfoAdd_Width", 450));
			dialogStage.setHeight(prefs.getDouble("stageInfoAdd_Height", 300));
			dialogStage.setX(prefs.getDouble("stageInfoAdd_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageInfoAdd_PosY", 0));
    		
			// Даём контроллеру доступ к главному прилодению.
			InfoAdd_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setTabPane_Cur(((SectionList_Controller)params.getParentObj()).tabPane_info);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(
					params,
					sectionId,
					null);
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Редактирование блока во внутреннем табе
     */
    @FXML
    private void handleButtonEditInfo() {
    	
    	if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке", 
    				"Выберите инфо блок, который необходимо редактировать.");
    		return;
    	}

    	InfoHeaderItem ihi = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue();
    	
    	//-------- открываем таб для редактирования
    	Params params = new Params(this.params);
		params.setTabPane_Cur(((SectionList_Controller)params.getParentObj()).tabPane_info);
		params.setObjContainer((SectionList_Controller)params.getParentObj());
		params.setParentObj(this);
		params.setStageCur(null);
		
		AppDataObj.openEditInfo (params, ihi);
    }

	/**
	 * Редактирование блока в главном табе
	 */
	@FXML
	private void handleButtonEditInfo2() {

		if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке",
					"Выберите инфо блок, который необходимо редактировать.");
			return;
		}

		InfoHeaderItem ihi = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue();

		//-------- открываем таб для редактирования
		Params params = new Params(this.params);
		params.setTabPane_Cur(params.getTabPane_Main());
		params.setObjContainer(params.getRootController());
		params.setParentObj(this);
		params.setStageCur(null);
		
		AppDataObj.openEditInfo (params, ihi);
	}

	/**
	 * Редактирование блока в отдельном окне
	 */
	@FXML
	private void handleButtonEditInfo3() {

		if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке",
					"Выберите инфо блок, который необходимо редактировать.");
			return;
		}

		InfoHeaderItem ihi = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue();

		//-------- открываем окно для редактирования
		(new AppDataObj()).openEditInfoInWin(params, ihi);
	}
    
    /**
     * Удаление текущего инфо блока
     */
    @FXML
    private void handleButtonDeleteInfo() {
    	TreeItem<InfoHeaderItem> ti = treeTableView_InfoHeader.getSelectionModel().getSelectedItem();

    	if (ti == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке", 
    				"Выберите инфо блок, который необходимо удалить.");
    		return;
    	}
    	
    	if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление инфо блока", 
                "Удаление блока '"+ ti.getValue().getName() +"' ("+ ti.getValue().getId() +")", "Удалить блок ?"))
    		return;
    	
    	params.getConCur().db.infoDelete(ti.getValue().getId());
    	
    	// delete from TreeTableView
    	TreeItem<InfoHeaderItem> parentItem = ti.getParent();
    	if (parentItem != null) {     // текущая иконка не корневая
            parentItem.getChildren().remove(ti);
    	}
    	
    	//
    	load (sectionId, true);
    	
    	// выводим сообщение в статус бар
        params.setMsgToStatusBar("Инфо блок '" + ti.getValue().getName() + "' удален.");
    }
    
    /**
     * Закрываем таб/окно просмотра документа
     */
    @FXML
    private void handleButtonClose() {
    	params.getObjContainer().closeContainer(getOID());
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
		return AppItem_Interface.ELEMENT_DOCUMENT_VIEW;
	}

	/**
	 * id обекта элемента приложения - id заголовка инфо блока
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public long getAppItemId() {
		return sectionId;
	}

	/**
	 * id соединения с базой данных
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getDbConnId() {
		return params.getConCur().Id;
	}

	/**
	 * контроллер элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Object getController() {
		return this;
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		String splitOrientation = 
				(splitPane_info.getOrientation() == Orientation.HORIZONTAL) ? "HORIZONTAL" : "VERTICAL";
		
		stateList.add(
				"splitPane_info_Orientation",
				splitOrientation,
				null);
		stateList.add(
				"splitPane_info_Position",
				String.valueOf(splitPane_info.getDividerPositions()[0]),
				null);
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		
		for (StateItem si : stateList.list) {
			switch (si.getName()) {
			case "splitPane_info_Orientation" :
				if (si.getParams().equals("HORIZONTAL")) {
					splitPane_info.setOrientation(Orientation.HORIZONTAL);
				} else {
					splitPane_info.setOrientation(Orientation.VERTICAL);
				}
				break;
			case "splitPane_info_Position" :
				splitPane_info.setDividerPositions(Double.parseDouble(si.getParams()));  
				break;
			}
		}
	}
}
