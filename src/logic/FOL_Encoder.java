package logic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import converter.Pair;
import filters.Filter;
import paths.PropertyPath;

public interface FOL_Encoder {

	public String getEncodingAsString();
	
	public void addNodeTargets(Resource s, Set<Value> values);

	public void addClassTargets(Resource s, Set<Resource> classTargets);

	public void addSubjectOfTargets(Resource s, Set<Resource> properties, boolean invertProperties);
	
	public void addNodeTargets(Resource s, Set<Value> values, boolean positiveTarget);

	public void addClassTargets(Resource s, Set<Resource> classTargets, boolean positiveTarget);

	public void addSubjectOfTargets(Resource s, Set<Resource> properties, boolean invertProperties, boolean positiveTarget);

	public void encodeUNA(Set<Value> constants);

	public void addHasValueConstraint(Resource s, Value v, PropertyPath path);

	public void finaliseShapeConstraint(Resource s);

	public void addHasClassConstraint(Resource s, Value v, PropertyPath path);

	public void addHasInConstraint(Resource s, List<Value> values, PropertyPath path);

	public void addNotConstraint(Resource s, Resource r, PropertyPath path);

	public void addAndConstraint(Resource s, List<Resource> resources, PropertyPath path);

	public void addOrConstraint(Resource s, List<Resource> resources, PropertyPath path);

	public void addNodeConstraint(Resource s, Resource r, PropertyPath path);

	public void addPropertyConstraint(Resource s, Resource r, PropertyPath path);

	public void addHasMinCountConstraint(Resource s, Value v, PropertyPath path, boolean isMax);

	public void addHasQualifiedMinCountConstraint(Resource s, Value v, PropertyPath path, boolean isMax,
			Set<Resource> hasqualifiedValueShapeValues);

	public void addEqualsConstraint(Resource s, Resource r, PropertyPath path);

	public void addDisjointConstraint(Resource s, Resource r, PropertyPath path);

	public void addCloseProperty(Resource s, PropertyPath disallowedProperty, PropertyPath disallowedProperty2);

	public void encodeFilter(Resource s, PropertyPath path, Filter nodeKindFilter);

	public void axiomatiseFilters(Set<Filter> filters, Set<Value> knownConstants);

	public void encodeDataGraph(Map<Resource, Set<Pair<Value, Value>>> dataGraph);

	public void addNegatedTargetAxioms();

}
