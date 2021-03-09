package app.model;

import app.exceptions.KBase_DublicateConnIdEx;
import app.lib.ShowAppMsg;
import app.model.DBConn_Parameters;
import app.model.DBConnList_Wrapper;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.*;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Класс содержит список настроек подключений к БД.
 * 
 * @author Игорь Макаревич
 */
public class DBConnList_Parameters {
	/**
	 * Наблюдаемый Список соединений (параметров соединений)
	 */
	public ObservableList<DBConn_Parameters> dbConnListParam = FXCollections.observableArrayList();
	
	private File file;
	private String password = "KBase solo";        // пароль для шифрования
	
	/**
	 * Constructor
	 */
	public DBConnList_Parameters () {
		file = new File("DBConnList.xml");
		
		loadFromFile();
	}
	
	/**
	 * Добавляем соединение в список
	 */
	public void add (String connName, String type, String host, String port, String name, String login, String password,
			         boolean autoConn, LocalDate lastConn, int counter,  boolean colorEnable,
					 double colorTRed_A, double colorTGreen_A, double colorTBlue_A, double colorTOpacity_A,
					 double colorBRed_A, double colorBGreen_A, double colorBBlue_A, double colorBOpacity_A,
					 double colorTRed_N, double colorTGreen_N, double colorTBlue_N, double colorTOpacity_N,
					 double colorBRed_N, double colorBGreen_N, double colorBBlue_N, double colorBOpacity_N)
			throws KBase_DublicateConnIdEx {
		//int connId = LocalDate.now().hashCode();
		int connId = LocalDateTime.now().hashCode();
		
		// проходим в цикле по списку и сравниваем названия
		for (int i=0; i<dbConnListParam.size(); i++) {
			if (dbConnListParam.get(i).getConnId() == connId) {
				throw new KBase_DublicateConnIdEx (connId, connName, this);
			}
		}
		
		this.dbConnListParam.add(new DBConn_Parameters (connId, connName, type, host, port, name, login, password, 
				                                        autoConn, lastConn, counter, colorEnable,
				                                        colorTRed_A, colorTGreen_A, colorTBlue_A, colorTOpacity_A,
				                   	                    colorBRed_A, colorBGreen_A, colorBBlue_A, colorBOpacity_A,
				                   	                    colorTRed_N, colorTGreen_N, colorTBlue_N, colorTOpacity_N,
				                   	                    colorBRed_N, colorBGreen_N, colorBBlue_N, colorBOpacity_N));
	}
	
	/**
     * Загружает информацию о конектах из файла.
     * Текущая информация о конектах будет заменена.
     */
    public void loadFromFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(DBConnList_Wrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Чтение XML из файла и демаршализация.
            DBConnList_Wrapper wrapper = (DBConnList_Wrapper) um.unmarshal(file);

            dbConnListParam.clear();
            dbConnListParam.addAll(decryptPasswords(wrapper.getList()));
            //dbConnListParam.addAll(wrapper.getList());
            
        } catch (Exception e) { // catches ANY exception
        	ShowAppMsg.showAlert(
        			"ERROR", 
        			"Ошибка", 
        			"Невозможно загрузить данные", 
        			"Невозможно загрузить данные из файла " + file.getPath());
        }
    }
	
	/**
     * Сохраняет список подключений в файле.
     */
    public void saveToFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(DBConnList_Wrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Обёртываем наши данные
            DBConnList_Wrapper wrapper = new DBConnList_Wrapper();
            wrapper.setList(encryptPasswords(dbConnListParam));

            // Маршаллируем и сохраняем XML в файл.
            m.marshal(wrapper, file);
        } catch (Exception e) { // catches ANY exception
        	ShowAppMsg.showAlert(
        			"ERROR", 
        			"Ошибка", 
        			"Невозможно сохранить данные", 
        			"Невозможно сохранить данные в файл " + file.getPath());
        }
    }

    /**
     * Зашифровывает все пароли в списке соединений и возвращает новый список.
     * @param dbConnListParam
     * @return
     */
	private List<DBConn_Parameters> encryptPasswords(ObservableList<DBConn_Parameters> dbConnListParam) {
		List<DBConn_Parameters> list = cloneList(dbConnListParam);
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

		encryptor.setPassword(password);                       // we HAVE TO set a password
		//encryptor.setAlgorithm("PBEWithMD5AndTripleDES");    // optionally set the algorithm
		
		for (DBConn_Parameters cp : list) {
			//System.out.println(cp.getConnName() + " -- " + cp.getPassword());
			cp.setPassword(encryptor.encrypt(cp.getPassword()));
		}
		
		return list;
	}
	
	/**
	 * Расшифровывает все пароли в списке соединений и возвращает новый список.
	 */
	private List<DBConn_Parameters> decryptPasswords(List<DBConn_Parameters> listSource) {
		List<DBConn_Parameters> list = cloneList(listSource);
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

		encryptor.setPassword(password);                       // we HAVE TO set a password
		//encryptor.setAlgorithm("PBEWithMD5AndTripleDES");    // optionally set the algorithm
		
		for (DBConn_Parameters cp : list) {
			try {
				cp.setPassword(encryptor.decrypt(cp.getPassword()));
			} catch (Exception e) {
				ShowAppMsg.showAlert(
	        			"ERROR", 
	        			"Ошибка", 
	        			"Ошибка расшифровки пароля", 
	        			"Невозможно расшифровать пароль для соединения " + cp.getConnName());
				e.printStackTrace();
				
				cp.setPassword("");
			/*} finally {
				System.out.println("err " + cp.getConnName() + " -- " + cp.getPassword());
				cp.setPassword("");
				System.out.println("err " + cp.getConnName() + " -- " + cp.getPassword());*/
			}
		}
		
		return list;
	}

	/**
	 * Возвращает копию переданного в метод списка.
	 * @param listSource
	 * @return
	 */
	private List<DBConn_Parameters> cloneList(List<DBConn_Parameters> listSource) {
		List<DBConn_Parameters> listTarget = new ArrayList<DBConn_Parameters>();
		
		for (DBConn_Parameters cp : listSource) {
			listTarget.add(new DBConn_Parameters (cp));
		}
				
		return listTarget;
	}
}
