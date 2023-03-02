package app.view.business.template;

import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.InfoTypeItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateStyleItem;
import app.model.business.template.TemplateThemeItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер форма для додавання/редагування стилю шаблонів
 * 
 * @author Igor Makarevich
 */
public class TemplateStyleEdit_Controller {
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
	
	private TemplateStyleItem curStyleItem;
	private TemplateThemeItem curThemeItem;
	private InfoTypeItem curInfoTypeItem; 
	private TemplateItem curTemplateItem;
	
	// controls
	@FXML
	private Label label_Theme;
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
	private TextField textField_StyleTag;
	@FXML
	private TextField textField_TemplateId;
	@FXML
	private Label label_TemplateName;
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
	private Button button_Ok;
	@FXML
	private Button button_Cancel;
	
	//
	private Preferences prefs;
	private DateConv dateConv;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateStyleEdit_Controller () {
    	prefs = Preferences.userNodeForPackage(TemplateStyleEdit_Controller.class);
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
    	curStyleItem = conn.db.templateStyleGet(editedItem.getId());
    	curThemeItem = conn.db.templateThemeGetById(editedItem.getThemeId());
    	
    	label_Theme.setText(curThemeItem.getName() +" ("+ Long.toString(curThemeItem.getId()) +")");
    	imageView_StyleDef.setImage(new Image("file:resources/images/icon_default_item_16.png"));
    	
    	//======== get info block type
    	if (editedItem.getFlag() > 0) {
    		curInfoTypeItem = conn.db.infoTypeGet(editedItem.getFlag());
    		label_StyleInfoTypeId.setText(curInfoTypeItem.getName() +" ("+ Long.toString(curInfoTypeItem.getId()) +")");
    	} else {
    		label_StyleInfoTypeId.setText("Зарезервований стиль");
    	}

    	TemplateStyleItem defStyleItem = conn.db.templateStyleGetDefault(editedItem.getThemeId(), editedItem.getFlag());
    	label_StyleDefCur.setText(defStyleItem.getName() +" ("+ Long.toString(defStyleItem.getId()) +")");
    	
    	//========
    	if (actionType == ACTION_TYPE_ADD) { 
    		label_StyleId.setText("");
    		label_StyleParentId.setText(editedItem.getName() +" ("+ Long.toString(editedItem.getId()) +")");
    		label_TemplateName.setText("");
    		label_StyleDateCreated.setText("");
    		label_StyleDateModified.setText("");
    		label_StyleUserCreated.setText("");
    		label_StyleUserModified.setText("");
    		label_StyleDefDate.setText("");
    	} else if (actionType == ACTION_TYPE_EDIT) {
    		label_StyleId.setText(Long.toString(editedItem.getId()));
    		label_StyleParentId.setText(
    				editedItem_ti.getParent().getValue().getName() +
    				" ("+ Long.toString(editedItem_ti.getParent().getValue().getId()) +")");
    		textField_StyleName.setText(editedItem.getName());
        	textField_StyleDescr.setText(editedItem.getDescr());
        	textField_StyleTag.setText(curStyleItem.getTag());

        	// template
        	if (conn.db.templateIsLinkPresent(curThemeItem.getId(), editedItem.getId())) {
        		curTemplateItem = conn.db.templateGet(curThemeItem.getId(), editedItem.getId());
        		textField_TemplateId.setText(Long.toString(curTemplateItem.getId()));
        		label_TemplateName.setText(curTemplateItem.getName());
        	} else {
        		label_TemplateName.setText("ШАБЛОН ПО ВКАЗАНОМУ id НЕ ЗНАЙДЕНО !!!");
        	}
        	
        	label_StyleDateCreated.setText(dateConv.dateTimeToStr(editedItem.getDateCreated()));
        	label_StyleDateModified.setText(dateConv.dateTimeToStr(editedItem.getDateModified()));
        	label_StyleUserCreated.setText(editedItem.getUserCreated());
        	label_StyleUserModified.setText(editedItem.getUserModified());
    		
        	//--------default
        	if (conn.db.templateStyleIsDefault (curThemeItem.getId(), editedItem.getId())) {
        		checkBox_StyleDef.setSelected(true);
        		label_StyleDefDate.setText(dateConv.dateTimeToStr(
        				conn.db.templateStyleGetDefaultDateModified(curThemeItem.getId(), editedItem.getId())));
        	} else {
        		label_StyleDefDate.setText("");
        	}
    	}
    	
    	//======== buttons
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
	
    /**
     * Визивається при закінченні вводу id шаблона
     */
    @FXML
    private void handleTextFieldTemplateId() {
    	try {
    		long templateId = Long.parseLong(textField_TemplateId.getText());
        	
        	if (conn.db.templateIsPresent(templateId)) {
        		curTemplateItem = conn.db.templateGet(templateId);
        		label_TemplateName.setText(curTemplateItem.getName());
        	} else {
        		label_TemplateName.setText("ШАБЛОН ПО ВКАЗАНОМУ id НЕ ЗНАЙДЕНО !!!");
        	}
    	} catch (NumberFormatException e) {
    		label_TemplateName.setText("ВКАЗАН КОРЯВИЙ id ШАБЛОНА !!!");
    	}
    }
    
    /**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	long templateId = 0;
    	
    	//---------- check data in fields
    	if ((textField_StyleName.getText().equals("") || (textField_StyleName.getText() == null))) {
			ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название стиля", "Укажите Название стиля");
			return;
		}
    	
    	//System.out.println(textField_StyleTag.getText());
    	if ((textField_StyleTag.getText() == null) || textField_StyleTag.getText().trim().equals("")) {
    		if (editedItem.getSubtypeItem() >= 10) {
    			ShowAppMsg.showAlert("WARNING", "Нет данных", "Не вказан Тег для зарезервованого стилю", "Вкажіть Тег");
    			return;
    		}
		} else {
			TemplateStyleItem styleByTag = conn.db.templateStyleGetByTag(textField_StyleTag.getText().trim());
			if (actionType == ACTION_TYPE_ADD) {
				if (styleByTag != null) {
					ShowAppMsg.showAlert("WARNING", "Перевірка Тега", "Такий Тег вже існує", "Вкажіть інший Тег");
	    			return;
				}
			} else {
				if ((styleByTag != null) && (editedItem.getId() != styleByTag.getId())) {
					ShowAppMsg.showAlert("WARNING", "Перевірка Тега", "Такий Тег вже існує у іншого стиля", "Вкажіть інший Тег");
	    			return;
				}
			}
		}
    	
    	if ((! textField_TemplateId.getText().equals("")) && (textField_TemplateId.getText() != null)) {
    		try {
    			templateId = Long.parseLong(textField_TemplateId.getText());
    			if (! conn.db.templateIsPresent(templateId)) {
    				ShowAppMsg.showAlert("WARNING", "Некоректні дані", "ШАБЛОН ПО ВКАЗАНОМУ id НЕ ЗНАЙДЕНО.", "Вкажіть коректне значення.");
    				return;
            	}
    		} catch (NumberFormatException e) {
    			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректний id шаблона.", "Вкажіть коректне значення.");
    			return;
    		}
    	}
    	
    	//-------- Сохраняем
    	switch (actionType) {
    	case ACTION_TYPE_ADD :
    		long newId = conn.db.templateStyleNextId();
    		
    		//-------- add to DB
    		TemplateStyleItem si = new TemplateStyleItem (
    				newId,
    				editedItem.getId(),    // parentId 
    				((curInfoTypeItem != null) && (curInfoTypeItem.getId() > 0)) ? 0 : 10,   // type
    				(curInfoTypeItem != null) ? curInfoTypeItem.getId() : 0,  // infoTypeId
    				textField_StyleName.getText(),
					textField_StyleDescr.getText(),
					textField_StyleTag.getText()
			);
			conn.db.templateStyleAdd(si);  // стиль добавляем в БД
			si = conn.db.templateStyleGet(newId);                   // get full info by Id

    		// додати зв'язку стиль-шаблон
			if (textField_TemplateId.getText().trim().length() > 0) {
				templateId = Long.parseLong(textField_TemplateId.getText().trim());
				TemplateItem ti = conn.db.templateGet (templateId);
				
				if ((ti.getType() == 0) || (ti.getType() == 10)) {
					conn.db.templateSetLink (curThemeItem.getId(), si.getId(), templateId);
				} else {
					ShowAppMsg.showAlert("WARNING", "Увага", "Помилка при зв'язуванні стиля і шаблона", 
				             "Не можливо зв'язувати директорію шаблона, тільки шаблон потрібно вказувати.");
					return;
				}
			}

			
    		// обробити ситуацію дефолтного стилю
			
			
    		
    		break;
    	case ACTION_TYPE_EDIT :
    		
    		
    		
    		
    		
    		
    		break;
    	}
    	//TODO handleButtonOk
    	
    	//-------- save stage position
    	prefs.putDouble("stageTemplateStyleEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateStyleEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateStyleEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateStyleEdit_PosY",  params.getStageCur().getY());
    	
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
    	prefs.putDouble("stageTemplateStyleEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateStyleEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateStyleEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateStyleEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
