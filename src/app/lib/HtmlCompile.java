
package app.lib;

import app.exceptions.KBase_HtmlCompileEx;
import app.model.ConfigMainList;
import app.model.DBConCur_Parameters;
import app.model.business.DocumentItem;
import app.model.business.InfoHeaderItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.Info_TextItem;
import app.model.business.SectionItem;
import app.model.business.template.TemplateThemeItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateFileItem;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс содержит инструментарий по компиляции документов в HTML из инфо блоков с использованием шаблонов.
 * @author IMakarevich
 */
public class HtmlCompile {
	// tag delimiters
	static final String TAG_DELIMITER_LEFT  = "{[";    // 
	static final String TAG_DELIMITER_RIGHT = "]}";    //
	
	/**
	 * Настройки программы
	 */
	ConfigMainList config;
	/**
	 * Конектор к БД
	 */
	DBConCur_Parameters conn;
	/**
	 * Id раздела документа
	 */
	long sectionId;
	
	/**
	 * Ассоциативный массив тегов Имя-Значение
	 */
	Map< String, String > mapTags = new HashMap< String, String >();
	/**
	 * Скомилированный html
	 */
	private String resultHtml;
	
	//
	SectionItem si;
	// Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы
	int cacheType;
	//
	TemplateThemeItem tti;
	//
	TemplateFileItem templateMain;
	//
	boolean isDocumentCached;
	//
	DocumentItem di;
	
	/**
	 * Constructor 
	 * @param conn 
	 * @param sectionId
	 */
	public HtmlCompile (ConfigMainList config, DBConCur_Parameters conn, long sectionId) {
		this.config = config;
		this.conn = conn;
		this.sectionId = sectionId;
		
		si = conn.db.sectionGetById(sectionId);
		cacheType = 
				(si.getCacheType() == 0) ?
				Integer.parseInt(conn.db.settingsGetValue("MAIN__CACHE_DOC__ENABLE")) :
				si.getCacheType();
		tti = conn.db.templateThemeGetById(conn.db.sectionGetThemeId(sectionId, true));
		//templateMain = conn.db.templateFileGetByType(tti.getId(), TemplateRequiredFileItem.FILETYPE_MAIN_FILE);
		templateMain = conn.db.templateFileGet(tti.getId(), "kbase_main.html");
		//TODO
		
		isDocumentCached = conn.db.documentFindBySectionId(sectionId);
		di = (isDocumentCached) ? conn.db.documentGetBySectionId(sectionId) : null;
		
		resultHtml = new String("");
	}
	
	/**
	 * geter. Скомилированный html
	 */
	public String getResultHtml () {  return resultHtml;  }
	
	/**
	 * Компилируем.
	 */
	public void compile () throws KBase_HtmlCompileEx {
		String tmplMainBody = templateMain.getBody();
		int posBegin;
		int posEnd;
		String tagName;
		
		posBegin = tmplMainBody.indexOf(TAG_DELIMITER_LEFT);
		while (posBegin >= 0) {
			posEnd = tmplMainBody.indexOf(TAG_DELIMITER_RIGHT);
			// пишем текст слева от тега
			resultHtml += tmplMainBody.substring(0, posBegin);
			// получаем название тега
			tagName = tmplMainBody.substring(posBegin+2, posEnd);
			// удаляем из буфера шаблона тег
			tmplMainBody = tmplMainBody.substring(posEnd+2);
			
			//---- получаем значение тега-переменной
			if (! mapTags.containsKey(tagName))  setTagValue(tagName);
			resultHtml += mapTags.get(tagName);
			
			// for next iteration
			posBegin = tmplMainBody.indexOf(TAG_DELIMITER_LEFT);       // for next iteration
		}
		// дописываем хвост шаблона
		resultHtml += tmplMainBody;
		
		//System.out.println(resultHtml);

		saveToDB();
	}
	
