package ru.fenske.diploma;

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
	
	private OWLOntologyManager ontologyManager;	
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private String strOntologyIRI;
	
	private List<OWLOntology> importedOntologies;
	private List<OWLClass> domainClasses;
	private List<OWLNamedIndividual> domainInstances;
	
	private Retreiver1(String strOntologyIRI) throws OWLOntologyCreationException {
		ontologyManager = OWLManager.createOWLOntologyManager();
		ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create(strOntologyIRI));
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
			OWLNamedIndividual i = null;
			for (OWLOntology o : importedOntologies) {
//				i = (OWLNamedIndividual)getOWLEntity(ontology, s);
				if (i != null) {
					break;
				}
			}
			if (i == null) {
				continue;
			}
		}
		
	}
	
	private List<OWLEntity> getOWLEntities(String fragment, String owlClass) {
		List<OWLEntity> necessaryEntities = new ArrayList<OWLEntity>();				
		for (OWLOntology o : importedOntologies) {		
			Set<OWLEntity> entitySet = o.getEntitiesInSignature(IRI.create(o.getOntologyID().getOntologyIRI() + "#" + fragment));			
			if (entitySet.size() != 0) {							
				necessaryEntities.add(entitySet.iterator().next());				
			}			
		}
		return necessaryEntities;
	}
}
