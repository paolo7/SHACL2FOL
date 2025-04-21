package converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import actions.Action;
import filters.Filter;
import filters.FilterImpl;
import logic.FOL_Encoder;
import paths.IRI_Path;
import paths.PropertyPath;
import paths.List_Path;
import paths.UnaryPathExpression;
import paths.PathType;

public class ShapeReader {

	public static String sh_prefix = "http://www.w3.org/ns/shacl#";
	
	public static String SPARQL_sh_prefix = "PREFIX sh:  <"+sh_prefix+">\n";
	
	public static IRI freshIRIbase = Values.iri("shacl2fol:FreshIRI");
	public static IRI rdf_type = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	// RDF list terms
	public static IRI rdf_first = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
	public static IRI rdf_rest = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
	public static IRI rdf_nil = Values.iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
	
	public static IRI sh_BlankNode = Values.iri(sh_prefix+"BlankNode");
	public static IRI sh_IRI = Values.iri(sh_prefix+"IRI");
	public static IRI sh_Literal = Values.iri(sh_prefix+"Literal");
	public static IRI sh_BlankNodeOrIRI = Values.iri(sh_prefix+"BlankNodeOrIRI");
	public static IRI sh_BlankNodeOrLiteral = Values.iri(sh_prefix+"BlankNodeOrLiteral");
	public static IRI sh_IRIOrLiteral = Values.iri(sh_prefix+"IRIOrLiteral");
	
	private static IRI sh_NodeShape = Values.iri(sh_prefix+"NodeShape");
	private static IRI sh_PropertyShape = Values.iri(sh_prefix+"PropertyShape");
	// SHACL properties
	private static IRI sh_path = Values.iri(sh_prefix+"path");
	private static IRI sh_alternativePath = Values.iri(sh_prefix+"alternativePath");
	private static IRI sh_inversePath = Values.iri(sh_prefix+"inversePath");
	private static IRI sh_zeroOrMorePath = Values.iri(sh_prefix+"zeroOrMorePath");
	private static IRI sh_oneOrMorePath = Values.iri(sh_prefix+"oneOrMorePath");
	private static IRI sh_zeroOrOnePath = Values.iri(sh_prefix+"zeroOrOnePath");
	// SHACL target properties
	private static IRI sh_targetNode = Values.iri(sh_prefix+"targetNode");
	private static IRI sh_targetClass = Values.iri(sh_prefix+"targetClass");
	private static IRI sh_targetSubjectsOf = Values.iri(sh_prefix+"targetSubjectsOf");
	private static IRI sh_targetObjectsOf = Values.iri(sh_prefix+"targetObjectsOf");
	// SHACL constraint properties
	private static IRI sh_class = Values.iri(sh_prefix+"class");
	private static IRI sh_hasValue = Values.iri(sh_prefix+"hasValue");
	private static IRI sh_in = Values.iri(sh_prefix+"in");
	private static IRI sh_datatype = Values.iri(sh_prefix+"datatype");
	public static IRI sh_nodeKind = Values.iri(sh_prefix+"nodeKind");
	private static IRI sh_minExclusive = Values.iri(sh_prefix+"minExclusive");
	private static IRI sh_maxExclusive = Values.iri(sh_prefix+"maxExclusive");
	private static IRI sh_minInclusive = Values.iri(sh_prefix+"minInclusive");
	private static IRI sh_maxInclusive = Values.iri(sh_prefix+"maxInclusive");
	private static IRI sh_maxLength = Values.iri(sh_prefix+"maxLength");
	private static IRI sh_minLength = Values.iri(sh_prefix+"minLength");
	private static IRI sh_pattern = Values.iri(sh_prefix+"pattern");
	private static IRI sh_languageIn = Values.iri(sh_prefix+"languageIn");
	private static IRI sh_not = Values.iri(sh_prefix+"not");
	private static IRI sh_and = Values.iri(sh_prefix+"and");
	private static IRI sh_or = Values.iri(sh_prefix+"or");
	private static IRI sh_node = Values.iri(sh_prefix+"node");
	private static IRI sh_property = Values.iri(sh_prefix+"property");
	
	private static IRI sh_uniqueLang = Values.iri(sh_prefix+"uniqueLang");
	private static IRI sh_minCount = Values.iri(sh_prefix+"minCount");
	private static IRI sh_maxCount = Values.iri(sh_prefix+"maxCount");
	private static IRI sh_equals = Values.iri(sh_prefix+"equals");
	private static IRI sh_disjoint = Values.iri(sh_prefix+"disjoint");
	private static IRI sh_lessThan = Values.iri(sh_prefix+"lessThan");
	private static IRI sh_lessThanOrEquals = Values.iri(sh_prefix+"lessThanOrEquals");
	