	/**
	 * Вычисляем значение тега и записываем его в список
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue (String tagName) throws KBase_HtmlCompileEx {
		String retVal = "";
	
		switch (tagName) {
		case "Doc_Path" :                 // путь (URL) к документу
			retVal = getDocPath();
			break;
			
		case "Title" :                    // заголовок документа (вверху, в начале документа)
			retVal = si.getName() +"(id = "+ si.getId() +")";
			break;
			
		case "Title_Path" :               // путь документа в базе данных
			retVal = conn.db.sectionGetPathName(sectionId, " > ");
			break;
			
		case "Insert_Info_Blocks" :       // место вставки инфо блоков
			List<InfoHeaderItem> infoList = conn.db.infoListBySectionId (sectionId);
        	
        	for (InfoHeaderItem i : infoList) {
        		retVal += compileInfoBlock(i);
    		}
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, "Неверное имя тега-переменной : "+ tagName, this);
		}
		mapTags.put(tagName, retVal);
		
		return retVal;
	}
	
	/**
	 * возвращает путь к каталогу кеша на диске с документами
	 */
	private String getDocPath () throws KBase_HtmlCompileEx {
		String retVal;
		
		Path path = Paths.get(
				config.getItemValue("directories", "PathDirCache") +
    			((conn.param.getConnId() > 0) ? conn.param.getConnId() : ("m"+(-1 * conn.param.getConnId()))) +"/"+
    			tti.getId());
        Path absolutePath = path.toAbsolutePath();

        try {
			retVal = absolutePath.toUri().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new KBase_HtmlCompileEx (sectionId, "Ошибка формирования пути (URL) к документу при компиляции.", this);
		}
	
		return retVal;
	}
	
	/**
	 * Сохраняем скомпилированный документ в БД
	 */
	private void saveToDB() {
		DocumentItem dt;
		
		if (isDocumentCached) {         // update text
			dt = new DocumentItem (
					di.getId(),
					di.getSectionId(),
					resultHtml,
					cacheType,
					di.getDateCreated(),
					null,
					di.getUserCreated(),
					"");
			conn.db.documentUpdate(dt);
		} else {                        // insert document
			dt = new DocumentItem (
					conn.db.documentNextId(),
					sectionId,
					resultHtml,
					cacheType);
			conn.db.documentAdd(dt);
		}
	}
	
	/**
	 * Компиляция инфо блоков
	 */
	String compileInfoBlock (InfoHeaderItem infoHeader) throws KBase_HtmlCompileEx {
		TemplateItem template = conn.db.templateGet(tti.getId(), infoHeader);
		String tmplInfoBody = template.getBody();
		int posBegin;
		int posEnd;
		String tagName;
		Map< String, String > mapTagsInfoBlock = new HashMap< String, String >();
		String retVal = "";
	
		posBegin = tmplInfoBody.indexOf(TAG_DELIMITER_LEFT);
		while (posBegin >= 0) {
			posEnd = tmplInfoBody.indexOf(TAG_DELIMITER_RIGHT);
			// пишем текст слева от тега
			retVal += tmplInfoBody.substring(0, posBegin);
			// получаем название тега
			tagName = tmplInfoBody.substring(posBegin+2, posEnd);
			// удаляем из буфера шаблона тег
			tmplInfoBody = tmplInfoBody.substring(posEnd+2);
			
			//---- получаем значение тега-переменной
			if (! mapTagsInfoBlock.containsKey(tagName)) {
				switch ((int)infoHeader.getInfoTypeId()) {
	    		case 1 :                            // Простой текст
	    			setTagValue_Text(mapTagsInfoBlock, tagName, infoHeader);
	    			break;
	    		case 2 :                            // Изображение
	    			setTagValue_Image(mapTagsInfoBlock, tagName, infoHeader);
	    			break;
	    		case 3 :                            // Файл	
	    			setTagValue_File(mapTagsInfoBlock, tagName, infoHeader);
	    			break;
	    		default:
	    			throw new KBase_HtmlCompileEx (sectionId, "Не обработан инфоблок типа id="+ infoHeader.getInfoTypeId(), this);
	    		}
			}
			retVal += mapTagsInfoBlock.get(tagName);
			
			// for next iteration
			posBegin = tmplInfoBody.indexOf(TAG_DELIMITER_LEFT);       // for next iteration
		}
		// дописываем хвост шаблона
		retVal += tmplInfoBody;
		
		return retVal;
	}
	
