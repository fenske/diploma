package ru.fenske.diploma;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.AutoIRIMapper;


public class Retreiver {

	private static Retreiver instance;
	
	private OWLOntologyManager ontologyManager;	
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private String ontologyIRI;
	
	private Retreiver(String ontologyIRI) throws OWLOntologyCreationException {
		this.ontologyIRI = ontologyIRI;		
		ontologyManager = OWLManager.createOWLOntologyManager();
		ontologyManager.addIRIMapper(new AutoIRIMapper(new File(""), true));		 
		ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create(ontologyIRI));		 
		dataFactory = OWLManager.getOWLDataFactory();
	}
	
	public Map<Double, OWLNamedIndividual> retreiveDocuments(String query) throws OWLOntologyCreationException {
		Set<OWLNamedIndividual> documentSet = new HashSet<OWLNamedIndividual>();
		List<String> queryList = Arrays.asList(query.toLowerCase().split("\\W"));
		OWLObjectProperty cAnnotationRefProperty = OWLUtils.getOntologyObjectProperty(ontology, Fragments.C_ANNOTATION_REF_PROPERTY);
		OWLObjectProperty documentRefProperty = OWLUtils.getOntologyObjectProperty(ontology, Fragments.DOCUMENT_REF_PROPERTY);
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		List<OWLNamedIndividual> queryOntList = new ArrayList<OWLNamedIndividual>();
		for (String s : queryList) {
			OWLNamedIndividual i = OWLUtils.getOntologyInstance(ontology, dataFactory.getOWLThing(), s);
			if (i == null) {
				break;
			} else {
				// TODO : Проверить на принадлежность к атрибутам документа
				queryOntList.add(i);
				Set<OWLNamedIndividual> annotationSet = reasoner.getObjectPropertyValues(i, cAnnotationRefProperty).getFlattened();
				for (OWLNamedIndividual annotation : annotationSet) {
					documentSet.addAll(reasoner.getObjectPropertyValues(annotation, documentRefProperty).getFlattened());
				}
				// Add relashionship links
				Set<OWLClass> typeSet = reasoner.getTypes(i, true).getFlattened();
				for (OWLClass c : typeSet) {
					Set<OWLClass> subclassSet = reasoner.getSubClasses(c, false).getFlattened();
					for (OWLClass subclass : subclassSet) {
						Set<OWLNamedIndividual> iSet = reasoner.getInstances(subclass, true).getFlattened();
						for (OWLNamedIndividual subclassInstance : iSet) {
							Set<OWLNamedIndividual> subclassAnnotationSet = reasoner.getObjectPropertyValues(subclassInstance, documentRefProperty).getFlattened();
							for (OWLNamedIndividual annotation : subclassAnnotationSet) {
								documentSet.addAll(reasoner.getObjectPropertyValues(annotation, documentRefProperty).getFlattened());
							}
						}
					}
				}
				// Add additional links
				// TODO : Исключить те экземпляры, которые относятся к классу Annotation и Document
				Set<OWLNamedIndividual> relatedIndividuals = OWLUtils.getRelatedIndividuals(ontology, i);
				for (OWLNamedIndividual relInd : relatedIndividuals) {
					Set<OWLNamedIndividual> relatedAnnotationSet = reasoner.getObjectPropertyValues(relInd, documentRefProperty).getFlattened();
					for (OWLNamedIndividual annotation : relatedAnnotationSet) {
						documentSet.addAll(reasoner.getObjectPropertyValues(annotation, documentRefProperty).getFlattened());
					}
				}
			}
		}
		//Ranging
		List<OWLNamedIndividual> ontInstances = OWLUtils.getOntologyInstances(ontology, ontologyIRI);		
		boolean[] queryVector = new boolean[ontInstances.size()];
		int matches = 0;
		for (OWLNamedIndividual i : queryOntList) {
			int index = ontInstances.indexOf(i);
			queryVector[index] = true;
			matches++;
		}		
		OWLObjectProperty dAnnotationRefProp = OWLUtils.getOntologyObjectProperty(ontology, Fragments.D_ANNOTATION_REF_PROPERTY);
		OWLDataProperty weightProp = OWLUtils.getOntologyDataProperty(ontology, Fragments.WEIGHT_PROPERTY);
		Map<Double, OWLNamedIndividual> rangedDocMap = new TreeMap<Double, OWLNamedIndividual>(new Comparator<Double>() {
			@Override
		    public int compare(Double o1, Double o2) {
		    	return o2.compareTo(o1);
		    }
		});
		OWLObjectProperty conceptRefProp = OWLUtils.getOntologyObjectProperty(ontology, Fragments.CONCEPT_REF_PROPERTY);
		for (OWLNamedIndividual document : documentSet) {
			Set<OWLNamedIndividual> docAnnotations = reasoner.getObjectPropertyValues(document, dAnnotationRefProp).getFlattened();			
			int[] docVector = new int[ontInstances.size()]; 
			// сформировать вектор документа
			for (OWLNamedIndividual a : docAnnotations) {
				Set<OWLNamedIndividual> relatedConcepts = reasoner.getObjectPropertyValues(a, conceptRefProp).getFlattened();
				for (OWLNamedIndividual i : relatedConcepts) {
					int index = ontInstances.indexOf(i);				
					int weight = 0;
					for (OWLLiteral l : reasoner.getDataPropertyValues(a, weightProp)) {
						weight = Integer.parseInt(l.getLiteral());
					}
					docVector[index] = weight;
				}
			}
			double sim = 0; 
			for (int i = 0; i < ontInstances.size(); i++) {
				if (queryVector[i]) {
					sim += docVector[i];
				}
			}
			double docVecNorm = 0;
			for (int i : docVector) {
				docVecNorm += i*i;
			}
			sim /= Math.sqrt(docVecNorm * matches);
			rangedDocMap.put(sim, document);
		}
		return rangedDocMap;
	}
	
	
	
	public static Retreiver getInstance() throws OWLOntologyCreationException {
		if (instance == null) {
			instance = new Retreiver("file:///D:/Anton/diploma/ontology/doc.owl");
		}
		return instance;
	}	
	
//	public static void main(String[] args) throws OWLOntologyCreationException {
//		Retreiver r = Retreiver.getInstance();
//		//Set<OWLNamedIndividual> set = r.retreiveDocuments("vEAL tUnA");
////		long start = System.currentTimeMillis();
////		Map<Double, OWLNamedIndividual> documents = r.retreiveDocuments("vEAL tUnA");
////		Iterator<Map.Entry<Double, OWLNamedIndividual>> iterator = documents.entrySet().iterator();
////		while (iterator.hasNext()) {
////			Map.Entry<Double, OWLNamedIndividual> pairs = iterator.next();
////			System.out.println(pairs.getValue().getIRI().getFragment());
////		}
////		long stop = System.currentTimeMillis();
////		System.out.println("Total time: " + ((double)stop / (double)start) / 100);
////		for (OWLNamedIndividual i : set) {
////			System.out.println(i.getIRI().getFragment());
////		}
//	}
}
