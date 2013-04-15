package ru.fenske.diploma;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class Annotator {
	
	private static Annotator instance;
	
	private OWLOntologyManager ontologyManager;	
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private String ontologyIRI;
	
	private Annotator(String ontologyIRI) throws OWLOntologyCreationException {
		this.ontologyIRI = ontologyIRI;		
		ontologyManager = OWLManager.createOWLOntologyManager();
		ontologyManager.addIRIMapper(new AutoIRIMapper(new File(""), true));		 
		ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create(ontologyIRI));		 
		dataFactory = OWLManager.getOWLDataFactory();
	}

	/**
	 * Returns sorted list of document's words
	 * 
	 * @param filePath 
	 * @return 
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	private List<String> getWordList(String fileURI) throws IOException, URISyntaxException {
		BufferedReader in = new BufferedReader(new FileReader(new File(new URI(fileURI))));
		StringBuilder sb = new StringBuilder();
		String s;
		while ((s = in.readLine()) != null) {
			sb.append(s.toLowerCase());
		}
		in.close();
		String[] arr = sb.toString().split("\\W");
		StrLen cmp = new StrLen();
		Arrays.sort(arr, cmp);
		List<String> list = Arrays.asList(arr);
		List<String> resultList = new ArrayList<String>();
		for (String ss : list) {
			if (ss.length() > 2) {
				resultList.add(ss);
			}
		}
		return resultList;
	}
	
	/**
	 * Annotates documents
	 * 
	 * @param documentPaths 
	 * @throws IOException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 * @throws URISyntaxException 
	 */
	public void annotateDocuments() throws IOException, OWLOntologyCreationException, OWLOntologyStorageException, URISyntaxException {
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);		
		
		OWLClass docClass = OWLUtils.getOntologyClass(ontology, Fragments.DOCUMENT_CLASS);
		Set<OWLNamedIndividual> docInstances = reasoner.getInstances(docClass, false).getFlattened();
		
		List<Document> documents = new ArrayList<Document>();		
		
		for (OWLNamedIndividual i : docInstances) {
			OWLDataProperty nameProp = OWLUtils.getOntologyDataProperty(ontology, Fragments.NAME_PROPERTY);
			OWLDataProperty authorProp = OWLUtils.getOntologyDataProperty(ontology, Fragments.AUTHOR_PROPERTY);
			OWLDataProperty uriProp = OWLUtils.getOntologyDataProperty(ontology, Fragments.URI_PROPERTY);			
			
			Set<OWLLiteral> propSet = reasoner.getDataPropertyValues(i, nameProp);
			List<OWLLiteral> propList = new ArrayList<OWLLiteral>();			
			propList.addAll(propSet);
			OWLLiteral nameLiteral = propList.get(0);
			
			propSet = reasoner.getDataPropertyValues(i, authorProp);
			propList = new ArrayList<OWLLiteral>();
			propList.addAll(propSet);
			OWLLiteral authorLiteral = propList.get(0);
			
			propSet = reasoner.getDataPropertyValues(i, uriProp);
			propList = new ArrayList<OWLLiteral>();
			propList.addAll(propSet);
			OWLLiteral uriLiteral = propList.get(0);
			
			documents.add(new Document(null, nameLiteral.getLiteral(), authorLiteral.getLiteral(), uriLiteral.getLiteral()));
		}
		
		List<OWLNamedIndividual> ontInstances = OWLUtils.getOntologyInstances(ontology, ontologyIRI);
		List<String> ontNamesInstancesList = new ArrayList<String>();
		for (OWLNamedIndividual ind : ontInstances) {
			ontNamesInstancesList.add(ind.getIRI().getFragment().toLowerCase());
		}
		for (Document doc : documents) {
			List<String> textWords = getWordList(doc.getUri());
			int cur = 0;
			int total = textWords.size();
			for (String word : textWords) {
				cur++;
				int percent = 100*cur/total;
				System.out.println(doc + " has been annotated: " + percent + "%");
				if (ontNamesInstancesList.contains(word)) {
					createAnnotation(word, doc.getName());	
				}
			}
			System.out.println(doc + " well done!");
		}
	}

	/**
	 * Create an annotation
	 * 
	 * @param word 
	 * @throws OWLOntologyStorageException
	 */
	private void createAnnotation(String word, String docName) throws OWLOntologyStorageException {		
		// get required instance of Annotation class				
		OWLNamedIndividual annotationIndividual = OWLUtils.getOntologyAnnotationInstance(ontology, word + Fragments.ANNOTATION_CLASS);
		if (annotationIndividual == null) {
			annotationIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + word + docName + Fragments.ANNOTATION_CLASS));
		}				
		// get weight property
		OWLDataProperty weightProperty = OWLUtils.getOntologyDataProperty(ontology, Fragments.WEIGHT_PROPERTY);
		
		// get required instance of Document class		
		OWLIndividual documentIndividual = OWLUtils.getOntologyDocumentInstance(ontology, docName);
		
		// get required instance of concept 		
		OWLIndividual conceptIndividual = OWLUtils.getOntologyInstance(ontology, dataFactory.getOWLThing(), word);
				
		// get conceptRef property
		OWLObjectProperty conceptRefProperty = OWLUtils.getOntologyObjectProperty(ontology, Fragments.CONCEPT_REF_PROPERTY);
		
		OWLObjectPropertyAssertionAxiom propertyConceptAssertion = 
				dataFactory.getOWLObjectPropertyAssertionAxiom(conceptRefProperty, annotationIndividual, conceptIndividual);
		ontologyManager.addAxiom(ontology, propertyConceptAssertion);
		
		// get documentRef property
		OWLObjectProperty documentRefProperty =  OWLUtils.getOntologyObjectProperty(ontology, Fragments.DOCUMENT_REF_PROPERTY);
		
		OWLObjectPropertyAssertionAxiom propertyDocumentAssertion = 
				dataFactory.getOWLObjectPropertyAssertionAxiom(documentRefProperty, annotationIndividual, documentIndividual);
		ontologyManager.addAxiom(ontology, propertyDocumentAssertion);		
		
		// get cAnnotationRef property
		OWLObjectProperty cAnnotationRefProperty = OWLUtils.getOntologyObjectProperty(ontology, Fragments.C_ANNOTATION_REF_PROPERTY);
		
		OWLObjectPropertyAssertionAxiom cAnnotationAxiom = 
				dataFactory.getOWLObjectPropertyAssertionAxiom(cAnnotationRefProperty, conceptIndividual, annotationIndividual);
		ontologyManager.addAxiom(ontology, cAnnotationAxiom);
		
		// get dAnnotationRef property
		OWLObjectProperty dAnnotationProperty = OWLUtils.getOntologyObjectProperty(ontology, Fragments.D_ANNOTATION_REF_PROPERTY);
		
		OWLObjectPropertyAssertionAxiom dAnnotationAxiom = 
				dataFactory.getOWLObjectPropertyAssertionAxiom(dAnnotationProperty, documentIndividual, annotationIndividual);
		ontologyManager.addAxiom(ontology, dAnnotationAxiom);		
		
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);		
		
		Set<OWLLiteral> set = reasoner.getDataPropertyValues(annotationIndividual, weightProperty);
		
		String strWeightVal = "";			
		int intWeightVal = 0;
		
		for (OWLLiteral l : set) {			
			strWeightVal = l.getLiteral();
		}
				
		if (strWeightVal.equals("")) {
			intWeightVal = 1;
		} else {
			intWeightVal = Integer.parseInt(strWeightVal) + 1;
			Set<OWLIndividualAxiom> conceptAxioms = ontology.getAxioms(annotationIndividual);
			for (OWLIndividualAxiom a : conceptAxioms) {				
				Set<OWLDataProperty> axiomDataProperties = a.getDataPropertiesInSignature();
				for (OWLDataProperty p : axiomDataProperties) {
					if (p.getIRI().getFragment().equals(Fragments.WEIGHT_PROPERTY)) {
						ontologyManager.applyChange(new RemoveAxiom(ontology, a));
					}
				}
			}
		}					
		OWLDataPropertyAssertionAxiom dataPropertyAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(weightProperty, annotationIndividual, intWeightVal);
		ontologyManager.addAxiom(ontology, dataPropertyAssertion);
		
		
		ontologyManager.saveOntology(ontology, IRI.create(ontologyIRI));				
	}	
	
	/**
	 * Sorting comparator
	 * 
	 * @author fenske
	 *
	 */
	private class StrLen implements Comparator<String> {
		public int compare(String o1, String o2) {
			if (o1.length() < o2.length()) {
				return 1;
			} else if (o1.length() > o2.length()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * Returns the annotator
	 * 
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public static Annotator getInstance() throws OWLOntologyCreationException {
		if (instance == null) {
			instance = new Annotator("file:///D:/Soft/web_eclipse/ontology/doc.owl");
		}
		return instance;
	}
			
//	public void temp() throws OWLOntologyCreationException {		
//		List<String> l = getOntologyInstances(ontologyIRI);
//		for (String s : l) {
//			System.out.println(s + " ");
//		}
//	}
//	
//
	public static void main(String[] args) throws Exception {				
		
		Annotator annotator = Annotator.getInstance();
		long start = System.currentTimeMillis();
		
		annotator.annotateDocuments();
		
		long stop = System.currentTimeMillis();		
		double resultTime = (double)((stop - start)) / 1000;
		
		System.out.println("Well done!");
		System.out.println("Working time: " + resultTime);
//		annotator.temp();
		
	}
}
