package com.example.databaseui;

import ru.fenske.diploma.DataBaseComposite;
import ru.fenske.diploma.DbUtils;
import ru.fenske.diploma.Document;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Main UI class
 */
@SuppressWarnings("serial")
public class DatabaseUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		final DataBaseComposite composite = new DataBaseComposite();
		Button buttonInsert = composite.getButtonInsert();

		buttonInsert.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Document doc = new Document(null, 
											composite.getNameText().getValue(),
											composite.getAuthorText().getValue(),
											composite.getUriText().getValue());
				try {
					DbUtils.insertDocument(doc);
					composite.getNameText().setValue("");
					composite.getAuthorText().setValue("");
					composite.getUriText().setValue("");
					Notification.show("Документ был добавлен", Notification.TYPE_HUMANIZED_MESSAGE);
				} catch (Exception e) {
					Notification.show("Произошла ошибка", Notification.TYPE_ERROR_MESSAGE);
					e.printStackTrace();
				} 
			}
		});

		setContent(composite);
	}
}