	private static IRI sh_qualifiedValueShape = Values.iri(sh_prefix+"qualifiedValueShape");
	private static IRI sh_qualifiedMinCount = Values.iri(sh_prefix+"qualifiedMinCount");
	private static IRI sh_qualifiedMaxCount = Values.iri(sh_prefix+"qualifiedMaxCount");
	private static IRI sh_closed = Values.iri(sh_prefix+"closed");
	private static IRI sh_ignoredProperties = Values.iri(sh_prefix+"ignoredProperties");
	
	public static String actionBeginTag = "@@ab@@->";
	public static String actionEndTag = "<-@@ae@@";	
	
	private Set<IRI> shacl_properties = null;
	
	private RepositoryConnection conn = null;
	private RepositoryConnection connData = null;
	
	Set<Value> knownConstants = new HashSet<Value>();
	
	public Set<Filter> filters = new HashSet<Filter>();
	
	Set<Resource> knownIRIs = new HashSet<Resource>();
	
	private List<Action> actions;
	
	public ShapeReader(RepositoryConnection conn) {
		this(conn, null);
	}
	
	public ShapeReader(RepositoryConnection conn, RepositoryConnection connData) {
		this(conn, connData, null);
	}
	public ShapeReader(RepositoryConnection conn, RepositoryConnection connData, List<Action> actions) {
		replaceBlankNodesInRepository(conn);
		replaceBlankNodesInRepository(connData);
		shacl_properties = new HashSet<IRI>();
		shacl_properties.add(sh_path);
		shacl_properties.add(sh_targetNode);
		shacl_properties.add(sh_targetClass);
		shacl_properties.add(sh_targetSubjectsOf);
		shacl_properties.add(sh_targetObjectsOf);
		
		shacl_properties.add(sh_class);
		shacl_properties.add(sh_hasValue);
		shacl_properties.add(sh_in);
		
		shacl_properties.add(sh_datatype);
		shacl_properties.add(sh_nodeKind);
		shacl_properties.add(sh_minExclusive);
		shacl_properties.add(sh_maxExclusive);
		shacl_properties.add(sh_minInclusive);
		shacl_properties.add(sh_maxInclusive);
		shacl_properties.add(sh_maxLength);
		shacl_properties.add(sh_minLength);
		
		shacl_properties.add(sh_pattern);
		shacl_properties.add(sh_languageIn);
		shacl_properties.add(sh_not);
		shacl_properties.add(sh_and);
		shacl_properties.add(sh_or);
		shacl_properties.add(sh_node);
		shacl_properties.add(sh_property);
		
		shacl_properties.add(sh_uniqueLang);
		shacl_properties.add(sh_minCount);
		shacl_properties.add(sh_maxCount);
		shacl_properties.add(sh_equals);
		shacl_properties.add(sh_disjoint);
		shacl_properties.add(sh_lessThan);
		shacl_properties.add(sh_lessThanOrEquals);
		shacl_properties.add(sh_qualifiedValueShape);
		shacl_properties.add(sh_qualifiedMinCount);
		shacl_properties.add(sh_qualifiedMaxCount);
		shacl_properties.add(sh_closed);
		shacl_properties.add(sh_ignoredProperties);
		
		this.actions = actions;
		
		this.conn = conn;
		this.connData = connData;
		
		knownConstants.addAll(getAllConstants());
		knownIRIs.addAll(getAllIRI());
	}
	
	public String getDebugPrint() {
		String print = "";
		Set<Resource> nodeshapes = getNodeShapes(conn);
		Set<Resource> propertyshapes = getPropertyShapes();
		print += "Node Shapes:\n";
		for(Resource r : nodeshapes)
			print += "- "+r+":\n";
		print += "\nProperty Shapes:\n";
		for(Resource r : propertyshapes)
			print += "- "+r+":\n";
		return print;
	}
	
	// a characted indicating the type of encoding
	// 's' for satisfiability
	// 'c' for containment
	// 'v' for validation
	private Character mode = null;
	private boolean isSAT() {
		return mode.equals('s');
	}
	private boolean isCON() {
		return mode.equals('c');
	}
	private boolean isVAL() {
		return mode.equals('v');
	}
	private boolean isASV() {
		return mode.equals('a');
	}
	
	
	
	

    private static final String BASE_IRI_BLANK_NODE_SHAPE = "https://github.com/paolo7/SHACL2FOL/bnodeToIRI/";
    private static final ValueFactory vf = SimpleValueFactory.getInstance();


