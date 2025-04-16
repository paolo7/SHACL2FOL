package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import actions.Action;
import converter.Config;
import converter.NegativeCountForCountingQuantifierException;
import converter.Pair;
import converter.ShapeReader;
import converter.UnkownPathTypeException;
import filters.Filter;
import paths.IRI_Path;
import paths.List_Path;
import paths.PathType;
import paths.PropertyPath;
import paths.UnaryPathExpression;

public class TPTP_Encoder implements FOL_Encoder {

	private Map<Value,String> dict_constants;
	
	private Map<String,String> dict_constraints;
	
	private Map<Resource,String> data_predicates;

	private List<Action> actions;
	
	private String tptp = "";
	
	public String getTptpPrefix() {
		if(Config.encodeUNA)
			return Config.tptpPrefix;
		else
			return "tff";
	}
	
	public TPTP_Encoder() {
		this(null);
	}
	
	public TPTP_Encoder(List<Action> actions) {
		dict_constraints = new HashMap<String,String>();
		dict_constants = new HashMap<Value,String>();
		data_predicates = new HashMap<Resource,String>();
		dict_constants.put(ShapeReader.rdf_type, "isA");
		this.actions = actions;
	}

	@Override
	public String getEncodingAsString() {
		return tptp;
	}
	
	public String lookup(Value v) {
		return lookup(v,'c');
	}
	
	/**
	 * Lookup (or create if dosen't exist) a string encoding for a constant or a predicate name
	 * @param v the RDF value to lookup
	 * @param p use a "c" prefix to denote constants of the domain, and "p" to denote predicate 
	 * @return
	 */
	public String lookup(Value v, char p) {
		String code = null;
		if (dict_constants.containsKey(v))
			code = p+"_"+dict_constants.get(v);
		else {
			code = p+"_"+mintConstant(v);
		}
		if(p == 'p') {
			data_predicates.put((Resource) v, code);
		}
		return code;
	}
	
	
	public String mintConstant(Value v) {
		String v_as_string = v.stringValue();
		int i = v_as_string.lastIndexOf("/");
		v_as_string = v_as_string.substring(i+1);
		String c = v_as_string.replaceAll("[^a-zA-Z]", "");
		if (dict_constants.containsValue(c))
			c += "_i";
		while (dict_constants.containsValue(c))
			c += "i";
		dict_constants.put(v, c);
		return c;
	}
	
	private String ls = "\n";
	private String indent = "  ";
	private String TRUE = "$true";
	private String FALSE = "$false";
	
	private int axiomCounter = 0;
	private String getAxiomName() {
		int i = axiomCounter;
		String alphab = toAlphabetic(i);
		axiomCounter++;
		return alphab;
		
	}
	// code adapted from https://stackoverflow.com/questions/10813154/how-do-i-convert-a-number-to-a-letter-in-java
	private static String toAlphabetic(int i) {
	    int quot = i/26;
	    int rem = i%26;
	    char letter = (char)((int)'A' + rem);
	    if( quot == 0 ) {
	        return ""+letter;
	    } else {
	        return toAlphabetic(quot-1) + letter;
	    }
	}
	
	private String negatedTargetAxioms = "";
	
	@Override
	public void addNegatedTargetAxioms() {
		tptp += ls+"% Negated Target Axioms: ";
		tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom, ";
		if(negatedTargetAxioms.length() > 0) {
			tptp += indent + "~ ( "+ negatedTargetAxioms;
			tptp += ls+indent + " )";
		} else {
			tptp += indent + "$false";
		}
		tptp += ls+ indent + ").";
	}
	
	@Override
	public void addNodeTargets(Resource s, Set<Value> values) {
		addNodeTargets(s, values, true);
	}
	@Override
	public void addNodeTargets(Resource s, Set<Value> values, boolean positiveTarget) {
		if(values.isEmpty())
			return;
		if(positiveTarget) {
			String shape = lookup(s,'s');
			tptp += ls+"% Target Nodes of Shape: "+s.stringValue();
			tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom, "+ls;
			tptp += indent;
			tptp += getNodeTargetAxiom(s, values);
			tptp += ls+ indent + ").";
		} else {
			if (negatedTargetAxioms.length() > 0) {
				negatedTargetAxioms += " &"; 
			}				
			negatedTargetAxioms += ls + indent + getNodeTargetAxiom(s, values);
		}
	}
	private String getNodeTargetAxiom(Resource s, Set<Value> values) {
		String axiom = "";
		String shape = lookup(s,'s');
		Iterator<Value> i = values.iterator();
		while (i.hasNext()) {
			Value v = i.next();
			String target = lookup(v);
			axiom += shape+"("+target+")";
			if (i.hasNext())
				axiom += " & ";
		}
		return axiom;
	}

