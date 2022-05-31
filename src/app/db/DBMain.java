package app.db;

import app.exceptions.KBase_DbConnEx;
import app.lib.ShowAppMsg;
import app.model.ConfigMainList;
import app.model.business.DocumentItem;
import app.model.business.IconItem;
import app.model.business.InfoHeaderItem;
import app.model.business.InfoTypeItem;
import app.model.business.InfoTypeStyleItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.Info_TextItem;
import app.model.business.SectionItem;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateStyleItem;
import app.model.business.template.TemplateThemeItem;
import app.model.business.template.TemplateItem;
import app.model.business.templates_old.TemplateRequiredFileItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Основной класс для работы с запросами к БД
 * @author Igor Makarevich
 */
public class DBMain {
	// Дескриптор соединения с сервером БД
	private Connection con;

	/**
	 * Конструктор
	 * @throw KBase_DbConnEx
	 * @param dbURL
     * @param user
     * @param password
	 */
	public DBMain (String dbURL, String user, String password) throws KBase_DbConnEx {
		con = null;
		
		try {
    		Class.forName("org.postgresql.Driver");
    	} catch (ClassNotFoundException e) {
    		System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
    		//e.printStackTrace();
    		throw new KBase_DbConnEx ("PostgreSQL JDBC Driver is not found. Include it in your library path ", this);
    	}
    	
    	try {
    		con = DriverManager.getConnection(dbURL, user, password);
    	} catch (SQLException e) {
    		System.out.println("Connection Failed");
    		//e.printStackTrace();
    		throw new KBase_DbConnEx ("Ошибка подключения.", this);
    	}
    	
    	if (con == null) {
    		System.out.println("Failed to make connection to database");
    		throw new KBase_DbConnEx ("Ошибка подключения.", this);
    	}
    	
    	//-------- set search_path for schemas
    	executeQuery("SELECT pg_catalog.set_config('search_path', 'kbase,\"$user\",public', false);");
    	
    	//-------- check version
    	ConfigMainList configSys = new ConfigMainList("ConfigSysMain.xml");
    	String versionProgram = configSys.getItemValue("DB", "Required version");
    	String versionDB = settingsGetValue("VERSION_DB_NUMBER");
    	
    	versionProgram = versionProgram.substring(0, versionProgram.lastIndexOf("."));
    	versionDB = versionDB.substring(0, versionDB.lastIndexOf("."));
    	//System.out.println("versionProgram = " + versionProgram);
    	//System.out.println("versionDB      = " + versionDB);
    	if (versionProgram.compareTo(versionDB) != 0) {
    		throw new KBase_DbConnEx (
    				"Версия Базы Данных "+ settingsGetValue("VERSION_DB_NUMBER") +" не поддерживается этой версией программы.", 
    				this);
    	}
	}
	
	/**
	 * Закрываем соединение
	 */
	public void close() throws KBase_DbConnEx {
		try {
    		con.close();
    	} catch (SQLException e) {
    		System.out.println("Close connection Failed");
    		//e.printStackTrace();
    		throw new KBase_DbConnEx ("Ошибка закрытия соединения с БД.", this);
    	}
	}
	
	/*
public static int getRowCount(ResultSet set) throws SQLException
{
   int rowCount;
   int currentRow = set.getRow();            // Get current row
   rowCount = set.last() ? set.getRow() : 0; // Determine number of rows
   if (currentRow == 0)                      // If there was no current row
      set.beforeFirst();                     // We want next() to go to first row
   else                                      // If there WAS a current row
      set.absolute(currentRow);              // Restore it
   return rowCount;
}
	 */
	
	/**
	 * Выполняет запрос и возвращает строку. 
	 */
	public String executeQuery (String strSQL) {
		String retVal = "";
		
		try {
			String stm = strSQL;
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			//retVal = rs.getString("path_name");
			retVal = rs.getString(1);
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка выполнения executeQuery.");
    	}
		
		return retVal;
	}
	
	/**
	 * Документ. Добавление нового.
	 */
	public void documentAdd (DocumentItem i) {
		PreparedStatement pst = null;
		
		try {
            String stm = "INSERT INTO documents (id, section_id, text, type) " + 
            			 "VALUES(?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getSectionId());
            pst.setString(3, i.getText());
            pst.setInt   (4, i.getType());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового скомпилированного документа (documentAdd).");
		}
	}
	
	/**
	 * Документ. true - документ присутствует в таблице кешированных документов.
	 */
	public boolean documentFindBySectionId (long sectionId) {
		boolean retVal = false;
	
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM documents " +
				         " WHERE section_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			if (rs.getLong("CountR") > 0)  retVal = true;
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("documentFindBySectionId : execute query Failed");
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (documentFindBySectionId)", 
					e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Документ. Получаем документ по идентификатору раздела.
	 */
	public DocumentItem documentGetBySectionId (long sectionId) {
		DocumentItem retVal = null;
		
		try {
			String stm = "SELECT id, section_id, text, type,  " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM documents " +
				         " WHERE section_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new DocumentItem(
         			rs.getLong("id"), 
         			rs.getLong("section_id"),
         			rs.getString("text"),
         			rs.getInt("type"), 
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"));
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("documentGetBySectionId (sectionId = "+ sectionId +") : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового документа
	 */
	public long documentNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_documents');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", "documentNextId ()");
    	}
		
		return retVal;
	}
	
	/**
	 * Документ. Изменение.
	 */
	public void documentUpdate (DocumentItem p) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = 	  "UPDATE documents " +
					  "   SET text = ?, type = ?, "+
					  "       date_modified = now(), user_modified = \"current_user\"() " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getText());
			pst.setInt   (2, p.getType());
			pst.setLong  (3, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении скомпилированного документа (documentUpdate()).");
		}
	}
	