	/**
	 * Вычисляем значение тега для инфо блока "Простой текст" и записываем его в список данного инфо блока
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue_Text (Map< String, String > mapTags_Text, String tagName, InfoHeaderItem infoHeader) 
			throws KBase_HtmlCompileEx {
		Info_TextItem infoText = conn.db.info_TextGet(infoHeader.getInfoId());
		String retVal = "";
	
		switch (tagName) {
		case "InfoBlockId" :
			retVal = Long.toString(infoHeader.getId());
			break;
		case "isShowDescr" :
			retVal = Integer.toString(infoText.getIsShowTitle());
			break;
		case "InfoDescr" :
			String descr = infoText.getTitle();
			retVal = descr.replaceAll("\"", "&quot;");
			//retVal = descr.replaceAll("1", "z");
			break;
		case "kbase_Info_Text" :
			retVal = infoText.getText();
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, 
					"Неверное имя тега-переменной для инфо блока \"Простой текст\" : "+ tagName, this);
		}
		mapTags_Text.put(tagName, retVal);
		
		return retVal;
	}
	
	/**
	 * Вычисляем значение тега для инфо блока "Изображение" и записываем его в список данного инфо блока
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue_Image (Map< String, String > mapTags_Image, String tagName, InfoHeaderItem infoHeader) 
			throws KBase_HtmlCompileEx {
		Info_ImageItem infoImage = conn.db.info_ImageGet(infoHeader.getInfoId());
		String retVal = "";
	
		switch (tagName) {
		case "InfoBlockId" :
			retVal = Long.toString(infoHeader.getId());
			break;
		case "InfoImageTitle" :
			String title = infoImage.getTitle();
			retVal = title.replaceAll("\"", "&quot;");
			break;
		case "InfoImagePath" :
			//retVal = getDocPath() + "files_" + Long.toString(sectionId) + 
			//	     "/image_" + Long.toString(infoHeader.getId()) + ".png";
			retVal = "files_" + Long.toString(sectionId) + 
				     "/image_" + Long.toString(infoHeader.getId()) + ".png";
			break;
		case "InfoImageWidth" :
			retVal = Integer.toString(infoImage.getWidth());
			break;
		case "InfoImageHeight" :
			retVal = Integer.toString(infoImage.getHeight());
			break;
		case "InfoImageDescr" :
			String descr = infoImage.getDescr();
			retVal = descr.replaceAll("\"", "&quot;");
			break;
		case "InfoImageText" :
			retVal = infoImage.getText();
			break;
		case "isShowTitle" :
			retVal = Integer.toString(infoImage.getIsShowTitle());
			break;
		case "isShowDescr" :
			retVal = Integer.toString(infoImage.getIsShowDescr());
			break;
		case "isShowText" :
			retVal = Integer.toString(infoImage.getIsShowText());
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, 
					"Неверное имя тега-переменной для инфо блока \"Изображение\" : "+ tagName, this);
		}
		mapTags_Image.put(tagName, retVal);
		
		return retVal;
	}
	
	/**
	 * Вычисляем значение тега для инфо блока "Файл" и записываем его в список данного инфо блока
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue_File (Map< String, String > mapTags_File, String tagName, InfoHeaderItem infoHeader) 
			throws KBase_HtmlCompileEx {
		Info_FileItem infoFile = conn.db.info_FileGet(infoHeader.getInfoId());
		String retVal = "";
	
		switch (tagName) {
		case "InfoBlockId" :
			retVal = Long.toString(infoHeader.getId());
			break;
		case "InfoFileTitle" :
			String title = infoFile.getTitle();
			retVal = title.replaceAll("\"", "&quot;");
			break;
		case "InfoFilePath" :
			retVal = "files_" + Long.toString(sectionId) + 
				     "/file_" + Long.toString(infoHeader.getId()) + "." + FileUtil.getFileExt(infoFile.getName());
			break;
		case "InfoFileName" :
			retVal = infoFile.getName();
			break;
		case "InfoIconPath" :
			retVal = "files_" + Long.toString(sectionId) +
			         "/file_" + Long.toString(infoHeader.getId()) + "_image.png";
			break;
		case "InfoFileDescr" :
			String descr = infoFile.getDescr();
			retVal = descr.replaceAll("\"", "&quot;");
			break;
		case "InfoFileText" :
			retVal = infoFile.getText();
			break;
		case "isShowTitle" :
			retVal = Integer.toString(infoFile.getIsShowTitle());
			break;
		case "isShowDescr" :
			retVal = Integer.toString(infoFile.getIsShowDescr());
			break;
		case "isShowText" :
			retVal = Integer.toString(infoFile.getIsShowText());
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, 
					"Неверное имя тега-переменной для инфо блока \"Файл\" : "+ tagName, this);
		}
		mapTags_File.put(tagName, retVal);
		
		return retVal;
	}
}