	@Override
	public void addClassTargets(Resource s, Set<Resource> classes) {
		addClassTargets(s,classes, true);
	}
	@Override
	public void addClassTargets(Resource s, Set<Resource> classes, boolean positiveTarget) {
		if(classes.isEmpty())
			return;
		if(positiveTarget) {
			String shape = lookup(s,'s');
			tptp += ls+"% Class Targets of Shape: "+s.stringValue();
			tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
			tptp += getClassTargetsAxiom(s, classes);
			tptp += ls + indent + ").";
		} else {
			if (negatedTargetAxioms.length() > 0) {
				negatedTargetAxioms += " &"; 
			}	
			negatedTargetAxioms += ls + indent + getClassTargetsAxiom(s, classes);
		}
	}
	private String getClassTargetsAxiom(Resource s, Set<Resource> classes) {
		String axiom = "";
		String shape = lookup(s,'s');
		Iterator<Resource> i = classes.iterator();
		while (i.hasNext()) {
			Resource c = i.next();
			String class_s = lookup(c);
			axiom += ls+indent+"( ![X] : ( "+lookup(ShapeReader.rdf_type,'p')+"(X,"+class_s+") => "+shape+"(X) ) )";
			if (i.hasNext())
				axiom += " & ";
		}
		return axiom;
	}

	@Override
	public void addSubjectOfTargets(Resource s, Set<Resource> properties, boolean invertProperties) {
		addSubjectOfTargets(s, properties, invertProperties, true);
	}
	@Override
	public void addSubjectOfTargets(Resource s, Set<Resource> properties, boolean invertProperties, boolean positiveTarget) {
		if(properties.isEmpty())
			return;
		if(positiveTarget) {
			String shape = lookup(s,'s');
			tptp += ls+"% "+(invertProperties ? "Object" : "Subject")+"-Of Targets of Shape: "+s.stringValue();
			tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
			tptp += getSubjectOfTargetsAxioms(s, properties, invertProperties);
			tptp += ls+ indent + ").";
		} else {
			if (negatedTargetAxioms.length() > 0) {
				negatedTargetAxioms += " &"; 
			}	
			negatedTargetAxioms += ls + indent + getSubjectOfTargetsAxioms(s, properties, invertProperties);
		}
	}
	private String getSubjectOfTargetsAxioms(Resource s, Set<Resource> properties, boolean invertProperties){
		String axiom = "";
		String shape = lookup(s,'s');
		Iterator<Resource> i = properties.iterator();
		while (i.hasNext()) {
			Resource p = i.next();
			String property_s = lookup(p,'p');
			tptp += ls+indent+"( ![X,Y] : ( "+property_s+(invertProperties ? "(Y,X)" : "(X,Y)")+" => "+shape+"(X) ) )";
			if (i.hasNext())
				tptp += " & ";
		}
		return axiom;
	}

	@Override
	public void encodeUNA(Set<Value> constants) {
		Set<Value> nonBlankConstants = new HashSet<Value>();
		for(Value v : constants) if(!v.isBNode())
			nonBlankConstants.add(v);
		if(nonBlankConstants.size() <= 1)
			return;
		List<Value> orderedConstants = new ArrayList<Value>();
		orderedConstants.addAll(nonBlankConstants);
		tptp += ls+"% Unique Name Assumption Axiom. Total number of constants: "+nonBlankConstants.size();
		tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
		if(!Config.encodeUNA) {
			tptp += ls + indent+ "$distinct(";
		}
		for (int i = 0; i < orderedConstants.size()-1; i++) {
			if(!Config.encodeUNA) {
				String c = lookup(orderedConstants.get(i));
				tptp += c;
				if(i+1 < orderedConstants.size()-1)
					tptp += ", ";
			}
			else {
				tptp += ls + indent;
				for (int k = i+1; k < orderedConstants.size(); k++) {
					String c_one = lookup(orderedConstants.get(i));
					String c_two = lookup(orderedConstants.get(k));
					tptp += "~("+c_one+" = "+c_two+")";
					if(k+1 < orderedConstants.size())
						tptp += " & ";
				}
				if(i+1 < orderedConstants.size()-1)
					tptp += " & ";
			}
		}
		if(!Config.encodeUNA) {
			tptp += ")";
		}
		
		tptp += ls+ indent + ").";
	}
	