	/**
	 * Пиктограмма. Добавление новой.
	 */
	public void iconAdd (IconItem i) {
		PreparedStatement pst = null;
		
		try {
            String stm = "INSERT INTO icons (id, parent_id, name, file_name, descr, image) " + 
            			 "VALUES(?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getParentId());
            pst.setString(3, i.getName());
            pst.setString(4, i.getFileName());
            pst.setString(5, i.getDescr());
            
            File file = new File(i.getFileName());
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(6, fis, (int)file.length());

            pst.executeUpdate();
            pst.close();
            fis.close();

        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении новой пиктограммы.");
        } catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при добавлении новой пиктограммы (сохранение картинки).");
		}
	}
	
	/**
	 * Пиктограмма. Удаление пиктограммы со всеми подчиненными пиктограммами.
	 * @param
	 */
	public void iconDelete (long id) {
		PreparedStatement pst = null;
	
		try {
            //String stm = "DELETE FROM catalog_icons WHERE id = ? ";
			String stm = 
					"WITH RECURSIVE x AS ( "+
					"	SELECT id "+
					"     FROM icons "+
					"    WHERE id = ? "+
					"   UNION  ALL "+
					"   SELECT a.id "+
					"     FROM x "+
					"     JOIN icons a ON a.parent_id = x.id "+
					") "+
					"DELETE FROM icons a "+
					" USING  x "+
					" WHERE a.id = x.id" +
					";";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	
        	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Пиктограмма. Сохраняем последнюю выбранную пиктограмму для текущего пользователя.
	 */
	@SuppressWarnings("resource")
	public void iconEditCurrent (long iconId) {
		String stm;
		int countR;
		
		try {
			//-------- check record for exist
			stm = "SELECT count(ci.id) CountR " +
			      "  FROM current_icon ci " + 
                  " WHERE ci.\"user\" = \"current_user\"() " +
                  ";";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			rs.next();

			countR = rs.getInt("CountR");
			
			rs.close();
			pst.close();
			
			//-------- insert style
			if (countR == 0) {
				stm = "INSERT INTO current_icon (icon_id) " + 
           			  "VALUES(?)";
				pst = con.prepareStatement(stm);
				pst.setLong (1, iconId);
			} else {
			//-------- update style
				stm = 	  "UPDATE current_icon " +
						  "   SET icon_id = ?, "+
						  "       date_modified = now() " +
					      " WHERE \"user\" = \"current_user\"() " +
					      ";";
				pst = con.prepareStatement(stm);
				pst.setLong(1, iconId);
			}
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных iconEditCurrent", 
					e.getMessage());
    	}
	}
	
	/**
	 * Пиктограмма. Получение информации по id
	 */
	public IconItem iconGetById (long id) {
		IconItem retVal = null;
		
		//System.out.println("iconGetById.id = " + id);
		
		try {
			String stm = "SELECT id, parent_id, name, file_name, descr, image, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM icons " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
			
			retVal = new IconItem(
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getString("name"),
         			rs.getString("file_name"),
         			rs.getString("descr"),
         			new Image(isImage),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"));
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("get icon info (DBMain.iconGetById) : execute query Failed (id = "+ id +")");
    		//e.printStackTrace();
    	} catch (NullPointerException e) {
    		System.out.println("get icon info (DBMain.iconGetById) : NullPointerException (id = "+ id +")");
    		//e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Пиктограмма. Возвращает последнюю выбранную иконку для текущего пользователя.
	 */
	public IconItem iconGetCurrent () {
		IconItem retVal = null;
	
		try {
			String stm = 
					 "SELECT i.id, i.parent_id, i.name, i.file_name, i.descr, i.image, " +
	                 "       i.date_created, i.date_modified, i.user_created, i.user_modified " +
			         "  FROM icons i, current_icon ci " +
			         " WHERE ci.icon_id = i.id " +
			         "   AND ci.\"user\" = \"current_user\"() " +
                     ";";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
				
				retVal = new IconItem(
						rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getString("name"),
	         			rs.getString("file_name"),
	         			rs.getString("descr"),
	         			new Image(isImage),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified"));
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("infoTypeStyleGet : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , iconGetCurrent ()", 
		            e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Пиктограмма. Возвращает изображение пиктограммы.
	 * @param
	 */
	public Image iconGetImageById (long id) {
		InputStream isImage = null;
		Image retVal;
		
		try {
			String stm = "SELECT image " +
					     "  FROM icons " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				//byte[] imgBytes = rs.getBytes("image");
				isImage = new ByteArrayInputStream(rs.getBytes("image"));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		if (isImage == null) retVal = null;
		else                 retVal = new Image(isImage);
		
		return retVal;
	}
	
	/**
	 * Пиктограмма. Возвращает название пиктограммы.
	 * @param
	 */
	public String iconGetNameById (long id) {
		String retVal = null;
		
		try {
			String stm = "SELECT name " +
					     "  FROM icons " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				retVal = rs.getString("name");
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Пиктограмма. Возвращает кол-во потомков данной ноды-пиктограммы.
	 * @param
	 */
	public int iconGetNumberOfChildren (long id) {
		int retVal = 0;
		
		try {
			String stm = "SELECT count(*) CountR FROM icons where parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getInt("CountR");

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список иконок для указанного родителя
	 */
	public List<IconItem> iconListByParentId (long parentId) {
		List<IconItem> retVal = new ArrayList<IconItem>();
		
		try {
			String stm = "SELECT id, parent_id, name, file_name, descr, image, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM icons " +
					     " WHERE parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, parentId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				//byte[] imgBytes = rs.getBytes("image");
				InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
				//Image img = new Image(isImage);
				
				retVal.add(new IconItem(
	         			rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getString("name"),
	         			rs.getString("file_name"),
	         			rs.getString("descr"),
	         			new Image(isImage),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Меняет у текущей иконки родителя (перемещение иконки по дереву)
	 * @param currentId
	 * @param targetId
	 */
	public void iconMove (long currentId, long targetId) {
		PreparedStatement pst = null;
		String stm;
	
		try {
			stm = 	  "UPDATE icons " +
					  "   SET parent_id = ?, date_modified = now() " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setLong (1, targetId);
			pst.setLong (2, currentId);
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при перемещении пиктограммы.");
		}
	}
	
	/**
	 * Выдает следующий Id для добавления новой иконки
	 */
	public long iconNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_icons');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_icons');");
    	}
		
		return retVal;
	}
	
	/**
	 * Пиктограмма. Изменение пиктограммы.
	 * @param
	 */
	public void iconUpdate (IconItem i, boolean isImgUpdate) {
		PreparedStatement pst = null;
		String stm;
		
		if (isImgUpdate) {             // с изменением картинки
			try {
				stm = 	  "UPDATE icons " +
						  "   SET name = ?, file_name = ?, descr = ?, image = ?, " +
						  "       date_modified = now(), user_modified = \"current_user\"() " +
					      " WHERE id = ? " +
					      ";";
				pst = con.prepareStatement(stm);
				pst.setString(1, i.getName());
				pst.setString(2, i.getFileName());
				pst.setString(3, i.getDescr());
				
				File file = new File(i.getFileName());
	            FileInputStream fis = new FileInputStream(file);
	            pst.setBinaryStream(4, fis, (int)file.length());
				
	            pst.setLong  (5, i.getId());
				
	            pst.executeUpdate();
	            pst.close();
	            fis.close();
			} catch (SQLException ex) {
	            //Logger lgr = Logger.getLogger(Prepared.class.getName());
	            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при изменении пиктограммы.");
	        } catch (IOException e) {
				e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			             "Ошибка при изменении пиктограммы (сохранение новой картинки).");
			}
		} else {
			try {
				stm = 	  "UPDATE icons " +
						  "   SET name = ?, descr = ?, date_modified = now(), user_modified = \"current_user\"() " +
					      " WHERE id = ? " +
					      ";";
				pst = con.prepareStatement(stm);
				pst.setString(1, i.getName());
				pst.setString(2, i.getDescr());
				pst.setLong  (3, i.getId());
				
				pst.executeUpdate();
	            pst.close();
			} catch (SQLException ex) {
	            //Logger lgr = Logger.getLogger(Prepared.class.getName());
	            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при изменении пиктограммы.");
			}
		}
	}
	
	/**
	 * Тип инфо блока. Получение информации по id
	 */
	public InfoTypeItem infoTypeGet (long id) {
		InfoTypeItem retVal = null;
	
		try {
			String stm = "SELECT id, name, descr, table_name, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM infotype " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new InfoTypeItem (
					rs.getLong("id"), 
         			rs.getString("name"),
         			rs.getString("table_name"),
         			rs.getString("descr"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("infoTypeGet : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список информационных блоков
	 */
	public List<InfoTypeItem> infoTypeList () {
		List<InfoTypeItem> retVal = new ArrayList<InfoTypeItem>();
		
		try {
			String stm = "SELECT id, name, table_name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM infotype " +
		                 " WHERE id > 0 ";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new InfoTypeItem(
	         			rs.getLong("id"), 
	         			rs.getString("name"),
	         			rs.getString("table_name"),
	         			rs.getString("descr"),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
	         			));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	//TODO OLD STYLE
	//########################################################## OLD STYLE
	/**
	 * Подсчет количества стилей для указанного родительского стиля.
	 * @param
	 */
	public long infoTypeStyleCountByParentId (long parentId) {
		long retVal = 0;
	
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM infotype_style " +
				         " WHERE parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, parentId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("CountR");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("count templates : execute query Failed");
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Стили шаблонов. Удаление всех стилей.
	 */
	public void infoTypeStyleDelete () {
		PreparedStatement pst = null;
	
		try {
            String stm = "DELETE FROM infotype_style ; ";
            pst = con.prepareStatement(stm);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	
        	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (infoTypeStyleDelete)", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Стили шаблонов. Удаление стиля по его Id.
	 */
	public void infoTypeStyleDelete (long id) {
		PreparedStatement pst = null;
	
		try {
            String stm = "DELETE FROM infotype_style WHERE id = ? ; ";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	
        	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (infoTypeStyleDelete by id)", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Стиль шаблонов. Обновление последнего используемого стиля для темы, типа блока и пользователя.
	 */
	@SuppressWarnings("resource")
	public void infoTypeStyleEditCurrent (long themeId, long infoTypeId, long infoTypeStyleId, int flag) {
		String stm;
		int countR;
		
		try {
			//-------- check record with the style for exist
			stm = "SELECT count(cs.id) CountR " +
			      "  FROM infotype_style s, current_style cs " + 
                  " WHERE s.id = cs.template_style_id " +
                  "   AND cs.theme_id = ? " +
                  "   AND s.infotype_id = ? " +
                  "   AND cs.\"user\" = \"current_user\"() " +
                  "   AND cs.flag = ? " +
                  ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, infoTypeId);
			pst.setInt  (3, flag);
			ResultSet rs = pst.executeQuery();
			rs.next();

			countR = rs.getInt("CountR");
			
			rs.close();
			pst.close();
			
			//-------- insert style
			if (countR == 0) {
				stm = "INSERT INTO current_style (theme_id, template_style_id, flag) " + 
           			  "VALUES(?, ?, ?)";
				pst = con.prepareStatement(stm);
				pst.setLong (1, themeId);
				pst.setLong (2, infoTypeStyleId);
				pst.setInt  (3, flag);
			} else {
			//-------- update style
				stm = 	  "UPDATE current_style " +
						  "   SET template_style_id = ?, "+
						  "       date_modified = now() " +
					      " WHERE theme_id = ? " +
						  "   AND template_style_id = ? " +
					      "   AND flag = ? " +
					      ";";
				pst = con.prepareStatement(stm);
				pst.setLong(1, infoTypeStyleId);
				pst.setLong(2, themeId);
				pst.setLong(3, infoTypeStyleGetCurrent (themeId, infoTypeId, flag).getId()); // old current style
				pst.setInt (4, flag);
			}
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных InfoTypeStyleEditCurrent", 
					e.getMessage());
    	}
	}
	
	/**
	 * Стиль шаблонов. Получение информации по id
	 */
	public InfoTypeStyleItem infoTypeStyleGet (long id) {
		InfoTypeStyleItem retVal = null;
	
		try {
			String stm = "SELECT id, parent_id, infotype_id, name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM infotype_style " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new InfoTypeStyleItem (
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getLong("infotype_id"),
         			rs.getString("name"),
         			rs.getString("descr"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("infoTypeStyleGet : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , infoTypeStyleGet ("+id+")", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает текущий стиль согласно флагу в указанной теме для указанного типа инфо блока.
	 */
	public InfoTypeStyleItem infoTypeStyleGetCurrent (long themeId, long infoTypeId, int flag) {
		InfoTypeStyleItem retVal = null;
	
		try {
			String stm = "SELECT s.id, s.parent_id, s.infotype_id, s.name, s.descr, " +
		                 "       s.date_created, s.date_modified, s.user_created, s.user_modified " +
				         "  FROM infotype_style s, current_style cs " + 
                         " WHERE s.id = cs.template_style_id " +
                         "   AND cs.theme_id = ? " +
                         "   AND s.infotype_id = ? " +
                         "   AND cs.\"user\" = \"current_user\"() " +
                         "   AND cs.flag = ? " +
                         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, infoTypeId);
			pst.setInt  (3, flag);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal = new InfoTypeStyleItem (
						rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getLong("infotype_id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("infoTypeStyleGet : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , infoTypeStyleGetCurrent ("+themeId+","+infoTypeId+","+flag+")", 
		            e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Возвращает стиль по умолчанию в указанной теме для указанного типа инфо блока.
	 * Если такого стиля нет, возвращается null.
	 */
	public InfoTypeStyleItem infoTypeStyleGetDefault (long themeId, long infoTypeId) {
		long styleId = -1;
	
		try {
			String stm = "SELECT InfoTypeStyle_getIdDefault(?,?,3) AS id ;";
     		PreparedStatement pst = con.prepareStatement(stm);
	    	pst.setLong  (1, themeId);
		    pst.setLong  (2, infoTypeId);
		    ResultSet rs = pst.executeQuery();
			
		    rs.next();
		    styleId = rs.getLong("id");
		    
		    rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , infoTypeStyleGetDefault ("+themeId+","+infoTypeId+")", 
		            e.getMessage());
    	}
		
		return infoTypeStyleGet(styleId);
	}
	
	/**
	 * Возващает список стилей типов инфоблоков по id типа инфоблока 
	 * или по id родительского стиля. 
	 * Если infoTypeStyleId = 0, то это корневые стили типа инфоблока.
	 * @param infoTypeId, infoTypeStyleId - родительский стиль
	 * @return
	 */
	public List<app.model.business.templates_old.TemplateSimpleItem> infoTypeStyleList 
			(long infoTypeId, long infoTypeStyleId) {
		List<app.model.business.templates_old.TemplateSimpleItem> retVal = 
				new ArrayList<app.model.business.templates_old.TemplateSimpleItem>();
		PreparedStatement pst = null;
	
		try {
			if (infoTypeStyleId == 0) {
				String stm = "SELECT id, name, descr, " +
						     "       date_created, date_modified, user_created, user_modified " +
					         "  FROM infotype_style " +
					         " WHERE coalesce(parent_id,0) = 0 " +
				             "   AND infotype_id = ?";
				pst = con.prepareStatement(stm);
				pst.setLong (1, infoTypeId);
			} else {
				String stm = "SELECT id, name, descr, " +
				  	         "       date_created, date_modified, user_created, user_modified " +
				             "  FROM infotype_style " +
				             " WHERE parent_id = ? ";
				pst = con.prepareStatement(stm);
				pst.setLong (1, infoTypeStyleId);
			}
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new app.model.business.templates_old.TemplateSimpleItem(
	         			rs.getLong("id"),
	         			0,                 // здесь themeId взять негде
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			app.model.business.templates_old.TemplateSimpleItem.TYPE_STYLE,
						0,
						0,
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						));
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
    		System.out.println("execute query Failed (infoTypeStyleListByInfoTypeId)");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * функция устанавливает указанный стиль как дефолтный для указанной темы
	 */
	public void infoTypeStyleSetDefault (long themeId, long infoTypeStyleId) {
		PreparedStatement pst = null;
		
		try {
            String stm = "SELECT TemplateStyle_setdefault (?, ?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);
            pst.setLong  (2, infoTypeStyleId);
            ResultSet rs = pst.executeQuery();
            
            rs.close();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, infoTypeStyleSetDefault", 
					             e.getMessage());
		}
	}
	
	/**
	 * функция удаляет указанный стиль как дефолтный для указанной темы
	 */
	public void infoTypeStyleUnsetDefault (long themeId, long infoTypeStyleId) {
		PreparedStatement pst = null;
		
		try {
            String stm = "SELECT TemplateStyle_unsetdefault (?, ?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);
            pst.setLong  (2, infoTypeStyleId);
            ResultSet rs = pst.executeQuery();
            
            rs.close();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, infoTypeStyleUnsetDefault", 
					             e.getMessage());
		}
	}
	//TODO OLD STYLE end
	//########################################################## OLD STYLE

	/**
	 * Заголовок инфоблока. Добавление нового.
	 */
	public void infoAdd (InfoHeaderItem i) {
		PreparedStatement pst;
		
		try {
            String stm = "INSERT INTO info (id, sectionid, infotypeid, infoid, position, infotypestyleid, " +
		                 "                            name, descr) " + 
            			 "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getSectionId());
            pst.setLong  (3, i.getInfoTypeId());
            pst.setLong  (4, i.getInfoId());
            pst.setLong  (5, i.getPosition());
            pst.setLong  (6, i.getInfoTypeStyleId());
            pst.setString(7, i.getName());
            pst.setString(8, i.getDescr());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового заголовка блока (infoAdd).");
		}
		
		sectionUpdateDateModifiedInfo (i.getSectionId());
	}
	
	/**
	 * Подсчет кол-ва инфоблоков для указанного раздела.
	 */
	public long infoCount (long sectionId) {
		long retVal = 0;
		
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM info " +
				         " WHERE SectionId = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("CountR");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("count templates : execute query Failed");
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (infoCount)", e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок. Удаление одного инфо блока.
	 * @param
	 */
	public void infoDelete (long infoHeaderId) {
		PreparedStatement pst = null;
	
		try {
			String stm = "SELECT info_delete1(?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, infoHeaderId);

            ResultSet rs = pst.executeQuery();
			rs.next();
			
			rs.close();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при удалении инфо блока.", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Заголовок инфо блока. Получение информации по id
	 */
	public InfoHeaderItem infoGet (long id) {
		InfoHeaderItem retVal = null;
	
		try {
			String stm = "SELECT id,sectionid,infotypeid,infoid,position,name,descr,infotypestyleid, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM info " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new InfoHeaderItem (
					rs.getLong("id"), 
					rs.getLong("sectionid"),
					rs.getLong("infoTypeId"),
					rs.getLong("infoTypeStyleId"),
					rs.getLong("infoId"),
					rs.getLong("position"),
					rs.getString("name"),
         			rs.getString("descr"),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			rs.close();
			pst.close();
		} catch (SQLException e) {
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (infoGet)", e.getMessage());
    		e.printStackTrace();
    	}
		
		return retVal;
	}

    /**
     * Заголовок инфо блока. Получение информации по infoTypeId и infoId
     */
    public InfoHeaderItem infoGet(long infoTypeId, long infoId) {
        InfoHeaderItem retVal = null;

        try {
            String stm = "SELECT id,sectionid,infotypeid,infoid,position,name,descr,infotypestyleid, " +
                    "       date_created, date_modified, user_created, user_modified " +
                    "  FROM info " +
                    " WHERE InfoTypeId = ?" +
                    "   AND InfoId = ? ";
            PreparedStatement pst = con.prepareStatement(stm);
            pst.setLong (1, infoTypeId);
            pst.setLong (2, infoId);
            ResultSet rs = pst.executeQuery();
            rs.next();

            java.util.Date dateTmpCre;
            Timestamp timestampCr = rs.getTimestamp("date_created");
            if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
            else                      dateTmpCre = null;

            java.util.Date dateTmpMod;
            Timestamp timestampMo = rs.getTimestamp("date_modified");
            if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
            else                      dateTmpMod = null;

            retVal = new InfoHeaderItem (
                    rs.getLong("id"),
                    rs.getLong("sectionid"),
                    rs.getLong("infoTypeId"),
                    rs.getLong("infoTypeStyleId"),
                    rs.getLong("infoId"),
                    rs.getLong("position"),
                    rs.getString("name"),
                    rs.getString("descr"),
                    dateTmpCre,
                    dateTmpMod,
                    rs.getString("user_created"),
                    rs.getString("user_modified")
            );
            rs.close();
            pst.close();
        } catch (SQLException e) {
            ShowAppMsg.showAlert(
                    "WARNING", "db error",
                    "Ошибка при работе с базой данных : infoGet(long infoTypeId, long infoId)",
                    e.getMessage());
            e.printStackTrace();
        }

        return retVal;
    }

	/**
	 * Возвращает максимальное значение позиции инфо блока в разделе (currentPosition = 0).
	 * Если currentPosition > 0, то ищется максимальное значение меньшее этого.
	 */
	public long infoGetMaxPosition (long sectionId, long currentPosition) {
		String stm;
		PreparedStatement pst;
		long retVal = 0;
		
		try {
			if (currentPosition == 0) {
				stm = "SELECT max(position) MaxPosition " +
				      "  FROM info " +
				      " WHERE SectionId = ?";
				pst = con.prepareStatement(stm);
				pst.setLong (1, sectionId);
			} else {   
				stm = "SELECT max(position) MaxPosition " +
					      "  FROM info " +
					      " WHERE SectionId = ?" +
					      "   AND position < ? ";
				pst = con.prepareStatement(stm);
				pst.setLong (1, sectionId);
				pst.setLong (2, currentPosition);
			}
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("MaxPosition");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("count templates : execute query Failed");
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (infoGetMaxPosition)", e.getMessage());
    	}
	
		return retVal;
	}
	
	/**
	 * Возвращает список заголовков инфо блоков для указанного раздела
	 */
	public List<InfoHeaderItem> infoListBySectionId (long sectionId) {
		List<InfoHeaderItem> retVal = new ArrayList<InfoHeaderItem>();
		
		try {
			String stm = "SELECT id, name, descr, position, " +
					     "       sectionid, infotypeid, infoid, infotypestyleid, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM info " +
					     " WHERE sectionid = ? " +
					     " ORDER BY position ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new InfoHeaderItem(
						rs.getLong("id"), 
						rs.getLong("sectionId"),
						rs.getLong("infoTypeId"),
						rs.getLong("infoTypeStyleId"),
						rs.getLong("infoId"),
						rs.getLong("position"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed ( infoListBySectionId() ) ");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового заголовка инфо блока
	 */
	public long infoNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_info');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_info');");
    	}
		
		return retVal;
	}
	
	/**
	 * Проверяет на дубль добавляемую позицию и при необходимости перенумеровывает всю последовательность блоков в разделе.
	 * Возвращает новое значение newPosition в списке после перенумерации.
	 */
	public long infoPositionCheckAndRenumber (long sectionId, long newPosition) {
		String stm;
		long retVal = 0;
		
		stm = "SELECT InfoPositionCheckAndRenumber (?,?) as position_new;";
			
		try {
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			pst.setLong (2, newPosition);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("position_new");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			//System.out.println("get section themeId (id = "+ sectionId +") : execute query Failed");
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "infoPositionCheckAndRenumber");
		}
		
		return retVal;
	}
	
	/**
	 * Инфо заголовок. Изменение.
	 */
	public void infoUpdate (InfoHeaderItem p) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = 	  "UPDATE info " +
					  "   SET sectionId = ?, infoTypeId = ?, infoId = ?, infoTypeStyleId = ?, " +
					  "       position = ?, name = ?, descr = ?, " +
					  "       date_modified = now(), user_modified = \"current_user\"() " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setLong  (1, p.getSectionId());
			pst.setLong  (2, p.getInfoTypeId());
			pst.setLong  (3, p.getInfoId());
			pst.setLong  (4, p.getInfoTypeStyleId());
			pst.setLong  (5, p.getPosition());
			pst.setString(6, p.getName());
			pst.setString(7, p.getDescr());
			pst.setLong  (8, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо заголовка (infoUpdate).");
		}
	}
	
	/**
	 * Инфо блок "Файл". 
	 * Добавление нового.
	 */
	public void info_FileAdd (Info_FileItem i, String fileName) {
		PreparedStatement pst = null;
		
		try {
			String stm = "INSERT INTO info_file(id, title, file_body, file_name, icon_id, descr, text, " +
		                 "                       isShowTitle, isshowdescr, isshowtext) " + 
       			         "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getTitle());
            
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(3, fis, (int)file.length());
            
            pst.setString(4, i.getName());     // fileName
            pst.setLong  (5, i.getIconId());
            pst.setString(6, i.getDescr());
            pst.setString(7, i.getText());
            pst.setInt   (8, i.getIsShowTitle());
            pst.setInt   (9, i.getIsShowDescr());
            pst.setInt   (10,i.getIsShowText());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового блока (info_FileAdd).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при добавлении нового блока (info_FileAdd).\n" +
					 "Не найден файл " + fileName);
		}
	}
	
	/**
	 * Инфо блок "Файл". 
	 * Возвращает тело инфо блока.
	 */
	public Info_FileItem info_FileGet (long id) {
		Info_FileItem retVal = null;
		
		try {
			String stm = "SELECT id, title, file_body, file_name, icon_id, descr, text, " +
		                 "       isshowtitle, isshowdescr, isshowtext " +
					     "  FROM info_file " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			//byte[] fileBody = rs.getBytes("file_body");
			
			retVal = new Info_FileItem (
					rs.getLong("id"),
					rs.getString("title"),
					rs.getBytes("file_body"),
					rs.getString("file_name"),
					rs.getLong("icon_id"),
					rs.getString("descr"),
					rs.getString("text"),
         			rs.getInt("isshowtitle"),
         			rs.getInt("isshowdescr"),
         			rs.getInt("isshowtext")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("infoTypeGet : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при чтении Инфо блока \"Файл\" (info_FileGet).");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Файл".
	 * Выдает следующий Id для добавления нового блока
	 */
	public long info_FileNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_info_file');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_info_file');");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Файл". 
	 * Modify an existing file.
	 */
	public void info_FileUpdate (Info_FileItem i, String fileName) {
		PreparedStatement pst = null;
		
		try {
			String stm = 
					"UPDATE info_file " +
					"   SET title = ?, descr = ?, text = ?, isshowtitle = ?, isshowdescr = ?, isshowtext = ?, " +
					"       file_body = ?, file_name = ?, icon_id = ? " +
					" WHERE id = ? " +	
					"";
			
			pst = con.prepareStatement(stm);
			pst.setString(1, i.getTitle());
			pst.setString(2, i.getDescr());
            pst.setString(3, i.getText());		
            pst.setInt   (4, i.getIsShowTitle());
            pst.setInt   (5, i.getIsShowDescr());
            pst.setInt   (6, i.getIsShowText());
            
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(7, fis, (int)file.length());
            
            pst.setString(8, i.getName());
            pst.setLong  (9, i.getIconId());
            pst.setLong  (10, i.getId());		
            
            pst.executeUpdate();
            pst.close();

            sectionUpdateDateModifiedInfo (infoGet((long)3, i.getId()).getSectionId());
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо блока (info_FileUpdate).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при изменении Файла, файл не найден (info_FileUpdate).");
		}
	}
	
	/**
	 * Инфо блок "Изображение". 
	 * Добавление нового.
	 */
	public void info_ImageAdd (Info_ImageItem i, String fileName) {
		PreparedStatement pst = null;
		
		try {
			String stm = "INSERT INTO info_image(id, title, image, width, height, descr, text, " +
		                 "                       isShowTitle, isshowdescr, isshowtext) " + 
       			         "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getTitle());
            
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(3, fis, (int)file.length());
            
            pst.setInt   (4, i.getWidth());
            pst.setInt   (5, i.getHeight());
            pst.setString(6, i.getDescr());
            pst.setString(7, i.getText());
            pst.setInt   (8, i.getIsShowTitle());
            pst.setInt   (9, i.getIsShowDescr());
            pst.setInt   (10,i.getIsShowText());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового блока (info_ImageAdd).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при добавлении нового блока (info_ImageAdd).\n" +
					 "Не найден файл " + fileName);
		}
	}
	
	/**
	 * Инфо блок "Изображение". 
	 * Возвращает тело инфо блока.
	 */
	public Info_ImageItem info_ImageGet (long id) {
		Info_ImageItem retVal = null;
		
		try {
			String stm = "SELECT id, title, image, width, height, descr, text, " +
		                 "       isshowtitle, isshowdescr, isshowtext " +
					     "  FROM info_image " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
			
			retVal = new Info_ImageItem (
					rs.getLong("id"),
					rs.getString("title"),
					new Image(isImage),
					rs.getInt("width"),
					rs.getInt("height"),
					rs.getString("descr"),
					rs.getString("text"),
         			rs.getInt("isshowtitle"),
         			rs.getInt("isshowdescr"),
         			rs.getInt("isshowtext")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("infoTypeGet : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при чтении Инфо блока \"Изображение\".");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Изображение".
	 * Выдает следующий Id для добавления нового блока
	 */
	public long info_ImageNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_info_image');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_info_image');");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Изображение". 
	 * Изменение существующего.
	 */
	public void info_ImageUpdate (Info_ImageItem i, String imageFileName) {
		PreparedStatement pst = null;
		
		try {
			String stm = 
					"UPDATE info_image " +
					"   SET title = ?, image = ?, width = ?, height = ?, " +
					"       descr = ?, text = ?, isshowtitle = ?, isshowdescr = ?, isshowtext = ? " +
					" WHERE id = ? " +	
					"";
			pst = con.prepareStatement(stm);
			pst.setString(1, i.getTitle());
			
			File file = new File(imageFileName);
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(2, fis, (int)file.length());
            
            pst.setInt   (3, i.getWidth());
            pst.setInt   (4, i.getHeight());
            pst.setString(5, i.getDescr());
            pst.setString(6, i.getText());		
            pst.setInt   (7, i.getIsShowTitle());
            pst.setInt   (8, i.getIsShowDescr());
            pst.setInt   (9, i.getIsShowText());
            pst.setLong  (10, i.getId());
            
            pst.executeUpdate();
            pst.close();

            sectionUpdateDateModifiedInfo (infoGet((long)2, i.getId()).getSectionId());
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо блока (info_ImageUpdate).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при изменении Изображения, файл не найден (info_ImageUpdate).");
		}
	}

	/**
	 * Инфо блок "Простой текст". 
	 * Добавление нового.
	 */
	public void info_TextAdd (Info_TextItem i) {
		PreparedStatement pst = null;
		
		try {
            String stm = "INSERT INTO info_text (id, title, text, isShowTitle) " + 
            			 "VALUES(?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getTitle());
            pst.setString(3, i.getText());
            pst.setInt   (4, i.getIsShowTitle());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового блока (info_TextAdd).");
		}
	}
	
	/**
	 * Инфо блок "Простой текст". 
	 * Возвращает тело инфо блока.
	 */
	public Info_TextItem info_TextGet (long id) {
		Info_TextItem retVal = null;
		
		try {
			String stm = "SELECT id, title, text, isshowtitle " +
				         "  FROM info_text " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = new Info_TextItem (
					rs.getLong("id"), 
         			rs.getString("title"),
         			rs.getString("text"),
         			rs.getInt("isshowtitle")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("infoTypeGet : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при чтении Инфо блока \"Простой текст\".");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Простой текст".
	 * Выдает следующий Id для добавления нового блока
	 */
	public long info_TextNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_info_text');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_info_text');");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Простой текст". 
	 * Изменение существующего.
	 */
	public void info_TextUpdate (Info_TextItem i) {
		PreparedStatement pst = null;
		
		try {
			String stm = 
					"UPDATE info_text " +
					"   SET title = ?, text = ?, isShowTitle = ?" +
					" WHERE id = ? ";
            pst = con.prepareStatement(stm);
            pst.setString(1, i.getTitle());
            pst.setString(2, i.getText());
            pst.setInt   (3, i.getIsShowTitle());
            pst.setLong  (4, i.getId());
            
            pst.executeUpdate();
            pst.close();

            sectionUpdateDateModifiedInfo (infoGet((long)1, i.getId()).getSectionId());
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо блока (info_TextUpdate).");
		}
	}

	/**
	 * Раздел. Добавление нового.
	 */
	public void sectionAdd (SectionItem i) {
		PreparedStatement pst = null;
		
		try {
            String stm = "INSERT INTO sections (id, parent_id, name, icon_id, descr, " +
            		     "                      icon_id_root, icon_id_def, theme_id, cache_type) " + 
            			 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getParentId());
            pst.setString(3, i.getName());
            pst.setLong  (4, i.getIconId());
            pst.setString(5, i.getDescr());
            pst.setLong  (6, i.getIconIdRoot());
            pst.setLong  (7, i.getIconIdDef());
            pst.setLong  (8, i.getThemeId());
            pst.setInt   (9, i.getCacheType());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового раздела.");
		}
	}

	/**
	 * Раздел. Копирование всех инфо блоков с одного раздела в другой.
	 * @param
	 */
	public void sectionCopyInfoBlocks (long sectionSrcId, long sectionTrgId) {
		PreparedStatement pst = null;

		try {
			String stm = "SELECT section_copyInfoBlocks(?,?);";
			pst = con.prepareStatement(stm);
			pst.setLong  (1, sectionSrcId);
			pst.setLong  (2, sectionTrgId);

			ResultSet rs = pst.executeQuery();
			rs.next();

			rs.close();
			pst.close();
		} catch (SQLException ex) {
			//Logger lgr = Logger.getLogger(Prepared.class.getName());
			//lgr.log(Level.SEVERE, ex.getMessage(), ex);
			ex.printStackTrace();
			ShowAppMsg.showAlert(
					"WARNING", "db error",
					"Ошибка при копировании всех инфо блоков с одного раздела в другой (sectionCopyInfoBlocks).",
					ex.getMessage());
		}
	}

	/**
	 * Раздел. Удаление раздела со всеми подчиненными разделами.
	 * @param
	 */
	public void sectionDelete (long id) {
		PreparedStatement pst = null;
	
		try {
			String stm = "SELECT section_delete(?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            ResultSet rs = pst.executeQuery();
			rs.next();
			
			rs.close();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при удалении раздела.", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Раздел. Получение информации по id
	 */
	public SectionItem sectionGetById (long id) {
		SectionItem retVal = null;
	
		try {
			String stm = "SELECT id, parent_id, name, icon_id, descr, " +
		                 "       date_created, date_modified, user_created, user_modified, date_modified_info, " +
	                     "       icon_id_root, icon_id_def, theme_id, cache_type " +
				         "  FROM sections " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			java.util.Date dateTmpModInfo;
			Timestamp timestampMoInf = rs.getTimestamp("date_modified_info");
			if (timestampMoInf != null)  dateTmpModInfo = new java.util.Date(timestampMoInf.getTime());
			else                         dateTmpModInfo = null;
			
			retVal = new SectionItem(
         			rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getString("name"),
         			rs.getLong("icon_id"), 
         			(rs.getLong("icon_id") > 0) ? iconGetImageById (rs.getLong("icon_id")) : null,
         			rs.getString("descr"),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"),
         			dateTmpModInfo,
         			rs.getLong("icon_id_root"),
         			rs.getLong("icon_id_def"),
         			rs.getLong("theme_id"),
         			rs.getInt("cache_type"));
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("get section info (id = "+ id +") : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает id пиктограммы по умолчанию для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public long sectionGetIconIdDefault (long sectionId, boolean isRecursive) {
		String stm;
		long retVal = 0;
		
		if (isRecursive) {
			stm = "SELECT Section_GetIconIdDefault (?) as icon_id_def;";
			
		} else {
			stm = "SELECT icon_id_def  FROM sections  WHERE id = ?";
		}
		
		try {
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("icon_id_def");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении id пиктограммы по умолчанию для раздела ( sectionGetIconIdDefault("+ sectionId +") ).");
    	}
		
		return retVal;
	}
	
	/**
	 * Раздел. Возвращает кол-во потомков данной ноды-раздела.
	 * @param
	 */
	public int sectionGetNumberOfChildren (long id) {
		int retVal = 0;
		
		try {
			String stm = "SELECT count(*) CountR FROM sections where parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getInt("CountR");

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает цепочку имен разделов от указанного до самого верхнего родителя. 
	 */
	public String sectionGetPathName (long sectionId, String delimiter) {
		String retVal = "";
		
		try {
			String stm = "SELECT section_get_pathname(?,?) AS path_name ;";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, sectionId);
			pst.setString(2, delimiter);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			retVal = rs.getString("path_name");
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении пути раздела ( sectionGetPathName() ).");
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает id темы для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public long sectionGetThemeId (long sectionId, boolean isRecursive) {
		String stm;
		long retVal = 0;
		
		if (isRecursive) {
			stm = "SELECT Section_GetThemeId (?) as theme_id;";
			
		} else {
			stm = "SELECT theme_id  FROM sections  WHERE id = ?";
		}
		
		try {
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("theme_id");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("get section themeId (id = "+ sectionId +") : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список разделов для указанного родителя
	 */
	public List<SectionItem> sectionListByParentId (long parentId) {
		List<SectionItem> retVal = new ArrayList<SectionItem>();
		
		try {
			String stm = "SELECT id, parent_id, name, icon_id, descr, date_created, date_modified, " +
		                 "       user_created, user_modified, date_modified_info, " +
					     "       icon_id_root, icon_id_def, theme_id, cache_type " +
					     "  FROM sections " +
					     " WHERE parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, parentId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				java.util.Date dateTmpModInfo;
				Timestamp timestampMoInf = rs.getTimestamp("date_modified_info");
				if (timestampMoInf != null)  dateTmpModInfo = new java.util.Date(timestampMoInf.getTime());
				else                         dateTmpModInfo = null;
				
				retVal.add(new SectionItem(
	         			rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getString("name"),
	         			rs.getLong("icon_id"), 
	         			(rs.getLong("icon_id") > 0) ? iconGetImageById (rs.getLong("icon_id")) : null,
	         			rs.getString("descr"),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified"),
	         			dateTmpModInfo,
						rs.getLong("icon_id_root"),
	         			rs.getLong("icon_id_def"),
	         			rs.getLong("theme_id"),
	         			rs.getInt("cache_type")));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении списка подразделов.");
    	}
		
		return retVal;
	}
	
	/**
	 * Меняет у текущего раздела родителя (перемещение раздела по дереву)
	 * @param currentId
	 * @param targetId
	 */
	public void sectionMove (long currentId, long targetId) {
		PreparedStatement pst = null;
		String stm;
	
		try {
			stm = 	  "UPDATE sections " +
					  "   SET parent_id = ?, date_modified = now() " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setLong (1, targetId);
			pst.setLong (2, currentId);
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при перемещении раздела.");
		}
	}
	
	/**
	 * Выдает следующий Id для добавления нового раздела
	 */
	public long sectionNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_sections');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_sections');");
    	}
		
		return retVal;
	}
	
	/**
	 * Раздел. Изменение.
	 */
	public void sectionUpdate (SectionItem p) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = 	  "UPDATE sections " +
					  "   SET name = ?, icon_id = ?, descr = ?, "+
					  "       date_modified = now(), user_modified = \"current_user\"(), " +
					  "       icon_id_root = ?, icon_id_def = ?, theme_id = ?, cache_type = ? " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getName());
			pst.setLong  (2, p.getIconId());
			pst.setString(3, p.getDescr());
			pst.setLong  (4, p.getIconIdRoot());
            pst.setLong  (5, p.getIconIdDef());
            pst.setLong  (6, p.getThemeId());
            pst.setInt   (7, p.getCacheType());
			pst.setLong  (8, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении раздела.");
		}
	}
	
	/**
	 * Раздел. Обновление даты последней модификации инфо блоков.
	 */
	public void sectionUpdateDateModifiedInfo (long sectionId) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = 	  "UPDATE sections " +
					  "   SET date_modified_info = now() " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении раздела (sectionUpdateDateModifiedInfo).");
		}
	}
	
	/**
	 * Установки. Получаем значение по алиасу
	 */
	public String settingsGetValue (String alias) {
		String retVal = null;
		
		try {
			String stm = "SELECT value " +
				         "  FROM settings " +
				         " WHERE alias = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString (1, alias);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getString("value");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("settingsGetValue : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Установки. Проверяем наличие установки по алиасу.
	 */
	public boolean settingsIsPresent (String alias) {
		boolean retVal = false;
		
		try {
			String stm = "SELECT count(*) CountR FROM settings where alias = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString (1, alias);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			if (rs.getInt("CountR") > 0)  retVal = true;
			else                          retVal = false;

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed (settingsIsPresent)");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Установки. Сохраняем установку (добавляем или изменяем). Поиск существующей установки делается по алиасу.
	 * @param
	 */
	public void settingsSave (String alias, String section, String subject, String name, String value, String descr) {
		PreparedStatement pst = null;
		
		if (! settingsIsPresent(alias)) {            // INSERT
			try {
	            String stm = "INSERT INTO settings (id, alias, section, subject, name, value, descr) " +
	            			 "VALUES(nextval('seq_settings'), ?, ?, ?, ?, ?, ?)";
	            pst = con.prepareStatement(stm);
	            pst.setString(1, alias);
	            pst.setString(2, section);
	            pst.setString(3, subject);
	            pst.setString(4, name);
	            pst.setString(5, value);
	            pst.setString(6, descr);
	            
	            pst.executeUpdate();
	            pst.close();
	        } catch (SQLException ex) {
	            //Logger lgr = Logger.getLogger(Prepared.class.getName());
	            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при добавлении новой установки(настройки).");
			}
		} else {                                     // UPDATE
			try {
				String stm = "UPDATE settings " +
						     "   SET value = ?, descr = ?, "+
						     "       date_modified = now(), user_modified = \"current_user\"() " +
					         " WHERE alias = ? " +
					         ";";
				pst = con.prepareStatement(stm);
				pst.setString(1, value);
				pst.setString(2, descr);
				pst.setString(3, alias);
				
				pst.executeUpdate();
	            pst.close();
			} catch (SQLException ex) {
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при изменении установки.");
			}
		}
	}
	
	/**
	 * Директория Файлов для шаблонов. Добавление новой.
	 * Додається без боді.
	 */
	public void templateFileAdd (TemplateFileItem i) {
		PreparedStatement pst = null;
		
		try {
			if ((i.getType() == 1) || (i.getType() == 11)) {
				String stm = "INSERT INTO template_files (id, parent_id, theme_id, type, file_type, file_name, descr) " + 
      			         "VALUES(?, ?, ?, ?, ?, ?, ?)";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, i.getId());
				pst.setLong  (2, i.getParentId());
				pst.setLong  (3, i.getThemeId());
				pst.setInt   (4, i.getType());
				pst.setInt   (5, i.getFileType());
				pst.setString(6, i.getFileName());
				pst.setString(7, i.getDescr());
			
				pst.executeUpdate();
				pst.close();
			} else {
				ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			             "Ошибка при добавлении новой директории файлов шаблонов : тип " + i.getType() + " не определен.");
			}
		} catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении новой директории файлов шаблонов.");
		}
	}
	
	/**
	 * Файл для шаблонов. Добавление нового.
	 */
	public void templateFileAdd (TemplateFileItem i, String fileName) {
		PreparedStatement pst = null;
		
		try {
			if (i.getFileType() == TemplateFileItem.FILE_TYPE_TEXT) {          // text file
				String stm = "insert into template_files (id, parent_id, theme_id, type, file_type, file_name, descr, body) " +
						     "values (?, ?, ?, ?, ?, ?, ?, ?)";
				pst = con.prepareStatement(stm);
	            pst.setLong  (1, i.getId());
	            pst.setLong  (2, i.getParentId());
	            pst.setLong  (3, i.getThemeId());
	            pst.setInt   (4, i.getType());
	            pst.setInt   (5, i.getFileType());
	            pst.setString(6, i.getFileName());
	            pst.setString(7, i.getDescr());
				pst.setString(8, i.getBody());
				
				pst.executeUpdate();
	            pst.close();
			} else if (i.getFileType() == TemplateFileItem.FILE_TYPE_IMAGE) {   // picture file
				String stm = "INSERT INTO template_files (id, parent_id, theme_id, type, file_type, file_name, descr, body_bin) " + 
      			         	 "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, i.getId());
	            pst.setLong  (2, i.getParentId());
	            pst.setLong  (3, i.getThemeId());
	            pst.setInt   (4, i.getType());
	            pst.setInt   (5, i.getFileType());
	            pst.setString(6, i.getFileName());
	            pst.setString(7, i.getDescr());
           
				try {
					File file = new File(fileName);
					FileInputStream fis = new FileInputStream(file);
					pst.setBinaryStream(8, fis, (int)file.length());
					
					pst.executeUpdate();
	           		pst.close();
	           		fis.close();
				} catch (FileNotFoundException ex) {
					ShowAppMsg.showAlert("ERROR", "Добавление файла для шаблона", 
										 "Файл "+ fileName +" не найден", "Добавление файла в БД прервано.");
				} catch (IOException ex) {
					ShowAppMsg.showAlert("ERROR", "Добавление файла для шаблона", 
							 "Не получается прочитать файл "+ fileName, "Добавление файла в БД прервано.");
				} finally {
					pst.close();
				}
			}
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового файла шаблонов.");
		}
	}
	
	/**
	 * Файл (или директория) для шаблона. Получение информации по id
	 */
	public TemplateFileItem templateFileGetById (long id) {
		TemplateFileItem retVal = null;
		Image isImage = null;
	
		try {
			String stm = "SELECT id, parent_id, theme_id, type, file_type, file_name, descr, body, body_bin, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_files " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			if (rs.getInt("file_type") == TemplateFileItem.FILE_TYPE_IMAGE) 
				isImage = new Image(new ByteArrayInputStream(rs.getBytes("body_bin")));
			
			retVal = new TemplateFileItem (
					rs.getLong("id"), 
					rs.getLong("parent_id"),
         			rs.getLong("theme_id"),
         			rs.getInt("type"),
         			rs.getInt("file_type"),
         			rs.getString("file_name"),
         			rs.getString("descr"),
         			rs.getString("body"),
         			isImage,
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("get templateFile info : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает true, если в указанной директорії є файл (чи директорія) с таким именем.
	 */
	public boolean templateFileIsExistNameInDir (
			long curFileId, long parentId, long themeId, int type, String fileName) {
		boolean retVal = true;
		
		String subQuery = (type < 10) ? " and f.type < 10 " : " and f.type >= 10 "; 
	
		try {
			String stm = "select count(*) CountR "+
					     "  FROM template_files f "+
					     " where f.id <> ? "+
					     "   and f.parent_id = ? "+
					     "   and f.theme_id = ? "+
					     //"   and f.type = ? "+
					     subQuery +
					     "   and upper(f.file_name) = upper(?) ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, curFileId);
			pst.setLong  (2, parentId);
			pst.setLong  (3, themeId);
			//pst.setInt   (4, type);
			pst.setString(4, fileName);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = (rs.getInt("CountR") > 0) ? true : false;

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateFileIsExistNameInDir()");
    		e.printStackTrace();
    	}
		return retVal;
	}
	
	/**
	 * Возващает список файлов и директорий по id родительской директории. 
	 * @param parentId
	 * @return
	 */
	public List<TemplateSimpleItem> templateFileListByParent (TemplateSimpleItem parentItem) {
		List<TemplateSimpleItem> retVal = new ArrayList<TemplateSimpleItem>();
		PreparedStatement pst = null;
	
		try {
			String subSql = "";
			if (parentItem.getId() == 0) {
				switch (parentItem.getTypeItem()) {
				case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
					subSql = " and type < 10 ";
					break;
				case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
					subSql = " and type >= 10 ";
					break;
				default :
					subSql = " and type < 10 ";    // что то присвоили на всякий случай
				}
			}
			
			String stm = "select id, theme_id, type, file_type, file_name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " + 
					     "  from template_files " +
					     " where parent_id = ? " +
					     "   and theme_id = ? " +
					     subSql; 
			pst = con.prepareStatement(stm);
			pst.setLong (1, parentItem.getId());
			pst.setLong (2, parentItem.getThemeId());
				
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				TemplateFileItem tfi = new TemplateFileItem (
						rs.getLong("id"), 
						parentItem.getId(),
	         			rs.getLong("theme_id"),
	         			rs.getInt("type"),
	         			rs.getInt("file_type"),
	         			rs.getString("file_name"),
	         			rs.getString("descr"),
	         			"",
	         			null,
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
				retVal.add(tfi);
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateFileListByParent("+parentItem.getId()+")");
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список під-директорій і файлів для вказаної директорії.
	 * Элементы списка типа TemplateFileItem
	 * type : 0 - обов'язкові файли з їх директоріями ; 10 - не обов'язкові
	 */
	public List<TemplateFileItem> templateFileListByType (long parentId, long themeId, int type) {
		List<TemplateFileItem> retVal = new ArrayList<TemplateFileItem>();
		Image isImage = null;
	
		try {
			String subSql = "";
			if (parentId == 0) {
				switch (type) {
				case 0 :
					subSql = " and type < 10 ";
					break;
				case 10 :
					subSql = " and type >= 10 ";
					break;
				default :
					subSql = " and type < 10 ";    // что то присвоили на всякий случай
				}
			}
			
			String stm = 
					 "select id, type, file_type, file_name, descr, body, body_bin, " +
					 "       date_created, date_modified, user_created, user_modified " + 
				     "  from template_files " +
				     " where parent_id = ? " +
				     "   and theme_id = ? " +
				     subSql +
				     " order by type desc, file_name ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong(1, parentId);
			pst.setLong(2, themeId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				if (rs.getInt("file_type") == TemplateFileItem.FILE_TYPE_IMAGE) 
					isImage = new Image(new ByteArrayInputStream(rs.getBytes("body_bin")));
				
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new TemplateFileItem(
						rs.getLong("id"),
						parentId,
						themeId,
	         			rs.getInt("type"),
	         			rs.getInt("file_type"),
	         			rs.getString("file_name"),
	         			rs.getString("descr"),
	         			rs.getString("body"),
	         			isImage,
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateFileListByType()");
    	}
	
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового файла (или директории) для шаблонов
	 */
	public long templateFileNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_template_files');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_template_files');");
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает цепочку имен директорій от указанного файла(директорії) до самого верхнего родителя.
	 * withFileName - шлях з кінцевим іменем файла (директорії) чи без 
	 */
	public String templateFileGetPathName (long fileId, String delimiter, boolean withFileName) {
		String retVal = "";
		
		try {
			String stm = "SELECT template_file_get_pathname(?,?,?) AS path_name ;";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, fileId);
			pst.setString(2, delimiter);
			pst.setInt   (3, ((withFileName) ? 0 : 1));
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			retVal = rs.getString("path_name");
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении пути ( templateFileGetPathName() ).");
    	}
		
		return retVal;
	}
	
	/**
	 * Обновление информации директории файлов для шаблонов
	 */
	public void templateFileUpdate (TemplateFileItem tf) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			if ((tf.getType() == 1) || (tf.getType() == 11)) {
				stm = 	"UPDATE template_files " +
						"   SET parent_id = ?, theme_id = ?, type = ?, file_type = ?, " +
						"       file_name = ?, descr = ?, date_modified = now(), user_modified = \"current_user\"() " +
						" WHERE id = ? " +
						";";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, tf.getParentId());
				pst.setLong  (2, tf.getThemeId());
				pst.setInt   (3, tf.getType());
				pst.setInt   (4, tf.getFileType());
				pst.setString(5, tf.getFileName());
				pst.setString(6, tf.getDescr());
				pst.setLong  (7, tf.getId());
				
				pst.executeUpdate();
				pst.close();
			} else {
				ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			             "Ошибка при обновлении директории файлов шаблонов : тип " + tf.getType() + " не определен.");
			}
		} catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при обновлении директории файлов шаблонов.");
		}
	}
	
	/**
	 * Обновление информации файла для шаблонов
	 */
	public void templateFileUpdate (TemplateFileItem fi, String fileNameImage) {
		PreparedStatement pst = null;
		String stm;
		
		if ((fi.getType() != 0) && (fi.getType() != 10)) {
			ShowAppMsg.showAlert("WARNING", "db warning", "Ошибка при работе с базой данных", 
		             "Ошибка при обновлении файлов шаблонов : тип " + fi.getType() + " не определен.");
			return;
		}
	
		if (fi.getFileType() == TemplateFileItem.SUBTYPE_FILE_TEXT) {              // обновляем с текстом
			try {
				stm = 	"update template_files " +
						"   set file_type = ?, file_name = ?, descr = ?, body = ?, " +
						"       date_modified = now(), user_modified = \"current_user\"() " +
						" where id = ? " +
						";";
				pst = con.prepareStatement(stm);
				pst.setInt   (1, fi.getFileType());
				pst.setString(2, fi.getFileName());
				pst.setString(3, fi.getDescr());
				pst.setString(4, fi.getBody());
				pst.setLong  (5, fi.getId());
				
				pst.executeUpdate();
	            pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при обновлении файла для шаблонов (с текстом).");
			}
		} else {
			if (fileNameImage.equals("") || (fileNameImage == null)) {       // обновляем без содержимого файла картинки
				try {
					stm = 	"update template_files " +
							"   set file_type = ?, file_name = ?, descr = ?, " +
							"       date_modified = now(), user_modified = \"current_user\"() " +
							" where id = ? " +
							";";
					pst = con.prepareStatement(stm);
					pst.setInt   (1, fi.getFileType());
					pst.setString(2, fi.getFileName());
					pst.setString(3, fi.getDescr());
					pst.setLong  (4, fi.getId());
					
					pst.executeUpdate();
		            pst.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
		        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
							             "Ошибка при обновлении файла для шаблонов (без картинки).");
				}
			} else {
				// обновляем с картинкой
				try {
					stm = 	"update template_files " +
							"   set file_type = ?, file_name = ?, descr = ?, body_bin = ?, " +
							"       date_modified = now(), user_modified = \"current_user\"() " +
							" where id = ? " +
							";";
					pst = con.prepareStatement(stm);
					pst.setInt   (1, fi.getFileType());
					pst.setString(2, fi.getFileName());
					pst.setString(3, fi.getDescr());
					
					File file = new File(fileNameImage);
		            FileInputStream fis = new FileInputStream(file);
		            pst.setBinaryStream(4, fis, (int)file.length());
					
		            pst.setLong  (5, fi.getId());
					
					pst.executeUpdate();
		            pst.close();
		            fis.close();
				} catch (SQLException ex) {
		            //Logger lgr = Logger.getLogger(Prepared.class.getName());
		            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
		        	ex.printStackTrace();
		        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
							             "Ошибка при обновлении файла для шаблонов (с картинкой).");
		        } catch (IOException e) {
					e.printStackTrace();
					ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
				             "Ошибка при изменении картинки для шаблонов.");
				}
			}
		}
	}
	
	/**
	 * Стиль шаблонов. Добавление нового.
	 */
	public void templateStyleAdd (TemplateStyleItem i) {
		PreparedStatement pst = null;
		
		try {
            String stm = "INSERT INTO template_style (id, parent_id, type, infotype_id, name, descr, tag) " + 
            			 "VALUES(?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getParentId());
            pst.setLong  (3, i.getType());
            pst.setLong  (4, i.getInfoTypeId());
            pst.setString(5, i.getName());
            pst.setString(6, i.getDescr());
            pst.setString(7, i.getTag());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового стиля шаблона.");
		}
	}
	
	/**
	 * Стиль шаблонов. Получение информации по id
	 */
	public TemplateStyleItem templateStyleGet (long id) {
		TemplateStyleItem retVal = null;
	
		try {
			String stm = "SELECT id, parent_id, type, infotype_id, name, descr, tag, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_style " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new TemplateStyleItem (
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getInt("type"),
         			rs.getLong("infotype_id"),
         			rs.getString("name"),
         			rs.getString("descr"),
         			rs.getString("tag"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("infoTypeStyleGet : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateStyleGet ("+id+")", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает стиль по умолчанию в указанной теме для указанного типа инфо блока.
	 * Если такого стиля нет, возвращается null.
	 */
	public TemplateStyleItem templateStyleGetDefault (long themeId, long infoTypeId) {
		long styleId = -1;
	
		try {
			String stm = "SELECT TemplateStyle_getIdDefault(?,?,3) AS id ;";
     		PreparedStatement pst = con.prepareStatement(stm);
	    	pst.setLong  (1, themeId);
		    pst.setLong  (2, infoTypeId);
		    ResultSet rs = pst.executeQuery();
			
		    rs.next();
		    styleId = rs.getLong("id");
		    
		    rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , templateStyleGetDefault ("+themeId+","+infoTypeId+")", 
		            e.getMessage());
    	}
		
		return templateStyleGet(styleId);
	}
	
	/**
	 * Возвращает дату установки признака По умолчанию на стилі
	 */
	public java.util.Date templateStyleGetDefaultDateModified (long themeId, long templateStyleId) {
		java.util.Date retVal = null;
		
		try {
			String stm = "SELECT cs.date_modified " +
				         "  FROM current_style cs " + 
                         " WHERE cs.template_style_id = ? " +
				         "   AND cs.theme_id = ? " +
                         "   AND cs.\"user\" = \"current_user\"() " +
                         "   AND cs.flag = 0 " +
                         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, templateStyleId);
			pst.setLong (2, themeId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  retVal = new java.util.Date(timestampMo.getTime());
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , templateStyleGetDefaultDateModified ("+templateStyleId+")", 
		            e.getMessage());
    	}
		return retVal;
	}
	//TODO templateStyleGetDefaultDateModified
	
	/**
	 * Проверяем, является ли указанный стиль дефолтным для темы и пользователя
	 */
	public boolean templateStyleIsDefault (long themeId, long templateStyleId) { 
		boolean retVal = false;
	
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM current_style " +
				         " WHERE theme_id = ? " +
				         "   AND template_style_id = ? " +
				         "   AND \"user\" = \"current_user\"() " +
                         "   AND flag = 0 " +
				         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, templateStyleId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = (rs.getLong("CountR") > 0) ? true : false;
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("count templates : execute query Failed");
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Возвращает список стилей и директорий родительской директории стилей или типа инфоблока. 
	 * @param parentId
	 * @return
	 */
	public List<TemplateSimpleItem> templateStyleListByParent (TemplateSimpleItem parentItem) {
		List<TemplateSimpleItem> retVal = new ArrayList<TemplateSimpleItem>();
		PreparedStatement pst = null;
	
		try {
			String subSql = "";
			if (parentItem.getId() == 0) {
				if (parentItem.getSubtypeItem() < 10) {
					subSql = " and s.type < 10 ";
				} else {
					subSql = " and s.type >= 10 ";
				}
			}
			
			String stm = "select s.id, s.type, s.name, s.descr, " +
						 "       coalesce(t.template_id,0) as flag2, "+
	                 	 "       s.date_created, s.date_modified, s.user_created, s.user_modified " + 
	                 	 "  from template_style s " +
	                 	 "  left join template_style_link t     on t.style_id = s.id "+
	                 	 "                                     and t.theme_id = ? "+
	                 	 " where s.parent_id = ? " +
	                 	 "   and s.infotype_id = ? "+
	                 	 subSql;
			
			pst = con.prepareStatement(stm);
			pst.setLong (1, parentItem.getThemeId());
			pst.setLong (2, parentItem.getId());
			pst.setLong (3, parentItem.getFlag());
				
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				int typeItem;
				switch (rs.getInt("type")) {
				case  0 :
				case 10 :
					typeItem = TemplateSimpleItem.TYPE_ITEM_STYLE;
					break;
				case  1 :
				case 11 :
					typeItem = TemplateSimpleItem.TYPE_ITEM_DIR_STYLE;
					break;
				default :
					typeItem = parentItem.getTypeItem();  // что нибудь присвоим
				}
				
				TemplateSimpleItem newItem = new TemplateSimpleItem(
	         			rs.getLong("id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			parentItem.getThemeId(),
	         			typeItem,
	         			rs.getInt("type"),
	         			parentItem.getFlag(),    // info type id
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
				newItem.setFlag2(rs.getLong("flag2"));
				retVal.add(newItem);
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateStyleListByParent("+parentItem.getId()+")");
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового стиля шаблона
	 */
	public long templateStyleNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_template_style');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_template_style');");
    	}
		
		return retVal;
	}
	
	/**
	 * Стиль шаблонов. Изменение.
	 */
	public void templateStyleUpdate (TemplateStyleItem p) {
		PreparedStatement pst;
		String stm;
		
		try {
			stm = 	  "UPDATE template_style " +
					  "   SET name = ?, descr = ?, parent_id = ?, " +
					  "       date_modified = now(), user_modified = \"current_user\"() " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getName());
			pst.setString(2, p.getDescr());
			pst.setLong  (3, p.getParentId());
			pst.setLong  (4, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении стиля шаблонов (templateStyleUpdate).");
		}
	}
	
	/**
	 * Тема для шаблонов. Добавление новой.
	 */
	public void templateThemeAdd (TemplateThemeItem i) {
		PreparedStatement pst = null;
		
		try {
            String stm = "INSERT INTO template_themes (id, name, descr) " + 
            			 "VALUES(?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getName());
            pst.setString(3, i.getDescr());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении новой темы шаблонов.");
		}
	}
	
	/**
	 * Тема для шаблонов. Подсчет количества тем
	 */
	public long templateThemeCount () {
		long retVal = 0;
	
		try {
			String stm = "SELECT count(*) as CountR " +
				         "  FROM template_themes " +
						 ";";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("CountR"); 
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("templateThemeCount : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Тема для шаблонов. Получение информации по id
	 */
	public TemplateThemeItem templateThemeGetById (long id) {
		TemplateThemeItem retVal = null;
	
		try {
			String stm = "SELECT id, name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_themes " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new TemplateThemeItem(
         			rs.getLong("id"), 
         			rs.getString("name"),
         			rs.getString("descr"),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"));
			retVal.setThemeId(retVal.getId());
			retVal.setTypeItem(TemplateSimpleItem.TYPE_ITEM_THEME);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("get templateTheme info : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список тем шаблонов
	 */
	public List<TemplateThemeItem> templateThemesList () {
		List<TemplateThemeItem> retVal = new ArrayList<TemplateThemeItem>();
		
		try {
			String stm = "SELECT id, name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM template_themes ";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new TemplateThemeItem(
	         			rs.getLong("id"), 
	         			rs.getString("name"),
	         			rs.getString("descr"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка получения списка тем (templateThemesList).");
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления новой темы шаблонов
	 */
	public long templateThemeNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_template_themes');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_template_themes');");
    	}
		
		return retVal;
	}
	
	/**
	 * Тема для шаблонов. Изменение.
	 */
	public void templateThemeUpdate (TemplateThemeItem p) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = 	  "UPDATE template_themes " +
					  "   SET name = ?, descr = ?, "+
					  "       date_modified = now(), user_modified = \"current_user\"() " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getName());
			pst.setString(2, p.getDescr());
			pst.setLong  (3, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении темы шаблонов.");
		}
	}
	
	/**
	 * Шаблон. Добавление нового.
	 */
	public void templateAdd (TemplateItem i) {
		PreparedStatement pst = null;
		
		try {
				String stm = "INSERT INTO template (id, parent_id, type, name, descr, body) " + 
           			         "VALUES(?, ?, ?, ?, ?, ?)";
				pst = con.prepareStatement(stm);
	            pst.setLong  (1, i.getId());
	            pst.setLong  (2, i.getParentId());
	            pst.setInt   (3, i.getType());
	            pst.setString(4, i.getName());
	            pst.setString(5, i.getDescr());
				pst.setString(6, i.getBody());
				
				pst.executeUpdate();
	            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового шаблона.");
		}
	}
	
	/**
	 * Шаблон. Получение информации по id
	 */
	public TemplateItem templateGet (long id) {
		TemplateItem retVal = null;
	
		try {
			String stm = "SELECT t.id, t.parent_id, t.type, t.name, t.descr, t.body, "+
					     "       t.date_created, t.date_modified, t.user_created, t.user_modified " +
					     "  FROM template t " +
					     " WHERE t.id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new TemplateItem (
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getInt("type"),
         			rs.getString("name"),
         			rs.getString("descr"),
         			rs.getString("body"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("get template info : execute query Failed");
    		e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateGet ("+ id +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Шаблон. Получение информации по themeId и templateStyleId
	 */
	public TemplateItem templateGet (long themeId, long templateStyleId) {
		TemplateItem retVal = null;
	
		try {
			String stm = "select t.id, t.parent_id, t.type, t.name, t.descr, t.body, " +
					     "       t.date_created, t.date_modified, t.user_created, t.user_modified " +
					     "  from template t " +
					     "  join template_style_link tsl   on tsl.template_id = t.id " + 
					 	 "                                and tsl.style_id = ? " +
					 	 "                                and tsl.theme_id = ? " +
					     ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, templateStyleId);
			pst.setLong (2, themeId);
			ResultSet rs = pst.executeQuery();
			//rs.next();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal = new TemplateItem (
						rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getInt("type"),
						rs.getString("name"),
	         			rs.getString("descr"),
	         			rs.getString("body"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
			}
				
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateGet ("+ themeId +", "+ templateStyleId +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Шаблон. Перевіряємо по themeId и templateStyleId чи існує звязок між стилем та шаблоном і такий шаблон
	 */
	public boolean templateIsLinkPresent (long themeId, long templateStyleId) {
		boolean retVal = false;
	
		try {
			String stm = "select count(*) as cnt " +
				         "  from template t " +
				         "  join template_style_link tsl   on tsl.template_id = t.id " + 
				 	     "                                and tsl.style_id = ? " +
				 	     "                                and tsl.theme_id = ? " +
				         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, templateStyleId);
			pst.setLong (2, themeId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			if (rs.getInt("cnt") > 0) {
				retVal = true; 
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("get template info : execute query Failed");
    		e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateIsLinkPresent ("+ themeId +", "+ templateStyleId +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Шаблон. Перевіряємо по id чи існує такий шаблон (або директорія шаблонів)
	 */
	public boolean templateIsPresent (long id) {
		boolean retVal = false;
	
		try {
			String stm = "SELECT count(*) as cnt " +
					     "  FROM template " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			if (rs.getInt("cnt") > 0) {
				retVal = true; 
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("get template info : execute query Failed");
    		e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateIsPresent ("+ id +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Возващает список шаблонов и директорий шаблонов по id родительской директории. 
	 * @param parentId
	 * @return
	 */
	public List<TemplateSimpleItem> templateListByParent (long parentId) {
		List<TemplateSimpleItem> retVal = new ArrayList<TemplateSimpleItem>();
		PreparedStatement pst = null;
	
		try {
			String stm = "select t.id, t.type, "+ 
				         "       (select count(*) from template_style_link s where s.template_id = t.id) as flag, "+
		                 "       t.name, t.descr, t.date_created, t.date_modified, t.user_created, t.user_modified "+  
	                     "  from template t "+ 
	                     " where t.parent_id = ? ";
			pst = con.prepareStatement(stm);
			pst.setLong (1, parentId);
				
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new TemplateSimpleItem(
	         			rs.getLong("id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			0,
	         			((rs.getInt("type")==0)||(rs.getInt("type")==10)) ? 
	         					TemplateSimpleItem.TYPE_ITEM_TEMPLATE : TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE,
	         			rs.getInt("type"),
	         			rs.getLong("flag"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						));
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateListByParent("+parentId+")");
    		//System.out.println("execute query Failed (infoTypeStyleListByInfoTypeId)");
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового шаблона
	 */
	public long templateNextId () {
		long retVal = -1;
		
		try {
			String stm = "select nextval('seq_template');";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "select nextval('seq_template');");
    	}
		
		return retVal;
	}
	
	/**
	 * Обновление шаблона
	 */
	public void templateUpdate (TemplateItem tip) {
		PreparedStatement pst = null;
		String stm;
	
		try {
			stm = 	"UPDATE template " +
					"   SET name = ?, descr = ?, body = ?, " +
					"       date_modified = now(), user_modified = \"current_user\"() " +
					" WHERE id = ? " +
					";";
			pst = con.prepareStatement(stm);
			pst.setString(1, tip.getName());
			pst.setString(2, tip.getDescr());
			pst.setString(3, tip.getBody());
			pst.setLong  (4, tip.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при обновлении шаблона.");
		}
	}
	
	//TODO OLD TEMPLATE
	/* >>>  OLD TEMPLATE ################################################################### */
	/**
	 * Подсчет количества шаблонов для указанной темы указанного типа.
	 * @param
	 */
	public long templateCount (long themeId, long infoTypeId) {
		long retVal = 0;
	
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM templates " +
				         " WHERE theme_id = ? " +
				         "   AND infotype_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, infoTypeId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("CountR");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("count templates : execute query Failed");
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", e.getMessage());
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Подсчет количества шаблонов для указанного стиля.
	 * @param
	 */
	public long templateCountByInfoTypeStyle (long infoTypeStyleId) {
		long retVal = 0;
	
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM templates " +
				         " WHERE infotype_style_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, infoTypeStyleId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("CountR");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		//System.out.println("count templates : execute query Failed");
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Шаблон. Удаление одного шаблона.
	 * @param
	 */
	public void templateDelete (long id) {
		PreparedStatement pst = null;
	
		try {
			String stm = "DELETE FROM templates WHERE id = ? ;";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	
        	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Подсчет количества файлов для указанной темы, тип файла которых 0 (обязательные файлы).
	 * @param
	 */
	public long templateFileCount (long themeId) {
		long retVal = 0;
	
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM template_required_files " +
				         " WHERE theme_id = ? " +
				         "   AND type = 0 ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("CountR");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("count required_files : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Файл для шаблона. Удаление одного файла.
	 * @param
	 */
	public void templateFileDelete (long id) {
		PreparedStatement pst = null;
	
		try {
			String stm = "DELETE FROM template_required_files WHERE id = ? ;";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	
        	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Файл для шаблона. Получение тектового содержимого
	 */
	public String templateFileGetBody (long id) {
		String retVal = null;
	
		try {
			String stm = "SELECT body " +
				         "  FROM template_required_files " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = new String (rs.getString("body"));
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("templateFileGetBody : execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает типизированный файл по для указанной темы по типу.
	 */
	public TemplateRequiredFileItem templateFileGetByType(long themeId, int type) {
		InputStream isImage = null;
		TemplateRequiredFileItem retVal = null;
	
		try {
			// find record
			String stm = "SELECT count(id) as CountR " +
					     "  FROM template_required_files " +
					     " WHERE theme_id = ? " +
					     "   AND type = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong(1, themeId);
			pst.setInt (2, type);
			ResultSet rs = pst.executeQuery();
			rs.next();

			if (rs.getInt("CountR") <= 0) {
				rs.close();
	            pst.close();
				return null;
			}
			
			rs.close();
            pst.close();
			
			// read record
            String stm2= "SELECT id, theme_id, file_name, descr, body, body_bin, type, file_type, " +
            			 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_required_files " +
				         " WHERE theme_id = ? " +
				         "   AND type = ?";
            PreparedStatement pst2 = con.prepareStatement(stm2);
            pst2.setLong(1, themeId);
            pst2.setInt (2, type);
            ResultSet rs2 = pst2.executeQuery();
            rs2.next();
			
            if (rs2.getInt("file_type") == TemplateRequiredFileItem.FILETYPEEXT_IMAGE) {
            	isImage = new ByteArrayInputStream(rs.getBytes("image"));
            }
            
            java.util.Date dateTmpCre;
			Timestamp timestampCr = rs2.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs2.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
            
            retVal = new TemplateRequiredFileItem(
         			rs2.getLong("id"), 
         			rs2.getLong("theme_id"),
         			rs2.getString("file_name"),
         			rs2.getString("descr"),
         			rs2.getString("body"),
         			(rs2.getInt("file_type") == TemplateRequiredFileItem.FILETYPEEXT_IMAGE) ? new Image(isImage) : null,
					rs2.getInt("type"),
					rs2.getInt("file_type"),
					dateTmpCre, 
         			dateTmpMod,
         			rs2.getString("user_created"),
         			rs2.getString("user_modified")
            		);
			
            rs2.close();
            pst2.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Файл для шаблона. Получение информации по themeId и fileName
	 */
	public TemplateRequiredFileItem templateFileGet (long themeId, String fileName) {
		TemplateRequiredFileItem retVal = null;
		Image isImage = null;
	
		try {
			String stm = "SELECT id, theme_id, file_name, descr, type, file_type, body, body_bin, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_required_files " +
				         " WHERE theme_id = ? " +
				         "   AND file_name = ? ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setString(2, fileName);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			if (rs.getInt("file_type") == TemplateRequiredFileItem.FILETYPEEXT_IMAGE) 
				isImage = new Image(new ByteArrayInputStream(rs.getBytes("body_bin")));
			
			retVal = new TemplateRequiredFileItem (
					rs.getLong("id"), 
         			rs.getLong("theme_id"),
         			rs.getString("file_name"),
         			rs.getString("descr"),
         			rs.getString("body"),
         			isImage,
         			rs.getInt("type"),
         			rs.getInt("file_type"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("templateFileGet (long themeId, String fileName) : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Файлы для шаблонов. Удаление всех файлов указанной темы.
	 * @param
	 */
	public void templateFilesDelete (long themeId) {
		PreparedStatement pst = null;
	
		try {
			String stm = "DELETE FROM template_required_files WHERE theme_id = ? ;";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	
        	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Шаблон. Получение информации по themeId и infoTypeStyleId
	 */
	public app.model.business.templates_old.TemplateItem templateGet__old (long themeId, long infoTypeStyleId) {
		app.model.business.templates_old.TemplateItem retVal = null;
	
		try {
			String stm = "SELECT id, theme_id, infotype_id, infotype_style_id, name, file_name, descr, body, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM templates " +
				         " WHERE theme_id = ? " +
				         "   AND infotype_style_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, infoTypeStyleId);
			ResultSet rs = pst.executeQuery();
			//rs.next();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal = new app.model.business.templates_old.TemplateItem (
						rs.getLong("id"), 
	         			rs.getLong("theme_id"),
	         			rs.getLong("infotype_id"),
	         			rs.getLong("infotype_style_id"),
	         			rs.getString("name"),
	         			rs.getString("file_name"),
	         			rs.getString("descr"),
	         			rs.getString("body"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
			}
				
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("templateGetByInfoTypeStyleId : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Шаблон. Получение информации по themeId и InfoHeaderItem
	 */
	public app.model.business.templates_old.TemplateItem templateGet (long themeId, InfoHeaderItem infoHeader) {
		long infoTypeStyleId = 0;
		app.model.business.templates_old.TemplateItem retVal = null;
		
		if (infoHeader.getInfoTypeStyleId() <= 0) {
			infoTypeStyleId = infoTypeStyleGetDefault(themeId, infoHeader.getInfoTypeId()).getId();
		} else {
			infoTypeStyleId = infoHeader.getInfoTypeStyleId();
		}
		
		retVal = templateGet__old (themeId, infoTypeStyleId);
		
		return retVal;
	}
	
	/**
	 * Возвращает список шаблонов для показа
	 */
	public List<app.model.business.templates_old.TemplateItem> templateList (long themeId, long infoTypeId) {
		List<app.model.business.templates_old.TemplateItem> retVal = new ArrayList<app.model.business.templates_old.TemplateItem>();
	
		try {
			String stm = "SELECT id, theme_id, infotype_id, infotype_style_id, name, file_name, descr, body, " +
						 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM templates " +
					     " WHERE theme_id = ? " +
				         "   AND infotype_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong(1, themeId);
			pst.setLong (2, infoTypeId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new app.model.business.templates_old.TemplateItem(
	         			rs.getLong("id"),
	         			rs.getLong("theme_id"),
	         			rs.getLong("infotype_id"),
	         			rs.getLong("infotype_style_id"),
	         			rs.getString("name"),
	         			rs.getString("file_name"),
	         			//rs.getString("descr") +" ("+ rs.getString("file_name") +")",
	         			rs.getString("descr"),
	         			rs.getString("body"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
	
		return retVal;
	}
	
	/**
	 * Шаблоны. Удаление всех шаблонов указанной темы.
	 * @param
	 */
	public void templatesDelete (long themeId) {
		PreparedStatement pst = null;

		try {
			String stm = "DELETE FROM templates WHERE theme_id = ? ;";
	        pst = con.prepareStatement(stm);
	        pst.setLong  (1, themeId);

	        pst.executeUpdate();
	        pst.close();
	    } catch (SQLException ex) {
	        //Logger lgr = Logger.getLogger(Prepared.class.getName());
	        //lgr.log(Level.SEVERE, ex.getMessage(), ex);
	    	ex.printStackTrace();
	    	
	    	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
	    	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             ex.getMessage());
	    }
	}
	
	/**
	 * Тема для шаблонов. Удаление темы.
	 */
	public void templateThemeDelete (long id) {
		PreparedStatement pst = null;
	
		try {
            String stm = "DELETE FROM template_themes WHERE id = ? ;"; 
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
            //Logger lgr = Logger.getLogger(Prepared.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при удалении темы шаблонов.");
		}
	}
	/* <<<  OLD TEMPLATE ################################################################### */	
	//TODO OLD TEMPLATE
}