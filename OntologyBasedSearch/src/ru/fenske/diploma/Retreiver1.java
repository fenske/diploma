package ru.fenske.diploma;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Retreiver1 {
	
	private static Retreiver1 instance;
	
	private OWLOntologyManager ontologyManager;	
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private String strOntologyIRI;
	
	private List<OWLOntology> importedOntologies;
	private List<OWLClass> domainClasses;
	private List<OWLNamedIndividual> domainInstances;
	
	private Retreiver1(String strOntologyIRI) throws OWLOntologyCreationException {
		ontologyManager = OWLManager.createOWLOntologyManager();
		ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create(new File(strOntologyIRI)));
		dataFactory = OWLManager.getOWLDataFactory();
		importedOntologies = new ArrayList<OWLOntology>();
		importedOntologies.addAll(ontology.getImports());
		domainClasses = new ArrayList<OWLClass>();
		domainInstances = new ArrayList<OWLNamedIndividual>();
		for (OWLOntology o : importedOntologies) {
			domainClasses.addAll(o.getClassesInSignature());
			domainInstances.addAll(o.getIndividualsInSignature());
		}
	}
	
	public void retreive(String query) {
		List<String> queryList = Arrays.asList(query.toLowerCase().split("\\W"));
		
		for (String s : queryList) {
			
		}
		
	}
	
	public List<OWLEntity> getOWLEntities(List<String> fragments, String owlClass) {
		StringBuilder result = new StringBuilder();
		for (String s : fragments) {
			StringBuilder temp = new StringBuilder(s);			
			temp.setCharAt(0, Character.toUpperCase(s.charAt(0)));
			result.append(temp);
		}		
		List<OWLEntity> necessaryEntities = new ArrayList<OWLEntity>();				
		for (OWLOntology o : ontology.getImports()) {		
			Set<OWLEntity> entitySet = o.getEntitiesInSignature(IRI.create(o.getOntologyID().getOntologyIRI() + "#" + result.toString()));			
			if (entitySet.size() != 0) {
				OWLEntity entity = entitySet.iterator().next();
				if (entity.getEntityType().getName().equals(owlClass)) {
					necessaryEntities.add(entity);				
				}
			}			
		}		
		return necessaryEntities;
	}
	
	public static Retreiver1 getInstance() throws OWLOntologyCreationException {
		if (instance == null) {
			instance = new Retreiver1("resources/doc.owl");
		}
		return instance;
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		Retreiver1 r = Retreiver1.getInstance();
		System.out.println(r.getOWLEntities(Arrays.asList("mixed", "fruit"), OWLTypes.OWL_INDIVIDUAL));
	}
}