	private String encodeDisjoint(List<String> variables) {
		String encoding = "";
		for (int i = 0; i < variables.size()-1; i++) {
			for (int k = i+1; k < variables.size(); k++) {
				String v_one = variables.get(i);
				String v_two = variables.get(k);
				encoding += "~("+v_one+" = "+v_two+")";
				if(k+1 < variables.size())
					encoding += " & ";
			}
			if(i+1 < variables.size()-1)
				encoding += " & ";
		}
		return encoding;
	}

	private void addConstraint(String shape, String constraint) {
		if(!dict_constraints.containsKey(shape)) {
			dict_constraints.put(shape, "");
		}
		String updatedConstraint = dict_constraints.get(shape);
		if(updatedConstraint.length() > 0)
			updatedConstraint += ls + indent + " & ";
		updatedConstraint += "( "+constraint+" )";
		dict_constraints.put(shape, updatedConstraint);
	}

	
	private int recursive_counter = 0;
	private String encodeRecursivePath(PropertyPath path,String firstVar, String lastVar, boolean inverted) {
		String recursive_predicate = "rec_"+toAlphabetic(recursive_counter);
		recursive_counter++;
		
		tptp += ls+"% Encoding of recursive path n."+recursive_counter;
		tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
		tptp += ls+ indent + " ( ! [X, Y] : (";
		tptp += ls+ indent + indent + recursive_predicate+"(X,Y) <= " + encodePath(path,"X","Y",false);
		tptp += ls+ indent + indent + " ) )";
		tptp += ls+ indent + " & ( ! [X, Y, Z] : (";
		tptp += ls+ indent + indent + recursive_predicate+"(X,Z) <= " + recursive_predicate+"(X,Y) & " + encodePath(path,"Y","Z",false);
		tptp += ls+ indent + indent + " ) )";
		tptp += ls+ indent + " ).";
		
		return recursive_predicate + ( inverted ? "("+lastVar+", "+firstVar+")" : "("+firstVar+", "+lastVar+")");
	}
		
	private String encodePath(PropertyPath path,String firstVar, String lastVar, boolean inverted) {
		if(path.getType() == PathType.PREDICATE) {
			IRI p = ((IRI_Path) path).getIRI();
			String predicate = lookup(p,'p');
			return predicate + (inverted ? "("+lastVar+", "+firstVar+")" : "("+firstVar+", "+lastVar+")");
		}
		if(path.getType() == PathType.INVERSE) {
			return encodePath(((UnaryPathExpression) path).getPath(),firstVar, lastVar, ! inverted);
		}
		if(path.getType() == PathType.SEQUENCE || path.getType() == PathType.ALTERNATIVE) {
			List_Path list_path = (List_Path) path;
			List<PropertyPath> list = list_path.getPathList();
			String listEncoding = "";
			if(list_path.getType() == PathType.ALTERNATIVE) {
				for(PropertyPath p : list) {
					if(listEncoding.length() > 0)
						listEncoding += " | ";
					listEncoding += "("+encodePath(p,firstVar,lastVar,inverted)+")";
				}
			}
			if(list_path.getType() == PathType.SEQUENCE) {
				if(inverted) {
					Collections.reverse(list);
				}
				List<String> varNames = new LinkedList<String>();
				varNames.add(firstVar);
				for(int i = 0; i < list.size()-1; i++) {
					varNames.add(firstVar+"_"+toAlphabetic(i));
				}
				varNames.add(lastVar);
				if(varNames.size() > 2) {					
					listEncoding += " ? [";
					for(int i = 1; i < varNames.size()-1; i++) {
						if(i > 1)
							listEncoding += ", ";
						listEncoding += varNames.get(i);
					}
					listEncoding += "] : ";
				}
				listEncoding += " ( ";
				for(int i = 0; i < list.size(); i++) {
					PropertyPath p = list.get(i);
					String local_var_first = varNames.get(i);
					String local_var_last = varNames.get(i+1);
					if(i > 0)
						listEncoding += " & ";
					listEncoding += "("+encodePath(p,local_var_first,local_var_last,inverted)+")";
				}
				listEncoding += " ) ";
			}
			return listEncoding;
		}
		else {
			String unaryEncoding = "";
			UnaryPathExpression unary_path = (UnaryPathExpression) path;
			PropertyPath pathAttribute = unary_path.getPath();
			if(unary_path.getType() == PathType.ZEROORONE) {
				unaryEncoding += firstVar+" = "+lastVar + " | ("+encodePath(pathAttribute, firstVar, lastVar, inverted)+")" ;
				return unaryEncoding;
			}
			if(unary_path.getType() == PathType.ZEROORMORE) {
				unaryEncoding += firstVar+" = "+lastVar + " | ("+encodeRecursivePath(pathAttribute, firstVar, lastVar, inverted)+")" ;
				return unaryEncoding;
			}
			if(unary_path.getType() == PathType.ONEORMORE) {
				unaryEncoding += encodeRecursivePath(pathAttribute, firstVar, lastVar, inverted) ;
				return unaryEncoding;
			}
		}
		
			
		throw new UnkownPathTypeException();
	}