    /**
     * Replaces all blank nodes in all graphs in the repository with freshly minted IRIs.
     * Modifies the data in-place (original statements are removed, replaced with updated ones).
     *
     * @param conn the RepositoryConnection to modify
     */
    public static void replaceBlankNodesInRepository(RepositoryConnection conn) {
        // Load all statements
        List<Statement> allStatements = new ArrayList<>();
        conn.getStatements(null, null, null, true).forEachRemaining(allStatements::add);

        // SHACL-related IRIs
        IRI NODE_SHAPE = vf.createIRI("http://www.w3.org/ns/shacl#NodeShape");
        IRI PROPERTY_SHAPE = vf.createIRI("http://www.w3.org/ns/shacl#PropertyShape");

        // List of SHACL core constraint component predicates
        Set<IRI> shaclConstraintPredicates = Set.of(
        	    vf.createIRI("http://www.w3.org/ns/shacl#class"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#datatype"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#nodeKind"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#minCount"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#maxCount"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#minExclusive"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#minInclusive"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#maxExclusive"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#maxInclusive"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#minLength"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#maxLength"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#pattern"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#flags"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#languageIn"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#uniqueLang"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#equals"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#disjoint"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#lessThan"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#lessThanOrEquals"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#not"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#and"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#xone"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#or"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#node"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#property"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#qualifiedValueShape"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#qualifiedMinCount"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#qualifiedMaxCount"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#qualifiedValueShapesDisjoint"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#closed"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#ignoredProperties"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#hasValue"),
        	    vf.createIRI("http://www.w3.org/ns/shacl#in"),     	    
        	    vf.createIRI("http://www.w3.org/ns/shacl#value")
        	);

        // Identify blank nodes to replace
        Set<BNode> bNodesToReplace = new HashSet<>();

        for (Statement st : allStatements) {
            Resource subj = st.getSubject();
            IRI pred = st.getPredicate();
            Value obj = st.getObject();

            // Case 1: subject is a blank node that is declared as NodeShape or PropertyShape
            if (subj instanceof BNode && pred.equals(RDF.TYPE)
                    && (obj.equals(NODE_SHAPE) || obj.equals(PROPERTY_SHAPE))) {
                bNodesToReplace.add((BNode) subj);
            }

            // Case 2: subject is a blank node used in a SHACL constraint component
            if (subj instanceof BNode && shaclConstraintPredicates.contains(pred)) {
                bNodesToReplace.add((BNode) subj);
            }
        }

        // Map for replacement
        Map<BNode, IRI> bNodeMap = new HashMap<>();

        List<Statement> newStatements = new ArrayList<>();

        for (Statement st : allStatements) {
            Resource subj = st.getSubject();
            IRI pred = st.getPredicate();
            Value obj = st.getObject();
            Resource context = st.getContext();

            // Replace subject if it's a tracked blank node
            if (subj instanceof BNode && bNodesToReplace.contains(subj)) {
                subj = bNodeMap.computeIfAbsent((BNode) subj,
                        b -> vf.createIRI(BASE_IRI_BLANK_NODE_SHAPE + UUID.randomUUID()));
            }

            // Replace object if it's a tracked blank node
            if (obj instanceof BNode && bNodesToReplace.contains(obj)) {
                obj = bNodeMap.computeIfAbsent((BNode) obj,
                        b -> vf.createIRI(BASE_IRI_BLANK_NODE_SHAPE + UUID.randomUUID()));
            }

            newStatements.add(vf.createStatement(subj, pred, obj, context));
        }

        // Optional: clear original data
        conn.clear();

        // Add updated statements
        conn.add(newStatements);
    }
    /*public static void replaceBlankNodesInRepository(RepositoryConnection conn) {
        // Get all statements (including contexts)
        List<Statement> allStatements = new ArrayList<>();
        conn.getStatements(null, null, null, true).forEachRemaining(allStatements::add);

        // Map to track replacements for blank nodes
        Map<BNode, IRI> bNodeMap = new HashMap<>();

        // Prepare transformed statements
        List<Statement> newStatements = new ArrayList<>();

        for (Statement st : allStatements) {
            Resource subject = st.getSubject();
            Value object = st.getObject();
            IRI predicate = st.getPredicate();
            Resource context = st.getContext();

            // Replace subject if it's a blank node
            if (subject instanceof BNode) {
                subject = bNodeMap.computeIfAbsent((BNode) subject,
                        b -> vf.createIRI(BASE_IRI_BLANK_NODE_SHAPE + UUID.randomUUID()));
            }

            // Replace object if it's a blank node
            if (object instanceof BNode) {
                object = bNodeMap.computeIfAbsent((BNode) object,
                        b -> vf.createIRI(BASE_IRI_BLANK_NODE_SHAPE + UUID.randomUUID()));
            }

            newStatements.add(vf.createStatement(subject, predicate, object, context));
        }

        // Remove original statements
        conn.clear(); // optional: clears everything, including named graphs

        // Add updated statements
        conn.add(newStatements);
    }*/
    
    
    
    
	public void convert(FOL_Encoder encoder, char mode) throws RDFParseException, RepositoryException, IOException {
		this.mode = mode;
		Set<Resource> allshapes = getShapes();
		Set<Resource> nodeshapesOne = getNodeShapes(conn);
		Set<Resource> propertyshapesOne = getPropertyShapes(conn);
		Set<Resource> secondDocumentShapes = null;
		if(isCON() || isASV())
			secondDocumentShapes = getShapes(connData);
		convert_targets(allshapes,secondDocumentShapes,encoder);
		
		convert_constraints(nodeshapesOne, true, encoder, conn);
		convert_constraints(propertyshapesOne, false, encoder, conn);
		
		if(isCON() || isASV()) {
			if(isASV())
				encoder.writeComment(actionBeginTag);
			Set<Resource> nodeshapesTwo = getNodeShapes(connData);
			Set<Resource> propertyshapesTwo = getPropertyShapes(connData);
			convert_constraints(nodeshapesTwo, true, encoder, connData);
			convert_constraints(propertyshapesTwo, false, encoder, connData);
			if(isASV())
				encoder.writeComment(actionEndTag);
		}
		axiomatiseFilters(encoder);
		
		// this needs to be done before UNA encoding as the actions might introduce new constants
		if(isASV())
			encoder.applyActions(actions, this);
		
		encodeUNA(encoder);
		
		if(connData != null && isVAL()) {
			convert_data_graph(encoder);
		}

	}
	
	private void convert_data_graph(FOL_Encoder encoder) {
		Map<Resource,Set<Pair<Value,Value>>> dataGraph = new HashMap<Resource,Set<Pair<Value,Value>>>();
		String queryString = "SELECT DISTINCT ?s ?p ?o WHERE { ?s ?p ?o }";
		TupleQuery q = connData.prepareTupleQuery(queryString);
		try (TupleQueryResult result = q.evaluate()) {
			for (BindingSet solution : result) {
				Value s = solution.getValue("s");
				Resource p = (Resource) solution.getValue("p");
				Value o = solution.getValue("o");
				if(!dataGraph.containsKey(p))
					dataGraph.put(p, new HashSet<Pair<Value,Value>>());
				dataGraph.get(p).add(new Pair<Value,Value>(s,o));
			}
		}
		encoder.encodeDataGraph(dataGraph);
	}
	
	public void axiomatiseFilters(FOL_Encoder encoder) {
		encoder.axiomatiseFilters(filters, knownConstants);
	}
	
	public PropertyPath parsePropertyPath(Resource s, RepositoryConnection c) {
		Set<Value> path_results = getAllValueObjectsOf(s, sh_path, c);
		if(path_results.size() != 1)
			throw new WrongNumberOfPathAttributesException();
		Value path = path_results.iterator().next();
		
		return parsePathNode(path, c);
	}
	
	private boolean isRDFCollection(Resource r, RepositoryConnection c) {
		return getObjectOfBlankNode(r, rdf_first, c)!= null;
	}
	private boolean isAlternativePath(Resource r, RepositoryConnection c) {
		return getObjectOfBlankNode(r, sh_alternativePath, c) != null;
	}
	private boolean isInversePath(Resource r, RepositoryConnection c) {
		return getObjectOfBlankNode(r, sh_inversePath, c)!= null;
	}
	private boolean isZeroOrMorePath(Resource r, RepositoryConnection c) {
		return getObjectOfBlankNode(r, sh_zeroOrMorePath, c)!= null;
	}
	private boolean isOneOrMorePath(Resource r, RepositoryConnection c) {
		return getObjectOfBlankNode(r, sh_oneOrMorePath, c)!= null;
	}
	private boolean isZeroOrOnePath(Resource r, RepositoryConnection c) {
		return getObjectOfBlankNode(r, sh_zeroOrOnePath, c)!= null;
	}
	private List<Value> getRDFList(Resource r, RepositoryConnection c) {
		Model m = new LinkedHashModel();
		RepositoryResult<Statement> results = c.getStatements(null, null, null);
		while(results.hasNext()) {
			m.add(results.next());
		}
		//m.addAll(c.getStatements(null, null, null).asList());
		List<Value> collection = new LinkedList<Value>();
		RDFCollections.asValues(m, r,collection);
		return collection;
	}
	private List<Resource> getRDFResourceList(Resource r, RepositoryConnection c) {
		List<Value> values = getRDFList(r, c);
		List<Resource> collection = new LinkedList<Resource>();
		for(Value v : values) {
			collection.add((Resource) v);
		}
		return collection;
	}
	
	private Value getObjectOfBlankNode(Resource r, IRI predicate, RepositoryConnection c) {
		Model m = new LinkedHashModel();
		RepositoryResult<Statement> results = c.getStatements(null, null, null);
		while(results.hasNext()) {
			m.add(results.next());
		}
		//m.addAll(c.getStatements(null, null, null).asList());
		Model matchedTriples = m.filter(r, predicate, null);
		for (Statement st : matchedTriples) {
			return st.getObject();
		}
		return null;
	}
	
	private PropertyPath parsePathNode(Value path, RepositoryConnection c) {
		if(path.isLiteral())
			throw new LiteralAsPathException();
		if(path.isIRI()) 
			return new IRI_Path((IRI) path);
		else {
			Resource p = (Resource) path;
			if(isRDFCollection(p, c)) {
				List<Value> list = getRDFList(p, c);
				List<PropertyPath> pathList = new LinkedList<PropertyPath>();
				for(Value v : list) {
					pathList.add(parsePathNode(v, c));
				}
				return new List_Path(pathList,PathType.SEQUENCE);
			} else if(isAlternativePath(p, c)){
				List<Value> list = getRDFList(getAllResourceObjectsOf(p, sh_alternativePath, c).iterator().next(), c);
				List<PropertyPath> pathList = new LinkedList<PropertyPath>();
				for(Value v : list) {
					pathList.add(parsePathNode(v, c));
				}
				return new List_Path(pathList,PathType.ALTERNATIVE);
			} else {
				Resource p_object;
				PathType type = null;
				if(isZeroOrMorePath(p, c)) {
					p_object = (Resource) getObjectOfBlankNode(p, sh_zeroOrMorePath, c);
					type = PathType.ZEROORMORE;
				}
				else if(isZeroOrOnePath(p, c)) {
					p_object = (Resource) getObjectOfBlankNode(p, sh_zeroOrOnePath, c);
					type = PathType.ZEROORONE;
				}
				else if(isOneOrMorePath(p, c)) {
					p_object = (Resource) getObjectOfBlankNode(p, sh_oneOrMorePath, c);
					type = PathType.ONEORMORE;
				}
				else if(isInversePath(p, c)) {
					p_object = (Resource) getObjectOfBlankNode(p, sh_inversePath, c);
					type = PathType.INVERSE;
				}
				else throw new UnkownPathTypeException();
				return new UnaryPathExpression(parsePathNode(p_object, c), type);
			}
		}
	}

	public void convert_constraints(Set<Resource> shapes, boolean isNodeShape, FOL_Encoder encoder, RepositoryConnection c) {
		for(Resource s : shapes) {
			PropertyPath path = null;
			if(!isNodeShape) {
				path = parsePropertyPath(s, c);
			}
			
			Set<Value> hasValueValues = getAllValueObjectsOf(s,sh_hasValue, c);
			for(Value v : hasValueValues)
				encoder.addHasValueConstraint(s,v,path);
			
			Set<Resource> hasInValues = getAllResourceObjectsOf(s,sh_in, c);
			for(Resource v : hasInValues) {
				List<Value> values = getRDFList(v, c);
				encoder.addHasInConstraint(s,values,path);
			}
			
			Set<Value> hasClassValues = getAllValueObjectsOf(s,sh_class, c);
			for(Value v : hasClassValues)
				encoder.addHasClassConstraint(s,v,path);
			
			//TODO datatype
			
			//TODO nodeKind
			Set<Resource> hasNodeKindValues = getAllResourceObjectsOf(s,sh_nodeKind, c);
			for(Resource v : hasNodeKindValues) {
				String testQuery = SPARQL_sh_prefix+ "ASK {\n" + 
						"	FILTER ((isIRI($value) && <"+v.stringValue()+"> IN ( sh:IRI, sh:BlankNodeOrIRI, sh:IRIOrLiteral ) ) ||\n" + 
						"		(isLiteral($value) && <"+v.stringValue()+"> IN ( sh:Literal, sh:BlankNodeOrLiteral, sh:IRIOrLiteral ) ) ||\n" + 
						"		(isBlank($value)   && <"+v.stringValue()+"> IN ( sh:BlankNode, sh:BlankNodeOrIRI, sh:BlankNodeOrLiteral ) )) .\n" + 
						"}";
				if(!v.equals(sh_IRI) && !v.equals(sh_Literal) && !v.equals(sh_BlankNode) 
						&& !v.equals(sh_BlankNodeOrIRI) && ! v.equals(sh_BlankNodeOrLiteral) &&
						! v.equals(sh_IRIOrLiteral))
					throw new RuntimeException("Wrong object for nodeKind constraint: "+v);
				Filter nodeKindFilter = new FilterImpl(testQuery,sh_nodeKind,v);
				filters.add(nodeKindFilter);
				encoder.encodeFilter(s,path,nodeKindFilter);
				//boolean t = nodeKindFilter.test(sh_alternativePath, conn);
				//boolean k =true;
			}
			
			//TODO minExclusive ... maxInclusive
			
			//TODO maxLength minLength
			
			//TODO languageIn
			
			Set<Resource> notValues = getAllResourceObjectsOf(s,sh_not, c);
			for(Resource r : notValues)
				encoder.addNotConstraint(s,r,path);
			
			Set<Resource> andValues = getAllResourceObjectsOf(s,sh_and, c);
			for(Resource r : andValues) {
				List<Resource> resources = getRDFResourceList(r, c);
				encoder.addAndConstraint(s,resources,path);
			}
			
			Set<Resource> orValues = getAllResourceObjectsOf(s,sh_or, c);
			for(Resource r : orValues) {
				List<Resource> resources = getRDFResourceList(r, c);
				encoder.addOrConstraint(s,resources,path);
			}
			
			Set<Resource> nodeValues = getAllResourceObjectsOf(s,sh_node, c);
			for(Resource r : nodeValues)
				encoder.addNodeConstraint(s,r,path);
			Set<Resource> propertyValues = getAllResourceObjectsOf(s,sh_property, c);
			for(Resource r : propertyValues)
				encoder.addPropertyConstraint(s,r,path);
			
			Set<Value> closed = getAllValueObjectsOf(s,sh_closed, c);
			if(closed.size() > 0 && ((Literal) closed.iterator().next()).booleanValue() == true) {
				Set<Resource> ignoredPropertiesList = getAllResourceObjectsOf(s,sh_ignoredProperties, c);
				Set<Resource> ignoredProperties = new HashSet<Resource>();
				if(ignoredPropertiesList.size() > 0)
					ignoredProperties.addAll(getRDFResourceList(ignoredPropertiesList.iterator().next(), c));
				Set<Resource> omega = new HashSet<Resource>();
				omega.addAll(knownIRIs);
				omega.add(freshIRIbase);
				omega.removeAll(ignoredProperties);
				omega.removeAll(getAllClosedPredicates(s, c));
				for(Resource r : omega) if(r.isIRI()){
					PropertyPath disallowedProperty = new IRI_Path((IRI)r);
					encoder.addCloseProperty(s,path,disallowedProperty);
				}
			}
			
			if(!isNodeShape) {
				
				//TODO uniqueLang
				
				Set<Value> hasminCountValues = getAllValueObjectsOf(s,sh_minCount, c);
				for(Value v : hasminCountValues)
					encoder.addHasMinCountConstraint(s,v,path,false);
				Set<Value> hasmaxCountValues = getAllValueObjectsOf(s,sh_maxCount, c);
				for(Value v : hasmaxCountValues)
					encoder.addHasMinCountConstraint(s,v,path,true);
				
				Set<Resource> equalsValues = getAllResourceObjectsOf(s,sh_equals, c);
				for(Resource r : equalsValues)
					encoder.addEqualsConstraint(s,r,path);
				
				Set<Resource> disjointValues = getAllResourceObjectsOf(s,sh_disjoint, c);
				for(Resource r : disjointValues)
					encoder.addDisjointConstraint(s,r,path);
				
				//TODO lessThan
				
				//TODO lessThanOrEquals
				
				Set<Resource> hasqualifiedValueShapeValues = getAllResourceObjectsOf(s,sh_qualifiedValueShape, c);
				Set<Value> qualifiedMax = getAllValueObjectsOf(s,sh_qualifiedMaxCount, c);
				Set<Value> qualifiedMin = getAllValueObjectsOf(s,sh_qualifiedMinCount, c);
				if(hasqualifiedValueShapeValues.size() > 0) {
					if(qualifiedMax.size() > 1)
						throw new IncorrectlyConfiguredQualifiedValueShapeConstraintException();
					if(qualifiedMin.size() > 1)
						throw new IncorrectlyConfiguredQualifiedValueShapeConstraintException();
					if(qualifiedMax.size() == 1)
						encoder.addHasQualifiedMinCountConstraint(s,qualifiedMax.iterator().next(),path,true,hasqualifiedValueShapeValues);
					if(qualifiedMin.size() == 1)
						encoder.addHasQualifiedMinCountConstraint(s,qualifiedMin.iterator().next(),path,false,hasqualifiedValueShapeValues);
				}
			}
			encoder.finaliseShapeConstraint(s);
		}
	}
	
	public void convert_targets(Set<Resource> shapes, Set<Resource> secondDocumentShapes, FOL_Encoder encoder) {
		
		for(Resource s : shapes) {
			convert_node_targets(s,encoder,true);
			convert_class_targets(s,encoder,true);
			convert_subject_of_targets(s,encoder,true);
			convert_object_of_targets(s,encoder,true);
		}
		if(isCON() || isASV())
			for(Resource s : secondDocumentShapes) {
			convert_node_targets(s,encoder,false);
			convert_class_targets(s,encoder,false);
			convert_subject_of_targets(s,encoder,false);
			convert_object_of_targets(s,encoder,false);
		}
		if(isASV())
			encoder.writeComment(actionBeginTag);
		encoder.addNegatedTargetAxioms();
		if(isASV())
			encoder.writeComment(actionEndTag);
	}
	public void convert_node_targets(Resource s, FOL_Encoder encoder, boolean positive) {
		Set<Value> nodeTargets = getAllValueObjectsOf(s,sh_targetNode, (positive ? conn : connData));
		encoder.addNodeTargets(s,nodeTargets,positive);
	}
	public void convert_class_targets(Resource s, FOL_Encoder encoder, boolean positive) {
		Set<Resource> classTargets = getAllResourceObjectsOf(s,sh_targetClass, (positive ? conn : connData));
		encoder.addClassTargets(s,classTargets,positive);
	}
	public void convert_subject_of_targets(Resource s, FOL_Encoder encoder, boolean positive) {
		Set<Resource> classTargets = getAllResourceObjectsOf(s,sh_targetSubjectsOf, (positive ? conn : connData));
		encoder.addSubjectOfTargets(s,classTargets,false,positive);
	}
	public void convert_object_of_targets(Resource s, FOL_Encoder encoder, boolean positive) {
		Set<Resource> properties = getAllResourceObjectsOf(s,sh_targetObjectsOf, (positive ? conn : connData));
		encoder.addSubjectOfTargets(s,properties,true,positive);
	}
	
	// Encode the Unique Name Assumption: all known constants are different from each other
	public void encodeUNA(FOL_Encoder encoder){
		encoder.encodeUNA(knownConstants);
	}
	
	public Set<Resource> getShapes(){
		Set<Resource> shapes = getShapes(conn);
		if(isCON() || isASV())
			shapes.addAll(getShapes(connData));
		return shapes;
	}
	public Set<Resource> getShapes(RepositoryConnection c){

		Set<Resource> shapes = new HashSet<Resource>();
		
		String queryString = "SELECT DISTINCT ?shape WHERE {"
				+ "{?shape <"+rdf_type+"> <"+sh_NodeShape+">}"
				+ "UNION"
				+ "{?shape <"+rdf_type+"> <"+sh_PropertyShape+">}"
				+ "UNION"
				+ "{?s <"+sh_qualifiedValueShape+"> ?shape}";
		for (IRI sh_p : shacl_properties) {
			queryString += "UNION"
					+ "{ ?shape <"+sh_p+"> ?o }";
		}
		queryString += "}";
		
		TupleQuery q = c.prepareTupleQuery(queryString);
		
		try (TupleQueryResult result = q.evaluate()) {
			for (BindingSet solution : result) {
				shapes.add((Resource) solution.getValue("shape"));
			}
		}
		
		return shapes;
	}
	
	public Set<Resource> getPropertyShapes(){
		Set<Resource> shapes = getPropertyShapes(conn);
		if(isCON() || isASV())
			shapes.addAll(getPropertyShapes(connData));
		return shapes;
	}
	public Set<Resource> getPropertyShapes(RepositoryConnection c){

		Set<Resource> shapes = new HashSet<Resource>();
		
		String queryString = "SELECT DISTINCT ?shape WHERE {"
				+ "?shape <"+sh_path+"> ?o "
				+ "}";
		
		TupleQuery q = c.prepareTupleQuery(queryString);
		
		try (TupleQueryResult result = q.evaluate()) {
			for (BindingSet solution : result) {
				shapes.add((Resource) solution.getValue("shape"));
			}
		}
		
		return shapes;
	}
	
	public Set<Resource> getNodeShapes(RepositoryConnection c){
		Set<Resource> shapes = getShapes(c);
		shapes.removeAll(getPropertyShapes(c));
		return shapes;
	}
	
	private Set<Value> getAllConstants(){
		Set<Value> values = getAllConstants(conn);
		values.addAll(getAllConstants(connData));
		return values;
	}
	private Set<Value> getAllConstants(RepositoryConnection c){
		Set<Value> values = new HashSet<Value>();
		if(c == null)
			return values;
		String queryString = "SELECT DISTINCT ?s ?o WHERE {"
				+ "?s ?p ?o "
				+ "}";
		TupleQuery q = c.prepareTupleQuery(queryString);
		try (TupleQueryResult result = q.evaluate()) {
			for (BindingSet solution : result) {
				values.add(solution.getValue("s"));
				values.add(solution.getValue("o"));
			}
		}
		return values;
	}
	
	private Set<Resource> getAllIRI(){
		Set<Resource> values = getAllIRI(conn);
		values.addAll(getAllIRI(connData));
		return values;
	}
	private Set<Resource> getAllIRI(RepositoryConnection conn){
		Set<Resource> values = new HashSet<Resource>();
		if(conn == null) 
			return values;
		String queryString = "SELECT DISTINCT ?s ?p ?o WHERE {"
				+ "?s ?p ?o "
				+ "}";
		TupleQuery q = conn.prepareTupleQuery(queryString);
		try (TupleQueryResult result = q.evaluate()) {
			for (BindingSet solution : result) {
				if(solution.getValue("s").isIRI())
					values.add((Resource) solution.getValue("s"));
				values.add((Resource) solution.getValue("p"));
				if(solution.getValue("o").isIRI())
					values.add((Resource) solution.getValue("o"));
			}
		}
		return values;
	}
	
	private Set<Resource> getAllClosedPredicates(Resource subject, RepositoryConnection c){
		Set<Resource> res = new HashSet<Resource>();
		String queryString = "SELECT DISTINCT ?p WHERE {"
				+ (subject.isIRI() ? "<"+(subject)+">" : subject)+" <"+sh_property+"> ?prop . "
				+ " ?prop <"+sh_path+"> ?p"
				+ "}";
		TupleQuery q = c.prepareTupleQuery(queryString);
		try (TupleQueryResult result = q.evaluate()) {
			for (BindingSet solution : result) {
				res.add((Resource) solution.getValue("p"));
			}
		}
		return res;
	}
	
	//private Set<Value> getAllValueObjectsOf(Resource subject, Resource predicate){
	//	return getAllValueObjectsOf(subject, predicate, conn);
	//}
	private Set<Value> getAllValueObjectsOf(Resource subject, Resource predicate, RepositoryConnection c){
		Set<Value> values = new HashSet<Value>();
		String queryString = "SELECT DISTINCT ?o WHERE {"
				+ (subject.isIRI() ? "<"+(subject)+">" : subject)+" <"+predicate+"> ?o "
				+ "}";
		TupleQuery q = c.prepareTupleQuery(queryString);
		try (TupleQueryResult result = q.evaluate()) {
			for (BindingSet solution : result) {
				values.add(solution.getValue("o"));
			}
		}
		return values;
	}
	
	//private Set<Resource> getAllResourceObjectsOf(Resource subject, Resource predicate){
	//	return getAllResourceObjectsOf(subject, predicate, conn);
	//}

	private Set<Resource> getAllResourceObjectsOf(Resource subject, Resource predicate, RepositoryConnection c){
		Set<Value> values = getAllValueObjectsOf(subject,predicate, c);
		Set<Resource> resources = new HashSet<Resource>();
		for(Value v : values)
			if (v.isResource())
				resources.add((Resource) v);
		return resources;
	}
	
	private static String actionBaseURI = "https://github.com/paolo7/SHACL2FOL/mint/";
	
	public PropertyPath parseActionPath(String path) throws RDFParseException, RepositoryException, IOException {
		RepositoryConnection conn = connectToStringGraph("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
				+ "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
				+ "@prefix : <"+actionBaseURI+"> .\n"
				+ "\n"
				+ ":shape a sh:PropertyShape ;\n"
				+ "sh:path "+path+".\n");
		return parsePropertyPath(Values.iri(actionBaseURI+"shape"),conn);
	}
	
	private static String actionShapeBaseURI = "https://github.com/paolo7/SHACL2FOL/mint_a/ac_sh";
	private static int actionShapeBaseURIiterator = 0;
	
	public static void replaceIriInAllTriples(RepositoryConnection conn, IRI oldIri, IRI newIri) {
        List<Statement> updatedStatements = new ArrayList<>();
        SimpleValueFactory vf = SimpleValueFactory.getInstance();

        try (var iter = conn.getStatements(null, null, null, false)) {
            while (iter.hasNext()) {
                Statement st = iter.next();

                Value subj = st.getSubject().equals(oldIri) ? newIri : st.getSubject();
                Value pred = st.getPredicate().equals(oldIri) ? newIri : st.getPredicate();
                Value obj = st.getObject().equals(oldIri) ? newIri : st.getObject();

                Statement updated = vf.createStatement(
                    (org.eclipse.rdf4j.model.Resource) subj,
                    (IRI) pred,
                    obj,
                    st.getContext()
                );

                updatedStatements.add(updated);
            }
        }

        // Clear existing statements
        conn.clear();

        // Add updated statements
        conn.add(updatedStatements);
    }
	
	public IRI parseActionShape(String shape, FOL_Encoder encoder) throws RDFParseException, RepositoryException, IOException {
		RepositoryConnection conn = connectToStringGraph(shape);
		Set<Resource> nodeShapes = getNodeShapes(conn);
		Set<Resource> propertyShapes = getPropertyShapes(conn);
		Set<Resource> namedShapes = new HashSet<Resource>();
		for(Resource r: nodeShapes)
			if(r.isIRI())
				namedShapes.add(r);
		for(Resource r: propertyShapes)
			if(r.isIRI())
				namedShapes.add(r);
		if(namedShapes.size() != 1)
			throw new IOException("This shape is used as part of an action, but it does not meet the requirement of consisting of a single shape definition:\n"+shape);
		//String mainShape = namedShapes.iterator().next().stringValue();
		replaceBlankNodesInRepository(conn);
		actionShapeBaseURIiterator += 1;
		IRI newIRIforShape = Values.iri(actionShapeBaseURI+actionShapeBaseURIiterator);
		replaceIriInAllTriples(conn, (IRI) namedShapes.iterator().next(), newIRIforShape);
		nodeShapes = getNodeShapes(conn);
		propertyShapes = getPropertyShapes(conn);
		convert_constraints(nodeShapes, true, encoder, conn);
		convert_constraints(propertyShapes, false, encoder, conn);
		return newIRIforShape;
	}
	
	private RepositoryConnection connectToStringGraph(String graph) throws RDFParseException, RepositoryException, IOException {
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection conn = repo.getConnection();
		conn.add(new ByteArrayInputStream(graph.getBytes(StandardCharsets.UTF_8)),actionBaseURI, RDFFormat.TURTLE);
		return conn;
	}
	
}
