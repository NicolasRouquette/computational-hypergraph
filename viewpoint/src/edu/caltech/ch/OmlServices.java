package edu.caltech.ch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.opencaesar.oml.AnnotationProperty;
import io.opencaesar.oml.Classifier;
import io.opencaesar.oml.ConceptInstance;
import io.opencaesar.oml.ConceptInstanceReference;
import io.opencaesar.oml.Description;
import io.opencaesar.oml.LinkAssertion;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.NamedInstanceReference;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.RelationEntity;
import io.opencaesar.oml.RelationInstance;
import io.opencaesar.oml.RelationInstanceReference;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlSearch;
import io.opencaesar.oml.util.OmlSwitch;

/**
 * The services class used by VSM.
 */
public class OmlServices {
    
    public static boolean isKindOf(NamedInstance instance, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), kindName);
		return (kind != null) ? OmlSearch.findIsKindOf(instance, kind) : true;
	}

    public static boolean isTypeOf(NamedInstance instance, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), kindName);
		return (kind != null) ? OmlSearch.findIsTypeOf(instance, kind) : false;
	}

    public static boolean isKindOf(LinkAssertion link, String relationName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(link.getOntology(), relationName);
		return (relation != null) ? link.getRelation() == relation : false;
	}

    public static String getAnnotationValue(NamedInstance instance, String propertyName) {
    	var property = (AnnotationProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), propertyName);
		var literal = (property != null) ? OmlRead.getAnnotationValue(instance, property) : null;
		return (literal != null) ? OmlRead.getStringValue(literal) : null;
	}

    public static boolean hasAnnotationValue(NamedInstance instance, String propertyName) {
    	var property = (AnnotationProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), propertyName);
		var literal = (property != null) ? OmlRead.getAnnotationValue(instance, property) : null;
		return literal != null;
	}


    public static String getScalarValueIfAny(NamedInstance instance, String prefix, String propertyName) {
    	var property = (ScalarProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), propertyName);
		var literal = (property != null) ? (Literal) OmlRead.getPropertyValue(instance, property) : null;
		return (literal != null) ? prefix+OmlRead.getStringValue(literal) : null;
	}
    
    public static String getScalarValue(NamedInstance instance, String propertyName) {
    	var property = (ScalarProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), propertyName);
		var literal = (property != null) ? (Literal) OmlRead.getPropertyValue(instance, property) : null;
		return (literal != null) ? OmlRead.getStringValue(literal) : null;
	}

    public static boolean hasScalarValue(NamedInstance instance, String propertyName) {
    	var property = (ScalarProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), propertyName);
		var literal = (property != null) ? (Literal) OmlRead.getPropertyValue(instance, property) : null;
		return literal != null;
	}

    public static String getTooltip(NamedInstance instance) {
    	return instance.getAbbreviatedIri();
    }

    public static NamedInstance resolveInstance(NamedInstanceReference reference) {
    	return (NamedInstance) OmlRead.resolve(reference);
    }

    public static NamedInstance getSource(LinkAssertion link) {
		return OmlRead.getSource(link);
	}

    public static NamedInstance getTarget(LinkAssertion link) {
		return OmlRead.getTarget(link);
	}

    public static NamedInstance getSource(RelationInstance instance) {
		return !instance.getSources().isEmpty() ? instance.getSources().get(0) : null;
	}

    public static NamedInstance getTarget(RelationInstance instance) {
		return !instance.getTargets().isEmpty() ? instance.getTargets().get(0) : null;
	}

    public static List<NamedInstance> getRelatedTargets(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), relationyName);
		return (relation != null) ? OmlSearch.findInstancesRelatedFrom(instance, relation) : Collections.emptyList();
	}

    public static List<NamedInstance> getAllRelatedTargets(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), relationyName);
		return (relation != null) ? OmlRead.closure(instance, true, i -> OmlSearch.findInstancesRelatedFrom(i, relation)) : Collections.emptyList();
    }
    
    public static List<NamedInstance> getRelatedSources(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), relationyName);
		return (relation != null) ? OmlSearch.findInstancesRelatedTo(instance, relation) : Collections.emptyList();
	}

    public static List<NamedInstance> getAllRelatedSources(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), relationyName);
		return (relation != null) ? OmlRead.closure(instance, true, i -> OmlSearch.findInstancesRelatedTo(i, relation)) : Collections.emptyList();
    }

    public static List<NamedInstance> getAllRelatedSourcesAndTargets(NamedInstance instance, String relationyName) {
    	final HashSet<NamedInstance> instances = new HashSet<>();
    	instances.addAll(getAllRelatedSources(instance, relationyName));
    	instances.addAll(getAllRelatedTargets(instance, relationyName));
    	final List<NamedInstance> result = new ArrayList<>();
    	result.addAll(instances);
    	return result;
    }
    
    public static List<RelationInstance> getRelationInstancesWithSource(NamedInstance instance, String relationyEntityName) {
    	final var relationEntity = (RelationEntity) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), relationyEntityName);
		List<RelationInstance> instances = OmlSearch.findRelationInstancesWithSource(instance);
		instances.removeIf(i -> !OmlSearch.findTypes(i).contains(relationEntity));
		return instances;
	}

    public static List<RelationInstance> getRelationInstancesWithTarget(NamedInstance instance, String relationyEntityName) {
    	final var relationEntity = (RelationEntity) OmlRead.getMemberByAbbreviatedIri(instance.getOntology(), relationyEntityName);
		List<RelationInstance> instances = OmlSearch.findRelationInstancesWithTarget(instance);
		instances.removeIf(i -> !OmlSearch.findTypes(i).contains(relationEntity));
		return instances;
	}

    public static List<NamedInstance> getOwnedRootNamedInstancesOfKind(Description description, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.getOntology(), kindName);
    	var bContains = (Relation) OmlRead.getMemberByAbbreviatedIri(description.getOntology(), "base:contains");
		
    	return description.getOwnedStatements().stream()
    			.filter(i -> 
    			i instanceof NamedInstance && 
    			OmlSearch.findIsTypeOf((NamedInstance) i, kind) &&
    			OmlSearch.findInstancesRelatedTo((NamedInstance) i, bContains).isEmpty())
    			.map(i -> (NamedInstance)i)
    			.collect(Collectors.toList());
    }
    
    public static List<NamedInstance> getOwnedNamedInstances(Description description) {
    	return description.getOwnedStatements().stream()
    			.filter(i -> i instanceof NamedInstance)
    			.map(i -> (NamedInstance)i)
    			.collect(Collectors.toList());
    }
    
    public static List<NamedInstanceReference> getOwnedNamedInstanceReferences(Description description) {
    	return description.getOwnedStatements().stream()
    			.filter(i -> i instanceof NamedInstanceReference)
    			.map(i -> (NamedInstanceReference)i)
    			.collect(Collectors.toList());
    }

    public static Set<NamedInstance> getNamedInstancesInContext(Description description, boolean includeImports) {
		var allDescriptions = new HashSet<Ontology>();
		allDescriptions.add(description);
		
		if (includeImports) {
    		allDescriptions.addAll(
    			OmlRead.getAllImportedOntologies(description, false).stream()
    				.filter(o -> o instanceof Description)
    				.collect(Collectors.toSet()));
    	}
    	
    	var instances = new LinkedHashSet<NamedInstance>();
    	var visitor = new OmlSwitch<Void>() {
			@Override
			public Void caseLinkAssertion(LinkAssertion object) {
				instances.add(object.getTarget());
				return null;
			}
			@Override
			public Void caseConceptInstance(ConceptInstance object) {
				instances.add(object);
				return null;
			}
			@Override
			public Void caseConceptInstanceReference(ConceptInstanceReference object) {
				instances.add((ConceptInstance)OmlRead.resolve(object));
				return null;
			}
			@Override
			public Void caseRelationInstance(RelationInstance object) {
				instances.add(object);
				instances.addAll(object.getSources());
				instances.addAll(object.getTargets());
				return null;
			}
			@Override
			public Void caseRelationInstanceReference(RelationInstanceReference object) {
				instances.add((RelationInstance)OmlRead.resolve(object));
				return null;
			}
    	};
    	allDescriptions.forEach(d -> d.eAllContents().forEachRemaining(i -> visitor.doSwitch(i)));
    	return instances;
	}
    
}