	@Override
	public void finaliseShapeConstraint(Resource s) {
		String shape = lookup(s,'s');
		tptp += ls+"% Constraints of shape "+s.stringValue();
		tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
		tptp += ls+ indent + "! [X] : ( "+shape+"(X) <=> ( ";
		if(!dict_constraints.containsKey(shape) || dict_constraints.get(shape).length() == 0)
			tptp += TRUE;
		else
			tptp += dict_constraints.get(shape);
		tptp += ls+ indent + ") ) ).";
		
	}

	@Override
	public void addHasValueConstraint(Resource s, Value v, PropertyPath path) {
		String shape = lookup(s,'s');
		String value = lookup(v,'c');
		if(path == null)
			addConstraint(shape, "X = "+value);
		else
			addConstraint(shape, "( ? [Y] : ( "+encodePath(path,"X","Y", false)+" & Y = "+value+" ) )");
	}
	
	@Override
	public void addHasClassConstraint(Resource s, Value v, PropertyPath path) {
		String shape = lookup(s,'s');
		String value = lookup(v,'c');
		String isA = lookup(ShapeReader.rdf_type,'p');
		String mainVar = "X";
		String constraint = "";
		if(path != null) {
			mainVar = "Y";
			constraint += "( ! [Y] : ( ("+encodePath(path,"X","Y", false)+") => ( ";
			
		}
		constraint += isA+"("+mainVar+", "+value+")";
		if(path != null) 
			constraint += ") ) )";
		addConstraint(shape, constraint);
	}

	@Override
	public void addHasInConstraint(Resource s, List<Value> values, PropertyPath path) {
		String shape = lookup(s,'s');
		String mainVar = "X";
		String constraint = "";
		if(path != null) {
			mainVar = "Y";
			constraint += "( ! [Y] : ( ("+encodePath(path,"X","Y", false)+") => ( ";
			
		}
		Iterator<Value> i = values.iterator();
		while(i.hasNext()) {
			String valueString = lookup(i.next(),'c');
			constraint += mainVar+" = "+valueString;
			if(i.hasNext())
				constraint += " | ";
		}
		if(path != null) 
			constraint += ") ) )";
		addConstraint(shape, constraint);
		
	}

	@Override
	public void addNotConstraint(Resource s, Resource r, PropertyPath path) {
		String shape = lookup(s,'s');
		String negatedShape = lookup(r,'s');
		String mainVar = "X";
		String constraint = "";
		if(path != null) {
			mainVar = "Y";
			constraint += "( ! [Y] : ( ("+encodePath(path,"X","Y", false)+") => ( ";
			
		}
		constraint += "~ ( "+negatedShape+"("+mainVar+") ) ";
		if(path != null) 
			constraint += ") ) )";
		addConstraint(shape, constraint);
	}

	@Override
	public void addAndConstraint(Resource s, List<Resource> resources, PropertyPath path) {
		addLogicConstraint(s,resources,path,"&");
	}

	@Override
	public void addOrConstraint(Resource s, List<Resource> resources, PropertyPath path) {
		addLogicConstraint(s,resources,path,"|");
	}
	
