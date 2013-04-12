package com.example.webui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import ru.fenske.diploma.Annotator;
import ru.fenske.diploma.Indexer;
import ru.fenske.diploma.MainComposite;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;


/**
 * Main UI class
 */
@SuppressWarnings("serial")
public class WebuiUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		final MainComposite mainComposite = new MainComposite();
		setContent(mainComposite);
		mainComposite.getButtonSynchronizer().addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				Indexer indexer = null;
				try {
					indexer = Indexer.getInstance();
				} catch (OWLOntologyCreationException e) {
					System.err.println(e);
				}
				if (indexer != null) {					
//					try {
//						indexer.indexDocuments();
//					} catch (OWLOntologyCreationException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (OWLOntologyStorageException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SQLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (ClassNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					Notification.show("Синхронизация с базой данных выполнена успешно");
				} else {
					Notification.show("В процессе синхронизации с базой данных возникли ошибки", Notification.TYPE_ERROR_MESSAGE);
				}
			}
		});
		mainComposite.getButtonAnnotator().addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				Annotator annotator = null;
				try {
					annotator = Annotator.getInstance();
				} catch (OWLOntologyCreationException e) {
					System.err.println(e);
				}
				if (annotator != null) {						
//					try {
//						annotator.annotateDocuments();
//						System.out.println("Well done");
//					} catch (OWLOntologyCreationException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (OWLOntologyStorageException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (URISyntaxException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}	
					Notification.show("Аннотирование выполнено успешно");
				} else {
					Notification.show("В процессе аннотирования возникли ошибки", Notification.TYPE_ERROR_MESSAGE);
				}
				
			}
		});
	}

}