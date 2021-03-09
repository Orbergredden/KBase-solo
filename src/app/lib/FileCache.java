
package app.lib;

import app.exceptions.KBase_Ex;
import app.model.ConfigMainList;
import app.model.DBConCur_Parameters;
import app.model.business.DocumentItem;
import app.model.business.IconItem;
import app.model.business.InfoHeaderItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.templates.TemplateRequiredFileItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

/**
 * Класс отвечает за кеширование документов и обязательных файлов на диске
 * @author Igor Makarevich
 */
public class FileCache {
	/**
	 * Настройки программы
	 */
	ConfigMainList config;
	/**
	 * Соединения к БД
	 */
	DBConCur_Parameters conn;
	/**
	 * Id темы шаблонов
	 */
	long themeId;
	/**
	 * Путь директории для кеширования
	 */
	private String path;
	/**
	 * Имя файла документа, который кешировался последним
	 */
	private String docFileName;

	/**
     * Конструктор.
     */
    public FileCache (DBConCur_Parameters conn, long themeId) {
    	config = new ConfigMainList();
    	this.conn = conn;
    	this.themeId = themeId;
    	
    	path = config.getItemValue("directories", "PathDirCache") + 
    			((conn.param.getConnId() > 0) ? conn.param.getConnId() : ("m"+(-1 * conn.param.getConnId()))) +"/"+
    			themeId +"/";
    }
	
	/**
	 * Проверяет наличие директории. Если ее нет, тогда создаем директорию и обязательные файлы.
	 */
	public void createDirAndFiles() throws KBase_Ex {
		File fDocDir = new File(path);
		
		if (! fDocDir.exists()) {
			if (! fDocDir.mkdirs())
				throw new KBase_Ex (-1, "FileCache.createDirAndFiles", "Невозможно создать директорию "+ path, this);
			
			File fDocDirFiles = new File(path +"_files/");
			if (! fDocDirFiles.mkdirs()) 
				throw new KBase_Ex (-2, "FileCache.createDirAndFiles", "Невозможно создать директорию "+ path +"_files/", this);
			
			// create required files
			List<TemplateRequiredFileItem> filesList = 
					conn.db.templateFileListByType(themeId, TemplateRequiredFileItem.FILETYPE_REQUIRED_FILE);
			for (TemplateRequiredFileItem i : filesList) {       // cycle for files
				i.saveToDisk(path+"_files/");
			}
		}
	}
	
	/**
	 * Создает файл документа на диске, если такого файла нет или он устарел.
	 */
	public void createFileDoc (DocumentItem doc) {
		docFileName = "doc_"+ doc.getSectionId() + ".html";
		boolean isNeedFileCache = false;
    	File f = new File(path + docFileName);
		
		if (f.exists()) {
			if (doc.getDateModified().compareTo(new Date(f.lastModified())) > 0) {
				isNeedFileCache = true;
			}
		} else {
			isNeedFileCache = true;
		}
		
		if (isNeedFileCache) {
			doc.saveToDisk(path + docFileName);
		}
	}
	
	/**
	 * Если уже есть директория кеша с файлами, тогда записываем указанный файл.
	 * Записываются только файлы из директории "_files" (type = TYPE_REQUIRED_FILE)
	 */
	public void updateRequredFile (TemplateRequiredFileItem trf) {
		File fDocDir = new File(path+"_files/");
	
		if (fDocDir.exists()) {
			trf.saveToDisk(path+"_files/");
		}
	}
	
	/**
	 * Записывает файлы инфо блоков "Изображение" и "Файл" для указанного раздела.
	 */
	public void createFilesOfInfoBlocksForDoc (long sectionId) throws KBase_Ex {
		String sFilesDir = path + "files_" + Long.toString(sectionId) + "/";
		File fFilesDir = new File (sFilesDir); 
	
		if (! fFilesDir.exists()) {
			if (! fFilesDir.mkdirs())
				throw new KBase_Ex (-3, "FileCache.createFilesOfInfoBlocksForDoc", "Невозможно создать директорию "+ fFilesDir.getName(), this);
		}
		
		for (InfoHeaderItem ih : conn.db.infoListBySectionId(sectionId)) {
			if (ih.getInfoTypeId() == 2) {            // Изображение
				Info_ImageItem ii = conn.db.info_ImageGet(ih.getInfoId());
				FileUtil.writeImageFile(sFilesDir+"image_" + Long.toString(ih.getId()) + ".png", ii.image);
			} else if (ih.getInfoTypeId() == 3) {     // Файл
				Info_FileItem ii = conn.db.info_FileGet(ih.getInfoId());
				FileUtil.writeBinaryFile(
						sFilesDir+"file_" + Long.toString(ih.getId()) + "." + FileUtil.getFileExt(ii.getName()), 
						ii.getFileBody());
				// write icon file
				IconItem ic = conn.db.iconGetById(ii.getIconId());
				if (ic != null) {
					FileUtil.writeImageFile(sFilesDir + "file_" + Long.toString(ih.getId()) + "_image.png", ic.image);
				} else {	// копируем иконку по умолчанию
					Path src = Paths.get("resources/images/icon_file_unknown.png");
			        Path dest = Paths.get(sFilesDir + "file_" + Long.toString(ih.getId()) + "_image.png");
			        try {
						Files.copy(src, dest);
					} catch (IOException e) {
						e.printStackTrace();
					}
					//TODO
				}
			}
		}
	}
	
	/**
	 * Геттер path
	 */
	public String getPath () {  return path;  }
	
	/**
	 * Геттер DocFileName
	 */
	public String getDocFileName () {  return docFileName;  }
}