	public void addLogicConstraint(Resource s, List<Resource> resources, PropertyPath path, String connector) {
		String shape = lookup(s,'s');
		
		String mainVar = "X";
		String constraint = "";
		if(path != null) {
			mainVar = "Y";
			constraint += "( ! [Y] : ( ("+encodePath(path,"X","Y", false)+") => ( ";
			
		}
		Iterator<Resource> i = resources.iterator();
		while(i.hasNext()) {
			String resString = lookup(i.next(),'s');
			constraint += resString+"("+mainVar+")";
			if(i.hasNext())
				constraint += " "+connector+" ";
		}
		if(path != null) 
			constraint += ") ) )";
		addConstraint(shape, constraint);
		
	}

	@Override
	public void addNodeConstraint(Resource s, Resource r, PropertyPath path) {
		String shape = lookup(s,'s');
		String nodeShape = lookup(r,'s');
		String mainVar = "X";
		String constraint = "";
		if(path != null) {
			mainVar = "Y";
			constraint += "( ! [Y] : ( ("+encodePath(path,"X","Y", false)+") => ( ";
			
		}
		constraint += nodeShape+"("+mainVar+")";
		if(path != null) 
			constraint += ") ) )";
		addConstraint(shape, constraint);
		
	}

	@Override
	public void addPropertyConstraint(Resource s, Resource r, PropertyPath path) {
		addNodeConstraint(s,r,path);
	}

	private String varplaceholder = "#COUNTVAR#";
	
	private String countingQuantifierMin(int min, String baseVar, String sentence,Set<Resource> hasqualifiedValueShapeValues) {
		
		String constraint = "";
		List<String> vars = new LinkedList<String>();
		for(int i = 0; i < min; i++) {
			String varname = baseVar+"_C_"+toAlphabetic(i);
			vars.add(varname);
			if(i > 0)
				constraint += " & ";
			constraint += "("+sentence.replaceAll(varplaceholder, varname)+")";
		}
		if(vars.size()>1) {
			constraint += " & "+encodeDisjoint(vars);
		}
		if(hasqualifiedValueShapeValues != null) {
			for(Resource q : hasqualifiedValueShapeValues) {
				String shape_q = lookup(q,'s');
				for(int i = 0; i < min; i++) {
					String varname = baseVar+"_C_"+toAlphabetic(i);
					constraint += " & shape_q("+varname+")";
				}
			}
		}
		return constraint;
	}
	
	@Override
	public void addHasMinCountConstraint(Resource s, Value v, PropertyPath path, boolean isMax) {
		addHasQualifiedMinCountConstraint(s,v,path,isMax,null);
		/* String shape = lookup(s,'p');
		int limit = ((Literal) v).integerValue().intValue();
		if(limit == 0) {
			if(isMax) 			
				addConstraint(shape, FALSE);
			else 			
				addConstraint(shape, TRUE);
		} else if(limit <  0){
			throw new NegativeCountForCountingQuantifierException();
		} else {
			String quantification = "? [ ";
			for(int i = 0; i < (isMax ? limit+1 : limit); i++) {
				String varname = "X"+"_C_"+toAlphabetic(i);
				if(i>0)
					quantification += ", ";
				quantification += varname;
			}
			quantification += " ] : ";
			if(isMax) {
				String constraint = countingQuantifierMin(limit+1,"X",encodePath(path,"X",varplaceholder, false));		
				addConstraint(shape, " ~( "+quantification+"("+constraint+") )");
			} else {
				String constraint = countingQuantifierMin(limit,"X",encodePath(path,"X",varplaceholder, false));		
				addConstraint(shape, quantification+"("+constraint+")");
			}
		} */
	}

