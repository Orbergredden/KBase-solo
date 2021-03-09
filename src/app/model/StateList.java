package app.model;

import app.lib.ShowAppMsg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Класс содержит список состояний элементов интерфейса программы
 * @author Igor Makarevich
 */
public class StateList {
    /**
     * Наблюдаемый Список состояний
     */
    public ObservableList<StateItem> list = FXCollections.observableArrayList();

    private File file;

    /**
     * Constructor
     */
    public StateList() {
        file = new File("StateAppCurrent.xml");
    }

    /**
     * Constructor
     */
    public StateList (String fileName) {
        file = new File(fileName);
    }

    /**
     * Добавляем конфигурационный параметр в список
     * и возвращает на него ссылку
     */
    public StateItem add (String name, String params, StateList subItems) {
        StateItem si = new StateItem(name, params, subItems);
        this.list.add(si);
        return si;
    }

    /**
     * Загружает информацию из файла.
     * Текущая информация в списке будет заменена.
     */
    public void loadFromFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(StateList_Wrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Чтение XML из файла и демаршализация.
            StateList_Wrapper wrapper = (StateList_Wrapper) um.unmarshal(file);

            list.clear();
            list.addAll(wrapper.getList());
        } catch (Exception e) {                           // catches ANY exception
            e.printStackTrace();
            ShowAppMsg.showAlert(
                    "ERROR",
                    "Ошибка",
                    "Невозможно загрузить данные из файла " + file.getPath(),
                    e.getMessage());
        }
    }
    //TODO

    /**
     * Сохраняет список в файле.
     */
    public void saveToFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(StateList_Wrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Обёртываем наши данные
            StateList_Wrapper wrapper = new StateList_Wrapper();
            wrapper.setList(list);

            // Маршаллируем и сохраняем XML в файл.
            m.marshal(wrapper, file);
        } catch (Exception e) {                             // catches ANY exception
            ShowAppMsg.showAlert(
                    "ERROR",
                    "Ошибка",
                    "Невозможно сохранить данные",
                    "Невозможно сохранить данные в файл " + file.getPath());
        }
    }
}
