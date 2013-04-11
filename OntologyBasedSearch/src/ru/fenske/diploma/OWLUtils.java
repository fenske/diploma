package ru.fenske.diploma;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class OWLUtils {

	/**
	 * Returns list of concept instances
	 * 
	 * @param rootDirectory
	 * @param ontologyIRI
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	static List<OWLNamedIndividual> getOntologyInstances(OWLOntology ontology,
			String ontologyIRI) throws OWLOntologyCreationException {
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		List<OWLNamedIndividual> instances = new ArrayList<OWLNamedIndividual>();
		Set<OWLClass> subClasses = ontology.getClassesInSignature(true);
		for (OWLClass c : subClasses) {
			String owlClassName = c.getIRI().getFragment();
			if (owlClassName.equals(Fragments.DOCUMENT_CLASS)
					|| owlClassName.equals(Fragments.ANNOTATION_CLASS)) {
				continue;
			}
			for (OWLNamedIndividual i : reasoner.getInstances(c, true)
					.getFlattened()) {
				instances.add(i);
			}
		}
		return instances;
	}

	/**
	 * Return specific ontology class
	 * 
	 * @param classFragment
	 * @return
	 */
	static OWLClass getOntologyClass(OWLOntology ontology, String classFragment) {
		Set<OWLClass> ontologyClasses = ontology.getClassesInSignature(true);
		OWLClass requiredClass = null;
		for (OWLClass c : ontologyClasses) {
			if (c.getIRI().getFragment().equals(classFragment)) {
				requiredClass = c;
				break;
			}
		}
		return requiredClass;
	}

	/**
	 * Returns specific ontology instance belong to selected class, except
	 * Document class and AnnotationClass
	 * 
	 * @param owlClass
	 * @param instanceFragment
	 * @return
	 */
	static OWLNamedIndividual getOntologyInstance(OWLOntology ontology,
			OWLClassExpression owlClass, String instanceFragment) {
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

		Set<OWLClass> subClasses = ontology.getClassesInSignature(true);
		OWLNamedIndividual requiredInstance = null;
		for (OWLClass c : subClasses) {
			String owlClassName = c.getIRI().getFragment();
			if (owlClassName.equals(Fragments.DOCUMENT_CLASS)
					|| owlClassName.equals(Fragments.ANNOTATION_CLASS)) {
				continue;
			}
			for (OWLNamedIndividual i : reasoner.getInstances(c, true)
					.getFlattened()) {
				if (i.getIRI().getFragment().toLowerCase()
						.equals(instanceFragment)) {
					requiredInstance = i;
					break;
				}
			}
		}
		return requiredInstance;
	}

	/**
	 * Returns specific instance of Document class
	 * 
	 * @param instanceFragment
	 * @return
	 */
	static OWLNamedIndividual getOntologyDocumentInstance(OWLOntology ontology,
			String instanceFragment) {
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

		OWLClass documentClass = getOntologyClass(ontology,
				Fragments.DOCUMENT_CLASS);
		Set<OWLNamedIndividual> documentInstances = reasoner.getInstances(
				documentClass, false).getFlattened();
		OWLNamedIndividual requiredInstance = null;
		for (OWLNamedIndividual i : documentInstances) {
			if (i.getIRI().getFragment().equals(instanceFragment)) {
				requiredInstance = i;
				break;
			}
		}
		return requiredInstance;
	}

	/**
	 * Returns specific annotation instance
	 * 
	 * @param instanceFragment
	 * @return
	 */
	static OWLNamedIndividual getOntologyAnnotationInstance(OWLOntology ontology,
			String instanceFragment) {
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

		OWLClass annotationClass = getOntologyClass(ontology,
				Fragments.ANNOTATION_CLASS);
		Set<OWLNamedIndividual> annotationInstances = reasoner.getInstances(
				annotationClass, false).getFlattened();
		OWLNamedIndividual requiredInstance = null;
		for (OWLNamedIndividual i : annotationInstances) {
			if (i.getIRI().getFragment().equals(instanceFragment)) {
				requiredInstance = i;
				break;
			}
		}
		return requiredInstance;
	}

	/**
	 * Returns specific ontology data property
	 * 
	 * @param propertyFragment
	 * @return
	 */
	static OWLDataProperty getOntologyDataProperty(OWLOntology ontology,
			String propertyFragment) {
		Set<OWLDataProperty> dataProperties = ontology
				.getDataPropertiesInSignature();
		OWLDataProperty requiredDataProperty = null;
		for (OWLDataProperty p : dataProperties) {
			if (p.getIRI().getFragment().equals(propertyFragment)) {
				requiredDataProperty = p;
				break;
			}
		}
		return requiredDataProperty;
	}

	/**
	 * Returns specific ontology object property
	 * 
	 * @param propertyFragment
	 * @return
	 */
	static OWLObjectProperty getOntologyObjectProperty(OWLOntology ontology,
			String propertyFragment) {
		Set<OWLObjectProperty> objectProperties = ontology
				.getObjectPropertiesInSignature();
		OWLObjectProperty requiredObjectProperty = null;
		for (OWLObjectProperty p : objectProperties) {
			if (p.getIRI().getFragment().equals(propertyFragment)) {
				requiredObjectProperty = p;
				break;
			}
		}
		return requiredObjectProperty;
	}
	
	/**
	 * Return related instances of selected instance
	 * 
	 * @param ontology
	 * @param i
	 * @return
	 */
	static Set<OWLNamedIndividual> getRelatedIndividuals(OWLOntology ontology, OWLNamedIndividual i) {
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		Set<OWLObjectProperty> objPropSet = ontology.getObjectPropertiesInSignature();
		Set<OWLNamedIndividual> relatedIndSet = new HashSet<OWLNamedIndividual>();
		for (OWLObjectProperty p : objPropSet) {
			Set<OWLNamedIndividual> prop = reasoner.getObjectPropertyValues(i, p).getFlattened();
			if (prop == null) {
				continue;
			} else {
				for (OWLNamedIndividual j : prop ) {
					relatedIndSet.add(j);
				}
			}
		}
		return relatedIndSet;
		
	}
}