	@Override
	public void addHasQualifiedMinCountConstraint(Resource s, Value v, PropertyPath path, boolean isMax,
			Set<Resource> hasqualifiedValueShapeValues) {
		String shape = lookup(s,'s');
		int limit = ((Literal) v).integerValue().intValue();
		if(limit == 0) {
			if(isMax) 			
				addConstraint(shape, FALSE);
			else 			
				addConstraint(shape, TRUE);
		} else if(limit <  0){
			throw new NegativeCountForCountingQuantifierException();
		} else {
			String quantification = "? [ ";
			for(int i = 0; i < (isMax ? limit+1 : limit); i++) {
				String varname = "X"+"_C_"+toAlphabetic(i);
				if(i>0)
					quantification += ", ";
				quantification += varname;
			}
			quantification += " ] : ";
			if(isMax) {
				String constraint = countingQuantifierMin(limit+1,"X",encodePath(path,"X",varplaceholder, false),hasqualifiedValueShapeValues);		
				addConstraint(shape, " ~( "+quantification+"("+constraint+") )");
			} else {
				String constraint = countingQuantifierMin(limit,"X",encodePath(path,"X",varplaceholder, false),hasqualifiedValueShapeValues);		
				addConstraint(shape, quantification+"("+constraint+")");
			}
		} 
		
	}

	@Override
	public void addEqualsConstraint(Resource s, Resource r, PropertyPath path) {
		String shape = lookup(s,'s');
		PropertyPath simple_path = new IRI_Path((IRI) r);
		String constraint = "";
		constraint += " ! [Y] : ( ("+encodePath(path,"X","Y", false)+") <=> ("+encodePath(simple_path,"X","Y", false)+") ) ";
		addConstraint(shape, constraint);
		
	}

	@Override
	public void addDisjointConstraint(Resource s, Resource r, PropertyPath path) {
		String shape = lookup(s,'s');
		PropertyPath simple_path = new IRI_Path((IRI) r);
		String constraint = "";
		constraint += " ~ ( ? [Y] : ( ("+encodePath(path,"X","Y", false)+") & ("+encodePath(simple_path,"X","Y", false)+") ) )";
		addConstraint(shape, constraint);
		
	}

	@Override
	public void addCloseProperty(Resource s, PropertyPath path, PropertyPath disallowedProperty) {
		String shape = lookup(s,'s');
		String mainVar = "X";
		String constraint = "";
		if(path != null) {
			mainVar = "Y";
			constraint += "( ! [Y] : ( ("+encodePath(path,"X","Y", false)+") => ( ";
			
		}
		constraint += "~( ? [Z] : ("+encodePath(disallowedProperty,mainVar,"Z", false)+"))";
		
		if(path != null) 
			constraint += ") ) )";
		addConstraint(shape, constraint);
		
		
	}

	@Override
	public void encodeFilter(Resource s, PropertyPath path, Filter f) {
		String shape = lookup(s,'s');
		String mainVar = "X";
		String constraint = "";
		if(path != null) {
			mainVar = "Y";
			constraint += "( ! [Y] : ( ("+encodePath(path,"X","Y", false)+") => ( ";
			
		}
		constraint += getFilterPredicateName(f.getFilterPredicate(),f.getFilterObject())+"("+mainVar+")";
		if(path != null) 
			constraint += ") ) )";
		addConstraint(shape, constraint);
	}
	
	private String filterPrefix = "f_";
	private String fIRI = filterPrefix+"isIRI";
	private String fisLiteral = filterPrefix+"isLiteral";
	private String fisBlank = filterPrefix+"isBlank";
	private String fisBlankNodeOrIRI = filterPrefix+"isBlankNodeOrIRI";
	private String fisBlankNodeOrLiteral = filterPrefix+"isBlankNodeOrLiteral";
	private String fisIRIOrLiteral = filterPrefix+"isIRIOrLiteral";
	
	private String getFilterPredicateName(IRI fPredicate, Value fValue) {
		String fname = null;
		if(fPredicate.equals(ShapeReader.sh_nodeKind)) {
			if(fValue.equals(ShapeReader.sh_IRI))
				fname = fIRI;
			if(fValue.equals(ShapeReader.sh_Literal))
				fname = fisLiteral;
			if(fValue.equals(ShapeReader.sh_BlankNode))
				fname = fisBlank;
			if(fValue.equals(ShapeReader.sh_BlankNodeOrIRI))
				fname = fisBlankNodeOrIRI;
			if(fValue.equals(ShapeReader.sh_BlankNodeOrLiteral))
				fname = fisBlankNodeOrLiteral;
			if(fValue.equals(ShapeReader.sh_IRIOrLiteral))
				fname = fisIRIOrLiteral;
		}
		if (fname == null)
			throw new RuntimeException("Filter exception: "+fPredicate+" "+fValue);
		return fname;
	}


