package app.view.business.template;

import app.lib.ShowAppMsg;
import app.model.Params;
import app.model.business.SimpleItem;
import app.model.business.template.TemplateSimpleItem;

import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер окна выбора типа элемента (тема, файл, стиль, шаблон)
 * 
 * @author Igor Makarevich
 */
public class TemplateTypeSelect {
	private Params params;
    
    /**
     * Текущий элемент в дереве шаблонов
     */
    private TemplateSimpleItem currentItem;
	
	//
    @FXML
	private TreeTableView<SimpleItem> treeTableView_types;
	@FXML
	private TreeTableColumn<SimpleItem, String> treeTableColumn_name;
    @FXML
	private TreeTableColumn<SimpleItem, String> treeTableColumn_description;
    @FXML
	private TreeTableColumn<SimpleItem, String> treeTableColumn_id;
	
    @FXML
	private Button button_Select;
	@FXML
	private Button button_Cancel;
	
	// возвращаемые параметры
	public boolean isSelected = false;
	public int returnTypeItem = 0;
	/**
     * Элемент в дереве шаблонов, который будет родительским для добавляемого элемента
     */
    private TemplateSimpleItem targetItem;

	//
	private Preferences prefs;

	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateTypeSelect () {         }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {         }
    
    /**
     * Вызывается родительским обьектом, который даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, TemplateSimpleItem currentItem) {
        this.params      = params;
        this.currentItem = currentItem;
        
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
    	prefs = Preferences.userNodeForPackage(TemplateTypeSelect.class);

    	//======== TreeTableView ============================================================================
    	//-------- columns 
    	// setCellValueFactory
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<SimpleItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_description.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<SimpleItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<SimpleItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
    			);
    	
    	// set/get Pref Width
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("TemplateTypeSelect__treeTableColumn_id__PrefWidth", 75));

    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateTypeSelect__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("TemplateTypeSelect__treeTableColumn_name__PrefWidth", 300));

    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateTypeSelect__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_description.setPrefWidth(prefs.getDouble("TemplateTypeSelect__treeTableColumn_description__PrefWidth", 130));

    	treeTableColumn_description.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateTypeSelect__treeTableColumn_description__PrefWidth", t1.doubleValue());
            }
        });
    	
    	//-------- init 
    	final TreeItem<SimpleItem> root;
   		root = new TreeItem<>(new SimpleItem(0, "элементы", "это корень", new Date(), new Date(), "",""));
    	root.setExpanded(true);
    	treeTableView_types.setShowRoot(false);
    	treeTableView_types.setRoot(root);
    	addTreeItems(root);
    	
    	//======== Обработчик событий строки таблицы
    	treeTableView_types.setRowFactory( tv -> {
    	    TreeTableRow<SimpleItem> row = new TreeTableRow<>();
    	    
    	    //-------- коннект при двойном клике
    	    row.setOnMouseClicked(event -> {
    	        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
    	        	handleButtonSelect();
    	        }
    	    });
    	    return row ;
    	});

    	//======== buttons
    	button_Select.setGraphic(new ImageView(new Image("file:resources/images/icon_select_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * Инициализация TreeTableView.
     * @param
     */
    private void addTreeItems(TreeItem<SimpleItem> tiRoot) {
    	SimpleItem f = (SimpleItem) tiRoot.getValue();
    	
    	if (f == null) return;

    	// Theme
    	TreeItem<SimpleItem> siTheme = new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_THEME, "Тема",""));
    	tiRoot.getChildren().add(siTheme);
    	treeTableView_types.getSelectionModel().select(siTheme); // делаем активной, что бы что то было выбрано в начале

    	// File
    	if ((currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_SECTION_FILE) ||
    		(currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_FILE)) {
    		TreeItem<SimpleItem> siFileDir = 
    				new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_SECTION_FILE, "Директория файлов",""));
        	tiRoot.getChildren().add(siFileDir);
    		
        	TreeItem<SimpleItem> siFile = 
    				new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_FILE, "Файл",""));
        	tiRoot.getChildren().add(siFile);
    	}
    	if ((currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_SECTION_FILE_OPTIONAL) ||
    		(currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL)) {
    		TreeItem<SimpleItem> siFileDir = 
    				new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_SECTION_FILE_OPTIONAL, 
    						"Директория необязательных файлов",""));
        	tiRoot.getChildren().add(siFileDir);
    		
        	TreeItem<SimpleItem> siFile = 
    				new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL, "Файл необязательный",""));
        	tiRoot.getChildren().add(siFile);
    	}
    	
    	// Style
    	if ((currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_SECTION_STYLE) ||
        	(currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_STYLE)) {
        	TreeItem<SimpleItem> siStyleDir = 
        			new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_SECTION_STYLE, "Директория стилей",""));
           	tiRoot.getChildren().add(siStyleDir);
        		
           	TreeItem<SimpleItem> siStyle = 
        			new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_STYLE, "Стиль",""));
           	tiRoot.getChildren().add(siStyle);
        }
    	
    	// Template
    	if ((currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_SECTION_TEMPLATE) ||
           	(currentItem.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_TEMPLATE)) {
           	TreeItem<SimpleItem> siTemplateDir = 
           			new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_SECTION_TEMPLATE, "Директория шаблонов",""));
        	tiRoot.getChildren().add(siTemplateDir);
            		
           	TreeItem<SimpleItem> siTemplate = 
           			new TreeItem<>(new SimpleItem(TemplateSimpleItem.TYPE_ITEM_TEMPLATE, "Шаблон",""));
           	tiRoot.getChildren().add(siTemplate);
        }
    }
    
    /**
	 * Сохраняем текущее состояние фрейма и контролов
	 */
	private void saveState() {
		//-------- size and position
		prefs.putDouble("stageTemplateTypeSelect_Width", params.getStageCur().getWidth());
		prefs.putDouble("stageTemplateTypeSelect_Height",params.getStageCur().getHeight());
		prefs.putDouble("stageTemplateTypeSelect_PosX",  params.getStageCur().getX());
		prefs.putDouble("stageTemplateTypeSelect_PosY",  params.getStageCur().getY());
	}
    
    /**
     * Вызывается при нажатии на кнопке "Выбрать"
     */
    @FXML
    private void handleButtonSelect() {
/*    	if (treeTableView_themes.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Выбор темы", "Тема не выбрана.", "Выберите тему.");
    		return;
    	}
    	
    	TemplateThemeItem si = treeTableView_themes.getSelectionModel().getSelectedItem().getValue();

		saveState();      // save stage position and other

    	isSelected = true;
    	themeIdRet = si.getId();
*/    	
        //-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
    //TODO Ok
    
    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonCancel() {

		saveState();      // save stage position and other
    	
        // get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
