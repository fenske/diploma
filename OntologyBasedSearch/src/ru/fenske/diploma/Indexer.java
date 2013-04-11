package ru.fenske.diploma;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class Indexer {
	
	private static Indexer instance;
	
	private OWLOntologyManager ontologyManager;	
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	private String ontologyIRI;
	
	private Indexer(String ontologyIRI) throws OWLOntologyCreationException {
		this.ontologyIRI = ontologyIRI;
		ontologyManager = OWLManager.createOWLOntologyManager();
		ontologyManager.addIRIMapper(new AutoIRIMapper(new File(""), true));		 
		ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create(ontologyIRI));		 
		dataFactory = OWLManager.getOWLDataFactory();
	}
	
	public void indexDocuments() throws OWLOntologyCreationException, SQLException, OWLOntologyStorageException {		
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
		reasoner.precomputeInferences();
		OWLClass documentClass = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Document"));					
		Set<OWLNamedIndividual> documentIndividuals = reasoner.getInstances(documentClass, false).getFlattened();
		List<String> documentIndividualsNames = new ArrayList<String>();
		for (OWLNamedIndividual i : documentIndividuals) {
			documentIndividualsNames.add(i.getIRI().getFragment());
		}		
		List<Document> dbDocumentList = getDocumentList();
		OWLDataProperty name = dataFactory.getOWLDataProperty(IRI.create(ontologyIRI + "#name"));
		OWLDataProperty author = dataFactory.getOWLDataProperty(IRI.create(ontologyIRI + "#author"));
		OWLDataProperty uri = dataFactory.getOWLDataProperty(IRI.create(ontologyIRI + "#uri"));
		
		for (Document d : dbDocumentList) {
			if (!documentIndividualsNames.contains(d.getName())) {
				OWLNamedIndividual i = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + d.getName()));
				OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(documentClass, i);
				ontologyManager.addAxiom(ontology, axiom);
				Set<OWLAxiom> dataAxioms = new LinkedHashSet<OWLAxiom>();
				dataAxioms.add(dataFactory.getOWLDataPropertyAssertionAxiom(name, i, d.getName()));
				dataAxioms.add(dataFactory.getOWLDataPropertyAssertionAxiom(author, i, d.getAuthor()));
				dataAxioms.add(dataFactory.getOWLDataPropertyAssertionAxiom(uri, i, d.getUri()));
				ontologyManager.addAxioms(ontology, dataAxioms);
			}
		}
		ontologyManager.saveOntology(ontology, IRI.create(ontologyIRI));
	}
	
	private List<Document> getDocumentList() throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/enterprise";;
		Properties properties = new Properties();
		properties.setProperty("user", "postgres");
		properties.setProperty("password", "1234");	
		Connection connection = DriverManager.getConnection(url, properties);
		try {
			Statement select = connection.createStatement();
			ResultSet rs = select.executeQuery("select * from documents");
			List<Document> documentList = new ArrayList<Document>();
			while(rs.next()) {
				int docId = rs.getInt("id");
				String docName = rs.getString("name");
				String docAuthor = rs.getString("author");
				String docUri = rs.getString("uri");
				Document document = new Document(docId, docName, docAuthor, docUri);
				documentList.add(document);
			}
			return documentList;
		} finally {
			connection.close();
		}
	}
	
	public static Indexer getInstance() throws OWLOntologyCreationException {
		if (instance == null) {
			instance = new Indexer("file:///D:/Soft/web_eclipse/ontology/doc.owl");
		}
		return instance;
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException {
		Indexer i = Indexer.getInstance();
		i.indexDocuments();
		
	}
}