	@Override
	public void axiomatiseFilters(Set<Filter> filters, Set<Value> knownConstants) {
		if(filters.size() == 0)
			return;
		Repository repo = new SailRepository(new MemoryStore());
		RepositoryConnection conn = repo.getConnection();
		
		// axiomatise known constants:
		tptp += ls+"% Filter Axiomatisation of Constants: ";
		tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
		Set<String> usedFilterNames = new HashSet<String>();
		boolean first = true;
		for(Filter f : filters) {
			String filterName = getFilterPredicateName(f.getFilterPredicate(),f.getFilterObject());
			if(!usedFilterNames.contains(filterName)) {
				for(Value v : knownConstants) {
					if(!first)
						tptp += " & ";
					first = false;
					String constantName = lookup(v,'c');
					boolean filterTrue = f.test(v, conn);
					if(!filterTrue)
						tptp += "~(";
					tptp += filterName+"("+constantName+")";
					if(!filterTrue)
						tptp += ")";
				}
			}
			tptp += ls+ indent;
		}
		tptp += ls+ indent + ").";
		
		
		// check NodeType Filters
		boolean containsNodeType = false;
		for(Filter f : filters) {
			if(f.getFilterPredicate().equals(ShapeReader.sh_nodeKind)) {
				containsNodeType = true;
				break;
			}
		}
		if(containsNodeType) {
			tptp += ls+"% Filter Axiomatisation NodeKind: ";
			tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
			tptp += ls+ indent +"! [X] : ( ( ("+fIRI+"(X)) & ~("+fisLiteral+"(X)) & ~("+fisBlank+"(X)) ) ";
			tptp += ls+ indent +"| ( ~("+fIRI+"(X)) & ("+fisLiteral+"(X)) & ~("+fisBlank+"(X)) ) ";
			tptp += ls+ indent +"| ( ~("+fIRI+"(X)) & ~("+fisLiteral+"(X)) & ("+fisBlank+"(X)) ) )";
			tptp += ls+ indent + ").";
			tptp += ls+"% Filter Axiomatisation NodeKind Part Two: ";
			tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
			tptp += ls+ indent +"! [X] : ( ";
			tptp += ls+ indent + "( "+fisBlankNodeOrIRI+"(X) => ("+fisBlank+"(X)) | ("+fIRI+"(X)) ) ";
			tptp += ls+ indent + "& ( "+fisBlankNodeOrLiteral+"(X) => ("+fisBlank+"(X)) | ("+fisLiteral+"(X)) ) ";
			tptp += ls+ indent + "& ( "+fisIRIOrLiteral+"(X) => ("+fIRI+"(X)) | ("+fisLiteral+"(X)) ) ";
			tptp += ls+ indent + ") ).";
		}
		
	}


	@Override
	public void encodeDataGraph(Map<Resource, Set<Pair<Value, Value>>> dataGraph) {
		if(dataGraph.size() > 0) {
			tptp += ls+"% Data graph (positive part): ";
			tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
			boolean first = true;
			for(Resource p : dataGraph.keySet()) {
				String predicate = lookup(p,'p');
				tptp += ls+ indent;
				for(Pair<Value, Value> pair : dataGraph.get(p)) {
					String subject = lookup(pair.x,'c');
					String object = lookup(pair.y,'c');
					if(!first) tptp += " & ";
					first = false;
					tptp += predicate+"("+subject+", "+object+")";
				}
			}
			tptp += ls+ indent + ").";
		}
		if(data_predicates.size() > 0) {
			tptp += ls+"% Data graph (negative part): ";
			tptp += ls+getTptpPrefix()+"(axiom_"+getAxiomName()+",axiom,";
			boolean firstPredicate = true;
			for(Resource p : data_predicates.keySet()) {
				String predicate = lookup(p,'p');
				tptp += ls+ indent + (firstPredicate ? "" : "& ") + "(";
				firstPredicate = false;
				tptp += "![X,Y] : ( "+predicate+"(X,Y) => (";
				boolean firstTriple = true;
				for(Pair<Value, Value> pair : dataGraph.get(p)) {
					if(!firstTriple) tptp += " | ";
					firstTriple = false;
					String subject = lookup(pair.x,'c');
					String object = lookup(pair.y,'c');
					tptp += "(X = "+subject+" & Y = "+object+")";
				}
				tptp += "))";
				tptp += ")";
			}
			tptp += ls+ indent + ").";
		}
		
		
	}



}
