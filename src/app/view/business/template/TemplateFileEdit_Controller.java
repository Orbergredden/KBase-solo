package app.view.business.template;

import java.io.File;
import java.util.prefs.Preferences;

import app.exceptions.KBase_ReadTextFileUTFEx;
import app.lib.DateConv;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateStyleItem;
import app.model.business.template.TemplateThemeItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
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
	
	private TemplateThemeItem curThemeItem;

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
    	prefs = Preferences.userNodeForPackage(TemplateFileEdit_Controller.class);
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
    	curThemeItem = conn.db.templateThemeGetById(editedItem.getThemeId());
    	
    	OList_FileType = FXCollections.observableArrayList();
		OList_FileType.add("Текстовый");
		OList_FileType.add("Картинка");
		//OList_FileType.add("Бинарный");              // !!! пока не реализовываем
		comboBox_FileType.setItems(OList_FileType);
		
		if (actionType == ACTION_TYPE_ADD) {                 
    		switch (editedItem.getTypeItem()) {
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    			label_Title.setText("Обов'язковий файл");
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    			label_Title.setText("Необов'язковий файл");
    			break;
    		default :
    			label_Title.setText("Об'єкт невідомого типу");
    		}
    		
    		label_ThemeId.setText(curThemeItem.getName() +" ("+ Long.toString(curThemeItem.getId()) +")");
    		label_FileNameNew.setText("");
    		comboBox_FileType.setValue(OList_FileType.get(0));    // первый эдемент в списке
    		
    		label_DateCreated.setText("");
    		label_DateModified.setText("");
    		label_UserCreated.setText("");
    		label_UserModified.setText("");
    	} else if (actionType == ACTION_TYPE_EDIT) {
    		TemplateFileItem fi = conn.db.templateFileGetById(editedItem.getId());
    		
    		label_Title.setText("Файл " + editedItem.getName() +" ("+ editedItem.getId() +")");
    		label_ThemeId.setText(curThemeItem.getName() +" ("+ Long.toString(curThemeItem.getId()) +")");
    		textField_FileName.setText(fi.getFileName());
    		textField_Descr.setText(fi.getDescr());
    		label_FileNameNew.setText("");
    		
    		switch (fi.getFileType()) {
    		case TemplateFileItem.FILE_TYPE_TEXT : 
    			comboBox_FileType.setValue(OList_FileType.get(0));
    			textArea_FileContent.appendText(fi.getBody());
    			break;
    		case TemplateFileItem.FILE_TYPE_IMAGE : 
    			comboBox_FileType.setValue(OList_FileType.get(1));
    			textArea_FileContent.appendText("Ширина : "+ fi.bodyImage.getWidth() +"\n");
        		textArea_FileContent.appendText("Высота : "+ fi.bodyImage.getHeight() +"\n");
    			imageView_FileContent.setImage(fi.bodyImage);
    			break;
    		}
        		
       		label_DateCreated.setText(dateConv.dateTimeToStr(fi.getDateCreated()));
       		label_DateModified.setText(dateConv.dateTimeToStr(fi.getDateModified()));
       		label_UserCreated.setText(fi.getUserCreated());
       		label_UserModified.setText(fi.getUserModified()); 
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
    	
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
	 * Вызывается для открытия тестового файла
	 */
	@FXML
	private void handleButtonFileTextOpen() {
		FileChooser fileChooser = new FileChooser();
		String curDir;
		
		// Задаём фильтр расширений
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML файли (*.html)", "*.html"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JavaScript файли (*.js)", "*.js"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSS файли (*.css)", "*.css"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстові файли (*.txt)", "*.txt"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Усі файли (*.*)", "*.*"));
		
	    // set directory
	    curDir = prefs.get("SelectFileDir_Text", "");
	    if (! curDir.equals("")) 
	    	fileChooser.setInitialDirectory(new File(curDir));
	    
	    // Показываем диалог загрузки файла
	    File file = fileChooser.showOpenDialog(params.getStageCur());
		
	    if (file != null) {
	    	int fileType = FileUtil.getFileTypeByExt (file.toString());// return 0 - тип не определен ; 1 - текстовый ; 2 - картинка
	    	
	    	if (fileType == 0) {                   // 0 - тип не определен
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
	    	prefs.put("SelectFileDir_Text", curDir);
	    }
	}
	
	/**
	 * Вызывается для сохранения тестового файла на диске
	 */
	@FXML
	private void handleButtonFileTextSave() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранение текста в файл");
		  
	    //Set extension filter
	    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML файл (*.html)", "*.html");
	    fileChooser.getExtensionFilters().add(extFilter);
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JavaScript файли (*.js)", "*.js"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSS файлы (*.css)", "*.css"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
	    
	    fileChooser.setInitialFileName(textField_FileName.getText());
	    
	    // set current dir
	    String curDir = prefs.get("SelectFileDir_Text", "");
	    if (! curDir.equals("")) 
	    	fileChooser.setInitialDirectory(new File(curDir));
	    
	    //Show save file dialog
	    File file = fileChooser.showSaveDialog(params.getStageCur());
		
	    if (file != null) {
	    	FileUtil.writeTextFile(file.toString(), textArea_FileContent.getText());
	    	
	        // save dir name
	    	curDir = file.getAbsolutePath();
	    	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
	    	prefs.put("SelectFileDir_Text", curDir);
	    }
	}
	
	/**
	 * Вызывается для открытия файла с картинкой
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
	    curDir = prefs.get("SelectFileDir_Image", "");
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
	    	prefs.put("SelectFileDir_Image", curDir);
	    }
	}
	
	/**
	 * Вызывается для сохранения файла с картинкой на диске
	 */
	@FXML
	private void handleButtonFileImageSave() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранение картинки в файл");
		  
	    //Set extension filter
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG файлы (*.png)", "*.png"));
	    //fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения (*.png,*.gif,*.jpg,*.jpeg)", "*.png","*.gif","*.jpg","*.jpeg"));
		//fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
	    fileChooser.setInitialFileName(textField_FileName.getText());
	    
	    // set current dir
	    String curDir = prefs.get("SelectFileDir_Image", "");
	    if (! curDir.equals("")) 
	    	fileChooser.setInitialDirectory(new File(curDir));
	    
	    //Show save file dialog
	    File file = fileChooser.showSaveDialog(params.getStageCur());
		
	    if (file != null) {
	    	FileUtil.writeImageFile(file.toString(), imageView_FileContent.getImage());
			
	        // save dir name
	    	curDir = file.getAbsolutePath();
	    	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
	    	prefs.put("SelectFileDir_Image", curDir);
	    }
	}
	
    /**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	TemplateFileItem fi;
    	int fileType = 0;
    	
    	//-------- save stage position
    	prefs.putDouble("stageTemplateFileEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateFileEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateFileEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateFileEdit_PosY",  params.getStageCur().getY());
    	
		switch (comboBox_FileType.getSelectionModel().getSelectedItem()) {
			case "Текстовый" :
				fileType = TemplateSimpleItem.SUBTYPE_FILE_TEXT;
				break;
			case "Картинка" :
				fileType = TemplateSimpleItem.SUBTYPE_FILE_IMAGE;
				break;
		}
		
		//---------- check data in fields
		if ((textField_FileName.getText().equals("") || (textField_FileName.getText() == null))) {
			ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Имя файла", "Укажите Имя файла");
			return;
			//throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Имя файла", this);
		}
		if ((fileType == TemplateSimpleItem.SUBTYPE_FILE_TEXT) &&
			((textArea_FileContent.getText().equals("")) || (textArea_FileContent.getText() == null))) {
			ShowAppMsg.showAlert("WARNING", "Нет данных", "Нет текста файла", "Укажите текст файла.");
			return;
			//throw new KBase_Ex (1, "Ошибка при сохранении", "Нет текста файла", this);
		}
		if ((fileType == TemplateSimpleItem.SUBTYPE_FILE_IMAGE) && (imageView_FileContent.getImage() == null)) {
			ShowAppMsg.showAlert("WARNING", "Нет данных", "Нет картинки", "Выберите картинку для файла.");
			return;
			//throw new KBase_Ex (1, "Ошибка при сохранении", "Нет картинки", this);
		}
		
		//-------- Сохраняем
    	switch (actionType) {
    	case ACTION_TYPE_ADD :
    		
    		
    		
    		///////////// зробити перевірку для директорії (обов'язкових і необов'язкових файлів)
    		
    		
    		// проверка на уникальность имени файла в обов'язкових файлах вказаної схеми
    		if (conn.db.templateFileIsExistNameInTheme(curThemeItem.getId(), textField_FileName.getText())) {
    			ShowAppMsg.showAlert("WARNING", "Добавление файла.",
    					"Файл с таким именем уже существует в обов'язкових файлах вказаної схеми.",
    					"Добавление прервано.");
    			return;
			}
    		
    		
    		
    		
    		break;
    	case ACTION_TYPE_EDIT :
    	
    	
    		
    		
    		break;
    	}
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
